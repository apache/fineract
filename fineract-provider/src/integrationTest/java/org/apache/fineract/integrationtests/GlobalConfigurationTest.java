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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked", "static-access" })
public class GlobalConfigurationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private GlobalConfigurationHelper globalConfigurationHelper;
    private ResponseSpecification httpStatusForidden;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.httpStatusForidden = new ResponseSpecBuilder().expectStatusCode(403).build();
    }

    @Test
    public void testGlobalConfigurations() {
        this.globalConfigurationHelper = new GlobalConfigurationHelper(this.requestSpec, this.responseSpec);

        // Retrieving All Global Configuration details
        final ArrayList<HashMap> globalConfig = this.globalConfigurationHelper.getAllGlobalConfigurations(this.requestSpec,
                this.responseSpec);
        Assert.assertNotNull(globalConfig);

        String configName = "penalty-wait-period";
        for (Integer configIndex = 0; configIndex < (globalConfig.size() - 1); configIndex++) {
            if (globalConfig.get(configIndex).get("name").equals(configName)) {
                Integer configId = (Integer) globalConfig.get(configIndex).get("id");
                Assert.assertNotNull(configId);

                HashMap configDataBefore = this.globalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                        configId.toString());
                Assert.assertNotNull(configDataBefore);

                Integer value = (Integer) configDataBefore.get("value") + 1;

                // Updating Value for penalty-wait-period Global Configuration
                configId = this.globalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec,
                        configId.toString(), value.toString());
                Assert.assertNotNull(configId);

                HashMap configDataAfter = this.globalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                        configId.toString());

                // Verifying Value for penalty-wait-period after Updation
                Assert.assertEquals("Verifying Global Config Value after Updation", value, configDataAfter.get("value"));

                // Updating Enabled Flag for penalty-wait-period Global
                // Configuration
                Boolean enabled = (Boolean) globalConfig.get(configIndex).get("enabled");

                if (enabled == true) {
                    enabled = false;
                } else {
                    enabled = true;
                }

                configId = this.globalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(this.requestSpec, this.responseSpec,
                        configId.toString(), enabled);

                configDataAfter = this.globalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                        configId.toString());

                // Verifying Enabled Flag for penalty-wait-period after Updation
                Assert.assertEquals("Verifying Enabled Flag Global Config after Updation", enabled, configDataAfter.get("enabled"));
                break;
            }
        }
    }

    @Test
    public void testGlobalConfigurationIsCacheEnabled() {
        this.globalConfigurationHelper = new GlobalConfigurationHelper(this.requestSpec, this.responseSpec);

        // Retrieving Is Cache Enabled Global Configuration details
        ArrayList<HashMap> isCacheGlobalConfig = this.globalConfigurationHelper.getGlobalConfigurationIsCacheEnabled(this.requestSpec,
                this.responseSpec);
        Assert.assertNotNull(isCacheGlobalConfig);

        for (Integer cacheType = 0; cacheType <= ((isCacheGlobalConfig.size()) - 1); cacheType++) {

            // Retrieving Is Cache Enabled Global Configuration details
            isCacheGlobalConfig = this.globalConfigurationHelper.getGlobalConfigurationIsCacheEnabled(this.requestSpec, this.responseSpec);
            Assert.assertNotNull(isCacheGlobalConfig);

            HashMap cacheTypeAsHashMap = (HashMap) isCacheGlobalConfig.get(cacheType).get("cacheType");
            Integer cacheTypeId = (Integer) cacheTypeAsHashMap.get("id");
            String cacheTypeValue = (String) cacheTypeAsHashMap.get("value");
            Boolean enabled = (Boolean) isCacheGlobalConfig.get(cacheType).get("enabled");

            if (cacheTypeValue.compareTo("No cache") == 0 && enabled == true) {
                cacheTypeId += 1;
            } else if (cacheTypeValue.compareTo("Single node") == 0 && enabled == true) {
                cacheTypeId -= 1;
            }

            HashMap changes = this.globalConfigurationHelper.updateIsCacheEnabledForGlobalConfiguration(this.requestSpec,
                    this.responseSpec, cacheTypeId.toString());
            Assert.assertEquals("Verifying Is Cache Enabled Global Config after Updation", cacheTypeId, changes.get("cacheType"));
        }
    }
    
    @Test
	public void testGlobalConfigForcePasswordResetDays() {

		// Retrieving All Global Configuration details
		final ArrayList<HashMap> globalConfig = this.globalConfigurationHelper
				.getAllGlobalConfigurations(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(globalConfig);

		String configName = "force-password-reset-days";
		String newValue = "0";
		String newBooleanValue = "true";

		for (Integer configIndex = 0; configIndex < (globalConfig.size() - 1); configIndex++) {
			if (globalConfig.get(configIndex).get("name").equals(configName)) {
				Integer configId = (Integer) globalConfig.get(configIndex).get(
						"id");
				Assert.assertNotNull(configId);

				/*
				 * Update force-password-reset-days with value as 0 and Enable
				 * as true - failure case
				 */
				ArrayList error = (ArrayList) this.globalConfigurationHelper
						.updatePasswordResetDaysForGlobalConfiguration(
								this.requestSpec, this.httpStatusForidden,
								configId, newValue, newBooleanValue,
								CommonConstants.RESPONSE_ERROR);
				HashMap hash = (HashMap) error.get(0);

				Assert.assertEquals(
						"Force Password Reset days value must be greater than zero.",
						"error.msg.password.reset.days.value.must.be.greater.than.zero",
						hash.get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

				/*
				 * Update force-password-reset-days with value as 50 and Enable
				 * as true - success case
				 */
				final HashMap updateSuccess = (HashMap) this.globalConfigurationHelper
						.updatePasswordResetDaysForGlobalConfiguration(
								this.requestSpec, this.responseSpec, configId,
								"50", newBooleanValue, "changes");
				Assert.assertNotNull(updateSuccess);

				/* Update with value as 0 and Enable as false - success case */
				final HashMap updateSuccess1 = (HashMap) this.globalConfigurationHelper
						.updatePasswordResetDaysForGlobalConfiguration(
								this.requestSpec, this.responseSpec, configId,
								newValue, "false", "changes");
				Assert.assertNotNull(updateSuccess1);

				/* Update without sending value and Enable as true - failure case*/
				ArrayList failure = (ArrayList) this.globalConfigurationHelper
						.updatePasswordResetDaysForGlobalConfiguration(
								this.requestSpec, this.httpStatusForidden, configId,
								null, newBooleanValue, CommonConstants.RESPONSE_ERROR);
				HashMap failureHash = (HashMap) failure.get(0);
				Assert.assertEquals(
						"Force Password Reset days value must be greater than zero.",
						"error.msg.password.reset.days.value.must.be.greater.than.zero",
						failureHash.get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

				break;
			}
		}
		/* Update other global configuration property */
		String otherConfigName = "maker-checker";
		for (Integer configIndex = 0; configIndex < (globalConfig.size() - 1); configIndex++) {
			if (globalConfig.get(configIndex).get("name")
					.equals(otherConfigName)) {
				String configId = (globalConfig.get(configIndex).get("id"))
						.toString();
				Integer updateConfigId = this.globalConfigurationHelper
						.updateValueForGlobalConfiguration(this.requestSpec,
								this.responseSpec, configId, newValue);
				Assert.assertNotNull(updateConfigId);
				break;
			}
		}
	}
}