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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan.ChargeAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.springframework.stereotype.Component;

/**
 * Materializes a {@link WorkingCapitalLoanAllocationPlan} onto JPA entities: builds (or refreshes) the transaction
 * allocation and settles the decided charges through {@link WorkingCapitalLoanChargePaymentHandler}.
 *
 * <p>
 * All charge mutations <strong>accumulate</strong> (never overwrite), so the forward flow and the reprocessing replay -
 * which resets the buckets and replays in order - both build up the correct running state. The applier does not
 * persist, does not refresh the balance buckets ({@link WorkingCapitalLoanBalanceUpdater} does, driven by the
 * orchestrator), and does not touch the amortization schedule; those stay with the orchestrator.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanAllocationApplier {

    private final WorkingCapitalLoanChargePaymentHandler chargePaymentHandler;

    public WorkingCapitalLoanTransactionAllocation apply(final WorkingCapitalLoanTransaction transaction,
            final WorkingCapitalLoanTransactionAllocation existingAllocation, final WorkingCapitalLoanAllocationPlan plan,
            final List<WorkingCapitalLoanCharge> charges) {
        final Map<Long, WorkingCapitalLoanCharge> chargesById = charges.stream()
                .collect(Collectors.toMap(WorkingCapitalLoanCharge::getId, Function.identity()));
        return apply(transaction, existingAllocation, plan, chargesById);
    }

    public WorkingCapitalLoanTransactionAllocation apply(final WorkingCapitalLoanTransaction transaction,
            final WorkingCapitalLoanTransactionAllocation existingAllocation, final WorkingCapitalLoanAllocationPlan plan,
            final Map<Long, WorkingCapitalLoanCharge> chargesById) {
        for (final ChargeAllocation chargeAllocation : plan.chargeAllocations()) {
            final WorkingCapitalLoanCharge charge = chargesById.get(chargeAllocation.chargeId());
            if (charge != null) {
                chargePaymentHandler.applyChargePayment(charge, chargeAllocation.amount());
            } else {
                log.warn("WC loan allocation plan references chargeId {} not found in provided charges; skipping",
                        chargeAllocation.chargeId());
            }
        }

        return applyAllocation(transaction, existingAllocation, plan);
    }

    private WorkingCapitalLoanTransactionAllocation applyAllocation(final WorkingCapitalLoanTransaction transaction,
            final WorkingCapitalLoanTransactionAllocation existing, final WorkingCapitalLoanAllocationPlan plan) {
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
