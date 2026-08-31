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
 *
 * <h3>Expected versus actual</h3>
 * <p>
 * Each period carries both what the schedule planned and what really happened. The two are not independent: every
 * recorded payment re-seeds the projection that follows it from the balance the borrower actually owes, so a loan
 * running behind is projected forward from its real position rather than from instalments it never made. Periods before
 * the first payment keep the plan they were written with, because nothing is known about them yet.
 * <p>
 * Where the term is not enough to clear what is left, catch-up periods continue the schedule past it — same instalment,
 * balance still declining, fee still being earned — until the balance reaches zero.
 */
@Getter
@Accessors(fluent = true)
@Slf4j
public final class ProjectedAmortizationScheduleModel {

    private static final String MODEL_VERSION = "6";

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

    /**
     * Derived, and deliberately not persisted. Both payment lists are a pure function of the fields above - the
     * amounts, the rate, the dates, the recorded payments, the rate segments, the principal adjustments and
     * {@link #calculatedTillDate} - so storing them buys nothing and costs a great deal: a slow-amortizing product
     * (10000 at 1% of a 10000 volume is a 35716-period schedule) serialized to 15MB, which COB then re-read and rewrote
     * for every loan on every business date it caught up through. Rebuilding on load is roughly fifteen times cheaper
     * than parsing the stored copy.
     */
    @JsonExclude
    @Getter(AccessLevel.NONE)
    @SerializedName(value = "projectedPayments", alternate = "payments")
    private List<ProjectedPayment> projectedPayments;

    /**
     * Set whenever the lists above are known to be out of date - on load, and when an elapsed-period acknowledgement
     * moves {@link #calculatedTillDate}. The rebuild is deferred until something actually reads a period, because most
     * callers want only the scalars beside it (the rate, the term, the payment amount) and a same-date acknowledgement
     * changes nothing at all. On a 35716-period schedule an eager rebuild costs a quarter of a second, so doing it for
     * readers that never look at a period is what made COB time out.
     */
    @JsonExclude
    @Getter(AccessLevel.NONE)
    private boolean derivedPaymentsStale;

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
     * Creates a skeleton instance for Gson deserialization. Gson overwrites the final fields via reflection. The two
     * payment lists are not stored, so the instance is born stale and rebuilds them on the first read of a period - the
     * parser has nothing to remember.
     */
    public static ProjectedAmortizationScheduleModel forDeserialization(final MathContext mc, final CurrencyData currency) {
        return new ProjectedAmortizationScheduleModel(mc, currency);
    }

    /** Marks the payment lists as out of date. Re-armable: the schedule goes stale again every time a date moves. */
    private void invalidateDerivedPayments() {
        this.derivedPaymentsStale = true;
    }

    /** Rebuilds the payment lists if anything has invalidated them. Every read of a period goes through here. */
    private void materializeDerivedPayments() {
        if (derivedPaymentsStale) {
            rebuildPayments();
        }
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
        // Nothing has been read off the JSON yet, and the lists are never in it. Models written before they were
        // dropped still carry a copy; it is excluded on read and replaced by the rebuild, so no row needs migrating.
        this.derivedPaymentsStale = true;
        this.calculatedTillDate = null;
    }

    public List<ProjectedPayment> projectedPayments() {
        materializeDerivedPayments();
        return projectedPayments;
    }

