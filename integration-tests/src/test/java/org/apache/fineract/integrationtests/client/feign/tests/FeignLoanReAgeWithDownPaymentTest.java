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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Test;

/**
 * Covers re-aging a progressive (advanced payment allocation) loan on a down-payment enabled, interest bearing product
 * with interest recalculation, after an early payment has been applied.
 *
 * <p>
 * The equivalent flow on a product without down payments is kept here as the control case.
 * </p>
 */
public class FeignLoanReAgeWithDownPaymentTest extends FeignLoanTestBase {

    private static final String DISBURSEMENT_DATE = "21 May 2026";
    private static final String PAYMENT_DATE = "10 June 2026";
    private static final String RE_AGE_START_DATE = "10 July 2026";
    private static final String SECOND_TRANCHE_DATE = "25 June 2026";
    private static final BigDecimal PRINCIPAL = new BigDecimal("600.00");
    private static final BigDecimal FIRST_TRANCHE = new BigDecimal("400.00");
    private static final BigDecimal SECOND_TRANCHE = new BigDecimal("200.00");
    private static final BigDecimal GOODWILL_CREDIT = new BigDecimal("2.80");
    private static final BigDecimal PARTIAL_REPAYMENT = new BigDecimal("100.00");
    private static final BigDecimal LARGER_REPAYMENT = new BigDecimal("300.00");
    private static final BigDecimal INTEREST_RATE = new BigDecimal("9.99");
    private static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal("25.00");
    private static final BigDecimal MAX_OUTSTANDING_BALANCE = new BigDecimal("1000000.00");
    private static final int NUMBER_OF_REPAYMENTS = 3;

    @Test
    void reAgeAfterGoodwillCreditOnDownPaymentLoan() {
        runAt("2026-05-21", () -> {
            Long loanId = createDisbursedDownPaymentLoan();

            updateBusinessDate(PAYMENT_DATE);
            makeGoodwillCredit(loanId, repayment(GOODWILL_CREDIT.doubleValue(), PAYMENT_DATE));

            updateBusinessDate(RE_AGE_START_DATE);
            reAge(loanId, reAge(RE_AGE_START_DATE, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, 10));

            assertReAgedScheduleIsConsistent(loanId, GOODWILL_CREDIT);
        });
    }

    @Test
    void reAgeAfterPartialRepaymentOnDownPaymentLoan() {
        runAt("2026-05-21", () -> {
            Long loanId = createDisbursedDownPaymentLoan();

            updateBusinessDate(PAYMENT_DATE);
            addRepaymentForLoan(loanId, PARTIAL_REPAYMENT.doubleValue(), PAYMENT_DATE);

            updateBusinessDate(RE_AGE_START_DATE);
            reAge(loanId, reAge(RE_AGE_START_DATE, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, 10));

            assertReAgedScheduleIsConsistent(loanId, PARTIAL_REPAYMENT);
        });
    }

    /**
     * Spreads several early payments across future installments before re-aging, so that more than one
     * transaction-to-installment mapping has to be carried over onto the re-aged schedule.
     */
    @Test
    void reAgeAfterMultipleEarlyPaymentsOnDownPaymentLoan() {
        runAt("2026-05-21", () -> {
            Long loanId = createDisbursedDownPaymentLoan();

            updateBusinessDate(PAYMENT_DATE);
            makeGoodwillCredit(loanId, repayment(GOODWILL_CREDIT.doubleValue(), PAYMENT_DATE));
            addRepaymentForLoan(loanId, LARGER_REPAYMENT.doubleValue(), PAYMENT_DATE);

            updateBusinessDate(RE_AGE_START_DATE);
            reAge(loanId, reAge(RE_AGE_START_DATE, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, 10));

            assertReAgedScheduleIsConsistent(loanId, GOODWILL_CREDIT.add(LARGER_REPAYMENT));
        });
    }

