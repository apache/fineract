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
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;
import org.springframework.stereotype.Component;

/**
 * Builds the immutable {@link WorkingCapitalLoanAllocationRequest} from the live loan / balance / charge entities. Owns
 * the JPA access for the allocation decision: maps charges to {@link ChargeBalance} and resolves the configured payment
 * allocation order for the transaction type (specific rule, falling back to DEFAULT, then principal-only). The
 * processor itself stays free of entities.
 */
@Component
public class WorkingCapitalLoanAllocationRequestFactory {

    public WorkingCapitalLoanAllocationRequest build(final WorkingCapitalLoan loan, final WorkingCapitalLoanBalance balance,
            final List<WorkingCapitalLoanCharge> charges, final LocalDate transactionDate, final BigDecimal amount,
            final LoanTransactionType transactionType) {
        final List<ChargeBalance> chargeBalances = charges.stream().map(
                charge -> new ChargeBalance(charge.getId(), charge.getAmountOutstanding(), charge.getDueDate(), charge.isPenaltyCharge()))
                .toList();
        return new WorkingCapitalLoanAllocationRequest(transactionDate, amount, resolveAllocationOrder(loan, transactionType),
                balance.getPrincipalOutstanding(), chargeBalances);
    }

    private List<WorkingCapitalPaymentAllocationType> resolveAllocationOrder(final WorkingCapitalLoan loan,
            final LoanTransactionType transactionType) {
        final WorkingCapitalLoanPaymentAllocationRule rule = transactionType == null ? getDefaultAllocationRule(loan)
                : getAllocationRule(loan, transactionType);
        if (rule != null && rule.getAllocationTypes() != null && !rule.getAllocationTypes().isEmpty()) {
            return rule.getAllocationTypes();
        }
        // No configured order: keep the legacy principal-only behaviour.
        return List.of(WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL);
    }

    private WorkingCapitalLoanPaymentAllocationRule getAllocationRule(final WorkingCapitalLoan loan,
            final LoanTransactionType transactionType) {
        final List<WorkingCapitalLoanPaymentAllocationRule> rules = loan.getPaymentAllocationRules();
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        return rules.stream().filter(rule -> transactionType.equals(rule.getTransactionType().getLoanTransactionType())).findFirst()
                .orElseGet(() -> getDefaultAllocationRule(loan));
    }

    private WorkingCapitalLoanPaymentAllocationRule getDefaultAllocationRule(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanPaymentAllocationRule> rules = loan.getPaymentAllocationRules();
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        return rules.stream().filter(rule -> rule.getTransactionType().isDefault()).findFirst().orElse(null);
    }
}
