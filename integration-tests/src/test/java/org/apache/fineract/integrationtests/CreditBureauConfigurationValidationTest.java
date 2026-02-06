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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.CreditBureauConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Slf4j
public class CreditBureauConfigurationValidationTest {

    // Prerequisites - ThitsaWorks credit bureau is seeded in DB with ID 1
    private static final Long VALID_CREDIT_BUREAU_ID = 1L;
    private Long validOrganisationCreditBureauId;
    private Long validLoanProductId;

    @BeforeEach
    public void setup() {
        ensureOrganisationCreditBureauExists();
        this.validLoanProductId = createTestLoanProduct();
    }

    @ParameterizedTest(name = "Create configuration missing {0} should return 400")
    @CsvSource({ "configkey, value, description", "value, configkey, description", "description, configkey, value" })
    void testCreateConfiguration_MissingMandatoryFields(String fieldToOmit, String field1, String field2) {
        final Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put(field1, "testValue1");
        jsonMap.put(field2, "testValue2");
        final String jsonBody = new Gson().toJson(jsonMap);

        CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> CreditBureauConfigurationHelper.createCreditBureauConfigurationRaw(validOrganisationCreditBureauId, jsonBody));

        assertEquals(400, ex.getResponse().code());
        assertValidationErrorInMessage(ex.getMessage(), fieldToOmit);
    }

    @Test
    void testCreateConfiguration_BlankConfigKey_ShouldFail400() {
        final Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("configkey", "");
        jsonMap.put("value", "testValue");
        jsonMap.put("description", "testDescription");
        final String jsonBody = new Gson().toJson(jsonMap);

        CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> CreditBureauConfigurationHelper.createCreditBureauConfigurationRaw(validOrganisationCreditBureauId, jsonBody));

        assertEquals(400, ex.getResponse().code());
        assertValidationErrorInMessage(ex.getMessage(), "configkey");
    }

    @Test
    void testCreateConfiguration_ExceedingLength_ShouldFail400() {
        final String longValue = "a".repeat(101);
        final Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("configkey", longValue);
        jsonMap.put("value", "testValue");
        jsonMap.put("description", "testDescription");
        final String jsonBody = new Gson().toJson(jsonMap);

        CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> CreditBureauConfigurationHelper.createCreditBureauConfigurationRaw(validOrganisationCreditBureauId, jsonBody));

        assertEquals(400, ex.getResponse().code());
        assertValidationErrorInMessage(ex.getMessage(), "configkey");
    }

    @Test
    void testAddOrganisationCreditBureau_MissingAlias_ShouldFail400() {
        final Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("isActive", true);
        final String jsonBody = new Gson().toJson(jsonMap);

        CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> CreditBureauConfigurationHelper.addOrganisationCreditBureauRaw(VALID_CREDIT_BUREAU_ID, jsonBody));

        assertEquals(400, ex.getResponse().code());
        assertValidationErrorInMessage(ex.getMessage(), "alias");
    }

    @Test
    void testAddOrganisationCreditBureau_BlankAlias_ShouldFail400() {
        final Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("alias", "");
        jsonMap.put("isActive", true);
        final String jsonBody = new Gson().toJson(jsonMap);

        CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> CreditBureauConfigurationHelper.addOrganisationCreditBureauRaw(VALID_CREDIT_BUREAU_ID, jsonBody));

        assertEquals(400, ex.getResponse().code());
        assertValidationErrorInMessage(ex.getMessage(), "alias");
    }

    @Test
    void testAddOrganisationCreditBureau_ExceedingAliasLength_ShouldFail400() {
        final String longAlias = "a".repeat(101);
        final Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("alias", longAlias);
        jsonMap.put("isActive", true);
        final String jsonBody = new Gson().toJson(jsonMap);

        CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> CreditBureauConfigurationHelper.addOrganisationCreditBureauRaw(VALID_CREDIT_BUREAU_ID, jsonBody));

        assertEquals(400, ex.getResponse().code());
        assertValidationErrorInMessage(ex.getMessage(), "alias");
    }

    @ParameterizedTest(name = "Create mapping missing {0} should return 400")
    @CsvSource({ "isCreditcheckMandatory", "skipCreditcheckInFailure", "stalePeriod" })
    void testCreateMapping_MissingMandatoryFields(String fieldToOmit) {
        final Map<String, Object> jsonMap = buildMappingJsonOmitting(fieldToOmit);
        final String jsonBody = new Gson().toJson(jsonMap);

        CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> CreditBureauConfigurationHelper.createLoanProductMappingRaw(validOrganisationCreditBureauId, jsonBody));

        assertEquals(400, ex.getResponse().code());
        assertValidationErrorInMessage(ex.getMessage(), fieldToOmit);
    }

    @Test
    void testCreateMapping_MissingLoanProductId_ShouldFail400() {
        final Map<String, Object> jsonMap = buildMappingJsonOmitting("loanProductId");
        final String jsonBody = new Gson().toJson(jsonMap);

        CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> CreditBureauConfigurationHelper.createLoanProductMappingRaw(validOrganisationCreditBureauId, jsonBody));

        assertEquals(400, ex.getResponse().code());
        assertValidationErrorInMessage(ex.getMessage(), "loanProductId");
    }

    private void ensureOrganisationCreditBureauExists() {
        String response = CreditBureauConfigurationHelper.addOrganisationCreditBureau(VALID_CREDIT_BUREAU_ID,
                "Test Credit Bureau " + System.currentTimeMillis(), true);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        Integer resourceId = json.get("resourceId").getAsInt();
        assertNotNull(resourceId, "Organisation credit bureau creation should return resourceId");
        this.validOrganisationCreditBureauId = resourceId.longValue();
        log.info("Created organisation credit bureau with ID: {}", validOrganisationCreditBureauId);
    }

    private Long createTestLoanProduct() {
        Utils.initializeRESTAssured();
        final RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        final ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        final LoanTransactionHelper loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1")
                .withRepaymentTypeAsMonth().withNumberOfRepayments("1").withInterestRateFrequencyTypeAsMonths()
                .withinterestRatePerPeriod("0").withInterestTypeAsDecliningBalance().withAmortizationTypeAsEqualInstallments().build(null);
        return (long) loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Map<String, Object> buildMappingJsonOmitting(String fieldToOmit) {
        final Map<String, Object> jsonMap = new HashMap<>();
        if (!"loanProductId".equals(fieldToOmit)) {
            jsonMap.put("loanProductId", validLoanProductId);
        }
        if (!"isCreditcheckMandatory".equals(fieldToOmit)) {
            jsonMap.put("isCreditcheckMandatory", true);
        }
        if (!"skipCreditcheckInFailure".equals(fieldToOmit)) {
            jsonMap.put("skipCreditcheckInFailure", false);
        }
        if (!"stalePeriod".equals(fieldToOmit)) {
            jsonMap.put("stalePeriod", 30);
        }
        jsonMap.put("isActive", true);
        return jsonMap;
    }

    private void assertValidationErrorInMessage(String message, String expectedFieldInError) {
        assertNotNull(message, "Exception message should not be null");
        assertTrue(message.contains(expectedFieldInError),
                String.format("Expected validation error for field '%s' in message: %s", expectedFieldInError, message));
        log.info("Received expected validation error for field '{}'", expectedFieldInError);
    }
}
