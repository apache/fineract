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
package org.apache.fineract.infrastructure.core.config;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class FineractRemoteJobMessageHandlerCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean isSpringEventsEnabled = Optional
                .ofNullable(
                        context.getEnvironment().getProperty("fineract.remote-job-message-handler.spring-events.enabled", Boolean.class))
                .orElse(true);
        boolean isJmsEnabled = Optional
                .ofNullable(context.getEnvironment().getProperty("fineract.remote-job-message-handler.jms.enabled", Boolean.class))
                .orElse(true);
        boolean isBatchManagerModeEnabled = Optional
                .ofNullable(context.getEnvironment().getProperty("fineract.mode.batch-manager-enabled", Boolean.class)).orElse(true);
        boolean isBatchWorkerModeEnabled = Optional
                .ofNullable(context.getEnvironment().getProperty("fineract.mode.batch-worker-enabled", Boolean.class)).orElse(true);
        // TODO extend this expression with other message handlers in the future
        boolean conditionFails = false;
        boolean onlyOneMessageHandlerEnabled = isSpringEventsEnabled ^ isJmsEnabled;
        if (!onlyOneMessageHandlerEnabled) {
            conditionFails = true;
            log.error("For remote partitioning jobs exactly one Message Handler must be enabled.");
        }
        if (isSpringEventsEnabled) {
            if (!(isBatchManagerModeEnabled && isBatchWorkerModeEnabled)) {
                conditionFails = true;
                log.error("If Spring Event Message Handler is enabled, the instance must be Batch Manager and Batch Worker too.");
            }
        }
        return conditionFails;
    }
}
