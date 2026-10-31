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

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.test.data.DaysInYearType;
import org.apache.fineract.test.data.InterestRateFrequencyType;
import org.apache.fineract.test.data.RepaymentFrequencyType;
import org.apache.fineract.test.factory.LoanProductsRequestFactory;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

/**
 * Steps for the semi-monthly repayment frequency (two configured due days per calendar month). The product is created
 * on the fly from the LP1 cumulative template, so no global initializer entry is needed.
 */
@RequiredArgsConstructor
public class LoanSemiMonthlyRepaymentStepDef extends AbstractStepDef {

    private static final int SEMI_MONTHLY = RepaymentFrequencyType.SEMI_MONTHLY.value;
    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String LOCALE = "en";
    private static final int INTEREST_TYPE_DECLINING_BALANCE = 0;
    private static final int INTEREST_CALCULATION_PERIOD_DAILY = 0;
    private static final int AMORTIZATION_EQUAL_INSTALLMENTS = 1;

    private final FineractFeignClient fineractClient;
    private final LoanProductsRequestFactory loanProductsRequestFactory;
    private final LoanRequestFactory loanRequestFactory;

    @When("Admin creates a new cumulative semi-monthly Loan Product with due days {int} and {int}, {double}% yearly interest calculated daily and up to {int} repayments")
    public void createSemiMonthlyLoanProduct(final int firstRepaymentDayOfMonth, final int secondRepaymentDayOfMonth,
            final double interestRate, final int maxNumberOfRepayments) {
        final PostLoanProductsRequest request = loanProductsRequestFactory.defaultLoanProductsRequestLP1()//
                .repaymentEvery(1)//
                .repaymentFrequencyType((long) SEMI_MONTHLY)//
                .firstRepaymentDayOfMonth(firstRepaymentDayOfMonth)//
                .secondRepaymentDayOfMonth(secondRepaymentDayOfMonth)//
                .numberOfRepayments(maxNumberOfRepayments)//
                .maxNumberOfRepayments(maxNumberOfRepayments)//
                .minInterestRatePerPeriod(0.0)//
                .interestRatePerPeriod(interestRate)//
                .maxInterestRatePerPeriod(interestRate)//
                .interestRateFrequencyType(InterestRateFrequencyType.YEAR.value)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_DAILY)//
                // LP1 enables partial period interest, which daily interest calculation rejects.
                .allowPartialPeriodInterestCalculation(false)//
                .daysInYearType(DaysInYearType.DAYS365.value)//
                // Keep the EMI in cents, so the installments show the exact annuity.
                .installmentAmountInMultiplesOf(null);
        final PostLoanProductsResponse response = ok(() -> fineractClient.loanProducts().createLoanProduct(request));
        testContext().set(TestContextKey.LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.LOAN_PRODUCT_CREATE_REQUEST, request);
    }

    @When("Admin creates a semi-monthly Loan of the created product submitted on {string} with principal {string}, {double}% interest and {int} repayments")
    public void createSemiMonthlyLoan(final String submittedOnDate, final String principal, final double interestRate,
            final int numberOfRepayments) {
        final PostClientsResponse client = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final PostLoanProductsResponse product = testContext().get(TestContextKey.LOAN_PRODUCT_CREATE_RESPONSE);
        final PostLoansRequest request = loanRequestFactory.defaultLoansRequest(client.getClientId())//
                .productId(product.getResourceId())//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(submittedOnDate)//
                .principal(new BigDecimal(principal))//
                .loanTermFrequency(numberOfRepayments)//
                .loanTermFrequencyType(SEMI_MONTHLY)//
                .numberOfRepayments(numberOfRepayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(SEMI_MONTHLY)//
                .interestRatePerPeriod(BigDecimal.valueOf(interestRate))//
                .interestType(INTEREST_TYPE_DECLINING_BALANCE)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_DAILY)//
                .amortizationType(AMORTIZATION_EQUAL_INSTALLMENTS);
        final PostLoansResponse response = ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
    }

    @Then("Every installment of the semi-monthly Loan except the last one has a total due of {string}, and the last one differs from it by at most {string}")
    public void verifyEqualInstallments(final String expectedInstallment, final String maxLastInstallmentDifference) {
        final List<GetLoansLoanIdRepaymentPeriod> installments = installments();
        final BigDecimal expected = new BigDecimal(expectedInstallment);

        installments.subList(0, installments.size() - 1).forEach(installment -> assertThat(installment.getTotalDueForPeriod())
                .as("Total due of installment %d", installment.getPeriod()).isEqualByComparingTo(expected));
        final GetLoansLoanIdRepaymentPeriod last = installments.get(installments.size() - 1);
        assertThat(last.getTotalDueForPeriod().subtract(expected).abs())
                .as("Difference between the last installment (%s) and the others (%s)", last.getTotalDueForPeriod(), expected)
                .isLessThanOrEqualTo(new BigDecimal(maxLastInstallmentDifference));
    }

    @Then("The semi-monthly Loan has {int} installments, the first due on {string} and the last due on {string}")
    public void verifyInstallmentDates(final int numberOfInstallments, final String firstDueDate, final String lastDueDate) {
        final List<GetLoansLoanIdRepaymentPeriod> installments = installments();
        assertThat(installments).hasSize(numberOfInstallments);
        assertThat(installments.get(0).getDueDate()).isEqualTo(LocalDate.parse(firstDueDate, LoanRequestFactory.FORMATTER));
        assertThat(installments.get(installments.size() - 1).getDueDate())
                .isEqualTo(LocalDate.parse(lastDueDate, LoanRequestFactory.FORMATTER));
    }

    @Then("The created Loan Product template has semi-monthly due days {int} and {int}")
    public void verifyTemplateDueDays(final int firstRepaymentDayOfMonth, final int secondRepaymentDayOfMonth) {
        final GetLoanProductsProductIdResponse template = ok(
                () -> fineractClient.loanProducts().retrieveOneLoanProductUniversal(createdProductId(), Map.of("template", "true")));
        assertThat(template.getFirstRepaymentDayOfMonth()).as("First due day in the product template").isEqualTo(firstRepaymentDayOfMonth);
        assertThat(template.getSecondRepaymentDayOfMonth()).as("Second due day in the product template")
                .isEqualTo(secondRepaymentDayOfMonth);
    }

    @When("Admin changes the repayment frequency of the created Loan Product to {string}")
    public void changeRepaymentFrequency(final String repaymentFrequencyType) {
        final PutLoanProductsProductIdRequest request = new PutLoanProductsProductIdRequest()
                .repaymentFrequencyType(RepaymentFrequencyType.valueOf(repaymentFrequencyType).value).locale(LOCALE);
        ok(() -> fineractClient.loanProducts().updateLoanProduct(createdProductId(), request));
    }

    @Then("The created Loan Product has repayment frequency {string} and no semi-monthly due days")
    public void verifyNoDueDays(final String repaymentFrequencyType) {
        final GetLoanProductsProductIdResponse product = ok(
                () -> fineractClient.loanProducts().retrieveOneLoanProductUniversal(createdProductId(), Map.of()));
        assertThat(product.getRepaymentFrequencyType().getId())
                .isEqualTo(RepaymentFrequencyType.valueOf(repaymentFrequencyType).value.longValue());
        assertThat(product.getFirstRepaymentDayOfMonth()).as("First due day").isNull();
        assertThat(product.getSecondRepaymentDayOfMonth()).as("Second due day").isNull();
    }

    @Then("Admin fails to re-age the Loan with frequency type {string} starting on {string}, with status code {int} and error {string}")
    public void reAgeFails(final String frequencyType, final String startDate, final int statusCode, final String expectedError) {
        final long loanId = createdLoanId();
        final PostLoansLoanIdTransactionsRequest request = loanRequestFactory.defaultReAgingRequest().frequencyType(frequencyType)
                .frequencyNumber(1).startDate(startDate).numberOfInstallments(4);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "reAge")));

        assertThat(exception.getStatus()).isEqualTo(statusCode);
        assertThat(exception.getResponseBody()).contains(expectedError);
    }

    @Then("Admin fails to preview the re-aging of the Loan with frequency type {string} starting on {string}, with status code {int} and error {string}")
    public void reAgePreviewFails(final String frequencyType, final String startDate, final int statusCode, final String expectedError) {
        final long loanId = createdLoanId();
        final Map<String, Object> queryParams = new HashMap<>(Map.of("dateFormat", DATE_FORMAT, "locale", LOCALE, "frequencyType",
                frequencyType, "frequencyNumber", "1", "startDate", startDate, "numberOfInstallments", "4"));

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().previewReAgeLoanSchedule(loanId, queryParams));

        assertThat(exception.getStatus()).isEqualTo(statusCode);
        assertThat(exception.getResponseBody()).contains(expectedError);
    }

    private List<GetLoansLoanIdRepaymentPeriod> installments() {
        final long loanId = createdLoanId();
        final GetLoansLoanIdResponse loan = ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "repaymentSchedule")));
        return loan.getRepaymentSchedule().getPeriods().stream().filter(period -> period.getPeriod() != null).toList();
    }

    private Long createdProductId() {
        final PostLoanProductsResponse product = testContext().get(TestContextKey.LOAN_PRODUCT_CREATE_RESPONSE);
        return product.getResourceId();
    }

    private long createdLoanId() {
        final PostLoansResponse loan = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loan.getLoanId();
    }
}
