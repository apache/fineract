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
package org.apache.fineract.command.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.command.async.implementation.AsyncCommandDispatcher;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandlerManager;
import org.apache.fineract.command.core.CommandHookAfter;
import org.apache.fineract.command.core.CommandHookBefore;
import org.apache.fineract.command.core.CommandHookError;
import org.apache.fineract.command.implementation.DefaultCommandHookManager;
import org.junit.jupiter.api.Test;

/**
 * How the async dispatcher honours what the before hooks ask of it, and what it hands the error hooks.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class AsyncCommandDispatcherTest {

    private final List<String> calls = new CopyOnWriteArrayList<>();
    private final AtomicInteger handled = new AtomicInteger();
    private final IllegalStateException failure = new IllegalStateException("handler failed");

    private final CommandHandlerManager handler = new CommandHandlerManager() {

        @Override
        public <Q, S> S handle(Command<Q> command) {
            handled.incrementAndGet();
            throw failure;
        }
    };

    @Test
    void skipExecutionReturnsTheHookResponseWithoutRunningTheHandlerOrTheAfterHooks() {
        CommandHookBefore answering = ctx -> {
            ctx.setResponse("replayed");
            ctx.setSkipExecution(true);
        };
        var dispatcher = dispatcher(List.of(answering), List.of(ctx -> calls.add("after")), List.of());

        Object result = dispatcher.dispatch(new Command<>()).get();

        assertEquals("replayed", result);
        assertEquals(0, handled.get());
        assertTrue(calls.isEmpty(), "The after hooks must not run: " + calls);
    }

    @Test
    void errorHooksGetTheExceptionTheHandlerThrewNotTheCompletionWrapper() {
        List<Throwable> errors = new CopyOnWriteArrayList<>();
        var dispatcher = dispatcher(List.of(), List.of(), List.of(ctx -> errors.add(ctx.getError())));

        assertThrows(RuntimeException.class, () -> dispatcher.dispatch(new Command<>()).get());

        assertEquals(1, errors.size());
        assertSame(failure, errors.get(0));
    }

    private AsyncCommandDispatcher dispatcher(List<CommandHookBefore> before, List<CommandHookAfter> after, List<CommandHookError> error) {
        return new AsyncCommandDispatcher(handler, new DefaultCommandHookManager(before, after, error), new AsyncCommandProperties());
    }
}
