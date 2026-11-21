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

import static org.assertj.core.data.Offset.offset;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Test;

/**
 * Covers re-aging an interest bearing progressive loan whose last installment was paid off early (repayments are
 * allocated to the LAST installment) and that carries a specified due date charge, or a chargeback or credit balance
 * refund, which is still outstanding when the loan is re-aged.
 *
 * <p>
 * A chargeback or credit balance refund posted after maturity is booked only on an additional (N+1) installment and not
 * in the interest schedule model, its principal and interest must still be taken over by the re-age.
 * </p>
 *
 * <p>
 * The re-aged schedule is expected to consist of exactly the requested number of installments, each carrying an equal
 * share of the outstanding principal.
 * </p>
 */
@Slf4j
public class FeignLoanReAgeAfterEarlyLastInstallmentPaymentWithChargeTest extends FeignLoanTestBase {

    private static final String DISBURSEMENT_DATE = "14 April 2026";
    private static final String REPAYMENT_DATE = "20 April 2026";
    private static final String RE_AGE_DATE = "22 October 2026";
    private static final String RE_AGE_START_DATE = "22 November 2026";
    private static final LocalDate RE_AGE_LOCAL_DATE = LocalDate.of(2026, 10, 22);
    private static final double PRINCIPAL = 600.0;
    private static final double INTEREST_RATE = 9.99;
    private static final double CHARGE_AMOUNT = 10.0;
    private static final double CHARGEBACK_AMOUNT = 50.0;
    private static final int NUMBER_OF_REPAYMENTS = 6;
    private static final int RE_AGE_INSTALLMENTS = 3;
    private static final String PAYABLE_INTEREST = "EQUAL_AMORTIZATION_PAYABLE_INTEREST";
    private static final String FULL_INTEREST = "EQUAL_AMORTIZATION_FULL_INTEREST";
    private static final String CHARGEBACK_TYPE_CODE = "loanTransactionType.chargeback";
    private static final String CREDIT_BALANCE_REFUND_TYPE_CODE = "loanTransactionType.creditBalanceRefund";
    private static final String RE_AGE_TYPE_CODE = "loanTransactionType.reAge";

    /**
     * The charge is due after maturity, so it lives on an additional (N+1) installment when the loan is re-aged.
     */
    @Test
    void reAgeWithChargeDueAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            addCharge(loanId, false, CHARGE_AMOUNT, "20 October 2026");

            reAgeLoan(loanId);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * The charge is due on the last unpaid installment, before maturity.
     */
    @Test
    void reAgeWithChargeDueBeforeMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 100.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            addCharge(loanId, false, CHARGE_AMOUNT, "01 September 2026");