    /**
     * The scheduled maturity is the date of the last real projected payment (a period with {@code paymentNo > 0}, i.e.
     * excluding the disbursement row). Returns {@code null} when the schedule has no real payments.
     */
    public LocalDate scheduledMaturityDate() {
        materializeDerivedPayments();
        if (projectedPayments == null) {
            return null;
        }
        return projectedPayments.stream().filter(payment -> payment.paymentNo() > 0).map(ProjectedPayment::date).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
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
        materializeDerivedPayments();
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

    /**
     * How many periods the schedule currently runs to: the contractual term, or the number of periods that have already
     * gone by if the borrower has fallen past it.
     *
     * <p>
     * A working capital loan does not stop having a schedule because its original last day went by unpaid. The debt is
     * still there and still being billed, so the periods keep coming: a borrower who pays nothing for two years has two
     * years of missed instalments behind them and the same remaining days ahead, pushed out day by day. Bounding the
     * schedule at the contractual term instead froze it one term past maturity, and everything measured from it - what
     * a payment can be dated, where a rate change may take effect, which days count as missed - froze with it.
     */
    private int scheduleTerm() {
        // Today's period counts: it is due now, so it must have a row for a payment or a rate change dated today to
        // land on. Only the periods before it have elapsed.
        return Math.max(effectiveTotalTerm(), elapsedPeriodCount() + 1);
    }

    /** Periods whose date has already gone by, counted from the dates alone so it cannot depend on the schedule. */
    private int elapsedPeriodCount() {
        if (calculatedTillDate == null) {
            return 0;
        }
        final long elapsed = ChronoUnit.DAYS.between(expectedDisbursementDate, calculatedTillDate) - currentFirstPeriodDayOffset();
        return (int) Math.clamp(elapsed, 0L, MAX_CALCULABLE_TOTAL_DAYS);
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
        final LocalDate lastInstallmentDate = dateOfPeriod(scheduleTerm(), firstPeriodDayOffset);
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
        if (index < 0 || index >= scheduleTerm()) {
            throw new IllegalArgumentException("paymentDate " + paymentDate + " is outside the valid range ["
                    + dateOfPeriod(1, firstPeriodDayOffset) + " .. " + dateOfPeriod(scheduleTerm(), firstPeriodDayOffset) + "]");
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

    /**
     * Records that time has moved on to {@code businessDate}, so every instalment date it has passed with no payment
     * against it reports a nil payment rather than nothing at all.
     *
     * <p>
     * This fills the actual columns and stops there. It does not re-base the projection: a zeroed date is not a
     * transaction, it only says nothing has come in for that day yet, and the plan is restated when money actually
     * lands. A loan that has never paid therefore keeps the schedule it was written with, with the actual balance
     * standing at the full amount beside it.
     *
     * <p>
     * Payments and rate changes already carry the date forward on their own; this is what covers the loan where nothing
     * happens at all. The date only ever moves forward, so a call that would take it backwards - a COB re-run, a loan
     * whose last action is already later than today - leaves the schedule untouched.
     *
     * @return {@code true} when the schedule was rebuilt
     */
    public boolean acknowledgeElapsedPeriods(final LocalDate businessDate) {
        Objects.requireNonNull(businessDate, "businessDate");
        if (calculatedTillDate != null && !businessDate.isAfter(calculatedTillDate)) {
            return false;
        }
        updateCalculatedTillDate(businessDate);
        // Invalidated rather than rebuilt: COB acknowledges one loan-day at a time, and only the caller that goes on
        // to read a period needs the schedule materialised.
        invalidateDerivedPayments();
        return true;
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
        materializeDerivedPayments();
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
        final int splitDayIndex = Math.min(rawSplitDayIndex + (1 - currentFirstPeriodDayOffset()), scheduleTerm());

        // Remove existing segments at or after split (supports overwrite on second rate change)
        // Guard against null rateSegments from V1 model deserialization
        if (rateSegments == null) {
            throw new IllegalStateException("Model not properly initialized; rateSegments is null");
        }
        rateSegments.removeIf(s -> s.startDayIndex() >= splitDayIndex);

        // Collect actual payments received before the split
        materializeDerivedPayments();
        BigDecimal paymentsReceived = BigDecimal.ZERO;
        for (final ProjectedPayment p : projectedPayments) {
            if (p.paymentNo() <= 0 || p.paymentNo() > splitDayIndex) {
                continue;
            }
            if (p.actualPaymentAmount() != null) {
                paymentsReceived = paymentsReceived.add(p.actualPaymentAmount().getAmount(), mc);
            }
        }

        // What the segment has to bill is simply what the schedule has not billed yet: the balance still outstanding at
        // the split, plus the fee not yet earned against it. Both come off the same walk, so they cannot disagree about
        // how much of the loan the periods before the split already accounted for - and it is exactly that
        // disagreement,
        // between a balance measured one way and a deduction measured another, that let a schedule bill the loan twice.
        final BigDecimal balanceAtSplit;
        final BigDecimal newDiscount;
        if (splitDayIndex > 1) {
            final int lastPeriodBeforeSplit = splitDayIndex - 1;
            final Map<LocalDate, BigDecimal> settledPaymentsByDate = aggregatePaymentsByDate();
            final BalancesAndAmortizations ba = computeBalancesAndAmortizations(lastPeriodBeforeSplit, settledPaymentsByDate);
            // What the split period continues from, which on a loan behind on its instalments is the balance really
            // outstanding rather than the one the plan had drawn down to. Sizing the segment against the plan while
            // every period after it re-bases on reality left the two disagreeing across the boundary, and the schedule
            // stepped back up a period later.
            balanceAtSplit = ba.basisForNextPeriod();
            newDiscount = MathUtil.negativeToZero(
                    unearnedFeeAt(lastPeriodBeforeSplit, settledPaymentsByDate, ba.expectedAmortizations(), ba.planAmortizations()));
        } else {
            balanceAtSplit = netDisbursementAmount.getAmount();
            newDiscount = discountFeeAmount.getAmount();
        }

        final BigDecimal tpv = totalPaymentVolume.getAmount();
        final BigDecimal newNetDisb = balanceAtSplit;
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
        // Cleared up front rather than on the way out: the rebuild reads the lists it is replacing, and a read that
        // saw the flag still set would recurse. It also means every direct caller leaves the model fresh.
        this.derivedPaymentsStale = false;
        // Aggregated first: the live schedule's balances are driven by what was actually collected on the periods the
        // current date has passed, so the recursion needs this map before it can run.
        final Map<LocalDate, BigDecimal> paymentsByDate = aggregatePaymentsByDate();
        final BalancesAndAmortizations ba = computeBalancesAndAmortizations(paymentsByDate);
        final List<BigDecimal> paymentList = buildPaymentList(paymentsByDate);
        this.projectedPayments = List.copyOf(buildPayments(paymentList, paymentsByDate.size(), ba));
    }

    private Map<LocalDate, BigDecimal> aggregatePaymentsByDate() {
        final Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        // Every elapsed instalment date is seeded with zero and then overwritten by whatever really landed on it, so a
        // period the borrower missed is recorded as a nil payment rather than left absent. This has to happen for a
        // loan that has never paid too - that is precisely the loan whose every period was missed.
        //
        // Elapsed means strictly before the date reached, matching the passed-period test the actual columns are drawn
        // from: the instalment falling due today has not been missed, the borrower still has the day to pay it. Seeding
        // it would re-base the projection off a period that is still open, holding the next period at today's balance.
        if (calculatedTillDate != null) {
            final LocalDate firstInstallmentDate = dateOfPeriod(1);
            final LocalDate lastInstallmentDate = dateOfPeriod(scheduleTerm());
            final LocalDate lastElapsedDate = calculatedTillDate.minusDays(1);
            if (!lastElapsedDate.isBefore(firstInstallmentDate)) {
                final LocalDate mapEnd = lastElapsedDate.isAfter(lastInstallmentDate) ? lastInstallmentDate : lastElapsedDate;
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
        final int totalTerm = scheduleTerm();
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
        // Against the plan, not against ba.expectedAmortizations(): the displayed expected column is re-based by these
        // very payments, and consuming it would measure a payment against a schedule it had already moved.
        final List<BigDecimal> actualAmortizations = computeActualAmortizations(ba.planAmortizations(), payments, appliedCount);
        final BigDecimal excessForRunning = amountToAdjustTail.max(BigDecimal.ZERO);
        final List<BigDecimal> runningExpected = computeRunningExpectedPayments(excessForRunning);
        final List<ProjectedPayment> tailPayments = new ArrayList<>();
        final BigDecimal balanceAfterTerm = ba.balances().isEmpty() ? BigDecimal.ZERO : ba.balances().getLast().getAmount();
        // The catch-up periods exist to clear whatever the term ended still owing, so that balance is what they
        // recover. Sizing them from a separately counted payment shortfall instead let the two disagree: a schedule
        // whose balance had already reached zero still grew a run of periods billing against nothing.

        final BigDecimal discountFee = discountFeeAmount.getAmount();
        final BigDecimal netDisb = netDisbursementAmount.getAmount();

        final List<ProjectedPayment> result = new ArrayList<>(scheduleTerm() + 2 + tailPayments.size());
        result.add(createDisbursementPayment());

        BigDecimal cumulativeActualAmort = BigDecimal.ZERO;
        // Carried alongside the rounded total because only the total may be rounded: rounding each period before
        // adding it drops a fraction of a cent every time, and the fee then closes short of itself.
        BigDecimal unroundedCumulativeActualAmort = BigDecimal.ZERO;
        // And the same total again, measured the way the expected column measures itself - every period rounded before
        // it is added. Nothing reports this figure; it exists only to rewind the expected column at a settled period,
        // and it has to share that column's units or the rewind stops being a no-op for an instalment paid in full and
        // on time, which would move the plan of a loan that is behaving perfectly.
        BigDecimal rewindBasis = BigDecimal.ZERO;
        BigDecimal cumulativeExpectedAmort = BigDecimal.ZERO;
        BigDecimal runningActualBalance = netDisb;
        for (int i = 0; i < scheduleTerm(); i++) {
            final int periodNo = i + 1;
            final LocalDate periodDate = dateOfPeriod(periodNo);
            final BigDecimal periodPayment = payments.get(i);
            final boolean hasPositivePayment = periodPayment != null && periodPayment.signum() > 0;
            final boolean passedPeriod = calculatedTillDate != null && periodDate.isBefore(calculatedTillDate);
            final long paymentsLeft = paymentsLeft(periodNo, appliedCount);
            final BigDecimal safeDf = safeDiscountFactor(paymentsLeft, periodNo);
            final BigDecimal periodExpectedPayment = ba.billedPayments().get(i).getAmount();
            final BigDecimal safeRunningExpected = MathUtil.negativeToZero(runningExpected.get(i));
            final BigDecimal npvSource = resolveNpvSource(hasPositivePayment, passedPeriod, periodPayment, safeRunningExpected);
            final BigDecimal npvValue = MathUtil.negativeToZero(npvSource.multiply(safeDf, mc));
            final BigDecimal safeExpectedAmort = ba.expectedAmortizations().get(i).getAmount().min(discountFee);

            final BigDecimal actualAmortization;
            final BigDecimal incomeModification;

            // A rate segment restarts the projection, not the loan. The actual balance is a record of money that moved,
            // and no money moves because the rate changed, so it carries straight through the boundary - resetting it
            // to
            // the segment's projected basis reported the borrower as owing whatever the plan had assumed by then rather
            // than what they had really paid down.
            if (hasPositivePayment) {
                // The period earns whatever the running total moved by, so the figure shown, the figure accumulated
                // and the figure the balance is carried on are all one number. The running total is kept exact and
                // rounded once, rather than accumulated from periods rounded one by one: a per-period rounding drops a
                // fraction of a cent every period, and over a few hundred of them the sum wanders several cents off,
                // which is what left a large payment short of the fee. The rewind below reads rewindBasis rather than
                // this figure precisely because the two are measured differently - see its comment.
                // Capped at the fee, so the periods sum to exactly the fee however the rounding falls: the last one
                // absorbs the residual instead of the loan booking a cent of income it never held, and a balance
                // carried on the same figures closes at exactly zero.
                final BigDecimal cumulativeActualBefore = cumulativeActualAmort;
                unroundedCumulativeActualAmort = unroundedCumulativeActualAmort.add(actualAmortizations.get(i), mc).min(discountFee);
                cumulativeActualAmort = money(unroundedCumulativeActualAmort).getAmount();
                actualAmortization = cumulativeActualAmort.subtract(cumulativeActualBefore, mc);
                rewindBasis = rewindBasis.add(money(actualAmortizations.get(i)).getAmount(), mc).min(discountFee);
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

            // The deferred fee rewinds at a settled period for the same reason the balance does. Those periods booked
            // fee against instalments that were never collected; carrying that total forward would charge it a second
            // time as the re-projected periods earn it again. From here the fee runs down from what the payments have
            // really earned, so a loan two instalments behind shows the same fee balance two days later. A nil payment
            // earns nothing, which is exactly what a missed instalment should do to the fee.
            if (periodPayment != null) {
                cumulativeExpectedAmort = rewindBasis;
            }
        }

        // Built after the term's periods so the catch-up rows continue the same running fee total, rewind included,
        // rather than restarting from a figure that never saw those rewinds.
        buildTailPeriodsAndComputeNpv(tailPayments, appliedCount, balanceAfterTerm, cumulativeExpectedAmort);
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
     *
     * <p>
     * A period is only spent when the loan was already square before it began. Zero present value alone does not say
     * that: an elapsed period that went unpaid contributes nothing to present value yet still owes its instalment, so a
     * loan that has never paid a penny would otherwise have its whole schedule trimmed away the day it passed maturity.
     * The balance the period opened on is what tells the two apart.
     */
    private static void trimTrailingZeroNpvPayments(final List<ProjectedPayment> payments, final Set<LocalDate> adjustedDates) {
        while (payments.size() > 1) {
            final ProjectedPayment last = payments.getLast();
            final Money openingBalance = payments.get(payments.size() - 2).expectedBalance();
            final boolean nothingLeftToBill = openingBalance != null && openingBalance.getAmount().signum() <= 0;
            if (last.npvValue() != null && last.npvValue().isZero() && nothingLeftToBill && !adjustedDates.contains(last.date())) {
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
     * The whole schedule: the declining-balance walk over every period of the term, with the amortization column
     * settled so the deferred fee closes to zero when the loan is paid off.
     *
     * <p>
     * Rounding each period to the currency scale leaves a few cents unaccounted for, and the catch-up periods beyond
     * the term earn fee of their own; both are settled onto the term's final periods here. Whatever is still unearned
     * once the tail has taken its share is the shortfall to distribute.
     */
    private BalancesAndAmortizations computeBalancesAndAmortizations(final Map<LocalDate, BigDecimal> settledPaymentsByDate) {
        final BalancesAndAmortizations ba = computeBalancesAndAmortizations(scheduleTerm(), settledPaymentsByDate);
        final BigDecimal earnedByTail = ba.balances().isEmpty() ? BigDecimal.ZERO
                : simulateTail(ba.balances().getLast().getAmount()).stream().map(TailPeriod::amortization).reduce(BigDecimal.ZERO,
                        (a, b) -> a.add(b, mc));
        final BigDecimal unearned = unearnedFeeAt(ba.expectedAmortizations().size(), settledPaymentsByDate, ba.expectedAmortizations(),
                ba.planAmortizations());
        final int closingPeriod = ba.balances().isEmpty() ? ba.expectedAmortizations().size() - 1 : findClosingPeriod(ba.balances());
        final List<Money> settleTarget = ba.expectedAmortizations().subList(0, closingPeriod + 1);
        settleAmortizationOntoFinalPeriods(settleTarget, unearned.subtract(earnedByTail, mc));
        return ba;
    }

    /**
     * The fee still to be earned by the end of {@code period}, following the same rewind the displayed deferred balance
     * follows: fee the projection booked against instalments that were never collected does not count as earned, so at
     * each recorded payment only what the payments have really earned is held against the fee.
     *
     * <p>
     * A re-based schedule therefore books fee twice over its whole length - once as the history it already recorded,
     * once as the plan to earn it again - and that is why the column does not simply sum to the fee.
     */
    private BigDecimal unearnedFeeAt(final int period, final Map<LocalDate, BigDecimal> settledPaymentsByDate,
            final List<Money> amortizations, final List<BigDecimal> planAmortizations) {
        final List<BigDecimal> amounts = amortizations.stream().map(Money::getAmount).toList();
        final BigDecimal fee = discountFeeAmount.getAmount();
        final List<BigDecimal> actualAmortizationsByDay = settledPaymentsByDate == null ? List.of()
                : computeActualAmortizations(planAmortizations, paymentsByDay(settledPaymentsByDate, period), period);
        BigDecimal cumulativeExpected = BigDecimal.ZERO;
        BigDecimal rewindBasis = BigDecimal.ZERO;
        for (int dayIndex = 1; dayIndex <= period; dayIndex++) {
            cumulativeExpected = cumulativeExpected.add(amounts.get(dayIndex - 1), mc);
            final LocalDate periodDate = dateOfPeriod(dayIndex);
            final BigDecimal paid = settledPaymentsByDate == null ? null : settledPaymentsByDate.get(periodDate);
            if (paid == null) {
                continue;
            }
            // Every period rounded before it is added, which is how the rewind in buildPayments measures the same
            // running total: this is the residual the settle works from, so measuring it any other way than the rewind
            // it is predicting leaves the deferred balance closing off zero, and an instalment paid in full and on time
            // stops being the no-op it has to be.
            //
            // Deliberately uncapped, though. Capping what the payments have earned at the fee looks harmless - they can
            // never earn more than there is - but rounding can put their sum a cent either side of it. Holding the
            // total down to the fee swallows an overshoot the settle exists to take back off, and the schedule then
            // closes on a negative deferred balance.
            rewindBasis = rewindBasis.add(money(actualAmortizationsByDay.get(dayIndex - 1)).getAmount(), mc);
            cumulativeExpected = rewindBasis;
        }
        // Signed on purpose. Rounding each period to the currency scale can push the booked amortization a cent past
        // the fee, and the settle below needs to see that overshoot to take it back off; clamping here would hide it
        // and leave the schedule closing on a negative deferred balance.
        return fee.subtract(cumulativeExpected, mc);
    }

    /**
     * Walks the declining-balance recursion {@code balance[i] = balance[i-1]×(1+EIR) - expectedPayment} for the first
     * {@code upToDayIndex} periods, honouring every active {@link RateSegment} (its EIR, payment and the balance reset
     * at its {@code startDayIndex}), and re-basing the projection on reality wherever reality is known.
     *
     * <p>
     * Every recorded payment re-seeds the periods that follow it from what the borrower actually owes, so a loan that
     * has fallen behind stops being projected as though the instalments it missed had been collected. Periods before
     * the first payment keep the schedule they were written with: until something is known to have happened on a day,
     * the plan for it stands. A backdated payment lands earlier in this same walk and carries the periods after it
     * along, which is all the re-processing that case needs.
     *
     * <p>
     * The returned amortization list is <em>not</em> settled — the caller that needs the full schedule applies
     * {@link #settleAmortizationOntoFinalPeriods}; a bounded prefix used to read the balance at a rate-change split
     * does not. Pass {@code null} for {@code settledPaymentsByDate} to walk the pure projection throughout, which is
     * what the unchanging original schedule is built from.
     */
    private BalancesAndAmortizations computeBalancesAndAmortizations(final int upToDayIndex,
            final Map<LocalDate, BigDecimal> settledPaymentsByDate) {
        final List<Money> balances = new ArrayList<>(upToDayIndex);
        final List<Money> expectedAmortizations = new ArrayList<>(upToDayIndex);
        final List<BigDecimal> exactAmortizations = new ArrayList<>(upToDayIndex);
        final List<Money> billedPayments = new ArrayList<>(upToDayIndex);

        BigDecimal prevBalance = netDisbursementAmount.getAmount();
        BigDecimal actualBalance = netDisbursementAmount.getAmount();
        // The plan for the whole span, computed before a single payment is applied. The walk below used to consume
        // from the list it was still building, so a payment covering 198 periods could only find the one period walked
        // so far and collected a fraction of the fee it had earned - which drove the balance below zero and took the
        // rest of the schedule negative with it.
        final List<BigDecimal> planAmortizations = settledPaymentsByDate == null ? exactAmortizations
                : computeBalancesAndAmortizations(upToDayIndex, null).planAmortizations();
        final List<BigDecimal> actualAmortizationsByDay = settledPaymentsByDate == null ? List.of()
                : computeActualAmortizations(planAmortizations, paymentsByDay(settledPaymentsByDate, upToDayIndex), upToDayIndex);

        for (int i = 0; i < upToDayIndex; i++) {
            final int dayIndex = i + 1;
            final RateSegment seg = segmentForDay(dayIndex);
            // At segment boundary, reset the projection to the segment's net disbursement. The actual balance is left
            // alone: a rate change rewrites the plan, it does not move money.
            if (seg != null && seg.startDayIndex() == dayIndex) {
                prevBalance = seg.netDisbursementAtSplit().getAmount();
            }
            final BigDecimal eir = seg != null ? seg.effectiveInterestRate() : effectiveInterestRate;
            final BigDecimal grown = prevBalance.multiply(BigDecimal.ONE.add(eir, mc), mc);
            // A period can never ask for more than the balance it has to close. A segment's final instalment is solved
            // when the segment is created, so a later segment or a repayment can move the balance out from under it and
            // leave it billing far more than is owed - which is how a schedule ended on a negative balance. The
            // catch-up periods have always been capped this way; the term's periods now are too.
            final BigDecimal payment = MathUtil.negativeToZero(expectedPaymentForDay(dayIndex)).min(MathUtil.negativeToZero(grown));
            final BigDecimal balance = grown.subtract(payment, mc);
            final BigDecimal amortization = balance.add(payment, mc).subtract(prevBalance, mc);
            billedPayments.add(money(payment));
            balances.add(money(balance));
            expectedAmortizations.add(money(amortization));
            exactAmortizations.add(amortization);
            prevBalance = balance;

            if (settledPaymentsByDate == null) {
                continue;
            }
            final LocalDate periodDate = dateOfPeriod(dayIndex);
            final BigDecimal paid = settledPaymentsByDate.get(periodDate);
            if (paid == null) {
                continue;
            }
            // Reality only moves when money does, so a day with nothing on it leaves the actual balance standing.
            actualBalance = actualBalance.subtract(paid, mc).add(actualAmortizationsByDay.get(i), mc);

            // Every settled period re-bases what follows it. A day that elapsed with nothing on it is settled just as
            // firmly as one that was paid - it says the instalment was not collected - so from here the projection
            // continues from what the borrower actually owes rather than from instalments the plan merely assumed. Only
            // periods that have not come round yet keep the schedule they were written with. A backdated payment lands
            // earlier in this same walk and takes the periods after it with it.
            //
            // Never from less than nothing, though. A borrower who has handed over more than the payable owes nothing,
            // and nothing is what the periods after that have to project: accruing on a negative balance grows it
            // further from zero every period and makes each one amortize a negative amount, which is a period
            // un-earning fee the payments have already earned. Summed back up as the fee still outstanding, that
            // phantom lands on the closing period and drives the deferred fee balance below zero. The allocator caps
            // the principal it hands over here at what is owed, so a real loan does not reach this; the projection
            // should not depend on that to stay coherent.
            prevBalance = MathUtil.negativeToZero(actualBalance);
        }
        return new BalancesAndAmortizations(balances, expectedAmortizations, prevBalance, billedPayments, planAmortizations);
    }

    /**
     * Finds the closing period index where the balance first reaches zero or becomes negative. Periods beyond this are
     * tail periods and should not receive rounding adjustments.
     */
    private int findClosingPeriod(final List<Money> balances) {
        for (int i = 0; i < balances.size(); i++) {
            if (balances.get(i).getAmount().signum() <= 0) {
                return i;
            }
        }
        return balances.size() - 1;
    }

    /**
     * Settles the amortization rounding drift so the per-period amounts sum exactly to the discount fee, letting the
     * deferred discount-fee balance close to zero at payoff. The drift (a few cents, from rounding each period to the
     * currency scale) is placed on the final period; because a declining-balance schedule tapers to pennies at the end,
     * any part the final period cannot absorb without going negative spills back to earlier periods.
     */
    private void settleAmortizationOntoFinalPeriods(final List<Money> amortizations, final BigDecimal shortfall) {
        BigDecimal drift = shortfall;
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

    /**
     * What each payment amortizes off the discount fee, walking the plan's periods at the price each one actually
     * bills: a period amortizes its whole share once its instalment is covered, and a pro-rata part of it while the
     * instalment is only partly covered.
     *
     * <p>
     * A declining-balance schedule does not price every period alike - the closing one bills whatever is left rather
     * than a full instalment - so dividing a payment by a single period's instalment measures it against a price most
     * of the periods do not carry. A payment large enough to clear the loan then earns slightly less fee than the
     * periods it cleared actually hold, and the deferred balance closes a cent off zero.
     *
     * <p>
     * Computed for the whole span in one walk, for the same reason the plan itself is: every caller that converts money
     * into fee needs this same conversion, and deriving it separately in each of them is what let them drift apart.
     */
    private List<BigDecimal> computeActualAmortizations(final List<BigDecimal> planAmortizations, final List<BigDecimal> payments,
            final int appliedCount) {
        final List<BigDecimal> actualAmortizations = new ArrayList<>(appliedCount);
        int period = 0;
        BigDecimal amountAlreadyPaidIntoPeriod = BigDecimal.ZERO;
        for (int i = 0; i < appliedCount; i++) {
            final BigDecimal payment = payments.get(i);
            BigDecimal paymentNotYetApplied = payment == null ? BigDecimal.ZERO : MathUtil.negativeToZero(payment);
            BigDecimal amortizationFromPayment = BigDecimal.ZERO;
            while (paymentNotYetApplied.signum() > 0 && period < planAmortizations.size()) {
                final BigDecimal periodExpectedPayment = expectedPaymentForDay(period + 1);
                if (periodExpectedPayment.signum() <= 0) {
                    period++;
                    amountAlreadyPaidIntoPeriod = BigDecimal.ZERO;
                    continue;
                }
                final BigDecimal amountAppliedToPeriod = paymentNotYetApplied
                        .min(periodExpectedPayment.subtract(amountAlreadyPaidIntoPeriod, mc));
                final BigDecimal fractionOfPeriodPaid = amountAppliedToPeriod.divide(periodExpectedPayment, mc);
                amortizationFromPayment = amortizationFromPayment.add(planAmortizations.get(period).multiply(fractionOfPeriodPaid, mc), mc);
                paymentNotYetApplied = paymentNotYetApplied.subtract(amountAppliedToPeriod, mc);
                amountAlreadyPaidIntoPeriod = amountAlreadyPaidIntoPeriod.add(amountAppliedToPeriod, mc);
                if (amountAlreadyPaidIntoPeriod.compareTo(periodExpectedPayment) >= 0) {
                    period++;
                    amountAlreadyPaidIntoPeriod = BigDecimal.ZERO;
                }
            }
            actualAmortizations.add(amortizationFromPayment);
        }
        return actualAmortizations;
    }

    /** The recorded payments laid out by day index, the shape {@link #computeActualAmortizations} reads them in. */
    private List<BigDecimal> paymentsByDay(final Map<LocalDate, BigDecimal> settledPaymentsByDate, final int upToDayIndex) {
        final List<BigDecimal> payments = new ArrayList<>(upToDayIndex);
        for (int dayIndex = 1; dayIndex <= upToDayIndex; dayIndex++) {
            payments.add(settledPaymentsByDate.get(dateOfPeriod(dayIndex)));
        }
        return payments;
    }

    private List<BigDecimal> computeRunningExpectedPayments(final BigDecimal excess) {
        final int totalTerm = scheduleTerm();
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

    private void buildTailPeriodsAndComputeNpv(final List<ProjectedPayment> tailPayments, final int appliedCount,
            final BigDecimal balanceAfterTerm, final BigDecimal earnedFeeAtTermEnd) {
        final int totalTerm = scheduleTerm();
        final BigDecimal fee = discountFeeAmount.getAmount();
        final List<TailPeriod> tail = simulateTail(balanceAfterTerm);
        if (tail.isEmpty()) {
            return;
        }

        // The catch-up periods are where a loan that fell behind actually earns its fee, so the rounding residue has to
        // be settled onto them and not onto the term's final periods: those were rewound to what the payments really
        // earned, which for a loan that never paid is nothing at all, and the top-up placed there would simply vanish -
        // leaving the schedule closing on an unearned deferred balance.
        // Summed on the rounded figures, because those are what the fee balance is displayed from: totalling the exact
        // ones would leave the settle a cent out of step with the column it is settling.
        final List<Money> amortizations = new ArrayList<>(tail.size());
        BigDecimal earnedByTail = BigDecimal.ZERO;
        for (final TailPeriod period : tail) {
            final Money amortization = money(period.amortization());
            amortizations.add(amortization);
            earnedByTail = earnedByTail.add(amortization.getAmount(), mc);
        }
        // Settled onto the last period that still bills something. A catch-up run can end on a period asking for a
        // fraction of a cent, which rounds to nothing and is trimmed off the finished schedule - and the top-up would
        // be trimmed away with it, leaving the fee short by exactly the amount being settled.
        final List<Money> settleTarget = amortizations.subList(0, lastBillingPeriod(tail) + 1);
        settleAmortizationOntoFinalPeriods(settleTarget, fee.subtract(earnedFeeAtTermEnd, mc).subtract(earnedByTail, mc));

        BigDecimal cumulativeAmort = earnedFeeAtTermEnd;
        for (int tailIndex = 0; tailIndex < tail.size(); tailIndex++) {
            final int periodNo = totalTerm + tailIndex + 1;
            final long dl = paymentsLeft(periodNo, appliedCount);
            final BigDecimal df = safeDiscountFactor(dl, totalTerm);
            final BigDecimal npv = MathUtil.negativeToZero(tail.get(tailIndex).payment().multiply(df, mc));
            final Money amortization = amortizations.get(tailIndex);
            cumulativeAmort = cumulativeAmort.add(amortization.getAmount(), mc);
            tailPayments.add(new ProjectedPayment(periodNo, dateOfPeriod(periodNo), dl, money(tail.get(tailIndex).payment()), df,
                    money(npv), money(tail.get(tailIndex).balance()), null, amortization, null, null, null,
                    money(MathUtil.negativeToZero(fee.subtract(cumulativeAmort, mc))), null));
        }
    }

    /**
     * Index of the last catch-up period that bills a whole cent, so the settle above lands somewhere that survives
     * {@link #trimTrailingZeroNpvPayments}. Falls back to the final period when none of them bills anything.
     */
    private int lastBillingPeriod(final List<TailPeriod> tail) {
        for (int i = tail.size() - 1; i >= 0; i--) {
            if (money(tail.get(i).payment()).getAmount().signum() > 0) {
                return i;
            }
        }
        return tail.size() - 1;
    }

    /** A catch-up period: what it bills, the balance it leaves, and the fee it earns on the way. */
    private record TailPeriod(BigDecimal payment, BigDecimal balance, BigDecimal amortization) {
    }

    /**
     * Walks the catch-up periods needed to clear whatever the term ended still owing. They are a continuation of the
     * schedule, not an appendix to it: the balance keeps declining and the fee keeps being earned across them, so a
     * loan running two instalments late tapers its deferred fee to zero two days later instead of settling the
     * remainder in one lump on the last period of the original term.
     *
     * <p>
     * Catch-up periods bill the regular daily payment of whichever rate is in force at the end of the schedule, not
     * {@code expectedPaymentForDay(totalTerm)}: that final period pays only the remainder needed to close the balance,
     * and a one-off closing amount is meaningless repeated across the days needed to recover missed payments.
     */
    private List<TailPeriod> simulateTail(final BigDecimal balanceAfterTerm) {
        final int totalTerm = effectiveTotalTerm();
        final RateSegment lastSegment = segmentForDay(totalTerm);
        final BigDecimal regularPayment = lastSegment != null ? lastSegment.expectedPaymentAmount().getAmount()
                : expectedPaymentAmount.getAmount();
        final BigDecimal eir = lastSegment != null ? lastSegment.effectiveInterestRate() : effectiveInterestRate;
        final List<TailPeriod> result = new ArrayList<>();
        BigDecimal prevBalance = MathUtil.negativeToZero(balanceAfterTerm);
        while (prevBalance.signum() > 0 && result.size() <= MAX_CALCULABLE_TOTAL_DAYS) {
            final BigDecimal grown = prevBalance.multiply(BigDecimal.ONE.add(eir, mc), mc);
            final BigDecimal payment = grown.min(regularPayment);
            if (payment.compareTo(grown.subtract(prevBalance, mc)) <= 0) {
                // The instalment cannot even cover the fee accruing on the balance, so no number of catch-up periods
                // would ever close it. Stop rather than spin.
                break;
            }
            final BigDecimal balance = grown.subtract(payment, mc);
            result.add(new TailPeriod(payment, balance, balance.add(payment, mc).subtract(prevBalance, mc)));
            prevBalance = balance;
        }
        return result;
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

    /**
     * The result of one walk across the schedule: what each period bills, leaves owing and earns in fee.
     *
     * @param basisForNextPeriod
     *            the balance the period after the walk continues from. Not the last row's own balance: once a period is
     *            settled the walk carries reality forward instead, so on a loan that fell behind the two differ - the
     *            row shows what the period would have left had it been paid, this is what the borrower really owes.
     *            Exact rather than rounded, for the same reason the walk itself works on exact figures.
     * @param planAmortizations
     *            the fee each period earns in the schedule as written, before any payment re-bases it. It is the
     *            yardstick a payment is converted into earned fee against, and it has to be the unperturbed one:
     *            measuring a payment against a list the same payment has already re-based is circular, and on a payment
     *            large enough to clear the balance it inverts the sign.
     */
    private record BalancesAndAmortizations(List<Money> balances, List<Money> expectedAmortizations, BigDecimal basisForNextPeriod,
            List<Money> billedPayments, List<BigDecimal> planAmortizations) {
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
