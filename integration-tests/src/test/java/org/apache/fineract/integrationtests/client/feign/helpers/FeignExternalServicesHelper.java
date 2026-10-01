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
import org.apache.fineract.client.models.ExternalServicesPropertiesData;

public class FeignExternalServicesHelper {

    private final FineractFeignClient fineractClient;
    private final ExternalServiceCommandsApi externalServiceCommandsApi;

    public FeignExternalServicesHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.externalServiceCommandsApi = fineractClient.create(ExternalServiceCommandsApi.class);
    }

    public List<ExternalServicesPropertiesData> retrieveProperties(String serviceName) {
        return ok(() -> fineractClient.externalServices().retrieveExternalServicesConfiguration(serviceName));
    }

    public Map<String, Object> updateProperty(String serviceName, String name, String value) {
        return ok(() -> externalServiceCommandsApi.updateProperties(serviceName, Map.of(name, value))).getChanges();
    }
}
