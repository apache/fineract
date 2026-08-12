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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel.RateChange;

/**
 * The amortization schedule, walked one day at a time until the loan is square.
 *
 * <h3>What runs the loop</h3> Not a day count. The walk keeps going while the loan still owes something or the schedule
 * has not yet caught up with the date it is being calculated to, and it stops when both are satisfied. A borrower
 * paying exactly to plan closes on the day the rate was solved for, because that is what solving the rate against the
 * real cash flow means. A borrower who overpays closes sooner and the schedule is shorter; one who misses a day closes
 * later and it is longer. There is no separate run of catch-up days bolted onto the end, because the end is wherever
 * the loan happens to close.
 *
 * <h3>Expected against actual</h3> Both tracks are carried on every day. The expected track is the plan as it currently
 * stands: it projects forward on the declining balance, and wherever reality is known - a payment landed, or a day went
 * by with nothing on it - it is restated from that reality rather than from instalments it merely assumed. The actual
 * track is what the money received has really earned, read off {@link PlanCursor}.
 *
 * <h3>Rounding</h3> Nothing in here is rounded except through {@link #normalize}, and that rounds a <em>running
 * total</em>, never a single day's figure. A day's reported share of the fee is the difference between two rounded
 * totals, so it is always a whole number of minor units and the totals never drift from the high-precision figures they
 * shadow. When the high-precision total reaches the discount fee, the normalized total is the discount fee - which is
 * what lets the deferred fee balance close on nothing instead of a stray cent.
 */
@Slf4j
final class AmortizationWalk {

    private final BigDecimal netDisbursement;
    private final BigDecimal discountFee;
    private final BigDecimal totalPaymentVolume;
    private final BigDecimal basePeriodPaymentRate;
    private final int npvDayCount;
    private final LocalDate expectedDisbursementDate;
    private final int firstPeriodDayOffset;
    private final LocalDate calculatedTillDate;
    private final Map<LocalDate, BigDecimal> paymentsByDate;
    private final List<RateChange> rateChanges;
    private final int minimumDays;
    private final int currencyScale;
    private final MathContext mc;

    AmortizationWalk(final BigDecimal netDisbursement, final BigDecimal discountFee, final BigDecimal totalPaymentVolume,
            final BigDecimal basePeriodPaymentRate, final int npvDayCount, final LocalDate expectedDisbursementDate,
            final int firstPeriodDayOffset, final LocalDate calculatedTillDate, final Map<LocalDate, BigDecimal> paymentsByDate,
            final List<RateChange> rateChanges, final int minimumDays, final CurrencyData currency, final MathContext mc) {
        this.netDisbursement = netDisbursement;
        this.discountFee = discountFee;
        this.totalPaymentVolume = totalPaymentVolume;
        this.basePeriodPaymentRate = basePeriodPaymentRate;
        this.npvDayCount = npvDayCount;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.firstPeriodDayOffset = firstPeriodDayOffset;
        this.calculatedTillDate = calculatedTillDate;
        this.paymentsByDate = paymentsByDate;
        this.minimumDays = Math.max(1, minimumDays);
        this.rateChanges = rateChanges == null ? List.of()
                : rateChanges.stream().sorted(Comparator.comparing(RateChange::effectiveDate)).toList();
        this.currencyScale = currency.getDecimalPlaces();
        this.mc = mc;
    }

    /**
     * Rounds a running total to the loan currency. The only rounding the calculation performs, and it is deliberately
     * applied to the aggregate: rounding each day on its own drops a fraction of a minor unit every time, and over a
     * few hundred days those fractions add up to the cents some tickets report missing.
     */
    private BigDecimal normalize(final BigDecimal runningTotal) {
        return runningTotal.setScale(currencyScale, mc.getRoundingMode());
    }

    private LocalDate dateOfDay(final int dayIndex) {
        return expectedDisbursementDate.plusDays((long) dayIndex - 1 + firstPeriodDayOffset);
    }

    /** Day the rate change takes effect on, clamped so a change dated before the first instalment lands on it. */
    private int dayIndexOf(final LocalDate date) {
        final long index = ChronoUnit.DAYS.between(expectedDisbursementDate, date) - firstPeriodDayOffset + 1;
        return (int) Math.max(1L, index);
    }

