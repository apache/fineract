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
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
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
            .thenComparing(WorkingCapitalLoanTransaction::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanPaymentAllocationProcessor allocationProcessor;
    private final WorkingCapitalLoanAllocationRequestFactory allocationRequestFactory;
    private final WorkingCapitalLoanAllocationApplier allocationApplier;
    private final WorkingCapitalLoanBalanceUpdater balanceUpdater;
    private final WorkingCapitalLoanChargePaymentHandler chargePaymentHandler;
    private final WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;

    @Override
    public void reprocessTransactions(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanTransaction> allTransactions = transactionRepository
                .findByWcLoan_IdOrderByTransactionDateAscIdAsc(loan.getId());
        reprocessTransactions(loan, allTransactions);
    }

    @Override
    public void reprocessTransactions(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanTransaction> allTransactions) {
        reprocessTransactions(loan, allTransactions, false);
    }

    @Override
    public void reprocessTransactionsForChargeFreeUndo(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanTransaction> allTransactions = transactionRepository
                .findByWcLoan_IdOrderByTransactionDateAscIdAsc(loan.getId());
        reprocessTransactions(loan, allTransactions, true);
    }

    private void reprocessTransactions(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanTransaction> allTransactions,
            final boolean forceEvenWithoutCharges) {
        final List<WorkingCapitalLoanCharge> charges = chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());

        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loan.getId()).orElse(null);
        if (balance == null) {
            log.debug("Skipping transaction reprocessing for WC loan {}: no balance to recompute", loan.getId());
            return;
        }

        // Charge-free allocations are order-independent only while the loan is not overpaid: once outstanding hits zero
        // the chronological split differs from booking-time order, so an overpaid (or undo-on-overpaid) loan must
        // reprocess.
        if (charges.isEmpty() && !forceEvenWithoutCharges && !isOverpaidByReplayableTransactions(allTransactions, balance)) {
            log.debug(
                    "Skipping transaction reprocessing for WC loan {}: no active charges and not overpaid, allocations are order-independent",
                    loan.getId());
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

        // Charge adjustments settle a charge outside the repayment allocation, so re-apply them after the reset above;
        // otherwise the replayed repayments would re-settle the already-adjusted portion.
        final Map<Long, WorkingCapitalLoanCharge> chargesById = charges.stream()
                .collect(Collectors.toMap(WorkingCapitalLoanCharge::getId, Function.identity()));
        reapplyChargeAdjustments(allTransactions, chargesById, balance);

        // Re-allocate every non-reversed repayment-like transaction in chronological order.
        final List<WorkingCapitalLoanTransaction> replayable = allTransactions.stream()
                .filter(txn -> !txn.isReversed() && txn.isRepaymentLike()).sorted(TRANSACTION_ORDER).toList();

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
        final List<WorkingCapitalLoanTransactionAllocation> updatedAllocations = new ArrayList<>();
        for (final WorkingCapitalLoanTransaction txn : replayable) {
            // Build the request from the live (running) balance/charges so the decision is made against the
            // remaining outstanding after the previously replayed transactions; the balance is then refreshed onto it.
            final WorkingCapitalLoanAllocationRequest request = allocationRequestFactory.build(loan, balance, charges,
                    txn.getTransactionDate(), txn.getTransactionAmount());
            final WorkingCapitalLoanAllocationPlan plan = allocationProcessor.plan(request);
            updatedAllocations.add(allocationApplier.apply(txn, allocationsByTxnId.get(txn.getId()), plan, chargesById));
            balanceUpdater.apply(balance, plan);
            principalPayments.add(new PrincipalPayment(txn.getTransactionDate(), plan.principalPortion()));
        }

        // The recomputed overpayment ignores refunds already paid out, so subtract non-reversed CBRs to avoid
        // resurrecting a refunded overpayment. Floored at zero; the over-refund case is out of scope here.
        final BigDecimal creditBalanceRefundTotal = allTransactions.stream()
                .filter(txn -> !txn.isReversed() && txn.getTypeOf() == LoanTransactionType.CREDIT_BALANCE_REFUND)
                .map(WorkingCapitalLoanTransaction::getTransactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (creditBalanceRefundTotal.signum() > 0) {
            balance.setOverpaymentAmount(balance.getOverpaymentAmount().subtract(creditBalanceRefundTotal).max(BigDecimal.ZERO));
        }

        allocationRepository.saveAll(updatedAllocations);
        chargeRepository.saveAll(charges);
        balanceRepository.saveAndFlush(balance);

        // The amortization schedule depends only on the principal paid per day, which can shift when the principal
        // portions are re-allocated; rebuild it from the recomputed portions.
        amortizationScheduleWriteService.rebuildScheduleFromPrincipalPayments(loan, principalPayments);
    }

    private boolean isOverpaidByReplayableTransactions(final List<WorkingCapitalLoanTransaction> allTransactions,
            final WorkingCapitalLoanBalance balance) {
        final BigDecimal replayableTotal = allTransactions.stream().filter(txn -> !txn.isReversed() && txn.isRepaymentLike())
                .map(WorkingCapitalLoanTransaction::getTransactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return replayableTotal.compareTo(balance.getPrincipal()) > 0;
    }

    private void reapplyChargeAdjustments(final List<WorkingCapitalLoanTransaction> allTransactions,
            final Map<Long, WorkingCapitalLoanCharge> chargesById, final WorkingCapitalLoanBalance balance) {
        for (final WorkingCapitalLoanTransaction txn : allTransactions) {
            if (txn.isReversed() || txn.getTypeOf() != LoanTransactionType.CHARGE_ADJUSTMENT) {
                continue;
            }
            txn.getLoanTransactionRelations().stream()
                    .filter(relation -> relation.getRelationType() == LoanTransactionRelationTypeEnum.CHARGE_ADJUSTMENT
                            && relation.getToCharge() != null)
                    .findFirst().map(relation -> chargesById.get(relation.getToCharge().getId())).ifPresent(charge -> {
                        final BigDecimal amount = txn.getTransactionAmount();
                        chargePaymentHandler.applyChargePayment(charge, amount);
                        balanceUpdater.applyChargePayment(balance, charge, amount);
                    });
        }
    }
}
