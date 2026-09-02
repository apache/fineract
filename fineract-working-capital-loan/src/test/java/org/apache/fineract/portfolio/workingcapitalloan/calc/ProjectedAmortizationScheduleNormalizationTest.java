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
package org.apache.fineract.portfolio.workingcapitalloan.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The schedule's arithmetic held to its own promises, rather than to a recorded set of figures.
 *
 * <p>
 * Every test here is a property the schedule must satisfy for any inputs, so none of them needs updating when a cent
 * moves. They are what says the model is still correct; the golden schedules in
 * {@link ProjectedAmortizationScheduleCalculatorTest} say only that it has not changed.
 */
class ProjectedAmortizationScheduleNormalizationTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("USD", 2, null);
    private static final BigDecimal TPV = new BigDecimal("100000");
    private static final int DAY_COUNT = 360;
    private static final LocalDate DISBURSEMENT = LocalDate.of(2026, 1, 1);
    private static final BigDecimal CENT = new BigDecimal("0.01");

    @BeforeEach
    void setBusinessDate() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, DISBURSEMENT)));
    }

    @AfterEach
    void resetContext() {
        ThreadLocalContextUtil.reset();
    }

    private ProjectedAmortizationScheduleModel model(final String netDisbursement, final String discountFee, final String rate) {
        return ProjectedAmortizationScheduleModel.generate(new BigDecimal(discountFee), new BigDecimal(netDisbursement), TPV,
                new BigDecimal(rate), DAY_COUNT, DISBURSEMENT, MC, CURRENCY, DISBURSEMENT);
    }

    private static BigDecimal amount(final Money money) {
        return money == null ? null : money.getAmount();
    }

    /**
     * Every amount the schedule reports is payable in the loan currency. A figure carrying a third decimal is one no
     * borrower can pay and no ledger can hold, and it is where a cent goes missing.
     */
    @Test
    void everyReportedAmountIsAWholeNumberOfMinorUnits() {
        for (final String rate : new String[] { "18", "17", "13", "7", "1.5" }) {
            final ProjectedAmortizationScheduleModel model = model("9000", "1000", rate);
            for (final ProjectedPayment payment : model.projectedPayments()) {
                final String where = "rate " + rate + ", period " + payment.paymentNo() + ": ";
                assertScaled(where + "expectedPaymentAmount", payment.expectedPaymentAmount());
                assertScaled(where + "expectedBalance", payment.expectedBalance());
                assertScaled(where + "expectedAmortizationAmount", payment.expectedAmortizationAmount());
                assertScaled(where + "expectedDiscountFeeBalance", payment.expectedDiscountFeeBalance());
                assertScaled(where + "actualBalance", payment.actualBalance());
                assertScaled(where + "actualAmortizationAmount", payment.actualAmortizationAmount());
                assertScaled(where + "actualDiscountFeeBalance", payment.actualDiscountFeeBalance());
            }
        }
    }

    private static void assertScaled(final String where, final Money money) {
        if (money == null) {
            return;
        }
        final BigDecimal value = money.getAmount();
        assertEquals(0, value.compareTo(value.setScale(2, RoundingMode.UNNECESSARY)), where + " must be payable in the currency: " + value);
    }

    /**
     * What the borrower really owes, and what the money received has really earned, are both ledgers - so both only
     * ever fall, and neither goes below nothing.
     *
     * <p>
     * A payment cannot un-earn what an earlier one booked, and money once handed over cannot become owed again. Both
     * hold unconditionally - across missed days, across restatements, and across a re-pricing - because both are
     * records of money that came in rather than projections of money expected. The projected columns are not ledgers
     * and are held to the weaker rule in
     * {@link #theProjectedDeferredFeeFallsWhileTheRateStandsStillAndNeverGoesNegative}: a re-pricing changes what one
     * day ahead looks like, so it moves them, whereas nothing but money moves these.
     *
     * <p>
     * The balance is the half of this that has gone wrong before: a rate change that re-priced the schedule from the
     * disbursement rather than from the balance a repayment had already brought it down to reported the loan owing
     * several hundred more the day after the change than the day before it, on a day no money moved at all.
     */
    @Test
    void whatIsOwedAndWhatIsEarnedAreLedgersSoBothOnlyEverFall() {
        for (final String rate : new String[] { "18", "17", "13" }) {
            for (final Divergence divergence : divergencesIncludingNone()) {
                final ProjectedAmortizationScheduleModel model = model("9000", "1000", rate);
                if (divergence != null) {
                    divergence.applyTo(model);
                }
                BigDecimal previousDeferredFee = null;
                BigDecimal previousOwed = null;
                for (final ProjectedPayment payment : model.projectedPayments()) {
                    final String where = "rate " + rate + ", " + divergence + ", period " + payment.paymentNo() + ": ";

                    final BigDecimal deferredFee = amount(payment.actualDiscountFeeBalance());
                    if (deferredFee != null) {
                        assertTrue(deferredFee.signum() >= 0, where + "actualDiscountFeeBalance went negative: " + deferredFee);
                        if (previousDeferredFee != null) {
                            assertTrue(deferredFee.compareTo(previousDeferredFee) <= 0,
                                    where + "actualDiscountFeeBalance rose from " + previousDeferredFee + " to " + deferredFee);
                        }
                        previousDeferredFee = deferredFee;
                    }

                    final BigDecimal owed = amount(payment.actualBalance());
                    if (owed != null) {
                        assertTrue(owed.signum() >= 0, where + "actualBalance went negative: " + owed);
                        if (previousOwed != null) {
                            assertTrue(owed.compareTo(previousOwed) <= 0,
                                    where + "actualBalance rose from " + previousOwed + " to " + owed);
                        }
                        previousOwed = owed;
                    }
                }
            }
        }
    }

    /**
     * The projected deferred fee falls for as long as the rate it is projected at stands still, and never goes below
     * nothing.
     *
     * <p>
     * Only for as long as the rate stands still, because the expected columns are not a ledger: each day projects one
     * day forward from what the borrower really owes, so the figure a day reports is a fresh projection rather than a
     * running total the next day adds to. Two days either side of a re-pricing are therefore two different projections
     * of the same position, and comparing them measures the rate change rather than anything the schedule did wrong - a
     * rate cut deducts a smaller day's accrual from the same balance and so necessarily projects more fee still
     * deferred. {@link #aRateCutRaisesTheProjectedDeferredFeeAndTheFeeStillClosesOnNothing} pins that as the intended
     * behaviour; here the day it takes effect on is the one day the comparison is not made.
     *
     * <p>
     * Everywhere else the projection is comparable, and there it must only ever fall.
     */
    @Test
    void theProjectedDeferredFeeFallsWhileTheRateStandsStillAndNeverGoesNegative() {
        for (final String rate : new String[] { "18", "17", "13" }) {
            for (final Divergence divergence : divergencesIncludingNone()) {
                final ProjectedAmortizationScheduleModel model = model("9000", "1000", rate);
                if (divergence != null) {
                    divergence.applyTo(model);
                }
                final Set<LocalDate> repricedOn = model.rateChanges().stream()
                        .map(ProjectedAmortizationScheduleModel.RateChange::effectiveDate).collect(Collectors.toSet());
                BigDecimal previous = null;
                for (final ProjectedPayment payment : model.projectedPayments()) {
                    final BigDecimal projected = amount(payment.expectedDiscountFeeBalance());
                    final String where = "rate " + rate + ", " + divergence + ", period " + payment.paymentNo() + ": ";
                    assertTrue(projected.signum() >= 0, where + "expectedDiscountFeeBalance went negative: " + projected);
                    if (previous != null && !repricedOn.contains(payment.date())) {
                        assertTrue(projected.compareTo(previous) <= 0, where + "expectedDiscountFeeBalance rose from " + previous + " to "
                                + projected + " on a day the rate did not change");
                    }
                    previous = projected;
                }
            }
        }
    }

    /**
     * A re-pricing moves no money, so the balance the schedule projects from does not move across it.
     *
     * <p>
     * This is the one the projected balance needs, and monotonicity is not it: a rate cut deducts a smaller instalment
     * from the same balance and so legitimately reports a higher figure the day it takes effect, which means any rule
     * about the direction of travel has to exempt exactly the day the damage would show. What cannot change is the
     * balance being projected from - a rate change rewrites what is still to come, not what the borrower owes - and
     * that is recoverable from any row as {@code balance - amortization + payment}.
     *
     * <p>
     * Re-pricing from the disbursement instead of from the balance a repayment had brought the loan down to is what
     * went wrong before, and it reported several hundred more owed on the day of the change than the day before it.
     * Measured this way that is a discontinuity in the balance projected from, whichever direction the figure moved.
     */
    @Test
    void aRePricingMovesNoMoneySoTheBalanceItProjectsFromDoesNotMove() {
        for (final String rate : new String[] { "18", "17", "13" }) {
            for (final Divergence divergence : divergencesIncludingNone()) {
                final ProjectedAmortizationScheduleModel model = model("9000", "1000", rate);
                if (divergence != null) {
                    divergence.applyTo(model);
                }
                final Set<LocalDate> repricedOn = model.rateChanges().stream()
                        .map(ProjectedAmortizationScheduleModel.RateChange::effectiveDate).collect(Collectors.toSet());
                ProjectedPayment previous = null;
                for (final ProjectedPayment payment : model.projectedPayments()) {
                    if (payment.paymentNo() == 0) {
                        continue;
                    }
                    // A payment landing on the day of the change moves the balance for a reason of its own.
                    final boolean moneyMoved = payment.actualPaymentAmount() != null && amount(payment.actualPaymentAmount()).signum() > 0;
                    if (previous != null && repricedOn.contains(payment.date()) && !moneyMoved) {
                        final BigDecimal projectsFrom = projectedFrom(payment);
                        final BigDecimal carriedIn = amount(previous.expectedBalance());
                        final BigDecimal previouslyProjectedFrom = projectedFrom(previous);
                        assertTrue(
                                projectsFrom.subtract(carriedIn).abs().compareTo(CENT) <= 0
                                        || projectsFrom.subtract(previouslyProjectedFrom).abs().compareTo(CENT) <= 0,
                                "rate " + rate + ", " + divergence + ", period " + payment.paymentNo() + ": the re-pricing projects from "
                                        + projectsFrom + ", but the day before it carried " + carriedIn + " and projected from "
                                        + previouslyProjectedFrom);
                    }
                    previous = payment;
                }
            }
        }
    }

    /** The balance a row was projected forward from, recovered from the row itself. */
    private static BigDecimal projectedFrom(final ProjectedPayment payment) {
        return amount(payment.expectedBalance()).subtract(amount(payment.expectedAmortizationAmount()))
                .add(amount(payment.expectedPaymentAmount()));
    }

    /**
     * A rate cut raises the fee the schedule projects as still deferred, and the fee still closes on nothing.
     *
     * <p>
     * Not a defect but the arithmetic of a one-day-ahead projection: the day before the cut deducts a day's accrual at
     * the old rate from the balance the borrower really owes, the day of the cut deducts a smaller one from the same
     * balance, and a smaller deduction leaves more deferred. Asserted rather than merely tolerated, so that a change
     * which quietly stopped re-pricing the projection would be caught here instead of passing as a tidier column.
     */
    @Test
    void aRateCutRaisesTheProjectedDeferredFeeAndTheFeeStillClosesOnNothing() {
        final LocalDate repricedOn = DISBURSEMENT.plusDays(9);
        for (final String netDisbursement : new String[] { "9000", "5000" }) {
            for (final String[] cut : new String[][] { { "25", "11" }, { "18", "11" }, { "25", "18" } }) {
                final ProjectedAmortizationScheduleModel model = model(netDisbursement, "1000", cut[0]);
                Divergence.A_PART_PAYMENT_AFTER_A_MISSED_WEEK.applyTo(model);
                model.applyRateChange(new BigDecimal(cut[1]), repricedOn, repricedOn);

                final String where = "net " + netDisbursement + ", rate " + cut[0] + " cut to " + cut[1] + ": ";
                final BigDecimal before = amount(feeBalanceOn(model, repricedOn.minusDays(1)));
                final BigDecimal onTheDay = amount(feeBalanceOn(model, repricedOn));
                assertTrue(onTheDay.compareTo(before) > 0,
                        where + "a rate cut must project more fee still deferred, not less: " + before + " -> " + onTheDay);

                final ProjectedPayment closing = model.projectedPayments().getLast();
                assertEquals(0, amount(closing.expectedDiscountFeeBalance()).signum(),
                        where + "the fee must still close on nothing: " + closing.expectedDiscountFeeBalance());
                assertEquals(0, amount(closing.expectedBalance()).signum(),
                        where + "the schedule must still close on nothing: " + closing.expectedBalance());
            }
        }
    }

    private static Money feeBalanceOn(final ProjectedAmortizationScheduleModel model, final LocalDate date) {
        return model.projectedPayments().stream().filter(payment -> payment.paymentNo() > 0 && payment.date().equals(date))
                .map(ProjectedPayment::expectedDiscountFeeBalance).findFirst().orElseThrow();
    }

    /**
     * The plain schedule alongside every way of diverging from it, so the seams reality is restated across - and the
     * one a re-pricing introduces - are all covered.
     */
    private static List<Divergence> divergencesIncludingNone() {
        final List<Divergence> all = new ArrayList<>();
        all.add(null);
        all.addAll(List.of(Divergence.values()));
        return all;
    }

    /**
     * The whole point of normalizing a running total rather than each day: the reported fee tracks the exact one to
     * within half a minor unit at every single day, so it cannot wander off over a few hundred of them. Summing days
     * rounded one at a time is what let it drift, and what left a loan a cent short.
     */
    @Test
    void reportedFeeNeverDriftsFromTheBalanceItIsCarriedOn() {
        for (final String rate : new String[] { "18", "17", "13" }) {
            final ProjectedAmortizationScheduleModel model = model("9000", "1000", rate);
            final BigDecimal fee = model.discountFeeAmount().getAmount();
            final BigDecimal netDisbursement = model.netDisbursementAmount().getAmount();
            BigDecimal reportedFee = BigDecimal.ZERO;
            for (final ProjectedPayment payment : model.projectedPayments()) {
                if (payment.paymentNo() == 0) {
                    continue;
                }
                final String where = "rate " + rate + ", period " + payment.paymentNo() + ": ";
                reportedFee = reportedFee.add(amount(payment.expectedAmortizationAmount()));
                // What the day says it has earned and what its deferred balance says it has left are the same figure
                // read from two ends, so they must agree exactly - the schedule cannot be short by a cent it has
                // already reported as booked.
                assertEquals(0, fee.subtract(reportedFee).compareTo(amount(payment.expectedDiscountFeeBalance())),
                        where + "the fee earned so far and the fee still deferred do not add up to the fee");
                // The balance the day closes on is the disbursement it has not billed away yet, plus the fee it has
                // booked against it.
                final BigDecimal billed = billedThrough(model, payment.paymentNo());
                assertEquals(0, netDisbursement.subtract(billed).add(reportedFee).compareTo(amount(payment.expectedBalance())),
                        where + "the balance does not equal disbursement less billed plus fee booked");
            }
            assertEquals(0, reportedFee.compareTo(fee),
                    "rate " + rate + ": the days must earn exactly the discount fee, got " + reportedFee);
        }
    }

    private static BigDecimal billedThrough(final ProjectedAmortizationScheduleModel model, final int paymentNo) {
        return model.projectedPayments().stream().filter(p -> p.paymentNo() > 0 && p.paymentNo() <= paymentNo)
                .map(p -> p.expectedPaymentAmount().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * A working capital loan earns its fee on money received, not on time passed. Repaying the lot on the first day
     * earns the lot of the fee on the first day, and the schedule is one day long - there is nothing left to schedule.
     */
    @Test
    void repayingInFullOnTheFirstDayEarnsTheWholeFeeThatDay() {
        final ProjectedAmortizationScheduleModel model = model("900", "100", "18");
        model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal("1000"));

        final List<ProjectedPayment> payments = model.projectedPayments();
        assertEquals(2, payments.size(), "disbursement row plus the single day the loan lasted");
        final ProjectedPayment onlyDay = payments.getLast();
        assertEquals(0, amount(onlyDay.actualAmortizationAmount()).compareTo(new BigDecimal("100.00")),
                "the whole fee is earned by the money that closed the loan");
        assertEquals(0, amount(onlyDay.actualDiscountFeeBalance()).signum(), "no fee is left deferred");
        assertEquals(0, amount(onlyDay.actualBalance()).signum(), "nothing is left owing");
    }

    /**
     * Paying double the instalment earns two days of fee and takes a day off the end. Paying nothing earns none and
     * adds one. Both fall out of the walk running until the loan is square rather than for a counted number of days.
     */
    @Test
    void payingAheadShortensTheScheduleAndPayingNothingLengthensIt() {
        final int onSchedule = lastPaymentNo(paidOnDayOne(null));
        final int paidDouble = lastPaymentNo(paidOnDayOne("100"));
        assertEquals(onSchedule - 1, paidDouble, "an extra instalment up front must take a day off the end");

        final ProjectedAmortizationScheduleModel missed = model("9000", "1000", "18");
        missed.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(2));
        assertEquals(onSchedule + 1, lastPaymentNo(missed), "a day gone by unpaid must add a day to the end");
    }

    private ProjectedAmortizationScheduleModel paidOnDayOne(final String amount) {
        final ProjectedAmortizationScheduleModel model = model("9000", "1000", "18");
        if (amount != null) {
            model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal(amount));
        }
        return model;
    }

    private static int lastPaymentNo(final ProjectedAmortizationScheduleModel model) {
        return model.projectedPayments().getLast().paymentNo();
    }

    /**
     * A loan mostly cleared by one large backdated payment used to strand two cents of the discount fee unearned, so
     * its closing day billed 3.99 against the 4.00 really owed and the deferred fee closed on 0.02.
     */
    @Test
    void aLargeBackdatedPaymentEarnsTheWholeFeeAndClosesTheSchedule() {
        final ProjectedAmortizationScheduleModel model = model("9000", "1000", "17");
        model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(2));
        model.applyPayment(DISBURSEMENT.plusDays(2), new BigDecimal("46"));
        model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(3));
        model.applyPayment(DISBURSEMENT.plusDays(3), new BigDecimal("46"));
        model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal("9904"));

        final List<ProjectedPayment> payments = model.projectedPayments();
        final ProjectedPayment closing = payments.getLast();

        assertEquals(0, closing.expectedPaymentAmount().getAmount().compareTo(new BigDecimal("4.00")),
                "the closing day must bill the 4.00 still outstanding");
        assertEquals(0, amount(closing.expectedDiscountFeeBalance()).signum(), "the expected deferred fee must close on nothing");

        final ProjectedPayment lastKnown = payments.stream().filter(p -> p.actualDiscountFeeBalance() != null).reduce((a, b) -> b)
                .orElseThrow();
        assertEquals(0, amount(lastKnown.actualDiscountFeeBalance()).signum(), "the actual deferred fee must close on nothing");
        assertEquals(0, model.totalActualAmortization().compareTo(new BigDecimal("1000.00")),
                "the payments received must earn the whole fee");

        // What the loan has taken in plus what it still expects to bill is the whole 10000 it is owed.
        BigDecimal collected = BigDecimal.ZERO;
        for (final ProjectedPayment payment : payments) {
            if (payment.paymentNo() > 0 && payment.actualPaymentAmount() != null) {
                collected = collected.add(payment.actualPaymentAmount().getAmount());
            }
        }
        assertEquals(0, collected.add(closing.expectedPaymentAmount().getAmount()).compareTo(new BigDecimal("10000.00")),
                "collected plus the closing bill must account for the whole payable");
    }

    /**
     * A rate change re-prices what is still to come; it does not reach back into fee the borrower has already earned.
     * So the days before it keep every figure they had, and the fee still closes on nothing after it.
     */
    @Test
    void aRateChangeLeavesEarnedFeeAloneAndStillClosesTheFee() {
        // 150 against a 47.22 instalment reaches into a fourth plan day without covering it. The fee earned is the part
        // of that day the money paid for, not the whole of it - reading the plan cursor where it stands rather than
        // where the money reached is what once let a rate change hand over the rest of the day for free.
        final ProjectedAmortizationScheduleModel model = model("9000", "1000", "17");
        model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(2));
        model.applyPayment(DISBURSEMENT.plusDays(2), new BigDecimal("150"));
        final BigDecimal earnedBefore = model.totalActualAmortization();
        assertTrue(earnedBefore.signum() > 0, "the payment must have earned some fee before the change");

        final LocalDate changeDate = DISBURSEMENT.plusDays(9);
        model.applyRateChange(new BigDecimal("13"), changeDate, changeDate);
        assertEquals(0, model.totalActualAmortization().compareTo(earnedBefore),
                "a rate change must not restate fee the borrower has already earned");

        final LocalDate secondChange = DISBURSEMENT.plusDays(40);
        model.applyRateChange(new BigDecimal("22"), secondChange, secondChange);
        assertEquals(0, model.totalActualAmortization().compareTo(earnedBefore), "nor must a second one");

        assertEquals(0, amount(model.projectedPayments().getLast().expectedDiscountFeeBalance()).signum(),
                "the fee must still close on nothing across two rate changes");
    }

    /**
     * However much a borrower hands over, no day reports a negative amount and the fee earned never passes the fee.
     * Paying the whole payable earns all of it; paying more than the payable earns nothing extra, because there is
     * nothing left to earn; paying nearly all of it earns nearly all of the fee and leaves the rest to the days still
     * to come.
     */
    @Test
    void largePaymentsNeverProduceNegativeAmountsAndNeverEarnMoreThanTheFee() {
        final BigDecimal fee = new BigDecimal("1000.00");
        final BigDecimal payable = new BigDecimal("10000");
        for (final String overpayment : new String[] { "9950", "10000", "10500", "12000" }) {
            final ProjectedAmortizationScheduleModel model = model("9000", "1000", "17");
            model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal(overpayment));
            for (final ProjectedPayment payment : model.projectedPayments()) {
                final String where = "overpaid " + overpayment + ", period " + payment.paymentNo() + ": ";
                assertNonNegative(where + "expectedPaymentAmount", payment.paymentNo() == 0 ? null : payment.expectedPaymentAmount());
                assertNonNegative(where + "expectedBalance", payment.expectedBalance());
                assertNonNegative(where + "expectedAmortizationAmount", payment.expectedAmortizationAmount());
                assertNonNegative(where + "expectedDiscountFeeBalance", payment.expectedDiscountFeeBalance());
                assertNonNegative(where + "actualAmortizationAmount", payment.actualAmortizationAmount());
                assertNonNegative(where + "actualDiscountFeeBalance", payment.actualDiscountFeeBalance());
                // A borrower who has handed over more than the payable owes nothing, not a negative amount: the excess
                // is held as overpayment on the loan. Every outstanding bucket on the loan is clamped at nothing, so a
                // negative here would have the schedule contradict the loan it describes.
                assertNonNegative(where + "actualBalance", payment.actualBalance());
            }
            if (new BigDecimal(overpayment).compareTo(payable) >= 0) {
                assertEquals(0, amount(lastKnownActualBalance(model)).signum(),
                        "paid " + overpayment + ": a loan paid at or beyond the payable owes nothing");
            }
            final BigDecimal earned = model.totalActualAmortization();
            assertTrue(earned.compareTo(fee) <= 0, "paid " + overpayment + ": earned more than the fee: " + earned);
            if (new BigDecimal(overpayment).compareTo(payable) >= 0) {
                assertEquals(0, earned.compareTo(fee), "paid " + overpayment + ": the whole payable must earn the whole fee");
            } else {
                assertTrue(earned.signum() > 0 && earned.compareTo(fee) < 0,
                        "paid " + overpayment + ": short of the payable must earn short of the fee, got " + earned);
            }
            // Whatever the payments have not earned is still deferred, and the days ahead earn exactly that much.
            assertEquals(0, fee.subtract(earned).compareTo(amount(lastKnownActualFeeBalance(model))),
                    "paid " + overpayment + ": the fee earned and the fee deferred must add up to the fee");
        }
    }

    private static Money lastKnownActualFeeBalance(final ProjectedAmortizationScheduleModel model) {
        return lastKnown(model).actualDiscountFeeBalance();
    }

    private static Money lastKnownActualBalance(final ProjectedAmortizationScheduleModel model) {
        return lastKnown(model).actualBalance();
    }

    /** The last day the schedule knows anything actual about. */
    private static ProjectedPayment lastKnown(final ProjectedAmortizationScheduleModel model) {
        return model.projectedPayments().stream().filter(p -> p.actualDiscountFeeBalance() != null).reduce((a, b) -> b).orElseThrow();
    }

    private static void assertNonNegative(final String where, final Money money) {
        if (money == null) {
            return;
        }
        assertTrue(money.getAmount().signum() >= 0, where + "went negative: " + money.getAmount());
    }

    /**
     * The properties above are held over a spread of payment behaviour, not just over a schedule nobody has touched.
     *
     * <p>
     * Every way of paying it is a different shape of schedule: a missed day restates the projection off a balance a
     * little away from the plan, a part payment leaves it between two plan days, an overpayment closes it early.
     */
    @Test
    void theDeferredFeeClosesOnNothingHoweverTheLoanIsPaid() {
        for (final PaymentPattern pattern : PaymentPattern.values()) {
            final ProjectedAmortizationScheduleModel model = model("9000", "1000", "17");
            pattern.applyTo(model);
            final List<ProjectedPayment> payments = model.projectedPayments();

            // The actual track is never restated, so its two ends must agree exactly on every day it knows about.
            BigDecimal earnedActual = BigDecimal.ZERO;
            for (final ProjectedPayment payment : payments) {
                if (payment.paymentNo() == 0 || payment.actualAmortizationAmount() == null) {
                    continue;
                }
                earnedActual = earnedActual.add(amount(payment.actualAmortizationAmount()));
                assertEquals(0, new BigDecimal("1000.00").subtract(earnedActual).compareTo(amount(payment.actualDiscountFeeBalance())),
                        pattern + ", period " + payment.paymentNo()
                                + ": the fee earned by the money in and the fee still deferred must add up to the fee");
            }

            final ProjectedPayment closing = payments.getLast();
            assertEquals(0, amount(closing.expectedDiscountFeeBalance()).signum(),
                    pattern + ": the expected deferred fee must close on nothing, was " + amount(closing.expectedDiscountFeeBalance()));
            for (final ProjectedPayment payment : payments) {
                assertNonNegative(pattern + ", period " + payment.paymentNo() + " expectedDiscountFeeBalance ",
                        payment.expectedDiscountFeeBalance());
                assertNonNegative(pattern + ", period " + payment.paymentNo() + " actualDiscountFeeBalance ",
                        payment.actualDiscountFeeBalance());
                assertNonNegative(pattern + ", period " + payment.paymentNo() + " expectedBalance ", payment.expectedBalance());
                assertNonNegative(pattern + ", period " + payment.paymentNo() + " actualBalance ", payment.actualBalance());
            }
        }
    }

    /** The ways a borrower can diverge from the plan, each producing a differently shaped schedule. */
    private enum PaymentPattern {

        UNTOUCHED {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                // nothing paid and no day gone by: the schedule as written
            }
        },
        ONE_DAY_MISSED {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                // The payment on day two carries the date past day one, so day one is recorded as missed.
                model.applyPayment(DISBURSEMENT.plusDays(2), new BigDecimal("60"));
            }
        },
        PART_PAYMENT {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal("20"));
            }
        },
        PAID_AHEAD {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal("94.44"));
            }
        },
        NOTHING_FOR_A_FORTNIGHT {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(14));
            }
        },
        /**
         * Two small instalments paid on the days they fell due, then a lump sum backdated behind both of them - the
         * shape that used to strand a couple of cents of the fee unearned.
         */
        MOSTLY_CLEARED_BY_A_BACKDATED_LUMP_SUM {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(2));
                model.applyPayment(DISBURSEMENT.plusDays(2), new BigDecimal("46"));
                model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(3));
                model.applyPayment(DISBURSEMENT.plusDays(3), new BigDecimal("46"));
                model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal("9904"));
            }
        },
        REPAID_IN_FULL {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal("10000"));
            }
        },
        OVERPAID {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal("10500"));
            }
        };

        abstract void applyTo(ProjectedAmortizationScheduleModel model);
    }

    /**
     * A loan with no discount fee has no fee to amortize, so every fee column stays at nothing rather than drifting
     * around zero - the rate is exactly zero when there is no fee, and the normalization must not invent one.
     */
    @Test
    void aLoanWithoutADiscountFeeAmortizesNothing() {
        final ProjectedAmortizationScheduleModel model = model("9000", "0", "18");
        assertEquals(0, model.effectiveInterestRate().signum(), "no fee means no effective rate");
        for (final ProjectedPayment payment : model.projectedPayments()) {
            final String where = "period " + payment.paymentNo() + ": ";
            if (payment.expectedAmortizationAmount() != null) {
                assertEquals(0, payment.expectedAmortizationAmount().getAmount().signum(), where + "expectedAmortizationAmount");
            }
            assertEquals(0, amount(payment.expectedDiscountFeeBalance()).signum(), where + "expectedDiscountFeeBalance");
        }
    }

    /**
     * The smallest unit the loan currency has is the smallest amount the schedule may move by. A day that earns less
     * than half a cent reports nothing and its share is carried into a later day, which is what the running remainder
     * is for - it must never be reported as a fraction, and it must never be lost.
     */
    @Test
    void fractionsOfACentAreCarriedForwardRatherThanDropped() {
        // A one-cent fee over a two-hundred day term: almost every day earns a small fraction of a cent, so the
        // schedule can only report the fee at all if those fractions accumulate.
        final ProjectedAmortizationScheduleModel model = model("9000", "0.01", "18");
        BigDecimal earned = BigDecimal.ZERO;
        int daysReportingSomething = 0;
        for (final ProjectedPayment payment : model.projectedPayments()) {
            if (payment.paymentNo() == 0) {
                continue;
            }
            final BigDecimal amortization = amount(payment.expectedAmortizationAmount());
            assertTrue(amortization.signum() == 0 || amortization.compareTo(CENT) >= 0,
                    "period " + payment.paymentNo() + " reported a fraction of a cent: " + amortization);
            if (amortization.signum() > 0) {
                daysReportingSomething++;
            }
            earned = earned.add(amortization);
        }
        assertEquals(1, daysReportingSomething, "a one-cent fee can only ever be earned on a single day");
        assertEquals(0, earned.compareTo(CENT), "and that cent must not be lost");
        assertNotNull(model.projectedPayments().getLast().expectedDiscountFeeBalance());
        assertEquals(0, amount(model.projectedPayments().getLast().expectedDiscountFeeBalance()).signum(),
                "the fee must still close on nothing");
    }

    /**
     * A day bills the whole instalment unless it is the day that closes the loan.
     *
     * <p>
     * The two are one rule: every day asks for the instalment, capped at the balance there is to close, so a day
     * billing less than the instalment is a day billing the last of the balance. Anything else is a day that
     * under-bills and leaves a remainder for a further day to collect - which is a row the schedule did not need and a
     * closing date a day later than the loan really closes.
     *
     * <p>
     * The restatements below are what put that at risk. They leave the balance on the day the rate was solved to close
     * on higher than the remainder the plan predicted, and billing that stale remainder there strands the difference.
     */
    @Test
    void aDayBillsTheWholeInstalmentUnlessItIsClosingTheLoan() {
        for (final String rate : new String[] { "18", "17", "13" }) {
            for (final String netDisbursement : new String[] { "9000", "5000", "450" }) {
                for (final String discountFee : new String[] { "1000", "500", "0" }) {
                    for (final Divergence divergence : divergencesLeavingTheRateAlone()) {
                        final ProjectedAmortizationScheduleModel model = model(netDisbursement, discountFee, rate);
                        if (divergence != null) {
                            divergence.applyTo(model);
                        }
                        // Constant across the whole schedule because none of these divergences re-price it.
                        final BigDecimal instalment = amount(model.expectedPaymentAmount());
                        for (final ProjectedPayment payment : model.projectedPayments()) {
                            if (payment.paymentNo() == 0) {
                                continue;
                            }
                            final BigDecimal billed = amount(payment.expectedPaymentAmount());
                            if (billed.compareTo(instalment) >= 0) {
                                continue;
                            }
                            assertEquals(0, amount(payment.expectedBalance()).signum(),
                                    "rate " + rate + ", net " + netDisbursement + ", fee " + discountFee + ", " + divergence + ", period "
                                            + payment.paymentNo() + ": billed " + billed + " of a " + instalment
                                            + " instalment while still owing " + payment.expectedBalance());
                        }
                    }
                }
            }
        }
    }

    /** Divergences that leave the rate alone, so one instalment governs every day of the schedule. */
    private static List<Divergence> divergencesLeavingTheRateAlone() {
        final List<Divergence> all = new ArrayList<>();
        all.add(null);
        all.addAll(DELINQUENCIES);
        return all;
    }

    /**
     * Whatever shape the loan is in, the schedule runs until it is square.
     *
     * <p>
     * Swept rather than sampled, because whether this holds turns on arithmetic no single fixture exposes: the last day
     * of the solved term bills the closing remainder, a fraction of an instalment, and on a loan that has fallen behind
     * that day arrives with the balance still far above it. Reading that fraction as what the loan can earn declared
     * the schedule unclosable and ended it there, hundreds of currency units short - but only when the remainder
     * happened to land below one day's fee. Every combination below closed on the day it was written, which is why the
     * golden schedules never noticed.
     */
    @Test
    void everyShapeOfLoanRunsUntilItIsSquare() {
        for (final String rate : new String[] { "24", "18", "17", "13", "9", "1" }) {
            for (final String netDisbursement : new String[] { "9000", "5000", "450" }) {
                for (final String discountFee : new String[] { "1000", "500", "50", "0" }) {
                    for (final Divergence divergence : DELINQUENCIES) {
                        final ProjectedAmortizationScheduleModel model = model(netDisbursement, discountFee, rate);
                        divergence.applyTo(model);
                        final String where = "rate " + rate + ", net " + netDisbursement + ", fee " + discountFee + ", " + divergence
                                + ": ";
                        final ProjectedPayment closing = model.projectedPayments().getLast();
                        assertEquals(0, amount(closing.expectedBalance()).signum(),
                                where + "the schedule ended owing " + closing.expectedBalance());
                        assertEquals(0, amount(closing.expectedDiscountFeeBalance()).signum(),
                                where + "the schedule ended with fee still deferred: " + closing.expectedDiscountFeeBalance());
                    }
                }
            }
        }
    }

    /**
     * Ways of diverging from the plan, stated in amounts small enough to leave any of the loans above still owing.
     */
    /** The divergences safe to apply to any loan the closure sweep builds, whatever its size, fee or rate. */
    private static final List<Divergence> DELINQUENCIES = List.of(Divergence.NOTHING_PAID_FOR_A_MONTH,
            Divergence.NOTHING_PAID_FOR_TWO_MONTHS, Divergence.A_TOKEN_PAYMENT_THEN_NOTHING, Divergence.A_TRICKLE_OF_PAYMENTS_THEN_NOTHING,
            Divergence.A_PART_PAYMENT_AFTER_A_MISSED_WEEK);

    private enum Divergence {

        NOTHING_PAID_FOR_A_MONTH {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(25));
            }
        },
        NOTHING_PAID_FOR_TWO_MONTHS {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(60));
            }
        },
        A_TOKEN_PAYMENT_THEN_NOTHING {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.applyPayment(DISBURSEMENT.plusDays(1), new BigDecimal("10"));
                model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(30));
            }
        },
        A_TRICKLE_OF_PAYMENTS_THEN_NOTHING {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                for (int day = 1; day <= 10; day++) {
                    model.applyPayment(DISBURSEMENT.plusDays(day), BigDecimal.ONE);
                }
                model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(35));
            }
        },
        A_PART_PAYMENT_AFTER_A_MISSED_WEEK {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                model.acknowledgeElapsedPeriods(DISBURSEMENT.plusDays(5));
                model.applyPayment(DISBURSEMENT.plusDays(5), new BigDecimal("150"));
            }
        },
        /** The shape that re-prices the projection midway, so the seam a rate change introduces is covered too. */
        A_RATE_CUT_AFTER_A_PART_PAYMENT {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                A_PART_PAYMENT_AFTER_A_MISSED_WEEK.applyTo(model);
                model.applyRateChange(new BigDecimal("11"), DISBURSEMENT.plusDays(9), DISBURSEMENT.plusDays(9));
            }
        },
        /**
         * Money arriving on both sides of a re-pricing, which is what makes the ledger columns move across it. Without
         * a payment after the change they simply hold, and a rule about how they may move is never put to the test.
         */
        PAYMENTS_EITHER_SIDE_OF_A_RATE_CUT {

            @Override
            void applyTo(final ProjectedAmortizationScheduleModel model) {
                A_RATE_CUT_AFTER_A_PART_PAYMENT.applyTo(model);
                model.applyPayment(DISBURSEMENT.plusDays(12), new BigDecimal("200"));
                model.applyPayment(DISBURSEMENT.plusDays(13), new BigDecimal("40"));
            }
        };

        abstract void applyTo(ProjectedAmortizationScheduleModel model);
    }
}
