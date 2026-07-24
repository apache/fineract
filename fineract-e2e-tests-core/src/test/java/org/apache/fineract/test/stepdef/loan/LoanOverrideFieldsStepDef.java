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
package org.apache.fineract.test.stepdef.loan;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.test.data.loanproduct.DefaultLoanProduct;
import org.apache.fineract.test.data.loanproduct.LoanProductResolver;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class LoanOverrideFieldsStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private final FineractFeignClient fineractClient;
    private final LoanRequestFactory loanRequestFactory;
    private final LoanProductResolver loanProductResolver;

    @Then("LoanDetails has {string} field with value: {string}")
    public void checkLoanDetailsFieldWithValue(final String fieldName, final String expectedValue) throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse);
        final Long loanId = loanResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveOneLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));

        assertNotNull(loanDetails);

        verifyFieldValue(loanDetails, fieldName, expectedValue);
    }

    private void verifyFieldValue(final GetLoansLoanIdResponse loanDetails, final String fieldName, final String expectedValue) {
        final Integer actualValue = getIntFieldValue(loanDetails, fieldName);
        final Integer expected = Integer.valueOf(expectedValue);
        assertThat(actualValue).as("Expected %s to be %d but was %s", fieldName, expected, actualValue).isEqualTo(expected);
    }

    private Integer getIntFieldValue(final GetLoansLoanIdResponse loanDetails, final String fieldName) {
        return switch (fieldName) {
            case "inArrearsTolerance" -> loanDetails.getInArrearsTolerance();
            case "graceOnPrincipalPayment" -> loanDetails.getGraceOnPrincipalPayment();
            case "graceOnInterestPayment" -> loanDetails.getGraceOnInterestPayment();
            case "graceOnArrearsAgeing" -> loanDetails.getGraceOnArrearsAgeing();
            case "interestType" -> loanDetails.getInterestType().getId().intValue();
            case "amortizationType" -> loanDetails.getAmortizationType().getId().intValue();
            case "interestCalculationPeriodType" -> loanDetails.getInterestCalculationPeriodType().getId().intValue();
            case "repaymentEvery" -> loanDetails.getRepaymentEvery();
            case "principal" -> loanDetails.getPrincipal().intValue();
            default -> throw new IllegalArgumentException("Unknown override field: " + fieldName);
        };
    }

    @When("Admin creates a new Loan with the following override data:")
    public void createLoanWithOverrideData(final DataTable dataTable) throws IOException {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        assertNotNull(clientResponse);
        final Long clientId = clientResponse.getClientId();

        final Map<String, String> overrideData = dataTable.asMap(String.class, String.class);

        final String loanProductName = overrideData.get("loanProduct");
        if (loanProductName == null) {
            throw new IllegalArgumentException("loanProduct is required in override data");
        }

        final PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)
                .productId(loanProductResolver.resolve(DefaultLoanProduct.valueOf(loanProductName))).numberOfRepayments(6)
                .loanTermFrequency(180).interestRatePerPeriod(new BigDecimal(1));

        overrideData.forEach((fieldName, value) -> {
            if (!"loanProduct".equals(fieldName)) {
                applyOverrideField(loansRequest, fieldName, value);
            }
        });

        final PostLoansResponse response = ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);

    }

    private void applyOverrideField(final PostLoansRequest request, final String fieldName, final String value) {
        final boolean isNull = "null".equals(value);

        switch (fieldName) {
            case "inArrearsTolerance" -> request.inArrearsTolerance(isNull ? null : new BigDecimal(value));
            case "graceOnInterestPayment" -> request.graceOnInterestPayment(isNull ? null : Integer.valueOf(value));
            case "graceOnPrincipalPayment" -> request.graceOnPrincipalPayment(isNull ? null : Integer.valueOf(value));
            case "graceOnArrearsAgeing" -> request.graceOnArrearsAgeing(isNull ? null : Integer.valueOf(value));
            case "interestType" -> request.interestType(isNull ? null : Integer.valueOf(value));
            case "amortizationType" -> request.amortizationType(isNull ? null : Integer.valueOf(value));
            case "interestCalculationPeriodType" -> request.interestCalculationPeriodType(isNull ? null : Integer.valueOf(value));
            case "repaymentEvery" -> request.repaymentEvery(isNull ? null : Integer.valueOf(value));
            default -> throw new IllegalArgumentException("Unknown override field: " + fieldName);
        }
    }

    @When("Admin modifies the loan, changing principal to {string} and omitting the override-enabled schedule fields")
    public void modifyLoanOmittingOverrideEnabledScheduleFields(final String newPrincipal) throws IOException {
        modifyLoanPrincipal(newPrincipal, null);
    }

    @When("Admin modifies the loan, changing principal to {string} and setting interestType to {string}")
    public void modifyLoanSettingInterestType(final String newPrincipal, final String interestType) throws IOException {
        modifyLoanPrincipal(newPrincipal, Integer.valueOf(interestType));
    }

    /**
     * Modifies the loan application, always changing the principal so that the schedule has to be recalculated.
     * {@code interestType} is only sent when explicitly given; when it is {@code null} the parameter is left out of the
     * request entirely, together with the other override-enabled schedule fields (amortizationType,
     * interestCalculationPeriodType, repaymentEvery). Those fields are optional on modify, so omitting them must leave
     * the loan's existing values untouched rather than fail.
     */
    private void modifyLoanPrincipal(final String newPrincipal, final Integer interestType) throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse);
        final Long loanId = loanResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveOneLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        assertNotNull(loanDetails);

        final PutLoansLoanIdRequest modifyRequest = new PutLoansLoanIdRequest()//
                .productId(loanDetails.getLoanProductId())//
                .clientId(loanDetails.getClientId())//
                .principal(Long.valueOf(newPrincipal))//
                .loanTermFrequency(loanDetails.getTermFrequency())//
                .loanTermFrequencyType(loanDetails.getTermPeriodFrequencyType().getId().intValue())//
                .numberOfRepayments(loanDetails.getNumberOfRepayments())//
                .repaymentFrequencyType(loanDetails.getRepaymentFrequencyType().getId().intValue())//
                .interestRatePerPeriod(loanDetails.getInterestRatePerPeriod())//
                .expectedDisbursementDate(FORMATTER.format(loanDetails.getTimeline().getExpectedDisbursementDate()))//
                .submittedOnDate(FORMATTER.format(loanDetails.getTimeline().getSubmittedOnDate()))//
                .dateFormat(DATE_FORMAT)//
                .locale("en")//
                .loanType("individual");//

        if (interestType != null) {
            modifyRequest.interestType(interestType);
        }

        final PutLoansLoanIdResponse modifyResponse = ok(
                () -> fineractClient.loans().updateLoanApplication(loanId, modifyRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, modifyResponse);
    }

}
