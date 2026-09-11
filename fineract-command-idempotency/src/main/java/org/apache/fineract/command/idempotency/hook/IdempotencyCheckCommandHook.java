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
package org.apache.fineract.command.idempotency.hook;

import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_HOOK_CHECK_BEFORE;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_PROPERTY_HOOK_CHECK_PRE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandHookBefore;
import org.apache.fineract.command.core.CommandStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(COMMAND_IDEMPOTENCY_HOOK_CHECK_BEFORE)
@ConditionalOnProperty(value = COMMAND_IDEMPOTENCY_PROPERTY_HOOK_CHECK_PRE, havingValue = "true")
final class IdempotencyCheckCommandHook implements CommandHookBefore<Object, Object> {

    private final CommandStore store;

    @Override
    public void onBefore(CommandContext<Object, Object> ctx) {
        var command = ctx.getCommand();

        if (StringUtils.isNotEmpty(command.getIdempotencyKey())) {
            var exists = store.existsByKey(command.getIdempotencyKey());

            if (exists) {
                ctx.setSkipExecution(true);
                ctx.setSkipHooks(true);

                var isSameRequestClass = store.checkRequestInstanceByKey(command.getIdempotencyKey(), command.getPayload().getClass());

                log.warn("Duplicate key detected: {} (same request class = {})", command.getIdempotencyKey(), isSameRequestClass);

                if (isSameRequestClass) {
                    var response = store.getResponseByKey(command.getIdempotencyKey());

                    ctx.setResponse(response);
                }

                // TODO: else throw an exception?
            }
        }
    }
}
