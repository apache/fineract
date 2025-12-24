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
package org.apache.fineract.test.stepdef.hook;

import static org.awaitility.Awaitility.await;

import io.cucumber.java.Before;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.test.messaging.config.EventProperties;
import org.apache.fineract.test.messaging.store.EventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@Slf4j
public class MessagingHook {

    @Autowired
    private EventStore eventStore;

    @Autowired(required = false)
    private JmsListenerEndpointRegistry registry;

    @Autowired(required = false)
    private EventProperties eventProperties;

    private static final AtomicBoolean jmsStartupDelayCompleted = new AtomicBoolean(false);
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(20);

    @Before(order = 0)
    public void waitForJmsListenerStartup() {
        if (jmsStartupDelayCompleted.compareAndSet(false, true) && eventProperties != null
                && eventProperties.isEventVerificationEnabled()) {
            if (registry == null) {
                log.warn("=== JmsListenerEndpointRegistry not available - skipping JMS listener readiness check ===");
                return;
            }
            log.info("=== FIRST SCENARIO - Waiting for JMS Listener to connect to ActiveMQ (max {}s) ===", STARTUP_TIMEOUT.toSeconds());
            DefaultMessageListenerContainer container = (DefaultMessageListenerContainer) registry
                    .getListenerContainer("eventStoreListener");
            await().atMost(STARTUP_TIMEOUT).pollInterval(Duration.ofMillis(200)).until(container::isRunning);
            log.info("=== JMS Listener is running - tests can proceed ===");
        }
    }

    @Before(order = 1)
    public void emptyEventStore() {
        eventStore.reset();
    }
}
