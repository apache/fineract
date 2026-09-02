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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.serialization.gson.JsonExclude;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

/**
 * Projected Amortization Schedule model for Working Capital loans.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 * <li>{@link #generate} - create initial schedule (at loan creation)</li>
 * <li>{@link #regenerate} - recalculate with new amounts (at approval / disbursement)</li>
 * <li>{@link #applyPayment} - record payments by date; schedule rebuilds after each</li>
 * <li>{@link #applyRateChange} - record a mid-lifecycle rate change and rebuild</li>
 * <li>Discount fee adjustment - regenerate schedule with the new discount and re-apply actual payments only</li>
 * </ol>
 *
 * <h3>What this class holds</h3>
 * <p>
 * The six figures the loan was written with, the payments recorded against it, the rate changes it has seen, and the
 * date it has been calculated to. Nothing else. Every period of the schedule is derived from those by
 * {@link AmortizationWalk} and rebuilt whenever one of them moves, so there is exactly one account of how a schedule is
 * produced and no stored figure that can fall out of step with it.
 *
 * <h3>Expected against actual</h3>
 * <p>
 * Each day carries both what the schedule planned and what really happened. The two are not independent: every recorded
 * payment restates the projection that follows it from the balance the borrower actually owes, so a loan running behind
 * is projected forward from its real position rather than from instalments it never made. Days before the first payment
 * keep the plan they were written with, because nothing is known about them yet.
 * <p>
 * Where the term is not enough to clear what is left, the walk simply keeps going - same instalment, balance still
 * declining, fee still being earned - until the balance reaches zero. There is no separate run of catch-up periods,
 * because the schedule ends wherever the loan closes.
 */
@Getter
@Accessors(fluent = true)
@Slf4j
public final class ProjectedAmortizationScheduleModel {

    private static final String MODEL_VERSION = "7";

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

    /** {@code (TPV x periodPaymentRate) / npvDayCount / 100} - constant across all but the final payment. */
    private final Money expectedPaymentAmount;

    /**
     * Final-period payment: the remainder {@code (netDisbursementAmount + discountFeeAmount) - (originalPaymentNumber -
     * 1) x expectedPaymentAmount}. Equals {@link #expectedPaymentAmount} when the schedule divides evenly.
     */
    private final Money finalPaymentAmount;

    /** {@code roundUp((netDisbursementAmount + discountFeeAmount) / expectedPaymentAmount)} */
    @SerializedName(value = "originalPaymentNumber", alternate = "loanTerm")
    private final int originalPaymentNumber;

    /**
     * Periodic effective rate, solved as {@code IRR([-netDisbursement, expectedPayment x (n-1), finalPayment])}.
     * Because the cash flow carries the smaller final remainder payment rather than assuming a uniform payment
     * throughout, this is zero exactly when the loan carries no discount fee - and the declining-balance recursion
     * built on it closes at exactly zero on the last day, with its daily accruals summing to exactly the discount fee.
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
    private final List<RateChange> rateChanges;

    @Getter(AccessLevel.NONE)
    private final List<PrincipalAdjustment> principalAdjustments;

    /**
     * Derived, and deliberately not persisted. The payment list is a pure function of the fields above - the amounts,
     * the rate, the dates, the recorded payments, the rate changes, the principal adjustments and
     * {@link #calculatedTillDate} - so storing it buys nothing and costs a great deal: a slow-amortizing product (10000
     * at 1% of a 10000 volume is a 35716-period schedule) serialized to 15MB, which COB then re-read and rewrote for
     * every loan on every business date it caught up through. Rebuilding on load is roughly fifteen times cheaper than
     * parsing the stored copy.
     */
    @JsonExclude
    @Getter(AccessLevel.NONE)
    @SerializedName(value = "projectedPayments", alternate = "payments")
    private List<ProjectedPayment> projectedPayments;

    /**
     * The day the rate currently in force was solved to close on. Derived by the walk and cached because the date
     * arithmetic that clamps a payment or an adjustment into the term needs it before any period is read, and
     * materialising the schedule to answer it would recurse.
     */
    @JsonExclude
    @Getter(AccessLevel.NONE)
    private int contractualTerm;

