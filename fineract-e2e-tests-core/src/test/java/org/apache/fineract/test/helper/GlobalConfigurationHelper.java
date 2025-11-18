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
package org.apache.fineract.test.helper;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GlobalConfigurationHelper {

    private final FineractFeignClient fineractClient;

    public void disableGlobalConfiguration(String configKey, Long value) {
        switchAndSetGlobalConfiguration(configKey, false, value);
    }

    public void enableGlobalConfiguration(String configKey, Long value) {
        switchAndSetGlobalConfiguration(configKey, true, value);
    }

    private void switchAndSetGlobalConfiguration(String configKey, boolean enabled, Long value) {
        GlobalConfigurationPropertyData configuration = ok(
                () -> fineractClient.globalConfiguration().retrieveOneByName(configKey, Map.of()));
        Long configId = configuration.getId();

        PutGlobalConfigurationsRequest updateRequest = new PutGlobalConfigurationsRequest().enabled(enabled).value(value);

        ok(() -> fineractClient.globalConfiguration().updateConfiguration1(configId, updateRequest, Map.of()));
        GlobalConfigurationPropertyData updatedConfiguration = ok(
                () -> fineractClient.globalConfiguration().retrieveOneByName(configKey, Map.of()));
        boolean isEnabled = BooleanUtils.toBoolean(updatedConfiguration.getEnabled());
        assertThat(isEnabled).isEqualTo(enabled);
    }

    public void setGlobalConfigValueString(String configKey, String value) {
        GlobalConfigurationPropertyData configuration = ok(
                () -> fineractClient.globalConfiguration().retrieveOneByName(configKey, Map.of()));
        Long configId = configuration.getId();

        PutGlobalConfigurationsRequest updateRequest = new PutGlobalConfigurationsRequest().enabled(true).stringValue(value);

        ok(() -> fineractClient.globalConfiguration().updateConfiguration1(configId, updateRequest, Map.of()));
        GlobalConfigurationPropertyData updatedConfiguration = ok(
                () -> fineractClient.globalConfiguration().retrieveOneByName(configKey, Map.of()));
        boolean isEnabled = BooleanUtils.toBoolean(updatedConfiguration.getEnabled());
        assertThat(isEnabled).isEqualTo(true);
    }

    public GlobalConfigurationPropertyData getGlobalConfiguration(String configKey) {
        return ok(() -> fineractClient.globalConfiguration().retrieveOneByName(configKey, Map.of()));
    }
}
