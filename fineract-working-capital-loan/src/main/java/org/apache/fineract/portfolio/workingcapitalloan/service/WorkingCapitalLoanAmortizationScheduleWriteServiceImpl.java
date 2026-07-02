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
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleCalculator;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanEirNotCalculableException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
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
    private final ProjectedAmortizationScheduleCalculator calculator;
    private final ProjectedAmortizationScheduleModelParserService parserService;

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
        Validate.notNull(loan, "loan must not be null");
        Validate.notNull(disbursedAmount, "disbursedAmount must not be null");
        Validate.notNull(disbursementDate, "disbursementDate must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final BigDecimal discount = getWorkingCapitalLoanDiscountAmount(loan);
        final BigDecimal totalPaymentVolume = loan.getTotalPaymentVolume() != null ? loan.getTotalPaymentVolume() : BigDecimal.ZERO;
        final BigDecimal periodPaymentRate = loan.getLoanProductRelatedDetails() != null
                ? loan.getLoanProductRelatedDetails().getPeriodPaymentRate()
                : null;
        final Integer npvDayCount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getNpvDayCount()
                : null;

        Validate.isTrue(totalPaymentVolume.signum() > 0, "totalPaymentVolume must be positive");
        Validate.notNull(periodPaymentRate, "periodPaymentRate must not be null");
        Validate.notNull(npvDayCount, "npvDayCount must not be null");

        assertEirCalculable(discount, disbursedAmount, totalPaymentVolume, periodPaymentRate, npvDayCount, mc);

        return ProjectedAmortizationScheduleModel.generate(discount, disbursedAmount, totalPaymentVolume, periodPaymentRate, npvDayCount,
                disbursementDate, mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan), DateUtils.getBusinessLocalDate());
    }

    @Override
    public void generateAndSaveAmortizationScheduleOnApproval(final WorkingCapitalLoan loan) {
        generateAndSaveForApprovedLoanState(loan);
    }

    @Override
    public void regenerateAmortizationScheduleOnUndoDisbursal(final WorkingCapitalLoan loan) {
        generateAndSaveForApprovedLoanState(loan);
    }

    @Override
    public BigDecimal getWorkingCapitalLoanDiscountAmount(WorkingCapitalLoan loan) {
        BigDecimal discount = BigDecimal.ZERO;
        if (loan.getLoanProductRelatedDetails() != null) {
            if (loan.getLoanStatus().isSubmittedAndPendingApproval() && loan.getLoanProductRelatedDetails().getDiscountProposed() != null) {
                discount = loan.getLoanProductRelatedDetails().getDiscountProposed();
            } else if (loan.getLoanStatus().isApproved() && loan.getLoanProductRelatedDetails().getDiscountApproved() != null) {
                discount = loan.getLoanProductRelatedDetails().getDiscountApproved();
            } else if (loan.getLoanStatus().isActive() && loan.getLoanProductRelatedDetails().getDiscount() != null) {
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

        assertEirCalculable(discount, netDisbursementAmount, totalPaymentVolume, periodPaymentRate, npvDayCount, mc);

        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(discount, netDisbursementAmount,
                totalPaymentVolume, periodPaymentRate, npvDayCount, expectedDisbursementDate, mc,
                WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan), DateUtils.getBusinessLocalDate());
        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    /** Guards paths that bypass request validation, before {@code generate()} materialises the full schedule. */
    private void assertEirCalculable(final BigDecimal discount, final BigDecimal netDisbursementAmount, final BigDecimal totalPaymentVolume,
            final BigDecimal periodPaymentRate, final int npvDayCount, final MathContext mc) {
        if (!ProjectedAmortizationScheduleModel.isEirCalculable(discount, netDisbursementAmount, totalPaymentVolume, periodPaymentRate,
                npvDayCount, mc)) {
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
    public void regenerateAmortizationScheduleOnRateChange(final WorkingCapitalLoan loan, final BigDecimal newRate) {
        Validate.notNull(loan, "loan must not be null");
        Validate.notNull(newRate, "newRate must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final CurrencyData currency = WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan);
        final ProjectedAmortizationScheduleModel model = scheduleRepositoryWrapper.readModel(loan.getId(), mc, currency)
                .orElseThrow(() -> new IllegalStateException("Projected amortization schedule is not found for loan " + loan.getId()));

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final LocalDate loanDisbursementDate = resolveLoanDisbursementDate(loan);
        final int splitDayIndex = (int) ChronoUnit.DAYS.between(loanDisbursementDate, businessDate);
        final LocalDate modelRateChangeDate = model.expectedDisbursementDate().plusDays(splitDayIndex);

        model.clearLastRateSegment();

        // A pathological rate can make the re-solved split-term schedule non-computable (zero daily payment, over-cap
        // term, non-convergent EIR); surface those as a domain-rule error.
        try {
            calculator.applyRateChange(model, newRate, modelRateChangeDate);
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

        final List<ProjectedAmortizationScheduleModel.ActualPayment> preservedPayments = currentModel.snapshotActualPayments();
        // Preserve principal re-injected by an over-refund CBR: restating from a fresh model would otherwise drop the
        // CBR-date overlay, leaving the schedule out of step with the loan balance that still owns the adjustment.
        final List<ProjectedAmortizationScheduleModel.PrincipalAdjustment> preservedAdjustments = currentModel
                .snapshotPrincipalAdjustments();

        final BigDecimal disbursedAmount = resolveActualDisbursedAmount(loan);
        final LocalDate disbursementDate = resolveActualDisbursementDate(loan);

        final ProjectedAmortizationScheduleModel restatedModel = generateProjectedAmortizationScheduleModel(loan, disbursedAmount,
                disbursementDate);
        preservedPayments.stream().sorted(Comparator.comparing(ProjectedAmortizationScheduleModel.ActualPayment::date))
                .forEach(payment -> restatedModel.applyPayment(payment.date(), payment.amount().getAmount()));
        preservedAdjustments
                .forEach(adjustment -> restatedModel.applyPrincipalAdjustment(adjustment.date(), adjustment.amount().getAmount()));

        scheduleRepositoryWrapper.writeModel(loan, restatedModel);
    }

    @Override
    public void rebuildScheduleFromPrincipalPayments(final WorkingCapitalLoan loan, final List<PrincipalPayment> principalPayments,
            final List<PrincipalAdjustment> principalAdjustments) {
        Validate.notNull(loan, "loan must not be null");
        Validate.notNull(principalPayments, "principalPayments must not be null");
        Validate.notNull(principalAdjustments, "principalAdjustments must not be null");

        final BigDecimal disbursedAmount = resolveActualDisbursedAmount(loan);
        final LocalDate disbursementDate = resolveActualDisbursementDate(loan);

        final ProjectedAmortizationScheduleModel model = generateProjectedAmortizationScheduleModel(loan, disbursedAmount,
                disbursementDate);
        principalPayments.stream().filter(payment -> payment.amount() != null && payment.amount().signum() > 0)
                .sorted(Comparator.comparing(PrincipalPayment::date))
                .forEach(payment -> model.applyPayment(payment.date(), payment.amount()));
        principalAdjustments.stream().filter(adjustment -> adjustment.amount() != null && adjustment.amount().signum() > 0)
                .sorted(Comparator.comparing(PrincipalAdjustment::date))
                .forEach(adjustment -> model.applyPrincipalAdjustment(adjustment.date(), adjustment.amount()));

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

    private LocalDate resolveLoanDisbursementDate(final WorkingCapitalLoan loan) {
        if (loan.getDisbursementDetails() != null && !loan.getDisbursementDetails().isEmpty()) {
            final LocalDate actualDate = loan.getDisbursementDetails().getFirst().getActualDisbursementDate();
            if (actualDate != null) {
                return actualDate;
            }
        }
        throw new IllegalStateException("Active loan " + loan.getId() + " has no actual disbursement date");
    }

    private BigDecimal resolveActualDisbursedAmount(final WorkingCapitalLoan loan) {
        if (loan.getDisbursementDetails() != null && !loan.getDisbursementDetails().isEmpty()
                && loan.getDisbursementDetails().getFirst().getActualAmount() != null) {
            return loan.getDisbursementDetails().getFirst().getActualAmount();
        }
        return BigDecimal.ZERO;
    }

    private LocalDate resolveActualDisbursementDate(final WorkingCapitalLoan loan) {
        if (loan.getDisbursementDetails() != null && !loan.getDisbursementDetails().isEmpty()) {
            return loan.getDisbursementDetails().getFirst().getActualDisbursementDate();
        }
        return null;
    }
}
