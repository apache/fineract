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
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.command.core.HookableCommand;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.hooks.event.HookEvent;
import org.apache.fineract.infrastructure.hooks.event.HookEventSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Builds and publishes the {@link HookEvent} of a command processed by the new command processing, in the format of the
 * legacy {@code SynchronousCommandProcessingService}, for both its successful and its failed execution.
 */
@Slf4j
@RequiredArgsConstructor
@Component
class WebhookCommandEventPublisher {

    private final PlatformSecurityContext context;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper mapper;
    private final ErrorHandler errorHandler;

    void publishSuccess(HookableCommand hookable, Object request, Object response) {
        publish(hookable, request, payload -> {
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
        });
    }

    void publishError(HookableCommand hookable, Object request, Throwable error) {
        publish(hookable, request, payload -> {
            final ErrorInfo errorInfo = errorHandler.handle(ErrorHandler.getMappable(error));
            payload.put("status", "Exception");
            payload.put("response", errorResponse(errorInfo));
        });
    }

    private void publish(HookableCommand hookable, Object request, Consumer<Map<String, Object>> result) {
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
            result.accept(payload);
            payload.put("timestamp", Instant.now().toString());

            eventPublisher.publishEvent(new HookEvent(new HookEventSource(entityName, actionName), mapper.writeValueAsString(payload), user,
                    ThreadLocalContextUtil.getContext()));
        } catch (Exception e) {
            log.error("Failed to publish hook event for entity: {}, action: {}", entityName, actionName, e);
        }
    }

    // As in the legacy command processing, the fields of the error body are the response, next to its codes.
    private ObjectNode errorResponse(ErrorInfo errorInfo) {
        ObjectNode response = null;
        if (errorInfo.getMessage() != null) {
            try {
                if (mapper.readTree(errorInfo.getMessage()) instanceof ObjectNode body) {
                    response = body;
                }
            } catch (JacksonException e) {
                log.debug("Error message is not a JSON object, sending it as is", e);
            }
        }
        if (response == null) {
            response = mapper.createObjectNode();
            response.put("errorMessage", errorInfo.getMessage());
        }
        response.put("errorCode", errorInfo.getErrorCode());
        response.put("statusCode", errorInfo.getStatusCode());
        return response;
    }

    private static void putIfPresent(Map<String, Object> payload, String field, JsonNode value) {
        if (value != null && !value.isNull()) {
            payload.put(field, value);
        }
    }
}
