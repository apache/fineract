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
package org.apache.fineract.portfolio.loanproduct.service;

import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.FEE;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.INTEREST;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PENALTY;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PRINCIPAL;

import java.util.List;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductCreditAllocationRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanProductCreditAllocationRuleMergerTest {

    private LoanProductCreditAllocationRuleMerger underTest = new LoanProductCreditAllocationRuleMerger();

    @Test
    public void testMergerOneNewAdded() {
        LoanProduct loanProduct = new LoanProduct();

        LoanProductCreditAllocationRule rule1 = createRule(CreditAllocationTransactionType.CHARGEBACK,
                List.of(PENALTY, FEE, INTEREST, PRINCIPAL));

        boolean result = underTest.updateCreditAllocationRules(loanProduct, List.of(rule1));
        Assertions.assertTrue(result);
        Assertions.assertEquals(1, loanProduct.getCreditAllocationRules().size());
        Assertions.assertEquals(rule1, loanProduct.getCreditAllocationRules().get(0));
    }

    @Test
    public void testMergerExistingUpdated() {
        LoanProduct loanProduct = new LoanProduct();

        LoanProductCreditAllocationRule rule1 = createRule(CreditAllocationTransactionType.CHARGEBACK,
                List.of(PENALTY, FEE, INTEREST, PRINCIPAL));
        LoanProductCreditAllocationRule rule2 = createRule(CreditAllocationTransactionType.CHARGEBACK,
                List.of(FEE, INTEREST, PRINCIPAL, PENALTY));

        loanProduct.getCreditAllocationRules().add(rule1);

        boolean result = underTest.updateCreditAllocationRules(loanProduct, List.of(rule2));
        Assertions.assertTrue(result);
        Assertions.assertEquals(1, loanProduct.getCreditAllocationRules().size());
        Assertions.assertEquals(rule2.getTransactionType(), loanProduct.getCreditAllocationRules().get(0).getTransactionType());
        Assertions.assertEquals(rule2.getAllocationTypes(), loanProduct.getCreditAllocationRules().get(0).getAllocationTypes());
    }

    @Test
    public void testMergerNothingIsChanged() {
        LoanProduct loanProduct = new LoanProduct();

        LoanProductCreditAllocationRule rule1 = createRule(CreditAllocationTransactionType.CHARGEBACK,
                List.of(PENALTY, FEE, INTEREST, PRINCIPAL));
        LoanProductCreditAllocationRule rule2 = createRule(CreditAllocationTransactionType.CHARGEBACK,
                List.of(PENALTY, FEE, INTEREST, PRINCIPAL));

        loanProduct.getCreditAllocationRules().add(rule1);

        boolean result = underTest.updateCreditAllocationRules(loanProduct, List.of(rule2));
        Assertions.assertFalse(result);
        Assertions.assertEquals(1, loanProduct.getCreditAllocationRules().size());
        Assertions.assertEquals(rule1.getTransactionType(), loanProduct.getCreditAllocationRules().get(0).getTransactionType());
        Assertions.assertEquals(rule1.getAllocationTypes(), loanProduct.getCreditAllocationRules().get(0).getAllocationTypes());
    }

    @Test
    public void testMergerExistingDeleted() {
        LoanProduct loanProduct = new LoanProduct();

        LoanProductCreditAllocationRule rule1 = createRule(CreditAllocationTransactionType.CHARGEBACK,
                List.of(PENALTY, FEE, INTEREST, PRINCIPAL));

        loanProduct.getCreditAllocationRules().add(rule1);

        boolean result = underTest.updateCreditAllocationRules(loanProduct, List.of());
        Assertions.assertTrue(result);
        Assertions.assertEquals(0, loanProduct.getCreditAllocationRules().size());
    }

    public LoanProductCreditAllocationRule createRule(CreditAllocationTransactionType transactionType,
            List<AllocationType> allocationTypeList) {
        return new LoanProductCreditAllocationRule(null, transactionType, allocationTypeList);
    }

}
