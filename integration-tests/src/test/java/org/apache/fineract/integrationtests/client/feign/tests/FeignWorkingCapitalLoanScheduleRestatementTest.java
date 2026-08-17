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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The projected schedule is restated from what the borrower actually owes rather than from the instalments the plan
 * assumed would arrive. These tests pin what follows from that: a loan paid to schedule is projected exactly as it was
 * at disbursement and closes on nothing at all, a loan running late has its remaining periods re-projected from its
 * real position, a rate change only ever re-rates what is genuinely left to bill, and COB is what tells the schedule
 * that a day went by unpaid.
 *
 * <p>
 * A period that elapsed unpaid counts as settled just as firmly as one that was paid: it says the instalment was not
 * collected. So COB, which fills the elapsed dates in with nil payments, re-bases the projection exactly as a payment
 * does. The consequence is that the schedule keeps pace with the calendar — every missed instalment pushes the last
 * period out by a day — and a loan left unpaid indefinitely always shows the same days remaining, never a plan that
 * quietly paid itself off on its original last day.
 *
 * <p>
 * A delinquent loan therefore bills more across its schedule than it owes, by the arrears: the missed money is
 * re-presented on later dates. That is why {@code assertBillsWhatIsOwed} is only applied to loans with nothing missed.
 *
 * <p>
 * The loan is deliberately tiny — 1000 net, 100 discount, 18% of a 100000 payment volume over 360 days, so 50 a day
 * over a 22 day term — which makes the whole schedule small enough to read in a failure message.
 */
public class FeignWorkingCapitalLoanScheduleRestatementTest extends FeignIntegrationTest {

    private static final BigDecimal PRINCIPAL = new BigDecimal("1000");
    private static final BigDecimal DISCOUNT = new BigDecimal("100");
    /** Everything the borrower ever owes: the net disbursement plus the discount fee. */
    private static final BigDecimal TOTAL_OWED = new BigDecimal("1100");
    private static final BigDecimal ORIGINAL_RATE = new BigDecimal("18");
    private static final BigDecimal RAISED_RATE = new BigDecimal("20");
    /** (100000 × 18%) / 360 — the daily payment the base schedule bills. */
    private static final BigDecimal DAILY_PAYMENT = new BigDecimal("50");
    /** (100000 × 20%) / 360 — what a period bills once the raised rate takes effect. */
    private static final BigDecimal RAISED_DAILY_PAYMENT = new BigDecimal("55.56");
    /** Rounding slack: each period is settled to the currency scale, so totals can land a few cents out. */
    private static final BigDecimal TOLERANCE = new BigDecimal("0.05");

    private static final String DISBURSEMENT_DATE = "01 January 2026";
    private static final int BASE_TERM = 22;

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    /**
     * The safety property the whole restatement rests on: while the borrower pays what was asked when it was asked,
     * reality and the plan agree, so the projection must come back byte for byte as it was at disbursement. Anything
     * that moves here is a regression for every healthy loan on the book.
     */
    @Test
    void testRepaymentsOnScheduleLeaveTheProjectionUntouched() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();
            final ProjectedAmortizationScheduleData atDisbursement = wcLoanHelper.getAmortizationSchedule(loanId);

