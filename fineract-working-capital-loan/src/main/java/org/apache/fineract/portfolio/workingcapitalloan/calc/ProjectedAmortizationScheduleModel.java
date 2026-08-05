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
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.serialization.gson.JsonExclude;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
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
 * <li>{@link #applyRateChange} — apply a mid-lifecycle rate change; adds a {@link RateSegment} and rebuilds the payment
 * list in-place</li>
 * <li>Discount fee adjustment — regenerate schedule with the new discount and re-apply actual payments only</li>
 * </ol>
 */
@Getter
@Accessors(fluent = true)
@Slf4j
public final class ProjectedAmortizationScheduleModel {

    private static final String MODEL_VERSION = "5";

    /**
     * Cap on Total Days: beyond this the schedule materialises an unreasonable number of rows and the EIR is
     * meaningless.
     */
    public static final int MAX_CALCULABLE_TOTAL_DAYS = 100_000;

    @SerializedName(value = "discountFeeAmount", alternate = "originationFeeAmount")
    private final Money discountFeeAmount;
    private final Money netDisbursementAmount;
    private final Money totalPaymentVolume;
    private final BigDecimal periodPaymentRate;
    private final int npvDayCount;
    private final LocalDate expectedDisbursementDate;

    /** {@code (TPV × periodPaymentRate) / npvDayCount} — constant across all but the final payment. */
    private final Money expectedPaymentAmount;

    /**
     * Final-period payment: the remainder {@code (netDisbursementAmount + discountFeeAmount) − (originalPaymentNumber −
     * 1) × expectedPaymentAmount}. Equals {@link #expectedPaymentAmount} when the schedule divides evenly.
     */
    private final Money finalPaymentAmount;

    /** {@code roundUp((netDisbursementAmount + discountFeeAmount) / expectedPaymentAmount)} */
    @SerializedName(value = "originalPaymentNumber", alternate = "loanTerm")
    private final int originalPaymentNumber;

    /**
     * Periodic effective rate, solved as {@code IRR([−netDisbursement, expectedPayment × (n−1), finalPayment])} and
     * seeded with a linear rate estimate. Because the cash flow carries the smaller final remainder payment rather than
     * assuming a uniform payment throughout, this is zero exactly when the loan carries no discount fee.
     */
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
    private final List<PrincipalAdjustment> principalAdjustments;

    @Getter(AccessLevel.NONE)
    @SerializedName(value = "projectedPayments", alternate = "payments")
    private List<ProjectedPayment> projectedPayments;

    @Getter(AccessLevel.NONE)
    private List<ProjectedPayment> originalProjectedPayments;

    private LocalDate calculatedTillDate;

    private ProjectedAmortizationScheduleModel(final Money discountFeeAmount, final Money netDisbursementAmount,
            final Money totalPaymentVolume, final BigDecimal periodPaymentRate, final int npvDayCount,
            final LocalDate expectedDisbursementDate, final Money expectedPaymentAmount, final Money finalPaymentAmount,
            final int originalPaymentNumber, final BigDecimal effectiveInterestRate, final MathContext mc, final CurrencyData currency,
            final LocalDate currentBusinessDate) {
        this.discountFeeAmount = discountFeeAmount;
        this.netDisbursementAmount = netDisbursementAmount;
        this.totalPaymentVolume = totalPaymentVolume;
        this.periodPaymentRate = periodPaymentRate;
        this.npvDayCount = npvDayCount;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.expectedPaymentAmount = expectedPaymentAmount;
        this.finalPaymentAmount = finalPaymentAmount;
        this.originalPaymentNumber = originalPaymentNumber;
        this.effectiveInterestRate = effectiveInterestRate;
        this.mc = mc;
        this.currency = currency;
        this.actualPayments = new ArrayList<>();
        this.rateSegments = new ArrayList<>();
        this.principalAdjustments = new ArrayList<>();
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
        this.totalPaymentVolume = null;
        this.periodPaymentRate = null;
        this.npvDayCount = 0;
        this.expectedDisbursementDate = null;
        this.expectedPaymentAmount = null;
        this.finalPaymentAmount = null;
        this.originalPaymentNumber = 0;
        this.effectiveInterestRate = null;
        this.mc = mc;
        this.currency = currency;
        this.actualPayments = new ArrayList<>();
        this.rateSegments = new ArrayList<>();
        this.principalAdjustments = new ArrayList<>();
        this.projectedPayments = List.of();
        this.originalProjectedPayments = List.of();
        this.calculatedTillDate = null;
    }

    public List<ProjectedPayment> projectedPayments() {
        return projectedPayments;
    }

    /**
     * The scheduled maturity is the date of the last real projected payment (a period with {@code paymentNo > 0}, i.e.
     * excluding the disbursement row). Returns {@code null} when the schedule has no real payments.
     */
    public LocalDate scheduledMaturityDate() {
        if (projectedPayments == null) {
            return null;
        }
        return projectedPayments.stream().filter(payment -> payment.paymentNo() > 0).map(ProjectedPayment::date).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    public List<ProjectedPayment> originalProjectedPayments() {
        return originalProjectedPayments;
    }

    public List<RateSegment> rateSegments() {
        return rateSegments != null ? List.copyOf(rateSegments) : List.of();
    }

    public List<PrincipalAdjustment> principalAdjustments() {
        return principalAdjustments != null ? List.copyOf(principalAdjustments) : List.of();
    }

    /**
     * Records principal re-injected into the loan on {@code adjustmentDate} by an over-refunding credit balance refund,
     * and rebuilds the payment list.
     *
     * <p>
     * The adjustment is deliberately kept out of the amortization: it does not enter the EIR, the NPV, the expected
     * balance or the payment analysis. It only raises {@code expectedPaymentAmount} of its own period, because the
     * re-injected principal is owned by the loan balance ({@code principalAdjustment} / {@code principalOutstanding}),
     * while this model stays the projection of the originally disbursed amount.
     *
     * <p>
     * The date is clamped into the amortization term the same way {@link #applyPayment} clamps a payment date. A credit
     * balance refund lands on an overpaid loan, so its date is often at or past maturity; without the clamp the
     * adjustment would match no period at all and silently vanish from the projection while the balance still owed it.
     * Unlike a payment it does not advance {@code calculatedTillDate}: that would move which periods count as passed
     * and so change the NPV source, and the adjustment must leave the amortization alone.
     */
    public void applyPrincipalAdjustment(final LocalDate adjustmentDate, final BigDecimal amount) {
        Objects.requireNonNull(adjustmentDate, "adjustmentDate");
        Objects.requireNonNull(amount, "amount");
        principalAdjustments.add(new PrincipalAdjustment(calculateAllocationDate(adjustmentDate, currentFirstPeriodDayOffset()), //
                money(amount)));
        rebuildPayments();
    }

    /** Snapshot of repayments already applied; used when restating the schedule after a discount fee adjustment. */
    public List<ActualPayment> snapshotActualPayments() {
        return List.copyOf(actualPayments);
    }

    /**
     * Snapshot of the principal adjustments already applied; used alongside {@link #snapshotActualPayments()} when
     * restating the schedule after a discount fee adjustment, so an over-refund CBR's adjustment survives the restate.
     */
    public List<PrincipalAdjustment> snapshotPrincipalAdjustments() {
        return List.copyOf(principalAdjustments);
    }

    /** Sum of {@code actualAmortizationAmount} across all applied payment periods (paymentNo &gt; 0). */
    public BigDecimal totalActualAmortization() {
        if (projectedPayments == null) {
            return BigDecimal.ZERO;
        }
        return projectedPayments.stream().filter(p -> p.paymentNo() > 0 && p.actualAmortizationAmount() != null)
                .map(p -> p.actualAmortizationAmount().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalActualAmortizationWithDiscount(final BigDecimal asOfDiscount) {
        if (asOfDiscount == null || discountFeeAmount == null || asOfDiscount.compareTo(discountFeeAmount.getAmount()) == 0) {
            return totalActualAmortization();
        }
        return withDiscount(asOfDiscount).totalActualAmortization();
    }

    private ProjectedAmortizationScheduleModel withDiscount(final BigDecimal asOfDiscount) {
        final ProjectedAmortizationScheduleModel asOfModel = generate(asOfDiscount, netDisbursementAmount.getAmount(),
                totalPaymentVolume.getAmount(), periodPaymentRate, npvDayCount, expectedDisbursementDate, mc, currency,
                calculatedTillDate != null ? calculatedTillDate : expectedDisbursementDate);
        asOfModel.copyPrincipalAdjustmentsFrom(this);
        for (final ActualPayment payment : actualPayments) {
            asOfModel.applyPayment(payment.date(), payment.amount().getAmount());
        }
        if (calculatedTillDate != null) {
            asOfModel.updateCalculatedTillDate(calculatedTillDate);
            asOfModel.rebuildPayments();
        }
        return asOfModel;
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

    /**
     * {@code (TPV × periodPaymentRate) / npvDayCount / 100}, rounded to the loan currency's decimal places. Rounding is
     * intrinsic: the daily payment is what the borrower is billed, so it must be an amount actually payable in the loan
     * currency (matches the reference model's {@code ROUND(TPV*rate/dayCount, 2)} and the platform's Money rounding).
     *
     * <p>
     * A positive payment rate always bills something, so when the exact amount is positive but too small to survive the
     * rounding it is raised to one minor currency unit rather than collapsing to zero. Zero is not a payment the
     * borrower can make: it would leave the schedule with no way to repay the balance, and dividing the gross payable
     * by it to derive the term is undefined. Only this degenerate case is affected — any amount that already rounds to
     * a payable value is untouched.
     */
    private static BigDecimal computeDailyPayment(final BigDecimal totalPaymentValue, final BigDecimal periodPaymentRate,
            final int npvDayCount, final int currencyScale, final MathContext mc) {
        final BigDecimal exact = totalPaymentValue.multiply(periodPaymentRate, mc).divide(BigDecimal.valueOf(npvDayCount), mc)
                .divide(BigDecimal.valueOf(100), mc);
        final BigDecimal rounded = exact.setScale(currencyScale, mc.getRoundingMode());
        if (rounded.signum() == 0 && exact.signum() > 0) {
            return BigDecimal.ONE.movePointLeft(currencyScale);
        }
        return rounded;
    }

    /**
     * Feasibility pre-check reusing {@link #generate}'s exact formulas, without building the schedule. Null mandatory
     * inputs are treated as calculable so the caller's mandatory-field validation reports them instead.
     */
    public static boolean isEirCalculable(final BigDecimal discountFeeAmount, final BigDecimal netDisbursementAmount,
            final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate, final int npvDayCount, MonetaryCurrency currency,
            final MathContext mc) {
        if (discountFeeAmount == null || netDisbursementAmount == null || totalPaymentVolume == null || periodPaymentRate == null) {
            return true;
        }
        if (netDisbursementAmount.signum() <= 0 || npvDayCount <= 0) {
            return false;
        }
        final BigDecimal dailyPayment = computeDailyPayment(totalPaymentVolume, periodPaymentRate, npvDayCount,
                currency.getDigitsAfterDecimal(), mc);
        if (dailyPayment.signum() <= 0) {
            return false;
        }
        final BigDecimal totalDays = netDisbursementAmount.add(discountFeeAmount, mc).divide(dailyPayment, mc).setScale(0, RoundingMode.UP);
        if (totalDays.signum() <= 0 || totalDays.compareTo(BigDecimal.valueOf(MAX_CALCULABLE_TOTAL_DAYS)) > 0) {
            return false;
        }
        final BigDecimal grossPayable = netDisbursementAmount.add(discountFeeAmount, mc);
        final int paymentNumber = grossPayable.divide(dailyPayment, mc).setScale(0, RoundingMode.UP).intValueExact();
        if (paymentNumber <= 0) {
            throw new IllegalArgumentException("computed paymentNumber must be positive, got: " + paymentNumber);
        }

        // The final period pays only the remainder of the gross payable after the (n-1) full daily payments. When the
        // schedule divides evenly this equals the daily payment.
        final BigDecimal finalPayment = grossPayable.subtract(dailyPayment.multiply(BigDecimal.valueOf(paymentNumber - 1L), mc), mc);
        try {
            TvmFunctions.irr(buildEirCashFlows(netDisbursementAmount, dailyPayment, finalPayment, paymentNumber), mc);
        } catch (final ArithmeticException | IllegalArgumentException | IllegalStateException e) {
            return false;
        }
        return true;
    }

    public static ProjectedAmortizationScheduleModel generate(final BigDecimal discountFeeAmount, final BigDecimal netDisbursementAmount,
            final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate, final int npvDayCount,
            final LocalDate expectedDisbursementDate, final MathContext mc, final CurrencyData currency, final LocalDate currentDate) {

        Objects.requireNonNull(discountFeeAmount, "discountFeeAmount");
        Objects.requireNonNull(netDisbursementAmount, "netDisbursementAmount");
        Objects.requireNonNull(totalPaymentVolume, "totalPaymentVolume");
        Objects.requireNonNull(periodPaymentRate, "periodPaymentRate");
        Objects.requireNonNull(expectedDisbursementDate, "expectedDisbursementDate");
        Objects.requireNonNull(currency, "currency");
        if (netDisbursementAmount.signum() <= 0) {
            throw new IllegalArgumentException("netDisbursementAmount must be positive");
        }
        if (npvDayCount <= 0) {
            throw new IllegalArgumentException("npvDayCount must be positive");
        }

        final ScheduleParams params = computeScheduleParams(netDisbursementAmount, discountFeeAmount, totalPaymentVolume, periodPaymentRate,
                npvDayCount, currency, mc);

        return new ProjectedAmortizationScheduleModel(Money.of(currency, discountFeeAmount, mc),
                Money.of(currency, netDisbursementAmount, mc), Money.of(currency, totalPaymentVolume, mc), periodPaymentRate, npvDayCount,
                expectedDisbursementDate, Money.of(currency, params.dailyPayment(), mc), Money.of(currency, params.finalPayment(), mc),
                params.paymentNumber(), params.eir(), mc, currency, currentDate);
    }

    /**
     * Derives the amortization parameters for a schedule (or a rate-change sub-schedule): the currency-rounded daily
     * payment, the round-up payment count, the smaller remainder final payment, and the EIR as the IRR of the resulting
     * non-uniform cash-flow series. Shared by {@link #generate} and {@link #applyRateChange} so the base schedule and
     * every rate segment are computed identically.
     */
    private static ScheduleParams computeScheduleParams(final BigDecimal netDisbursement, final BigDecimal discountFee,
            final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate, final int npvDayCount, final CurrencyData currency,
            final MathContext mc) {
        // The daily payment is rounded to the loan currency, so every downstream value — payment number, remainder, EIR
        // and balances — is derived from the same amount the borrower is actually billed.
        final BigDecimal dailyPayment = computeDailyPayment(totalPaymentVolume, periodPaymentRate, npvDayCount, currency.getDecimalPlaces(),
                mc);
        if (dailyPayment.signum() <= 0) {
            throw new IllegalArgumentException("daily payment must be positive (check totalPaymentVolume and periodPaymentRate)");
        }

        final BigDecimal grossPayable = netDisbursement.add(discountFee, mc);
        final int paymentNumber = grossPayable.divide(dailyPayment, mc).setScale(0, RoundingMode.UP).intValueExact();
        if (paymentNumber <= 0) {
            throw new IllegalArgumentException("computed paymentNumber must be positive, got: " + paymentNumber);
        }

        // The final period pays only the remainder of the gross payable after the (n-1) full daily payments. When the
        // schedule divides evenly this equals the daily payment.
        final BigDecimal finalPayment = grossPayable.subtract(dailyPayment.multiply(BigDecimal.valueOf(paymentNumber - 1L), mc), mc);

        // EIR is the IRR of the actual (non-uniform) cash-flow series. The IRR seeds its Newton-Raphson search from a
        // linear estimate of the series, so no separate uniform-annuity RATE solve is needed.
        final BigDecimal eir = TvmFunctions.irr(buildEirCashFlows(netDisbursement, dailyPayment, finalPayment, paymentNumber), mc);

        return new ScheduleParams(dailyPayment, paymentNumber, finalPayment, eir);
    }

    /**
     * Builds the cash-flow series for the EIR/IRR solve:
     * {@code [−netDisbursement, dailyPayment × (n−1), finalPayment]}. The day-0 flow is the negated outstanding
     * balance; the periodic payments follow, the last being the remainder.
     */
    private static List<BigDecimal> buildEirCashFlows(final BigDecimal netDisbursementAmount, final BigDecimal dailyPayment,
            final BigDecimal finalPayment, final int paymentNumber) {
        final List<BigDecimal> cashFlows = new ArrayList<>(paymentNumber + 1);
        cashFlows.add(netDisbursementAmount.negate());
        for (int i = 0; i < paymentNumber - 1; i++) {
            cashFlows.add(dailyPayment);
        }
        cashFlows.add(finalPayment);
        return cashFlows;
    }

    /** First-period offset: 0 when a disbursement-date repayment shifts the grid onto the disbursement date, else 1. */
    private int currentFirstPeriodDayOffset() {
        return hasDisbursementDatePayment() ? 0 : 1;
    }

    private boolean hasDisbursementDatePayment() {
        return actualPayments.stream().anyMatch(payment -> payment.date().equals(expectedDisbursementDate));
    }

    /** Date of payment period {@code periodNo} (1-based) using the current first-period offset. */
    private LocalDate dateOfPeriod(final int periodNo) {
        return dateOfPeriod(periodNo, currentFirstPeriodDayOffset());
    }

    /** Date of payment period {@code periodNo} (1-based) for the given first-period day offset. */
    private LocalDate dateOfPeriod(final int periodNo, final int firstPeriodDayOffset) {
        return expectedDisbursementDate.plusDays((long) periodNo - 1 + firstPeriodDayOffset);
    }

    private LocalDate calculateAllocationDate(final LocalDate paymentDate, final int firstPeriodDayOffset) {
        final LocalDate firstInstallmentDate = dateOfPeriod(1, firstPeriodDayOffset);
        final LocalDate lastInstallmentDate = dateOfPeriod(effectiveTotalTerm(), firstPeriodDayOffset);
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
        // Offset includes the payment being added now, so the range check matches the subsequent rebuild.
        final int firstPeriodDayOffset = hasDisbursementDatePayment() || paymentDate.equals(expectedDisbursementDate) ? 0 : 1;
        final LocalDate allocationDate = calculateAllocationDate(paymentDate, firstPeriodDayOffset);
        final int index = resolvePaymentIndex(allocationDate, firstPeriodDayOffset);
        if (index < 0 || index >= effectiveTotalTerm()) {
            throw new IllegalArgumentException("paymentDate " + paymentDate + " is outside the valid range ["
                    + dateOfPeriod(1, firstPeriodDayOffset) + " .. " + dateOfPeriod(effectiveTotalTerm(), firstPeriodDayOffset) + "]");
        }
        actualPayments.add(new ActualPayment(allocationDate, money(amount)));
        rebuildPayments();
    }

    public void undoPayment(final LocalDate paymentDate, final BigDecimal amount) {
        Objects.requireNonNull(paymentDate, "paymentDate");
        Objects.requireNonNull(amount, "amount");
        final int firstPeriodDayOffset = hasDisbursementDatePayment() || paymentDate.equals(expectedDisbursementDate) ? 0 : 1;
        final LocalDate allocationDate = calculateAllocationDate(paymentDate, firstPeriodDayOffset);
        Optional<ActualPayment> first = actualPayments.stream()
                .filter(p -> p.date.equals(allocationDate) && p.amount.getAmount().compareTo(amount) == 0).findFirst();
        if (first.isEmpty()) {
            throw new IllegalStateException("payment not found: date=" + paymentDate + " with amount=" + amount);
        }
        actualPayments.remove(first.get());
        rebuildPayments();
        recalculateNetAmortizationAndDeferredBalanceFrom(paymentDate);
    }

    private void updateCalculatedTillDate(final LocalDate actionDate) {
        if (calculatedTillDate == null || actionDate.isAfter(calculatedTillDate)) {
            this.calculatedTillDate = actionDate;
        }
    }

    /** Creates a new model with updated parameters, preserving applied payments. */
    public ProjectedAmortizationScheduleModel regenerate(final BigDecimal newDiscountAmount, final BigDecimal newNetAmount,
            final LocalDate newStartDate, final LocalDate currentDate) {
        final ProjectedAmortizationScheduleModel newModel = generate(newDiscountAmount, newNetAmount, totalPaymentVolume.getAmount(),
                periodPaymentRate, npvDayCount, newStartDate, mc, currency, currentDate);
        newModel.actualPayments.addAll(actualPayments);
        newModel.copyPrincipalAdjustmentsFrom(this);
        newModel.rebuildPayments();
        return newModel;
    }

    private void copyPrincipalAdjustmentsFrom(final ProjectedAmortizationScheduleModel source) {
        if (source.principalAdjustments != null) {
            this.principalAdjustments.addAll(source.principalAdjustments);
        }
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

        BigDecimal runningActualDiscountFeeBalance = amountOrZero(projectedPayments.get(fromIndex).actualDiscountFeeBalance());

        final List<ProjectedPayment> adjusted = new ArrayList<>(projectedPayments.subList(0, fromIndex + 1));
        for (int i = fromIndex + 1; i < projectedPayments.size(); i++) {
            final ProjectedPayment current = projectedPayments.get(i);
            final BigDecimal actualTotalAmortization = amountOrZero(current.actualAmortizationAmount());
            runningActualDiscountFeeBalance = runningActualDiscountFeeBalance.subtract(actualTotalAmortization, mc);

            final boolean hasPayment = current.actualPaymentAmount() != null;
            adjusted.add(new ProjectedPayment(current.paymentNo(), current.date(), current.paymentsLeft(), current.expectedPaymentAmount(),
                    current.discountFactor(), current.npvValue(), current.expectedBalance(), current.actualBalance(),
                    current.expectedAmortizationAmount(), current.actualPaymentAmount(), current.actualAmortizationAmount(),
                    current.incomeModification(), current.expectedDiscountFeeBalance(),
                    hasPayment ? money(runningActualDiscountFeeBalance) : null));
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
     *            the new period payment rate as a <strong>percentage</strong>
     * @param rateChangeDate
     *            the date the new rate takes effect (must be within model's date range); may be in the future
     * @param currentDate
     *            today's business date, capping how far {@code calculatedTillDate} may advance; {@code null} lets the
     *            effective date advance it unchecked
     */
    public void applyRateChange(final BigDecimal newPeriodPaymentRate, final LocalDate rateChangeDate, final LocalDate currentDate) {
        Objects.requireNonNull(newPeriodPaymentRate, "newPeriodPaymentRate");
        Objects.requireNonNull(rateChangeDate, "rateChangeDate");
        // A rate change proves time has advanced only as far as the day it was booked on. Letting a future effective
        // date carry calculatedTillDate with it would mark the periods between today and that date as elapsed, so the
        // catch-up machinery would bill them as missed and the NPV would be taken from the wrong periods.
        final LocalDate reachedDate = currentDate != null && rateChangeDate.isAfter(currentDate) ? currentDate : rateChangeDate;
        updateCalculatedTillDate(reachedDate);
        final int rawSplitDayIndex = (int) ChronoUnit.DAYS.between(expectedDisbursementDate, rateChangeDate);
        if (rawSplitDayIndex < 0) {
            throw new IllegalArgumentException("rateChangeDate must not be before expectedDisbursementDate");
        }

        // Segment starts on the period whose date == rateChangeDate. Convert the calendar-day rawSplitDayIndex
        // to a period number via the first-period offset (0 when a disbursement-date repayment shifted the grid),
        // else the new rate starts one day early. Clamped to the active (effective) term so a change at/after the
        // current end of an already-segmented schedule starts on the final period.
        final int splitDayIndex = Math.min(rawSplitDayIndex + (1 - currentFirstPeriodDayOffset()), effectiveTotalTerm());

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

        // Compute balance at split: if past the active term, use remaining principal; otherwise follow the currently
        // active segment chain (earlier rate changes still apply) up to the period before the split.
        final BigDecimal balanceAtSplit;
        if (rawSplitDayIndex >= effectiveTotalTerm()) {
            balanceAtSplit = netDisbursementAmount.getAmount().subtract(paymentsReceived, mc);
        } else if (splitDayIndex > 1) {
            // Balance at the end of the LAST period before the split (splitDayIndex-1), computed segment-aware so a
            // prior rate change's payment/EIR is honoured, not the base schedule's.
            final int lastPeriodBeforeSplit = splitDayIndex - 1;
            final BalancesAndAmortizations ba = computeBalancesAndAmortizations(lastPeriodBeforeSplit);
            balanceAtSplit = ba.balances().get(lastPeriodBeforeSplit - 1).getAmount();
        } else {
            balanceAtSplit = netDisbursementAmount.getAmount();
        }

        final BigDecimal origNet = netDisbursementAmount.getAmount();
        final BigDecimal origDiscount = discountFeeAmount.getAmount();
        final BigDecimal tpv = totalPaymentVolume.getAmount();

        final BigDecimal newNetDisb = balanceAtSplit;
        final BigDecimal newDiscount;
        if (rawSplitDayIndex >= effectiveTotalTerm()) {
            newDiscount = origDiscount.add(origNet, mc).subtract(balanceAtSplit, mc).subtract(paymentsReceived, mc);
        } else {
            // The gross still to be billed is measured from the start of the sub-schedule the split falls in, not from
            // disbursement. A past-term rate change re-injects the unpaid principal as a fresh net + discount, so from
            // that segment onwards the original pair no longer describes what is left to consume - and summing payments
            // across the rebase counts the base schedule's periods and the segment's own against the same total, which
            // drives it negative and yields a negative term.
            //
            // Segments at or after the split were dropped above, so the active one is whichever covers the last period
            // before it; with none (a plain base schedule, or a day-0/day-1 change) this reduces to disbursement and
            // the
            // original pair, leaving single-change results untouched.
            final RateSegment segmentAtSplit = splitDayIndex > 1 ? segmentForDay(splitDayIndex - 1) : null;
            final int consumedFromDay = segmentAtSplit != null ? segmentAtSplit.startDayIndex() : 1;
            final BigDecimal grossAtSegmentStart = segmentAtSplit != null
                    ? segmentAtSplit.netDisbursementAtSplit().getAmount().add(segmentAtSplit.discountAtSplit().getAmount(), mc)
                    : origNet.add(origDiscount, mc);

            BigDecimal consumedBeforeSplit = BigDecimal.ZERO;
            for (int day = consumedFromDay; day < splitDayIndex; day++) {
                consumedBeforeSplit = consumedBeforeSplit.add(expectedPaymentForDay(day), mc);
            }
            // Stay entirely on the expected track: balanceAtSplit and consumedBeforeSplit both come from the projected
            // schedule, which already assumes each period before the split was paid. Deducting the actual payments
            // received here as well would remove them a second time and understate the fee the segment still has to
            // earn. (The past-term branch above rebases on an actual-payment balance, so it deducts them there.)
            final BigDecimal remainingTotal = grossAtSegmentStart.subtract(consumedBeforeSplit, mc);
            newDiscount = remainingTotal.subtract(balanceAtSplit, mc);
        }
        final int scale = currency.getDecimalPlaces();
        final BigDecimal newDailyPayment = computeDailyPayment(tpv, newPeriodPaymentRate, npvDayCount, currency.getDecimalPlaces(), mc)
                .setScale(scale, mc.getRoundingMode());
        final BigDecimal fractionalTotalDays = newNetDisb.add(newDiscount, mc).divide(newDailyPayment, mc).setScale(scale,
                mc.getRoundingMode());
        // Checked on the BigDecimal so int overflow cannot slip past the cap; the EIR solver may still succeed on an
        // over-cap term (zero-rate shortcut), so relying on the rate() call to fail is not enough.
        if (fractionalTotalDays.compareTo(BigDecimal.valueOf(MAX_CALCULABLE_TOTAL_DAYS)) > 0) {
            throw new IllegalStateException("rate change produces a term of " + fractionalTotalDays + " days, above the calculable cap of "
                    + MAX_CALCULABLE_TOTAL_DAYS);
        }

        if (newNetDisb.signum() <= 0) {
            throw new IllegalArgumentException("balance at split must be positive for rate change");
        }

        // The segment is a fresh sub-schedule of the remaining balance + discount at the new rate: round-up term,
        // remainder final payment and IRR EIR, computed identically to the base schedule.
        final ScheduleParams segment = computeScheduleParams(newNetDisb, newDiscount, tpv, newPeriodPaymentRate, npvDayCount, currency, mc);

        rateSegments.add(new RateSegment(splitDayIndex, money(segment.dailyPayment()), segment.paymentNumber(), segment.eir(),
                money(newNetDisb), money(newDiscount), money(segment.finalPayment())));
        rateSegments.sort(Comparator.comparingInt(RateSegment::startDayIndex));

        rebuildPayments();
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

        BigDecimal cumulativeExpectedAmort = BigDecimal.ZERO;
        for (int i = 0; i < totalTerm; i++) {
            final int periodNo = i + 1;
            final RateSegment seg = segmentForDay(periodNo);
            final long segRelativePeriod = seg != null ? periodNo - seg.startDayIndex() + 1 : periodNo;
            final BigDecimal periodExpectedPayment = MathUtil.negativeToZero(expectedPaymentForDay(periodNo));
            final BigDecimal safeDf = safeDiscountFactor(segRelativePeriod, periodNo);
            final BigDecimal npvValue = MathUtil.negativeToZero(periodExpectedPayment.multiply(safeDf, mc));
            final BigDecimal safeExpectedAmort = ba.expectedAmortizations().get(i).getAmount().min(discountFee);
            final BigDecimal balance = ba.balances().get(i).getAmount();
            cumulativeExpectedAmort = cumulativeExpectedAmort.add(safeExpectedAmort, mc);
            final BigDecimal expectedDiscFeeBalance = discountFee.subtract(cumulativeExpectedAmort, mc);

            result.add(new ProjectedPayment(periodNo, dateOfPeriod(periodNo), segRelativePeriod, money(periodExpectedPayment), safeDf,
                    money(npvValue), money(balance), null, money(safeExpectedAmort), null, null, null, money(expectedDiscFeeBalance),
                    null));
        }

        this.originalProjectedPayments = List.copyOf(result);
    }

    private Map<LocalDate, BigDecimal> aggregatePaymentsByDate() {
        final Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        if (!actualPayments.isEmpty() && calculatedTillDate != null) {
            final LocalDate firstInstallmentDate = dateOfPeriod(1);
            final LocalDate lastInstallmentDate = dateOfPeriod(effectiveTotalTerm());
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
            final LocalDate paymentDate = dateOfPeriod(i + 1);
            result.add(paymentsByDate.get(paymentDate));
        }
        return result;
    }

    private int resolvePaymentIndex(final LocalDate date, final int firstPeriodDayOffset) {
        return (int) ChronoUnit.DAYS.between(expectedDisbursementDate, date) - firstPeriodDayOffset;
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
        buildTailPeriodsAndComputeNpv(tailPayments, shortfallForTail, appliedCount);

        final BigDecimal discountFee = discountFeeAmount.getAmount();
        final BigDecimal netDisb = netDisbursementAmount.getAmount();

        final List<ProjectedPayment> result = new ArrayList<>(effectiveTotalTerm() + 2 + tailPayments.size());
        result.add(createDisbursementPayment());

        BigDecimal cumulativeActualAmort = BigDecimal.ZERO;
        BigDecimal cumulativeExpectedAmort = BigDecimal.ZERO;
        BigDecimal runningActualBalance = netDisb;
        for (int i = 0; i < effectiveTotalTerm(); i++) {
            final int periodNo = i + 1;
            final LocalDate periodDate = dateOfPeriod(periodNo);
            final BigDecimal periodPayment = payments.get(i);
            final boolean hasPositivePayment = periodPayment != null && periodPayment.signum() > 0;
            final boolean passedPeriod = calculatedTillDate != null && periodDate.isBefore(calculatedTillDate);
            final long paymentsLeft = paymentsLeft(periodNo, appliedCount);
            final BigDecimal safeDf = safeDiscountFactor(paymentsLeft, periodNo);
            final BigDecimal periodExpectedPayment = MathUtil.negativeToZero(expectedPaymentForDay(periodNo));
            final BigDecimal safeRunningExpected = MathUtil.negativeToZero(runningExpected.get(i));
            final BigDecimal npvSource = resolveNpvSource(hasPositivePayment, passedPeriod, periodPayment, safeRunningExpected);
            final BigDecimal npvValue = MathUtil.negativeToZero(npvSource.multiply(safeDf, mc));
            final BigDecimal safeExpectedAmort = ba.expectedAmortizations().get(i).getAmount().min(discountFee);

            final BigDecimal actualAmortization;
            final BigDecimal incomeModification;

            final RateSegment seg = segmentForDay(periodNo);
            // At segment boundary, reset balance to segment's net disbursement
            if (seg != null && seg.startDayIndex() == periodNo) {
                runningActualBalance = seg.netDisbursementAtSplit().getAmount();
            }

            if (hasPositivePayment) {
                actualAmortization = actualAmortizations.get(i);
                cumulativeActualAmort = cumulativeActualAmort.add(actualAmortization, mc).min(discountFee);
                runningActualBalance = runningActualBalance.subtract(periodPayment, mc).add(actualAmortization, mc);
                incomeModification = actualAmortization.subtract(safeExpectedAmort, mc);
            } else {
                actualAmortization = null;
                incomeModification = null;
            }

            cumulativeExpectedAmort = cumulativeExpectedAmort.add(safeExpectedAmort, mc);
            final BigDecimal expectedDiscountFeeBalance = discountFee.subtract(cumulativeExpectedAmort, mc);
            final BigDecimal actualDiscountFeeBalance = discountFee.subtract(cumulativeActualAmort, mc);
            final BigDecimal balance = ba.balances().get(i).getAmount();
            result.add(new ProjectedPayment(periodNo, periodDate, paymentsLeft, money(periodExpectedPayment), safeDf, money(npvValue),
                    money(balance), resolveActualBalance(hasPositivePayment, passedPeriod, runningActualBalance), money(safeExpectedAmort),
                    resolveActualAmount(hasPositivePayment, passedPeriod, periodPayment),
                    resolveActualAmount(hasPositivePayment, passedPeriod, actualAmortization),
                    incomeModification != null ? money(incomeModification) : null, money(expectedDiscountFeeBalance),
                    resolveActualBalance(hasPositivePayment, passedPeriod, actualDiscountFeeBalance)));
        }

        result.addAll(tailPayments);

        final Map<LocalDate, BigDecimal> adjustmentsByDate = aggregatePrincipalAdjustmentsByDate();
        addPrincipalAdjustments(result, adjustmentsByDate);
        trimTrailingZeroNpvPayments(result, adjustmentsByDate.keySet());

        return result;
    }

    private Map<LocalDate, BigDecimal> aggregatePrincipalAdjustmentsByDate() {
        if (principalAdjustments == null || principalAdjustments.isEmpty()) {
            return Map.of();
        }
        final Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        for (final PrincipalAdjustment adjustment : principalAdjustments) {
            result.merge(adjustment.date(), adjustment.amount().getAmount(), (a, b) -> a.add(b, mc));
        }
        return result;
    }

    /**
     * Adds each principal adjustment to the expected payment of the period falling on its own date. An adjustment dated
     * outside the amortization term has no period to carry it and is left out of the projection rather than attached to
     * an unrelated period; the loan balance still owns the amount.
     */
    private void addPrincipalAdjustments(final List<ProjectedPayment> payments, final Map<LocalDate, BigDecimal> adjustmentsByDate) {
        if (adjustmentsByDate.isEmpty()) {
            return;
        }
        for (int i = 0; i < payments.size(); i++) {
            final ProjectedPayment payment = payments.get(i);
            final BigDecimal adjustment = payment.paymentNo() > 0 ? adjustmentsByDate.get(payment.date()) : null;
            if (adjustment == null) {
                continue;
            }
            payments.set(i, withExpectedPaymentAmount(payment, payment.expectedPaymentAmount().getAmount().add(adjustment, mc)));
        }
    }

    private ProjectedPayment withExpectedPaymentAmount(final ProjectedPayment payment, final BigDecimal expectedPaymentAmount) {
        return new ProjectedPayment(payment.paymentNo(), payment.date(), payment.paymentsLeft(), money(expectedPaymentAmount),
                payment.discountFactor(), payment.npvValue(), payment.expectedBalance(), payment.actualBalance(),
                payment.expectedAmortizationAmount(), payment.actualPaymentAmount(), payment.actualAmortizationAmount(),
                payment.incomeModification(), payment.expectedDiscountFeeBalance(), payment.actualDiscountFeeBalance());
    }

    /**
     * Drops the trailing periods the loan will never see, keeping a period that carries a principal adjustment: it is
     * due even though nothing of the projected disbursement is left to discount on it.
     */
    private static void trimTrailingZeroNpvPayments(final List<ProjectedPayment> payments, final Set<LocalDate> adjustedDates) {
        while (payments.size() > 1) {
            final ProjectedPayment last = payments.getLast();
            if (last.npvValue() != null && last.npvValue().isZero() && !adjustedDates.contains(last.date())) {
                payments.removeLast();
            } else {
                break;
            }
        }
    }

    private BigDecimal resolveNpvSource(final boolean hasPositivePayment, final boolean passedPeriod, final BigDecimal periodPayment,
            final BigDecimal safeRunningExpected) {
        if (hasPositivePayment) {
            return periodPayment;
        }
        if (passedPeriod) {
            return BigDecimal.ZERO;
        }
        return safeRunningExpected;
    }

    private Money resolveActualBalance(final boolean hasPositivePayment, final boolean passedPeriod, final BigDecimal actualBalance) {
        if (hasPositivePayment || passedPeriod) {
            return money(actualBalance);
        }
        return null;
    }

    private Money resolveActualAmount(final boolean hasPositivePayment, final boolean passedPeriod, final BigDecimal actualAmount) {
        if (hasPositivePayment) {
            return money(actualAmount);
        }
        if (passedPeriod) {
            return Money.zero(currency, mc);
        }
        return null;
    }

    private static BigDecimal amountOrZero(final Money value) {
        return value != null && value.getAmount() != null ? value.getAmount() : BigDecimal.ZERO;
    }

    private ProjectedPayment createDisbursementPayment() {
        final Money negDisbursement = netDisbursementAmount.negated(mc);
        return new ProjectedPayment(0, expectedDisbursementDate, 0L, negDisbursement, BigDecimal.ONE, negDisbursement,
                netDisbursementAmount, netDisbursementAmount, null, null, null, null, discountFeeAmount, discountFeeAmount);
    }

    /**
     * {@code balance[i] = balance[i-1]×(1+EIR) - expectedPayment}<br>
     * {@code expectedAmort[i] = balance[i] + expectedPayment - balance[i-1]}
     */
    private BalancesAndAmortizations computeBalancesAndAmortizations() {
        final BalancesAndAmortizations ba = computeBalancesAndAmortizations(effectiveTotalTerm());
        // Per-period amortizations (the interest earned each day) sum to the discount fee by construction — a rate
        // change preserves the total (the segment's discount is derived to keep it) — but rounding each to the currency
        // scale leaves a few-cent drift, so the deferred (discount-fee) balance would not close to exactly zero at
        // payoff. Settle the drift on the final period(s) so the amortizations sum exactly to the fee.
        settleAmortizationRoundingToFee(ba.expectedAmortizations());
        return ba;
    }

    /**
     * Walks the declining-balance recursion {@code balance[i] = balance[i-1]×(1+EIR) - expectedPayment} for the first
     * {@code upToDayIndex} periods, honouring every active {@link RateSegment} (its EIR, payment and the balance reset
     * at its {@code startDayIndex}). The returned amortization list is <em>not</em> rounding-settled — callers that
     * need the full schedule (not a bounded prefix used to read the balance at a split) apply
     * {@link #settleAmortizationRoundingToFee} themselves.
     */
    private BalancesAndAmortizations computeBalancesAndAmortizations(final int upToDayIndex) {
        final List<Money> balances = new ArrayList<>(upToDayIndex);
        final List<Money> expectedAmortizations = new ArrayList<>(upToDayIndex);
        BigDecimal prevBalance = netDisbursementAmount.getAmount();
        for (int i = 0; i < upToDayIndex; i++) {
            final int dayIndex = i + 1;
            final RateSegment seg = segmentForDay(dayIndex);
            // At segment boundary, reset balance to segment's net disbursement
            if (seg != null && seg.startDayIndex() == dayIndex) {
                prevBalance = seg.netDisbursementAtSplit().getAmount();
            }
            final BigDecimal eir = seg != null ? seg.effectiveInterestRate() : effectiveInterestRate;
            final BigDecimal payment = expectedPaymentForDay(dayIndex);
            final BigDecimal onePlusRate = BigDecimal.ONE.add(eir, mc);
            final BigDecimal balance = prevBalance.multiply(onePlusRate, mc).subtract(payment, mc);
            balances.add(money(balance));
            expectedAmortizations.add(money(balance.add(payment, mc).subtract(prevBalance, mc)));
            prevBalance = balance;
        }
        return new BalancesAndAmortizations(balances, expectedAmortizations);
    }

    /**
     * Settles the amortization rounding drift so the per-period amounts sum exactly to the discount fee, letting the
     * deferred discount-fee balance close to zero at payoff. The drift (a few cents, from rounding each period to the
     * currency scale) is placed on the final period; because a declining-balance schedule tapers to pennies at the end,
     * any part the final period cannot absorb without going negative spills back to earlier periods.
     */
    private void settleAmortizationRoundingToFee(final List<Money> amortizations) {
        final BigDecimal amortSum = amortizations.stream().map(Money::getAmount).reduce(BigDecimal.ZERO, (a, b) -> a.add(b, mc));
        BigDecimal drift = discountFeeAmount.getAmount().subtract(amortSum, mc);
        for (int i = amortizations.size() - 1; i >= 0 && drift.signum() != 0; i--) {
            final BigDecimal current = amortizations.get(i).getAmount();
            final BigDecimal settled = current.add(drift, mc).max(BigDecimal.ZERO);
            amortizations.set(i, money(settled));
            drift = drift.subtract(settled.subtract(current, mc), mc);
        }
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

    private void buildTailPeriodsAndComputeNpv(final List<ProjectedPayment> tailPayments, final BigDecimal shortfall,
            final int appliedCount) {
        final int totalTerm = effectiveTotalTerm();
        // Catch-up periods bill the regular daily payment of whichever rate is in force at the end of the schedule, not
        // expectedPaymentForDay(totalTerm): that final period pays only the remainder needed to close the balance, and
        // a one-off closing amount is meaningless repeated across the days needed to recover missed payments.
        final RateSegment lastSegment = segmentForDay(totalTerm);
        final BigDecimal tailExpectedPayment = lastSegment != null ? lastSegment.expectedPaymentAmount().getAmount()
                : expectedPaymentAmount.getAmount();
        BigDecimal remaining = shortfall;
        int tailIndex = 0;
        while (remaining.signum() > 0) {
            final int periodNo = totalTerm + tailIndex + 1;
            final long dl = paymentsLeft(periodNo, appliedCount);
            final BigDecimal df = safeDiscountFactor(dl, totalTerm);
            final BigDecimal forecast = remaining.min(tailExpectedPayment);
            final BigDecimal npv = MathUtil.negativeToZero(forecast.multiply(df, mc));
            final Money zero = money(BigDecimal.ZERO);
            tailPayments.add(new ProjectedPayment(periodNo, dateOfPeriod(periodNo), dl, money(forecast), df, money(npv), zero, null, zero,
                    null, null, null, zero, null));
            remaining = remaining.subtract(forecast, mc);
            tailIndex++;
        }
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
        if (seg != null) {
            // Only the final segment runs to completion; its last period pays the remainder of its gross payable. An
            // intermediate segment is cut off by the next rate change, so it keeps billing its daily payment to the
            // cut. effectiveTotalTerm() is the last segment's final day, so a segment day equal to it is always the
            // last segment's last period; an intermediate segment's days are all below it and keep billing the daily.
            if (seg.finalPaymentAmount() != null && dayIndex == effectiveTotalTerm()) {
                return seg.finalPaymentAmount().getAmount();
            }
            return seg.expectedPaymentAmount().getAmount();
        }
        // Base schedule: the final period pays only the remainder of the gross payable.
        if (finalPaymentAmount != null && dayIndex == originalPaymentNumber) {
            return finalPaymentAmount.getAmount();
        }
        return expectedPaymentAmount.getAmount();
    }

    private Money money(final BigDecimal amount) {
        return Money.of(currency, amount, mc);
    }

    private record BalancesAndAmortizations(List<Money> balances, List<Money> expectedAmortizations) {
    }

    private record PaymentAnalysis(BigDecimal shortfall, BigDecimal excess) {
    }

    /** Amortization parameters shared by the base schedule and each rate-change segment. */
    private record ScheduleParams(BigDecimal dailyPayment, int paymentNumber, BigDecimal finalPayment, BigDecimal eir) {
    }

    public record ActualPayment(LocalDate date, Money amount) {
    }

    public record RateSegment(int startDayIndex, Money expectedPaymentAmount, int segmentTerm, BigDecimal effectiveInterestRate,
            Money netDisbursementAtSplit, Money discountAtSplit, Money finalPaymentAmount) {
    }

    /** Principal re-injected on a date by an over-refunding credit balance refund. */
    public record PrincipalAdjustment(LocalDate date, Money amount) {
    }

    public static String getModelVersion() {
        return MODEL_VERSION;
    }
}
