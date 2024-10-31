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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.PasswordPreferencesHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PasswordPreferencesIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordPreferencesIntegrationTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ResponseSpecification generalResponseSpec;
    private int originalPasswordPolicyId;

    @BeforeEach
    public void setUp() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.generalResponseSpec = new ResponseSpecBuilder().build();
        originalPasswordPolicyId = PasswordPreferencesHelper.getActivePasswordPreference(requestSpec, responseSpec);
    }

    @AfterEach
    void tearDown() {
        PasswordPreferencesHelper.updatePasswordPreferences(requestSpec, responseSpec, Integer.toString(originalPasswordPolicyId));
    }

    @Test
    public void updatePasswordPreferences() {
        String validationPolicyId = "2";
        PasswordPreferencesHelper.updatePasswordPreferences(requestSpec, responseSpec, validationPolicyId);
        this.validateIfThePasswordIsUpdated(validationPolicyId);
    }

    private void validateIfThePasswordIsUpdated(String validationPolicyId) {
        Integer id = PasswordPreferencesHelper.getActivePasswordPreference(requestSpec, responseSpec);
        assertEquals(validationPolicyId, id.toString());
        LOG.info("---------------------------------PASSWORD PREFERENCE VALIDATED SUCCESSFULLY-----------------------------------------");

    }

    @Test
    public void updateWithInvalidPolicyId() {
        String invalidValidationPolicyId = "2000";
        final List<HashMap> error = (List) PasswordPreferencesHelper.updateWithInvalidValidationPolicyId(requestSpec, generalResponseSpec,
                invalidValidationPolicyId, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.password.validation.policy.id.invalid", error.get(0).get("userMessageGlobalisationCode"),
                "Password Validation Policy with identifier 2000 does not exist");
    }

}