            for (int dayOfMonth = 2; dayOfMonth <= 6; dayOfMonth++) {
                businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-0" + dayOfMonth);
                wcLoanHelper.makeRepayment(loanId,
                        WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "0" + dayOfMonth + " January 2026"));
            }

            final ProjectedAmortizationScheduleData afterPaying = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEquals(periods(atDisbursement).size(), periods(afterPaying).size(), "paying to schedule must not change its length");
            assertProjectionMatchesThrough(atDisbursement, afterPaying, periods(atDisbursement).size(), "after five on-time repayments");
        });
    }

    /**
     * Two days elapse with nothing paid and the third is settled. Nothing was collected on the first two, so the
     * projection makes no progress across them and each repeats the balance the loan actually carried; only the
     * instalment that really arrived moves it on. The loan is two instalments behind, so it runs two days later than
     * planned.
     */
    @Test
    void testRepaymentRestatesTheProjectionFromWhatIsReallyOwed() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();
            final ProjectedAmortizationScheduleData atDisbursement = wcLoanHelper.getAmortizationSchedule(loanId);

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "04 January 2026"));
            final ProjectedAmortizationScheduleData restated = wcLoanHelper.getAmortizationSchedule(loanId);

            // The first period is the only one nothing can have changed: it is projected from the opening balance,
            // which is also what the borrower still owed.
            assertProjectionMatchesThrough(atDisbursement, restated, 1, "the first period is projected from the opening balance");

            // 2 and 3 January went by with nothing collected, so neither made any progress on the debt.
            assertCloseTo(balanceOf(atDisbursement, 1), balanceOf(restated, 2), "a period that collected nothing cannot pay the loan down");
            assertCloseTo(balanceOf(atDisbursement, 1), balanceOf(restated, 3), "a period that collected nothing cannot pay the loan down");

            // The 4 January instalment did arrive, so from there the loan stands exactly one instalment along.
            assertCloseTo(balanceOf(atDisbursement, 2), balanceOf(restated, 4),
                    "the period after the payment must stand one instalment on");
            assertCloseTo(feeBalanceOf(atDisbursement, 2), feeBalanceOf(restated, 4),
                    "the fee must have been earned by exactly the one instalment collected");

            assertTrue(periods(restated).size() > BASE_TERM, "two missed instalments must push the schedule out");
            assertScheduleClosesCleanly(restated, "after a repayment two days late");
            assertNoNegativeAmounts(restated, "after a repayment two days late");
        });
    }

    /**
     * A rate change re-rates what is still outstanding and nothing more: the periods before it keep the rate they were
     * billed at, and the new one takes effect on its own date.
     *
     * <p>
     * The total billed is not checked after the change. Thirteen instalments went by unpaid on the way to 15 January,
     * so the schedule legitimately re-presents that money later and bills more than the loan owes.
     */
    @Test
    void testInTermRateChangeBillsOnlyWhatIsLeftToBill() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();
            assertBillsWhatIsOwed(wcLoanHelper.getAmortizationSchedule(loanId), "before any rate change");

            // 15 January is period 14 of a 22 day term, so the change lands with eight periods still to run.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(RAISED_RATE, "15 January 2026"));

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEquals(0, DAILY_PAYMENT.compareTo(paymentOf(schedule, 13)), "the day before must still bill the original rate");
            assertEquals(0, RAISED_DAILY_PAYMENT.compareTo(paymentOf(schedule, 14)), "the new rate must take effect on its own date");
            assertScheduleClosesCleanly(schedule, "after an in-term rate change");
            assertNoNegativeAmounts(schedule, "after an in-term rate change");
        });
    }

    /**
     * A repayment made after a rate change. The rate change rewrote the plan but moved no money, so the actual balance
     * carries straight through it and the repayment comes off what the borrower really owed — not off the much smaller
     * figure the plan had assumed by then. The periods after the repayment are then re-projected from that real
     * balance.
     */
    @Test
    void testRepaymentAfterRateChangeComesOffTheRealBalance() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "04 January 2026"));
            final BigDecimal owedBeforeRateChange = actualBalanceOf(wcLoanHelper.getAmortizationSchedule(loanId), 3);

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(RAISED_RATE, "20 January 2026"));

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-21");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(RAISED_DAILY_PAYMENT, "21 January 2026"));

            // Read once at the end: a period only reports an actual balance after the current date has passed it, so
            // 20 January has nothing to say until the 21st has been recorded.
            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertCloseTo(owedBeforeRateChange, actualBalanceOf(schedule, 19),
                    "a rate change moves no money, so the actual balance must carry through it");
            final ProjectedAmortizationSchedulePaymentData paidPeriod = period(schedule, 20);
            // What is left is what was owed, less the money handed over, plus the fee that payment earned back onto it.
            final BigDecimal expected = owedBeforeRateChange.subtract(RAISED_DAILY_PAYMENT).add(paidPeriod.getActualAmortizationAmount());
            assertCloseTo(expected, paidPeriod.getActualBalance(), "the repayment must come off the balance really outstanding");

            assertScheduleClosesCleanly(schedule, "after a repayment following a rate change");
        });
    }

    @Test
    void testPayingEveryInstalmentClosesTheLoanAtExactlyZero() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();
            final ProjectedAmortizationScheduleData atDisbursement = wcLoanHelper.getAmortizationSchedule(loanId);

            // Period N falls on the disbursement date plus N days, so the 22 period term runs 02..23 January.
            for (int dayOfMonth = 2; dayOfMonth <= 23; dayOfMonth++) {
                businessDateHelper.updateBusinessDate("BUSINESS_DATE", String.format("2026-01-%02d", dayOfMonth));
                wcLoanHelper.makeRepayment(loanId,
                        WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, String.format("%02d January 2026", dayOfMonth)));
            }

            final ProjectedAmortizationScheduleData paidOff = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEquals(BASE_TERM, periods(paidOff).size(), "paying every instalment on time must not change the term");
            assertProjectionMatchesThrough(atDisbursement, paidOff, BASE_TERM, "after paying the loan off to schedule");
            assertNoNegativeAmounts(paidOff, "after paying the loan off to schedule");

            final ProjectedAmortizationSchedulePaymentData last = periods(paidOff).getLast();
            assertNotNull(last.getExpectedBalance());
            assertNotNull(last.getExpectedDiscountFeeBalance());
            assertNotNull(last.getActualBalance());
            assertNotNull(last.getActualDiscountFeeBalance());
            assertEquals(0, last.getExpectedBalance().signum(), "the plan must end owing nothing\n" + render(paidOff));
            assertEquals(0, last.getExpectedDiscountFeeBalance().signum(), "the fee must be exactly earned\n" + render(paidOff));
            assertEquals(0, last.getActualBalance().signum(), "the borrower must end owing nothing\n" + render(paidOff));
            assertEquals(0, last.getActualDiscountFeeBalance().signum(),
                    "the payments must have earned exactly the fee\n" + render(paidOff));

            BigDecimal earned = BigDecimal.ZERO;
            for (final ProjectedAmortizationSchedulePaymentData period : periods(paidOff)) {
                if (period.getActualAmortizationAmount() != null) {
                    earned = earned.add(period.getActualAmortizationAmount());
                }
            }
            assertEquals(0, DISCOUNT.compareTo(earned),
                    "the amortization must sum to exactly the " + DISCOUNT + " fee, but summed to " + earned + "\n" + render(paidOff));
        });
    }

    /**
     * COB is what tells the schedule that a day went by without a payment. Until it runs, a period that has fallen due
     * has nothing recorded against it at all; afterwards every elapsed instalment date reads as a nil payment, the
     * actual balance stands exactly where it did because no money moved, and the days those instalments should have
     * covered are pushed onto the end of the schedule.
     */
    @Test
    void testCobRecordsElapsedInstalmentsAsMissed() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();

            final ProjectedAmortizationScheduleData beforeCob = wcLoanHelper.getAmortizationSchedule(loanId);
            assertNull(period(beforeCob, 1).getActualPaymentAmount(),
                    "nothing is known about a period until its day has passed\n" + render(beforeCob));

            // Five days go by and the borrower pays nothing at all.
            final LocalDate cobDate = LocalDate.of(2026, 1, 6);
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-06");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final ProjectedAmortizationScheduleData afterCob = wcLoanHelper.getAmortizationSchedule(loanId);
            int elapsed = 0;
            for (final ProjectedAmortizationSchedulePaymentData period : periods(afterCob)) {
                assertNotNull(period.getPaymentDate());
                if (period.getPaymentDate().isBefore(cobDate)) {
                    elapsed++;
                    assertTrue(period.getActualPaymentAmount() != null && period.getActualPaymentAmount().signum() == 0,
                            "period " + period.getPaymentNo() + " fell due unpaid and must read as a nil payment, but was "
                                    + period.getActualPaymentAmount() + "\n" + render(afterCob));
                    assertCloseTo(TOTAL_OWED.subtract(DISCOUNT), period.getActualBalance(),
                            "period " + period.getPaymentNo() + " collected nothing, so the balance must not have moved");
                } else {
                    assertNull(period.getActualPaymentAmount(),
                            "period " + period.getPaymentNo() + " has not fallen due yet and must report nothing\n" + render(afterCob));
                }
            }
            assertTrue(elapsed > 0, "the business date moved past several instalments, so some must have elapsed");
            assertEquals(BASE_TERM + elapsed, periods(afterCob).size(),
                    "each missed instalment must push the schedule out by a day\n" + render(afterCob));

            assertScheduleClosesCleanly(afterCob, "after COB acknowledged five missed instalments");
            assertNoNegativeAmounts(afterCob, "after COB acknowledged five missed instalments");
        });
    }

    /**
     * The worst loan on the book: disbursed, never paid a penny, and now past the day it was due to mature. Its
     * schedule is exactly what someone chasing the debt needs to see, so COB carrying the date past the final period
     * must not take it away.
     *
     * <p>
     * It very nearly did. Trailing periods with no present value are dropped as periods the loan will never see, and
     * once every period counts as elapsed-and-unpaid the whole schedule qualified — collapsing to the disbursement row
     * alone. A loan with even one payment was never affected, which is why this needs a test of its own.
     */
    @Test
    void testCobPastMaturityKeepsTheScheduleOfALoanThatNeverPaid() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();

            // Well past 23 January, the last period of the original term.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-15");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final ProjectedAmortizationScheduleData afterCob = wcLoanHelper.getAmortizationSchedule(loanId);
            assertTrue(periods(afterCob).size() >= BASE_TERM, "a loan that never paid still owes every instalment, so its schedule must "
                    + "survive passing maturity, but only " + periods(afterCob).size() + " periods remain\n" + render(afterCob));
            assertScheduleClosesCleanly(afterCob, "after COB carried a never-paid loan past maturity");
            assertNoNegativeAmounts(afterCob, "after COB carried a never-paid loan past maturity");
        });
    }

    /**
     * A rate change re-rates the periods still to run, so one dated past the last of them has nothing to apply to. The
     * request is refused rather than recorded in the rate history and then quietly ignored by the schedule.
     *
     * <p>
     * Reaching this needs a date far beyond the loan's own life, because every missed instalment pushes maturity out by
     * a day: merely being overdue never puts the loan past its own end.
     */
    @Test
    void testRateChangeEffectiveAfterMaturityIsRejected() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();
            final ProjectedAmortizationScheduleData beforeAttempt = wcLoanHelper.getAmortizationSchedule(loanId);

            final CallFailedRuntimeException failure = wcLoanHelper.updateRateExpectingError(loanId,
                    WorkingCapitalLoanRequestBuilders.updateRate(RAISED_RATE, "01 January 2030"));

            assertEquals(400, failure.getStatus(), "a rate change past maturity must be a validation error");
            assertTrue(failure.getDeveloperMessage().contains("cannot.be.after.maturity.date"),
                    "the error must name the maturity rule, but was: " + failure.getDeveloperMessage());

            // Refused means refused: the schedule is exactly as it was.
            final ProjectedAmortizationScheduleData afterAttempt = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEquals(periods(beforeAttempt).size(), periods(afterAttempt).size(), "a rejected rate change must not touch the schedule");
            assertProjectionMatchesThrough(beforeAttempt, afterAttempt, periods(beforeAttempt).size(), "after a rejected rate change");
        });
    }

    /**
     * The mirror of the case above: the same loan, left unpaid well past the day it was written to mature, still takes
     * a rate change. Maturity travels with the missed instalments, so an overdue loan is never past its own end.
     */
    @Test
    void testRateChangeIsStillAcceptedOnALoanLeftUnpaidPastItsOriginalMaturity() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = disburseLoan();

            // 15 February is three weeks past 23 January, the last period the loan was written with.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-15");
            wcLoanHelper.executeInlineWCCOB(loanId);
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(RAISED_RATE, "15 February 2026"));

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertTrue(periods(schedule).size() > BASE_TERM, "the schedule must have grown past its original term\n" + render(schedule));
            assertScheduleClosesCleanly(schedule, "after re-rating a loan left unpaid past its original maturity");
            assertNoNegativeAmounts(schedule, "after re-rating a loan left unpaid past its original maturity");
        });
    }

    /**
     * No column of a repayment period may go negative. Balances, deferred fee and amortization are all amounts of money
     * owed, held or earned, and none of those has a negative reading in Fineract. The disbursement row is excluded: it
     * carries the money going out, so its payment and present value are negative by design.
     */
    private void assertNoNegativeAmounts(final ProjectedAmortizationScheduleData schedule, final String context) {
        for (final ProjectedAmortizationSchedulePaymentData period : periods(schedule)) {
            assertNotNegative(period, "expected payment", period.getExpectedPaymentAmount(), schedule, context);
            assertNotNegative(period, "actual payment", period.getActualPaymentAmount(), schedule, context);
            assertNotNegative(period, "expected balance", period.getExpectedBalance(), schedule, context);
            assertNotNegative(period, "actual balance", period.getActualBalance(), schedule, context);
            assertNotNegative(period, "expected amortization", period.getExpectedAmortizationAmount(), schedule, context);
            assertNotNegative(period, "actual amortization", period.getActualAmortizationAmount(), schedule, context);
            assertNotNegative(period, "expected discount fee balance", period.getExpectedDiscountFeeBalance(), schedule, context);
            assertNotNegative(period, "actual discount fee balance", period.getActualDiscountFeeBalance(), schedule, context);
        }
    }

    private static void assertNotNegative(final ProjectedAmortizationSchedulePaymentData period, final String column,
            final BigDecimal value, final ProjectedAmortizationScheduleData schedule, final String context) {
        assertTrue(value == null || value.signum() >= 0,
                "period " + period.getPaymentNo() + " has a negative " + column + " of " + value + ' ' + context + "\n" + render(schedule));
    }

    /**
     * Everything the schedule plans to bill adds up to the debt. Only meaningful while no instalment has been missed:
     * once periods fall due unpaid the schedule re-presents that money on later dates, and the total legitimately
     * exceeds the debt by the arrears.
     */
    private void assertBillsWhatIsOwed(final ProjectedAmortizationScheduleData schedule, final String context) {
        BigDecimal billed = BigDecimal.ZERO;
        for (final ProjectedAmortizationSchedulePaymentData period : periods(schedule)) {
            if (period.getExpectedPaymentAmount() != null) {
                billed = billed.add(period.getExpectedPaymentAmount());
            }
        }
        assertTrue(TOTAL_OWED.subtract(billed).abs().compareTo(TOLERANCE) <= 0, "the schedule must bill the " + TOTAL_OWED + " owed "
                + context + ", but billed " + billed + " over " + periods(schedule).size() + " periods\n" + render(schedule));
    }

    /**
     * The schedule runs out exactly when the debt does: the last period clears the balance and the deferred fee
     * together, and nothing is billed beyond that. A period billing against a balance already at zero is asking for
     * money with nothing standing behind it.
     */
    private void assertScheduleClosesCleanly(final ProjectedAmortizationScheduleData schedule, final String context) {
        final List<ProjectedAmortizationSchedulePaymentData> periods = periods(schedule);
        boolean cleared = false;
        for (final ProjectedAmortizationSchedulePaymentData period : periods) {
            if (cleared) {
                final BigDecimal billed = period.getExpectedPaymentAmount();
                assertTrue(billed == null || billed.signum() == 0, "period " + period.getPaymentNo() + " bills " + billed
                        + " although the balance already reached zero " + context + "\n" + render(schedule));
            }
            cleared = cleared || (period.getExpectedBalance() != null && period.getExpectedBalance().signum() == 0);
        }
        final ProjectedAmortizationSchedulePaymentData last = periods.getLast();
        assertEquals(0, last.getExpectedBalance().signum(),
                "the schedule must end on a cleared balance " + context + "\n" + render(schedule));
        assertEquals(0, last.getExpectedDiscountFeeBalance().signum(),
                "the fee must be fully earned by the time the balance clears " + context + "\n" + render(schedule));
    }

    /** Every projected column of periods 1..{@code throughPeriod} must be identical in both schedules. */
    private void assertProjectionMatchesThrough(final ProjectedAmortizationScheduleData before,
            final ProjectedAmortizationScheduleData after, final int throughPeriod, final String context) {
        for (int periodNo = 1; periodNo <= throughPeriod; periodNo++) {
            final ProjectedAmortizationSchedulePaymentData expected = period(before, periodNo);
            final ProjectedAmortizationSchedulePaymentData actual = period(after, periodNo);
            assertEquals(0, expected.getExpectedPaymentAmount().compareTo(actual.getExpectedPaymentAmount()),
                    "period " + periodNo + " payment must not move " + context + "\n" + render(after));
            assertEquals(0, expected.getExpectedBalance().compareTo(actual.getExpectedBalance()),
                    "period " + periodNo + " balance must not move " + context + "\n" + render(after));
            assertEquals(0, expected.getExpectedDiscountFeeBalance().compareTo(actual.getExpectedDiscountFeeBalance()),
                    "period " + periodNo + " fee balance must not move " + context + "\n" + render(after));
        }
    }

    private static void assertCloseTo(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertTrue(actual != null && expected.subtract(actual).abs().compareTo(TOLERANCE) <= 0,
                message + " — expected " + expected + " but was " + actual);
    }

    private static ProjectedAmortizationSchedulePaymentData period(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return periods(schedule).stream().filter(payment -> payment.getPaymentNo() == periodNo).findFirst()
                .orElseThrow(() -> new AssertionError("no period " + periodNo + " in\n" + render(schedule)));
    }

    private static BigDecimal balanceOf(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return period(schedule, periodNo).getExpectedBalance();
    }

    private static BigDecimal feeBalanceOf(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return period(schedule, periodNo).getExpectedDiscountFeeBalance();
    }

    private static BigDecimal paymentOf(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return period(schedule, periodNo).getExpectedPaymentAmount();
    }

    private static BigDecimal actualBalanceOf(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return period(schedule, periodNo).getActualBalance();
    }

    /** Repayment periods only — the disbursement row carries a negative amount and the opening balance. */
    private static List<ProjectedAmortizationSchedulePaymentData> periods(final ProjectedAmortizationScheduleData schedule) {
        return schedule.getPayments().stream().filter(payment -> payment.getPaymentNo() != null && payment.getPaymentNo() > 0).toList();
    }

    private static String render(final ProjectedAmortizationScheduleData schedule) {
        final StringBuilder out = new StringBuilder("schedule:\n");
        for (final ProjectedAmortizationSchedulePaymentData payment : schedule.getPayments()) {
            out.append("  #").append(payment.getPaymentNo()).append(' ').append(payment.getPaymentDate()).append(" payment=")
                    .append(payment.getExpectedPaymentAmount()).append(" balance=").append(payment.getExpectedBalance())
                    .append(" discountFeeBalance=").append(payment.getExpectedDiscountFeeBalance()).append(" actualPayment=")
                    .append(payment.getActualPaymentAmount()).append(" actualBalance=").append(payment.getActualBalance()).append('\n');
        }
        return out.toString();
    }

    private Long disburseLoan() {
        final Long clientForTest = clientHelper.createClient(DISBURSEMENT_DATE);
        final Long productId = createProductAllowingDiscount();
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders
                .submitApplication(clientForTest, productId, PRINCIPAL, ORIGINAL_RATE, DISBURSEMENT_DATE, DISBURSEMENT_DATE)
                .discount(DISCOUNT));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId,
                WorkingCapitalLoanRequestBuilders.approveWithDiscount(DISBURSEMENT_DATE, PRINCIPAL, DISBURSEMENT_DATE, DISCOUNT));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount(DISBURSEMENT_DATE, PRINCIPAL, DISCOUNT));
        return loanId;
    }

    private Long createProductAllowingDiscount() {
        final Long productId = productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL Restate " + UUID.randomUUID().toString().substring(0, 8))
                        .withShortName(UUID.randomUUID().toString().replace("-", "").substring(0, 4))
                        .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
