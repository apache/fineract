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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.models.GetGlobalConfigurationsResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "unchecked" })

public class GlobalConfigurationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }

    @Test
    public void testGlobalConfigurations() {
        // Retrieving All Global Configuration details
        final GetGlobalConfigurationsResponse globalConfig = globalConfigurationHelper.getAllGlobalConfigurations();
        Assertions.assertNotNull(globalConfig);
    }

    @Test
    public void testGlobalConfigurationUpdate() {
        String configName = GlobalConfigurationConstants.PENALTY_WAIT_PERIOD;
        GlobalConfigurationPropertyData config = globalConfigurationHelper.getGlobalConfigurationByName(configName);
        try {
            Assertions.assertNotNull(config);
            Assertions.assertNotNull(config.getValue());
            Long newValue = config.getValue() + 1;
            Assertions.assertNotNull(config.getEnabled());
            // Updating Value for penalty-wait-period Global Configuration
            boolean newEnabledValue = !config.getEnabled();
            // Updating Enabled Flag for penalty-wait-period Global
            // Configuration
            globalConfigurationHelper.updateGlobalConfiguration(config.getName(),
                    new PutGlobalConfigurationsRequest().value(newValue).enabled(newEnabledValue));
            GlobalConfigurationPropertyData updatedConfiguration = globalConfigurationHelper.getGlobalConfigurationByName(configName);
            // Verifying Value for penalty-wait-period after the update
            Assertions.assertEquals(newValue, updatedConfiguration.getValue(), "Verifying Global Config Value after the update");
            // Verifying Enabled Flag for penalty-wait-period after Updation
            Assertions.assertEquals(newEnabledValue, updatedConfiguration.getEnabled(),
                    "Verifying Enabled Flag Global Config after the update");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(config.getName(),
                    new PutGlobalConfigurationsRequest().value(config.getValue()).enabled(config.getEnabled()));
        }
    }

    @Test
    public void testPasswordUpdateFailing() {
        String configName = GlobalConfigurationConstants.FORCE_PASSWORD_RESET_DAYS;
        GlobalConfigurationPropertyData config = globalConfigurationHelper.getGlobalConfigurationByName(configName);
        Assertions.assertNotNull(config);

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> globalConfigurationHelper
                .updateGlobalConfiguration(configName, new PutGlobalConfigurationsRequest().enabled(true).value(null)));
        assertEquals(403, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.password.reset.days.value.must.be.greater.than.zero"));
    }
}
