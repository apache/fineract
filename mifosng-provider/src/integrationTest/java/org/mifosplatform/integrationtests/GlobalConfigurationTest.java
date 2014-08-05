/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.GlobalConfigurationHelper;

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

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
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
}