    /**
     * A second disbursement adds a second down payment installment. Down payment installments are skipped while the
     * re-aged schedule is matched against the interest schedule model, which shifts the alignment of the remaining
     * installments and leaves the early paid amounts without an installment to be carried over to.
     */
    @Test
    void reAgeAfterSecondDisbursementAndGoodwillCredit() {
        runAt("2026-05-21", () -> {
            Long clientId = createClient(DISBURSEMENT_DATE);
            Long productId = createLoanProduct(multiDisburseDownPaymentProduct());

            Long loanId = applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL.doubleValue(),
                    INTEREST_RATE.doubleValue(), NUMBER_OF_REPAYMENTS, null);
            disburseLoan(loanId, FIRST_TRANCHE, DISBURSEMENT_DATE);

            updateBusinessDate(PAYMENT_DATE);
            makeGoodwillCredit(loanId, repayment(GOODWILL_CREDIT.doubleValue(), PAYMENT_DATE));

            updateBusinessDate(SECOND_TRANCHE_DATE);
            disburseLoan(loanId, SECOND_TRANCHE, SECOND_TRANCHE_DATE);

            updateBusinessDate(RE_AGE_START_DATE);
            reAge(loanId, reAge(RE_AGE_START_DATE, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, 10));

            assertReAgedScheduleIsConsistent(loanId, GOODWILL_CREDIT);
        });
    }

    /**
     * Control case: identical flow on a product without down payments, which is reported to work.
     */
    @Test
    void reAgeAfterPartialRepaymentWithoutDownPayment() {
        runAt("2026-05-21", () -> {
            Long clientId = createClient(DISBURSEMENT_DATE);
            Long productId = createLoanProduct(progressiveInterestBearingProduct(false));

            Long loanId = applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL.doubleValue(),
                    INTEREST_RATE.doubleValue(), NUMBER_OF_REPAYMENTS, null);
            disburseLoan(loanId, PRINCIPAL, DISBURSEMENT_DATE);

            updateBusinessDate(PAYMENT_DATE);
            addRepaymentForLoan(loanId, PARTIAL_REPAYMENT.doubleValue(), PAYMENT_DATE);

            updateBusinessDate(RE_AGE_START_DATE);
            reAge(loanId, reAge(RE_AGE_START_DATE, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, 10));

            assertReAgedScheduleIsConsistent(loanId, PARTIAL_REPAYMENT);
        });
    }

    private Long createDisbursedDownPaymentLoan() {
        Long clientId = createClient(DISBURSEMENT_DATE);
        Long productId = createLoanProduct(progressiveInterestBearingProduct(true));

        Long loanId = applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL.doubleValue(),
                INTEREST_RATE.doubleValue(), NUMBER_OF_REPAYMENTS, null);
        disburseLoan(loanId, PRINCIPAL, DISBURSEMENT_DATE);

        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, GetLoansLoanIdStatus::getActive);
        return loanId;
    }

    /**
     * Mirrors the two live product configurations: a down-payment enabled product (25%, auto-repaid) and its equivalent
     * without down payments. Both are progressive / advanced payment allocation, interest bearing with daily interest
     * recalculation.
     *
     * <p>
     * The future installment allocation rules matter for this scenario: goodwill credits are allocated to the LAST
     * installment and repayments to the NEXT/LAST installment, which is what leaves paid amounts sitting on
     * installments that are still due when the loan is re-aged.
     * </p>
     */
    private PostLoanProductsRequest progressiveInterestBearingProduct(boolean withDownPayment) {
        return customizeProduct(create4IProgressive(), p -> {
            p.numberOfRepayments(NUMBER_OF_REPAYMENTS)//
                    .interestRatePerPeriod(INTEREST_RATE.doubleValue())//
                    .currencyCode(withDownPayment ? "USD" : "GBP")//
                    .allowPartialPeriodInterestCalculation(false)//
                    .enableInstallmentLevelDelinquency(true)//
                    .multiDisburseLoan(false)//
                    .disallowExpectedDisbursements(false)//
                    .maxTrancheCount(null)//
                    .outstandingLoanBalance(null)//
                    .enableDownPayment(withDownPayment)//
                    .paymentAllocation(withDownPayment ? downPaymentProductAllocations() : noDownPaymentProductAllocations());
            if (withDownPayment) {
                p.disbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE).enableAutoRepaymentForDownPayment(true);
            }
            return p;
        });
    }

    /**
     * Down payment enabled product that also allows multiple disbursements, so a loan can pick up more than one down
     * payment installment.
     */
    private PostLoanProductsRequest multiDisburseDownPaymentProduct() {
        return customizeProduct(progressiveInterestBearingProduct(true), p -> p//
                .multiDisburseLoan(true)//
                .maxTrancheCount(500)//
                .outstandingLoanBalance(MAX_OUTSTANDING_BALANCE.doubleValue())//
                .disallowExpectedDisbursements(true));
    }

    private List<AdvancedPaymentData> downPaymentProductAllocations() {
        return List.of(//
                LoanRequestBuilders.paymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                LoanRequestBuilders.paymentAllocation("REPAYMENT", "NEXT_LAST_INSTALLMENT"), //
                goodwillCreditAllocation());
    }

    private List<AdvancedPaymentData> noDownPaymentProductAllocations() {
        return List.of(//
                LoanRequestBuilders.paymentAllocation("DEFAULT", "LAST_INSTALLMENT"), //
                goodwillCreditAllocation());
    }

    /**
     * Goodwill credits settle principal before interest and push future amounts onto the last installment.
     */
    private AdvancedPaymentData goodwillCreditAllocation() {
        return LoanRequestBuilders.paymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT", "PAST_DUE_PRINCIPAL", "PAST_DUE_INTEREST",
                "PAST_DUE_PENALTY", "PAST_DUE_FEE", "DUE_PRINCIPAL", "DUE_INTEREST", "DUE_PENALTY", "DUE_FEE", "IN_ADVANCE_PRINCIPAL",
                "IN_ADVANCE_INTEREST", "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE");
    }

    private void assertReAgedScheduleIsConsistent(Long loanId, BigDecimal paidAmount) {
        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, GetLoansLoanIdStatus::getActive);

        assertNotNull(loan.getRepaymentSchedule());
        List<GetLoansLoanIdRepaymentPeriod> periods = loan.getRepaymentSchedule().getPeriods();
        assertNotNull(periods);

        BigDecimal totalPrincipalDue = periods.stream()//
                .filter(p -> p.getPeriod() != null)//
                .map(GetLoansLoanIdRepaymentPeriod::getPrincipalDue)//
                .filter(Objects::nonNull)//
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalPrincipalDue).as("Total principal due must still equal the disbursed amount").isEqualByComparingTo(PRINCIPAL);

        assertNotNull(loan.getSummary());
        assertThat(loan.getSummary().getTotalRepayment()).as("Paid amount attribution must survive re-aging")
                .isGreaterThanOrEqualTo(paidAmount);

        for (GetLoansLoanIdRepaymentPeriod period : periods) {
            if (period.getPrincipalLoanBalanceOutstanding() != null) {
                assertThat(period.getPrincipalLoanBalanceOutstanding())
                        .as("Balance should not be negative for period %s", period.getPeriod()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            }
        }
    }
}
