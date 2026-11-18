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
package org.apache.fineract.command.disruptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandHandlerManager;
import org.apache.fineract.command.core.CommandHookAfter;
import org.apache.fineract.command.core.CommandHookBefore;
import org.apache.fineract.command.core.CommandHookError;
import org.apache.fineract.command.disruptor.implementation.DisruptorCommandDispatcher.CommandEvent;
import org.apache.fineract.command.disruptor.implementation.DisruptorCommandDispatcher.CompleteableCommandEventHandler;
import org.apache.fineract.command.implementation.DefaultCommandHookManager;
import org.apache.fineract.command.test.sample.command.DummyCommand;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.junit.jupiter.api.Test;

/**
 * How the disruptor's event handler honours what the before hooks ask of it.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class CompleteableCommandEventHandlerTest {

    private final List<String> calls = new ArrayList<>();
    private final AtomicInteger handled = new AtomicInteger();

    private final CommandHandlerManager handler = new CommandHandlerManager() {

        @Override
        public <Q, S> S handle(Command<Q> command) {
            handled.incrementAndGet();
            return (S) "handled";
        }
    };

    @Test
    void skipExecutionCompletesWithTheHookResponseWithoutRunningTheHandlerOrTheAfterHooks() throws Exception {
        CommandHookBefore answering = ctx -> {
            ctx.setResponse("replayed");
            ctx.setSkipExecution(true);
        };
        var event = event();

        eventHandler(List.of(answering), List.of(ctx -> calls.add("after")), List.of()).onEvent(event, 0L, true);

        assertEquals("replayed", event.getFuture().get());
        assertEquals(0, handled.get());
        assertTrue(calls.isEmpty(), "The after hooks must not run: " + calls);
    }

    @Test
    void aFailingBeforeHookCompletesTheFutureExceptionallyAfterTheErrorHooks() {
        var refused = new IllegalStateException("refused");
        CommandHookBefore refusing = ctx -> {
            ctx.setSkipExecution(true);
            ctx.setSkipHooks(true);
            throw refused;
        };
        var event = event();

        eventHandler(List.of(refusing), List.of(), List.of(ctx -> calls.add("error"))).onEvent(event, 0L, true);

        var thrown = assertThrows(ExecutionException.class, () -> event.getFuture().get());
        assertSame(refused, thrown.getCause());
        assertEquals(List.of("error"), calls);
        assertEquals(0, handled.get());
    }

    private static CommandEvent<DummyRequest, Object> event() {
        var event = new CommandEvent<DummyRequest, Object>();
        event.setContext(CommandContext.<DummyRequest, Object>builder().command(new DummyCommand()).build());
        event.setFuture(new CompletableFuture<>());
        return event;
    }

    private CompleteableCommandEventHandler eventHandler(List<CommandHookBefore> before, List<CommandHookAfter> after,
            List<CommandHookError> error) {
        return new CompleteableCommandEventHandler(new DefaultCommandHookManager(before, after, error), handler);
    }
}
