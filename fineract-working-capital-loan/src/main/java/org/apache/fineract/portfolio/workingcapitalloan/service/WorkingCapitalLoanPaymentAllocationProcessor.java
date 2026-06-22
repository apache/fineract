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
import java.util.List;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.loanproduct.domain.DueType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan.ChargeAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest.ChargeBalance;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;
import org.springframework.stereotype.Component;

/**
 * Decides how a repayment-like amount splits across penalty charges, fee charges and principal, following the payment
 * allocation order configured on the loan. The same decision is used by the forward repayment flow and by
 * {@link WorkingCapitalLoanTransactionReprocessingService} when transactions are replayed in a changed chronological
 * order.
 *
 * <p>
 * This is a pure function: a {@link WorkingCapitalLoanAllocationRequest} goes in and a
 * {@link WorkingCapitalLoanAllocationPlan} comes out. It does not touch JPA entities and has no side effects -
 * materializing the plan (charge balances, amortization schedule, balance buckets) is the caller's responsibility.
 */
@Component
public class WorkingCapitalLoanPaymentAllocationProcessor {

    public WorkingCapitalLoanAllocationPlan plan(final WorkingCapitalLoanAllocationRequest request) {
        BigDecimal remaining = MathUtil.nullToZero(request.amount());
        BigDecimal runningPrincipalOutstanding = MathUtil.nullToZero(request.principalOutstanding());
        final BigDecimal[] runningChargeOutstanding = request.charges().stream().map(charge -> MathUtil.nullToZero(charge.outstanding()))
                .toArray(BigDecimal[]::new);

        BigDecimal principalPortion = BigDecimal.ZERO;
        BigDecimal feePortion = BigDecimal.ZERO;
        BigDecimal penaltyPortion = BigDecimal.ZERO;
        final List<ChargeAllocation> chargeAllocations = new ArrayList<>();

        for (final WorkingCapitalPaymentAllocationType allocationType : request.allocationOrder()) {
            if (!MathUtil.isGreaterThanZero(remaining)) {
                break;
            }
            final DueType dueType = allocationType.getDueType();
            switch (allocationType.getAllocationType()) {
                case PRINCIPAL -> {
                    final BigDecimal applied = remaining.min(runningPrincipalOutstanding).max(BigDecimal.ZERO);
                    principalPortion = principalPortion.add(applied);
                    runningPrincipalOutstanding = runningPrincipalOutstanding.subtract(applied);
                    remaining = remaining.subtract(applied);
                }
                case FEE -> {
                    final BigDecimal applied = allocateToCharges(request.charges(), runningChargeOutstanding, false, dueType,
                            request.transactionDate(), remaining, chargeAllocations);
                    feePortion = feePortion.add(applied);
                    remaining = remaining.subtract(applied);
                }
                case PENALTY -> {
                    final BigDecimal applied = allocateToCharges(request.charges(), runningChargeOutstanding, true, dueType,
                            request.transactionDate(), remaining, chargeAllocations);
                    penaltyPortion = penaltyPortion.add(applied);
                    remaining = remaining.subtract(applied);
                }
                default -> {
                }
            }
        }

        final BigDecimal overpayment = remaining.max(BigDecimal.ZERO);
        return new WorkingCapitalLoanAllocationPlan(principalPortion, feePortion, penaltyPortion, overpayment, chargeAllocations);
    }

    private BigDecimal allocateToCharges(final List<ChargeBalance> charges, final BigDecimal[] runningChargeOutstanding,
            final boolean penalty, final DueType dueType, final LocalDate transactionDate, final BigDecimal remaining,
            final List<ChargeAllocation> chargeAllocations) {
        BigDecimal available = remaining;
        BigDecimal totalApplied = BigDecimal.ZERO;
        for (int i = 0; i < charges.size(); i++) {
            if (!MathUtil.isGreaterThanZero(available)) {
                break;
            }
            final ChargeBalance charge = charges.get(i);
            if (charge.penalty() != penalty || !matchesDueType(charge, dueType, transactionDate)) {
                continue;
            }
            final BigDecimal outstanding = runningChargeOutstanding[i];
            if (!MathUtil.isGreaterThanZero(outstanding)) {
                continue;
            }
            final BigDecimal applied = available.min(outstanding);
            runningChargeOutstanding[i] = outstanding.subtract(applied);
            chargeAllocations.add(new ChargeAllocation(charge.chargeId(), penalty, applied));
            available = available.subtract(applied);
            totalApplied = totalApplied.add(applied);
        }
        return totalApplied;
    }

    private boolean matchesDueType(final ChargeBalance charge, final DueType dueType, final LocalDate transactionDate) {
        final LocalDate dueDate = charge.dueDate();
        // A charge with no due date is treated as already due; otherwise "due" means on/before the transaction date.
        final boolean isDue = dueDate == null || !dueDate.isAfter(transactionDate);
        return dueType == DueType.DUE ? isDue : !isDue;
    }
}
