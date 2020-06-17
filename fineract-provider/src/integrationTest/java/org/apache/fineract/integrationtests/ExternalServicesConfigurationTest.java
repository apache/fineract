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
import org.apache.fineract.integrationtests.common.ExternalServicesConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalServicesConfigurationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalServicesConfigurationTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ExternalServicesConfigurationHelper externalServicesConfigurationHelper;
    private ResponseSpecification httpStatusForidden;

    @BeforeEach
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
        ArrayList<HashMap> externalServicesConfig = ExternalServicesConfigurationHelper
                .getExternalServicesConfigurationByServiceName(requestSpec, responseSpec, "S3");
        Assertions.assertNotNull(externalServicesConfig);
        for (Integer configIndex = 0; configIndex < externalServicesConfig.size(); configIndex++) {
            String name = (String) externalServicesConfig.get(configIndex).get("name");
            String value = null;
            if (name.equals(configName)) {
                value = (String) externalServicesConfig.get(configIndex).get("value");
                if (value == null) {
                    value = "testnull";
                }
                String newValue = "test";
                LOG.info("{} : {}", name, value);
                HashMap arrayListValue = ExternalServicesConfigurationHelper.updateValueForExternaServicesConfiguration(requestSpec,
                        responseSpec, "S3", name, newValue);
                Assertions.assertNotNull(arrayListValue.get("value"));
                Assertions.assertEquals(arrayListValue.get("value"), newValue);
                HashMap arrayListValue1 = ExternalServicesConfigurationHelper.updateValueForExternaServicesConfiguration(requestSpec,
                        responseSpec, "S3", name, value);
                Assertions.assertNotNull(arrayListValue1.get("value"));
                Assertions.assertEquals(arrayListValue1.get("value"), value);
            }

        }

        // Checking for SMTP:

        configName = "username";
        externalServicesConfig = ExternalServicesConfigurationHelper.getExternalServicesConfigurationByServiceName(requestSpec,
                responseSpec, "SMTP");
        Assertions.assertNotNull(externalServicesConfig);

        for (Integer configIndex = 0; configIndex < externalServicesConfig.size(); configIndex++) {
            String name = (String) externalServicesConfig.get(configIndex).get("name");
            String value = null;
            if (name.equals(configName)) {
                value = (String) externalServicesConfig.get(configIndex).get("value");
                if (value == null) {
                    value = "testnull";
                }
                String newValue = "test";
                LOG.info("{} : {}", name, value);
                HashMap arrayListValue = ExternalServicesConfigurationHelper.updateValueForExternaServicesConfiguration(requestSpec,
                        responseSpec, "SMTP", name, newValue);
                Assertions.assertNotNull(arrayListValue.get("value"));
                Assertions.assertEquals(arrayListValue.get("value"), newValue);
                HashMap arrayListValue1 = ExternalServicesConfigurationHelper.updateValueForExternaServicesConfiguration(requestSpec,
                        responseSpec, "SMTP", name, value);
                Assertions.assertNotNull(arrayListValue1.get("value"));
                Assertions.assertEquals(arrayListValue1.get("value"), value);
            }

        }

    }

}
