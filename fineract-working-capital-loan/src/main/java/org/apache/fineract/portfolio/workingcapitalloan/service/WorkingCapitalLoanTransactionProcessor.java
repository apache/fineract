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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelation;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanTransactionProcessor {

    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanAllocationRequestFactory allocationRequestFactory;
    private final WorkingCapitalLoanPaymentAllocationProcessor allocationProcessor;
    private final WorkingCapitalLoanAllocationApplier allocationApplier;
    private final WorkingCapitalLoanBalanceUpdater balanceUpdater;
    private final WorkingCapitalLoanTransactionReprocessingService transactionReprocessingService;
    private final WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;
    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;
    private final WorkingCapitalLoanDelinquencyRangeScheduleService delinquencyRangeScheduleService;
    private final WorkingCapitalLoanLifecycleStateMachine stateMachine;
    private final WorkingCapitalLoanDiscountFeeAmortizationService discountFeeAmortizationService;
    private final WorkingCapitalLoanChargeAccrualService chargeAccrualService;

    public WorkingCapitalLoanTransactionAllocation processRepaymentLikeTransaction(final WorkingCapitalLoan loan,
            final WorkingCapitalLoanTransaction transaction, final LoanTransactionType transactionType, final LocalDate transactionDate,
            final BigDecimal transactionAmount) {
        final Long loanId = loan.getId();
        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loanId)
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        final List<WorkingCapitalLoanCharge> charges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loanId);

        WorkingCapitalLoanAllocationRequest allocationRequest;
        if (transactionType == LoanTransactionType.CHARGE_ADJUSTMENT) {
            final WorkingCapitalLoanTransactionRelation loanTransactionRelation = transaction.getLoanTransactionRelations().stream()
                    .filter(e -> e.getToCharge() != null).findFirst().orElseThrow();
            final WorkingCapitalLoanCharge adjustedCharge = loanTransactionRelation.getToCharge();
            // The factory honors a configured CHARGE_ADJUSTMENT allocation order when the product defines one;
            // otherwise it scopes the adjustment to its own charge (no principal) so a not-yet-due charge is not
            // diverted onto principal.
            allocationRequest = allocationRequestFactory.buildForChargeAdjustment(loan, balance, charges, adjustedCharge, transactionDate,
                    transactionAmount);
        } else {
            // Decide the allocation across penalty/fee/principal following the loan's configured payment allocation
            // order (principal-only when no order is configured), then materialize it onto the charges and refresh the
            // balance.
            allocationRequest = allocationRequestFactory.build(loan, balance, charges, transactionDate, transactionAmount, transactionType);
        }
        final WorkingCapitalLoanAllocationPlan allocationPlan = allocationProcessor.plan(allocationRequest);
        final WorkingCapitalLoanTransactionAllocation allocation = allocationApplier.apply(transaction, null, allocationPlan, charges);
        balanceUpdater.apply(balance, allocationPlan);
        chargeRepository.saveAll(charges);
        balanceRepository.saveAndFlush(balance);
        allocationRepository.saveAndFlush(allocation);

        final boolean backdated = !isLastMonetaryAction(transaction);
        final boolean reprocessingWillRebuildSchedule = backdated && !charges.isEmpty();
        if (!reprocessingWillRebuildSchedule) {
            // The amortization model records the principal on its actual day and recalculates forward.
            amortizationScheduleWriteService.applyRepayment(loan, transactionDate, allocationPlan.principalPortion());
        }
        if (backdated) {
            transactionReprocessingService.reprocessTransactions(loan);
            // A changed chronological order can redistribute principal across days in ways the incremental
            // applyRepayment below can't express, so the delinquency schedule needs a full rebuild too.
            delinquencyRangeScheduleService.reprocessDelinquencySchedule(loan);
        } else {
            delinquencyRangeScheduleService.applyRepayment(loan, transactionDate, transactionAmount);
        }
        // Breach schedule is maintained incrementally here; reprocessing does not rebuild it.
        breachScheduleService.applyRepayment(loanId, transactionDate, transactionAmount);

        stateMachine.determineAndTransition(loan, transactionDate);
        triggerInlineAmortizationIfLoanClosed(loan, transactionDate);
        // On early closure the loan leaves the COB scope, so any charge whose due-date accrual has not been posted yet
        // is accrued as of the closing date to make sure the income is recognized before the loan is closed.
        chargeAccrualService.accrueOnClosure(loan, transactionDate);

        return allocation;
    }

    /**
     * Whether nothing monetary sorts after this transaction: no later non-reversed transaction, and no active charge
     * due on or after its date. When it holds, an incremental balance and schedule update is enough, because no other
     * allocation can depend on this transaction. When it does not, the whole history has to be re-allocated.
     */
    public boolean isLastMonetaryAction(final WorkingCapitalLoanTransaction transaction) {
        final Long loanId = transaction.getWcLoan().getId();
        final OffsetDateTime createdDateTime = transaction.getCreatedDate().isPresent() ? transaction.getCreatedDate().get()
                : DateUtils.getAuditOffsetDateTime();
        return !transactionRepository.existsLaterTransaction(loanId, transaction.getTransactionDate(), createdDateTime)
                && !chargeRepository.existsActiveChargeDueOnOrAfter(loanId, transaction.getTransactionDate(), createdDateTime);
    }

    public void triggerInlineAmortizationIfLoanClosed(final WorkingCapitalLoan loan, final LocalDate transactionDate) {
        if ((loan.getLoanStatus().isClosed() || loan.getLoanStatus().isOverpaid())
                && loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            final BigDecimal discount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount()
                    : null;
            final boolean adjustmentNeeded = loan.getBalance() != null
                    && MathUtil.isGreaterThanZero(loan.getBalance().getRealizedIncomeFromDiscountFee());

            if (MathUtil.isGreaterThanZero(discount) || adjustmentNeeded) {
                discountFeeAmortizationService.processDiscountFeeAmortization(loan, transactionDate);
            }
        }
    }
}
