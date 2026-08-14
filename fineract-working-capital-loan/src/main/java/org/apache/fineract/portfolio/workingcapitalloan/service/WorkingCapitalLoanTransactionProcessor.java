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
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargePaidByRepository;
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
    private final WorkingCapitalLoanChargePaidByRepository chargePaidByRepository;
    private final WorkingCapitalLoanTransactionAllocator transactionAllocator;
    private final WorkingCapitalLoanTransactionReprocessingService transactionReprocessingService;
    private final WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;
    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;
    private final WorkingCapitalLoanDelinquencyRangeScheduleService delinquencyRangeScheduleService;
    private final WorkingCapitalLoanLifecycleStateMachine stateMachine;
    private final WorkingCapitalLoanDiscountFeeAmortizationService discountFeeAmortizationService;
    private final WorkingCapitalLoanChargeAccrualService chargeAccrualService;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final WorkingCapitalLoanTransactionFinder transactionFinder;

    public void processRepaymentLikeTransaction(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction transaction,
            final LocalDate transactionDate, final BigDecimal transactionAmount) {
        final Long loanId = loan.getId();
        final List<WorkingCapitalLoanCharge> charges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loanId);
        final boolean backdated = !isLastMonetaryAction(transaction);

        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loanId)
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));

        final boolean isLoanOverpaidWithCurrentTransaction = MathUtil.isGreaterThanZero(balance.getOverpaymentAmount())
                || transactionAmount.compareTo(balance.getPrincipalOutstanding()) > 0;

        // A backdated transaction that can reshuffle later allocations is handed straight to reprocessing, which
        // allocates this transaction as part of the replay. Charges reshuffle across the whole history (full rebuild);
        // charge-free overpayment reshuffles only from the insertion point on (suffix rebuild). A money-mover before an
        // active charge-off also goes through reprocessing so the charge-off snapshot and routing can be restated.
        // Either way, computing an incremental allocation first would only be rewound and redone.
        final boolean affectsChargeOffSnapshot = transactionFinder.isBeforeActiveChargeOff(loan, transaction);
        final boolean reprocessRebuildsEverything = backdated
                && (!charges.isEmpty() || isLoanOverpaidWithCurrentTransaction || affectsChargeOffSnapshot);

        final WorkingCapitalLoanTransactionAllocation allocation;
        if (reprocessRebuildsEverything) {
            if (charges.isEmpty()) {
                transactionReprocessingService.reprocessChargeFreeSuffix(loan, transactionDate, null);
            } else {
                transactionReprocessingService.reprocessTransactions(loan);
            }
            // A changed chronological order can redistribute principal across days in ways an incremental apply can't
            // express, so the delinquency schedule needs a full rebuild too.
            delinquencyRangeScheduleService.reprocessDelinquencySchedule(loan);
            // Reprocessing rebuilt this transaction's allocation from scratch and linked it back onto the transaction,
            // so it is readable directly for the accounting posting - no separate lookup needed.
            allocation = transaction.getAllocation();
            if (allocation == null) {
                // The replay allocates every transaction it is handed, so a missing allocation means it never ran -
                // in practice because the loan has no balance row to recompute, which both entry points skip on. The
                // transaction would otherwise be left with no allocation, no balance movement and no schedule entry,
                // and would either fail further downstream in the accounting posting or, with accounting off, be
                // committed in that broken state unnoticed.
                throw new IllegalStateException("Reprocessing did not allocate WC loan transaction " + transaction.getId() + " on loan "
                        + loanId + "; the loan most likely has no balance row, which reprocessing skips on");
            }
        } else {
            // Decide and materialize the allocation for this single transaction against the current balance (the
            // charge-adjustment routing lives in the allocator), then persist the mutated aggregate.
            final WorkingCapitalLoanTransactionAllocator.Result allocated = transactionAllocator.allocate(loan, balance, charges,
                    transaction);
            allocation = allocated.allocation();

            chargeRepository.saveAll(charges);
            balanceRepository.saveAndFlush(balance);
            allocationRepository.saveAndFlush(allocation);
            chargePaidByRepository.saveAll(allocated.chargesPaidBy());

            // The amortization model records the principal on its actual day and recalculates forward.
            amortizationScheduleWriteService.applyRepayment(loan, transactionDate, allocated.plan().principalPortion());

            if (backdated) {
                // Reaching here backdated means the loan is charge-free and stays within principal, so the allocation
                // is principal-only regardless of sequence and the incremental apply above is already the final answer
                // - no allocation replay is needed. Delinquency is date-driven rather than allocation-driven, though,
                // so inserting a payment into an earlier period still requires rebuilding that schedule.
                delinquencyRangeScheduleService.reprocessDelinquencySchedule(loan);
            } else {
                delinquencyRangeScheduleService.applyRepayment(loan, transactionDate, transactionAmount);
            }

            if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
                accountingProcessor.postJournalEntries(loan, transaction, allocation,
                        transactionFinder.isAfterActiveChargeOffForAccountingRouting(loan, transaction));
            }
        }

        // Breach schedule is maintained incrementally here; reprocessing does not rebuild it.
        breachScheduleService.applyRepayment(loanId, transactionDate, transactionAmount);

        stateMachine.determineAndTransition(loan, transactionDate);
        triggerInlineAmortizationIfLoanClosed(loan, transactionDate);
        // On early closure the loan leaves the COB scope, so any charge whose due-date accrual has not been posted yet
        // is accrued as of the closing date to make sure the income is recognized before the loan is closed.
        chargeAccrualService.accrueOnClosure(loan, transactionDate);
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