    /**
     * Set whenever the list above is known to be out of date - on load, and when an elapsed-period acknowledgement
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
        this.rateChanges = new ArrayList<>();
        this.principalAdjustments = new ArrayList<>();
        this.contractualTerm = originalPaymentNumber;
        this.calculatedTillDate = expectedDisbursementDate != null ? expectedDisbursementDate : currentBusinessDate;
        rebuildPayments();
    }

    /**
     * Creates a skeleton instance for Gson deserialization. Gson overwrites the final fields via reflection. The
     * payment list is not stored, so the instance is born stale and rebuilds it on the first read of a period - the
     * parser has nothing to remember.
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
        this.rateChanges = new ArrayList<>();
        this.principalAdjustments = new ArrayList<>();
        this.projectedPayments = List.of();
        this.contractualTerm = 0;
        // Nothing has been read off the JSON yet, and the list is never in it.
        this.derivedPaymentsStale = true;
        this.calculatedTillDate = null;
    }

    /** Marks the payment list as out of date. Re-armable: the schedule goes stale again every time a date moves. */
    private void invalidateDerivedPayments() {
        this.derivedPaymentsStale = true;
    }

    /** Rebuilds the payment list if anything has invalidated it. Every read of a period goes through here. */
    private void materializeDerivedPayments() {
        if (derivedPaymentsStale) {
            rebuildPayments();
        }
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

    public List<RateChange> rateChanges() {
        return rateChanges != null ? List.copyOf(rateChanges) : List.of();
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
     * balance or the fee. It only raises {@code expectedPaymentAmount} of its own period, because the re-injected
     * principal is owned by the loan balance ({@code principalAdjustment} / {@code principalOutstanding}), while this
     * model stays the projection of the originally disbursed amount.
     *
     * <p>
     * The date is clamped into the amortization term the same way {@link #applyPayment} clamps a payment date. A credit
     * balance refund lands on an overpaid loan, so its date is often at or past maturity; without the clamp the
     * adjustment would match no period at all and silently vanish from the projection while the balance still owed it.
     * Unlike a payment it does not advance {@code calculatedTillDate}: that would move which days count as passed and
     * so change the NPV source, and the adjustment must leave the amortization alone.
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
     * How many days the schedule is measured over: the term the rate in force was solved to close on, or the number of
     * days that have already gone by if the borrower has fallen past it.
     *
     * <p>
     * A working capital loan does not stop having a schedule because its original last day went by unpaid. The debt is
     * still there and still being billed, so the days keep coming: a borrower who pays nothing for two years has two
     * years of missed instalments behind them and the same remaining days ahead, pushed out day by day. Bounding this
     * at the contractual term instead froze it one term past maturity, and everything measured from it - what a payment
     * can be dated, where a rate change may take effect - froze with it.
     */
    private int scheduleTerm() {
        // Today's day counts: it is due now, so it must have a row for a payment or a rate change dated today to land
        // on. Only the days before it have elapsed.
        return Math.max(effectiveTotalTerm(), elapsedPeriodCount() + 1);
    }

    /** Days whose date has already gone by, counted from the dates alone so it cannot depend on the schedule. */
    private int elapsedPeriodCount() {
        if (calculatedTillDate == null) {
            return 0;
        }
        final long elapsed = ChronoUnit.DAYS.between(expectedDisbursementDate, calculatedTillDate) - currentFirstPeriodDayOffset();
        return (int) Math.clamp(elapsed, 0L, MAX_CALCULABLE_TOTAL_DAYS);
    }

    /**
     * The day the rate currently in force was solved to close on - what the schedule would run to if the borrower paid
     * exactly to plan from here. Equals {@link #originalPaymentNumber} until a rate change moves it.
     */
    public int effectiveTotalTerm() {
        return contractualTerm > 0 ? contractualTerm : originalPaymentNumber;
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
        try {
            AmortizationParams.solve(netDisbursementAmount, discountFeeAmount, totalPaymentVolume, periodPaymentRate, npvDayCount,
                    currency.getDigitsAfterDecimal(), mc);
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

        final AmortizationParams.Solved solved = AmortizationParams.solve(netDisbursementAmount, discountFeeAmount, totalPaymentVolume,
                periodPaymentRate, npvDayCount, currency.getDecimalPlaces(), mc);

        return new ProjectedAmortizationScheduleModel(Money.of(currency, discountFeeAmount, mc),
                Money.of(currency, netDisbursementAmount, mc), Money.of(currency, totalPaymentVolume, mc), periodPaymentRate, npvDayCount,
                expectedDisbursementDate, Money.of(currency, solved.dailyPayment(), mc), Money.of(currency, solved.closingPayment(), mc),
                solved.term(), solved.eir(), mc, currency, currentDate);
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
        // Invalidated rather than rebuilt: COB acknowledges one loan-day at a time, and only the caller that goes on to
        // read a period needs the schedule materialised.
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

    /**
     * Records a rate change taking effect on {@code rateChangeDate} and rebuilds.
     *
     * <p>
     * Only the date and the new rate are kept. What the change has to re-price - the balance still outstanding and the
     * fee not yet earned against it - is whatever the walk is carrying when it reaches that day, so the two can never
     * disagree about how much of the loan the days before the change already accounted for. It was exactly that
     * disagreement, between a balance measured one way and a deduction measured another, that let a schedule bill the
     * loan twice.
     *
     * <p>
     * Changes must arrive in ascending effective-date order, and a caller that breaks that is rejected rather than
     * quietly given a wrong schedule. This method can only append: it has no way to insert a change ahead of ones
     * already recorded, because each change is sized against the balance and unearned fee reached on its own day and a
     * change slotted in earlier moves both for every change after it. A backdated change is therefore applied by
     * replaying the whole history in effective-date order onto a fresh model - which is what
     * {@code regenerateAmortizationScheduleOnRateChange} does - not by calling this with an earlier date.
     *
     * <p>
     * A change already recorded on this exact date is replaced, so booking twice on one day overwrites rather than
     * leaving the day with two rates.
     *
     * @param newPeriodPaymentRate
     *            the new period payment rate as a <strong>percentage</strong>
     * @param rateChangeDate
     *            the date the new rate takes effect (must not precede disbursement, and must not precede a change
     *            already recorded); may be in the future
     * @param currentDate
     *            today's business date, capping how far {@code calculatedTillDate} may advance; {@code null} lets the
     *            effective date advance it unchecked
     */
    public void applyRateChange(final BigDecimal newPeriodPaymentRate, final LocalDate rateChangeDate, final LocalDate currentDate) {
        Objects.requireNonNull(newPeriodPaymentRate, "newPeriodPaymentRate");
        Objects.requireNonNull(rateChangeDate, "rateChangeDate");
        if (rateChangeDate.isBefore(expectedDisbursementDate)) {
            throw new IllegalArgumentException("rateChangeDate must not be before expectedDisbursementDate");
        }
        if (rateChanges == null) {
            throw new IllegalStateException("Model not properly initialized; rateChanges is null");
        }
        // A rate change proves time has advanced only as far as the day it was booked on. Letting a future effective
        // date carry calculatedTillDate with it would mark the days between today and that date as elapsed, so the
        // catch-up machinery would bill them as missed and the NPV would be taken from the wrong days.
        final LocalDate reachedDate = currentDate != null && rateChangeDate.isAfter(currentDate) ? currentDate : rateChangeDate;
        updateCalculatedTillDate(reachedDate);
        // Moving the date reached lengthens the schedule, and both the clamp and the balance check below are measured
        // against it, so the old schedule must not be the one they read.
        invalidateDerivedPayments();

        // Clamped to the day the schedule currently runs to, so a change dated at or past the end of an
        // already-lengthened schedule takes effect on its final day rather than falling off it.
        //
        // Deliberately not clamped up to the first instalment the way a payment date is. Which calendar day the
        // schedule starts on depends on whether anything was paid on the disbursement date, so the first instalment is
        // a day that moves - and a stored date normalized against it is only right for the offset it was normalized
        // under. Two changes a day apart both pulled onto that day would share one effective date, and recording the
        // second would overwrite the first: a change the loan still reports as in force, silently absent from the
        // schedule it was meant to re-rate. Kept raw, the walk resolves a date before the first instalment onto the
        // first day instead, where a later change supersedes an earlier one as it does on any other day.
        final LocalDate effectiveDate = clampToLastScheduledDay(rateChangeDate);
        if (owedOn(effectiveDate).signum() <= 0) {
            throw new IllegalArgumentException("balance at a rate change must be positive");
        }

        // Appending only. Dropping the later changes to make room - which is what this used to do - left a backdated
        // call returning normally having silently deleted them, and the schedule wrong with no signal. The service
        // replays in effective-date order, so nothing reaches here out of order; saying so here is what keeps that a
        // property of the code rather than of a comment.
        final Optional<RateChange> laterChange = rateChanges.stream().filter(change -> change.effectiveDate().isAfter(effectiveDate))
                .findFirst();
        if (laterChange.isPresent()) {
            throw new IllegalArgumentException("rate changes must be applied in ascending effective-date order; " + effectiveDate
                    + " precedes the change already recorded for " + laterChange.get().effectiveDate());
        }
        rateChanges.removeIf(change -> change.effectiveDate().equals(effectiveDate));
        rateChanges.add(new RateChange(effectiveDate, newPeriodPaymentRate));

        rebuildPayments();
    }

    /**
     * What the loan still owes going into {@code date}: the balance the day before it closed on, or the whole net
     * disbursement when the date is the first instalment. Read off the finished schedule rather than re-walked, so it
     * is the same figure the schedule is showing.
     */
    private BigDecimal owedOn(final LocalDate date) {
        materializeDerivedPayments();
        BigDecimal owed = netDisbursementAmount.getAmount();
        for (final ProjectedPayment payment : projectedPayments) {
            if (payment.paymentNo() <= 0 || !payment.date().isBefore(date)) {
                continue;
            }
            final Money actual = payment.actualBalance();
            owed = actual != null ? actual.getAmount() : payment.expectedBalance().getAmount();
        }
        return owed;
    }

    /**
     * {@code date}, or the last day the schedule currently runs to when it falls past it. Unlike
     * {@link #calculateAllocationDate} this only bounds the top: a rate change needs its own day to re-rate anything,
     * and the last day of the schedule is the last one that exists.
     */
    private LocalDate clampToLastScheduledDay(final LocalDate date) {
        final LocalDate lastScheduledDay = dateOfPeriod(scheduleTerm(), currentFirstPeriodDayOffset());
        return date.isAfter(lastScheduledDay) ? lastScheduledDay : date;
    }

    private void rebuildPayments() {
        // Cleared up front rather than on the way out: the rebuild reads the list it is replacing, and a read that saw
        // the flag still set would recurse. It also means every direct caller leaves the model fresh.
        this.derivedPaymentsStale = false;
        final Map<LocalDate, BigDecimal> paymentsByDate = aggregatePaymentsByDate();
        AmortizationWalk amortizationWalk = new AmortizationWalk(netDisbursementAmount.getAmount(), discountFeeAmount.getAmount(),
                totalPaymentVolume.getAmount(), periodPaymentRate, npvDayCount, expectedDisbursementDate, currentFirstPeriodDayOffset(),
                calculatedTillDate, paymentsByDate, rateChanges, minimumScheduleDays(), currency, mc);
        final AmortizationWalk.Result walked = amortizationWalk.walk();
        this.contractualTerm = walked.contractualTerm();
        this.projectedPayments = List.copyOf(buildPayments(walked.days()));
    }

    /**
     * How far the schedule has to run whatever the balance does.
     *
     * <p>
     * Today's day always has a row: it is due now, so a payment or a rate change dated today needs somewhere to land.
     * And a principal adjustment needs its own day, even on a loan already repaid in full - the re-injected principal
     * is still owed, so the projection has to bill it rather than end before the day it falls on.
     */
    private int minimumScheduleDays() {
        int minimum = elapsedPeriodCount() + 1;
        if (principalAdjustments != null) {
            final int offset = currentFirstPeriodDayOffset();
            for (final PrincipalAdjustment adjustment : principalAdjustments) {
                minimum = Math.max(minimum, resolvePaymentIndex(adjustment.date(), offset) + 1);
            }
        }
        return minimum;
    }

    private Map<LocalDate, BigDecimal> aggregatePaymentsByDate() {
        final Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        // Every elapsed instalment date is seeded with zero and then overwritten by whatever really landed on it, so a
        // day the borrower missed is recorded as a nil payment rather than left absent. This has to happen for a loan
        // that has never paid too - that is precisely the loan whose every day was missed.
        //
        // Elapsed means strictly before the date reached, matching the passed-day test the actual columns are drawn
        // from: the instalment falling due today has not been missed, the borrower still has the day to pay it. Seeding
        // it would re-base the projection off a day that is still open, holding the next day at today's balance.
        if (calculatedTillDate != null) {
            final LocalDate firstInstallmentDate = dateOfPeriod(1);
            final LocalDate lastElapsedDate = calculatedTillDate.minusDays(1);
            if (!lastElapsedDate.isBefore(firstInstallmentDate)) {
                result.putAll(generateDateMap(firstInstallmentDate, lastElapsedDate));
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

    private int resolvePaymentIndex(final LocalDate date, final int firstPeriodDayOffset) {
        return (int) ChronoUnit.DAYS.between(expectedDisbursementDate, date) - firstPeriodDayOffset;
    }

    /**
     * Turns the walk into the rows the schedule is read from. Every figure here is either a field of the day or the
     * difference of two of them - the arithmetic all happened in the walk, at full precision, and what is left is
     * deciding which columns a day is entitled to fill in.
     *
     * <p>
     * A day with no record at all leaves its actual columns empty: nothing is known about it yet. A day that elapsed
     * with nothing on it reports nil rather than nothing, because a missed instalment is a fact about the loan.
     */
    private List<ProjectedPayment> buildPayments(final List<AmortizationDay> days) {
        final BigDecimal fee = discountFeeAmount.getAmount();
        final List<ProjectedPayment> result = new ArrayList<>(days.size() + 1);
        result.add(createDisbursementPayment());

        for (final AmortizationDay day : days) {
            final boolean known = day.hasPayment() || day.elapsed();
            result.add(new ProjectedPayment(day.dayIndex(), day.date(), day.paymentsLeft(), money(day.billedInstalment()),
                    day.discountFactor(), money(day.npvValue()), money(day.balance()), known ? money(day.actualBalance()) : null,
                    money(day.normExpected()), known ? money(day.paidAmount() == null ? BigDecimal.ZERO : day.paidAmount()) : null,
                    known ? money(day.normActual()) : null,
                    day.hasPayment() ? money(day.normActual().subtract(day.normExpected(), mc)) : null,
                    money(fee.subtract(day.aggNormExpected(), mc)), known ? money(fee.subtract(day.aggNormActual(), mc)) : null));
        }

        addPrincipalAdjustments(result, aggregatePrincipalAdjustmentsByDate());
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

    private ProjectedPayment createDisbursementPayment() {
        final Money negDisbursement = netDisbursementAmount.negated(mc);
        return new ProjectedPayment(0, expectedDisbursementDate, 0L, negDisbursement, BigDecimal.ONE, negDisbursement,
                netDisbursementAmount, netDisbursementAmount, null, null, null, null, discountFeeAmount, discountFeeAmount);
    }

    private Money money(final BigDecimal amount) {
        return Money.of(currency, amount, mc);
    }

    public record ActualPayment(LocalDate date, Money amount) {
    }

    /** A rate change: the day the new rate takes effect and the rate itself, as a percentage. */
    public record RateChange(LocalDate effectiveDate, BigDecimal periodPaymentRate) {
    }

    /** Principal re-injected on a date by an over-refunding credit balance refund. */
    public record PrincipalAdjustment(LocalDate date, Money amount) {
    }

    public static String getModelVersion() {
        return MODEL_VERSION;
    }
}
