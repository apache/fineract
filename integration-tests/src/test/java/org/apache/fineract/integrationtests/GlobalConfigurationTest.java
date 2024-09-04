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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.CommonConstants;
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
    private ResponseSpecification httpStatusForidden;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.httpStatusForidden = new ResponseSpecBuilder().expectStatusCode(403).build();
    }

    @AfterEach
    public void tearDown() {
        GlobalConfigurationHelper.resetAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
        GlobalConfigurationHelper.verifyAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testGlobalConfigurations() {
        this.globalConfigurationHelper = new GlobalConfigurationHelper(this.requestSpec, this.responseSpec);

        // Retrieving All Global Configuration details
        final ArrayList<HashMap> globalConfig = GlobalConfigurationHelper.getAllGlobalConfigurations(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(globalConfig);

        String configName = "penalty-wait-period";
        for (Integer configIndex = 0; configIndex < globalConfig.size() - 1; configIndex++) {
            if (globalConfig.get(configIndex).get("name").equals(configName)) {
                Integer configId = (Integer) globalConfig.get(configIndex).get("id");
                Assertions.assertNotNull(configId);

                HashMap configDataBefore = GlobalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                        configId.toString());
                Assertions.assertNotNull(configDataBefore);

                Integer value = (Integer) configDataBefore.get("value") + 1;

                // Updating Value for penalty-wait-period Global Configuration
                configId = GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec,
                        configId.toString(), value.toString());
                Assertions.assertNotNull(configId);

                HashMap configDataAfter = GlobalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                        configId.toString());

                // Verifying Value for penalty-wait-period after Updation
                Assertions.assertEquals(value, configDataAfter.get("value"), "Verifying Global Config Value after Updation");

                // Updating Enabled Flag for penalty-wait-period Global
                // Configuration
                Boolean enabled = (Boolean) globalConfig.get(configIndex).get("enabled");

                if (enabled == true) {
                    enabled = false;
                } else {
                    enabled = true;
                }

                configId = GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(this.requestSpec, this.responseSpec,
                        configId.toString(), enabled);

                configDataAfter = GlobalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                        configId.toString());

                // Verifying Enabled Flag for penalty-wait-period after Updation
                Assertions.assertEquals(enabled, configDataAfter.get("enabled"), "Verifying Enabled Flag Global Config after Updation");
                break;
            }
        }
    }

    @Test
    public void testGlobalConfigurationsEnableDisable() {
        this.globalConfigurationHelper = new GlobalConfigurationHelper(this.requestSpec, this.responseSpec);

        // Retrieving All Global Configuration details
        final ArrayList<HashMap> globalConfig = GlobalConfigurationHelper.getAllGlobalConfigurations(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(globalConfig);
        String configName = "enable_payment_hub_integration";
        for (Integer configIndex = 0; configIndex < globalConfig.size() - 1; configIndex++) {
            if (globalConfig.get(configIndex).get("name").equals(configName)) {
                Integer configId = (Integer) globalConfig.get(configIndex).get("id");
                Assertions.assertNotNull(configId);

                HashMap configDataBefore = GlobalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                        configId.toString());
                Assertions.assertNotNull(configDataBefore);

                Integer value = (Integer) configDataBefore.get("value") + 1;

                // Updating Enabled Flag for use-payment-hub Global
                // Configuration
                Boolean enabled = (Boolean) globalConfig.get(configIndex).get("enabled");

                if (enabled == true) {
                    enabled = false;
                } else {
                    enabled = true;
                }

                configId = GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(this.requestSpec, this.responseSpec,
                        configId.toString(), enabled);

                HashMap configDataAfter = GlobalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                        configId.toString());

                // Verifying Enabled Flag for use-payment-hub after Updation
                Assertions.assertEquals(enabled, configDataAfter.get("enabled"), "Verifying Enabled Flag Global Config after Updation");
                break;
            }
        }
    }

    @Test
    public void testGlobalConfigurationIsCacheEnabled() {
        this.globalConfigurationHelper = new GlobalConfigurationHelper(this.requestSpec, this.responseSpec);

        // Retrieving Is Cache Enabled Global Configuration details
        ArrayList<HashMap> isCacheGlobalConfig = GlobalConfigurationHelper.getGlobalConfigurationIsCacheEnabled(this.requestSpec,
                this.responseSpec);
        Assertions.assertNotNull(isCacheGlobalConfig);

        for (Integer cacheType = 0; cacheType <= isCacheGlobalConfig.size() - 1; cacheType++) {

            // Retrieving Is Cache Enabled Global Configuration details
            isCacheGlobalConfig = GlobalConfigurationHelper.getGlobalConfigurationIsCacheEnabled(this.requestSpec, this.responseSpec);
            Assertions.assertNotNull(isCacheGlobalConfig);

            HashMap cacheTypeAsHashMap = (HashMap) isCacheGlobalConfig.get(cacheType).get("cacheType");
            Integer cacheTypeId = (Integer) cacheTypeAsHashMap.get("id");
            String cacheTypeValue = (String) cacheTypeAsHashMap.get("value");
            Boolean enabled = (Boolean) isCacheGlobalConfig.get(cacheType).get("enabled");

            if (cacheTypeValue.compareTo("No cache") == 0 && enabled == true) {
                cacheTypeId += 1;
            } else if (cacheTypeValue.compareTo("Single node") == 0 && enabled == true) {
                cacheTypeId -= 1;
            }

            HashMap changes = GlobalConfigurationHelper.updateIsCacheEnabledForGlobalConfiguration(this.requestSpec, this.responseSpec,
                    cacheTypeId.toString());
            Assertions.assertEquals(cacheTypeId, changes.get("cacheType"), "Verifying Is Cache Enabled Global Config after Updation");
        }
    }

    @Test
    public void testGlobalConfigForcePasswordResetDays() {

        // Retrieving All Global Configuration details
        final ArrayList<HashMap> globalConfig = GlobalConfigurationHelper.getAllGlobalConfigurations(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(globalConfig);

        String configName = "force-password-reset-days";
        String newValue = "0";
        String newBooleanValue = "true";

        for (Integer configIndex = 0; configIndex < globalConfig.size() - 1; configIndex++) {
            if (globalConfig.get(configIndex).get("name").equals(configName)) {
                Integer configId = (Integer) globalConfig.get(configIndex).get("id");
                Assertions.assertNotNull(configId);

                /*
                 * Update force-password-reset-days with value as 0 and Enable as true - failure case
                 */
                ArrayList error = (ArrayList) GlobalConfigurationHelper.updatePasswordResetDaysForGlobalConfiguration(this.requestSpec,
                        this.httpStatusForidden, configId, newValue, newBooleanValue, CommonConstants.RESPONSE_ERROR);
                HashMap hash = (HashMap) error.get(0);

                Assertions.assertEquals("error.msg.password.reset.days.value.must.be.greater.than.zero",
                        hash.get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE),
                        "Force Password Reset days value must be greater than zero.");

                /*
                 * Update force-password-reset-days with value as 50 and Enable as true - success case
                 */
                final HashMap updateSuccess = (HashMap) GlobalConfigurationHelper.updatePasswordResetDaysForGlobalConfiguration(
                        this.requestSpec, this.responseSpec, configId, "50", newBooleanValue, "changes");
                Assertions.assertNotNull(updateSuccess);

                /* Update with value as 0 and Enable as false - success case */
                final HashMap updateSuccess1 = (HashMap) GlobalConfigurationHelper.updatePasswordResetDaysForGlobalConfiguration(
                        this.requestSpec, this.responseSpec, configId, newValue, "false", "changes");
                Assertions.assertNotNull(updateSuccess1);

                /*
                 * Update without sending value and Enable as true - failure case
                 */
                ArrayList failure = (ArrayList) GlobalConfigurationHelper.updatePasswordResetDaysForGlobalConfiguration(this.requestSpec,
                        this.httpStatusForidden, configId, null, newBooleanValue, CommonConstants.RESPONSE_ERROR);
                HashMap failureHash = (HashMap) failure.get(0);
                Assertions.assertEquals("error.msg.password.reset.days.value.must.be.greater.than.zero",
                        failureHash.get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE),
                        "Force Password Reset days value must be greater than zero.");

                break;
            }
        }
        /* Update other global configuration property */
        String otherConfigName = "maker-checker";
        for (Integer configIndex = 0; configIndex < globalConfig.size() - 1; configIndex++) {
            if (globalConfig.get(configIndex).get("name").equals(otherConfigName)) {
                String configId = globalConfig.get(configIndex).get("id").toString();
                Integer updateConfigId = GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec,
                        configId, newValue);
                Assertions.assertNotNull(updateConfigId);
                break;
            }
        }
    }
}
