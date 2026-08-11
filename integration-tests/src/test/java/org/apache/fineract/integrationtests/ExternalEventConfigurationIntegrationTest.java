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
package org.apache.fineract.integrationtests;

import static org.apache.fineract.integrationtests.client.feign.modules.ExternalEventConfigurationTestData.CENTERS_CREATE_EVENT;
import static org.apache.fineract.integrationtests.client.feign.modules.ExternalEventConfigurationTestData.CLIENT_ACTIVATE_EVENT;
import static org.apache.fineract.integrationtests.client.feign.modules.ExternalEventConfigurationTestData.DEFAULT_DISABLED_EVENT_TYPES;

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.ExternalEventConfigurationItemResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventConfigurationHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ExternalEventConfigurationIntegrationTest extends FeignIntegrationTest {

    private static final String EXTERNAL_EVENT_CONFIGURATIONS_KEY = "externalEventConfigurations";

    private FeignExternalEventConfigurationHelper externalEventConfigurationHelper;

    @BeforeAll
    public void setup() {
        externalEventConfigurationHelper = new FeignExternalEventConfigurationHelper(fineractClient());
    }

    @Test
    public void getExternalEventConfigurations() {
        final List<ExternalEventConfigurationItemResponse> externalEventConfigurations = externalEventConfigurationHelper
                .retrieveAllConfigurations().getExternalEventConfiguration();

        Assertions.assertNotNull(externalEventConfigurations);
        Assertions.assertEquals(DEFAULT_DISABLED_EVENT_TYPES.size(), externalEventConfigurations.size());
        for (final ExternalEventConfigurationItemResponse configuration : externalEventConfigurations) {
            Assertions.assertTrue(DEFAULT_DISABLED_EVENT_TYPES.contains(configuration.getType()),
                    "Unexpected external event type: " + configuration.getType());
            Assertions.assertEquals(Boolean.FALSE, configuration.getEnabled(),
                    "External event " + configuration.getType() + " is expected to be disabled by default");
        }
    }

    @Test
    public void updateExternalEventConfigurations() {
        final Map<String, Boolean> update = Map.of(CENTERS_CREATE_EVENT, true, CLIENT_ACTIVATE_EVENT, true);

        final Map<String, Object> changes = externalEventConfigurationHelper.updateConfigurations(update).getChanges();

        // The command nests the flags it changed one level down, under the same key the request used.
        final Map<?, ?> changedFlags = (Map<?, ?>) changes.get(EXTERNAL_EVENT_CONFIGURATIONS_KEY);
        Assertions.assertNotNull(changedFlags, "changes payload did not contain " + EXTERNAL_EVENT_CONFIGURATIONS_KEY);
        Assertions.assertEquals(2, changedFlags.size());
        Assertions.assertEquals(Boolean.TRUE, changedFlags.get(CENTERS_CREATE_EVENT));
        Assertions.assertEquals(Boolean.TRUE, changedFlags.get(CLIENT_ACTIVATE_EVENT));

        final Map<String, Boolean> enabledByEventType = externalEventConfigurationHelper.retrieveEnabledByEventType();
        Assertions.assertEquals(Boolean.TRUE, enabledByEventType.get(CENTERS_CREATE_EVENT), CENTERS_CREATE_EVENT + " was not enabled");
        Assertions.assertEquals(Boolean.TRUE, enabledByEventType.get(CLIENT_ACTIVATE_EVENT), CLIENT_ACTIVATE_EVENT + " was not enabled");
    }

    @AfterEach
    public void tearDown() {
        externalEventConfigurationHelper.disableEventTypes(List.of(CENTERS_CREATE_EVENT, CLIENT_ACTIVATE_EVENT));
    }

}
