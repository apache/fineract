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
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetGlobalConfigurationsResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;

public class FeignGlobalConfigurationHelper {

    private final FineractFeignClient fineractClient;

    public FeignGlobalConfigurationHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public void enableOriginatorCreationDuringLoanApplication() {
        updateConfigurationByName("enable-originator-creation-during-loan-application", true);
    }

    public void disableOriginatorCreationDuringLoanApplication() {
        updateConfigurationByName("enable-originator-creation-during-loan-application", false);
    }

    public void updateConfigurationByName(String configName, boolean enabled) {
        Long configId = getConfigurationIdByName(configName);
        ok(() -> fineractClient.globalConfiguration().updateGlobalConfiguration(configId,
                new PutGlobalConfigurationsRequest().enabled(enabled)));
    }

    public Long getConfigurationIdByName(String configName) {
        List<GlobalConfigurationPropertyData> configs = getConfigurationList();
        return configs.stream().filter(c -> configName.equals(c.getName())).findFirst().map(GlobalConfigurationPropertyData::getId)
                .orElseThrow(() -> new RuntimeException("Configuration not found: " + configName));
    }

    private List<GlobalConfigurationPropertyData> getConfigurationList() {
        GetGlobalConfigurationsResponse response = ok(() -> fineractClient.globalConfiguration().retrieveConfiguration(false));
        return response.getGlobalConfiguration();
    }
}
