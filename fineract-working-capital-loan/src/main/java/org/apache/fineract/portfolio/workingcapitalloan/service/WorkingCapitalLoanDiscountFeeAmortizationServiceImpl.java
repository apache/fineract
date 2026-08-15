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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelation;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkingCapitalLoanDiscountFeeAmortizationServiceImpl implements WorkingCapitalLoanDiscountFeeAmortizationService {

    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final ExternalIdFactory externalIdFactory;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;
    private final WorkingCapitalLoanTransactionFinder transactionFinder;

    @Override
    @Transactional
    public void processDiscountFeeAmortization(final WorkingCapitalLoan loan, final LocalDate transactionDate) {
        // Evaluate the schedule target against the discount in force on the COB date being processed. During a COB
        // fast-forward (catch-up over skipped days) this keeps a replayed day that precedes a discount fee adjustment
        // from reacting to that not-yet-effective adjustment, so the resulting amortization adjustment is never dated
        // before the discount adjustment that caused it.
        final BigDecimal scheduleAmortization = calculateScheduleAmortization(loan, transactionDate);
        // Fully paid (obligations met) or overpaid: recognize the whole discount immediately. The schedule carries an
        // NPV residual that a lump-sum payoff never fully consumes, so close must recognize the full discount, not the
        // schedule amount. Written-off / charge-off states follow a separate write-off accounting path and are excluded
        // here.
        final boolean fullyPaid = loan.getLoanStatus().isOverpaid() || loan.getLoanStatus().isClosedObligationsMet();
        // Derive the already-amortized income from the (non-reversed) amortization transactions rather than the cached
        // balance, so the recalculation stays correct after backdated payments, reversals or schedule config changes.
        final BigDecimal alreadyPosted = queryNetAmortized(loan.getId());

        if (MathUtil.isZero(scheduleAmortization) && !fullyPaid && MathUtil.isZero(alreadyPosted)) {
            log.debug("Skipping discount fee amortization for WC loan [{}] - no amortization on schedule", loan.getId());
            return;
        }

        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount()
                : BigDecimal.ZERO;

        final BigDecimal amortizationAmount = fullyPaid && MathUtil.isGreaterThanZero(discount) ? discount.subtract(alreadyPosted)
                : scheduleAmortization.subtract(alreadyPosted);

        if (MathUtil.isZero(amortizationAmount)) {
            log.debug("Skipping discount fee amortization for WC loan [{}] - no new amount to amortize (schedule={}, posted={})",
                    loan.getId(), scheduleAmortization, alreadyPosted);
            return;
        }

        // On a charged-off loan the recognized amortization is routed to the charge-off expense account instead of
        // discount-fee income (handled by the accounting processor via the isChargedOff flag).
        if (MathUtil.isGreaterThanZero(amortizationAmount)) {
            final WorkingCapitalLoanTransaction amortizationTxn = WorkingCapitalLoanTransaction.discountFeeAmortization(loan,
                    amortizationAmount, transactionDate, externalIdFactory.create());
            transactionRepository.saveAndFlush(amortizationTxn);
            if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
                accountingProcessor.postJournalEntriesForDiscountFeeAmortization(loan, amortizationTxn,
                        transactionFinder.isAfterActiveChargeOffForAccountingRouting(loan, amortizationTxn));
            }
        } else {
            final BigDecimal adjustmentAmount = amortizationAmount.negate();
            final WorkingCapitalLoanTransaction adjustmentTxn = WorkingCapitalLoanTransaction.discountFeeAmortizationAdjustment(loan,
                    adjustmentAmount, transactionDate, externalIdFactory.create());
            linkToTriggeringDiscountAdjustment(loan, adjustmentTxn);
            transactionRepository.saveAndFlush(adjustmentTxn);
            if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
                accountingProcessor.postJournalEntriesForDiscountFeeAmortizationAdjustment(loan, adjustmentTxn,
                        transactionFinder.isAfterActiveChargeOffForAccountingRouting(loan, adjustmentTxn));
            }
        }

        recalculateRealizedIncome(loan);

        log.debug("Posted discount fee amortization of {} for WC loan [{}]", amortizationAmount, loan.getId());
    }

    @Override
    @Transactional
    public void recalculateRealizedIncome(final WorkingCapitalLoan loan) {
        if (loan.getBalance() == null) {
            return;
        }
        // Requires any amortization transaction posts/reversals to be flushed first, so the aggregate sees them.
        loan.getBalance().setRealizedIncomeFromDiscountFee(queryNetAmortized(loan.getId()));
    }

    private BigDecimal queryNetAmortized(final Long loanId) {
        return transactionRepository.sumNetAmortization(loanId, LoanTransactionType.DISCOUNT_FEE_AMORTIZATION,
                LoanTransactionType.DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT);
    }

    private BigDecimal calculateScheduleAmortization(final WorkingCapitalLoan loan, final LocalDate cobDate) {
        final MathContext mc = MoneyHelper.getMathContext();
        return scheduleRepositoryWrapper.readModel(loan.getId(), mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan))
                .map(model -> model.totalActualAmortizationWithDiscount(discountInForceOn(loan, model, cobDate))).orElse(BigDecimal.ZERO);
    }

    /**
     * The total discount fee in force on {@code cobDate}. The model's discount is the latest (net of every adjustment);
     * adding back the non-reversed discount fee adjustments dated after the COB date yields the discount that was
     * effective on a replayed day, so a fast-forward does not evaluate amortization against a not-yet-effective change.
     */
    private BigDecimal discountInForceOn(final WorkingCapitalLoan loan, final ProjectedAmortizationScheduleModel model,
            final LocalDate cobDate) {
        final BigDecimal notYetEffective = transactionRepository.sumDiscountFeeAdjustmentsAfter(loan.getId(),
                LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT, cobDate);
        return model.discountFeeAmount().getAmount().add(notYetEffective);
    }

    private void linkToTriggeringDiscountAdjustment(final WorkingCapitalLoan loan,
            final WorkingCapitalLoanTransaction amortizationAdjustment) {
        transactionRepository.findActiveByTypeOrderByIdDesc(loan.getId(), LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT).stream().findFirst()
                .ifPresent(discountAdjustment -> amortizationAdjustment.getLoanTransactionRelations()
                        .add(new WorkingCapitalLoanTransactionRelation(amortizationAdjustment, discountAdjustment,
                                LoanTransactionRelationTypeEnum.RELATED)));
    }
}
