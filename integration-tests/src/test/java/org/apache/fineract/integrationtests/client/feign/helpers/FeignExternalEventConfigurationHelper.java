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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ExternalEventConfigurationResponse;
import org.apache.fineract.client.models.ExternalEventConfigurationUpdateRequest;
import org.apache.fineract.client.models.ExternalEventConfigurationUpdateResponse;

public class FeignExternalEventConfigurationHelper {

    private final FineractFeignClient fineractClient;

    public FeignExternalEventConfigurationHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public ExternalEventConfigurationResponse retrieveAllConfigurations() {
        return ok(() -> fineractClient.externalEventConfiguration().getExternalEventConfigurations());
    }

    public Map<String, Boolean> retrieveEnabledByEventType() {
        Map<String, Boolean> enabledByEventType = new LinkedHashMap<>();
        retrieveAllConfigurations().getExternalEventConfiguration()
                .forEach(configuration -> enabledByEventType.put(configuration.getType(), configuration.getEnabled()));
        return enabledByEventType;
    }

    public ExternalEventConfigurationUpdateResponse updateConfigurations(Map<String, Boolean> enabledByEventType) {
        ExternalEventConfigurationUpdateRequest request = new ExternalEventConfigurationUpdateRequest()//
                .externalEventConfigurations(enabledByEventType);
        return ok(() -> fineractClient.externalEventConfiguration().updateExternalEventConfigurations(request));
    }

    public ExternalEventConfigurationUpdateResponse disableEventTypes(Collection<String> eventTypes) {
        Map<String, Boolean> disabled = new LinkedHashMap<>();
        eventTypes.forEach(eventType -> disabled.put(eventType, false));
        return updateConfigurations(disabled);
    }
}
