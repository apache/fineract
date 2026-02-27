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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.serialization.gson.JsonExclude;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

/**
 * Projected Amortization Schedule model for Working Capital loans.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 * <li>{@link #generate} — create initial schedule (at loan creation)</li>
 * <li>{@link #regenerate} — recalculate with new amounts (at approval / disbursement)</li>
 * <li>{@link #applyPayment} — record payments by date; schedule rebuilds after each</li>
 * </ol>
 */
@Getter
@Accessors(fluent = true)
public final class ProjectedAmortizationScheduleModel {

    private static final String MODEL_VERSION = "1";

    private final Money originationFeeAmount;
    private final Money netDisbursementAmount;
    private final Money totalPaymentValue;
    private final BigDecimal periodPaymentRate;
    private final int npvDayCount;
    private final LocalDate expectedDisbursementDate;

    /** {@code (TPV × periodPaymentRate) / npvDayCount} — constant across payments. */
    private final Money expectedPaymentAmount;

    /** {@code roundUp((netDisbursementAmount + originationFeeAmount) / expectedPaymentAmount)} */
    private final int loanTerm;

    /** Periodic EIR from {@code RATE(loanTerm, -expectedPayment, netDisbursementAmount)}. */
    private final BigDecimal effectiveInterestRate;

    @JsonExclude
    private final MathContext mc;

    @JsonExclude
    private final MonetaryCurrency currency;

    @Getter(AccessLevel.NONE)
    private final List<AppliedPayment> appliedPayments;

    @Getter(AccessLevel.NONE)
    private List<ProjectedPayment> payments;

    private ProjectedAmortizationScheduleModel(final Money originationFeeAmount, final Money netDisbursementAmount,
            final Money totalPaymentValue, final BigDecimal periodPaymentRate, final int npvDayCount,
            final LocalDate expectedDisbursementDate, final Money expectedPaymentAmount, final int loanTerm,
            final BigDecimal effectiveInterestRate, final MathContext mc, final MonetaryCurrency currency) {
        this.originationFeeAmount = originationFeeAmount;
        this.netDisbursementAmount = netDisbursementAmount;
        this.totalPaymentValue = totalPaymentValue;
        this.periodPaymentRate = periodPaymentRate;
        this.npvDayCount = npvDayCount;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.expectedPaymentAmount = expectedPaymentAmount;
        this.loanTerm = loanTerm;
        this.effectiveInterestRate = effectiveInterestRate;
        this.mc = mc;
        this.currency = currency;
        this.appliedPayments = new ArrayList<>();
        rebuildPayments();
    }

    /**
     * Creates a skeleton instance for Gson deserialization. Gson will overwrite final fields via reflection; payments
     * are restored from JSON directly (no rebuild needed).
     */
    public static ProjectedAmortizationScheduleModel forDeserialization(final MathContext mc, final MonetaryCurrency currency) {
        return new ProjectedAmortizationScheduleModel(mc, currency);
    }

    private ProjectedAmortizationScheduleModel(final MathContext mc, final MonetaryCurrency currency) {
        this.originationFeeAmount = null;
        this.netDisbursementAmount = null;
        this.totalPaymentValue = null;
        this.periodPaymentRate = null;
        this.npvDayCount = 0;
        this.expectedDisbursementDate = null;
        this.expectedPaymentAmount = null;
        this.loanTerm = 0;
        this.effectiveInterestRate = null;
        this.mc = mc;
        this.currency = currency;
        this.appliedPayments = new ArrayList<>();
        this.payments = List.of();
    }

    public List<ProjectedPayment> payments() {
        return payments;
    }

