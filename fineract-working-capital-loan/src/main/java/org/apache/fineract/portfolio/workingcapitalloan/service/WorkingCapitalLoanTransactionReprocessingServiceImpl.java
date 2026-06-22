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
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
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
            .thenComparing(WorkingCapitalLoanTransaction::getId);

    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanPaymentAllocationProcessor allocationProcessor;
    private final WorkingCapitalLoanAllocationRequestFactory allocationRequestFactory;
    private final WorkingCapitalLoanAllocationApplier allocationApplier;
    private final WorkingCapitalLoanBalanceUpdater balanceUpdater;
    private final WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;

    @Override
    public void reprocessTransactions(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanTransaction> allTransactions = transactionRepository
                .findByWcLoan_IdOrderByTransactionDateAscIdAsc(loan.getId());
        reprocessTransactions(loan, allTransactions);
    }

    @Override
    public void reprocessTransactions(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanTransaction> allTransactions) {
        // Allocation order only matters when payments compete for charge buckets. Without charges, every
        // repayment-like transaction allocates to principal only — min(amount, outstanding) — which is
        // order-independent, so a changed chronological order cannot change any allocation.
        final List<WorkingCapitalLoanCharge> charges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());
        if (charges.isEmpty()) {
            log.debug("Skipping transaction reprocessing for WC loan {}: no active charges, allocations are order-independent",
                    loan.getId());
            return;
        }

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
        for (final WorkingCapitalLoanCharge charge : charges) {
            charge.setAmountPaid(BigDecimal.ZERO);
            charge.setPaid(false);
        }

        // Re-allocate every non-reversed repayment-like transaction in chronological order.
        final List<WorkingCapitalLoanTransaction> replayable = allTransactions.stream()
                .filter(txn -> !txn.isReversed() && isRepaymentLike(txn.getTypeOf())).sorted(TRANSACTION_ORDER).toList();

        // Pre-load the existing allocations in one query rather than per transaction. Looking them up via the
        // repository (instead of txn.getAllocation()) also avoids the lazy inverse side being stale for the
        // transaction that just triggered the reprocessing, which would otherwise create a second allocation row and
        // violate the one-allocation-per-transaction unique constraint.
        final Map<Long, WorkingCapitalLoanTransactionAllocation> allocationsByTxnId = allocationRepository
                .findByWcLoanTransactionIdIn(replayable.stream().map(WorkingCapitalLoanTransaction::getId).toList()).stream()
                .collect(Collectors.toMap(allocation -> allocation.getWcLoanTransaction().getId(), Function.identity()));

        final List<PrincipalPayment> principalPayments = new ArrayList<>();
        final List<WorkingCapitalLoanTransactionAllocation> updatedAllocations = new ArrayList<>();
        for (final WorkingCapitalLoanTransaction txn : replayable) {
            // Build the request from the live (running) balance/charges so the decision is made against the
            // remaining outstanding after the previously replayed transactions; the balance is then refreshed onto it.
            final WorkingCapitalLoanAllocationRequest request = allocationRequestFactory.build(loan, balance, charges,
                    txn.getTransactionDate(), txn.getTransactionAmount());
            final WorkingCapitalLoanAllocationPlan plan = allocationProcessor.plan(request);
            updatedAllocations.add(allocationApplier.apply(txn, allocationsByTxnId.get(txn.getId()), plan, charges));
            balanceUpdater.apply(balance, plan);
            principalPayments.add(new PrincipalPayment(txn.getTransactionDate(), plan.principalPortion()));
        }

        allocationRepository.saveAll(updatedAllocations);
        chargeRepository.saveAll(charges);
        balanceRepository.saveAndFlush(balance);

        // The amortization schedule depends only on the principal paid per day, which can shift when the principal
        // portions are re-allocated; rebuild it from the recomputed portions.
        amortizationScheduleWriteService.rebuildScheduleFromPrincipalPayments(loan, principalPayments);
    }

    private boolean isRepaymentLike(final LoanTransactionType type) {
        return type == LoanTransactionType.REPAYMENT || type == LoanTransactionType.GOODWILL_CREDIT;
    }
}
