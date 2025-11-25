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
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;

public class SavingsAccountsBirthdaySearchIntegrationTest {

    // Added constants used in the test
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
    }

    @Test
    public void testSavingsAccountWithBirthday() {
        final Integer matchingClientId = ClientHelper.createClientWithBDay(this.requestSpec, this.responseSpec, ClientHelper.DEFAULT_DATE, ClientHelper.DEFAULT_OFFICE_ID, "12 August 1990");
        Assertions.assertNotNull(matchingClientId);

        final Integer nonMatchingClientId = ClientHelper.createClientWithBDay(this.requestSpec, this.responseSpec, ClientHelper.DEFAULT_DATE, ClientHelper.DEFAULT_OFFICE_ID, "15 August 1990");
        Assertions.assertNotNull(nonMatchingClientId);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(savingsProductID);

        final Integer matchingSavingsId = this.savingsAccountHelper.applyForSavingsApplication(matchingClientId, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(matchingSavingsId);
        this.savingsAccountHelper.updateSavingsAccount(matchingClientId, savingsProductID, matchingSavingsId, ACCOUNT_TYPE_INDIVIDUAL);
        this.savingsAccountHelper.approveSavings(matchingSavingsId);
        this.savingsAccountHelper.activateSavings(matchingSavingsId);

        final Integer nonMatchingSavingsId = this.savingsAccountHelper.applyForSavingsApplication(nonMatchingClientId, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assertions.assertNotNull(nonMatchingSavingsId);
        this.savingsAccountHelper.updateSavingsAccount(nonMatchingClientId, savingsProductID, nonMatchingSavingsId, ACCOUNT_TYPE_INDIVIDUAL);
        this.savingsAccountHelper.approveSavings(nonMatchingSavingsId);
        this.savingsAccountHelper.activateSavings(nonMatchingSavingsId);

        final List<Map<String, Object>> pageItems = SavingsAccountHelper.getSavingsByBirthday(this.requestSpec, this.responseSpec, "8", "12");

        Assertions.assertNotNull(pageItems);
        assertEquals(1, pageItems.size());
        Map<String, Object> returned = pageItems.get(0);

        assertEquals(matchingClientId, returned.get("clientId"));
        assertEquals(matchingSavingsId, returned.get("id"));

        boolean nonMatchingPresent = pageItems.stream().anyMatch(m -> (m.get("clientId")) == nonMatchingClientId);
        Assertions.assertFalse(nonMatchingPresent);
    }

    @Test
    public void testInvalidBirthMonthNonIntegerReturnsBadRequest() {
        final ResponseSpecification responseSpec400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        Response response = SavingsAccountHelper.getSavingsByBirthdayGetRaw(this.requestSpec, responseSpec400, "notAnInt", "10");
        assertEquals(400, response.statusCode());
        final String json = response.asString();
        final java.util.List<java.util.Map<String, Object>> errors = JsonPath.from(json).getList("errors");
        Assertions.assertNotNull(errors);
        final String devMsg = JsonPath.from(json).getString("errors[0].developerMessage");
        final String expectedDevMsg = "The query parameter invalid field: birthMonth has an unsupported value of: invalid value: notAnInt";
        Assertions.assertEquals(expectedDevMsg, devMsg);
        final java.util.List<String> supported = JsonPath.from(json).getList("errors[0].args[2].value");
        Assertions.assertNotNull(supported);
        Assertions.assertTrue(supported.contains("birthMonth must be a integer between 1 to 12"));
    }

    @Test
    public void testInvalidBirthMonthOutOfRangeReturnsBadRequest() {
        final ResponseSpecification responseSpec400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        Response response = SavingsAccountHelper.getSavingsByBirthdayGetRaw(this.requestSpec, responseSpec400, "13", "10");
        assertEquals(400, response.statusCode());
        final String json = response.asString();
        final java.util.List<java.util.Map<String, Object>> errors = JsonPath.from(json).getList("errors");
        Assertions.assertNotNull(errors);
        final String devMsg = JsonPath.from(json).getString("errors[0].developerMessage");
        final String expectedDevMsg = "The query parameter invalid field: birthMonth has an unsupported value of: invalid value: 13";
        Assertions.assertEquals(expectedDevMsg, devMsg);
        final java.util.List<String> supported = JsonPath.from(json).getList("errors[0].args[2].value");
        Assertions.assertNotNull(supported);
        Assertions.assertTrue(supported.contains("birthMonth must be a integer between 1 to 12"));
    }

    @Test
    public void testMissingBirthDayWhenMonthProvidedReturnsBadRequest() {
        final ResponseSpecification responseSpec400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        Response response = SavingsAccountHelper.getSavingsByBirthdayGetRaw(this.requestSpec, responseSpec400, "5", "");
        assertEquals(400, response.statusCode());
        final String json = response.asString();
        final java.util.List<String> supported = JsonPath.from(json).getList("errors[0].args[2].value");
        Assertions.assertNotNull(supported);
        Assertions.assertTrue(supported.contains("Both birthMonth and birthDay must be provided together"));
    }

    @Test
    public void testInvalidBirthDayOutOfRangeReturnsBadRequest() {
        final ResponseSpecification responseSpec400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        Response response = SavingsAccountHelper.getSavingsByBirthdayGetRaw(this.requestSpec, responseSpec400, "10", "33");
        assertEquals(400, response.statusCode());
        final String json = response.asString();
        final String devMsg = JsonPath.from(json).getString("errors[0].developerMessage");
        final String expectedDevMsg = "The query parameter invalid field: birthDay has an unsupported value of: invalid value: 33";
        Assertions.assertEquals(expectedDevMsg, devMsg);
        final java.util.List<String> supported = JsonPath.from(json).getList("errors[0].args[2].value");
        Assertions.assertNotNull(supported);
        Assertions.assertTrue(supported.contains("birthDay for month 10 must be between 1 and 31"));
    }

    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsMonthly()
                .withInterestCalculationPeriodTypeAsDailyBalance()
                .withMinimumOpenningBalance(SavingsAccountsBirthdaySearchIntegrationTest.MINIMUM_OPENING_BALANCE)
                .build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }
}
