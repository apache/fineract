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

        // Updating Value for penalty-wait-period Global Configuration
        Integer configId = (Integer) globalConfig.get(7).get("id");
        Assert.assertNotNull(configId);

        HashMap configDataBefore = this.globalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                configId.toString());
        Assert.assertNotNull(configDataBefore);

        Integer value = Utils.randomValueGenerator(1, 5);

        configId = this.globalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec,
                configId.toString(), value.toString());
        Assert.assertNotNull(configId);

        HashMap configDataAfter = this.globalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                configId.toString());

        // Verifying Value for penalty-wait-period after Updation
        Assert.assertEquals("Verifying Global Config Value after Updation", value, configDataAfter.get("value"));

        // Updating Enabled Flag for penalty-wait-period Global Configuration
        Boolean enabled = (Boolean) globalConfig.get(7).get("enabled");

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

    }

    @Test
    public void testGlobalConfigurationIsCacheEnabled() {
        this.globalConfigurationHelper = new GlobalConfigurationHelper(this.requestSpec, this.responseSpec);

        // Retrieving Is Cache Enabled Global Configuration details
        final ArrayList<HashMap> isCacheGlobalConfig = this.globalConfigurationHelper.getGlobalConfigurationIsCacheEnabled(
                this.requestSpec, this.responseSpec);
        Assert.assertNotNull(isCacheGlobalConfig);
        Integer cacheType = Utils.randomValueGenerator(0, 1);
        Boolean enabled = (Boolean) isCacheGlobalConfig.get(cacheType).get("enabled");

        if (cacheType == 0 && enabled == true) {
            cacheType = 1;
        } else if (cacheType == 1 && enabled == true) {
            cacheType = 0;
        }
        cacheType += 1;
        HashMap changes = this.globalConfigurationHelper.updateIsCacheEnabledForGlobalConfiguration(this.requestSpec, this.responseSpec,
                cacheType.toString());
        Assert.assertEquals("Verifying Is Cache Enabled Global Config after Updation", cacheType, changes.get("cacheType"));
    }
}