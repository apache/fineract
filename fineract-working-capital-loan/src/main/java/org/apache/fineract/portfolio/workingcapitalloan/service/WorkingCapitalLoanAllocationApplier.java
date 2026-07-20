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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan.ChargeAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanChargeAllocationMismatchException;
import org.springframework.stereotype.Component;

/**
 * Materializes a {@link WorkingCapitalLoanAllocationPlan} onto JPA entities: builds (or refreshes) the transaction
 * allocation and settles the decided charges through {@link WorkingCapitalLoanChargePaymentHandler}.
 *
 * <p>
 * All charge mutations <strong>accumulate</strong> (never overwrite), so the forward flow and the reprocessing replay -
 * which resets the buckets and replays in order - both build up the correct running state. Settling each charge also
 * produces a {@link WorkingCapitalLoanChargePaidBy} row (returned in the {@link Result} for the orchestrator to
 * persist) recording which transaction paid which charge. The applier does not persist, does not refresh the balance
 * buckets ({@link WorkingCapitalLoanBalanceUpdater} does, driven by the orchestrator), and does not touch the
 * amortization schedule; those stay with the orchestrator.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanAllocationApplier {

    private final WorkingCapitalLoanChargePaymentHandler chargePaymentHandler;

    /**
     * The materialized outcome of applying a plan: the transaction allocation and the per-charge paid-by rows produced
     * while settling the plan's charge allocations. Both are for the orchestrator to persist.
     */
    public record Result(WorkingCapitalLoanTransactionAllocation allocation, List<WorkingCapitalLoanChargePaidBy> chargesPaidBy) {
    }

    public Result apply(final WorkingCapitalLoanTransaction transaction, final WorkingCapitalLoanAllocationPlan plan,
            final List<WorkingCapitalLoanCharge> charges) {
        final Map<Long, WorkingCapitalLoanCharge> chargesById = charges.stream()
                .collect(Collectors.toMap(WorkingCapitalLoanCharge::getId, Function.identity()));
        return apply(transaction, plan, chargesById);
    }

    public Result apply(final WorkingCapitalLoanTransaction transaction, final WorkingCapitalLoanAllocationPlan plan,
            final Map<Long, WorkingCapitalLoanCharge> chargesById) {

        final List<WorkingCapitalLoanChargePaidBy> chargesPaidBy = applyChargeAllocations(transaction, plan, chargesById);
        return new Result(applyAllocation(transaction, plan), chargesPaidBy);
    }

    private List<WorkingCapitalLoanChargePaidBy> applyChargeAllocations(WorkingCapitalLoanTransaction transaction,
            WorkingCapitalLoanAllocationPlan plan, Map<Long, WorkingCapitalLoanCharge> chargesById) {
        final List<WorkingCapitalLoanChargePaidBy> chargesPaidBy = new ArrayList<>();
        for (final ChargeAllocation chargeAllocation : plan.chargeAllocations()) {
            final WorkingCapitalLoanCharge charge = chargesById.get(chargeAllocation.chargeId());
            if (charge == null) {
                throw new WorkingCapitalLoanChargeAllocationMismatchException(chargeAllocation.chargeId());
            }
            final WorkingCapitalLoanChargePaidBy paidBy = chargePaymentHandler.applyChargePayment(transaction, charge,
                    chargeAllocation.amount());
            if (paidBy != null) {
                chargesPaidBy.add(paidBy);
            }
        }
        return chargesPaidBy;
    }

    private WorkingCapitalLoanTransactionAllocation applyAllocation(final WorkingCapitalLoanTransaction transaction,
            final WorkingCapitalLoanAllocationPlan plan) {
        final WorkingCapitalLoanTransactionAllocation existing = transaction.getAllocation();
        if (existing == null) {
            return WorkingCapitalLoanTransactionAllocation.forPortions(transaction, plan.principalPortion(), plan.feeChargesPortion(),
                    plan.penaltyChargesPortion(), plan.overpaymentPortion());
        }
        existing.setPrincipalPortion(plan.principalPortion());
        existing.setFeeChargesPortion(plan.feeChargesPortion());
        existing.setPenaltyChargesPortion(plan.penaltyChargesPortion());
        existing.setOverpaymentPortion(plan.overpaymentPortion());
        return existing;
    }
}
