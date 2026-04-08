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

import static org.apache.fineract.command.audit.AuditCommandConstants.COMMAND_HOOK_AUDIT_AFTER;
import static org.apache.fineract.command.core.CommandState.PROCESSED;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHookAfter;
import org.apache.fineract.command.core.CommandStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(COMMAND_HOOK_AUDIT_AFTER)
@ConditionalOnProperty(value = "fineract.command.hooks.audit-post", havingValue = "true")
final class AuditCommandHookAfter implements CommandHookAfter<Object, Object> {

    private final CommandStore store;

    @Override
    public void onAfter(Command<Object> command, Object response) {
        final var now = Instant.now();

        command.setExecutedByUsername(command.getInitiatedByUsername());
        command.setUpdatedAt(now);
        command.setExecutedAt(now);

        store.store(command, response, PROCESSED);
    }
}
