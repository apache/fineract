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

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.serialization.gson.JsonExclude;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;

/**
 * Projected Amortization Schedule model for Working Capital loans.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 * <li>{@link #generate} — create initial schedule (at loan creation)</li>
 * <li>{@link #regenerate} — recalculate with new amounts (at approval / disbursement)</li>
 * <li>{@link #applyPayment} — record payments by date; schedule rebuilds after each</li>
 * <li>{@link #applyRateChange} — apply a mid-lifecycle rate change; adds a {@link RateSegment} and rebuilds the payment
 * list in-place</li>
 * <li>Discount fee adjustment — regenerate schedule with the new discount and re-apply actual payments only</li>
 * </ol>
 */
@Getter
@Accessors(fluent = true)
@Slf4j
public final class ProjectedAmortizationScheduleModel {

    private static final String MODEL_VERSION = "4";

    @SerializedName(value = "discountFeeAmount", alternate = "originationFeeAmount")
    private final Money discountFeeAmount;
    private final Money netDisbursementAmount;
    private final Money totalPaymentValue;
    private final BigDecimal periodPaymentRate;
    private final int npvDayCount;
    private final LocalDate expectedDisbursementDate;

    /** {@code (TPV × periodPaymentRate) / npvDayCount} — constant across payments. */
    private final Money expectedPaymentAmount;

    /** {@code roundUp((netDisbursementAmount + discountFeeAmount) / expectedPaymentAmount)} */
    @SerializedName(value = "originalPaymentNumber", alternate = "loanTerm")
    private final int originalPaymentNumber;

    /** Periodic EIR from {@code RATE(originalPaymentNumber, -expectedPayment, netDisbursementAmount)}. */
    private final BigDecimal effectiveInterestRate;

    @JsonExclude
    private final MathContext mc;

    @JsonExclude
    private final CurrencyData currency;

    @Getter(AccessLevel.NONE)
    @SerializedName(value = "actualPayments", alternate = "appliedPayments")
    private final List<ActualPayment> actualPayments;

    @Getter(AccessLevel.NONE)
    private final List<RateSegment> rateSegments;

    @Getter(AccessLevel.NONE)
    @SerializedName(value = "projectedPayments", alternate = "payments")
    private List<ProjectedPayment> projectedPayments;

    @Getter(AccessLevel.NONE)
    private List<ProjectedPayment> originalProjectedPayments;

    private LocalDate calculatedTillDate;

    private ProjectedAmortizationScheduleModel(final Money discountFeeAmount, final Money netDisbursementAmount,
            final Money totalPaymentValue, final BigDecimal periodPaymentRate, final int npvDayCount,
            final LocalDate expectedDisbursementDate, final Money expectedPaymentAmount, final int originalPaymentNumber,
            final BigDecimal effectiveInterestRate, final MathContext mc, final CurrencyData currency,
            final LocalDate currentBusinessDate) {
        this.discountFeeAmount = discountFeeAmount;
        this.netDisbursementAmount = netDisbursementAmount;
        this.totalPaymentValue = totalPaymentValue;
        this.periodPaymentRate = periodPaymentRate;
        this.npvDayCount = npvDayCount;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.expectedPaymentAmount = expectedPaymentAmount;
        this.originalPaymentNumber = originalPaymentNumber;
        this.effectiveInterestRate = effectiveInterestRate;
        this.mc = mc;
        this.currency = currency;
        this.actualPayments = new ArrayList<>();
        this.rateSegments = new ArrayList<>();
        this.calculatedTillDate = expectedDisbursementDate != null ? expectedDisbursementDate : currentBusinessDate;
        rebuildPayments();
    }

    /**
     * Creates a skeleton instance for Gson deserialization. Gson will overwrite final fields via reflection; payments
     * are restored from JSON directly (no rebuild needed).
     */
    public static ProjectedAmortizationScheduleModel forDeserialization(final MathContext mc, final CurrencyData currency) {
        return new ProjectedAmortizationScheduleModel(mc, currency);
    }

    private ProjectedAmortizationScheduleModel(final MathContext mc, final CurrencyData currency) {
        this.discountFeeAmount = null;
        this.netDisbursementAmount = null;
        this.totalPaymentValue = null;
        this.periodPaymentRate = null;
        this.npvDayCount = 0;
        this.expectedDisbursementDate = null;
        this.expectedPaymentAmount = null;
        this.originalPaymentNumber = 0;
        this.effectiveInterestRate = null;
        this.mc = mc;
        this.currency = currency;
        this.actualPayments = new ArrayList<>();
        this.rateSegments = new ArrayList<>();
        this.projectedPayments = List.of();
        this.originalProjectedPayments = List.of();
        this.calculatedTillDate = null;
    }

