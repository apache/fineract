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
package org.apache.fineract.integrationtests;

import java.util.Arrays;
import java.util.Optional;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanProductUpdateApiTest extends FeignLoanTestBase {

    private static final String DEFAULT_TRANSACTION_TYPE = "DEFAULT";
    private static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";

    @Test
    public void loanProductModifyForAdvancedPaymentAllocationRuleTest() {
        // create loan product with Advanced Payment Allocation Strategy with default allocation with future installment
        // allocation as NEXT_INSTALLMENT
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        Long loanProductId = createAdvancedPaymentAllocationProduct(defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        // verify allocation rule
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());

        Optional<AdvancedPaymentData> defaultAllocationAfterCreate = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> DEFAULT_TRANSACTION_TYPE.equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(defaultAllocationAfterCreate.isPresent());
        Assertions.assertEquals(futureInstallmentAllocationRule, defaultAllocationAfterCreate.get().getFutureInstallmentAllocationRule());

        // Change future installment allocation rule to "LAST_INSTALLMENT" and update loan product
        futureInstallmentAllocationRule = "LAST_INSTALLMENT";
        defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        loanProductId = updatePaymentAllocation(loanProductId, defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());

        // verify allocation rule
        Optional<AdvancedPaymentData> defaultAllocationAfterUpdate = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> DEFAULT_TRANSACTION_TYPE.equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(defaultAllocationAfterUpdate.isPresent());
        Assertions.assertEquals(futureInstallmentAllocationRule, defaultAllocationAfterUpdate.get().getFutureInstallmentAllocationRule());

    }

    @Test
    public void loanProductWithInterestCalculationTypeDailyModifyForAdvancedPaymentAllocationRuleTest() {
        // create loan product with Advanced Payment Allocation Strategy with default allocation with future installment
        // allocation as NEXT_INSTALLMENT
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        Long loanProductId = createAdvancedPaymentAllocationProductWithInterestCalculationPeriodTypeDaily(defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        // verify allocation rule
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());

        Optional<AdvancedPaymentData> defaultAllocationAfterCreate = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> DEFAULT_TRANSACTION_TYPE.equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(defaultAllocationAfterCreate.isPresent());
        Assertions.assertEquals(futureInstallmentAllocationRule, defaultAllocationAfterCreate.get().getFutureInstallmentAllocationRule());

        // Change future installment allocation rule to "LAST_INSTALLMENT" and update loan product
        futureInstallmentAllocationRule = "LAST_INSTALLMENT";
        defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        loanProductId = updatePaymentAllocation(loanProductId, defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());

        // verify allocation rule
        Optional<AdvancedPaymentData> defaultAllocationAfterUpdate = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> DEFAULT_TRANSACTION_TYPE.equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(defaultAllocationAfterUpdate.isPresent());
        Assertions.assertEquals(futureInstallmentAllocationRule, defaultAllocationAfterUpdate.get().getFutureInstallmentAllocationRule());

    }

    private Long updatePaymentAllocation(Long loanProductId, AdvancedPaymentData... advancedPaymentData) {
        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest()
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .paymentAllocation(Arrays.stream(advancedPaymentData).toList()).locale("en");
        return updateLoanProduct(loanProductId, requestModifyLoan).getResourceId();
    }

    private Long createAdvancedPaymentAllocationProduct(AdvancedPaymentData... advancedPaymentData) {
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .addAdvancedPaymentAllocation(advancedPaymentData).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL).buildRequest());
    }

    private Long createAdvancedPaymentAllocationProductWithInterestCalculationPeriodTypeDaily(AdvancedPaymentData... advancedPaymentData) {
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsDays().withAllowPartialPeriodInterestCalculation(false)
                .addAdvancedPaymentAllocation(advancedPaymentData).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL).buildRequest());
    }
}
