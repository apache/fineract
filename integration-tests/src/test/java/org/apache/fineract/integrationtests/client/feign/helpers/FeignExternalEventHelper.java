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

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ExternalEventConfigurationUpdateRequest;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;

public class FeignExternalEventHelper {

    private final FineractFeignClient fineractClient;
    private final InternalExternalEventsApi internalEventsApi;

    public FeignExternalEventHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.internalEventsApi = fineractClient.create(InternalExternalEventsApi.class);
    }

    public void enableBusinessEvent(String eventName) {
        ok(() -> fineractClient.externalEventConfiguration().updateExternalEventConfigurations(
                new ExternalEventConfigurationUpdateRequest().externalEventConfigurations(Map.of(eventName, true))));
    }

    public void disableBusinessEvent(String eventName) {
        ok(() -> fineractClient.externalEventConfiguration().updateExternalEventConfigurations(
                new ExternalEventConfigurationUpdateRequest().externalEventConfigurations(Map.of(eventName, false))));
    }

    public List<ExternalEventResponse> getExternalEventsByType(String type) {
        return ok(() -> internalEventsApi.getAllExternalEvents(Map.of("type", type)));
    }

    public void deleteAllExternalEvents() {
        ok(() -> {
            internalEventsApi.deleteAllExternalEvents();
            return null;
        });
    }
}
