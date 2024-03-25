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

import static java.lang.System.lineSeparator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.ExternalEventConfigurationItemData;
import org.apache.fineract.client.models.GetExternalEventConfigurationsResponse;
import org.apache.fineract.client.models.PutExternalEventConfigurationsRequest;
import org.apache.fineract.client.services.ExternalEventConfigurationApi;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@RequiredArgsConstructor
@Component
public class ExternalEventSuiteInitializerStep implements FineractSuiteInitializerStep {

    private final ExternalEventConfigurationApi eventConfigurationApi;

    @Override
    public void initializeForSuite() throws Exception {
        Map<String, Boolean> eventConfigMap = new HashMap<>();

        Response<GetExternalEventConfigurationsResponse> response = eventConfigurationApi.retrieveExternalEventConfiguration().execute();
        if (!response.isSuccessful()) {
            String responseBody = response.errorBody().string();
            throw new RuntimeException("Cannot configure external events due to " + lineSeparator() + responseBody);
        }

        List<ExternalEventConfigurationItemData> externalEventConfiguration = response.body().getExternalEventConfiguration();
        externalEventConfiguration.forEach(e -> {
            eventConfigMap.put(e.getType(), true);
        });

        PutExternalEventConfigurationsRequest request = new PutExternalEventConfigurationsRequest()
                .externalEventConfigurations(eventConfigMap);

        eventConfigurationApi.updateExternalEventConfigurationsDetails(request).execute();
    }
}
