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
package org.apache.fineract.integrationtests.common;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.client.models.ExternalEventConfigurationItemResponse;
import org.apache.fineract.client.models.ExternalEventConfigurationResponse;
import org.apache.fineract.client.models.ExternalEventConfigurationUpdateRequest;
import org.apache.fineract.client.models.ExternalEventConfigurationUpdateResponse;

public class ExternalEventConfigurationHelper {

    protected ExternalEventConfigurationHelper() {}

    public static ArrayList<Map<String, Object>> getAllExternalEventConfigurations(RequestSpecification requestSpec,
            ResponseSpecification responseSpec) {
        ExternalEventConfigurationResponse response = ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().externalEventConfiguration().getExternalEventConfigurations());
        ArrayList<Map<String, Object>> configurations = new ArrayList<>();
        if (response.getExternalEventConfiguration() != null) {
            for (ExternalEventConfigurationItemResponse item : response.getExternalEventConfiguration()) {
                configurations.add(toMap(item));
            }
        }
        return configurations;
    }

    public static ArrayList<Map<String, Object>> getDefaultExternalEventConfigurations() {
        ArrayList<Map<String, Object>> defaults = getAllExternalEventConfigurations(null, null);
        for (Map<String, Object> defaultConfiguration : defaults) {
            defaultConfiguration.put("enabled", false);
        }
        return defaults;
    }

    public static ExternalEventConfigurationUpdateRequest getExternalEventConfigurationsForUpdateRequest() {
        return new ExternalEventConfigurationUpdateRequest()
                .externalEventConfigurations(Map.of("CentersCreateBusinessEvent", true, "ClientActivateBusinessEvent", true));
    }

    public static Map<String, Boolean> updateExternalEventConfigurations(RequestSpecification requestSpec,
            ResponseSpecification responseSpec, ExternalEventConfigurationUpdateRequest request) {
        ExternalEventConfigurationUpdateResponse response = ok(() -> FineractFeignClientHelper.getFineractFeignClient()
                .externalEventConfiguration().updateExternalEventConfigurations(request));
        Map<String, Boolean> updatedConfigurations = new HashMap<>();
        Object configurations = response.getChanges().get("externalEventConfigurations");
        if (configurations instanceof Map<?, ?> configurationMap) {
            for (Map.Entry<?, ?> entry : configurationMap.entrySet()) {
                updatedConfigurations.put(String.valueOf(entry.getKey()), (Boolean) entry.getValue());
            }
        }
        return updatedConfigurations;
    }

    public static void resetDefaultConfigurations(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        updateExternalEventConfigurations(requestSpec, responseSpec, new ExternalEventConfigurationUpdateRequest()
                .externalEventConfigurations(Map.of("CentersCreateBusinessEvent", false, "ClientActivateBusinessEvent", false)));
    }

    private static Map<String, Object> toMap(ExternalEventConfigurationItemResponse item) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", item.getType());
        map.put("enabled", item.getEnabled());
        return map;
    }
}
