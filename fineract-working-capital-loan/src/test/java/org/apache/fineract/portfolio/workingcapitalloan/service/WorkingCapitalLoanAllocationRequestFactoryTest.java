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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPaymentAllocationRule;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;
import org.junit.jupiter.api.Test;

class WorkingCapitalLoanAllocationRequestFactoryTest {

    private static final LocalDate TXN_DATE = LocalDate.of(2026, 6, 19);

    private final WorkingCapitalLoanAllocationRequestFactory factory = new WorkingCapitalLoanAllocationRequestFactory();

    @Test
    void resolvesTheRuleConfiguredForTheGivenTransactionTypeRatherThanAlwaysUsingRepayment() {
        final WorkingCapitalLoanPaymentAllocationRule repaymentRule = rule(PaymentAllocationTransactionType.REPAYMENT,
                WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL);
        final WorkingCapitalLoanPaymentAllocationRule goodwillCreditRule = rule(PaymentAllocationTransactionType.GOODWILL_CREDIT,
                WorkingCapitalPaymentAllocationType.DUE_PENALTY, WorkingCapitalPaymentAllocationType.DUE_FEE);
        final WorkingCapitalLoan loan = loanWithRules(repaymentRule, goodwillCreditRule);

        final WorkingCapitalLoanAllocationRequest request = factory.build(loan, balance("100"), List.of(), TXN_DATE, new BigDecimal("50"),
                LoanTransactionType.GOODWILL_CREDIT);

        assertEquals(List.of(WorkingCapitalPaymentAllocationType.DUE_PENALTY, WorkingCapitalPaymentAllocationType.DUE_FEE),
                request.allocationOrder());
    }

    @Test
    void fallsBackToDefaultRuleWhenNoTransactionSpecificRuleIsConfigured() {
        final WorkingCapitalLoanPaymentAllocationRule repaymentRule = rule(PaymentAllocationTransactionType.REPAYMENT,
                WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL);
        final WorkingCapitalLoanPaymentAllocationRule defaultRule = rule(PaymentAllocationTransactionType.DEFAULT,
                WorkingCapitalPaymentAllocationType.DUE_FEE, WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL);
        final WorkingCapitalLoan loan = loanWithRules(repaymentRule, defaultRule);

        final WorkingCapitalLoanAllocationRequest request = factory.build(loan, balance("100"), List.of(), TXN_DATE, new BigDecimal("50"),
                LoanTransactionType.GOODWILL_CREDIT);

        // Must fall back to DEFAULT, not silently reuse the REPAYMENT rule.
        assertEquals(List.of(WorkingCapitalPaymentAllocationType.DUE_FEE, WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL),
                request.allocationOrder());
    }

    @Test
    void repaymentStillResolvesItsOwnRule() {
        final WorkingCapitalLoanPaymentAllocationRule repaymentRule = rule(PaymentAllocationTransactionType.REPAYMENT,
                WorkingCapitalPaymentAllocationType.DUE_PENALTY, WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL);
        final WorkingCapitalLoan loan = loanWithRules(repaymentRule);

        final WorkingCapitalLoanAllocationRequest request = factory.build(loan, balance("100"), List.of(), TXN_DATE, new BigDecimal("50"),
                LoanTransactionType.REPAYMENT);

        assertEquals(List.of(WorkingCapitalPaymentAllocationType.DUE_PENALTY, WorkingCapitalPaymentAllocationType.DUE_PRINCIPAL),
                request.allocationOrder());
    }

    private WorkingCapitalLoanPaymentAllocationRule rule(final PaymentAllocationTransactionType transactionType,
            final WorkingCapitalPaymentAllocationType... allocationTypes) {
        return new WorkingCapitalLoanPaymentAllocationRule(null, transactionType, List.of(allocationTypes));
    }

    private WorkingCapitalLoan loanWithRules(final WorkingCapitalLoanPaymentAllocationRule... rules) {
        final WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        when(loan.getPaymentAllocationRules()).thenReturn(List.of(rules));
        return loan;
    }

    private WorkingCapitalLoanBalance balance(final String principalOutstanding) {
        final WorkingCapitalLoanBalance balance = mock(WorkingCapitalLoanBalance.class);
        when(balance.getPrincipalOutstanding()).thenReturn(new BigDecimal(principalOutstanding));
        return balance;
    }
}
