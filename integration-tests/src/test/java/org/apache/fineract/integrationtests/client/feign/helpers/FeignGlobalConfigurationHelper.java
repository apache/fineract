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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetGlobalConfigurationsResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;

@SuppressWarnings("rawtypes")
@Slf4j
public class FeignGlobalConfigurationHelper {

    private final FineractFeignClient fineractClient;
    private final InternalConfigurationsApi internalConfigurationsApi;

    public FeignGlobalConfigurationHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.internalConfigurationsApi = fineractClient.create(InternalConfigurationsApi.class);
    }

    /** Sets a numeric global configuration value and applies it immediately, without a tenant refresh. */
    public void updateGlobalConfigurationInternal(String configName, Long value) {
        ok(() -> {
            internalConfigurationsApi.updateInternalGlobalConfiguration(configName, value);
            return null;
        });
    }

    public void enableOriginatorCreationDuringLoanApplication() {
        updateConfigurationByName("enable-originator-creation-during-loan-application", true);
    }

    public void disableOriginatorCreationDuringLoanApplication() {
        updateConfigurationByName("enable-originator-creation-during-loan-application", false);
    }

    public void updateConfigurationByName(String configName, boolean enabled) {
        ok(() -> fineractClient.globalConfiguration().updateConfigurationByName(configName,
                new PutGlobalConfigurationsRequest().enabled(enabled)));
    }

    public void manageConfigurations(String configName, boolean enabled) {
        updateConfigurationByName(configName, enabled);
    }

    public void updateGlobalConfiguration(String configName, PutGlobalConfigurationsRequest request) {
        ok(() -> fineractClient.globalConfiguration().updateConfigurationByName(configName, request));
    }

    public Long getConfigurationIdByName(String configName) {
        List<GlobalConfigurationPropertyData> configs = getConfigurationList();
        return configs.stream().filter(c -> configName.equals(c.getName())).findFirst().map(GlobalConfigurationPropertyData::getId)
                .orElseThrow(() -> new RuntimeException("Configuration not found: " + configName));
    }

    public GlobalConfigurationPropertyData getGlobalConfigurationByName(String configName) {
        List<GlobalConfigurationPropertyData> configs = getConfigurationList();
        return configs.stream().filter(c -> configName.equals(c.getName())).findFirst().orElse(null);
    }

    private List<GlobalConfigurationPropertyData> getConfigurationList() {
        GetGlobalConfigurationsResponse response = ok(() -> fineractClient.globalConfiguration().retrieveConfiguration(false));
        return response.getGlobalConfiguration();
    }

    /** Restores every modifiable global configuration to its default so a test cannot leak state into the next one. */
    public void resetAllDefaultGlobalConfigurations() {
        Map<String, HashMap> defaults = defaultsByName();

        int changedNo = 0;
        for (GlobalConfigurationPropertyData actual : getConfigurationList()) {
            HashMap expected = defaults.get(actual.getName());
            if (expected == null) {
                throw new IllegalStateException("Global configuration '" + actual.getName()
                        + "' found in database but not in integration test defaults. "
                        + "You must add it to GlobalConfigurationHelper.getAllDefaultGlobalConfigurations() to ensure test isolation.");
            }
            if (isMatching(expected, actual)) {
                continue;
            }
            // trapDoor configurations reject updates with GlobalConfigurationPropertyCannotBeModfied.
            if ((Boolean) expected.get("trapDoor")) {
                continue;
            }
            updateGlobalConfiguration((String) expected.get("name"),
                    new PutGlobalConfigurationsRequest().value((Long) expected.get("value")).enabled((Boolean) expected.get("enabled")));
            changedNo++;
        }
        log.info("--------------------------------- UPDATED GLOBAL CONFIG ENTRY SIZE: {} ---------------------------------------------",
                changedNo);
    }

    /** Fails the test class if any global configuration is left away from its default. */
    public void verifyAllDefaultGlobalConfigurations() {
        Map<String, HashMap> expectedByName = defaultsByName();
        List<GlobalConfigurationPropertyData> actualConfigurations = getConfigurationList();

        assertEquals(expectedByName.size(), actualConfigurations.size(), "Unexpected number of global configurations");

        for (GlobalConfigurationPropertyData actual : actualConfigurations) {
            String configName = actual.getName();
            HashMap expected = expectedByName.get(configName);
            assertNotNull(expected, "Configuration found in API but not in expected defaults: " + configName);

            final String assertionFailedMessage = "Assertion failed for configName:<" + configName + ">";
            assertEquals(expected.get("name"), actual.getName(), assertionFailedMessage);
            assertEquals(expected.get("value"), actual.getValue(), assertionFailedMessage);
            assertEquals(expected.get("enabled"), actual.getEnabled(), assertionFailedMessage);
            assertEquals(expected.get("trapDoor"), actual.getTrapDoor(), assertionFailedMessage);
        }
    }

    private static Map<String, HashMap> defaultsByName() {
        Map<String, HashMap> defaultsByName = new HashMap<>();
        for (HashMap config : GlobalConfigurationHelper.getAllDefaultGlobalConfigurations()) {
            defaultsByName.put((String) config.get("name"), config);
        }
        return defaultsByName;
    }

    private static boolean isMatching(HashMap expected, GlobalConfigurationPropertyData actual) {
        return expected.get("name").equals(actual.getName()) && expected.get("value").equals(actual.getValue())
                && expected.get("enabled").equals(actual.getEnabled()) && expected.get("trapDoor").equals(actual.getTrapDoor());
    }
}
