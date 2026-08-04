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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.springframework.stereotype.Component;

/**
 * Allocates one repayment-like transaction against the running balance. This is the single compute core shared by the
 * forward (live) flow and the reprocessing replay: it builds the allocation request, plans the split, materializes it
 * onto the transaction's allocation and charge paid-by rows, and folds it into the balance buckets.
 *
 * <p>
 * It is a pure in-memory mutation over the already-loaded aggregate (transaction, balance, charge entities): it does
 * <strong>not</strong> persist, does not touch the amortization schedule, and does not post journal entries. Flush
 * timing and the schedule/journal strategy differ between the live path (single transaction) and the replay (batch, or
 * per-transaction undo/redo), so those stay with the caller.
 */
@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanTransactionAllocator {

    private final WorkingCapitalLoanAllocationRequestFactory allocationRequestFactory;
    private final WorkingCapitalLoanPaymentAllocationProcessor allocationProcessor;
    private final WorkingCapitalLoanAllocationApplier allocationApplier;
    private final WorkingCapitalLoanBalanceUpdater balanceUpdater;

    /**
     * The outcome of allocating one transaction: the (created or updated) allocation and the charge paid-by rows for
     * the caller to persist, the plan that produced them, and the change bookkeeping the replay needs - whether the
     * allocation differs from what was stored, whether a stored allocation existed at all (brand-new vs re-allocated),
     * and the principal portion that was on the stored allocation <em>before</em> it was mutated in place (for the
     * schedule undo of the old value).
     */
    public record Result(WorkingCapitalLoanTransactionAllocation allocation, List<WorkingCapitalLoanChargePaidBy> chargesPaidBy,
            WorkingCapitalLoanAllocationPlan plan, boolean allocationChanged, boolean hadStoredAllocation,
            BigDecimal previousPrincipalPortion) {
    }

    public Result allocate(final WorkingCapitalLoan loan, final WorkingCapitalLoanBalance balance,
            final List<WorkingCapitalLoanCharge> charges, final WorkingCapitalLoanTransaction txn) {
        final Map<Long, WorkingCapitalLoanCharge> chargesById = charges.stream()
                .collect(Collectors.toMap(WorkingCapitalLoanCharge::getId, Function.identity()));
        return allocate(loan, balance, charges, chargesById, txn);
    }

    public Result allocate(final WorkingCapitalLoan loan, final WorkingCapitalLoanBalance balance,
            final List<WorkingCapitalLoanCharge> charges, final Map<Long, WorkingCapitalLoanCharge> chargesById,
            final WorkingCapitalLoanTransaction txn) {
        // Every transaction type - charge adjustments included - is allocated through the loan's configured payment
        // allocation order for its type (falling back to DEFAULT). An adjustment is therefore not privileged towards
        // the charge it adjusts: it settles charges in the configured order, then principal, then overpayment.
        final WorkingCapitalLoanAllocationRequest request = allocationRequestFactory.build(loan, balance, charges, txn.getTransactionDate(),
                txn.getTransactionAmount(), txn.getTypeOf());
        final WorkingCapitalLoanAllocationPlan plan = allocationProcessor.plan(request);

        // Capture the currently-stored allocation state BEFORE the applier mutates the row in place: reading the
        // principal portion afterwards would return the just-written value and desync the caller's schedule undo.
        final WorkingCapitalLoanTransactionAllocation stored = txn.getAllocation();
        final boolean hadStoredAllocation = stored != null;
        final BigDecimal previousPrincipalPortion = hadStoredAllocation ? MathUtil.nullToZero(stored.getPrincipalPortion())
                : BigDecimal.ZERO;
        final boolean allocationChanged = !hadStoredAllocation || !allocationMatchesPlan(stored, plan);

        final WorkingCapitalLoanAllocationApplier.Result applied = allocationApplier.apply(txn, plan, chargesById);
        balanceUpdater.apply(balance, plan);

        return new Result(applied.allocation(), applied.chargesPaidBy(), plan, allocationChanged, hadStoredAllocation,
                previousPrincipalPortion);
    }

    private boolean allocationMatchesPlan(final WorkingCapitalLoanTransactionAllocation allocation,
            final WorkingCapitalLoanAllocationPlan plan) {
        return portionsEqual(allocation.getPrincipalPortion(), plan.principalPortion())
                && portionsEqual(allocation.getFeeChargesPortion(), plan.feeChargesPortion())
                && portionsEqual(allocation.getPenaltyChargesPortion(), plan.penaltyChargesPortion())
                && portionsEqual(allocation.getOverpaymentPortion(), plan.overpaymentPortion());
    }

    private boolean portionsEqual(final BigDecimal a, final BigDecimal b) {
        return MathUtil.nullToZero(a).compareTo(MathUtil.nullToZero(b)) == 0;
    }
}
