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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsResponse;
import org.apache.fineract.client.services.GlobalConfigurationApi;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@Component
@RequiredArgsConstructor
public class GlobalConfigurationHelper {

    private final GlobalConfigurationApi globalConfigurationApi;

    public void disableGlobalConfiguration(String configKey, Long value) throws IOException {
        switchAndSetGlobalConfiguration(configKey, false, value);
    }

    public void enableGlobalConfiguration(String configKey, Long value) throws IOException {
        switchAndSetGlobalConfiguration(configKey, true, value);
    }

    private void switchAndSetGlobalConfiguration(String configKey, boolean enabled, Long value) throws IOException {
        Response<GlobalConfigurationPropertyData> configuration = globalConfigurationApi.retrieveOneByName(configKey).execute();
        ErrorHelper.checkSuccessfulApiCall(configuration);
        Long configId = configuration.body().getId();

        PutGlobalConfigurationsRequest updateRequest = new PutGlobalConfigurationsRequest().enabled(enabled).value(value);

        Response<PutGlobalConfigurationsResponse> updateResponse = globalConfigurationApi.updateConfiguration1(configId, updateRequest)
                .execute();
        assertThat(updateResponse.code()).isEqualTo(HttpStatus.SC_OK);
        Response<GlobalConfigurationPropertyData> updatedConfiguration = globalConfigurationApi.retrieveOneByName(configKey).execute();
        boolean isEnabled = BooleanUtils.toBoolean(updatedConfiguration.body().getEnabled());
        assertThat(isEnabled).isEqualTo(enabled);
    }

    public void setGlobalConfigValueString(String configKey, String value) throws IOException {
        Response<GlobalConfigurationPropertyData> configuration = globalConfigurationApi.retrieveOneByName(configKey).execute();
        ErrorHelper.checkSuccessfulApiCall(configuration);
        Long configId = configuration.body().getId();

        PutGlobalConfigurationsRequest updateRequest = new PutGlobalConfigurationsRequest().enabled(true).stringValue(value);

        Response<PutGlobalConfigurationsResponse> updateResponse = globalConfigurationApi.updateConfiguration1(configId, updateRequest)
                .execute();
        assertThat(updateResponse.code()).isEqualTo(HttpStatus.SC_OK);
        Response<GlobalConfigurationPropertyData> updatedConfiguration = globalConfigurationApi.retrieveOneByName(configKey).execute();
        boolean isEnabled = BooleanUtils.toBoolean(updatedConfiguration.body().getEnabled());
        assertThat(isEnabled).isEqualTo(true);
    }
}
