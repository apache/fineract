/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.command.disruptor.implementation;

import static java.util.Objects.requireNonNull;
import static org.apache.fineract.command.core.CommandState.PROCESSED;
import static org.apache.fineract.command.disruptor.DisruptorCommandConstants.COMMAND_DISRUPTOR_PROPERTY_ENABLED;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.command.core.CommandHandlerManager;
import org.apache.fineract.command.core.CommandHookManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// TODO: WIP - not ready yet for prime time
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = COMMAND_DISRUPTOR_PROPERTY_ENABLED, havingValue = "true")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DisruptorCommandDispatcher implements CommandDispatcher, Closeable {

    private final Disruptor<CommandEvent> disruptor;

    @Override
    public <REQ, RES> Supplier<RES> dispatch(Command<REQ> command) {
        requireNonNull(command, "Command must not be null");

        var future = next(CommandContext.<REQ, RES>builder().command(command).build());

        return future::join;
    }

    @Override
    public void close() throws IOException {
        disruptor.shutdown();
    }

    @EventListener(ApplicationStartedEvent.class)
    void onStartup() {
        disruptor.start();
    }

    /**
     * Ring buffer slots are reused, so each command needs a fresh future. It has to be created and handed back to the
     * caller <i>before</i> the slot is published: from that moment the slot belongs to the consumer, which may already
     * have completed and replaced anything read from it afterwards.
     */
    @SuppressWarnings({ "unchecked" })
    private <REQ, RES> CompletableFuture<RES> next(CommandContext<REQ, RES> ctx) {
        var ringBuffer = disruptor.getRingBuffer();

        var sequenceId = ringBuffer.next();

        try {
            CommandEvent<REQ, RES> event = ringBuffer.get(sequenceId);
            var future = new CompletableFuture<RES>();

            event.setContext(ctx);
            event.setFuture(future);

            return future;
        } finally {
            ringBuffer.publish(sequenceId);
        }
    }

    @Getter
    @Setter
    public static class CommandEvent<REQ, RES> {

        private CommandContext<REQ, RES> context;
        private CompletableFuture<RES> future;
    }

    @RequiredArgsConstructor
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static class CompleteableCommandEventHandler implements EventHandler<CommandEvent> {

        private final CommandHookManager hookManager;
        private final CommandHandlerManager handlerManager;

        @Override
        public void onEvent(CommandEvent event, long sequence, boolean endOfBatch) {
            final var ctx = event.getContext();

            try {
                hookManager.before(ctx);

                if (ctx.isSkipExecution()) {
                    event.getFuture().complete(ctx.getResponse());
                } else {
                    var response = handlerManager.handle(ctx.getCommand());

                    ctx.setResponse(response);
                    ctx.setState(PROCESSED);

                    hookManager.after(ctx);

                    event.getFuture().complete(ctx.getResponse());
                }
            } catch (Exception e) {
                ctx.setError(e);

                hookManager.error(ctx);

                event.getFuture().completeExceptionally(e);
            }
        }
    }
}
