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
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
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
import org.springframework.data.domain.Pageable;
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
        recalculateOverpaidOnDate(loan, transaction);
        recalculateSettlementDates(loan);
        triggerInlineAmortizationIfLoanClosed(loan, transactionDate);
        // On early closure the loan leaves the COB scope, so any charge whose due-date accrual has not been posted yet
        // is accrued as of the day the loan settled - not the day the closing transaction happens to carry.
        chargeAccrualService.accrueOnClosure(loan, closureIncomeDate(loan, transactionDate));
    }

    /**
     * Re-derives {@code overpaidOnDate}, correcting the optimistic value the lifecycle state machine stamps when a loan
     * first becomes overpaid.
     * <p>
     * The rule is: the date of the earliest non-reversed repayment-like transaction whose allocation carries an
     * overpayment portion -- the first day money was actually on the loan in excess of what was due. That is not the
     * same as the date of whichever transaction triggered the transition, which is all the state machine knows. Two
     * cases make them differ:
     * <ul>
     * <li>a backdated transaction tips an already-settled loan into overpayment: the transition is stamped with the
     * transaction's own earlier date, but the loan was only fully paid later;</li>
     * <li>an undo removes the earliest of several overpaying transactions and leaves the loan overpaid: no transition
     * fires at all, so the stored date survives on a day that no longer carries an overpayment.</li>
     * </ul>
     * The guard skips the query when the stored value cannot have gone stale. A transaction dated on the business date
     * is not backdated, so no reallocation can have moved the earliest overpaying transaction; and one dated after the
     * stored date cannot become the new earliest either.
     * <p>
     * Finding nothing is a deliberate no-op rather than a clear. A loan whose overpayment came from the one-directional
     * clamp in {@code WorkingCapitalLoanWritePlatformServiceImpl.updateBalanceForDiscountChange}, before that path
     * reprocessed allocations, has no transaction carrying an overpayment portion at all -- its date is left as the
     * backfill found it, which for those rows is null.
     */
    public void recalculateOverpaidOnDate(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction transaction) {
        // Mirrors determineAndTransition's own tolerance of a not-yet-established status: it returns without
        // transitioning when the loan has no balance, which leaves the status unset on a freshly built loan.
        if (loan.getLoanStatus() == null || !loan.getLoanStatus().isOverpaid()) {
            return;
        }
        final LocalDate storedDate = loan.getOverpaidOnDate();
        final LocalDate transactionDate = transaction.getTransactionDate();
        final boolean storedDateMayBeStale = storedDate == null
                || (!transactionDate.isAfter(storedDate) && !transactionDate.isEqual(DateUtils.getBusinessLocalDate()));
        if (!storedDateMayBeStale) {
            return;
        }
        final List<TransactionDateAndAmountHolder> firstOverpayingTransaction = transactionRepository
                .findFirstActiveTransactionDateAndAmountByLoanIdWithOverpaidPortion(loan.getId(),
                        LoanTransactionType.getRepaymentLikeTransactionTypes(), Pageable.ofSize(1));
        firstOverpayingTransaction.stream().findFirst().map(TransactionDateAndAmountHolder::transactionDate)
                .ifPresent(loan::setOverpaidOnDate);
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
        if (loan.isClosedWrittenOff() || loan.isClosedObligationsMet() || loan.isOverpaid()) {
            final BigDecimal discount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount()
                    : null;
            final boolean adjustmentNeeded = loan.getBalance() != null
                    && MathUtil.isGreaterThanZero(loan.getBalance().getRealizedIncomeFromDiscountFee());

            if (MathUtil.isGreaterThanZero(discount) || adjustmentNeeded) {
                discountFeeAmortizationService.processDiscountFeeAmortization(loan, settlementDate(loan, transactionDate));
            }
        }
    }

    /**
     * Re-derives the two dates published as {@code timeline.closedOnDate} and {@code timeline.actualMaturityDate} from
     * the loan's history, correcting the optimistic values the lifecycle state machine stamps when a loan settles.
     * <p>
     * The machine stamps whichever transaction triggered the transition, which is the day the loan settled only when
     * that transaction is also the one that finished the settlement. A backdated repayment completing an already
     * part-paid loan is not, and neither is a surplus payment, a refund or an undo on an already-settled loan. It is
     * the same defect {@link #recalculateOverpaidOnDate} fixes on {@code overpaidOnDate}, reaching the API the same way
     * through {@code WorkingCapitalLoanSummaryMapper.buildTimeline}.
     * <p>
     * Must be called wherever a transaction can settle the loan, alongside
     * {@link #recalculateOverpaidOnDate(WorkingCapitalLoan, WorkingCapitalLoanTransaction)}: the repayment path here, a
     * discount fee adjustment and a transaction undo. {@link #settlementDate} reads what this writes, so a site that
     * settles a loan without correcting these dates would date the closing amortization and the closure accruals from
     * the raw stamp.
     * <p>
     * A credit balance refund is deliberately not one of those sites. It is a real money movement rather than a
     * reallocation of one, and the account is not closed until it happens, so it closes the loan on its own date the
     * way core's {@code DefaultLoanLifecycleStateMachine} does.
     */
    public void recalculateSettlementDates(final WorkingCapitalLoan loan) {
        // Mirrors determineAndTransition's own tolerance of a not-yet-established status: a freshly built loan with no
        // balance is left unset.
        final LoanStatus status = loan.getLoanStatus();
        if (status == null || (!status.isOverpaid() && !status.isClosedObligationsMet())) {
            return;
        }
        final LocalDate settledOn = settlementDateFromHistory(loan);
        if (settledOn == null) {
            return;
        }
        // An overpaid loan has met its obligations too, so it carries a maturity date even though it is not closed.
        loan.setMaturedOnDate(settledOn);
        if (status.isClosedObligationsMet()) {
            loan.setClosedOnDate(settledOn);
        }
    }

    /**
     * The day the loan's obligations were met, read off its history rather than off the event in hand: the last
     * repayment the loan actually needed. Money is allocated in date order, so everything after that payment - a
     * surplus payment, and the refund or undo that takes the surplus away again - is excess, and excess says nothing
     * about when the loan was settled. Being a function of history alone it lands on the same day whichever event
     * recalculates it, including when that moves the answer earlier.
     * <p>
     * A discount fee adjustment can settle a loan too, by reducing what is owed to what has already been paid. It is
     * not a repayment-like transaction, so the query cannot see it, and the stamp is the only record of that day - kept
     * for exactly as long as an adjustment on that day backs it up, so a payment-derived stamp cannot outlive the
     * payment it came from.
     */
    private LocalDate settlementDateFromHistory(final WorkingCapitalLoan loan) {
        final LocalDate obligationsMetOn = transactionRepository.findLatestActiveTransactionDateWithDuePortion(loan.getId(),
                LoanTransactionType.getRepaymentLikeTransactionTypes());
        final LocalDate stamped = loan.getMaturedOnDate();
        if (DateUtils.isAfter(stamped, obligationsMetOn)
                && transactionRepository.existsActiveTransactionOn(loan.getId(), LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT, stamped)) {
            return stamped;
        }
        return obligationsMetOn;
    }

    /**
     * The day the loan actually became settled, which is the day the whole discount is earned - not the date of
     * whichever transaction happened to trigger the recalculation.
     * <p>
     * The two are the same only when the triggering transaction is also the chronologically last one. A backdated
     * repayment that completes an already part-paid loan is not: it carries an earlier date, while the money that
     * finished the settlement arrived later. Dating the closing amortization on the trigger would recognize the income
     * before that cash came in.
     * <p>
     * Mirrors core's {@code getFinalAccrualTransactionDate}: it reads the loan's settlement state rather than the
     * transaction in hand. It reads {@code maturedOnDate}, which
     * {@link #recalculateSettlementDates(WorkingCapitalLoan)} re-derives immediately before this runs and maintains on
     * an overpaid loan as well as a closed one, so the income, the closure accruals and the closure the API reports are
     * all dated from the same answer rather than drifting apart.
     * <p>
     * Never earlier than the triggering transaction. Settlement can be completed by something that is not a repayment
     * at all - a write-off, a waiver, a charge adjustment - and for those the trigger's own date remains the best
     * answer, so this only ever moves the recognition later, never earlier than it is booked today.
     */
    private LocalDate settlementDate(final WorkingCapitalLoan loan, final LocalDate transactionDate) {
        final LocalDate settledOn = settledOn(loan);
        return settledOn == null || !settledOn.isAfter(transactionDate) ? transactionDate : settledOn;
    }

    /**
     * The day the loan settled, for the income that belongs to the closure itself rather than to whichever transaction
     * revealed it.
     * <p>
     * Unlike {@link #settlementDate} this is not floored at the transaction in hand, because the two answer different
     * questions. The discount is measured against a schedule, and a schedule changes on the day it is changed, so an
     * adjustment arriving after the settlement earns its correction on its own later day. A charge accrual is not: it
     * recognizes income the closure leaves unrecognized, and the closure happened when the loan met its obligations. A
     * surplus payment, a credit balance refund or an undo arriving afterwards hands back or removes money the loan
     * never needed, so none of them closed it and none of them may date its income.
     * <p>
     * Falls back to {@code transactionDate} for a loan with no settlement date of its own - a write-off, where the
     * closing transaction is the settlement.
     */
    public LocalDate closureIncomeDate(final WorkingCapitalLoan loan, final LocalDate transactionDate) {
        final LocalDate settledOn = settledOn(loan);
        return settledOn == null ? transactionDate : settledOn;
    }

    private static LocalDate settledOn(final WorkingCapitalLoan loan) {
        return loan.isOverpaid() || loan.isClosedObligationsMet() ? loan.getMaturedOnDate() : null;
    }
}
