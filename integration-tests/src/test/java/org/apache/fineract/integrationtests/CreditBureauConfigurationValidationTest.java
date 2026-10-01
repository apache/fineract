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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostCreditBureauConfigurationRequest;
import org.apache.fineract.client.models.PostCreditBureauLoanProductMappingRequest;
import org.apache.fineract.client.models.PostOrganisationCreditBureauRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCreditBureauHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Slf4j
public class CreditBureauConfigurationValidationTest extends FeignIntegrationTest {

    // Prerequisites - ThitsaWorks credit bureau is seeded in DB with ID 1
    private static final Long VALID_CREDIT_BUREAU_ID = 1L;
    private Long validOrganisationCreditBureauId;
    private Long validLoanProductId;
    private FeignCreditBureauHelper creditBureauHelper;

    @BeforeEach
    public void setup() {
        creditBureauHelper = new FeignCreditBureauHelper(fineractClient());
        ensureOrganisationCreditBureauExists();
        this.validLoanProductId = createTestLoanProduct();
    }

    @ParameterizedTest(name = "Create configuration missing {0} should return 400")
    @CsvSource({ "configkey, value, description", "value, configkey, description", "description, configkey, value" })
    void testCreateConfiguration_MissingMandatoryFields(String fieldToOmit, String field1, String field2) {
        final PostCreditBureauConfigurationRequest request = new PostCreditBureauConfigurationRequest();
        setConfigurationField(request, field1, "testValue1");
        setConfigurationField(request, field2, "testValue2");

        CallFailedRuntimeException ex = creditBureauHelper.createConfigurationExpectingError(validOrganisationCreditBureauId, request);

        assertEquals(400, ex.getStatus());
        assertValidationErrorFor(ex, fieldToOmit);
    }

    @Test
    void testCreateConfiguration_BlankConfigKey_ShouldFail400() {
        final PostCreditBureauConfigurationRequest request = new PostCreditBureauConfigurationRequest().configkey("").value("testValue")
                .description("testDescription");

        CallFailedRuntimeException ex = creditBureauHelper.createConfigurationExpectingError(validOrganisationCreditBureauId, request);

        assertEquals(400, ex.getStatus());
        assertValidationErrorFor(ex, "configkey");
    }

    @Test
    void testCreateConfiguration_ExceedingLength_ShouldFail400() {
        final String longValue = "a".repeat(101);
        final PostCreditBureauConfigurationRequest request = new PostCreditBureauConfigurationRequest().configkey(longValue)
                .value("testValue").description("testDescription");

        CallFailedRuntimeException ex = creditBureauHelper.createConfigurationExpectingError(validOrganisationCreditBureauId, request);

        assertEquals(400, ex.getStatus());
        assertValidationErrorFor(ex, "configkey");
    }

    @Test
    void testAddOrganisationCreditBureau_MissingAlias_ShouldFail400() {
        final PostOrganisationCreditBureauRequest request = new PostOrganisationCreditBureauRequest().isActive(true);

        CallFailedRuntimeException ex = creditBureauHelper.addOrganisationCreditBureauExpectingError(VALID_CREDIT_BUREAU_ID, request);

        assertEquals(400, ex.getStatus());
        assertValidationErrorFor(ex, "alias");
    }

    @Test
    void testAddOrganisationCreditBureau_BlankAlias_ShouldFail400() {
        final PostOrganisationCreditBureauRequest request = new PostOrganisationCreditBureauRequest().alias("").isActive(true);

        CallFailedRuntimeException ex = creditBureauHelper.addOrganisationCreditBureauExpectingError(VALID_CREDIT_BUREAU_ID, request);

        assertEquals(400, ex.getStatus());
        assertValidationErrorFor(ex, "alias");
    }

