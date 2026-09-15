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
package org.apache.fineract.command.audit.hook;

import static org.apache.fineract.command.audit.AuditCommandConstants.COMMAND_AUDIT_HOOK_BEFORE;
import static org.apache.fineract.command.audit.AuditCommandConstants.COMMAND_AUDIT_PROPERTY_HOOK_PRE_ENABLED;
import static org.apache.fineract.command.core.CommandState.UNDER_PROCESSING;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandHookBefore;
import org.apache.fineract.command.core.CommandStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(COMMAND_AUDIT_HOOK_BEFORE)
@ConditionalOnProperty(value = COMMAND_AUDIT_PROPERTY_HOOK_PRE_ENABLED, havingValue = "true")
final class AuditCommandHookBefore implements CommandHookBefore<Object, Object> {

    private final CommandStore store;

    @Override
    public void onBefore(CommandContext<Object, Object> ctx) {
        final var now = Instant.now();

        var command = ctx.getCommand();

        command.setExecutedByUsername(command.getInitiatedByUsername());
        command.setUpdatedAt(now);
        command.setExecutedAt(now);

        ctx.setState(UNDER_PROCESSING);

        store.store(ctx);
    }
}
