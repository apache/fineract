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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandHookAfter;
import org.apache.fineract.command.core.CommandHookBefore;
import org.apache.fineract.command.core.CommandHookError;
import org.apache.fineract.command.implementation.DefaultCommandHookManager;
import org.apache.fineract.command.test.sample.command.DummyCommand;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.junit.jupiter.api.Test;

/**
 * {@code skipHooks} is how a hook stops the rest of its phase. It must stop exactly that phase and nothing else: it is
 * cleared when the phase ends, also when a hook throws, so that the next phase starts with every hook in it.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class DefaultCommandHookManagerTest {

    private final List<String> calls = new ArrayList<>();

    @Test
    void runsEveryHookOfAPhaseInOrder() {
        var manager = manager(List.of(before("b1"), before("b2")), List.of(after("a1"), after("a2")), List.of(error("e1"), error("e2")));
        var ctx = context();

        manager.before(ctx);
        manager.after(ctx);
        manager.error(ctx);

        assertEquals(List.of("b1", "b2", "a1", "a2", "e1", "e2"), calls);
    }

    @Test
    void skipHooksStopsTheRestOfThePhaseAndIsClearedAfterwards() {
        CommandHookBefore skipping = ctx -> {
            calls.add("b1");
            ctx.setSkipHooks(true);
        };
        var manager = manager(List.of(skipping, before("b2")), List.of(after("a1")), List.of());
        var ctx = context();

        manager.before(ctx);

        assertEquals(List.of("b1"), calls, "The hook after the one that set skipHooks must not run.");
        assertFalse(ctx.isSkipHooks(), "skipHooks applies to one phase only.");

        manager.after(ctx);

        assertEquals(List.of("b1", "a1"), calls, "The next phase runs in full.");
    }

    @Test
    void skipHooksIsClearedWhenTheHookThatSetItThrows() {
        CommandHookBefore skippingThenFailing = ctx -> {
            ctx.setSkipHooks(true);
            throw new IllegalStateException("refused");
        };
        var manager = manager(List.of(skippingThenFailing), List.of(), List.of(error("e1"), error("e2")));
        var ctx = context();

        assertThrows(IllegalStateException.class, () -> manager.before(ctx));
        assertFalse(ctx.isSkipHooks(), "A before hook that throws must not leave the error phase silenced.");

        manager.error(ctx);

        assertEquals(List.of("e1", "e2"), calls);
    }

    @Test
    void skipHooksStopsTheErrorPhaseLikeAnyOther() {
        CommandHookError skipping = ctx -> {
            calls.add("e1");
            ctx.setSkipHooks(true);
        };
        var manager = manager(List.of(), List.of(), List.of(skipping, error("e2")));
        var ctx = context();

        manager.error(ctx);

        assertEquals(List.of("e1"), calls);
        assertFalse(ctx.isSkipHooks());
    }

    private static DefaultCommandHookManager manager(List<CommandHookBefore> before, List<CommandHookAfter> after,
            List<CommandHookError> error) {
        return new DefaultCommandHookManager(before, after, error);
    }

    private static CommandContext<DummyRequest, Object> context() {
        return CommandContext.<DummyRequest, Object>builder().command(new DummyCommand()).build();
    }

    private CommandHookBefore before(String name) {
        return ctx -> calls.add(name);
    }

    private CommandHookAfter after(String name) {
        return ctx -> calls.add(name);
    }

    private CommandHookError error(String name) {
        return ctx -> calls.add(name);
    }
}
