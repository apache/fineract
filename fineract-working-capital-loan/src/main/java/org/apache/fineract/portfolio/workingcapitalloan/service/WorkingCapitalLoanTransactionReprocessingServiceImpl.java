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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanUndoChargeOffBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanDiscountFeeAmortizationTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionComparator;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargePaidByRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAdjustTransactionEventPublisher.WorkingCapitalLoanTransactionAdjustment;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAmortizationScheduleWriteService.PrincipalAdjustment;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAmortizationScheduleWriteService.PrincipalPayment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkingCapitalLoanTransactionReprocessingServiceImpl implements WorkingCapitalLoanTransactionReprocessingService {

    private static final Comparator<WorkingCapitalLoanTransaction> TRANSACTION_ORDER = WorkingCapitalLoanTransactionComparator.TRANSACTION_ORDER;

    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanTransactionAllocator transactionAllocator;
    private final WorkingCapitalLoanChargePaidByRepository chargePaidByRepository;
    private final WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final WorkingCapitalLoanTransactionRelationRepository transactionRelationRepository;
    private final WorkingCapitalLoanDiscountFeeAmortizationService discountFeeAmortizationService;
    private final WorkingCapitalLoanAdjustTransactionEventPublisher adjustTransactionEventPublisher;
    private final WorkingCapitalLoanTransactionDataFactory transactionDataFactory;

    @Override
    public void reprocessTransactions(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanTransaction> allTransactions = transactionRepository
                .findByWcLoan_IdOrderByTransactionDateAscIdAsc(loan.getId());
        reprocessTransactions(loan, allTransactions);
    }

    @Override
    public void reprocessTransactions(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanTransaction> allTransactions) {
        reprocessFully(loan, allTransactions);
    }

    @Override
    public void reprocessChargeFreeSuffix(final WorkingCapitalLoan loan, final LocalDate boundaryDate,
            final WorkingCapitalLoanTransaction reversedTransaction) {
        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loan.getId()).orElse(null);
        if (balance == null) {
            log.debug("Skipping suffix reprocessing for WC loan {}: no balance to recompute", loan.getId());
            return;
        }

        // The suffix rewind is exact only for the charge-free case. If charges are present (the caller should have
        // routed elsewhere, but guard anyway), or a credit balance refund sits in the suffix, fall back to the proven
        // full reset+replay.
        final List<WorkingCapitalLoanCharge> charges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());
        if (!charges.isEmpty()) {
            reprocessTransactions(loan);
            return;
        }

        // Load ONLY the suffix. The prefix - potentially thousands of rows on an old loan - is never read; the
        // pre-suffix balance is instead recovered by rewinding the suffix off the current balance.
        final List<WorkingCapitalLoanTransaction> suffix = transactionRepository
                .findByWcLoan_IdAndTransactionDateGreaterThanEqualOrderByTransactionDateAscIdAsc(loan.getId(), boundaryDate);

        final boolean suffixHasCreditBalanceRefund = suffix.stream()
                .anyMatch(txn -> !txn.isReversed() && txn.getTypeOf() == LoanTransactionType.CREDIT_BALANCE_REFUND);
        // A refund's split is re-derived against the running overpayment, which the rewind cannot reconstruct from the
        // stored allocation alone: hand the whole history to the full replay instead.
        if (suffixHasCreditBalanceRefund) {
            reprocessTransactions(loan);
            return;
        }

        // Money-movers in the suffix are rewound then replayed. Prior-reversed transactions are already excluded from
        // the live balance, so they are neither rewound nor replayed; the just-reversed transaction is the exception -
        // its contribution is still in the balance, so it is rewound (below) but never replayed. Charge-off is replayed
        // (amount / lift / routing) but not rewound — it does not move the balance.
        final List<WorkingCapitalLoanTransaction> suffixMoneyMovers = suffix.stream()
                .filter(txn -> !txn.isReversed() && txn.getTransactionType().isRepaymentType()).sorted(TRANSACTION_ORDER).toList();
        final List<WorkingCapitalLoanTransaction> replaySet = suffix.stream()
                .filter(txn -> !txn.isReversed() && (txn.getTransactionType().isRepaymentType() || isChargeOff(txn)))
                .sorted(TRANSACTION_ORDER).toList();

        // 1) Rewind the suffix contributions off the live balance to recover the pre-suffix (prefix-only) state. The
        // allocation is read straight off each (eagerly-loaded) transaction now that both sides of the association are
        // kept in sync on creation.
        for (final WorkingCapitalLoanTransaction txn : suffixMoneyMovers) {
            rewindChargeFreeBalance(balance, txn.getAllocation());
        }
        if (reversedTransaction != null) {
            rewindChargeFreeBalance(balance, reversedTransaction.getAllocation());
        }

        // 2) The reversed transaction's principal is gone for good: drop it from the schedule (no matching replay).
        if (reversedTransaction != null) {
            final WorkingCapitalLoanTransactionAllocation reversedAllocation = reversedTransaction.getAllocation();
            if (reversedAllocation != null) {
                amortizationScheduleWriteService.applyRepaymentUndo(loan, reversedTransaction.getTransactionDate(),
                        MathUtil.nullToZero(reversedAllocation.getPrincipalPortion()));
            }
        }

        // 3) Forward-replay the suffix onto the rewound balance, re-planning each allocation and mutating it in place.
        final boolean accountingEnabled = loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization();
        final boolean chargeOffInSuffix = replaySet.stream().anyMatch(this::isChargeOff);
        boolean afterChargeOff = loan.isChargedOff() && !chargeOffInSuffix;
        boolean afterLiftedChargeOff = false;
        WorkingCapitalLoanTransaction liftedChargeOffTransaction = null;
        final Map<Long, WorkingCapitalLoanCharge> chargesById = Map.of();
        final List<WorkingCapitalLoanTransactionAllocation> updatedAllocations = new ArrayList<>();
        // The replay mutates the allocations in place, so the "before" side of the adjustment pair must be read now.
        final Map<Long, WorkingCapitalLoanTransactionData> preReplaySnapshots = captureSnapshots(replaySet);
        final List<WorkingCapitalLoanTransaction> adjustedTransactions = new ArrayList<>();
        for (final WorkingCapitalLoanTransaction txn : replaySet) {
            if (isChargeOff(txn)) {
                final boolean stillChargedOff = replayChargeOff(loan, balance, txn, accountingEnabled, updatedAllocations,
                        adjustedTransactions);
                afterChargeOff = stillChargedOff;
                afterLiftedChargeOff = !stillChargedOff;
                if (afterLiftedChargeOff) {
                    liftedChargeOffTransaction = txn;
                }
                continue;
            }

            final WorkingCapitalLoanTransactionAllocator.Result allocated = transactionAllocator.allocate(loan, balance, charges,
                    chargesById, txn);
            updatedAllocations.add(allocated.allocation());

            // The schedule only needs touching when the principal portion actually shifted: swap the stale principal
            // (captured before the allocation was mutated in place) for the recomputed one. The model fully rebuilds
            // from its payment list on each call, so touching only the changed transactions still lands on the same
            // schedule a from-scratch rebuild would produce.
            if (allocated.allocationChanged()) {
                if (allocated.hadStoredAllocation()) {
                    amortizationScheduleWriteService.applyRepaymentUndo(loan, txn.getTransactionDate(),
                            allocated.previousPrincipalPortion());
                }
                amortizationScheduleWriteService.applyRepayment(loan, txn.getTransactionDate(), allocated.plan().principalPortion());
            }

            if (allocated.allocationChanged() && allocated.hadStoredAllocation()) {
                adjustedTransactions.add(txn);
            }

            if (accountingEnabled) {
                final boolean chargeOffRoutingMayHaveChanged = afterChargeOff || afterLiftedChargeOff;
                postOrRestateJournalEntries(loan, txn, allocated.allocation(), afterChargeOff, allocated.hadStoredAllocation(),
                        allocated.allocationChanged(), chargeOffRoutingMayHaveChanged);
            }
        }

        allocationRepository.saveAll(updatedAllocations);
        balanceRepository.saveAndFlush(balance);

        adjustTransactionEventPublisher.publishReprocessed(loan.getId(), buildAdjustments(preReplaySnapshots, adjustedTransactions));
        notifyChargeOffLifted(loan, liftedChargeOffTransaction);
    }

    /**
     * Removes one transaction's charge-free contribution from the running balance: its stored principal portion from
     * principal-paid and its stored overpayment portion from the overpayment. The two are disjoint and sum to the
     * transaction amount, so this exactly undoes what the forward apply added.
     */
    private void rewindChargeFreeBalance(final WorkingCapitalLoanBalance balance,
            final WorkingCapitalLoanTransactionAllocation storedAllocation) {
        if (storedAllocation == null) {
            // No stored allocation means the transaction never reached the balance - a freshly booked backdated
            // payment handed straight to this replay to be allocated for the first time. There is nothing to unwind,
            // and treating its principal as zero here would wrongly strip the whole amount off the overpayment.
            return;
        }
        // Both portions are read straight off the allocation rather than derived from the transaction amount. Deriving
        // the overpayment as "amount minus principal" only holds while fee and penalty are necessarily zero, which is
        // true here but silently stops being true the moment this is reused outside the charge-free case.
        final BigDecimal principalPortion = MathUtil.nullToZero(storedAllocation.getPrincipalPortion());
        final BigDecimal overpayPortion = MathUtil.nullToZero(storedAllocation.getOverpaymentPortion());
        balance.setPrincipalPaid(MathUtil.subtract(balance.getPrincipalPaid(), principalPortion));
        balance.setOverpaymentAmount(MathUtil.subtract(balance.getOverpaymentAmount(), overpayPortion));
    }

    private void reprocessFully(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanTransaction> allTransactions) {
        final List<WorkingCapitalLoanCharge> charges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());

        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loan.getId()).orElse(null);
        if (balance == null) {
            log.debug("Skipping transaction reprocessing for WC loan {}: no balance to recompute", loan.getId());
            return;
        }

        // Re-allocate every non-reversed repayment-like transaction (and replay each credit balance refund and
        // charge-off) in chronological order.
        final List<WorkingCapitalLoanTransaction> replayable = allTransactions.stream().filter(
                txn -> !txn.isReversed() && (txn.getTransactionType().isRepaymentType() || isCreditBalanceRefund(txn) || isChargeOff(txn)))
                .sorted(TRANSACTION_ORDER).toList();

        // Last point where the "before" side of the adjustment pair is readable: the charge-paid-by rows are deleted
        // below and the allocations are mutated in place.
        final Map<Long, WorkingCapitalLoanTransactionData> preReplaySnapshots = captureSnapshots(replayable);

        // Reset the paid distribution; the principal/fee/penalty totals stay, only how much of each is paid is
        // recomputed. The charge-paid-by rows are output for the old distribution, so drop them here and let the
        // reset+replay below rebuild them (mirrors the core module's clear-and-rewrite of LoanChargePaidBy).
        balance.setPrincipalPaid(BigDecimal.ZERO);
        balance.setFeePaid(BigDecimal.ZERO);
        balance.setPenaltyPaid(BigDecimal.ZERO);
        balance.setOverpaymentAmount(BigDecimal.ZERO);
        balance.setPrincipalAdjustment(BigDecimal.ZERO);
        for (final WorkingCapitalLoanCharge charge : charges) {
            charge.setAmountPaid(BigDecimal.ZERO);
            charge.setPaid(false);
        }
        chargePaidByRepository.deleteByLoanId(loan.getId());

        final List<WorkingCapitalLoanChargePaidBy> rebuiltChargesPaidBy = new ArrayList<>();

        final Map<Long, WorkingCapitalLoanCharge> chargesById = charges.stream()
                .collect(Collectors.toMap(WorkingCapitalLoanCharge::getId, Function.identity()));

        final boolean accountingEnabled = loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization();
        final List<PrincipalPayment> principalPayments = new ArrayList<>();
        final List<PrincipalAdjustment> principalAdjustments = new ArrayList<>();
        final List<WorkingCapitalLoanTransactionAllocation> updatedAllocations = new ArrayList<>();
        final List<WorkingCapitalLoanTransaction> adjustedTransactions = new ArrayList<>();
        boolean afterChargeOff = false;
        boolean afterLiftedChargeOff = false;
        WorkingCapitalLoanTransaction liftedChargeOffTransaction = null;
        for (final WorkingCapitalLoanTransaction txn : replayable) {
            if (isChargeOff(txn)) {
                final boolean stillChargedOff = replayChargeOff(loan, balance, txn, accountingEnabled, updatedAllocations,
                        adjustedTransactions);
                afterChargeOff = stillChargedOff;
                afterLiftedChargeOff = !stillChargedOff;
                if (afterLiftedChargeOff) {
                    liftedChargeOffTransaction = txn;
                }
                continue;
            }

            final boolean hadStoredAllocation;
            final boolean allocationChanged;
            final WorkingCapitalLoanTransactionAllocation updatedAllocation;
            if (isCreditBalanceRefund(txn)) {
                // A refund is an inverse money movement: it allocates to no charge or principal bucket. Re-derive its
                // outcome against the overpayment recomputed so far - only the part of the refund no longer backed by
                // an overpayment becomes due principal; the rest is its overpayment portion. A refund still fully
                // funded by the overpayment moves no principal at all.
                final WorkingCapitalLoanTransactionAllocation storedAllocation = txn.getAllocation();
                hadStoredAllocation = storedAllocation != null;
                final BigDecimal excessPrincipal = replayCreditBalanceRefund(balance, txn.getTransactionAmount());
                final BigDecimal overpaymentPortion = MathUtil.subtract(txn.getTransactionAmount(), excessPrincipal);
                allocationChanged = !hadStoredAllocation
                        || !creditBalanceRefundAllocationMatches(storedAllocation, excessPrincipal, overpaymentPortion);
                updatedAllocation = applyCreditBalanceRefundAllocation(txn, excessPrincipal, overpaymentPortion);
                updatedAllocations.add(updatedAllocation);
                principalAdjustments.add(new PrincipalAdjustment(txn.getTransactionDate(), excessPrincipal));
            } else {
                // Allocate against the live (running) balance/charges so the decision is made against the remaining
                // outstanding after the previously replayed transactions. The transaction is an immutable ledger fact:
                // only its separately stored allocation is recomputed and updated in place - the row is never reversed
                // or replaced by reprocessing.
                final WorkingCapitalLoanTransactionAllocator.Result allocated = transactionAllocator.allocate(loan, balance, charges,
                        chargesById, txn);
                hadStoredAllocation = allocated.hadStoredAllocation();
                allocationChanged = allocated.allocationChanged();
                updatedAllocation = allocated.allocation();
                updatedAllocations.add(updatedAllocation);
                rebuiltChargesPaidBy.addAll(allocated.chargesPaidBy());
                principalPayments.add(new PrincipalPayment(txn.getTransactionDate(), allocated.plan().principalPortion(),
                        txn.getCreatedDate().orElse(null), txn.getId()));
            }

            if (allocationChanged && hadStoredAllocation) {
                adjustedTransactions.add(txn);
            }

            if (accountingEnabled) {
                final boolean chargeOffRoutingMayHaveChanged = afterChargeOff || afterLiftedChargeOff;
                postOrRestateJournalEntries(loan, txn, updatedAllocation, afterChargeOff, hadStoredAllocation, allocationChanged,
                        chargeOffRoutingMayHaveChanged);
            }
        }

        allocationRepository.saveAll(updatedAllocations);
        chargeRepository.saveAll(charges);
        chargePaidByRepository.saveAll(rebuiltChargesPaidBy);
        // The post-replay snapshot below re-reads these rows with a query, so they must already be in the database.
        chargePaidByRepository.flush();
        balanceRepository.saveAndFlush(balance);

        // The amortization schedule depends on the principal paid per day, which can shift when the principal portions
        // are re-allocated; rebuild it from the recomputed portions, then re-inject the over-refunded principal on the
        // date of the refund that created it.
        amortizationScheduleWriteService.rebuildScheduleFromPrincipalPayments(loan, principalPayments, principalAdjustments);

        adjustTransactionEventPublisher.publishReprocessed(loan.getId(), buildAdjustments(preReplaySnapshots, adjustedTransactions));
        notifyChargeOffLifted(loan, liftedChargeOffTransaction);
    }

    private void notifyChargeOffLifted(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction liftedChargeOffTransaction) {
        if (liftedChargeOffTransaction == null) {
            return;
        }
        businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanUndoChargeOffBusinessEvent(loan));
        adjustTransactionEventPublisher.publishReversal(loan.getId(), liftedChargeOffTransaction);
    }

    /**
     * Transactions without a stored allocation are left out: a first allocation is a creation rather than an
     * adjustment, and already has its own transaction-type event.
     */
    private Map<Long, WorkingCapitalLoanTransactionData> captureSnapshots(final List<WorkingCapitalLoanTransaction> transactions) {
        return adjustTransactionEventPublisher
                .snapshots(transactions.stream().filter(txn -> txn.getId() != null && txn.getAllocation() != null).toList());
    }

    private List<WorkingCapitalLoanTransactionAdjustment> buildAdjustments(
            final Map<Long, WorkingCapitalLoanTransactionData> preReplaySnapshots,
            final List<WorkingCapitalLoanTransaction> adjustedTransactions) {
        final List<WorkingCapitalLoanTransaction> paired = adjustedTransactions.stream()
                .filter(txn -> preReplaySnapshots.containsKey(txn.getId())).toList();
        final Map<Long, WorkingCapitalLoanTransactionData> postReplaySnapshots = adjustTransactionEventPublisher.snapshots(paired);
        return paired.stream().map(txn -> new WorkingCapitalLoanTransactionAdjustment(preReplaySnapshots.get(txn.getId()),
                postReplaySnapshots.get(txn.getId()))).toList();
    }

    private boolean isCreditBalanceRefund(final WorkingCapitalLoanTransaction txn) {
        return txn.getTypeOf() == LoanTransactionType.CREDIT_BALANCE_REFUND;
    }

    private boolean isChargeOff(final WorkingCapitalLoanTransaction txn) {
        return txn.getTypeOf() == LoanTransactionType.CHARGE_OFF;
    }

    private void postOrRestateJournalEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final WorkingCapitalLoanTransactionAllocation allocation, final boolean afterChargeOff, final boolean hadStoredAllocation,
            final boolean allocationChanged, final boolean chargeOffRoutingMayHaveChanged) {
        if (!hadStoredAllocation) {
            accountingProcessor.postJournalEntries(loan, txn, allocation, afterChargeOff);
            return;
        }
        if (allocationChanged || chargeOffRoutingMayHaveChanged) {
            accountingProcessor.restateJournalEntries(loan, txn, allocation, afterChargeOff);
        }
    }

    /**
     * @return {@code false} when outstanding is zero and the charge-off is lifted.
     */
    private boolean replayChargeOff(final WorkingCapitalLoan loan, final WorkingCapitalLoanBalance balance,
            final WorkingCapitalLoanTransaction chargeOffTransaction, final boolean accountingEnabled,
            final List<WorkingCapitalLoanTransactionAllocation> updatedAllocations,
            final List<WorkingCapitalLoanTransaction> adjustedTransactions) {
        final BigDecimal chargeOffAmount = balance.getTotalOutstanding();
        final BigDecimal principalPortion = balance.getPrincipalOutstanding();
        final BigDecimal feePortion = balance.getFeeOutstanding();
        final BigDecimal penaltyPortion = balance.getPenaltyOutstanding();
        final BigDecimal overpaymentPortion = MathUtil.nullToZero(balance.getOverpaymentAmount());

        if (!MathUtil.isGreaterThanZero(chargeOffAmount)) {
            // Mirrors the explicit undo-charge-off path: lifting the charge-off must also reverse its linked final
            // discount-fee amortization, or that transaction's journal entries keep crediting the charge-off expense
            // account on a loan that is no longer charged off.
            discountFeeAmortizationService.undoDiscountFeeAmortizationOnChargeOff(loan, chargeOffTransaction);

            chargeOffTransaction.setReversed(true);
            chargeOffTransaction.setReversedOnDate(DateUtils.getBusinessLocalDate());
            transactionRepository.saveAndFlush(chargeOffTransaction);
            loan.liftChargeOff();
            loanRepository.saveAndFlush(loan);
            if (accountingEnabled) {
                accountingProcessor.postReversalJournalEntries(loan, chargeOffTransaction);
            }
            return false;
        }

        final WorkingCapitalLoanTransactionAllocation storedAllocation = chargeOffTransaction.getAllocation();
        if (storedAllocation == null) {
            throw new IllegalStateException("Charge-off transaction " + chargeOffTransaction.getId() + " has no allocation to reprocess");
        }

        final boolean amountChanged = MathUtil.nullToZero(chargeOffTransaction.getTransactionAmount()).compareTo(chargeOffAmount) != 0;
        final boolean allocationChanged = !chargeOffAllocationMatches(storedAllocation, principalPortion, feePortion, penaltyPortion,
                overpaymentPortion);

        chargeOffTransaction.updateAmount(chargeOffAmount);
        storedAllocation.setPrincipalPortion(principalPortion);
        storedAllocation.setFeeChargesPortion(feePortion);
        storedAllocation.setPenaltyChargesPortion(penaltyPortion);
        storedAllocation.setOverpaymentPortion(overpaymentPortion);
        updatedAllocations.add(storedAllocation);
        transactionRepository.save(chargeOffTransaction);

        // Charge-off is the only branch that restates the transaction amount itself and not just its split, hence the
        // amountChanged half of the condition.
        if (amountChanged || allocationChanged) {
            adjustedTransactions.add(chargeOffTransaction);
        }

        if (accountingEnabled && (amountChanged || allocationChanged)) {
            accountingProcessor.restateJournalEntries(loan, chargeOffTransaction, storedAllocation, true);
        }

        restateFinalDiscountFeeAmortization(loan, balance, chargeOffTransaction, accountingEnabled);

        return true;
    }

    /**
     * Replays the charge-off's final lump-sum discount-fee amortization (see
     * {@code WorkingCapitalLoanDiscountFeeAmortizationServiceImpl#processFinalDiscountFeeAmortizationOnChargeOff}), in
     * place, against the current discount pool. A no-op when nothing is linked (no discount was ever charged off) or
     * the recomputed amount has not moved.
     * <p>
     * The link is looked up regardless of the amortization's own reversed state: a prior replay can have zeroed the
     * pool and reversed it (see below), and a later backdated change (e.g. undoing the discount-fee adjustment that
     * zeroed it) can bring the pool back up, needing the same transaction revived rather than left behind for good.
     * </p>
     */
    private void restateFinalDiscountFeeAmortization(final WorkingCapitalLoan loan, final WorkingCapitalLoanBalance balance,
            final WorkingCapitalLoanTransaction chargeOffTransaction, final boolean accountingEnabled) {
        final List<WorkingCapitalLoanTransactionRelation> linkedAmortizations = transactionRelationRepository
                .findAllByToTransactionAndFromTransactionTransactionType(chargeOffTransaction,
                        LoanTransactionType.DISCOUNT_FEE_AMORTIZATION);
        if (linkedAmortizations.isEmpty()) {
            return;
        }

        final BigDecimal netDiscountPool = MathUtil.subtract(MathUtil.nullToZero(balance.getTotalDiscountFee()),
                MathUtil.nullToZero(balance.getTotalDiscountFeeAdjustment()));
        final BigDecimal netAmortized = transactionRepository.sumNetAmortization(loan.getId(),
                LoanTransactionType.DISCOUNT_FEE_AMORTIZATION, LoanTransactionType.DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT);

        for (final WorkingCapitalLoanTransactionRelation relation : linkedAmortizations) {
            final WorkingCapitalLoanTransaction amortizationTxn = relation.getFromTransaction();
            final boolean wasReversed = amortizationTxn.isReversed();
            // sumNetAmortization only counts non-reversed transactions, so a currently-reversed amortization
            // contributes nothing to netAmortized already - only a live one's own (stale) amount needs excluding from
            // what is "already realized elsewhere" to recompute the target as if the final push were happening now.
            final BigDecimal ownLiveContribution = wasReversed ? BigDecimal.ZERO
                    : MathUtil.nullToZero(amortizationTxn.getTransactionAmount());
            final BigDecimal realizedElsewhere = MathUtil.subtract(netAmortized, ownLiveContribution);
            final BigDecimal newAmount = MathUtil.subtract(netDiscountPool, realizedElsewhere).max(BigDecimal.ZERO);

            if (!MathUtil.isGreaterThanZero(newAmount)) {
                if (!wasReversed) {
                    // The discount pool no longer leaves anything to push to expense: the final transaction becomes
                    // moot.
                    amortizationTxn.setReversed(true);
                    amortizationTxn.setReversedOnDate(DateUtils.getBusinessLocalDate());
                    transactionRepository.saveAndFlush(amortizationTxn);
                    if (accountingEnabled) {
                        accountingProcessor.postReversalJournalEntries(loan, amortizationTxn);
                    }
                    final WorkingCapitalLoanTransactionData reversedTxnData = transactionDataFactory.create(amortizationTxn);
                    businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanAdjustTransactionBusinessEvent(
                            WorkingCapitalLoanAdjustTransactionBusinessEvent.Data.reversal(reversedTxnData), loan.getId()));
                }
                continue;
            }

            final boolean amountChanged = MathUtil.nullToZero(amortizationTxn.getTransactionAmount()).compareTo(newAmount) != 0;
            if (wasReversed) {
                // The pool moved back above zero: revive the transaction this same charge-off already created rather
                // than leaving it behind reversed forever.
                amortizationTxn.setReversed(false);
                amortizationTxn.setReversedOnDate(null);
            }
            amortizationTxn.updateAmount(newAmount);
            transactionRepository.save(amortizationTxn);
            if (accountingEnabled && (amountChanged || wasReversed)) {
                accountingProcessor.restateJournalEntriesForDiscountFeeAmortization(loan, amortizationTxn, true);
            }
            if (amountChanged || wasReversed) {
                // Reuses the same event the transaction's original creation fires, rather than a dedicated
                // "restated"/"revived" event: it announces the same thing either way -- this discount-fee
                // amortization transaction is live with this amount.
                businessEventNotifierService.notifyPostBusinessEvent(
                        new WorkingCapitalLoanDiscountFeeAmortizationTransactionBusinessEvent(amortizationTxn, loan.getId()));
            }
        }

        discountFeeAmortizationService.recalculateRealizedIncome(loan);
    }

    private boolean chargeOffAllocationMatches(final WorkingCapitalLoanTransactionAllocation allocation, final BigDecimal principalPortion,
            final BigDecimal feePortion, final BigDecimal penaltyPortion, final BigDecimal overpaymentPortion) {
        return allocationPortionsEqual(allocation.getPrincipalPortion(), principalPortion)
                && allocationPortionsEqual(allocation.getFeeChargesPortion(), feePortion)
                && allocationPortionsEqual(allocation.getPenaltyChargesPortion(), penaltyPortion)
                && allocationPortionsEqual(allocation.getOverpaymentPortion(), overpaymentPortion);
    }

    private boolean creditBalanceRefundAllocationMatches(final WorkingCapitalLoanTransactionAllocation allocation,
            final BigDecimal principalPortion, final BigDecimal overpaymentPortion) {
        return allocationPortionsEqual(allocation.getPrincipalPortion(), principalPortion)
                && allocationPortionsEqual(allocation.getOverpaymentPortion(), overpaymentPortion);
    }

    private boolean allocationPortionsEqual(final BigDecimal a, final BigDecimal b) {
        return MathUtil.nullToZero(a).compareTo(MathUtil.nullToZero(b)) == 0;
    }

    /**
     * Writes the split the replay derived for the refund, reusing the stored allocation row when there is one. The
     * returned allocation must be collected into the persisted batch: mutating it in place is not enough for a row that
     * already exists but is not otherwise part of the save path.
     */
    private WorkingCapitalLoanTransactionAllocation applyCreditBalanceRefundAllocation(final WorkingCapitalLoanTransaction txn,
            final BigDecimal principalPortion, final BigDecimal overpaymentPortion) {
        final WorkingCapitalLoanTransactionAllocation existing = txn.getAllocation();
        if (existing == null) {
            return WorkingCapitalLoanTransactionAllocation.forCreditBalanceRefund(txn, principalPortion, overpaymentPortion);
        }
        existing.setPrincipalPortion(principalPortion);
        existing.setFeeChargesPortion(BigDecimal.ZERO);
        existing.setPenaltyChargesPortion(BigDecimal.ZERO);
        existing.setOverpaymentPortion(overpaymentPortion);
        return existing;
    }

    /**
     * Replays a refund against the overpayment recomputed so far and returns the over-refund excess: the part of the
     * refund no longer backed by overpayment, which becomes due principal. Zero for a refund still fully funded by the
     * overpayment.
     */
    private BigDecimal replayCreditBalanceRefund(final WorkingCapitalLoanBalance balance, final BigDecimal refundAmount) {
        final BigDecimal fullRefund = MathUtil.nullToZero(refundAmount);
        final BigDecimal overpayment = MathUtil.nullToZero(balance.getOverpaymentAmount());
        // Only the part of the refund that exceeds the recomputed overpayment becomes due principal; the remainder is
        // simply consumed from the overpayment.
        final BigDecimal excessPrincipal = MathUtil.subtract(fullRefund, overpayment).max(BigDecimal.ZERO);
        final BigDecimal overpaymentConsumed = MathUtil.subtract(fullRefund, excessPrincipal);
        balance.setOverpaymentAmount(MathUtil.subtract(overpayment, overpaymentConsumed).max(BigDecimal.ZERO));
        if (MathUtil.isGreaterThanZero(excessPrincipal)) {
            // The over-refunded amount is re-injected as due principal to be paid (a principal adjustment), not
            // amortised. The real principal paid is left untouched, so the loan's outstanding principal rises by
            // exactly the excess.
            balance.setPrincipalAdjustment(MathUtil.add(MathUtil.nullToZero(balance.getPrincipalAdjustment()), excessPrincipal));
        }
        return excessPrincipal;
    }
}
