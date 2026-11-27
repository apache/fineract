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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.loans.CobHelper;
import org.junit.jupiter.api.Test;

/**
 * Covers the operations that rebuild the repayment schedule of a loan whose outstanding charge was taken over by an
 * equal amortization re-age.
 *
 * <p>
 * Such a re-age spreads the charge over the re-aged installments as installment charges. When the schedule is rebuilt
 * later, by regenerating it or by replaying the transactions, the re-aged installments are dropped and created again.
 * The installment charges of the dropped installments must go with them: the schedule must end up with exactly the
 * re-aged installments the re-age asked for, each installment number once, and the loan must stay usable.
 * </p>
 */
public class FeignLoanReAgedChargeScheduleRebuildTest extends FeignLoanTestBase {

    private static final String DISBURSEMENT_DATE = "14 April 2026";
    private static final String REPAYMENT_DATE = "20 April 2026";
    private static final String CHARGE_DUE_DATE = "20 October 2026";
    private static final String RE_AGE_DATE = "22 October 2026";
    private static final String RE_AGE_START_DATE = "22 November 2026";
    private static final LocalDate RE_AGE_LOCAL_DATE = LocalDate.of(2026, 10, 22);
    private static final double PRINCIPAL = 600.0;
    private static final double EARLY_REPAYMENT = 200.0;
    private static final double INTEREST_RATE = 9.99;
    private static final double CHARGE_AMOUNT = 10.0;
    private static final int NUMBER_OF_REPAYMENTS = 6;
    private static final int RE_AGE_INSTALLMENTS = 3;
    private static final String PAYABLE_INTEREST = "EQUAL_AMORTIZATION_PAYABLE_INTEREST";
    private static final String FULL_INTEREST = "EQUAL_AMORTIZATION_FULL_INTEREST";

    /**
     * PS-3557: the early repayment is reversed after the re-age took over a penalty that was due after maturity, on an
     * additional (N+1) installment.
     */
    @Test
    void reverseRepaymentAfterReAgeWithPenaltyDueAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            Long loanWithoutRepaymentId = createDisbursedLoan();
            Long repaymentId = repayEarly(loanId, EARLY_REPAYMENT);
            reAgeWithChargeDueAfterMaturity(loanId, true, PAYABLE_INTEREST);
            reAgeWithChargeDueAfterMaturity(loanWithoutRepaymentId, true, PAYABLE_INTEREST);
            assertReAgedSchedule(loanId, PRINCIPAL - EARLY_REPAYMENT);

            reverseRepayment(loanId, repaymentId, RE_AGE_DATE);

