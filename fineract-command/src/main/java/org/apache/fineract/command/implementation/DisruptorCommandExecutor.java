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
package org.apache.fineract.command.implementation;

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
import org.apache.fineract.command.core.CommandAuditor;
import org.apache.fineract.command.core.CommandRouter;

@Slf4j
// TODO: not ready yet for prime time
// @Component
// @ConditionalOnProperty(value = "fineract.command.executor", havingValue = "disruptor")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DisruptorCommandExecutor extends BaseCommandExecutor implements Closeable {

    private final Disruptor<CommandEvent> disruptor;

    public DisruptorCommandExecutor(Disruptor<CommandEvent> disruptor, CommandRouter router, CommandAuditor auditor) {
        super(router, auditor);
        this.disruptor = disruptor;
    }

    @Override
    public <REQ, RES> Supplier<RES> execute(Command<REQ> command) {
        CommandEvent<REQ, RES> processedEvent = next(command);

        var future = processedEvent.getFuture().whenComplete((response, t) -> {
            if (t == null) {
                auditor.processed(command, response);
            } else {
                command.setError(t.getMessage());

                auditor.error(command);
            }
        });

        return future::join;
    }

    @Override
    public void close() throws IOException {
        disruptor.shutdown();
    }

    @SuppressWarnings({ "unchecked" })
    private <REQ, RES> CommandEvent<REQ, RES> next(Command<REQ> command) {
        var ringBuffer = disruptor.getRingBuffer();

        var sequenceId = ringBuffer.next();

        CommandEvent<REQ, RES> event = ringBuffer.get(sequenceId);
        event.setCommand(command);
        ringBuffer.publish(sequenceId);

        return event;
    }

    @Getter
    @Setter
    public static class CommandEvent<REQ, RES> {

        private Command<REQ> command;
        private CompletableFuture<RES> future = new CompletableFuture<>();
    }

    @RequiredArgsConstructor
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static class CompleteableCommandEventHandler implements EventHandler<CommandEvent> {

        private final CommandRouter router;

        @Override
        public void onEvent(CommandEvent event, long sequence, boolean endOfBatch) {
            try {
                var handler = router.route(event.getCommand());

                event.getFuture().complete(handler.handle(event.getCommand()));
            } catch (Exception e) {
                event.getFuture().completeExceptionally(e);
            }
        }
    }
}