    @Test
    void testAddOrganisationCreditBureau_ExceedingAliasLength_ShouldFail400() {
        final String longAlias = "a".repeat(101);
        final PostOrganisationCreditBureauRequest request = new PostOrganisationCreditBureauRequest().alias(longAlias).isActive(true);

        CallFailedRuntimeException ex = creditBureauHelper.addOrganisationCreditBureauExpectingError(VALID_CREDIT_BUREAU_ID, request);

        assertEquals(400, ex.getStatus());
        assertValidationErrorFor(ex, "alias");
    }

    @ParameterizedTest(name = "Create mapping missing {0} should return 400")
    @CsvSource({ "isCreditcheckMandatory", "skipCreditcheckInFailure", "stalePeriod" })
    void testCreateMapping_MissingMandatoryFields(String fieldToOmit) {
        final PostCreditBureauLoanProductMappingRequest request = buildMappingRequestOmitting(fieldToOmit);

        CallFailedRuntimeException ex = creditBureauHelper.createLoanProductMappingExpectingError(validOrganisationCreditBureauId, request);

        assertEquals(400, ex.getStatus());
        assertValidationErrorFor(ex, fieldToOmit);
    }

    @Test
    void testCreateMapping_MissingLoanProductId_ShouldFail400() {
        final PostCreditBureauLoanProductMappingRequest request = buildMappingRequestOmitting("loanProductId");

        CallFailedRuntimeException ex = creditBureauHelper.createLoanProductMappingExpectingError(validOrganisationCreditBureauId, request);

        assertEquals(400, ex.getStatus());
        assertValidationErrorFor(ex, "loanProductId");
    }

    private void ensureOrganisationCreditBureauExists() {
        Long resourceId = creditBureauHelper
                .addOrganisationCreditBureau(VALID_CREDIT_BUREAU_ID, "Test Credit Bureau " + System.currentTimeMillis(), true)
                .getResourceId();
        assertNotNull(resourceId, "Organisation credit bureau creation should return resourceId");
        this.validOrganisationCreditBureauId = resourceId;
        log.info("Created organisation credit bureau with ID: {}", validOrganisationCreditBureauId);
    }

    private Long createTestLoanProduct() {
        return new FeignLoanHelper(fineractClient())
                .createLoanProduct(
                        new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1").withRepaymentTypeAsMonth()
                                .withNumberOfRepayments("1").withInterestRateFrequencyTypeAsMonths().withinterestRatePerPeriod("0")
                                .withInterestTypeAsDecliningBalance().withAmortizationTypeAsEqualInstallments().buildRequest())
                .getResourceId();
    }

    private static void setConfigurationField(PostCreditBureauConfigurationRequest request, String field, String value) {
        switch (field) {
            case "configkey" -> request.configkey(value);
            case "value" -> request.value(value);
            case "description" -> request.description(value);
            default -> throw new IllegalArgumentException("Unknown configuration field " + field);
        }
    }

    private PostCreditBureauLoanProductMappingRequest buildMappingRequestOmitting(String fieldToOmit) {
        final PostCreditBureauLoanProductMappingRequest request = new PostCreditBureauLoanProductMappingRequest();
        if (!"loanProductId".equals(fieldToOmit)) {
            request.loanProductId(validLoanProductId);
        }
        if (!"isCreditcheckMandatory".equals(fieldToOmit)) {
            request.isCreditcheckMandatory(true);
        }
        if (!"skipCreditcheckInFailure".equals(fieldToOmit)) {
            request.skipCreditcheckInFailure(false);
        }
        if (!"stalePeriod".equals(fieldToOmit)) {
            request.stalePeriod(30);
        }
        request.isActive(true);
        return request;
    }

    private void assertValidationErrorFor(CallFailedRuntimeException exception, String expectedFieldInError) {
        final List<String> fieldsInError = FeignErrors.reportedErrors(exception).stream().map(FeignErrors.ReportedError::parameterName)
                .toList();
        assertTrue(fieldsInError.contains(expectedFieldInError),
                String.format("Expected validation error for field '%s', got errors for %s", expectedFieldInError, fieldsInError));
        log.info("Received expected validation error for field '{}'", expectedFieldInError);
    }
}
