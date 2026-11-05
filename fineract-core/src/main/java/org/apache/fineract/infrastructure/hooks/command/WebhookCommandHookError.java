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
import org.apache.fineract.command.core.CommandHookError;
import org.apache.fineract.command.core.HookableCommand;
import org.springframework.stereotype.Component;

/**
 * Publishes the {@link org.apache.fineract.infrastructure.hooks.event.HookEvent} with {@code status: Exception} for a
 * failed command processed by the new command processing, as the legacy command processing publishes a failure event
 * for every command.
 */
@RequiredArgsConstructor
@Component
final class WebhookCommandHookError implements CommandHookError<Object> {

    private final WebhookCommandEventPublisher publisher;

    @Override
    public void onError(Command<Object> command, Throwable error) {
        if (!(command instanceof HookableCommand hookable)) {
            return;
        }

        // Publish right away, as in the legacy command processing: when the handler fails, its work is rolled back, so
        // there is no commit to wait for. A hook can also fail after the handler has committed, e.g. when the audit
        // hook can't store the command. The event then reports the error the API returns, for a change that was saved.
        publisher.publishError(hookable, command.getPayload(), error);
    }
}
