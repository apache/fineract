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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.StringEnumOptionData;
import org.apache.fineract.test.factory.LoanChargeRequestFactory;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanDeferredChargeTransactionCreatedBusinessEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

/**
 * Steps for amortization configuration frozen onto a loan charge and deferred charge creation.
 */
@RequiredArgsConstructor
public class LoanChargeAmortizationStepDef extends AbstractStepDef {

    private static final String PRODUCT_ID = "chargeAmortizationLoanProductId";
    private static final String LOAN_CHARGE_ID = "chargeAmortizationLoanChargeId";
    private static final String PERIOD_TYPE = "amortizationPeriodType";
    private static final String DAYS = "amortizationDays";
    private static final String CALCULATION_TYPE = "amortizationCalculationType";
    private static final String INCOME_TYPE = "amortizationIncomeType";

    private final FineractFeignClient fineractClient;
    private final LoanRequestFactory loanRequestFactory;
    private final EventAssertion eventAssertion;
    private final LoanStepDef loanStepDef;

    @When("Admin creates a new Loan with charge amortization product and adds the amortized charge")
    public void createLoanWithAmortizedCharge() {
        createLoan(LoanRequestFactory.DATE_SUBMIT_STRING, LoanRequestFactory.DEFAULT_PRINCIPAL, false, null, null);
        addCharge(LoanRequestFactory.DATE_SUBMIT_STRING);
    }

    @When("Admin creates a new Loan with charge amortization product on {string}")
    public void createLoanWithChargeAmortizationProductOn(final String submittedOnDate) {
        createLoan(submittedOnDate, LoanRequestFactory.DEFAULT_PRINCIPAL, false, null, null);
    }

    @When("Admin creates a new Loan with charge amortization product on {string} with principal {string}")
    public void createLoanOnDate(final String submittedOnDate, final String principal) {
        createLoan(submittedOnDate, new BigDecimal(principal), false, null, null);
    }

    @When("Admin creates a new Loan with charge amortization product and the amortized charge on {string} with principal {string}")
    public void createLoanWithChargeOnDate(final String submittedOnDate, final String principal) {
        createLoan(submittedOnDate, new BigDecimal(principal), true, null, null);
    }

    @When("Admin creates a new Loan with charge amortization product and the amortized charge in the loan application")
    public void createLoanWithAmortizedChargeInApplication() {
        createLoan(LoanRequestFactory.DATE_SUBMIT_STRING, LoanRequestFactory.DEFAULT_PRINCIPAL, true, null,
                LoanRequestFactory.DATE_SUBMIT_STRING);
    }

    @Then("Creating a new Loan with charge amortization product and the amortized charge in the loan application results an error:")
    public void createLoanWithAmortizedChargeInApplicationFails(final DataTable table) {
        final Map<String, String> data = table.asMaps().getFirst();
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final PostLoansRequestChargeData charge = new PostLoansRequestChargeData().chargeId(createdChargeId()).amount(BigDecimal.TEN)
                .dueDate(LoanRequestFactory.DATE_SUBMIT_STRING);
        final PostLoansRequest loansRequest = loanRequestFactory.defaultProgressiveLoansRequest(clientResponse.getClientId())
                .productId(createdProductId()).charges(List.of(charge));
        final CallFailedRuntimeException failure = fail(
                () -> fineractClient.loans().calculateOrSubmitLoanApplication(loansRequest, Map.of()));
        assertThat(failure.getStatus()).as("HTTP status code, response: " + failure.getMessage())
                .isEqualTo(Integer.parseInt(data.get("httpCode")));
        assertThat(failure.getMessage()).as("Error message should contain: " + data.get("errorMessage").trim())
                .contains(data.get("errorMessage").trim());
    }

    @When("Admin creates a new Loan with charge amortization product and the amortized specified due date charge due on {string} submitted on {string} with principal {string}")
    public void createLoanWithSpecifiedDueDateCharge(final String dueDate, final String submittedOnDate, final String principal) {
        createLoan(submittedOnDate, new BigDecimal(principal), true, null, dueDate);
    }

    @When("Admin creates a new Loan with charge amortization product, the amortized charge and two expected tranches on {string} and {string} with principal {string}")
    public void createLoanWithChargeAndTwoTranches(final String firstTrancheDate, final String secondTrancheDate, final String principal) {
        final BigDecimal total = new BigDecimal(principal);
        final BigDecimal firstTranche = total.divide(BigDecimal.valueOf(2));
        final BigDecimal secondTranche = total.subtract(firstTranche);
        final List<PostLoansDisbursementData> tranches = List.of(
                new PostLoansDisbursementData().expectedDisbursementDate(firstTrancheDate).principal(firstTranche),
                new PostLoansDisbursementData().expectedDisbursementDate(secondTrancheDate).principal(secondTranche));
        createLoan(firstTrancheDate, total, true, tranches, null);
    }

