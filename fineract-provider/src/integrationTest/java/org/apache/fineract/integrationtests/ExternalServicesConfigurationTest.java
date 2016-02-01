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

import org.apache.fineract.integrationtests.common.ExternalServicesConfigurationHelper;
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
                if(value == null){
                    value = "testnull";
                }
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
                if(value == null){
                    value = "testnull";
                }
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
