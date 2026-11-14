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

import java.util.List;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandHookAfter;
import org.apache.fineract.command.core.CommandHookBefore;
import org.apache.fineract.command.core.CommandHookError;
import org.apache.fineract.command.core.CommandHookManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnMissingBean(value = CommandHookManager.class, ignored = DefaultCommandHookManager.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DefaultCommandHookManager implements CommandHookManager {

    private final List<CommandHookBefore> beforeHooks;
    private final List<CommandHookAfter> afterHooks;
    private final List<CommandHookError> errorHooks;

    @Override
    public void before(CommandContext<?, ?> ctx) {
        run("pre", beforeHooks, ctx, CommandHookBefore::onBefore);
    }

    @Override
    public void after(CommandContext<?, ?> ctx) {
        run("post", afterHooks, ctx, CommandHookAfter::onAfter);
    }

    @Override
    public void error(CommandContext<?, ?> ctx) {
        run("error", errorHooks, ctx, CommandHookError::onError);
    }

    private <H> void run(String phase, List<H> hooks, CommandContext ctx, BiConsumer<H, CommandContext> invoke) {
        try {
            for (var hook : hooks) {
                invoke.accept(hook, ctx);

                if (ctx.isSkipHooks()) {
                    log.debug("Skipping remaining {} hooks after: {}", phase, hook.getClass().getCanonicalName());

                    return;
                }
            }
        } finally {
            // skipHooks stops the rest of this phase only. It is cleared here, also when a hook throws, so that it
            // never carries over: a before hook that sets it and then throws must not silence the error hooks
            ctx.setSkipHooks(false);
        }
    }
}