    public List<ProjectedPayment> projectedPayments() {
        return projectedPayments;
    }

    public List<ProjectedPayment> originalProjectedPayments() {
        return originalProjectedPayments;
    }

    public List<RateSegment> rateSegments() {
        return rateSegments != null ? List.copyOf(rateSegments) : List.of();
    }

    /** Snapshot of repayments already applied; used when restating the schedule after a discount fee adjustment. */
    public List<ActualPayment> snapshotActualPayments() {
        return List.copyOf(actualPayments);
    }

    /** Sum of {@code actualAmortizationAmount} across all applied payment periods (paymentNo &gt; 0). */
    public BigDecimal totalActualAmortization() {
        if (projectedPayments == null) {
            return BigDecimal.ZERO;
        }
        return projectedPayments.stream().filter(p -> p.paymentNo() > 0 && p.actualAmortizationAmount() != null)
                .map(p -> p.actualAmortizationAmount().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int effectiveTotalTerm() {
        if (rateSegments == null || rateSegments.isEmpty()) {
            return originalPaymentNumber;
        }
        final RateSegment last = rateSegments.getLast();
        // When startDayIndex > 0, the segment overlaps one day with the base schedule (the split day),
        // so subtract 1. When startDayIndex == 0, there are no base days — no overlap.
        final int overlap = last.startDayIndex() > 0 ? 1 : 0;
        return last.startDayIndex() + last.segmentTerm() - overlap;
    }

    public static ProjectedAmortizationScheduleModel generate(final BigDecimal discountFeeAmount, final BigDecimal netDisbursementAmount,
            final BigDecimal totalPaymentValue, final BigDecimal periodPaymentRate, final int npvDayCount,
            final LocalDate expectedDisbursementDate, final MathContext mc, final CurrencyData currency, final LocalDate currentDate) {

        Objects.requireNonNull(discountFeeAmount, "discountFeeAmount");
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

        final BigDecimal expectedPayment = totalPaymentValue.multiply(periodPaymentRate, mc).divide(BigDecimal.valueOf(npvDayCount), mc)
                .divide(BigDecimal.valueOf(100), mc);
        if (expectedPayment.signum() <= 0) {
            throw new IllegalArgumentException("expectedPaymentAmount must be positive (check totalPaymentValue and periodPaymentRate)");
        }

        final int originalPaymentNumber = netDisbursementAmount.add(discountFeeAmount, mc).divide(expectedPayment, mc)
                .setScale(0, RoundingMode.UP).intValueExact();
        if (originalPaymentNumber <= 0) {
            throw new IllegalArgumentException("computed originalPaymentNumber must be positive, got: " + originalPaymentNumber);
        }

        final BigDecimal eir = TvmFunctions.rate(originalPaymentNumber, expectedPayment.negate(), netDisbursementAmount, mc);

        return new ProjectedAmortizationScheduleModel(Money.of(currency, discountFeeAmount, mc),
                Money.of(currency, netDisbursementAmount, mc), Money.of(currency, totalPaymentValue, mc), periodPaymentRate, npvDayCount,
                expectedDisbursementDate, Money.of(currency, expectedPayment, mc), originalPaymentNumber, eir, mc, currency, currentDate);
    }

    private LocalDate calculateAllocationDate(final LocalDate paymentDate) {
        final LocalDate firstInstallmentDate = expectedDisbursementDate.plusDays(1);
        final LocalDate lastInstallmentDate = expectedDisbursementDate.plusDays(effectiveTotalTerm());
        if (paymentDate.isBefore(firstInstallmentDate) || paymentDate.equals(expectedDisbursementDate)) {
            return firstInstallmentDate;
        }
        if (paymentDate.isAfter(lastInstallmentDate)) {
            return lastInstallmentDate;
        }
        return paymentDate;
    }

    public void applyPayment(final LocalDate paymentDate, final BigDecimal amount) {
        Objects.requireNonNull(paymentDate, "paymentDate");
        Objects.requireNonNull(amount, "amount");
        updateCalculatedTillDate(paymentDate);
        final LocalDate allocationDate = calculateAllocationDate(paymentDate);
        final int index = resolvePaymentIndex(allocationDate);
        if (index < 0 || index >= effectiveTotalTerm()) {
            throw new IllegalArgumentException("paymentDate " + paymentDate + " is outside the valid range ["
                    + expectedDisbursementDate.plusDays(1) + " .. " + expectedDisbursementDate.plusDays(effectiveTotalTerm()) + "]");
        }
        actualPayments.add(new ActualPayment(allocationDate, money(amount)));
        rebuildPayments();
    }

    private void updateCalculatedTillDate(final LocalDate actionDate) {
        if (calculatedTillDate == null || actionDate.isAfter(calculatedTillDate)) {
            this.calculatedTillDate = actionDate;
        }
    }

    /** Creates a new model with updated parameters, preserving applied payments. */
    public ProjectedAmortizationScheduleModel regenerate(final BigDecimal newDiscountAmount, final BigDecimal newNetAmount,
            final LocalDate newStartDate, final LocalDate currentDate) {
        final ProjectedAmortizationScheduleModel newModel = generate(newDiscountAmount, newNetAmount, totalPaymentValue.getAmount(),
                periodPaymentRate, npvDayCount, newStartDate, mc, currency, currentDate);
        newModel.actualPayments.addAll(actualPayments);
        newModel.rebuildPayments();
        return newModel;
    }

    public void recalculateNetAmortizationAndDeferredBalanceFrom(final LocalDate repaymentDate) {
        if (repaymentDate == null || projectedPayments == null || projectedPayments.isEmpty()) {
            return;
        }
        updateCalculatedTillDate(repaymentDate);
        final ProjectedPayment lastRepayment = projectedPayments.stream().filter(p -> p.paymentNo() > 0)
                .filter(p -> repaymentDate.equals(p.date())).reduce((a, b) -> b).orElse(null);

        if (lastRepayment == null) {
            log.warn("Repayment date {} not found among projected payments; skipping net/deferred recalculation", repaymentDate);
            return;
        }

        int fromIndex = projectedPayments.indexOf(lastRepayment);

        BigDecimal runningNetAmortization = amountOrZero(projectedPayments.get(fromIndex).totalAmortizedAmount());
        BigDecimal runningDeferredBalance = amountOrZero(projectedPayments.get(fromIndex).deferredBalance());

        final List<ProjectedPayment> adjusted = new ArrayList<>(projectedPayments.subList(0, fromIndex + 1));
        for (int i = fromIndex + 1; i < projectedPayments.size(); i++) {
            final ProjectedPayment current = projectedPayments.get(i);
            final BigDecimal actualTotalAmortization = amountOrZero(current.actualAmortizationAmount());
            runningNetAmortization = runningNetAmortization.subtract(actualTotalAmortization, mc);
            runningDeferredBalance = runningDeferredBalance.subtract(actualTotalAmortization, mc);

            adjusted.add(new ProjectedPayment(current.paymentNo(), current.date(), current.paymentsLeft(), current.expectedPaymentAmount(),
                    current.forecastPaymentAmount(), current.discountFactor(), current.npvValue(), current.balance(),
                    current.expectedAmortizationAmount(), money(runningNetAmortization), current.actualPaymentAmount(),
                    current.actualAmortizationAmount(), current.incomeModification(), money(runningDeferredBalance)));
        }
        this.projectedPayments = List.copyOf(adjusted);
    }

    /**
     * Applies a rate change at the given date. Adds a {@link RateSegment} covering the remaining term from the change
     * date forward. The model is mutated in-place; the payment list is rebuilt.
     *
     * <p>
     * Any existing segments at or after the split point are removed first (supports undo/overwrite).
     *
     * @param newPeriodPaymentRate
     *            the new period payment rate as a <strong>percentage</strong> (same convention as {@link #generate})
     * @param rateChangeDate
     *            the date of the rate change (must be within model's date range)
     */
    public void applyRateChange(final BigDecimal newPeriodPaymentRate, final LocalDate rateChangeDate) {
        Objects.requireNonNull(newPeriodPaymentRate, "newPeriodPaymentRate");
        Objects.requireNonNull(rateChangeDate, "rateChangeDate");

        updateCalculatedTillDate(rateChangeDate);
        final int rawSplitDayIndex = (int) ChronoUnit.DAYS.between(expectedDisbursementDate, rateChangeDate);
        if (rawSplitDayIndex < 0) {
            throw new IllegalArgumentException("rateChangeDate must not be before expectedDisbursementDate");
        }

        // When the rate change is past the base schedule's term, clamp the segment start
        // to originalPaymentNumber. The loan is still active (borrower hasn't paid), so the remaining
        // balance is netDisbursement - paymentsReceived.
        final int splitDayIndex = Math.min(rawSplitDayIndex, originalPaymentNumber);

        // Remove existing segments at or after split (supports overwrite on second rate change)
        // Guard against null rateSegments from V1 model deserialization
        if (rateSegments == null) {
            throw new IllegalStateException("Model not properly initialized; rateSegments is null");
        }
        rateSegments.removeIf(s -> s.startDayIndex() >= splitDayIndex);

        // Collect actual payments received before the split
        BigDecimal paymentsReceived = BigDecimal.ZERO;
        for (final ProjectedPayment p : projectedPayments) {
            if (p.paymentNo() <= 0 || p.paymentNo() > splitDayIndex) {
                continue;
            }
            if (p.actualPaymentAmount() != null) {
                paymentsReceived = paymentsReceived.add(p.actualPaymentAmount().getAmount(), mc);
            }
        }

        // Compute balance at split: if past term, use remaining principal; otherwise use base amortization
        final BigDecimal balanceAtSplit;
        if (rawSplitDayIndex >= originalPaymentNumber) {
            balanceAtSplit = netDisbursementAmount.getAmount().subtract(paymentsReceived, mc);
        } else if (splitDayIndex > 1) {
            // Balance at the end of the LAST base period (splitDayIndex-1), not at splitDayIndex itself
            final int lastBasePeriod = splitDayIndex - 1;
            final BalancesAndAmortizations ba = computeBaseBalancesUpTo(lastBasePeriod);
            balanceAtSplit = ba.balances().get(lastBasePeriod - 1).getAmount();
        } else {
            balanceAtSplit = netDisbursementAmount.getAmount();
        }

        final BigDecimal origNet = netDisbursementAmount.getAmount();
        final BigDecimal origDiscount = discountFeeAmount.getAmount();
        final BigDecimal tpv = totalPaymentValue.getAmount();

        final BigDecimal newNetDisb = balanceAtSplit;
        final BigDecimal newDiscount;
        if (rawSplitDayIndex >= originalPaymentNumber) {
            newDiscount = origDiscount.add(origNet, mc).subtract(balanceAtSplit, mc).subtract(paymentsReceived, mc);
        } else {
            final BigDecimal baseExpectedPayment = expectedPaymentAmount.getAmount();
            final BigDecimal consumedByBaseSchedule = baseExpectedPayment.multiply(BigDecimal.valueOf(splitDayIndex - 1L), mc);
            final BigDecimal remainingTotal = origNet.add(origDiscount, mc).subtract(consumedByBaseSchedule, mc).subtract(paymentsReceived,
                    mc);
            newDiscount = remainingTotal.subtract(balanceAtSplit, mc);
        }
        final int scale = currency.getDecimalPlaces();
        final BigDecimal newDailyPayment = tpv.multiply(newPeriodPaymentRate, mc).divide(BigDecimal.valueOf(npvDayCount), mc)
                .divide(BigDecimal.valueOf(100), mc).setScale(scale, mc.getRoundingMode());
        final BigDecimal fractionalTotalDays = newNetDisb.add(newDiscount, mc).divide(newDailyPayment, mc).setScale(scale,
                mc.getRoundingMode());
        final int newTerm = fractionalTotalDays.intValue();

        // When daily payment exceeds remaining gross (e.g., very short-term loan with high TPV),
        // the fractional term rounds to 0. Use at least 1 period.
        final int safeTerm = Math.max(newTerm, 1);
        if (newNetDisb.signum() <= 0) {
            throw new IllegalArgumentException("balance at split must be positive for rate change");
        }

        final BigDecimal newEir = TvmFunctions.rate(safeTerm, newDailyPayment.negate(), newNetDisb, mc);

        rateSegments.add(new RateSegment(splitDayIndex, money(newDailyPayment), safeTerm, newEir, money(newNetDisb), money(newDiscount)));
        rateSegments.sort(Comparator.comparingInt(RateSegment::startDayIndex));

        rebuildPayments();
    }

    /**
     * Removes the last rate change segment without triggering a rebuild. Use when a subsequent operation (e.g.,
     * {@link #applyRateChange}) will rebuild anyway.
     */
    public void clearLastRateSegment() {
        if (rateSegments != null && !rateSegments.isEmpty()) {
            rateSegments.removeLast();
        }
    }

    private void rebuildPayments() {
        final BalancesAndAmortizations ba = computeBalancesAndAmortizations();
        rebuildOriginalProjectedPayments(ba);
        final Map<LocalDate, BigDecimal> paymentsByDate = aggregatePaymentsByDate();
        final List<BigDecimal> paymentList = buildPaymentList(paymentsByDate);
        this.projectedPayments = List.copyOf(buildPayments(paymentList, paymentsByDate.size(), ba));
    }

    private void rebuildOriginalProjectedPayments(final BalancesAndAmortizations ba) {
        final int totalTerm = effectiveTotalTerm();
        final BigDecimal discountFee = discountFeeAmount.getAmount();
        final List<ProjectedPayment> result = new ArrayList<>(totalTerm + 1);

        result.add(createDisbursementPayment());

        for (int i = 0; i < totalTerm; i++) {
            final int periodNo = i + 1;
            final RateSegment seg = segmentForDay(periodNo);
            final long segRelativePeriod = seg != null ? periodNo - seg.startDayIndex() + 1 : periodNo;
            final BigDecimal periodExpectedPayment = MathUtil.negativeToZero(expectedPaymentForDay(periodNo));
            final BigDecimal safeDf = safeDiscountFactor(segRelativePeriod, periodNo);
            final BigDecimal npvValue = MathUtil.negativeToZero(periodExpectedPayment.multiply(safeDf, mc));
            final BigDecimal safeExpectedAmort = ba.expectedAmortizations().get(i).getAmount().min(discountFee);
            final BigDecimal balance = ba.balances().get(i).getAmount();

            result.add(new ProjectedPayment(periodNo, expectedDisbursementDate.plusDays(periodNo), segRelativePeriod,
                    money(periodExpectedPayment), money(periodExpectedPayment), safeDf, money(npvValue), money(balance),
                    money(safeExpectedAmort), null, null, null, null, money(discountFee)));
        }

        this.originalProjectedPayments = List.copyOf(result);
    }

    private Map<LocalDate, BigDecimal> aggregatePaymentsByDate() {
        final Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        if (!actualPayments.isEmpty() && calculatedTillDate != null) {
            final LocalDate firstInstallmentDate = expectedDisbursementDate.plusDays(1);
            final LocalDate lastInstallmentDate = expectedDisbursementDate.plusDays(effectiveTotalTerm());
            if (!calculatedTillDate.isBefore(firstInstallmentDate)) {
                final LocalDate mapEnd = calculatedTillDate.isAfter(lastInstallmentDate) ? lastInstallmentDate : calculatedTillDate;
                result.putAll(generateDateMap(firstInstallmentDate, mapEnd));
            }
        }
        for (final ActualPayment payment : actualPayments) {
            result.merge(payment.date(), payment.amount().getAmount(), BigDecimal::add);
        }
        return result;
    }

    public static Map<LocalDate, BigDecimal> generateDateMap(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();

        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            result.put(current, BigDecimal.ZERO);
            current = current.plusDays(1);
        }

        return result;
    }

    private List<BigDecimal> buildPaymentList(final Map<LocalDate, BigDecimal> paymentsByDate) {
        final int totalTerm = effectiveTotalTerm();
        final List<BigDecimal> result = new ArrayList<>(totalTerm);
        for (int i = 0; i < totalTerm; i++) {
            final LocalDate paymentDate = expectedDisbursementDate.plusDays(i + 1);
            result.add(paymentsByDate.get(paymentDate));
        }
        return result;
    }

    private int resolvePaymentIndex(final LocalDate date) {
        return (int) ChronoUnit.DAYS.between(expectedDisbursementDate, date) - 1;
    }

    private List<ProjectedPayment> buildPayments(final List<BigDecimal> payments, final int appliedCount,
            final BalancesAndAmortizations ba) {
        final PaymentAnalysis pa = analyzePayments(payments, appliedCount);
        final BigDecimal amountToAdjustTail = pa.excess.subtract(pa.shortfall);
        final List<BigDecimal> expectedAmortizationAmounts = ba.expectedAmortizations().stream().map(Money::getAmount).toList();
        final List<BigDecimal> actualAmortizations = computeActualAmortizations(expectedAmortizationAmounts, payments, appliedCount);
        final BigDecimal excessForRunning = amountToAdjustTail.max(BigDecimal.ZERO);
        final BigDecimal shortfallForTail = amountToAdjustTail.min(BigDecimal.ZERO).negate();
        final List<BigDecimal> runningExpected = computeRunningExpectedPayments(excessForRunning);
        final List<ProjectedPayment> tailPayments = new ArrayList<>();
        final BigDecimal tailNpv = buildTailPeriodsAndComputeNpv(tailPayments, shortfallForTail, appliedCount);
        final BigDecimal totalNetAmortization = computeTotalNetAmortization(payments, runningExpected, appliedCount, tailNpv);

        final BigDecimal discountFee = discountFeeAmount.getAmount();

        final List<ProjectedPayment> result = new ArrayList<>(effectiveTotalTerm() + 2 + tailPayments.size());
        result.add(createDisbursementPayment());

        BigDecimal cumulativeActualAmort = BigDecimal.ZERO;
        for (int i = 0; i < effectiveTotalTerm(); i++) {
            final int periodNo = i + 1;
            final LocalDate periodDate = expectedDisbursementDate.plusDays(periodNo);
            final BigDecimal periodPayment = payments.get(i);
            final boolean hasPositivePayment = periodPayment != null && periodPayment.signum() > 0;
            final long paymentsLeft = paymentsLeft(periodNo, appliedCount);
            final BigDecimal safeDf = safeDiscountFactor(paymentsLeft, periodNo);
            final BigDecimal periodExpectedPayment = MathUtil.negativeToZero(expectedPaymentForDay(periodNo));
            final BigDecimal safeRunningExpected = MathUtil.negativeToZero(runningExpected.get(i));
            final BigDecimal npvSource = resolveNpvSource(hasPositivePayment, periodPayment, periodDate, safeRunningExpected);
            final BigDecimal npvValue = MathUtil.negativeToZero(npvSource.multiply(safeDf, mc));
            final BigDecimal safeExpectedAmort = ba.expectedAmortizations().get(i).getAmount().min(discountFee);

            final BigDecimal netAmortization;
            final BigDecimal actualAmortization;
            final BigDecimal incomeModification;

            if (hasPositivePayment) {
                actualAmortization = actualAmortizations.get(i);
                netAmortization = totalNetAmortization.subtract(cumulativeActualAmort, mc).min(discountFee);
                cumulativeActualAmort = cumulativeActualAmort.add(actualAmortization, mc).min(discountFee);
                incomeModification = actualAmortization.subtract(safeExpectedAmort, mc);
            } else {
                netAmortization = BigDecimal.ZERO;
                actualAmortization = null;
                incomeModification = null;
            }

            final BigDecimal deferredBalance = discountFee.subtract(cumulativeActualAmort, mc);
            final BigDecimal balance = ba.balances().get(i).getAmount();
            result.add(new ProjectedPayment(periodNo, periodDate, paymentsLeft, money(periodExpectedPayment), money(safeRunningExpected),
                    safeDf, money(npvValue), money(balance), money(safeExpectedAmort), money(netAmortization),
                    hasPositivePayment ? money(periodPayment) : null, actualAmortization != null ? money(actualAmortization) : null,
                    incomeModification != null ? money(incomeModification) : null, money(deferredBalance)));
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

    private BigDecimal resolveNpvSource(final boolean hasPositivePayment, final BigDecimal periodPayment, final LocalDate periodDate,
            final BigDecimal safeRunningExpected) {
        if (hasPositivePayment) {
            return periodPayment;
        }
        if (calculatedTillDate != null && periodDate.isBefore(calculatedTillDate)) {
            return BigDecimal.ZERO;
        }
        return safeRunningExpected;
    }

    private static BigDecimal amountOrZero(final Money value) {
        return value != null && value.getAmount() != null ? value.getAmount() : BigDecimal.ZERO;
    }

    private ProjectedPayment createDisbursementPayment() {
        final Money negDisbursement = netDisbursementAmount.negated(mc);
        return new ProjectedPayment(0, expectedDisbursementDate, 0L, negDisbursement, null, BigDecimal.ONE, negDisbursement,
                netDisbursementAmount, null, null, null, null, null, discountFeeAmount);
    }

    /**
     * {@code balance[i] = balance[i-1]×(1+EIR) - expectedPayment}<br>
     * {@code expectedAmort[i] = balance[i] + expectedPayment - balance[i-1]}
     */
    private BalancesAndAmortizations computeBalancesAndAmortizations() {
        final int totalTerm = effectiveTotalTerm();
        final List<Money> balances = new ArrayList<>(totalTerm);
        final List<Money> expectedAmortizations = new ArrayList<>(totalTerm);
        BigDecimal prevBalance = netDisbursementAmount.getAmount();
        for (int i = 0; i < totalTerm; i++) {
            final int dayIndex = i + 1;
            final RateSegment seg = segmentForDay(dayIndex);
            // At segment boundary, reset balance to segment's net disbursement
            if (seg != null && seg.startDayIndex() == dayIndex) {
                prevBalance = seg.netDisbursementAtSplit().getAmount();
            }
            final BigDecimal eir = seg != null ? seg.effectiveInterestRate() : effectiveInterestRate;
            final BigDecimal payment = seg != null ? seg.expectedPaymentAmount().getAmount() : expectedPaymentAmount.getAmount();
            final BigDecimal onePlusRate = BigDecimal.ONE.add(eir, mc);
            final BigDecimal balance = prevBalance.multiply(onePlusRate, mc).subtract(payment, mc);
            balances.add(money(balance));
            expectedAmortizations.add(money(balance.add(payment, mc).subtract(prevBalance, mc)));
            prevBalance = balance;
        }
        return new BalancesAndAmortizations(balances, expectedAmortizations);
    }

    private PaymentAnalysis analyzePayments(final List<BigDecimal> payments, final int appliedCount) {
        BigDecimal shortfall = BigDecimal.ZERO;
        BigDecimal excess = BigDecimal.ZERO;
        for (int i = 0; i < appliedCount; i++) {
            final BigDecimal payment = payments.get(i);
            final BigDecimal expectedPayment = expectedPaymentForDay(i + 1);
            if (payment == null || payment.signum() == 0) {
                shortfall = shortfall.add(expectedPayment, mc);
                continue;
            }
            final BigDecimal diff = payment.subtract(expectedPayment, mc);
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
        final List<BigDecimal> result = new ArrayList<>(appliedCount);
        BigDecimal cursor = BigDecimal.ZERO;
        for (int i = 0; i < appliedCount; i++) {
            final BigDecimal periodPayment = payments.get(i);
            final BigDecimal expectedPayment = expectedPaymentForDay(i + 1);
            final BigDecimal periodsConsumed = periodPayment != null && periodPayment.signum() > 0
                    && expectedPayment.compareTo(BigDecimal.ZERO) != 0 ? periodPayment.divide(expectedPayment, mc) : BigDecimal.ZERO;
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
        final int totalTerm = effectiveTotalTerm();
        final List<BigDecimal> running = new ArrayList<>(totalTerm);
        for (int i = 0; i < totalTerm; i++) {
            running.add(expectedPaymentForDay(i + 1));
        }
        BigDecimal remainingExcess = excess;
        for (int i = totalTerm - 1; i >= 0 && remainingExcess.signum() > 0; i--) {
            final BigDecimal reduction = remainingExcess.min(running.get(i));
            running.set(i, running.get(i).subtract(reduction, mc));
            remainingExcess = remainingExcess.subtract(reduction, mc);
        }
        return running;
    }

    private BigDecimal buildTailPeriodsAndComputeNpv(final List<ProjectedPayment> tailPayments, final BigDecimal shortfall,
            final int appliedCount) {
        final int totalTerm = effectiveTotalTerm();
        BigDecimal tailNpv = BigDecimal.ZERO;
        BigDecimal remaining = shortfall;
        int tailIndex = 0;
        while (remaining.signum() > 0) {
            final int periodNo = totalTerm + tailIndex + 1;
            final BigDecimal tailExpectedPayment = expectedPaymentForDay(totalTerm); // use last segment's payment
            final long dl = paymentsLeft(periodNo, appliedCount);
            final BigDecimal df = safeDiscountFactor(dl, totalTerm);
            final BigDecimal forecast = remaining.min(tailExpectedPayment);
            final BigDecimal npv = MathUtil.negativeToZero(forecast.multiply(df, mc));
            tailNpv = tailNpv.add(npv, mc);
            tailPayments.add(new ProjectedPayment(periodNo, expectedDisbursementDate.plusDays(periodNo), dl, null, money(forecast), df,
                    money(npv), null, null, money(BigDecimal.ZERO), null, null, null, null));
            remaining = remaining.subtract(forecast, mc);
            tailIndex++;
        }
        return tailNpv;
    }

    /** {@code totalNetAmortization = -netDisbursementAmount + sum(npvSource × DF) + tailNpv} */
    private BigDecimal computeTotalNetAmortization(final List<BigDecimal> payments, final List<BigDecimal> runningExpected,
            final int appliedCount, final BigDecimal tailNpv) {
        final int totalTerm = effectiveTotalTerm();
        BigDecimal total = netDisbursementAmount.getAmount().negate();
        for (int i = 0; i < totalTerm; i++) {
            final BigDecimal npvSource = payments.get(i) != null ? payments.get(i) : runningExpected.get(i);
            final BigDecimal df = safeDiscountFactor(paymentsLeft(i + 1, appliedCount), i + 1);
            total = total.add(npvSource.multiply(df, mc), mc);
        }
        return total.add(tailNpv, mc);
    }

    private BigDecimal safeDiscountFactor(final long paymentsLeft, final int dayIndex) {
        final BigDecimal eir = eirForDay(dayIndex);
        final BigDecimal df = TvmFunctions.discountFactor(eir, paymentsLeft, mc);
        return df.signum() <= 0 ? BigDecimal.ONE : df;
    }

    private long paymentsLeft(final int periodNumber, final int appliedCount) {
        final RateSegment seg = segmentForDay(periodNumber);
        final int segmentRelativePeriod = seg != null ? periodNumber - seg.startDayIndex() + 1 : periodNumber;
        return Math.max(0L, (long) segmentRelativePeriod - appliedCount);
    }

    private RateSegment segmentForDay(final int dayIndex) {
        if (rateSegments == null || rateSegments.isEmpty()) {
            return null;
        }
        RateSegment active = null;
        for (final RateSegment seg : rateSegments) {
            if (seg.startDayIndex() <= dayIndex) {
                active = seg;
            } else {
                break;
            }
        }
        return active;
    }

    private BigDecimal eirForDay(final int dayIndex) {
        final RateSegment seg = segmentForDay(dayIndex);
        return seg != null ? seg.effectiveInterestRate() : effectiveInterestRate;
    }

    private BigDecimal expectedPaymentForDay(final int dayIndex) {
        final RateSegment seg = segmentForDay(dayIndex);
        return seg != null ? seg.expectedPaymentAmount().getAmount() : expectedPaymentAmount.getAmount();
    }

    private BalancesAndAmortizations computeBaseBalancesUpTo(final int upToDayIndex) {
        final BigDecimal onePlusRate = BigDecimal.ONE.add(effectiveInterestRate, mc);
        final BigDecimal basePayment = expectedPaymentAmount.getAmount();
        final List<Money> balances = new ArrayList<>(upToDayIndex);
        final List<Money> expectedAmortizations = new ArrayList<>(upToDayIndex);
        BigDecimal prevBalance = netDisbursementAmount.getAmount();
        for (int i = 0; i < upToDayIndex; i++) {
            final BigDecimal balance = prevBalance.multiply(onePlusRate, mc).subtract(basePayment, mc);
            balances.add(money(balance));
            expectedAmortizations.add(money(balance.add(basePayment, mc).subtract(prevBalance, mc)));
            prevBalance = balance;
        }
        return new BalancesAndAmortizations(balances, expectedAmortizations);
    }

    private Money money(final BigDecimal amount) {
        return Money.of(currency, amount, mc);
    }

    private record BalancesAndAmortizations(List<Money> balances, List<Money> expectedAmortizations) {
    }

    private record PaymentAnalysis(BigDecimal shortfall, BigDecimal excess) {
    }

    public record ActualPayment(LocalDate date, Money amount) {
    }

    public record RateSegment(int startDayIndex, Money expectedPaymentAmount, int segmentTerm, BigDecimal effectiveInterestRate,
            Money netDisbursementAtSplit, Money discountAtSplit) {
    }

    public static String getModelVersion() {
        return MODEL_VERSION;
    }
}