            assertReAgedSchedule(loanId, PRINCIPAL);
            assertSameScheduleAs(loanId, loanWithoutRepaymentId);
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
    }

    /**
     * Same as {@link #reverseRepaymentAfterReAgeWithPenaltyDueAfterMaturity()}, with a fee instead of a penalty.
     */
    @Test
    void reverseRepaymentAfterReAgeWithFeeDueAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            Long repaymentId = repayEarly(loanId, EARLY_REPAYMENT);
            reAgeWithChargeDueAfterMaturity(loanId, false, PAYABLE_INTEREST);

            reverseRepayment(loanId, repaymentId, RE_AGE_DATE);

            assertReAgedSchedule(loanId, PRINCIPAL);
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
    }

    /**
     * Same as {@link #reverseRepaymentAfterReAgeWithPenaltyDueAfterMaturity()}, with the full interest equal
     * amortization handling.
     */
    @Test
    void reverseRepaymentAfterFullInterestReAge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            Long loanWithoutRepaymentId = createDisbursedLoan();
            Long repaymentId = repayEarly(loanId, EARLY_REPAYMENT);
            reAgeWithChargeDueAfterMaturity(loanId, true, FULL_INTEREST);
            reAgeWithChargeDueAfterMaturity(loanWithoutRepaymentId, true, FULL_INTEREST);

            reverseRepayment(loanId, repaymentId, RE_AGE_DATE);

            assertReAgedSchedule(loanId, PRINCIPAL);
            assertSameScheduleAs(loanId, loanWithoutRepaymentId);
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
    }

    /**
     * PS-3557: a second repayment reversal on the same loan, the case the ticket saw the duplicates come back with.
     */
    @Test
    void reverseTwoRepaymentsOneAfterTheOtherAfterReAge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            Long firstRepaymentId = repayEarly(loanId, 100.0);
            Long secondRepaymentId = addRepaymentForLoan(loanId, 100.0, REPAYMENT_DATE);
            reAgeWithChargeDueAfterMaturity(loanId, true, PAYABLE_INTEREST);
            assertReAgedSchedule(loanId, PRINCIPAL - EARLY_REPAYMENT);

            reverseRepayment(loanId, firstRepaymentId, RE_AGE_DATE);
            assertReAgedSchedule(loanId, PRINCIPAL - 100.0);

            reverseRepayment(loanId, secondRepaymentId, RE_AGE_DATE);
            assertReAgedSchedule(loanId, PRINCIPAL);
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
    }

    /**
     * PS-3557: the loan is reprocessed after the re-age, which regenerates the schedule with interest recalculation,
     * and the repayment is reversed afterwards.
     */
    @Test
    void reprocessThenReverseRepaymentAfterReAge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            Long repaymentId = repayEarly(loanId, EARLY_REPAYMENT);
            reAgeWithChargeDueAfterMaturity(loanId, true, PAYABLE_INTEREST);

            CobHelper.reprocessLoan(loanId);
            assertReAgedSchedule(loanId, PRINCIPAL - EARLY_REPAYMENT);

            reverseRepayment(loanId, repaymentId, RE_AGE_DATE);
            assertReAgedSchedule(loanId, PRINCIPAL);

            CobHelper.reprocessLoan(loanId);
            assertReAgedSchedule(loanId, PRINCIPAL);
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
    }

    /**
     * A repayment backdated before the re-age regenerates the schedule and replays the re-age.
     */
    @Test
    void backdatedRepaymentAfterReAge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            repayEarly(loanId, EARLY_REPAYMENT);
            reAgeWithChargeDueAfterMaturity(loanId, true, PAYABLE_INTEREST);

            addRepaymentForLoan(loanId, 50.0, "01 October 2026");

            assertReAgedSchedule(loanId, PRINCIPAL - EARLY_REPAYMENT - 50.0);
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
    }

    /**
     * A repayment made after the re-age is reversed. The reversal regenerates the schedule and replays the re-age.
     */
    @Test
    void reverseRepaymentMadeAfterReAge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            repayEarly(loanId, EARLY_REPAYMENT);
            reAgeWithChargeDueAfterMaturity(loanId, true, PAYABLE_INTEREST);

            updateBusinessDate("25 October 2026");
            Long repaymentId = addRepaymentForLoan(loanId, 50.0, "25 October 2026");

            reverseRepayment(loanId, repaymentId, "25 October 2026");

            assertReAgedSchedule(loanId, PRINCIPAL - EARLY_REPAYMENT);
            assertLoanStaysUsable(loanId, "25 October 2026");
        });
    }

    /**
     * Undoing the re-age regenerates the schedule: the charge is back on the additional (N+1) installment.
     */
    @Test
    void undoReAge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            repayEarly(loanId, EARLY_REPAYMENT);
            reAgeWithChargeDueAfterMaturity(loanId, true, PAYABLE_INTEREST);

            undoReAgeLoan(loanId);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            List<GetLoansLoanIdRepaymentPeriod> periods = assertValidSchedule(loan);
            assertThat(periods).as("Regular installments and the additional (N+1) installment").hasSize(NUMBER_OF_REPAYMENTS + 1);
            assertThat(periods.getLast().getDueDate()).isEqualTo(LocalDate.of(2026, 10, 20));
            assertThat(periods.getLast().getPenaltyChargesOutstanding()).isEqualByComparingTo(BigDecimal.valueOf(CHARGE_AMOUNT));
            assertThat(sum(periods, GetLoansLoanIdRepaymentPeriod::getPrincipalDue)).isEqualByComparingTo(BigDecimal.valueOf(PRINCIPAL));
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
    }

    /**
     * Waiving the charge after the re-age took it over.
     */
    @Test
    void waiveChargeAfterReAge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            Long repaymentId = repayEarly(loanId, EARLY_REPAYMENT);
            Long loanChargeId = reAgeWithChargeDueAfterMaturity(loanId, true, PAYABLE_INTEREST);

            waiveLoanCharge(loanId, loanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));
            assertReAgedSchedule(loanId, PRINCIPAL - EARLY_REPAYMENT, 0.0);

            reverseRepayment(loanId, repaymentId, RE_AGE_DATE);
            assertReAgedSchedule(loanId, PRINCIPAL, 0.0);
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
    }

    /**
     * A second equal amortization re-age drops the installments of the first one.
     */
    @Test
    void secondReAgeAfterReAge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            Long repaymentId = repayEarly(loanId, EARLY_REPAYMENT);
            reAgeWithChargeDueAfterMaturity(loanId, true, PAYABLE_INTEREST);

            updateBusinessDate("25 November 2026");
            reAgeLoan(loanId, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, "25 December 2026", RE_AGE_INSTALLMENTS,
                    PAYABLE_INTEREST);
            // The second re-age moves the interest of the first one to its first installment and shifts principal to
            // keep that installment's total, so its principal is not equal
            assertReAgedSchedule(loanId, PRINCIPAL - EARLY_REPAYMENT, CHARGE_AMOUNT, LocalDate.of(2026, 11, 25), false);

            reverseRepayment(loanId, repaymentId, "25 November 2026");
            assertReAgedSchedule(loanId, PRINCIPAL, CHARGE_AMOUNT, LocalDate.of(2026, 11, 25), false);
            assertLoanStaysUsable(loanId, "25 November 2026");
        });
    }

    /**
     * The loan is re-aged before maturity, so the re-aged installments take the numbers of regular installments, which
     * the regenerated schedule has again.
     */
    @Test
    void reverseRepaymentAfterReAgeBeforeMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            Long loanWithoutRepaymentId = createDisbursedLoan();
            Long repaymentId = repayEarly(loanId, 100.0);

            updateBusinessDate("20 August 2026");
            for (Long id : List.of(loanId, loanWithoutRepaymentId)) {
                addCharge(id, true, CHARGE_AMOUNT, "01 July 2026");
                reAgeLoan(id, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, "20 September 2026", RE_AGE_INSTALLMENTS,
                        PAYABLE_INTEREST);
            }
            LocalDate reAgeDate = LocalDate.of(2026, 8, 20);
            assertReAgedSchedule(loanId, PRINCIPAL - 100.0, CHARGE_AMOUNT, reAgeDate);

            reverseRepayment(loanId, repaymentId, "20 August 2026");

            assertReAgedSchedule(loanId, PRINCIPAL, CHARGE_AMOUNT, reAgeDate);
            assertSameScheduleAs(loanId, loanWithoutRepaymentId);
            assertLoanStaysUsable(loanId, "20 August 2026");
        });
    }

    /**
     * The re-age takes over an installment fee, which has installment charges on the regular installments too.
     */
    @Test
    void reverseRepaymentAfterReAgeWithInstallmentFee() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            addLoanCharge(loanId, createInstallmentFeeCharge(2.0), null, 2.0);
            Long repaymentId = repayEarly(loanId, EARLY_REPAYMENT);

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoan(loanId, PAYABLE_INTEREST);
            BigDecimal feesBefore = sum(getLoanDetails(loanId).getRepaymentSchedule().getPeriods(),
                    GetLoansLoanIdRepaymentPeriod::getFeeChargesOutstanding);
            assertThat(feesBefore).isGreaterThan(BigDecimal.ZERO);

            reverseRepayment(loanId, repaymentId, RE_AGE_DATE);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            List<GetLoansLoanIdRepaymentPeriod> periods = assertValidSchedule(loan);
            assertThat(reAgedPeriods(periods, RE_AGE_LOCAL_DATE)).hasSize(RE_AGE_INSTALLMENTS);
            assertThat(sum(reAgedPeriods(periods, RE_AGE_LOCAL_DATE), GetLoansLoanIdRepaymentPeriod::getPrincipalDue))
                    .isEqualByComparingTo(BigDecimal.valueOf(PRINCIPAL));
            assertThat(sum(periods, GetLoansLoanIdRepaymentPeriod::getFeeChargesOutstanding))
                    .as("Fees outstanding on the schedule match the fees outstanding on the loan")
                    .isEqualByComparingTo(loan.getSummary().getFeeChargesOutstanding());
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
    }

    /**
     * The default interest handling does not spread the charge over the re-aged installments, the reversal must work
     * the same.
     */
    @Test
    void reverseRepaymentAfterDefaultReAge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();
            Long loanWithoutRepaymentId = createDisbursedLoan();
            Long repaymentId = repayEarly(loanId, EARLY_REPAYMENT);
            reAgeWithChargeDueAfterMaturity(loanId, true, null);
            reAgeWithChargeDueAfterMaturity(loanWithoutRepaymentId, true, null);

            reverseRepayment(loanId, repaymentId, RE_AGE_DATE);
            assertSameScheduleAs(loanId, loanWithoutRepaymentId);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            List<GetLoansLoanIdRepaymentPeriod> periods = assertValidSchedule(loan);
            assertThat(sum(periods, GetLoansLoanIdRepaymentPeriod::getPrincipalDue)).isEqualByComparingTo(BigDecimal.valueOf(PRINCIPAL));
            assertThat(sum(periods, GetLoansLoanIdRepaymentPeriod::getPenaltyChargesOutstanding))
                    .isEqualByComparingTo(BigDecimal.valueOf(CHARGE_AMOUNT));
            assertLoanStaysUsable(loanId, RE_AGE_DATE);
        });
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
     * allocated to the LAST installment, so an early repayment pays off the final installments first.
     */
    private PostLoanProductsRequest payMonthlyInterestBearingProduct() {
        return customizeProduct(create4IProgressive(), p -> p//
                .numberOfRepayments(NUMBER_OF_REPAYMENTS)//
                .interestRatePerPeriod(INTEREST_RATE)//
                .currencyCode("USD")//
                .enableDownPayment(false)//
                .paymentAllocation(List.of(LoanRequestBuilders.paymentAllocation("DEFAULT", "LAST_INSTALLMENT"))));
    }

    private Long repayEarly(Long loanId, double amount) {
        updateBusinessDate(REPAYMENT_DATE);
        return addRepaymentForLoan(loanId, amount, REPAYMENT_DATE);
    }

    /**
     * Adds a charge due after maturity, on an additional (N+1) installment, and re-ages the loan.
     *
     * @return the id of the loan charge
     */
    private Long reAgeWithChargeDueAfterMaturity(Long loanId, boolean penalty, String interestHandling) {
        updateBusinessDate(RE_AGE_DATE);
        Long loanChargeId = addCharge(loanId, penalty, CHARGE_AMOUNT, CHARGE_DUE_DATE);
        reAgeLoan(loanId, interestHandling);
        return loanChargeId;
    }

    private void reAgeLoan(Long loanId, String interestHandling) {
        reAgeLoan(loanId, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, RE_AGE_START_DATE, RE_AGE_INSTALLMENTS, interestHandling);
    }

    private void assertReAgedSchedule(Long loanId, double principalOutstanding) {
        assertReAgedSchedule(loanId, principalOutstanding, CHARGE_AMOUNT);
    }

    private void assertReAgedSchedule(Long loanId, double principalOutstanding, double chargeOutstanding) {
        assertReAgedSchedule(loanId, principalOutstanding, chargeOutstanding, RE_AGE_LOCAL_DATE);
    }

    /**
     * Checks that the schedule has each installment number once, nothing outstanding before the re-age date and exactly
     * the requested number of re-aged installments, which amortize the principal and the charge outstanding equally.
     */
    private void assertReAgedSchedule(Long loanId, double principalOutstanding, double chargeOutstanding, LocalDate reAgeDate) {
        assertReAgedSchedule(loanId, principalOutstanding, chargeOutstanding, reAgeDate, true);
    }

    private void assertReAgedSchedule(Long loanId, double principalOutstanding, double chargeOutstanding, LocalDate reAgeDate,
            boolean equalPrincipal) {
        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, GetLoansLoanIdStatus::getActive);
        List<GetLoansLoanIdRepaymentPeriod> periods = assertValidSchedule(loan);

        periods.stream().filter(p -> !p.getDueDate().isAfter(reAgeDate)).forEach(p -> assertThat(p.getTotalOutstandingForPeriod())
                .as("Installment %s due before the re-age has nothing outstanding", p.getPeriod()).isEqualByComparingTo(BigDecimal.ZERO));

        List<GetLoansLoanIdRepaymentPeriod> reAgedPeriods = reAgedPeriods(periods, reAgeDate);
        assertThat(reAgedPeriods).as("Re-aged installments").hasSize(RE_AGE_INSTALLMENTS);
        if (equalPrincipal) {
            // The re-age rounds each installment, so the last ones may differ by a few cents
            BigDecimal firstPrincipal = reAgedPeriods.getFirst().getPrincipalOutstanding();
            reAgedPeriods.forEach(p -> assertThat(p.getPrincipalOutstanding())
                    .as("Principal is equally amortized over re-aged installment %s", p.getPeriod())
                    .isCloseTo(firstPrincipal, offset(new BigDecimal("0.05"))));
        }

        assertThat(sum(reAgedPeriods, GetLoansLoanIdRepaymentPeriod::getPrincipalOutstanding)).as("Principal outstanding")
                .isEqualByComparingTo(BigDecimal.valueOf(principalOutstanding))
                .isEqualByComparingTo(loan.getSummary().getPrincipalOutstanding());
        assertThat(sum(periods, GetLoansLoanIdRepaymentPeriod::getPrincipalDue)).as("Principal due on the schedule")
                .isEqualByComparingTo(BigDecimal.valueOf(PRINCIPAL));

        BigDecimal chargesOutstanding = sum(reAgedPeriods, GetLoansLoanIdRepaymentPeriod::getFeeChargesOutstanding)
                .add(sum(reAgedPeriods, GetLoansLoanIdRepaymentPeriod::getPenaltyChargesOutstanding));
        assertThat(chargesOutstanding).as("Charges outstanding on the re-aged installments")
                .isEqualByComparingTo(BigDecimal.valueOf(chargeOutstanding));
        assertThat(chargesOutstanding).as("Charges outstanding on the schedule match the loan")
                .isEqualByComparingTo(loan.getSummary().getFeeChargesOutstanding().add(loan.getSummary().getPenaltyChargesOutstanding()));
    }

    /**
     * Reversing a repayment must leave the loan as if the repayment had never been made: the schedule must equal the
     * one of a loan that went through the same steps without it.
     */
    private void assertSameScheduleAs(Long loanId, Long expectedLoanId) {
        assertThat(scheduleOf(loanId)).as("Schedule after the reversal").isEqualTo(scheduleOf(expectedLoanId));
    }

    private List<String> scheduleOf(Long loanId) {
        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        List<String> rows = new ArrayList<>(assertValidSchedule(loan).stream()
                .map(p -> String.format("#%s %s principal %s interest %s fee %s penalty %s outstanding %s", p.getPeriod(), p.getDueDate(),
                        amount(p.getPrincipalDue()), amount(p.getInterestDue()), amount(p.getFeeChargesDue()),
                        amount(p.getPenaltyChargesDue()), amount(p.getTotalOutstandingForPeriod())))
                .toList());
        rows.add("total outstanding " + amount(loan.getSummary().getTotalOutstanding()));
        return rows;
    }

    private static String amount(BigDecimal value) {
        return Objects.requireNonNullElse(value, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_EVEN).toPlainString();
    }

    /**
     * A later write request, and the close of business, must still work on the loan.
     */
    private void assertLoanStaysUsable(Long loanId, String businessDate) {
        executeInlineCOB(loanId);
        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        assertValidSchedule(loan);
        Double payoffAmount = Objects.requireNonNull(getPrepayAmount(loanId, businessDate).getAmount());
        addRepaymentForLoan(loanId, payoffAmount, businessDate);
        assertValidSchedule(getLoanDetails(loanId));
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getClosedObligationsMet);
    }

    /**
     * Checks that each installment number is in the schedule once and that the balance of the loan never goes below
     * zero and ends at zero. Installments left over from an earlier schedule show up as duplicate numbers, and their
     * principal on top of the loan's drives the balance negative.
     */
    private static List<GetLoansLoanIdRepaymentPeriod> assertValidSchedule(GetLoansLoanIdResponse loan) {
        List<GetLoansLoanIdRepaymentPeriod> periods = Objects.requireNonNull(loan.getRepaymentSchedule().getPeriods()).stream()
                .filter(p -> p.getPeriod() != null).toList();
        assertThat(periods.stream().map(GetLoansLoanIdRepaymentPeriod::getPeriod).toList()).as("Installment numbers")
                .doesNotHaveDuplicates();
        periods.forEach(p -> assertThat(p.getPrincipalLoanBalanceOutstanding())
                .as("Balance of the loan after installment %s", p.getPeriod()).isGreaterThanOrEqualTo(BigDecimal.ZERO));
        assertThat(periods.getLast().getPrincipalLoanBalanceOutstanding()).as("Balance of the loan after the last installment")
                .isEqualByComparingTo(BigDecimal.ZERO);
        return periods;
    }

    private static List<GetLoansLoanIdRepaymentPeriod> reAgedPeriods(List<GetLoansLoanIdRepaymentPeriod> periods, LocalDate reAgeDate) {
        return periods.stream().filter(p -> p.getDueDate().isAfter(reAgeDate)).toList();
    }

    private static BigDecimal sum(List<GetLoansLoanIdRepaymentPeriod> periods, Function<GetLoansLoanIdRepaymentPeriod, BigDecimal> amount) {
        return Objects.requireNonNull(periods).stream().filter(p -> p.getPeriod() != null)
                .map(p -> Objects.requireNonNullElse(amount.apply(p), BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
