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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class DownPaymentFeesTest {

    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);
    private static final LocalDate JAN_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate JAN_2 = LocalDate.of(2026, 1, 2);
    private static final LocalDate JAN_3 = LocalDate.of(2026, 1, 3);

    private final MonetaryCurrency currency = MonetaryCurrency.fromCurrencyData(new CurrencyData("EUR"));
    private final Charge flatFee = mockDefinition(1L);
    private final Charge otherFee = mockDefinition(2L);

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    public static void destruct() {
        MONEY_HELPER.close();
    }

    @Test
    public void testDueOnReturnsActiveDownPaymentFeesOfTheDateById() {
        final LoanCharge second = mockFee(20L, flatFee, JAN_1);
        final LoanCharge first = mockFee(10L, flatFee, JAN_1);
        final LoanCharge otherDate = mockFee(30L, flatFee, JAN_2);
        final LoanCharge inactive = mockFee(40L, flatFee, JAN_1);
        when(inactive.isActive()).thenReturn(false);
        final LoanCharge notDownPayment = mockFee(50L, flatFee, JAN_1);
        when(notDownPayment.isDueAtDownPayment()).thenReturn(false);

        assertEquals(List.of(first, second), DownPaymentFees.dueOn(List.of(second, first, otherDate, inactive, notDownPayment), JAN_1));
        assertEquals(List.of(), DownPaymentFees.dueOn(null, JAN_1));
        assertEquals(List.of(), DownPaymentFees.dueOn(List.of(first), null));
    }

    @Test
    public void testDownPaymentPeriodsOnReturnsDownPaymentPeriodsOfTheDateByInstallmentNumber() {
        final LoanRepaymentScheduleInstallment third = mockPeriod(3, JAN_1, true);
        final LoanRepaymentScheduleInstallment first = mockPeriod(1, JAN_1, true);
        final LoanRepaymentScheduleInstallment regular = mockPeriod(2, JAN_1, false);
        final LoanRepaymentScheduleInstallment otherDate = mockPeriod(4, JAN_2, true);

        assertEquals(List.of(first, third), DownPaymentFees.downPaymentPeriodsOn(List.of(third, regular, otherDate, first), JAN_1));
    }

    @Test
    public void testFeeWithoutTrancheBelongsToTheFirstPeriodOfItsDate() {
        final LoanCharge first = mockFee(10L, flatFee, JAN_1);
        final LoanCharge second = mockFee(20L, flatFee, JAN_1);
        final List<LoanCharge> charges = List.of(first, second);

        assertEquals(0, DownPaymentFees.periodIndexOf(second, 1));
        assertEquals(0, DownPaymentFees.periodIndexOf(second, 2));
        assertEquals(List.of(first, second), DownPaymentFees.ofPeriod(charges, JAN_1, 0, 1));
    }

    @Test
    public void testSameDayTranchesOwnTheDownPaymentPeriodsInTheirOrder() {
        // same date and principal: equal by LoanDisbursementDetails.equals, still different tranches
        final LoanDisbursementDetails firstTranche = tranche(JAN_1);
        final LoanDisbursementDetails secondTranche = tranche(JAN_1);
        final Loan loan = mockLoan(firstTranche, secondTranche, tranche(JAN_2));
        final LoanCharge firstTrancheFee = mockFee(10L, flatFee, JAN_1, loan, firstTranche);
        final LoanCharge firstTrancheOtherFee = mockFee(11L, otherFee, JAN_1, loan, firstTranche);
        final LoanCharge secondTrancheFee = mockFee(20L, flatFee, JAN_1, loan, secondTranche);
        final LoanCharge secondTrancheExtraFee = mockFee(30L, flatFee, JAN_1, loan, secondTranche);
        final List<LoanCharge> charges = List.of(secondTrancheExtraFee, secondTrancheFee, firstTrancheOtherFee, firstTrancheFee);

        assertEquals(0, DownPaymentFees.periodIndexOf(firstTrancheFee, 2));
        assertEquals(1, DownPaymentFees.periodIndexOf(secondTrancheFee, 2));
        assertEquals(List.of(firstTrancheFee, firstTrancheOtherFee), DownPaymentFees.ofPeriod(charges, JAN_1, 0, 2));
        assertEquals(List.of(secondTrancheFee, secondTrancheExtraFee), DownPaymentFees.ofPeriod(charges, JAN_1, 1, 2));
    }

    @Test
    public void testOfPeriodResolvesTheIndexOfTheGivenDownPaymentPeriod() {
        final LoanDisbursementDetails firstTranche = tranche(JAN_1);
        final LoanDisbursementDetails secondTranche = tranche(JAN_1);
        final Loan loan = mockLoan(firstTranche, secondTranche);
        final LoanCharge firstTrancheFee = mockFee(10L, flatFee, JAN_1, loan, firstTranche);
        final LoanCharge secondTrancheFee = mockFee(20L, flatFee, JAN_1, loan, secondTranche);
        final LoanRepaymentScheduleInstallment firstPeriod = mockPeriod(1, JAN_1, true);
        final LoanRepaymentScheduleInstallment secondPeriod = mockPeriod(2, JAN_1, true);
        final List<LoanRepaymentScheduleInstallment> installments = List.of(secondPeriod, firstPeriod);
        final List<LoanCharge> charges = List.of(secondTrancheFee, firstTrancheFee);

        assertEquals(List.of(firstTrancheFee), DownPaymentFees.ofPeriod(charges, firstPeriod, installments));
        assertEquals(List.of(secondTrancheFee), DownPaymentFees.ofPeriod(charges, secondPeriod, installments));
    }

    @Test
    public void testFeeAddedLaterToTheFirstTrancheStillBelongsToTheFirstPeriod() {
        // the first tranche was disbursed with skipDownPaymentFee, its fee was added manually after the second one
        final LoanDisbursementDetails firstTranche = tranche(JAN_1);
        final LoanDisbursementDetails secondTranche = tranche(JAN_1);
        final Loan loan = mockLoan(firstTranche, secondTranche);
        final LoanCharge secondTrancheFee = mockFee(9L, flatFee, JAN_1, loan, secondTranche);
        final LoanCharge firstTrancheFee = mockFee(15L, flatFee, JAN_1, loan, firstTranche);
        final LoanRepaymentScheduleInstallment firstPeriod = mockPeriod(1, JAN_1, true);
        final LoanRepaymentScheduleInstallment secondPeriod = mockPeriod(2, JAN_1, true);
        final List<LoanRepaymentScheduleInstallment> installments = List.of(firstPeriod, secondPeriod);
        final List<LoanCharge> charges = List.of(secondTrancheFee, firstTrancheFee);

        assertEquals(List.of(firstTrancheFee), DownPaymentFees.ofPeriod(charges, firstPeriod, installments));
        assertEquals(List.of(secondTrancheFee), DownPaymentFees.ofPeriod(charges, secondPeriod, installments));
    }

    @Test
    public void testDownPaymentFeeBelongsOnlyToItsOwnTrancheInstance() {
        final LoanDisbursementDetails ownTranche = tranche(JAN_1);
        final LoanDisbursementDetails equalTranche = tranche(JAN_1);
        final LoanCharge fee = new LoanCharge();
        fee.setChargeTime(ChargeTimeType.DOWN_PAYMENT.getValue());
        fee.setDisbursementDetail(ownTranche);

        assertTrue(fee.isDownPaymentFeeOf(ownTranche));
        assertFalse(fee.isDownPaymentFeeOf(equalTranche));
        assertFalse(fee.isDownPaymentFeeOf(null));
        fee.setChargeTime(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue());
        assertFalse(fee.isDownPaymentFeeOf(ownTranche));
    }

    @Test
    public void testSavedTranchesAreComparedById() {
        final LoanDisbursementDetails ownTranche = tranche(JAN_1);
        ownTranche.setId(1L);
        final LoanDisbursementDetails ownTrancheLoadedAgain = tranche(JAN_1);
        ownTrancheLoadedAgain.setId(1L);
        final LoanDisbursementDetails sameDayTranche = tranche(JAN_1);
        sameDayTranche.setId(2L);
        final LoanCharge fee = new LoanCharge();
        fee.setChargeTime(ChargeTimeType.DOWN_PAYMENT.getValue());
        fee.setDisbursementDetail(ownTranche);

        assertTrue(fee.isDownPaymentFeeOf(ownTrancheLoadedAgain));
        assertFalse(fee.isDownPaymentFeeOf(sameDayTranche));
        assertFalse(fee.isDownPaymentFeeOf(tranche(JAN_1)));
        fee.setDisbursementDetail(null);
        assertFalse(fee.isDownPaymentFeeOf(ownTranche));
    }

    @Test
    public void testAmountToCollectPrefersOutstandingSkipsWaivedAndCollectsPaidFeesInFull() {
        final LoanCharge partiallyPaid = mockFee(10L, flatFee, JAN_1);
        mockAmounts(partiallyPaid, "10", "4");
        final LoanCharge paid = mockFee(20L, otherFee, JAN_1);
        mockAmounts(paid, "5", "0");
        final LoanCharge waived = mockFee(30L, otherFee, JAN_1);
        mockAmounts(waived, "7", "0");
        when(waived.isWaived()).thenReturn(true);

        final Money amount = DownPaymentFees.amountToCollect(List.of(partiallyPaid, paid, waived), currency, JAN_1, 0, 1);

        assertEquals(0, new BigDecimal("9").compareTo(amount.getAmount()));
    }

    @Test
    public void testFeeOfUndisbursedTrancheAwaitsItsDisbursement() {
        final LoanDisbursementDetails expectedTranche = new LoanDisbursementDetails(JAN_1, null, BigDecimal.valueOf(500), null, false);
        final LoanCharge fee = new LoanCharge();
        fee.setChargeTime(ChargeTimeType.DOWN_PAYMENT.getValue());
        fee.setDisbursementDetail(expectedTranche);

        assertTrue(fee.isDownPaymentFeeAwaitingDisbursement());
        expectedTranche.updateActualDisbursementDate(JAN_2);
        assertFalse(fee.isDownPaymentFeeAwaitingDisbursement());
        expectedTranche.updateActualDisbursementDate(null);
        fee.setChargeTime(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue());
        assertFalse(fee.isDownPaymentFeeAwaitingDisbursement());
        fee.setChargeTime(ChargeTimeType.DOWN_PAYMENT.getValue());
        fee.setDisbursementDetail(null);
        assertFalse(fee.isDownPaymentFeeAwaitingDisbursement());
    }

    @Test
    public void testOnlyAnActiveFeeKeepsTheFeeItReplaces() {
        final LoanCharge configuredFee = new LoanCharge();
        final LoanCharge overrideFee = new LoanCharge();
        overrideFee.setReplacedLoanCharge(configuredFee);

        overrideFee.setActive(false);

        assertNull(overrideFee.getReplacedLoanCharge());
    }

    @Test
    public void testDueOnSkipsFeesAwaitingTheirTranche() {
        final LoanCharge disbursed = mockFee(10L, flatFee, JAN_1);
        final LoanCharge awaiting = mockFee(20L, flatFee, JAN_1);
        when(awaiting.isDownPaymentFeeAwaitingDisbursement()).thenReturn(true);

        assertEquals(List.of(disbursed), DownPaymentFees.dueOn(List.of(disbursed, awaiting), JAN_1));
        assertEquals(List.of(disbursed), DownPaymentFees.ofPeriod(List.of(disbursed, awaiting), JAN_1, 0, 1));
    }

    private void mockAmounts(final LoanCharge fee, final String amount, final String outstanding) {
        // Money is built before stubbing: Money.of calls the static MoneyHelper mock
        final Money amountMoney = Money.of(currency, new BigDecimal(amount));
        final Money outstandingMoney = Money.of(currency, new BigDecimal(outstanding));
        when(fee.getAmount(currency)).thenReturn(amountMoney);
        when(fee.getAmountOutstanding(currency)).thenReturn(outstandingMoney);
    }

    private static LoanDisbursementDetails tranche(final LocalDate date) {
        return new LoanDisbursementDetails(date, date, BigDecimal.valueOf(500), null, false);
    }

    private static Loan mockLoan(final LoanDisbursementDetails... tranches) {
        final Loan loan = mock(Loan.class);
        when(loan.getDisbursementDetails()).thenReturn(List.of(tranches));
        return loan;
    }

    @Test
    public void testRefundAmountIsRatioOfTheOriginalFeeAmounts() {
        final Loan loan = mockRefundLoan(List.of(disbursement(JAN_1, 1000), disbursement(JAN_1, 500)),
                List.of(chargedFee(JAN_1, 25), chargedFee(JAN_1, "12.50")));

        assertEqualsAmount("18.75", DownPaymentFees.refundAmount(loan, BigDecimal.valueOf(750), JAN_2, BigDecimal.ZERO));
    }

    @Test
    public void testRefundAmountIsRoundedByTheLoanCurrency() {
        final Loan loan = mockRefundLoan(List.of(disbursement(JAN_1, 1500)), List.of(chargedFee(JAN_1, 50)));

        assertEqualsAmount("16.67", DownPaymentFees.refundAmount(loan, BigDecimal.valueOf(500), JAN_2, BigDecimal.ZERO));
    }

    @Test
    public void testRefundAmountIsCappedByWhatIsLeftToRefund() {
        final Loan loan = mockRefundLoan(List.of(disbursement(JAN_1, 1000)), List.of(chargedFee(JAN_1, 25)));

        assertEqualsAmount("10", DownPaymentFees.refundAmount(loan, BigDecimal.valueOf(600), JAN_2, BigDecimal.valueOf(15)));
        assertEqualsAmount("0", DownPaymentFees.refundAmount(loan, BigDecimal.valueOf(600), JAN_2, BigDecimal.valueOf(25)));
    }

    @Test
    public void testRefundAmountCountsOnlyWhatExistsOnTheRefundDate() {
        final Loan loan = mockRefundLoan(List.of(disbursement(JAN_1, 1000), disbursement(JAN_3, 500)),
                List.of(chargedFee(JAN_1, 25), chargedFee(JAN_3, 25)));

        assertEqualsAmount("12.50", DownPaymentFees.refundAmount(loan, BigDecimal.valueOf(500), JAN_2, BigDecimal.ZERO));
        assertEqualsAmount("16.67", DownPaymentFees.refundAmount(loan, BigDecimal.valueOf(500), JAN_3, BigDecimal.ZERO));
    }

    @Test
    public void testRefundAmountSkipsTheFeeOfATrancheNotDisbursedYet() {
        final LoanCharge awaitingFee = chargedFee(JAN_1, 10);
        when(awaitingFee.isDownPaymentFeeAwaitingDisbursement()).thenReturn(true);
        final Loan loan = mockRefundLoan(List.of(disbursement(JAN_1, 600)), List.of(chargedFee(JAN_1, 15), awaitingFee));

        assertEqualsAmount("7.50", DownPaymentFees.refundAmount(loan, BigDecimal.valueOf(300), JAN_2, BigDecimal.ZERO));
    }

    @Test
    public void testRefundAmountSkipsReversedDisbursementsAndInactiveFees() {
        final LoanTransaction reversed = disbursement(JAN_1, 1000);
        when(reversed.isNotReversed()).thenReturn(false);
        final LoanCharge inactive = chargedFee(JAN_1, 25);
        when(inactive.isActive()).thenReturn(false);
        final LoanCharge notDownPayment = chargedFee(JAN_1, 25);
        when(notDownPayment.isDueAtDownPayment()).thenReturn(false);
        final Loan loan = mockRefundLoan(List.of(reversed, disbursement(JAN_1, 500)),
                List.of(chargedFee(JAN_1, 10), inactive, notDownPayment));

        assertEqualsAmount("5", DownPaymentFees.refundAmount(loan, BigDecimal.valueOf(250), JAN_2, BigDecimal.ZERO));
    }

    @Test
    public void testRefundAmountIsZeroWithoutDisbursementOrFee() {
        assertEqualsAmount("0", DownPaymentFees.refundAmount(mockRefundLoan(List.of(), List.of(chargedFee(JAN_1, 25))),
                BigDecimal.valueOf(100), JAN_2, BigDecimal.ZERO));
        assertEqualsAmount("0", DownPaymentFees.refundAmount(mockRefundLoan(List.of(disbursement(JAN_1, 1000)), List.of()),
                BigDecimal.valueOf(100), JAN_2, BigDecimal.ZERO));
    }

    @Test
    public void testBackdatedRefundIsCalculatedBeforeTheLaterRefund() {
        // UC27: MIR 1000 on Jan 3 refunded the whole fee, then MIR 400 is backdated to Jan 2
        final LoanTransaction laterRefund = refund(5L, JAN_3, 1000);
        final LoanTransaction backdatedRefund = refund(null, JAN_2, 400);
        final List<LoanTransaction> transactions = new ArrayList<>(List.of(disbursement(JAN_1, 1000), laterRefund));
        final Loan loan = mockRefundLoan(transactions, List.of(chargedFee(JAN_1, 25)));

        assertEqualsAmount("10", DownPaymentFees.refundAmount(loan, backdatedRefund));
        transactions.add(backdatedRefund);
        assertEqualsAmount("15", DownPaymentFees.refundAmount(loan, laterRefund));
    }

    @Test
    public void testRefundsOnTheSameDateUseUpTheCapInReplayOrder() {
        final LoanTransaction first = refund(5L, JAN_2, 600);
        final LoanTransaction second = refund(6L, JAN_2, 600);
        final Loan loan = mockRefundLoan(List.of(disbursement(JAN_1, 1000), second, first), List.of(chargedFee(JAN_1, 25)));

        assertEqualsAmount("15", DownPaymentFees.refundAmount(loan, first));
        assertEqualsAmount("10", DownPaymentFees.refundAmount(loan, second));
    }

    @Test
    public void testRefundWithoutFeeRefundUsesUpTheCap() {
        // its Fee refund is missing now, but it is created on the next replay
        final LoanTransaction refundWithoutFeeRefund = refund(5L, JAN_2, 600);
        final Loan loan = mockRefundLoan(List.of(disbursement(JAN_1, 1000), refundWithoutFeeRefund), List.of(chargedFee(JAN_1, 25)));

        assertEqualsAmount("10", DownPaymentFees.refundAmount(loan, refund(6L, JAN_3, 600)));
    }

    @Test
    public void testReversedRefundAndRefundOfAnUnsupportedTypeDoNotUseUpTheCap() {
        final LoanTransaction reversedRefund = refund(5L, JAN_2, 1000);
        when(reversedRefund.isNotReversed()).thenReturn(false);
        final LoanTransaction payoutRefund = refund(6L, JAN_2, 1000);
        when(payoutRefund.getTypeOf()).thenReturn(LoanTransactionType.PAYOUT_REFUND);
        final Loan loan = mockRefundLoan(List.of(disbursement(JAN_1, 1000), reversedRefund, payoutRefund), List.of(chargedFee(JAN_1, 25)));

        assertEqualsAmount("15", DownPaymentFees.refundAmount(loan, refund(7L, JAN_3, 600)));
    }

    @Test
    public void testRefundsWithoutFeeRefundAreTheSupportedRefundsWithoutANonReversedFeeRefund() {
        final LoanTransaction withFeeRefund = refund(5L, JAN_2, 100);
        final LoanTransaction withReversedFeeRefund = refund(6L, JAN_3, 100);
        final LoanTransaction reversedFeeRefund = feeRefundOf(withReversedFeeRefund);
        when(reversedFeeRefund.isNotReversed()).thenReturn(false);
        final LoanTransaction withoutFeeRefund = refund(7L, JAN_2, 100);
        final LoanTransaction unsavedWithFeeRefund = refund(null, JAN_3, 100);
        final LoanTransaction reversedRefund = refund(8L, JAN_2, 100);
        when(reversedRefund.isNotReversed()).thenReturn(false);
        final Loan loan = mockRefundLoan(
                List.of(disbursement(JAN_1, 1000), withReversedFeeRefund, reversedFeeRefund, withFeeRefund, feeRefundOf(withFeeRefund),
                        withoutFeeRefund, reversedRefund, unsavedWithFeeRefund, feeRefundOf(unsavedWithFeeRefund)),
                List.of(chargedFee(JAN_1, 25)));

        assertEquals(List.of(withoutFeeRefund, withReversedFeeRefund), DownPaymentFees.refundsWithoutFeeRefund(loan));
    }

    private static void assertEqualsAmount(final String expected, final BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    private static Loan mockRefundLoan(final List<LoanTransaction> transactions, final List<LoanCharge> charges) {
        final Loan loan = mock(Loan.class);
        lenient().when(loan.getCurrency()).thenReturn(new MonetaryCurrency("EUR", 2, null));
        when(loan.getLoanTransactions()).thenReturn(transactions);
        lenient().when(loan.getLoanCharges()).thenReturn(new LinkedHashSet<>(charges));
        lenient().when(loan.getSupportedFeeRefundTransactionTypes()).thenReturn(List.of(LoanTransactionType.MERCHANT_ISSUED_REFUND));
        return loan;
    }

    private static LoanTransaction disbursement(final LocalDate date, final long amount) {
        final LoanTransaction transaction = mock(LoanTransaction.class);
        lenient().when(transaction.isNotReversed()).thenReturn(true);
        lenient().when(transaction.isDisbursement()).thenReturn(true);
        lenient().when(transaction.getTypeOf()).thenReturn(LoanTransactionType.DISBURSEMENT);
        lenient().when(transaction.getTransactionDate()).thenReturn(date);
        lenient().when(transaction.getAmount()).thenReturn(BigDecimal.valueOf(amount));
        return transaction;
    }

    private static LoanTransaction refund(final Long id, final LocalDate date, final long amount) {
        final LoanTransaction refund = mock(LoanTransaction.class);
        lenient().when(refund.getId()).thenReturn(id);
        lenient().when(refund.isNotReversed()).thenReturn(true);
        lenient().when(refund.getTypeOf()).thenReturn(LoanTransactionType.MERCHANT_ISSUED_REFUND);
        lenient().when(refund.getTransactionDate()).thenReturn(date);
        lenient().when(refund.getAmount()).thenReturn(BigDecimal.valueOf(amount));
        return refund;
    }

    private static LoanTransaction feeRefundOf(final LoanTransaction refund) {
        final LoanTransactionRelation relation = mock(LoanTransactionRelation.class);
        lenient().when(relation.getRelationType()).thenReturn(LoanTransactionRelationTypeEnum.RELATED);
        lenient().when(relation.getToTransaction()).thenReturn(refund);
        final LoanTransaction feeRefund = mock(LoanTransaction.class);
        lenient().when(feeRefund.isNotReversed()).thenReturn(true);
        lenient().when(feeRefund.isFeeRefund()).thenReturn(true);
        lenient().when(feeRefund.getTypeOf()).thenReturn(LoanTransactionType.FEE_REFUND);
        lenient().when(feeRefund.getLoanTransactionRelations(any())).thenReturn(List.of(relation));
        return feeRefund;
    }

    private static LoanCharge chargedFee(final LocalDate dueDate, final long amount) {
        return chargedFee(dueDate, String.valueOf(amount));
    }

    private static LoanCharge chargedFee(final LocalDate dueDate, final String amount) {
        final LoanCharge fee = mock(LoanCharge.class);
        lenient().when(fee.isActive()).thenReturn(true);
        lenient().when(fee.isDueAtDownPayment()).thenReturn(true);
        lenient().when(fee.getDueLocalDate()).thenReturn(dueDate);
        lenient().when(fee.amount()).thenReturn(new BigDecimal(amount));
        return fee;
    }

    private static LoanCharge mockFee(final Long id, final Charge definition, final LocalDate dueDate, final Loan loan,
            final LoanDisbursementDetails tranche) {
        final LoanCharge fee = mockFee(id, definition, dueDate);
        when(fee.getLoan()).thenReturn(loan);
        when(fee.getDisbursementDetail()).thenReturn(tranche);
        when(fee.isDownPaymentFeeOf(same(tranche))).thenReturn(true);
        return fee;
    }

    private static Charge mockDefinition(final Long id) {
        final Charge charge = mock(Charge.class);
        when(charge.getId()).thenReturn(id);
        return charge;
    }

    private static LoanCharge mockFee(final Long id, final Charge definition, final LocalDate dueDate) {
        final LoanCharge fee = mock(LoanCharge.class);
        when(fee.getId()).thenReturn(id);
        when(fee.getCharge()).thenReturn(definition);
        when(fee.getDueLocalDate()).thenReturn(dueDate);
        when(fee.isActive()).thenReturn(true);
        when(fee.isDueAtDownPayment()).thenReturn(true);
        return fee;
    }

    private static LoanRepaymentScheduleInstallment mockPeriod(final Integer number, final LocalDate dueDate, final boolean downPayment) {
        final LoanRepaymentScheduleInstallment period = mock(LoanRepaymentScheduleInstallment.class);
        when(period.getInstallmentNumber()).thenReturn(number);
        when(period.getDueDate()).thenReturn(dueDate);
        when(period.isDownPayment()).thenReturn(downPayment);
        return period;
    }
}
