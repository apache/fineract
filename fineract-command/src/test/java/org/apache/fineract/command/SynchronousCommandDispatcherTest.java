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
package org.apache.fineract.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandlerManager;
import org.apache.fineract.command.core.CommandHookAfter;
import org.apache.fineract.command.core.CommandHookBefore;
import org.apache.fineract.command.core.CommandHookError;
import org.apache.fineract.command.core.CommandState;
import org.apache.fineract.command.implementation.DefaultCommandHookManager;
import org.apache.fineract.command.implementation.SynchronousCommandDispatcher;
import org.apache.fineract.command.test.sample.command.DummyCommand;
import org.junit.jupiter.api.Test;

/**
 * How the synchronous dispatcher honours what the before hooks ask of it.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class SynchronousCommandDispatcherTest {

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
    void skipExecutionReturnsTheHookResponseWithoutRunningTheHandlerOrTheAfterHooks() {
        CommandHookBefore answering = ctx -> {
            ctx.setResponse("replayed");
            ctx.setSkipExecution(true);
            ctx.setSkipHooks(true);
        };
        var dispatcher = dispatcher(List.of(answering, ctx -> calls.add("before")), List.of(ctx -> calls.add("after")), List.of());

        Object result = dispatcher.dispatch(new DummyCommand()).get();

        assertEquals("replayed", result);
        assertEquals(0, handled.get(), "A command whose execution is skipped must not reach its handler.");
        assertTrue(calls.isEmpty(), "Neither the remaining before hooks nor the after hooks run: " + calls);
    }

    @Test
    void skipExecutionWithoutSkipHooksStillRunsTheRemainingBeforeHooks() {
        CommandHookBefore answering = ctx -> {
            ctx.setResponse("replayed");
            ctx.setSkipExecution(true);
        };
        var dispatcher = dispatcher(List.of(answering, ctx -> calls.add("before")), List.of(ctx -> calls.add("after")), List.of());

        Object result = dispatcher.dispatch(new DummyCommand()).get();

        assertEquals("replayed", result);
        assertEquals(0, handled.get());
        assertEquals(List.of("before"), calls);
    }

    @Test
    void errorHooksRunAfterABeforeHookThatSkippedAndThrew() {
        var refused = new IllegalStateException("refused");
        CommandHookBefore refusing = ctx -> {
            ctx.setSkipExecution(true);
            ctx.setSkipHooks(true);
            throw refused;
        };
        List<Throwable> errors = new ArrayList<>();
        var dispatcher = dispatcher(List.of(refusing), List.of(), List.of(ctx -> errors.add(ctx.getError()), ctx -> calls.add("error2")));

        var thrown = assertThrows(IllegalStateException.class, () -> dispatcher.dispatch(new DummyCommand()).get());

        assertSame(refused, thrown);
        assertEquals(List.of(refused), errors, "The error hooks see the exception the hook threw.");
        assertEquals(List.of("error2"), calls, "skipHooks set in the before phase must not silence the error phase.");
    }

    @Test
    void everyCallOfTheSupplierStartsFromAFreshContext() {
        List<Object> seen = new ArrayList<>();
        CommandHookBefore recording = ctx -> {
            seen.add(ctx.getState());
            seen.add(ctx.getResponse());
        };
        var dispatcher = dispatcher(List.of(recording), List.of(), List.of());
        var supplier = dispatcher.dispatch(new DummyCommand());

        supplier.get();
        supplier.get();

        assertEquals(CommandState.UNDER_PROCESSING, seen.get(2), "The second call must not see the first call's state.");
        assertNull(seen.get(3), "The second call must not see the first call's response.");
    }

    private SynchronousCommandDispatcher dispatcher(List<CommandHookBefore> before, List<CommandHookAfter> after,
            List<CommandHookError> error) {
        return new SynchronousCommandDispatcher(handler, new DefaultCommandHookManager(before, after, error));
    }
}