    public static ProjectedAmortizationScheduleModel generate(final BigDecimal originationFeeAmount, final BigDecimal netDisbursementAmount,
            final BigDecimal totalPaymentValue, final BigDecimal periodPaymentRate, final int npvDayCount,
            final LocalDate expectedDisbursementDate, final MathContext mc, final MonetaryCurrency currency) {

        Objects.requireNonNull(originationFeeAmount, "originationFeeAmount");
        Objects.requireNonNull(netDisbursementAmount, "netDisbursementAmount");
        Objects.requireNonNull(totalPaymentValue, "totalPaymentValue");
        Objects.requireNonNull(periodPaymentRate, "periodPaymentRate");
        Objects.requireNonNull(expectedDisbursementDate, "expectedDisbursementDate");
        Objects.requireNonNull(currency, "currency");
        if (netDisbursementAmount.signum() <= 0) {
            throw new IllegalArgumentException("netDisbursementAmount must be positive");
        }
        if (npvDayCount <= 0) {
            throw new IllegalArgumentException("npvDayCount must be positive");
        }

        final BigDecimal expectedPayment = totalPaymentValue.multiply(periodPaymentRate, mc).divide(BigDecimal.valueOf(npvDayCount), mc);
        if (expectedPayment.signum() <= 0) {
            throw new IllegalArgumentException("expectedPaymentAmount must be positive (check totalPaymentValue and periodPaymentRate)");
        }

        final int term = netDisbursementAmount.add(originationFeeAmount, mc).divide(expectedPayment, mc).setScale(0, RoundingMode.UP)
                .intValueExact();
        if (term <= 0) {
            throw new IllegalArgumentException("computed loan term must be positive, got: " + term);
        }

        final BigDecimal eir = TvmFunctions.rate(term, expectedPayment.negate(), netDisbursementAmount, mc);

        return new ProjectedAmortizationScheduleModel(Money.of(currency, originationFeeAmount, mc),
                Money.of(currency, netDisbursementAmount, mc), Money.of(currency, totalPaymentValue, mc), periodPaymentRate, npvDayCount,
                expectedDisbursementDate, Money.of(currency, expectedPayment, mc), term, eir, mc, currency);
    }

    public void applyPayment(final LocalDate paymentDate, final BigDecimal amount) {
        Objects.requireNonNull(paymentDate, "paymentDate");
        Objects.requireNonNull(amount, "amount");
        final int index = resolvePaymentIndex(paymentDate);
        if (index < 0 || index >= loanTerm) {
            throw new IllegalArgumentException("paymentDate " + paymentDate + " is outside the valid range ["
                    + expectedDisbursementDate.plusDays(1) + " .. " + expectedDisbursementDate.plusDays(loanTerm) + "]");
        }
        appliedPayments.add(new AppliedPayment(paymentDate, amount));
        rebuildPayments();
    }

    /** Creates a new model with updated parameters, preserving applied payments. */
    public ProjectedAmortizationScheduleModel regenerate(final BigDecimal newDiscountAmount, final BigDecimal newNetAmount,
            final LocalDate newStartDate) {
        final ProjectedAmortizationScheduleModel newModel = generate(newDiscountAmount, newNetAmount, totalPaymentValue.getAmount(),
                periodPaymentRate, npvDayCount, newStartDate, mc, currency);
        newModel.appliedPayments.addAll(appliedPayments);
        newModel.rebuildPayments();
        return newModel;
    }

    private void rebuildPayments() {
        final Map<LocalDate, BigDecimal> paymentsByDate = aggregatePaymentsByDate();
        final List<BigDecimal> paymentList = buildPaymentList(paymentsByDate);
        this.payments = List.copyOf(buildPayments(paymentList, paymentsByDate.size()));
    }

    private Map<LocalDate, BigDecimal> aggregatePaymentsByDate() {
        final Map<LocalDate, BigDecimal> result = new HashMap<>();
        for (final AppliedPayment payment : appliedPayments) {
            result.merge(payment.date(), payment.amount(), BigDecimal::add);
        }
        return result;
    }

