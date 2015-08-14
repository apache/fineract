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
import org.mifosplatform.integrationtests.common.ExternalServicesConfigurationHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked", "static-access" })
public class ExternalServicesConfigurationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ExternalServicesConfigurationHelper externalServicesConfigurationHelper;
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
    public void testExternalServicesConfiguration() {
        this.externalServicesConfigurationHelper = new ExternalServicesConfigurationHelper(this.requestSpec, this.responseSpec);

        // Checking for S3
        String configName = "s3_access_key";
        ArrayList<HashMap> externalServicesConfig = this.externalServicesConfigurationHelper
                .getExternalServicesConfigurationByServiceName(requestSpec, responseSpec, "S3");
        Assert.assertNotNull(externalServicesConfig);
        for (Integer configIndex = 0; configIndex < (externalServicesConfig.size()); configIndex++) {
            String name = (String) externalServicesConfig.get(configIndex).get("name");
            String value = null;
            if (name.equals(configName)) {
                value = (String) externalServicesConfig.get(configIndex).get("value");
                String newValue = "test";
                System.out.println(name + ":" + value);
                HashMap arrayListValue = this.externalServicesConfigurationHelper.updateValueForExternaServicesConfiguration(requestSpec,
                        responseSpec, "S3", name, newValue);
                Assert.assertNotNull(arrayListValue.get("value"));
                Assert.assertEquals(arrayListValue.get("value"), newValue);
                HashMap arrayListValue1 = this.externalServicesConfigurationHelper.updateValueForExternaServicesConfiguration(requestSpec,
                        responseSpec, "S3", name, value);
                Assert.assertNotNull(arrayListValue1.get("value"));
                Assert.assertEquals(arrayListValue1.get("value"), value);
            }

        }

        // Checking for SMTP:

        configName = "username";
        externalServicesConfig = this.externalServicesConfigurationHelper.getExternalServicesConfigurationByServiceName(requestSpec,
                responseSpec, "SMTP");
        Assert.assertNotNull(externalServicesConfig);
        for (Integer configIndex = 0; configIndex < (externalServicesConfig.size()); configIndex++) {
            String name = (String) externalServicesConfig.get(configIndex).get("name");
            String value = null;
            if (name.equals(configName)) {
                value = (String) externalServicesConfig.get(configIndex).get("value");
                String newValue = "test";
                System.out.println(name + ":" + value);
                HashMap arrayListValue = this.externalServicesConfigurationHelper.updateValueForExternaServicesConfiguration(requestSpec,
                        responseSpec, "SMTP", name, newValue);
                Assert.assertNotNull(arrayListValue.get("value"));
                Assert.assertEquals(arrayListValue.get("value"), newValue);
                HashMap arrayListValue1 = this.externalServicesConfigurationHelper.updateValueForExternaServicesConfiguration(requestSpec,
                        responseSpec, "SMTP", name, value);
                Assert.assertNotNull(arrayListValue1.get("value"));
                Assert.assertEquals(arrayListValue1.get("value"), value);
            }

        }

    }

}
