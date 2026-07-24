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
import java.util.List;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest.ChargeBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPaymentAllocationRule;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Builds the immutable {@link WorkingCapitalLoanAllocationRequest} from the live loan / balance / charge entities. Owns
 * the JPA access for the allocation decision: maps charges to {@link ChargeBalance} and resolves the configured payment
 * allocation order for the transaction type (specific rule, falling back to DEFAULT, then principal-only). The
 * processor itself stays free of entities.
 */
@Component
public class WorkingCapitalLoanAllocationRequestFactory {

    @NonNull
    public WorkingCapitalLoanAllocationRequest build(@NonNull final WorkingCapitalLoan loan,
            @NonNull final WorkingCapitalLoanBalance balance, @NonNull final List<WorkingCapitalLoanCharge> charges,
            @NonNull final LocalDate transactionDate, @NonNull final BigDecimal amount,
            @NonNull final LoanTransactionType transactionType) {
        final List<ChargeBalance> chargeBalances = charges.stream().map(
                charge -> new ChargeBalance(charge.getId(), charge.getAmountOutstanding(), charge.getDueDate(), charge.isPenaltyCharge()))
                .toList();
        return new WorkingCapitalLoanAllocationRequest(transactionDate, amount,
                getAllocationRule(loan, transactionType).getAllocationTypes(), balance.getPrincipalOutstanding(), chargeBalances);
    }

    /**
     * Builds the allocation request for a charge adjustment. When the product configures its own CHARGE_ADJUSTMENT
     * allocation order, that order is honored and the adjustment spreads across charges/principal like a repayment.
     * Otherwise an adjustment settles only its own charge: the request is scoped to that single charge with no
     * principal outstanding, which keeps the amount on the charge's fee/penalty bucket and prevents the fallback
     * (default) order - which ranks DUE_PRINCIPAL ahead of the IN_ADVANCE buckets - from diverting a not-yet-due charge
     * onto principal.
     */
    public WorkingCapitalLoanAllocationRequest buildForChargeAdjustment(@NonNull final WorkingCapitalLoan loan,
            @NonNull final WorkingCapitalLoanBalance balance, @NonNull final List<WorkingCapitalLoanCharge> charges,
            @NonNull final WorkingCapitalLoanCharge adjustedCharge, @NonNull final LocalDate transactionDate,
            @NonNull final BigDecimal amount) {
        if (hasConfiguredAllocationRule(loan, LoanTransactionType.CHARGE_ADJUSTMENT)) {
            return build(loan, balance, charges, transactionDate, amount, LoanTransactionType.CHARGE_ADJUSTMENT);
        }
        final ChargeBalance chargeBalance = new ChargeBalance(adjustedCharge.getId(), adjustedCharge.getAmountOutstanding(),
                adjustedCharge.getDueDate(), adjustedCharge.isPenaltyCharge());
        return new WorkingCapitalLoanAllocationRequest(transactionDate, amount, getDefaultAllocationRule(loan).getAllocationTypes(),
                BigDecimal.ZERO, List.of(chargeBalance));
    }

    private boolean hasConfiguredAllocationRule(@NonNull final WorkingCapitalLoan loan,
            @NonNull final LoanTransactionType transactionType) {
        return loan.getPaymentAllocationRules().stream()
                .anyMatch(rule -> transactionType.equals(rule.getTransactionType().getLoanTransactionType()));
    }

    @NonNull
    private WorkingCapitalLoanPaymentAllocationRule getAllocationRule(@NonNull final WorkingCapitalLoan loan,
            @NonNull final LoanTransactionType transactionType) {
        return loan.getPaymentAllocationRules().stream()
                .filter(rule -> transactionType.equals(rule.getTransactionType().getLoanTransactionType())).findFirst()
                .orElseGet(() -> getDefaultAllocationRule(loan));
    }

    @NonNull
    private WorkingCapitalLoanPaymentAllocationRule getDefaultAllocationRule(@NonNull final WorkingCapitalLoan loan) {
        return loan.getPaymentAllocationRules().stream().filter(rule -> rule.getTransactionType().isDefault()).findFirst().get();
    }
}
