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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAmortizationScheduleWriteService.PrincipalAdjustment;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAmortizationScheduleWriteService.PrincipalPayment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkingCapitalLoanTransactionReprocessingServiceImpl implements WorkingCapitalLoanTransactionReprocessingService {

    // Replay order matches the standard loan transaction ordering, simplified for WC (no accrual/income posting):
    // transaction date, then submitted date, then id.
    private static final Comparator<WorkingCapitalLoanTransaction> TRANSACTION_ORDER = Comparator
            .comparing(WorkingCapitalLoanTransaction::getTransactionDate)
            .thenComparing(WorkingCapitalLoanTransaction::getSubmittedOnDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(WorkingCapitalLoanTransaction::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanPaymentAllocationProcessor allocationProcessor;
    private final WorkingCapitalLoanAllocationRequestFactory allocationRequestFactory;
    private final WorkingCapitalLoanAllocationApplier allocationApplier;
    private final WorkingCapitalLoanBalanceUpdater balanceUpdater;
    private final WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;

    @Override
    public void reprocessTransactions(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanTransaction> allTransactions = transactionRepository
                .findByWcLoan_IdOrderByTransactionDateAscIdAsc(loan.getId());
        reprocessTransactions(loan, allTransactions);
    }

    private void reprocessTransactions(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanTransaction> allTransactions) {
        final List<WorkingCapitalLoanCharge> charges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());

        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loan.getId()).orElse(null);
        if (balance == null) {
            log.debug("Skipping transaction reprocessing for WC loan {}: no balance to recompute", loan.getId());
            return;
        }

        // Reset the paid distribution; the principal/fee/penalty totals stay, only how much of each is paid is
        // recomputed.
        balance.setPrincipalPaid(BigDecimal.ZERO);
        balance.setFeePaid(BigDecimal.ZERO);
        balance.setPenaltyPaid(BigDecimal.ZERO);
        balance.setOverpaymentAmount(BigDecimal.ZERO);
        balance.setPrincipalAdjustment(BigDecimal.ZERO);
        for (final WorkingCapitalLoanCharge charge : charges) {
            charge.setAmountPaid(BigDecimal.ZERO);
            charge.setPaid(false);
        }

        final Map<Long, WorkingCapitalLoanCharge> chargesById = charges.stream()
                .collect(Collectors.toMap(WorkingCapitalLoanCharge::getId, Function.identity()));

        // Re-allocate every non-reversed repayment-like transaction (and replay each CBR) in chronological order.
        final List<WorkingCapitalLoanTransaction> replayable = allTransactions.stream()
                .filter(txn -> !txn.isReversed() && (txn.getTransactionType().isRepaymentType() || isCreditBalanceRefund(txn.getTypeOf())))
                .sorted(TRANSACTION_ORDER).toList();

        // Pre-load the existing allocations in one query rather than per transaction. Looking them up via the
        // repository (instead of txn.getAllocation()) also avoids the lazy inverse side being stale for the
        // transaction that just triggered the reprocessing, which would otherwise create a second allocation row and
        // violate the one-allocation-per-transaction unique constraint.
        // Guard the empty case (e.g. undoing the only repayment leaves nothing to replay): an empty IN (...) clause is
        // invalid SQL on some databases, and there is nothing to pre-load anyway.
        final Map<Long, WorkingCapitalLoanTransactionAllocation> allocationsByTxnId = replayable.isEmpty() ? Map.of()
                : allocationRepository.findByWcLoanTransactionIdIn(replayable.stream().map(WorkingCapitalLoanTransaction::getId).toList())
                        .stream().collect(Collectors.toMap(allocation -> allocation.getWcLoanTransaction().getId(), Function.identity()));

        final List<PrincipalPayment> principalPayments = new ArrayList<>();
        final List<PrincipalAdjustment> principalAdjustments = new ArrayList<>();
        final List<WorkingCapitalLoanTransactionAllocation> updatedAllocations = new ArrayList<>();
        for (final WorkingCapitalLoanTransaction txn : replayable) {
            if (isCreditBalanceRefund(txn.getTypeOf())) {
                // A CBR is an inverse-money-movement: it does not allocate to charges/principal buckets. Re-derive its
                // outcome against the overpayment recomputed so far - only the part of the refund that no longer has an
                // overpayment behind it becomes due principal; the remainder is its overpayment portion. A refund fully
                // funded by the overpayment moves no principal at all.
                final BigDecimal excessPrincipal = replayCreditBalanceRefund(balance, txn.getTransactionAmount());
                updatedAllocations.add(applyCreditBalanceRefundAllocation(txn, allocationsByTxnId.get(txn.getId()), excessPrincipal,
                        MathUtil.subtract(txn.getTransactionAmount(), excessPrincipal)));
                principalAdjustments.add(new PrincipalAdjustment(txn.getTransactionDate(), excessPrincipal));
                continue;
            }
            // Build the request from the live (running) balance/charges so the decision is made against the
            // remaining outstanding after the previously replayed transactions; the balance is then refreshed onto it.
            final WorkingCapitalLoanAllocationRequest request = allocationRequestFactory.build(loan, balance, charges,
                    txn.getTransactionDate(), txn.getTransactionAmount(), txn.getTypeOf());
            final WorkingCapitalLoanAllocationPlan plan = allocationProcessor.plan(request);
            updatedAllocations.add(allocationApplier.apply(txn, allocationsByTxnId.get(txn.getId()), plan, chargesById));
            balanceUpdater.apply(balance, plan);
            principalPayments.add(new PrincipalPayment(txn.getTransactionDate(), plan.principalPortion()));
        }

        allocationRepository.saveAll(updatedAllocations);
        chargeRepository.saveAll(charges);
        balanceRepository.saveAndFlush(balance);

        // The amortization schedule depends on the principal paid per day, which can shift when the principal portions
        // are re-allocated; rebuild it from the recomputed portions, then re-inject the over-refunded principal on the
        // date of the CBR that created it.
        amortizationScheduleWriteService.rebuildScheduleFromPrincipalPayments(loan, principalPayments, principalAdjustments);

        // On an accounting-enabled loan the re-allocation changed the principal/overpayment split of the surviving
        // transactions, so their booking-time journal entries are stale (an overpayment leg shrinks, a refund turns
        // into extra-lending principal). Restate each surviving transaction's entries from its recomputed allocation.
        if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            for (final WorkingCapitalLoanTransactionAllocation allocation : updatedAllocations) {
                accountingProcessor.restateJournalEntries(loan, allocation.getWcLoanTransaction(), allocation, false);
            }
        }
    }

    private boolean isCreditBalanceRefund(final LoanTransactionType type) {
        return type == LoanTransactionType.CREDIT_BALANCE_REFUND;
    }

    /**
     * Writes the split the replay derived for the refund. The returned allocation must be collected into the persisted
     * batch: mutating it in place is not enough for a row that already exists but is not otherwise part of the save
     * path.
     */
    private WorkingCapitalLoanTransactionAllocation applyCreditBalanceRefundAllocation(final WorkingCapitalLoanTransaction txn,
            final WorkingCapitalLoanTransactionAllocation existingAllocation, final BigDecimal principalPortion,
            final BigDecimal overpaymentPortion) {
        if (existingAllocation == null) {
            return WorkingCapitalLoanTransactionAllocation.forCreditBalanceRefund(txn, principalPortion, overpaymentPortion);
        }
        existingAllocation.setPrincipalPortion(principalPortion);
        existingAllocation.setFeeChargesPortion(BigDecimal.ZERO);
        existingAllocation.setPenaltyChargesPortion(BigDecimal.ZERO);
        existingAllocation.setOverpaymentPortion(overpaymentPortion);
        return existingAllocation;
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
