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
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandHookAfter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publishes the {@link org.apache.fineract.infrastructure.hooks.event.HookEvent} of every command processed by the new
 * command processing, as the legacy {@code SynchronousCommandProcessingService} does for every command. The event
 * carries the command's {@link org.apache.fineract.command.core.Command#entityName() entity} and
 * {@link org.apache.fineract.command.core.Command#actionName() action}, which is what a hook subscribes to.
 *
 * <p>
 * Not opt-in: a module that moves to the new command processing keeps notifying the webhooks subscribed to its events
 * without having to remember it.
 */
@RequiredArgsConstructor
@Component
final class WebhookCommandHookAfter implements CommandHookAfter<Object, Object> {

    private final WebhookCommandEventPublisher publisher;

    @Override
    public void onAfter(CommandContext<Object, Object> ctx) {
        var command = ctx.getCommand();

        // read before registering, so that what is published is the response as it was when the command finished
        var response = ctx.getResponse();

        // Inside an enclosing (e.g. batch) transaction, wait for the commit so that webhooks never fire for a command
        // that is rolled back by a later failure, as in the legacy command processing.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    publisher.publishSuccess(command, command.getPayload(), response);
                }
            });
        } else {
            publisher.publishSuccess(command, command.getPayload(), response);
        }
    }
}