    /**
     * @param days
     *            every day the schedule runs to, in order
     * @param contractualTerm
     *            the day the rate currently in force was solved to close on. What the schedule would run to if the
     *            borrower paid exactly to plan from here, which is what the loan reports as its number of repayments -
     *            not the same as {@code days.size()}, which grows when instalments are missed.
     * @param rateChangeSolves
     *            what each rate change was solved to on the day the walk reached it, keyed by its effective date. A
     *            change the loan had already gone square before is absent: it re-priced nothing.
     */
    record Result(List<AmortizationDay> days, int contractualTerm, Map<LocalDate, AmortizationParams.Solved> rateChangeSolves) {
    }

    Result walk() {
        final List<AmortizationDay> days = new ArrayList<>();
        final int appliedCount = paymentsByDate.size();

        final PlanCursor plan = new PlanCursor(netDisbursement, discountFee, totalPaymentVolume, basePeriodPaymentRate, npvDayCount,
                currencyScale, mc);

        BigDecimal balance = netDisbursement;
        BigDecimal actualBalanceExact = netDisbursement;
        BigDecimal collected = BigDecimal.ZERO;
        BigDecimal aggregatedHighPrecisionExpected = BigDecimal.ZERO;
        BigDecimal aggregatedNormalizedExpected = BigDecimal.ZERO;
        BigDecimal aggregatedHighPrecisionActual = BigDecimal.ZERO;
        BigDecimal aggregatedNormalizedActual = BigDecimal.ZERO;

        int rateStartDay = 1;
        int nextRateChange = 0;
        final Map<LocalDate, AmortizationParams.Solved> rateChangeSolves = new LinkedHashMap<>();
        // The rate the days still to come are projected at. It starts as the rate the loan was written at and is
        // re-solved wherever the position it was solved for stops being the position the loan is really in - at a rate
        // change, and after reality has diverged from the plan. Separate from the rate the plan cursor earns fee at,
        // which stays as written: what a payment earns must depend on how much money came in and nothing else, and
        // re-solving the curve it is read off would make it depend on when the borrower deviated too.
        BigDecimal rateInForce = basePeriodPaymentRate;
        AmortizationParams.Solved projection = plan.solved();
        boolean projectionStale = false;

        for (int dayIndex = 1; dayIndex <= ProjectedAmortizationScheduleModel.MAX_CALCULABLE_TOTAL_DAYS; dayIndex++) {
            final LocalDate date = dateOfDay(dayIndex);

            // A rate change rewrites what is still to come. It moves no money, so the balance and the fee already
            // earned carry straight through it; only the instalment and the rate the days ahead are solved at change.
            while (nextRateChange < rateChanges.size() && dayIndexOf(rateChanges.get(nextRateChange).effectiveDate()) <= dayIndex) {
                final RateChange change = rateChanges.get(nextRateChange++);
                if (normalize(balance).signum() <= 0) {
                    // Nothing left to re-price. A loan that went square before the change took effect is simply not
                    // affected by it, and the walk is about to end anyway.
                    continue;
                }
                plan.changeRateTo(change.periodPaymentRate(), balance, discountFee.subtract(aggregatedNormalizedExpected, mc), collected);
                rateInForce = change.periodPaymentRate();
                projection = plan.solved();
                rateChangeSolves.put(change.effectiveDate(), projection);
                projectionStale = false;
                rateStartDay = dayIndex;
            }
            final BigDecimal paid = paymentsByDate.get(date);
            final boolean settled = paid != null;

            // Re-price the days still to come against the position the loan has actually reached.
            //
            // The rate was solved so that its daily accruals sum to exactly the fee, on the balance the plan projected.
            // A borrower who paid differently leaves the days ahead running on a different balance, so accruing them at
            // that rate sums to a little less than the fee still unearned - and the days would then bill a few cents
            // less than the loan is owed, a projection that does not foot. Re-solving from what is really owed and what
            // is really still unearned restores it: the accruals sum to the unearned fee again, so the days ahead bill
            // exactly what is left to collect.
            //
            // Both figures are the exact ones, not their reported roundings. Paired that way their sum is precisely
            // what the loan owes, which is what makes the re-solve a no-op for a borrower paying to plan - solving from
            // a position the plan already predicted returns the rate it already had.
            //
            // Once, on the first day reality stops covering, rather than on each settled day. Days the loan has a
            // record
            // for are always a run from the start, so the last of them is the position everything after continues from,
            // and one solve from there re-prices the whole tail - identically to solving on every one of them, at a
            // fraction of the cost.
            if (projectionStale && !settled) {
                final BigDecimal unearnedFee = discountFee.subtract(aggregatedHighPrecisionActual, mc);
                if (balance.signum() > 0 && unearnedFee.signum() > 0) {
                    try {
                        projection = AmortizationParams.solve(balance, unearnedFee, totalPaymentVolume, rateInForce, npvDayCount,
                                currencyScale, mc);
                    } catch (final IllegalArgumentException | IllegalStateException | ArithmeticException e) {
                        // A position no rate can be solved from keeps the one it had. The projection is then the stale
                        // one it would have been anyway, which is worse than re-priced but better than no schedule.
                        log.debug("Could not re-price the projection from balance {} with {} of fee unearned", balance, unearnedFee, e);
                    }
                }
                projectionStale = false;
            }
            final AmortizationParams.Solved rate = projection;

            final BigDecimal grown = balance.multiply(BigDecimal.ONE.add(rate.eir(), mc), mc);
            final int dayWithinRate = dayIndex - rateStartDay + 1;
            // Every day asks for the instalment, and no day can ask for more than the balance it has to close. That
            // second clause is what closes the loan: on the day it runs out the balance is less than an instalment, so
            // the day bills the balance and nothing is left. It needs no separate closing amount to do it - the rate
            // was solved so that the balance reaches exactly that remainder on exactly that day, and where a payment
            // has restated the balance since, what is owed is what the day should bill rather than what the plan once
            // predicted would be.
            final BigDecimal instalment = MathUtil.negativeToZero(rate.dailyPayment()).min(MathUtil.negativeToZero(grown));
            final BigDecimal highPrecisionExpectedFee = grown.subtract(balance, mc);
            final BigDecimal balanceAfter = grown.subtract(instalment, mc);

            final boolean hasPayment = paid != null && paid.signum() > 0;
            final boolean elapsed = calculatedTillDate != null && date.isBefore(calculatedTillDate);

            collected = paid == null ? collected : collected.add(paid, mc);
            final BigDecimal feeEarnedUpToNow = plan.feeEarnedAt(collected);
            final BigDecimal highPrecisionActual = feeEarnedUpToNow.subtract(aggregatedHighPrecisionActual, mc);
            aggregatedHighPrecisionActual = feeEarnedUpToNow;
            final BigDecimal normActualPrev = aggregatedNormalizedActual;
            aggregatedNormalizedActual = normalize(aggregatedHighPrecisionActual);
            actualBalanceExact = actualBalanceExact.subtract(paid == null ? BigDecimal.ZERO : paid, mc).add(highPrecisionActual, mc);

            // What the day after this one continues from. A settled day - one that was paid, or that went by with
            // nothing on it - restates it to what the borrower actually owes; a day that has not come round yet keeps
            // the plan it was written with.
            //
            // Never less than nothing: a borrower who has handed over more than the payable owes nothing, and accruing
            // on a negative balance would have each following day un-earn fee the payments have already earned.
            final BigDecimal carriedForward = settled ? MathUtil.negativeToZero(actualBalanceExact) : balanceAfter;
            // Measured on what is carried forward, not on the day's own projection, so that the day a payment closes
            // the loan is recognised as closing it. Keyed off the projection instead, a loan repaid in full on its
            // first day reported most of its fee as still deferred, on a schedule that had already ended.
            final boolean closed = normalize(carriedForward).signum() <= 0;

            // Never more fee than there is. A projection restated off a balance a little away from the plan accrues a
            // little more or less than the fee still unearned, and the excess would show as a deferred balance below
            // nothing.
            aggregatedHighPrecisionExpected = aggregatedHighPrecisionExpected.add(highPrecisionExpectedFee, mc).min(discountFee);
            if (closed) {
                // The loan is square, so every last unit of the fee has been earned.
                //
                // Only reachable for a loan that closes on a day it is paid, and that is the whole of what this is for.
                // Where days remain, they are re-priced to accrue exactly the fee still unearned, so the total arrives
                // at the fee on its own and this changes nothing. Where the money closes the loan outright there are no
                // days left to accrue over - a loan repaid in full on its first day has one row and one accrual - so
                // without this the deferred balance would stop at whatever that single day happened to earn.
                aggregatedHighPrecisionExpected = discountFee;
            }
            final BigDecimal normExpectedPrev = aggregatedNormalizedExpected;
            aggregatedNormalizedExpected = normalize(aggregatedHighPrecisionExpected);

            final long paymentsLeft = Math.max(0L, (long) dayWithinRate - appliedCount);
            final BigDecimal discountFactor = safeDiscountFactor(rate.eir(), paymentsLeft);
            final BigDecimal npvSource = hasPayment ? paid : elapsed ? BigDecimal.ZERO : instalment;

            // What the borrower still owes: the disbursement they have not paid off yet, plus the fee booked against
            // it.
            // Never less than nothing - a borrower who has handed over more than the payable owes nothing at all, and
            // the excess is held as overpayment on the loan rather than as a negative balance. The loan clamps every
            // outstanding bucket the same way, so reporting a negative here would have the schedule contradict it.
            final BigDecimal actualBalance = MathUtil
                    .negativeToZero(netDisbursement.subtract(collected, mc).add(aggregatedNormalizedActual, mc));

            // The share of the fee each track reports for the day: the movement of a rounded running total, never a
            // rounded day. Rounding the total and taking differences is what keeps the reported figures adding up to
            // the total they shadow, however many hundreds of days they run for.
            final BigDecimal reportedExpectedFee = aggregatedNormalizedExpected.subtract(normExpectedPrev, mc);
            final BigDecimal reportedActualFee = aggregatedNormalizedActual.subtract(normActualPrev, mc);
            final BigDecimal npvValue = MathUtil.negativeToZero(npvSource.multiply(discountFactor, mc));

            days.add(new AmortizationDay(dayIndex, date, rate.eir(), paymentsLeft, discountFactor, instalment, npvValue, paid, collected,
                    balanceAfter, actualBalance, reportedExpectedFee, aggregatedNormalizedExpected, reportedActualFee,
                    aggregatedNormalizedActual, hasPayment, elapsed));

            balance = carriedForward;
            if (settled && !closed) {
                // Reality has moved the position the days ahead continue from, so the rate they are projected at has
                // to be solved again. Done on the first day that has no record rather than here, so a run of settled
                // days costs one solve and not one each.
                projectionStale = true;
                // The deferred fee restates for the same reason the balance does. Days that booked fee against
                // instalments never collected must not carry that total forward, or the re-projected days would earn it
                // a second time. Keeping the normalized total in step with the high-precision one across the seam is
                // what holds the identity aggNorm = round(aggHp) true, and with it the fee closing on exactly nothing.
                aggregatedHighPrecisionExpected = aggregatedHighPrecisionActual;
                aggregatedNormalizedExpected = aggregatedNormalizedActual;
            }

            if (dayIndex >= minimumDays && closed) {
                break;
            }
            if (dayIndex >= minimumDays && !settled
                    && MathUtil.negativeToZero(rate.dailyPayment()).compareTo(highPrecisionExpectedFee) <= 0) {
                // The instalment cannot even cover the fee accruing on the balance, so no number of further days would
                // ever close it. Stop rather than spin.
                //
                // Measured on the instalment the plan asks for rather than the amount billed, which is capped at the
                // balance: a day billing less than the instalment is a day closing the loan, not one failing to.
                break;
            }
        }
        return new Result(days, rateStartDay + plan.solved().term() - 1, rateChangeSolves);
    }

    private BigDecimal safeDiscountFactor(final BigDecimal eir, final long paymentsLeft) {
        final BigDecimal df = TvmFunctions.discountFactor(eir, paymentsLeft, mc);
        return df.signum() <= 0 ? BigDecimal.ONE : df;
    }
}