    private List<BigDecimal> buildPaymentList(final Map<LocalDate, BigDecimal> paymentsByDate) {
        final List<BigDecimal> result = new ArrayList<>(loanTerm);
        for (int i = 0; i < loanTerm; i++) {
            final LocalDate paymentDate = expectedDisbursementDate.plusDays(i + 1);
            result.add(paymentsByDate.get(paymentDate));
        }
        return result;
    }

    private int resolvePaymentIndex(final LocalDate date) {
        return (int) ChronoUnit.DAYS.between(expectedDisbursementDate, date) - 1;
    }

    private List<ProjectedPayment> buildPayments(final List<BigDecimal> payments, final int appliedCount) {
        final BalancesAndAmortizations ba = computeBalancesAndAmortizations();
        final PaymentAnalysis pa = analyzePayments(payments, appliedCount);
        final List<BigDecimal> actualAmortizations = computeActualAmortizations(ba.expectedAmortizations, payments, appliedCount);
        final List<BigDecimal> runningExpected = computeRunningExpectedPayments(pa.excess);
        final List<ProjectedPayment> tailPayments = new ArrayList<>();
        final BigDecimal tailNpv = buildTailPeriodsAndComputeNpv(tailPayments, pa.shortfall, appliedCount);
        final BigDecimal totalNetAmortization = computeTotalNetAmortization(payments, runningExpected, appliedCount, tailNpv);

        final BigDecimal originationFee = originationFeeAmount.getAmount();
        final BigDecimal safeExpectedPayment = MathUtil.negativeToZero(expectedPaymentAmount.getAmount());

        final List<ProjectedPayment> result = new ArrayList<>(loanTerm + 2 + tailPayments.size());
        result.add(createDisbursementPayment(appliedCount));

        BigDecimal cumulativeActualAmort = BigDecimal.ZERO;
        for (int i = 0; i < loanTerm; i++) {
            final int periodNo = i + 1;
            final boolean hasAppliedAmount = payments.get(i) != null;
            final long count = (long) loanTerm + appliedCount - periodNo;
            final long paymentsLeft = paymentsLeft(periodNo, appliedCount);
            final BigDecimal safeDf = safeDiscountFactor(paymentsLeft);
            final BigDecimal safeRunningExpected = MathUtil.negativeToZero(runningExpected.get(i));
            final BigDecimal npvSource = hasAppliedAmount ? payments.get(i) : safeRunningExpected;
            final BigDecimal npvValue = MathUtil.negativeToZero(npvSource.multiply(safeDf, mc));
            final BigDecimal safeExpectedAmort = ba.expectedAmortizations.get(i).min(originationFee);

            final BigDecimal netAmortization;
            final BigDecimal actualAmortization;
            final BigDecimal incomeModification;

            if (hasAppliedAmount) {
                actualAmortization = actualAmortizations.get(i);
                netAmortization = totalNetAmortization.subtract(cumulativeActualAmort, mc).min(originationFee);
                cumulativeActualAmort = cumulativeActualAmort.add(actualAmortization, mc).min(originationFee);
                incomeModification = actualAmortization.subtract(safeExpectedAmort, mc);
            } else {
                netAmortization = BigDecimal.ZERO;
                actualAmortization = null;
                incomeModification = safeExpectedAmort.negate();
            }

            final BigDecimal deferredBalance = originationFee.subtract(cumulativeActualAmort, mc);
            final BigDecimal balance = ba.balances.get(i);
            result.add(new ProjectedPayment(periodNo, expectedDisbursementDate.plusDays(periodNo), count, paymentsLeft,
                    money(safeExpectedPayment), money(safeRunningExpected), safeDf, money(npvValue), money(balance),
                    money(safeExpectedAmort), money(netAmortization), hasAppliedAmount ? money(payments.get(i)) : null,
                    actualAmortization != null ? money(actualAmortization) : null, money(incomeModification), money(deferredBalance)));
        }

        result.addAll(tailPayments);

        while (result.size() > 1) {
            final ProjectedPayment last = result.getLast();
            if (last.forecastPaymentAmount() != null && last.forecastPaymentAmount().isZero()) {
                result.removeLast();
            } else {
                break;
            }
        }

        return result;
    }

