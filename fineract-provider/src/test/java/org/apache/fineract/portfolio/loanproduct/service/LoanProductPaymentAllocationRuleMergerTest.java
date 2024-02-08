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

import static org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule.LAST_INSTALLMENT;
import static org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType.DEFAULT;
import static org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType.REPAYMENT;
import static org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType.DUE_INTEREST;
import static org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType.PAST_DUE_FEE;

import java.util.List;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductPaymentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LoanProductPaymentAllocationRuleMergerTest {

    @Test
    public void testMergerOneNewAdded() {
        // given
        LoanProductPaymentAllocationRuleMerger underTest = new LoanProductPaymentAllocationRuleMerger();
        LoanProduct loanProduct = new LoanProduct();

        LoanProductPaymentAllocationRule rule1 = createRule(DEFAULT, LAST_INSTALLMENT, List.of(DUE_INTEREST));

        // when
        boolean result = underTest.updateProductPaymentAllocationRules(loanProduct, List.of(rule1));

        // then
        Assertions.assertTrue(result);
        Assertions.assertEquals(1, loanProduct.getPaymentAllocationRules().size());
        Assertions.assertEquals(rule1, loanProduct.getPaymentAllocationRules().get(0));
    }

    @Test
    public void testMergeExistingUpdated() {
        // given
        LoanProductPaymentAllocationRuleMerger underTest = new LoanProductPaymentAllocationRuleMerger();
        LoanProduct loanProduct = new LoanProduct();

        LoanProductPaymentAllocationRule rule1 = createRule(DEFAULT, LAST_INSTALLMENT, List.of(DUE_INTEREST));
        LoanProductPaymentAllocationRule rule2 = createRule(DEFAULT, LAST_INSTALLMENT, List.of(PAST_DUE_FEE));

        loanProduct.getPaymentAllocationRules().add(rule1);

        // when
        boolean result = underTest.updateProductPaymentAllocationRules(loanProduct, List.of(rule2));

        // then
        Assertions.assertTrue(result);
        Assertions.assertEquals(1, loanProduct.getPaymentAllocationRules().size());
        Assertions.assertEquals(PAST_DUE_FEE, loanProduct.getPaymentAllocationRules().get(0).getAllocationTypes().get(0));
    }

    @Test
    public void testNothingChanged() {
        // given
        LoanProductPaymentAllocationRuleMerger underTest = new LoanProductPaymentAllocationRuleMerger();
        LoanProduct loanProduct = new LoanProduct();

        LoanProductPaymentAllocationRule rule1 = createRule(DEFAULT, LAST_INSTALLMENT, List.of(DUE_INTEREST));
        LoanProductPaymentAllocationRule rule2 = createRule(REPAYMENT, LAST_INSTALLMENT, List.of(PAST_DUE_FEE));

        loanProduct.getPaymentAllocationRules().addAll(List.of(rule1, rule2));

        // when
        boolean result = underTest.updateProductPaymentAllocationRules(loanProduct, List.of(rule2, rule1));

        // then
        Assertions.assertFalse(result);
        Assertions.assertEquals(2, loanProduct.getPaymentAllocationRules().size());
        Assertions.assertEquals(rule1, loanProduct.getPaymentAllocationRules().get(0));
        Assertions.assertEquals(rule2, loanProduct.getPaymentAllocationRules().get(1));
    }

    @Test
    public void testMergerExistingDeleted() {
        // given
        LoanProductPaymentAllocationRuleMerger underTest = new LoanProductPaymentAllocationRuleMerger();
        LoanProduct loanProduct = new LoanProduct();
        LoanProductPaymentAllocationRule rule1 = createRule(DEFAULT, LAST_INSTALLMENT, List.of(DUE_INTEREST));
        loanProduct.getPaymentAllocationRules().add(rule1);

        // when
        boolean result = underTest.updateProductPaymentAllocationRules(loanProduct, List.of());

        // then
        Assertions.assertTrue(result);
        Assertions.assertEquals(0, loanProduct.getPaymentAllocationRules().size());
    }

    @Test
    public void testMergeOneOriginalOneAdded() {
        // given
        LoanProductPaymentAllocationRuleMerger underTest = new LoanProductPaymentAllocationRuleMerger();
        LoanProduct loanProduct = new LoanProduct();
        LoanProductPaymentAllocationRule rule1 = createRule(DEFAULT, LAST_INSTALLMENT, List.of(DUE_INTEREST));
        loanProduct.getPaymentAllocationRules().add(rule1);
        LoanProductPaymentAllocationRule rule2 = createRule(REPAYMENT, LAST_INSTALLMENT, List.of(DUE_INTEREST));

        // when
        boolean result = underTest.updateProductPaymentAllocationRules(loanProduct, List.of(rule1, rule2));

        // then
        Assertions.assertTrue(result);
        Assertions.assertEquals(2, loanProduct.getPaymentAllocationRules().size());
        Assertions.assertEquals(rule1, loanProduct.getPaymentAllocationRules().get(0));
        Assertions.assertEquals(rule2, loanProduct.getPaymentAllocationRules().get(1));
    }

    public LoanProductPaymentAllocationRule createRule(PaymentAllocationTransactionType transactionType,
            FutureInstallmentAllocationRule futureInstallmentAllocationRule, List<PaymentAllocationType> allocationTypeList) {
        return new LoanProductPaymentAllocationRule(null, transactionType, allocationTypeList, futureInstallmentAllocationRule);
    }

}
