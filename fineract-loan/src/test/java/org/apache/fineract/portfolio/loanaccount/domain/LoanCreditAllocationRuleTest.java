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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.junit.jupiter.api.Test;

class LoanCreditAllocationRuleTest {

    @Test
    void resolvesPrincipalOnlyWhenNoRulesAreConfigured() {
        assertEquals(CreditAllocationStrategy.PRINCIPAL_ONLY,
                LoanCreditAllocationRule.resolveAllocationStrategy(null, LoanTransactionType.CHARGEBACK));
        assertEquals(CreditAllocationStrategy.PRINCIPAL_ONLY,
                LoanCreditAllocationRule.resolveAllocationStrategy(List.of(), LoanTransactionType.CHARGEBACK));
    }

    @Test
    void resolvesTheConfiguredStrategyOfTheMatchingRule() {
        assertEquals(CreditAllocationStrategy.RULE_BASED, LoanCreditAllocationRule
                .resolveAllocationStrategy(List.of(rule(CreditAllocationStrategy.RULE_BASED)), LoanTransactionType.CHARGEBACK));
        assertEquals(CreditAllocationStrategy.PRINCIPAL_ONLY, LoanCreditAllocationRule
                .resolveAllocationStrategy(List.of(rule(CreditAllocationStrategy.PRINCIPAL_ONLY)), LoanTransactionType.CHARGEBACK));
    }

    @Test
    void resolvesPrincipalOnlyForTransactionTypesWithoutRule() {
        assertEquals(CreditAllocationStrategy.PRINCIPAL_ONLY, LoanCreditAllocationRule
                .resolveAllocationStrategy(List.of(rule(CreditAllocationStrategy.RULE_BASED)), LoanTransactionType.CREDIT_BALANCE_REFUND));
    }

    @Test
    void failsWhenTheMatchingRuleHasNoStrategy() {
        assertThrows(IllegalStateException.class,
                () -> LoanCreditAllocationRule.resolveAllocationStrategy(List.of(rule(null)), LoanTransactionType.CHARGEBACK));
    }

    private static LoanCreditAllocationRule rule(CreditAllocationStrategy strategy) {
        return new LoanCreditAllocationRule(null, CreditAllocationTransactionType.CHARGEBACK,
                List.of(AllocationType.PENALTY, AllocationType.FEE, AllocationType.INTEREST, AllocationType.PRINCIPAL), strategy);
    }
}