            reAgeLoan(loanId);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithChargeDueBeforeMaturity()}, except that the charge is waived and a new charge is added
     * for the same due date before re-aging.
     */
    @Test
    void reAgeWithWaivedAndRecreatedChargeDueBeforeMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 100.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            Long loanChargeId = addCharge(loanId, false, CHARGE_AMOUNT, "01 September 2026");
            waiveLoanCharge(loanId, loanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));
            addCharge(loanId, false, CHARGE_AMOUNT, "01 September 2026");

            reAgeLoan(loanId);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Control case: the same flow without any charge.
     */
    @Test
    void reAgeWithoutCharge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoan(loanId);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Part of the early repayment is charged back before maturity, so the charged back principal sits on an installment
     * that is past due when the loan is re-aged.
     */
    @Test
    void reAgeWithChargebackBeforeMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            Long repaymentId = addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

            updateBusinessDate("01 September 2026");
            applyChargebackTransaction(loanId, repaymentId, CHARGEBACK_AMOUNT, getPaymentTypeId(0));

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoan(loanId);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithChargeDueAfterMaturity()}, with the full interest equal amortization handling.
     */
    @Test
    void reAgeWithFullInterestAndChargeDueAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            addCharge(loanId, false, CHARGE_AMOUNT, "20 October 2026");

            reAgeLoan(loanId, FULL_INTEREST);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithChargeDueBeforeMaturity()}, with the full interest equal amortization handling.
     */
    @Test
    void reAgeWithFullInterestAndChargeDueBeforeMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 100.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            addCharge(loanId, false, CHARGE_AMOUNT, "01 September 2026");

            reAgeLoan(loanId, FULL_INTEREST);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Default interest handling, with the last two installments paid off early.
     */
    @Test
    void reAgeWithDefaultInterestHandlingAfterTwoInstallmentsPaidEarly() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoanWithDefaultInterestHandling(loanId);

            assertReAgedSchedule(loanId, false);
        });
    }

    /**
     * Default interest handling, with the last installment paid off early.
     */
    @Test
    void reAgeWithDefaultInterestHandlingAfterLastInstallmentPaidEarly() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 100.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoanWithDefaultInterestHandling(loanId);

            assertReAgedSchedule(loanId, false);
        });
    }

    /**
     * Part of the early repayment is charged back on the re-age date, after maturity, so the charged back principal
     * sits only on the additional (N+1) installment.
     */
    @Test
    void reAgeWithChargebackAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithChargebackAfterMaturity(RE_AGE_DATE);

            reAgeLoanAndAssertPrincipalTakenOver(loanId, () -> reAgeLoan(loanId));

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithChargebackAfterMaturity()}, except that the chargeback is posted after maturity but
     * before the re-age date, so the additional installment is already past due when the loan is re-aged.
     */
    @Test
    void reAgeWithChargebackAfterMaturityBeforeReAgeDate() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithChargebackAfterMaturity("20 October 2026");

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoanAndAssertPrincipalTakenOver(loanId, () -> reAgeLoan(loanId));

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithChargebackAfterMaturity()}, with the full interest equal amortization handling.
     */
    @Test
    void reAgeWithFullInterestAndChargebackAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithChargebackAfterMaturity(RE_AGE_DATE);

            reAgeLoanAndAssertPrincipalTakenOver(loanId, () -> reAgeLoan(loanId, FULL_INTEREST));

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithChargebackAfterMaturity()}, with the default interest handling.
     */
    @Test
    void reAgeWithDefaultInterestHandlingAndChargebackAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithChargebackAfterMaturity(RE_AGE_DATE);

            reAgeLoanAndAssertPrincipalTakenOver(loanId, () -> reAgeLoanWithDefaultInterestHandling(loanId));

            assertReAgedSchedule(loanId, false);
        });
    }

    /**
     * Same as {@link #reAgeWithChargebackAfterMaturityBeforeReAgeDate()}, with the default interest handling.
     */
    @Test
    void reAgeWithDefaultInterestHandlingAndChargebackAfterMaturityBeforeReAgeDate() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithChargebackAfterMaturity("20 October 2026");

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoanAndAssertPrincipalTakenOver(loanId, () -> reAgeLoanWithDefaultInterestHandling(loanId));

            assertReAgedSchedule(loanId, false);
        });
    }

    /**
     * The loan is overpaid after maturity and the overpayment is refunded. A backdated charge then uses up part of the
     * overpayment, so part of the credit balance refund becomes principal on the additional (N+1) installment.
     */
    @Test
    void reAgeWithCreditBalanceRefundPrincipalAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithCreditBalanceRefundPrincipalAfterMaturity();

            reAgeLoanAndAssertPrincipalTakenOver(loanId, () -> reAgeLoan(loanId));

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithCreditBalanceRefundPrincipalAfterMaturity()}, with the default interest handling.
     */
    @Test
    void reAgeWithDefaultInterestHandlingAndCreditBalanceRefundPrincipalAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithCreditBalanceRefundPrincipalAfterMaturity();

            reAgeLoanAndAssertPrincipalTakenOver(loanId, () -> reAgeLoanWithDefaultInterestHandling(loanId));

            assertReAgedSchedule(loanId, false);
        });
    }

    /**
     * The chargeback is posted after maturity and also charges back the interest of the repayment, as the chargeback
     * allocation of the product puts interest first. The additional (N+1) installment holds the interest too, the
     * re-age must take it over.
     */
    @Test
    void reAgeWithInterestChargebackAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithInterestChargebackAfterMaturity(RE_AGE_DATE);

            reAgeLoanAndAssertInterestTakenOver(loanId, () -> reAgeLoan(loanId));

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithInterestChargebackAfterMaturity()}, except that the chargeback is posted before the
     * re-age date.
     */
    @Test
    void reAgeWithInterestChargebackAfterMaturityBeforeReAgeDate() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithInterestChargebackAfterMaturity("20 October 2026");

            reAgeLoanAndAssertInterestTakenOver(loanId, () -> reAgeLoan(loanId));

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithInterestChargebackAfterMaturity()}, with the default interest handling.
     */
    @Test
    void reAgeWithDefaultInterestHandlingAndInterestChargebackAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithInterestChargebackAfterMaturity(RE_AGE_DATE);

            reAgeLoanAndAssertInterestTakenOver(loanId, () -> reAgeLoanWithDefaultInterestHandling(loanId));

            assertReAgedSchedule(loanId, false);
        });
    }

    /**
     * Same as {@link #reAgeWithInterestChargebackAfterMaturityBeforeReAgeDate()}, with the default interest handling.
     */
    @Test
    void reAgeWithDefaultInterestHandlingAndInterestChargebackAfterMaturityBeforeReAgeDate() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoanWithInterestChargebackAfterMaturity("20 October 2026");

            reAgeLoanAndAssertInterestTakenOver(loanId, () -> reAgeLoanWithDefaultInterestHandling(loanId));

            assertReAgedSchedule(loanId, false);
        });
    }

    private Long createDisbursedLoanWithChargebackAfterMaturity(String chargebackDate) {
        Long loanId = createDisbursedLoan();

        updateBusinessDate(REPAYMENT_DATE);
        Long repaymentId = addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

        updateBusinessDate(chargebackDate);
        applyChargebackTransaction(loanId, repaymentId, CHARGEBACK_AMOUNT, getPaymentTypeId(0));
        return loanId;
    }

    /**
     * The first installment is repaid on its due date, so the repayment has an interest portion, and is charged back
     * after maturity by a product whose chargeback allocation puts interest first.
     */
    private Long createDisbursedLoanWithInterestChargebackAfterMaturity(String chargebackDate) {
        Long clientId = createClient(DISBURSEMENT_DATE);
        Long productId = createLoanProduct(customizeProduct(payMonthlyInterestBearingProduct(), p -> p//
                .creditAllocation(List.of(LoanRequestBuilders.creditAllocation("CHARGEBACK", "INTEREST", "PRINCIPAL", "PENALTY", "FEE")))));
        Long loanId = applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, INTEREST_RATE, NUMBER_OF_REPAYMENTS,
                null);
        disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), DISBURSEMENT_DATE);

        String firstDueDate = "14 May 2026";
        updateBusinessDate(firstDueDate);
        GetLoansLoanIdRepaymentPeriod firstInstallment = Objects.requireNonNull(getLoanDetails(loanId).getRepaymentSchedule().getPeriods())
                .stream().filter(p -> Integer.valueOf(1).equals(p.getPeriod())).findFirst().orElseThrow();
        Long repaymentId = addRepaymentForLoan(loanId, Objects.requireNonNull(firstInstallment.getTotalDueForPeriod()).doubleValue(),
                firstDueDate);

        updateBusinessDate(chargebackDate);
        applyChargebackTransaction(loanId, repaymentId, CHARGEBACK_AMOUNT, getPaymentTypeId(0));

        updateBusinessDate(RE_AGE_DATE);
        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, GetLoansLoanIdStatus::getActive);
        assertThat(sumInterestPortions(loan, CHARGEBACK_TYPE_CODE)).as("Interest portion of the chargeback")
                .isEqualByComparingTo(Objects.requireNonNull(firstInstallment.getInterestDue()));
        return loanId;
    }

    private Long createDisbursedLoanWithCreditBalanceRefundPrincipalAfterMaturity() {
        Long loanId = createDisbursedLoan();

        updateBusinessDate(REPAYMENT_DATE);
        addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

        String payOffDate = "20 October 2026";
        updateBusinessDate(payOffDate);
        double totalOutstanding = Objects.requireNonNull(getLoanDetails(loanId).getSummary().getTotalOutstanding()).doubleValue();
        addRepaymentForLoan(loanId, totalOutstanding + CHARGEBACK_AMOUNT, payOffDate);
        GetLoansLoanIdResponse overpaidLoan = getLoanDetails(loanId);
        verifyLoanStatus(overpaidLoan, GetLoansLoanIdStatus::getOverpaid);
        double overpaidAmount = Objects.requireNonNull(overpaidLoan.getTotalOverpaid()).doubleValue();
        assertThat(overpaidAmount).isGreaterThan(CHARGE_AMOUNT);
        makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                .transactionDate(payOffDate).locale(LoanTestData.LOCALE).transactionAmount(overpaidAmount));

        updateBusinessDate(RE_AGE_DATE);
        addCharge(loanId, false, CHARGE_AMOUNT, "15 October 2026");

        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, GetLoansLoanIdStatus::getActive);
        assertThat(sumPrincipalPortions(loan, CREDIT_BALANCE_REFUND_TYPE_CODE)).as("Principal portion of the credit balance refund")
                .isEqualByComparingTo(BigDecimal.valueOf(CHARGE_AMOUNT));
        return loanId;
    }

    /**
     * Re-ages the loan and checks that the re-age transaction carries, and the re-aged installments amortize, the whole
     * principal that was outstanding before, and that the re-aged installments charge the interest the re-age took
     * over.
     */
    private void reAgeLoanAndAssertPrincipalTakenOver(Long loanId, Runnable reAge) {
        BigDecimal principalOutstanding = Objects.requireNonNull(getLoanDetails(loanId).getSummary().getPrincipalOutstanding());
        assertThat(principalOutstanding).isGreaterThan(BigDecimal.ZERO);

        reAge.run();

        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        assertThat(loan.getSummary().getPrincipalOutstanding()).as("Principal outstanding after the re-age")
                .isEqualByComparingTo(principalOutstanding);
        assertThat(sumPrincipalPortions(loan, RE_AGE_TYPE_CODE)).as("Principal portion of the re-age transaction")
                .isEqualByComparingTo(principalOutstanding);
        BigDecimal reAgedPrincipal = Objects.requireNonNull(loan.getRepaymentSchedule().getPeriods()).stream()//
                .filter(p -> p.getPeriod() != null && p.getDueDate().isAfter(RE_AGE_LOCAL_DATE))//
                .map(p -> Objects.requireNonNullElse(p.getPrincipalOutstanding(), BigDecimal.ZERO))//
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(reAgedPrincipal).as("Principal outstanding on the re-aged installments").isEqualByComparingTo(principalOutstanding);
        BigDecimal reAgedInterest = Objects.requireNonNull(loan.getRepaymentSchedule().getPeriods()).stream()//
                .filter(p -> p.getPeriod() != null && p.getDueDate().isAfter(RE_AGE_LOCAL_DATE))//
                .map(p -> Objects.requireNonNullElse(p.getInterestDue(), BigDecimal.ZERO))//
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(reAgedInterest).as("Interest due on the re-aged installments covers the interest taken over by the re-age")
                .isGreaterThanOrEqualTo(sumInterestPortions(loan, RE_AGE_TYPE_CODE));
    }

    /**
     * Same as {@link #reAgeLoanAndAssertPrincipalTakenOver(Long, Runnable)}, and checks that the re-age transaction
     * also carries the whole interest that was outstanding before, including the interest of a chargeback posted after
     * maturity.
     */
    private void reAgeLoanAndAssertInterestTakenOver(Long loanId, Runnable reAge) {
        BigDecimal interestOutstanding = Objects.requireNonNull(getLoanDetails(loanId).getSummary().getInterestOutstanding());

        reAgeLoanAndAssertPrincipalTakenOver(loanId, reAge);

        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        assertThat(sumInterestPortions(loan, RE_AGE_TYPE_CODE)).as("Interest portion of the re-age transaction")
                .isEqualByComparingTo(interestOutstanding);
        assertThat(loan.getSummary().getInterestOutstanding()).as("Interest outstanding after the re-age")
                .isGreaterThanOrEqualTo(interestOutstanding);
    }

    private static BigDecimal sumAmounts(GetLoansLoanIdResponse loan, String transactionTypeCode) {
        return Objects.requireNonNull(loan.getTransactions()).stream()//
                .filter(t -> !Boolean.TRUE.equals(t.getManuallyReversed()))//
                .filter(t -> t.getType() != null && transactionTypeCode.equals(t.getType().getCode()))//
                .map(t -> Objects.requireNonNullElse(t.getAmount(), BigDecimal.ZERO))//
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumInterestPortions(GetLoansLoanIdResponse loan, String transactionTypeCode) {
        return Objects.requireNonNull(loan.getTransactions()).stream()//
                .filter(t -> !Boolean.TRUE.equals(t.getManuallyReversed()))//
                .filter(t -> t.getType() != null && transactionTypeCode.equals(t.getType().getCode()))//
                .map(t -> Objects.requireNonNullElse(t.getInterestPortion(), BigDecimal.ZERO))//
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumPrincipalPortions(GetLoansLoanIdResponse loan, String transactionTypeCode) {
        return Objects.requireNonNull(loan.getTransactions()).stream()//
                .filter(t -> !Boolean.TRUE.equals(t.getManuallyReversed()))//
                .filter(t -> t.getType() != null && transactionTypeCode.equals(t.getType().getCode()))//
                .map(t -> Objects.requireNonNullElse(t.getPrincipalPortion(), BigDecimal.ZERO))//
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long createDisbursedLoan() {
        Long clientId = createClient(DISBURSEMENT_DATE);
        Long productId = createLoanProduct(payMonthlyInterestBearingProduct());

        Long loanId = applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, INTEREST_RATE, NUMBER_OF_REPAYMENTS,
                null);
        disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), DISBURSEMENT_DATE);

        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);
        return loanId;
    }

    /**
     * Progressive, interest bearing product with daily interest recalculation and no down payment. Repayments are
     * allocated to the LAST installment, so an early repayment pays off the final installment first.
     */
    private PostLoanProductsRequest payMonthlyInterestBearingProduct() {
        return customizeProduct(create4IProgressive(), p -> p//
                .numberOfRepayments(NUMBER_OF_REPAYMENTS)//
                .interestRatePerPeriod(INTEREST_RATE)//
                .currencyCode("USD")//
                .enableDownPayment(false)//
                .paymentAllocation(List.of(LoanRequestBuilders.paymentAllocation("DEFAULT", "LAST_INSTALLMENT"))));
    }

    private void reAgeLoan(Long loanId) {
        reAgeLoan(loanId, PAYABLE_INTEREST);
    }

    private void reAgeLoan(Long loanId, String interestHandling) {
        reAgeLoan(loanId, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, RE_AGE_START_DATE, RE_AGE_INSTALLMENTS, interestHandling);
    }

    private void reAgeLoanWithDefaultInterestHandling(Long loanId) {
        reAgeLoan(loanId, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, RE_AGE_START_DATE, RE_AGE_INSTALLMENTS, null);
    }

    private void assertReAgedSchedule(Long loanId, boolean equalPrincipal) {
        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, GetLoansLoanIdStatus::getActive);
        assertNotNull(loan.getRepaymentSchedule());
        List<GetLoansLoanIdRepaymentPeriod> periods = Objects.requireNonNull(loan.getRepaymentSchedule().getPeriods());

        List<GetLoansLoanIdRepaymentPeriod> pastPeriods = periods.stream()//
                .filter(p -> p.getPeriod() != null && !p.getDueDate().isAfter(RE_AGE_LOCAL_DATE))//
                .toList();
        List<GetLoansLoanIdRepaymentPeriod> reAgedPeriods = periods.stream()//
                .filter(p -> p.getPeriod() != null && p.getDueDate().isAfter(RE_AGE_LOCAL_DATE))//
                .toList();

        for (GetLoansLoanIdRepaymentPeriod period : pastPeriods) {
            assertThat(period.getTotalOutstandingForPeriod())
                    .as("Installment %s due before the re-age must have nothing left outstanding", period.getPeriod())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        assertThat(reAgedPeriods).as("Re-aged schedule must contain exactly the requested number of installments")
                .hasSize(RE_AGE_INSTALLMENTS);

        BigDecimal firstPrincipal = reAgedPeriods.getFirst().getPrincipalDue();
        for (GetLoansLoanIdRepaymentPeriod period : reAgedPeriods) {
            assertThat(period.getPrincipalDue()).as("Principal due of re-aged installment %s", period.getPeriod())
                    .isGreaterThan(BigDecimal.ZERO);
            if (equalPrincipal) {
                assertThat(period.getPrincipalDue()).as("Principal is equally amortized over re-aged installment %s", period.getPeriod())
                        .isCloseTo(firstPrincipal, offset(new BigDecimal("0.02")));
            }
        }

        BigDecimal totalPrincipalDue = periods.stream()//
                .filter(p -> p.getPeriod() != null)//
                .map(GetLoansLoanIdRepaymentPeriod::getPrincipalDue)//
                .filter(Objects::nonNull)//
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // the whole refunded amount is credited to the schedule as principal, the part covered by the overpayment is
        // paid off at once
        BigDecimal creditedPrincipal = sumPrincipalPortions(loan, CHARGEBACK_TYPE_CODE)
                .add(sumAmounts(loan, CREDIT_BALANCE_REFUND_TYPE_CODE));
        assertThat(totalPrincipalDue).as("Total principal due must equal the disbursed amount plus the charged back and refunded principal")
                .isEqualByComparingTo(BigDecimal.valueOf(PRINCIPAL).add(creditedPrincipal));
    }
}