    @When("Admin adds the amortized charge to the loan on {string}")
    public void addAmortizedChargeToLoanOn(final String dueDate) {
        addCharge(dueDate);
    }

    @When("Admin adds the amortized charge to the loan with due date {string}")
    public void addAmortizedChargeWithDueDate(final String dueDate) {
        addCharge(dueDate);
    }

    @When("Admin waives the added amortized charge")
    public void waiveAddedAmortizedCharge() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanChargeId = testContext().get(LOAN_CHARGE_ID);
        assertThat(loanChargeId).as("a charge must be added to the loan first in this scenario").isNotNull();
        ok(() -> fineractClient.loanCharges().executeLoanChargeOnExistingCharge(loanResponse.getLoanId(), loanChargeId,
                new PostLoansLoanIdChargesChargeIdRequest(), Map.<String, Object>of("command", "waive")));
    }

    @Then("Adding the amortized charge to a new Loan with the charge amortization product results an error:")
    public void addAmortizedChargeFails(final DataTable table) {
        createLoan(LoanRequestFactory.DATE_SUBMIT_STRING, LoanRequestFactory.DEFAULT_PRINCIPAL, false, null, null);
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

    @Then("Loan Charges tab has the amortized charge with the following data:")
    public void verifyAmortizedChargeInChargesTab(final DataTable table) {
        final String chargeName = testContext().get(ChargeAmortizationStepDef.CHARGE_NAME);
        assertThat(chargeName).as("a charge must be created first in this scenario").isNotNull();
        final List<List<String>> rows = new ArrayList<>();
        rows.add(table.row(0));
        for (int i = 1; i < table.height(); i++) {
            final List<String> row = new ArrayList<>(table.row(i));
            row.set(0, chargeName);
            rows.add(row);
        }
        // Keep the original table's converter — DataTable.create(rows) alone has none and asLists() fails.
        loanStepDef.loanChargesTabCheck(DataTable.create(rows, table.getTableConverter()));
    }

    @Then("LoanDeferredChargeTransactionCreatedBusinessEvent is created on {string}")
    public void verifyDeferredChargeBusinessEvent(final String date) {
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanCreateResponse.getLoanId();
        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final GetLoansLoanIdTransactions deferredChargeTransaction = loanDetailsResponse.getTransactions().stream()
                .filter(t -> date.equals(LoanRequestFactory.FORMATTER.format(t.getDate()))
                        && "Deferred Charge".equals(t.getType().getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Deferred Charge transaction found on %s", date)));
        eventAssertion.assertEventRaised(LoanDeferredChargeTransactionCreatedBusinessEvent.class, deferredChargeTransaction.getId());
    }

    private void createLoan(final String submittedOnDate, final BigDecimal principal, final boolean includeAmortizedCharge,
            final List<PostLoansDisbursementData> disbursementData, final String specifiedDueDate) {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final boolean cumulative = Boolean.TRUE.equals(testContext().get(LoanProductChargeAmortizationStepDef.CUMULATIVE_PRODUCT));
        final PostLoansRequest loansRequest = (cumulative ? loanRequestFactory.defaultLoansRequest(clientResponse.getClientId())
                : loanRequestFactory.defaultProgressiveLoansRequest(clientResponse.getClientId())).productId(createdProductId())
                .submittedOnDate(submittedOnDate).expectedDisbursementDate(submittedOnDate).principal(principal);
        if (includeAmortizedCharge) {
            final PostLoansRequestChargeData charge = new PostLoansRequestChargeData().chargeId(createdChargeId()).amount(BigDecimal.TEN);
            if (specifiedDueDate != null) {
                charge.dueDate(specifiedDueDate);
            }
            loansRequest.charges(List.of(charge));
        }
        if (disbursementData != null) {
            loansRequest.disbursementData(disbursementData);
        }
        final PostLoansResponse response = ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
    }

    private void addCharge(final String dueDate) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final PostLoansLoanIdChargesResponse response = ok(
                () -> fineractClient.loanCharges().createOrPayLoanCharge(loanResponse.getLoanId(), chargeRequest(dueDate), Map.of()));
        testContext().set(LOAN_CHARGE_ID, Long.valueOf(response.getResourceId()));
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
