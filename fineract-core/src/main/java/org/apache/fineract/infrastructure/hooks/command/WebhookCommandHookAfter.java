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

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHookAfter;
import org.apache.fineract.command.core.HookableCommand;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.hooks.event.HookEvent;
import org.apache.fineract.infrastructure.hooks.event.HookEventSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Publishes the {@link HookEvent} for commands processed by the new command processing, which unlike the legacy
 * {@code SynchronousCommandProcessingService} would otherwise never notify webhooks.
 */
@Slf4j
@RequiredArgsConstructor
@Component
final class WebhookCommandHookAfter implements CommandHookAfter<Object, Object> {

    private final PlatformSecurityContext context;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper mapper;

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
                    publish(hookable, command.getPayload(), response);
                }
            });
        } else {
            publish(hookable, command.getPayload(), response);
        }
    }

    private void publish(HookableCommand hookable, Object request, Object response) {
        final String entityName = hookable.hookEntityName();
        final String actionName = hookable.hookActionName();

        try {
            final AppUser user = context.authenticatedUser();

            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("entityName", entityName);
            payload.put("actionName", actionName);
            payload.put("createdBy", user.getId());
            payload.put("createdByName", user.getUsername());
            payload.put("createdByFullName", user.getDisplayName());
            payload.put("request", request);

            // As in the legacy command processing, the office and client are exposed at the top level of the payload,
            // where the SMS hooks look up the client to notify, and the office is cleared from the response.
            final JsonNode responseNode = mapper.valueToTree(response);
            if (responseNode instanceof ObjectNode responseObject) {
                putIfPresent(payload, "officeId", responseObject.get("officeId"));
                putIfPresent(payload, "clientId", responseObject.get("clientId"));
                if (responseObject.has("officeId")) {
                    responseObject.putNull("officeId");
                }
            }
            payload.put("response", responseNode);
            payload.put("timestamp", Instant.now().toString());

            eventPublisher.publishEvent(new HookEvent(new HookEventSource(entityName, actionName), mapper.writeValueAsString(payload), user,
                    ThreadLocalContextUtil.getContext()));
        } catch (Exception e) {
            log.error("Failed to publish hook event for entity: {}, action: {}", entityName, actionName, e);
        }
    }

    private static void putIfPresent(Map<String, Object> payload, String field, JsonNode value) {
        if (value != null && !value.isNull()) {
            payload.put(field, value);
        }
    }
}