    private ProjectedPayment createDisbursementPayment(final int appliedCount) {
        final Money negDisbursement = netDisbursementAmount.negated(mc);
        final long count = (long) loanTerm + appliedCount;
        return new ProjectedPayment(0, expectedDisbursementDate, count, 0L, negDisbursement, null, BigDecimal.ONE, negDisbursement,
                netDisbursementAmount, null, null, null, null, null, originationFeeAmount);
    }

    /**
     * {@code balance[i] = balance[i-1]×(1+EIR) - expectedPayment}<br>
     * {@code expectedAmort[i] = balance[i] + expectedPayment - balance[i-1]}
     */
    private BalancesAndAmortizations computeBalancesAndAmortizations() {
        final BigDecimal onePlusRate = BigDecimal.ONE.add(effectiveInterestRate, mc);
        final BigDecimal expectedPayment = expectedPaymentAmount.getAmount();
        final List<BigDecimal> balances = new ArrayList<>(loanTerm);
        final List<BigDecimal> expectedAmortizations = new ArrayList<>(loanTerm);
        BigDecimal prevBalance = netDisbursementAmount.getAmount();
        for (int i = 0; i < loanTerm; i++) {
            final BigDecimal balance = prevBalance.multiply(onePlusRate, mc).subtract(expectedPayment, mc);
            balances.add(balance);
            expectedAmortizations.add(balance.add(expectedPayment, mc).subtract(prevBalance, mc));
            prevBalance = balance;
        }
        return new BalancesAndAmortizations(balances, expectedAmortizations);
    }

    private PaymentAnalysis analyzePayments(final List<BigDecimal> payments, final int appliedCount) {
        final BigDecimal expectedPayment = expectedPaymentAmount.getAmount();
        BigDecimal shortfall = BigDecimal.ZERO;
        BigDecimal excess = BigDecimal.ZERO;
        for (int i = 0; i < appliedCount; i++) {
            final BigDecimal diff = payments.get(i).subtract(expectedPayment, mc);
            if (diff.signum() > 0) {
                excess = excess.add(diff, mc);
            } else if (diff.signum() < 0) {
                shortfall = shortfall.add(diff.negate(), mc);
            }
        }
        return new PaymentAnalysis(shortfall, excess);
    }

    /** Cursor-based: each payment consumes {@code actualPayment/expectedPayment} periods of expected amortization. */
    private List<BigDecimal> computeActualAmortizations(final List<BigDecimal> expectedAmortizations, final List<BigDecimal> payments,
            final int appliedCount) {
        final BigDecimal expectedPayment = expectedPaymentAmount.getAmount();
        final List<BigDecimal> result = new ArrayList<>(appliedCount);
        BigDecimal cursor = BigDecimal.ZERO;
        for (int i = 0; i < appliedCount; i++) {
            final BigDecimal periodsConsumed = payments.get(i).divide(expectedPayment, mc);
            result.add(consumeExpectedAmortization(expectedAmortizations, cursor, periodsConsumed));
            cursor = cursor.add(periodsConsumed, mc);
        }
        return result;
    }

