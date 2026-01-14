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
package org.apache.fineract.test.initializer.suite;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ExternalEventConfigurationItemResponse;
import org.apache.fineract.client.models.ExternalEventConfigurationResponse;
import org.apache.fineract.client.models.ExternalEventConfigurationUpdateRequest;
import org.apache.fineract.test.messaging.config.EventProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExternalEventSuiteInitializerStep implements FineractSuiteInitializerStep {

    private static final Duration JMS_STARTUP_TIMEOUT = Duration.ofSeconds(30);

    private final FineractFeignClient fineractClient;

    @Autowired(required = false)
    private JmsListenerEndpointRegistry registry;

    @Autowired(required = false)
    private EventProperties eventProperties;

    @Override
    public void initializeForSuite() throws InterruptedException {
        log.debug("=== ExternalEventSuiteInitializerStep.initializeForSuite() - START ===");

        // Step 1: Enable all external events
        Map<String, Boolean> eventConfigMap = new HashMap<>();

        ExternalEventConfigurationResponse response = ok(
                () -> fineractClient.externalEventConfiguration().getExternalEventConfigurations(Map.of()));

        List<ExternalEventConfigurationItemResponse> externalEventConfiguration = response.getExternalEventConfiguration();
        externalEventConfiguration.forEach(e -> {
            eventConfigMap.put(e.getType(), true);
        });

        ExternalEventConfigurationUpdateRequest request = new ExternalEventConfigurationUpdateRequest()
                .externalEventConfigurations(eventConfigMap);

        executeVoid(() -> fineractClient.externalEventConfiguration().updateExternalEventConfigurations(null, request, Map.of()));
        log.debug("=== External event configuration updated - all events enabled ===");

        // Step 2: Wait for JMS Listener to be ready before proceeding
        if (eventProperties != null && eventProperties.isEventVerificationEnabled()) {
            if (registry == null) {
                log.warn("=== JmsListenerEndpointRegistry not available - skipping JMS listener readiness check ===");
                log.warn("=== This is expected in CI environments where JMS may not be fully initialized during suite setup ===");
            } else {
                log.info("=== Waiting for JMS Listener to connect to ActiveMQ (max {}s) ===", JMS_STARTUP_TIMEOUT.toSeconds());
                DefaultMessageListenerContainer container = (DefaultMessageListenerContainer) registry
                        .getListenerContainer("eventStoreListener");

                if (container == null) {
                    log.warn("=== JMS Listener container 'eventStoreListener' not found - event verification may not work ===");
                } else {
                    await().atMost(JMS_STARTUP_TIMEOUT).pollInterval(Duration.ofMillis(200)).until(container::isRunning);
                    log.info("=== JMS Listener is running and ready to receive events ===");
                }
            }
        }

        log.debug("=== ExternalEventSuiteInitializerStep.initializeForSuite() - COMPLETED ===");
    }
}
