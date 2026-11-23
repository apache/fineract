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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.StringEnumOptionData;
import org.apache.fineract.test.factory.LoanChargeRequestFactory;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

/**
 * Steps for amortization configuration frozen onto a loan charge (story B1).
 */
@RequiredArgsConstructor
public class LoanChargeAmortizationStepDef extends AbstractStepDef {

    private static final String PRODUCT_ID = "chargeAmortizationLoanProductId";
    private static final String PERIOD_TYPE = "amortizationPeriodType";
    private static final String DAYS = "amortizationDays";
    private static final String CALCULATION_TYPE = "amortizationCalculationType";
    private static final String INCOME_TYPE = "amortizationIncomeType";

    private final FineractFeignClient fineractClient;
    private final LoanRequestFactory loanRequestFactory;

    @When("Admin creates a new Loan with charge amortization product and adds the amortized charge")
    public void createLoanWithAmortizedCharge() {
        createLoan();
        addCharge(LoanRequestFactory.DATE_SUBMIT_STRING);
    }

    @When("Admin creates a new Loan with charge amortization product on {string}")
    public void createLoanWithChargeAmortizationProductOn(final String submittedOnDate) {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final PostLoansRequest loansRequest = loanRequestFactory.defaultProgressiveLoansRequest(clientResponse.getClientId())
                .productId(createdProductId()).submittedOnDate(submittedOnDate).expectedDisbursementDate(submittedOnDate);
        final PostLoansResponse response = ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
    }

    @When("Admin creates a new Loan with charge amortization product and the amortized charge in the loan application")
    public void createLoanWithAmortizedChargeInApplication() {
        createLoanWithChargeInApplication();
    }

    @Then("Creating a new Loan with charge amortization product and the amortized charge in the loan application results an error:")
    public void createLoanWithAmortizedChargeInApplicationFails(final DataTable table) {
        final Map<String, String> data = table.asMaps().getFirst();
        final CallFailedRuntimeException failure = fail(
                () -> fineractClient.loans().calculateOrSubmitLoanApplication(loanRequestWithChargeInApplication(), Map.of()));
        assertThat(failure.getStatus()).as("HTTP status code, response: " + failure.getMessage())
                .isEqualTo(Integer.parseInt(data.get("httpCode")));
        assertThat(failure.getMessage()).as("Error message should contain: " + data.get("errorMessage").trim())
                .contains(data.get("errorMessage").trim());
    }

    @When("Admin adds the amortized charge to the loan on {string}")
    public void addAmortizedChargeToLoanOn(final String dueDate) {
        addCharge(dueDate);
    }

    @Then("Adding the amortized charge to a new Loan with the charge amortization product results an error:")
    public void addAmortizedChargeFails(final DataTable table) {
        createLoan();
        final Map<String, String> data = table.asMaps().getFirst();
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final PostLoansLoanIdChargesRequest request = chargeRequest(LoanRequestFactory.DATE_SUBMIT_STRING);
        final CallFailedRuntimeException failure = fail(
                () -> fineractClient.loanCharges().createOrPayLoanCharge(loanResponse.getLoanId(), request, Map.of()));
        assertThat(failure.getStatus()).as("HTTP status code, response: " + failure.getMessage())
                .isEqualTo(Integer.parseInt(data.get("httpCode")));
        assertThat(failure.getMessage()).as("Error message should contain: " + data.get("errorMessage").trim())
                .contains(data.get("errorMessage").trim());
    }

    @Then("The loan charge has the following amortization configuration:")
    public void verifyLoanChargeAmortization(final DataTable table) {
        final Map<String, String> expected = table.asMaps().getFirst();
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final GetLoansLoanIdResponse loan = ok(
                () -> fineractClient.loans().retrieveOneLoan(loanResponse.getLoanId(), Map.of("associations", "charges")));
        assertThat(loan.getCharges()).as("loan charges").isNotEmpty();
        final GetLoansLoanIdLoanChargeData loanCharge = loan.getCharges().stream()
                .filter(charge -> createdChargeId().equals(charge.getChargeId())).findFirst()
                .orElseThrow(() -> new AssertionError("loan charge for charge id " + createdChargeId() + " not found"));

        assertThat(idOf(loanCharge.getAmortizationPeriodType())).as(PERIOD_TYPE).isEqualTo(cell(expected, PERIOD_TYPE));
        assertThat(loanCharge.getAmortizationDays() == null ? null : String.valueOf(loanCharge.getAmortizationDays())).as(DAYS)
                .isEqualTo(cell(expected, DAYS));
        assertThat(idOf(loanCharge.getAmortizationCalculationType())).as(CALCULATION_TYPE).isEqualTo(cell(expected, CALCULATION_TYPE));
        assertThat(idOf(loanCharge.getAmortizationIncomeType())).as(INCOME_TYPE).isEqualTo(cell(expected, INCOME_TYPE));
    }

    private void createLoan() {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final PostLoansRequest loansRequest = loanRequestFactory.defaultProgressiveLoansRequest(clientResponse.getClientId())
                .productId(createdProductId());
        final PostLoansResponse response = ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
    }

    private void createLoanWithChargeInApplication() {
        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateOrSubmitLoanApplication(loanRequestWithChargeInApplication(), Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
    }

    private PostLoansRequest loanRequestWithChargeInApplication() {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final PostLoansRequestChargeData charge = new PostLoansRequestChargeData().chargeId(createdChargeId()).amount(BigDecimal.TEN)
                .dueDate(LoanRequestFactory.DATE_SUBMIT_STRING);
        return loanRequestFactory.defaultProgressiveLoansRequest(clientResponse.getClientId()).productId(createdProductId())
                .charges(List.of(charge));
    }

    private void addCharge(final String dueDate) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        ok(() -> fineractClient.loanCharges().createOrPayLoanCharge(loanResponse.getLoanId(), chargeRequest(dueDate), Map.of()));
    }

    private PostLoansLoanIdChargesRequest chargeRequest(final String dueDate) {
        return LoanChargeRequestFactory.defaultLoanChargeRequest().chargeId(createdChargeId()).dueDate(dueDate).amount(10.0);
    }

    private Long createdProductId() {
        final Long productId = testContext().get(PRODUCT_ID);
        assertThat(productId).as("a loan product must be created first in this scenario").isNotNull();
        return productId;
    }

    private Long createdChargeId() {
        final Long chargeId = testContext().get(ChargeAmortizationStepDef.CHARGE_ID);
        assertThat(chargeId).as("a charge must be created first in this scenario").isNotNull();
        return chargeId;
    }

    private static String idOf(final StringEnumOptionData option) {
        return option == null ? null : option.getId();
    }

    private static String cell(final Map<String, String> data, final String column) {
        final String value = data.get(column);
        return StringUtils.isBlank(value) ? null : value.trim();
    }
}