    private BigDecimal consumeExpectedAmortization(final List<BigDecimal> expectedAmortizations, final BigDecimal startPos,
            final BigDecimal count) {
        if (count.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal pos = startPos;
        BigDecimal remaining = count;
        while (remaining.signum() > 0 && pos.intValue() < expectedAmortizations.size()) {
            final int periodIndex = pos.intValue();
            final BigDecimal posInPeriod = pos.subtract(BigDecimal.valueOf(periodIndex), mc);
            final BigDecimal availableInPeriod = BigDecimal.ONE.subtract(posInPeriod, mc);
            final BigDecimal toConsume = remaining.min(availableInPeriod);
            sum = sum.add(toConsume.multiply(expectedAmortizations.get(periodIndex), mc), mc);
            pos = pos.add(toConsume, mc);
            remaining = remaining.subtract(toConsume, mc);
        }
        return sum;
    }

    private List<BigDecimal> computeRunningExpectedPayments(final BigDecimal excess) {
        final BigDecimal expectedPayment = expectedPaymentAmount.getAmount();
        final List<BigDecimal> running = new ArrayList<>(loanTerm);
        for (int i = 0; i < loanTerm; i++) {
            running.add(expectedPayment);
        }

        BigDecimal remainingExcess = excess;
        for (int i = loanTerm - 1; i >= 0 && remainingExcess.signum() > 0; i--) {
            final BigDecimal reduction = remainingExcess.min(running.get(i));
            running.set(i, running.get(i).subtract(reduction, mc));
            remainingExcess = remainingExcess.subtract(reduction, mc);
        }
        return running;
    }

    private BigDecimal buildTailPeriodsAndComputeNpv(final List<ProjectedPayment> tailPayments, final BigDecimal shortfall,
            final int appliedCount) {
        final BigDecimal expectedPayment = expectedPaymentAmount.getAmount();
        BigDecimal tailNpv = BigDecimal.ZERO;
        BigDecimal remaining = shortfall;
        int tailIndex = 0;
        while (remaining.signum() > 0) {
            final int periodNo = loanTerm + tailIndex + 1;
            final long dl = paymentsLeft(periodNo, appliedCount);
            final BigDecimal df = safeDiscountFactor(dl);
            final BigDecimal forecast = remaining.min(expectedPayment);
            final BigDecimal npv = MathUtil.negativeToZero(forecast.multiply(df, mc));

            final long count = (long) loanTerm + appliedCount - periodNo;
            tailNpv = tailNpv.add(npv, mc);
            tailPayments.add(new ProjectedPayment(periodNo, expectedDisbursementDate.plusDays(periodNo), count, dl, null, money(forecast),
                    df, money(npv), null, null, money(BigDecimal.ZERO), null, null, null, null));

            remaining = remaining.subtract(forecast, mc);
            tailIndex++;
        }
        return tailNpv;
    }

    /** {@code totalNetAmortization = -netDisbursementAmount + sum(npvSource × DF) + tailNpv} */
    private BigDecimal computeTotalNetAmortization(final List<BigDecimal> payments, final List<BigDecimal> runningExpected,
            final int appliedCount, final BigDecimal tailNpv) {
        BigDecimal total = netDisbursementAmount.getAmount().negate();
        for (int i = 0; i < loanTerm; i++) {
            final BigDecimal npvSource = payments.get(i) != null ? payments.get(i) : runningExpected.get(i);
            final BigDecimal df = safeDiscountFactor(paymentsLeft(i + 1, appliedCount));
            total = total.add(npvSource.multiply(df, mc), mc);
        }
        return total.add(tailNpv, mc);
    }

    private BigDecimal safeDiscountFactor(final long paymentsLeft) {
        final BigDecimal df = TvmFunctions.discountFactor(effectiveInterestRate, paymentsLeft, mc);
        return df.signum() <= 0 ? BigDecimal.ONE : df;
    }

    private long paymentsLeft(final int periodNumber, final int appliedCount) {
        return Math.max(0L, (long) periodNumber - appliedCount);
    }

    private Money money(final BigDecimal amount) {
        return Money.of(currency, amount, mc);
    }

    private record BalancesAndAmortizations(List<BigDecimal> balances, List<BigDecimal> expectedAmortizations) {
    }

    private record PaymentAnalysis(BigDecimal shortfall, BigDecimal excess) {
    }

    public record AppliedPayment(LocalDate date, BigDecimal amount) {
    }

    public static String getModelVersion() {
        return MODEL_VERSION;
    }
}
