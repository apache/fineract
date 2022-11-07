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
import java.util.Map;
import org.apache.fineract.integrationtests.common.ExternalEventConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExternalEventConfigurationIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

    }

    @Test
    public void getExternalEventConfigurations() {
        final ArrayList<Map<String, Object>> externalEventConfigurations = ExternalEventConfigurationHelper
                .getAllExternalEventConfigurations(requestSpec, responseSpec);
        Assertions.assertNotNull(externalEventConfigurations);
        final ArrayList<Map<String, Object>> defaultConfigurations = ExternalEventConfigurationHelper
                .getDefaultExternalEventConfigurations();
        Assertions.assertEquals(defaultConfigurations.size(), externalEventConfigurations.size());
        verifyAllEventConfigurations(externalEventConfigurations, defaultConfigurations);

    }

    private void verifyAllEventConfigurations(ArrayList<Map<String, Object>> actualEventConfigurations,
            ArrayList<Map<String, Object>> defaultConfigurations) {

        for (int index = 0; index < actualEventConfigurations.size(); index++) {
            Assertions.assertTrue(defaultConfigurations.contains(actualEventConfigurations.get(index)));
        }
    }

    @Test
    public void updateExternalEventConfigurations() {
        String updateRequestJson = ExternalEventConfigurationHelper.getExternalEventConfigurationsForUpdateJSON();
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, updateRequestJson);
        Assertions.assertEquals(updatedConfigurations.size(), 2);
        Assertions.assertTrue(updatedConfigurations.containsKey("CentersCreateBusinessEvent"));
        Assertions.assertTrue(updatedConfigurations.containsKey("ClientActivateBusinessEvent"));
        Assertions.assertTrue(updatedConfigurations.get("CentersCreateBusinessEvent"));
        Assertions.assertTrue(updatedConfigurations.get("ClientActivateBusinessEvent"));

    }

    @AfterEach
    public void tearDown() {
        ExternalEventConfigurationHelper.resetDefaultConfigurations(requestSpec, responseSpec);
    }

}
