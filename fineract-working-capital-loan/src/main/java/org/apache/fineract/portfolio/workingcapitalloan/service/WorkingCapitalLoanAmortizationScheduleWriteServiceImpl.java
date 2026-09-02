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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodPaymentRateChange;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanEirNotCalculableException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: This is a temporary testing implementation. In the real flow, the amortization schedule
// will be generated and saved as part of the loan lifecycle (approve/disburse) — not via a
// standalone endpoint. The parameters will come from the loan entity + product, not from the
// request body. Replace this once the full WCL lifecycle is implemented.
@Service
@RequiredArgsConstructor
@Transactional
public class WorkingCapitalLoanAmortizationScheduleWriteServiceImpl implements WorkingCapitalLoanAmortizationScheduleWriteService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;
    private final WorkingCapitalLoanPeriodPaymentRateChangeRepository rateChangeRepository;

    // Deliberately a different tie-break from the allocation replay's (which leads with the submitted-on date, matching
    // the core loan module). This stream mixes payments with rate changes, and creation time is the only key both carry
    // - a rate change has no submitted-on date.
    //
    // The leading tie-break is the event type, not creation time, so that a rate change effective on a date is in force
    // for the payments made on it: a backdated change is created long after the payments it governs, and ranking by
    // creation time would make the schedule depend on when the change happened to be entered. Creation time then orders
    // events of the same type, where two payments sharing a date are aggregated by date before the projection anyway,
    // so their relative order is immaterial.
    private static final Comparator<ScheduleEvent> REPLAY_ORDER = Comparator.comparing(ScheduleEvent::date)
            .thenComparingInt(event -> event instanceof RateChangeEvent ? 0 : 1)
            .thenComparing(ScheduleEvent::createdDate, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(ScheduleEvent::sourceId, Comparator.nullsFirst(Comparator.naturalOrder()));

    @Override
    public void generateAndSaveAmortizationSchedule(final Long loanId, final ProjectedAmortizationScheduleGenerateRequest request) {
        final WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        final MathContext mc = MoneyHelper.getMathContext();

        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(//
                request.getDiscountFeeAmount(), //
                request.getNetDisbursementAmount(), //
                request.getTotalPaymentVolume(), //
                request.getPeriodPaymentRate(), //
                request.getNpvDayCount(), //
                request.getExpectedDisbursementDate(), //
                mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan), DateUtils.getBusinessLocalDate());

        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    @Override
    public void generateAndSaveAmortizationScheduleOnDisbursement(final WorkingCapitalLoan loan, final BigDecimal disbursedAmount,
            final LocalDate disbursementDate) {
        final ProjectedAmortizationScheduleModel model = generateProjectedAmortizationScheduleModel(loan, disbursedAmount,
                disbursementDate);
        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    @NonNull
    private ProjectedAmortizationScheduleModel generateProjectedAmortizationScheduleModel(final WorkingCapitalLoan loan,
            final BigDecimal disbursedAmount, final LocalDate disbursementDate) {
        final BigDecimal periodPaymentRate = loan.getLoanProductRelatedDetails() != null
                ? loan.getLoanProductRelatedDetails().getPeriodPaymentRate()
                : null;
        return generateBaseModel(loan, disbursedAmount, disbursementDate, periodPaymentRate);
    }

    @NonNull
    private ProjectedAmortizationScheduleModel generateBaseModel(final WorkingCapitalLoan loan, final BigDecimal disbursedAmount,
            final LocalDate disbursementDate, final BigDecimal periodPaymentRate) {
        Validate.notNull(loan, "loan must not be null");
        Validate.notNull(disbursedAmount, "disbursedAmount must not be null");
        Validate.notNull(disbursementDate, "disbursementDate must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final BigDecimal discount = getWorkingCapitalLoanDiscountAmount(loan);
        final BigDecimal totalPaymentVolume = loan.getTotalPaymentVolume() != null ? loan.getTotalPaymentVolume() : BigDecimal.ZERO;
        final Integer npvDayCount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getNpvDayCount()
                : null;

        Validate.isTrue(totalPaymentVolume.signum() > 0, "totalPaymentVolume must be positive");
        Validate.notNull(periodPaymentRate, "periodPaymentRate must not be null");
        Validate.notNull(npvDayCount, "npvDayCount must not be null");

        assertEirCalculable(discount, disbursedAmount, totalPaymentVolume, periodPaymentRate, npvDayCount,
                loan.getLoanProduct().getCurrency(), mc);

        return ProjectedAmortizationScheduleModel.generate(discount, disbursedAmount, totalPaymentVolume, periodPaymentRate, npvDayCount,
                disbursementDate, mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan), DateUtils.getBusinessLocalDate());
    }

    /**
     * Rebuilds a schedule model from scratch preserving the loan's rate-change history: bases the model on the
     * <strong>original</strong> period payment rate (the rate the schedule was first generated at) and replays the
     * non-reversed rate changes and the given principal payments <strong>merged in date order</strong>. The merge
     * matters because {@link ProjectedAmortizationScheduleModel#applyRateChange} derives a segment's balance/discount
     * at the split from the payments received before it, so each rate change must see the payments that precede its
     * effective date. With no rate changes this is identical to a plain generate-and-replay at the current rate.
     */
    @NonNull
    private ProjectedAmortizationScheduleModel reconstructScheduleModel(final WorkingCapitalLoan loan,
            final List<PrincipalPayment> payments, final List<PrincipalAdjustment> adjustments) {
        final BigDecimal disbursedAmount = loan.getFirstActualDisbursementAmount();
        final LocalDate disbursementDate = loan.getFirstActualDisbursementDate();
        final List<WorkingCapitalLoanPeriodPaymentRateChange> rateChanges = rateChangeRepository
                .findByWorkingCapitalLoanIdAndReversedFalse(loan.getId());
        final ProjectedAmortizationScheduleModel model = generateBaseModel(loan, disbursedAmount, disbursementDate,
                resolveOriginalPeriodPaymentRate(loan));
        replayRateChangesAndPayments(model, rateChanges, payments);
        // Principal re-injected by an over-refunding credit balance refund is overlaid after the payment/rate-change
        // replay: it is not a payment competing for replay order, but a correction restoring principal the refund
        // handed back, so it lands on the schedule the replayed payments already produced.
        adjustments.stream().filter(adjustment -> adjustment.amount() != null && adjustment.amount().signum() > 0)
                .sorted(Comparator.comparing(PrincipalAdjustment::date))
                .forEach(adjustment -> model.applyPrincipalAdjustment(adjustment.date(), adjustment.amount()));
        return model;
    }

    /**
     * The rate the schedule was originally generated at, which is the rate the loan was created with and which nothing
     * moves afterwards.
     *
     * <p>
     * Read straight off the loan rather than out of the rate-change chain. The earliest change's {@code previousRate}
     * would normally answer the same, but only for a history that has been restated, so a loan carrying rows written
     * before restating existed would rebuild its base schedule at whatever rate that stale link happens to name.
     */
    private BigDecimal resolveOriginalPeriodPaymentRate(final WorkingCapitalLoan loan) {
        // Left null when the product details are missing, matching how generateBaseModel treats its other
        // product-detail reads.
        return loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getPeriodPaymentRate() : null;
    }

    private void replayRateChangesAndPayments(final ProjectedAmortizationScheduleModel model,
            final List<WorkingCapitalLoanPeriodPaymentRateChange> rateChanges, final List<PrincipalPayment> payments) {
        final Stream<ScheduleEvent> paymentEvents = payments.stream().filter(p -> p.amount() != null && p.amount().signum() > 0)
                .map(p -> new PaymentEvent(p.date(), p.amount(), p.createdDate(), p.transactionId()));
        final Stream<ScheduleEvent> rateChangeEvents = rateChanges.stream().map(change -> new RateChangeEvent(change.getEffectiveDate(),
                change.getNewRate(), change.getCreatedDate().orElse(null), change.getId()));

        // Replay every dated action in date-then-creation order, so each rate change sees the payments that precede its
        // split exactly as they were booked.
        Stream.concat(paymentEvents, rateChangeEvents).sorted(REPLAY_ORDER).forEach(event -> {
            switch (event) {
                case PaymentEvent p -> model.applyPayment(p.date(), p.amount());
                case RateChangeEvent r -> model.applyRateChange(r.newRate(), r.date(), DateUtils.getBusinessLocalDate());
            }
        });
    }

    /**
     * A dated schedule modifier replayed during reconstruction. Sealed so the dispatch in
     * {@link #replayRateChangesAndPayments} stays exhaustive: adding a new modifier type forces it to be handled there.
     */
    private sealed interface ScheduleEvent permits PaymentEvent, RateChangeEvent {

        LocalDate date();

        /** The source record's audit creation timestamp; the primary same-date tie-break. {@code null} when unknown. */
        OffsetDateTime createdDate();

        /** The source record's id; the final tie-break (mirrors core). {@code null} when unknown. */
        Long sourceId();
    }

    private record PaymentEvent(LocalDate date, BigDecimal amount, OffsetDateTime createdDate, Long sourceId) implements ScheduleEvent {
    }

    private record RateChangeEvent(LocalDate date, BigDecimal newRate, OffsetDateTime createdDate, Long sourceId) implements ScheduleEvent {
    }

    @Override
    public void generateAndSaveAmortizationScheduleOnApproval(final WorkingCapitalLoan loan) {
        generateAndSaveForApprovedLoanState(loan);
    }

    @Override
    public void regenerateAmortizationScheduleOnUndoDisbursal(final WorkingCapitalLoan loan) {
        generateAndSaveForApprovedLoanState(loan);
    }

    private BigDecimal getWorkingCapitalLoanDiscountAmount(WorkingCapitalLoan loan) {
        BigDecimal discount = BigDecimal.ZERO;
        if (loan.getLoanProductRelatedDetails() != null) {
            if (loan.isSubmittedAndPendingApproval() && loan.getLoanProductRelatedDetails().getDiscountProposed() != null) {
                discount = loan.getLoanProductRelatedDetails().getDiscountProposed();
            } else if (loan.isApproved() && loan.getLoanProductRelatedDetails().getDiscountApproved() != null) {
                discount = loan.getLoanProductRelatedDetails().getDiscountApproved();
            } else if (loan.getLoanProductRelatedDetails().getDiscount() != null) {
                discount = loan.getLoanProductRelatedDetails().getDiscount();
            }
        }
        return discount;
    }

    private void generateAndSaveForApprovedLoanState(final WorkingCapitalLoan loan) {
        Validate.notNull(loan, "loan must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final BigDecimal discount = getWorkingCapitalLoanDiscountAmount(loan);
        final BigDecimal totalPaymentVolume = loan.getBalance() != null && loan.getTotalPaymentVolume() != null
                ? loan.getTotalPaymentVolume()
                : BigDecimal.ZERO;
        final BigDecimal periodPaymentRate = loan.getLoanProductRelatedDetails() != null
                ? loan.getLoanProductRelatedDetails().getPeriodPaymentRate()
                : null;
        final Integer npvDayCount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getNpvDayCount()
                : null;

        final WorkingCapitalLoanDisbursementDetails detail = loan.getDisbursementDetails() != null
                && !loan.getDisbursementDetails().isEmpty() ? loan.getDisbursementDetails().getFirst() : null;
        final LocalDate expectedDisbursementDate = detail != null ? detail.getExpectedDisbursementDate() : null;

        final BigDecimal netDisbursementAmount;
        if (loan.getApprovedPrincipal() != null && loan.getApprovedPrincipal().compareTo(BigDecimal.ZERO) > 0) {
            netDisbursementAmount = loan.getApprovedPrincipal();
        } else {
            netDisbursementAmount = detail != null && detail.getExpectedAmount() != null ? detail.getExpectedAmount() : BigDecimal.ZERO;
        }

        Validate.isTrue(totalPaymentVolume.signum() > 0, "totalPaymentVolume must be positive");
        Validate.notNull(periodPaymentRate, "periodPaymentRate must not be null");
        Validate.notNull(npvDayCount, "npvDayCount must not be null");
        Validate.notNull(expectedDisbursementDate, "expectedDisbursementDate must not be null");
        Validate.isTrue(netDisbursementAmount.signum() > 0, "net disbursement amount for schedule must be positive");

        assertEirCalculable(discount, netDisbursementAmount, totalPaymentVolume, periodPaymentRate, npvDayCount,
                loan.getLoanProduct().getCurrency(), mc);

        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(discount, netDisbursementAmount,
                totalPaymentVolume, periodPaymentRate, npvDayCount, expectedDisbursementDate, mc,
                WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan), DateUtils.getBusinessLocalDate());
        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    /** Guards paths that bypass request validation, before {@code generate()} materialises the full schedule. */
    private void assertEirCalculable(final BigDecimal discount, final BigDecimal netDisbursementAmount, final BigDecimal totalPaymentVolume,
            final BigDecimal periodPaymentRate, final int npvDayCount, final MonetaryCurrency currency, final MathContext mc) {
        if (!ProjectedAmortizationScheduleModel.isEirCalculable(discount, netDisbursementAmount, totalPaymentVolume, periodPaymentRate,
                npvDayCount, currency, mc)) {
            throw new WorkingCapitalLoanEirNotCalculableException();
        }
    }

    @Override
    public void applyRepayment(final WorkingCapitalLoan loan, final LocalDate transactionDate, final BigDecimal repaymentAmount) {
        Validate.notNull(loan, "loan must not be null");
        Validate.notNull(transactionDate, "transactionDate must not be null");
        Validate.notNull(repaymentAmount, "repaymentAmount must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final ProjectedAmortizationScheduleModel model = scheduleRepositoryWrapper
                .readModel(loan.getId(), mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan))
                .orElseThrow(() -> new IllegalStateException("Projected amortization schedule is not found for loan " + loan.getId()));

        model.applyPayment(transactionDate, repaymentAmount);
        model.recalculateNetAmortizationAndDeferredBalanceFrom(transactionDate);

        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    @Override
    public void acknowledgeElapsedPeriods(final WorkingCapitalLoan loan, final LocalDate businessDate) {
        Validate.notNull(loan, "loan must not be null");
        Validate.notNull(businessDate, "businessDate must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        // Unlike the payment paths this runs for every loan the batch touches, including ones that never reached
        // disbursement, so a missing schedule is an ordinary outcome rather than a broken state.
        scheduleRepositoryWrapper.readModel(loan.getId(), mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan))
                .filter(model -> model.acknowledgeElapsedPeriods(businessDate))
                .ifPresent(model -> scheduleRepositoryWrapper.writeModel(loan, model));
    }

    @Override
    public void regenerateAmortizationScheduleOnRateChange(final WorkingCapitalLoan loan) {
        Validate.notNull(loan, "loan must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final CurrencyData currency = WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan);
        final ProjectedAmortizationScheduleModel currentModel = scheduleRepositoryWrapper.readModel(loan.getId(), mc, currency)
                .orElseThrow(() -> new IllegalStateException("Projected amortization schedule is not found for loan " + loan.getId()));

        // Rebuilt from scratch rather than split in place: rate changes are effective-dated, so a backdated one has to
        // land ahead of the changes that follow it, and applyRateChange drops every segment at or after its own split.
        // Reconstruction replays them all in effective-date order instead. The source transactions are not retained by
        // the model, so payments and adjustments are carried over from the model being replaced.
        final List<PrincipalPayment> preservedPayments = currentModel.snapshotActualPayments().stream()
                .map(payment -> new PrincipalPayment(payment.date(), payment.amount().getAmount(), null, null)).toList();
        final List<PrincipalAdjustment> preservedAdjustments = currentModel.snapshotPrincipalAdjustments().stream()
                .map(adjustment -> new PrincipalAdjustment(adjustment.date(), adjustment.amount().getAmount())).toList();

        // A pathological rate can make a re-solved segment non-computable (zero daily payment, over-cap term,
        // non-convergent EIR); surface those as a domain-rule error.
        final ProjectedAmortizationScheduleModel model;
        try {
            model = reconstructScheduleModel(loan, preservedPayments, preservedAdjustments);
        } catch (final IllegalStateException | IllegalArgumentException | ArithmeticException e) {
            throw new WorkingCapitalLoanEirNotCalculableException(e);
        }

        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    @Override
    public void applyDiscountFeeAdjustment(final WorkingCapitalLoan loan) {
        Validate.notNull(loan, "loan must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final CurrencyData currency = WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan);
        final ProjectedAmortizationScheduleModel currentModel = scheduleRepositoryWrapper.readModel(loan.getId(), mc, currency)
                .orElseThrow(() -> new IllegalStateException("Projected amortization schedule is not found for loan " + loan.getId()));

        // Reconstructed from the schedule model, which does not retain the source transactions.
        final List<PrincipalPayment> preservedPayments = currentModel.snapshotActualPayments().stream()
                .map(payment -> new PrincipalPayment(payment.date(), payment.amount().getAmount(), null, null)).toList();
        // Preserve principal re-injected by an over-refund CBR: restating from a fresh model would otherwise drop the
        // CBR-date overlay, leaving the schedule out of step with the loan balance that still owns the adjustment.
        final List<PrincipalAdjustment> preservedAdjustments = currentModel.snapshotPrincipalAdjustments().stream()
                .map(adjustment -> new PrincipalAdjustment(adjustment.date(), adjustment.amount().getAmount())).toList();

        final ProjectedAmortizationScheduleModel restatedModel = reconstructScheduleModel(loan, preservedPayments, preservedAdjustments);
        scheduleRepositoryWrapper.writeModel(loan, restatedModel);
    }

    @Override
    public void rebuildScheduleFromPrincipalPayments(final WorkingCapitalLoan loan, final List<PrincipalPayment> principalPayments,
            final List<PrincipalAdjustment> principalAdjustments) {
        Validate.notNull(loan, "loan must not be null");
        Validate.notNull(principalPayments, "principalPayments must not be null");
        Validate.notNull(principalAdjustments, "principalAdjustments must not be null");

        final ProjectedAmortizationScheduleModel model = reconstructScheduleModel(loan, principalPayments, principalAdjustments);
        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    @Override
    public void applyRepaymentUndo(final WorkingCapitalLoan loan, final LocalDate transactionDate, final BigDecimal repaymentAmount) {
        Validate.notNull(loan, "loan must not be null");
        Validate.notNull(transactionDate, "transactionDate must not be null");
        Validate.notNull(repaymentAmount, "repaymentAmount must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final ProjectedAmortizationScheduleModel model = scheduleRepositoryWrapper
                .readModel(loan.getId(), mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan))
                .orElseThrow(() -> new IllegalStateException("Projected amortization schedule is not found for loan " + loan.getId()));

        model.undoPayment(transactionDate, repaymentAmount);
        model.recalculateNetAmortizationAndDeferredBalanceFrom(transactionDate);

        scheduleRepositoryWrapper.writeModel(loan, model);
    }
}
