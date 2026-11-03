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
package org.apache.fineract.infrastructure.hooks.command;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHookAfter;
import org.apache.fineract.command.core.HookableCommand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publishes the {@link org.apache.fineract.infrastructure.hooks.event.HookEvent} for commands processed by the new
 * command processing, which unlike the legacy {@code SynchronousCommandProcessingService} would otherwise never notify
 * webhooks.
 */
@RequiredArgsConstructor
@Component
final class WebhookCommandHookAfter implements CommandHookAfter<Object, Object> {

    private final WebhookCommandEventPublisher publisher;

    @Override
    public void onAfter(Command<Object> command, Object response) {
        if (!(command instanceof HookableCommand hookable)) {
            return;
        }

        // Inside an enclosing (e.g. batch) transaction, wait for the commit so that webhooks never fire for a command
        // that is rolled back by a later failure, as in the legacy command processing.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    publisher.publishSuccess(hookable, command.getPayload(), response);
                }
            });
        } else {
            publisher.publishSuccess(hookable, command.getPayload(), response);
        }
    }
}
