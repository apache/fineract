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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoanProductUpdateApiTest {

    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER;
    private static ResponseSpecification RESPONSE_SPEC;
    private static RequestSpecification REQUEST_SPEC;

    @BeforeAll
    public static void setupTests() {
        Utils.initializeRESTAssured();
        REQUEST_SPEC = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        REQUEST_SPEC.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        RESPONSE_SPEC = new ResponseSpecBuilder().expectStatusCode(200).build();
        LOAN_TRANSACTION_HELPER = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC);
    }

    @Test
    public void loanProductModifyForAdvancedPaymentAllocationRuleTest() {
        // create loan product with Advanced Payment Allocation Strategy with default allocation with future installment
        // allocation as NEXT_INSTALLMENT
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        Integer loanProductId = createLoanProduct(defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        // verify allocation rule
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());

        Optional<AdvancedPaymentData> defaultAllocationAfterCreate = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(defaultAllocationAfterCreate.isPresent());
        Assertions.assertEquals(futureInstallmentAllocationRule, defaultAllocationAfterCreate.get().getFutureInstallmentAllocationRule());

        // Change future installment allocation rule to "LAST_INSTALLMENT" and update loan product
        futureInstallmentAllocationRule = "LAST_INSTALLMENT";
        defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        loanProductId = updateLoanProduct(loanProductId, defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());

        // verify allocation rule
        Optional<AdvancedPaymentData> defaultAllocationAfterUpdate = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(defaultAllocationAfterUpdate.isPresent());
        Assertions.assertEquals(futureInstallmentAllocationRule, defaultAllocationAfterUpdate.get().getFutureInstallmentAllocationRule());

    }

    @Test
    public void loanProductWithInterestCalculationTypeDailyModifyForAdvancedPaymentAllocationRuleTest() {
        // create loan product with Advanced Payment Allocation Strategy with default allocation with future installment
        // allocation as NEXT_INSTALLMENT
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        Integer loanProductId = createLoanProductWithInterestCalculationPeriodTypeDaily(defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        // verify allocation rule
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());

        Optional<AdvancedPaymentData> defaultAllocationAfterCreate = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(defaultAllocationAfterCreate.isPresent());
        Assertions.assertEquals(futureInstallmentAllocationRule, defaultAllocationAfterCreate.get().getFutureInstallmentAllocationRule());

        // Change future installment allocation rule to "LAST_INSTALLMENT" and update loan product
        futureInstallmentAllocationRule = "LAST_INSTALLMENT";
        defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        loanProductId = updateLoanProduct(loanProductId, defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());

        // verify allocation rule
        Optional<AdvancedPaymentData> defaultAllocationAfterUpdate = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(defaultAllocationAfterUpdate.isPresent());
        Assertions.assertEquals(futureInstallmentAllocationRule, defaultAllocationAfterUpdate.get().getFutureInstallmentAllocationRule());

    }

    private Integer updateLoanProduct(Integer loanProductId, AdvancedPaymentData... advancedPaymentData) {
        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest()
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy")
                .paymentAllocation(Arrays.stream(advancedPaymentData).toList()).locale("en");
        return LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(), requestModifyLoan).getResourceId().intValue();
    }

    private Integer createLoanProduct(AdvancedPaymentData... advancedPaymentData) {
        String loanProductCreateJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .addAdvancedPaymentAllocation(advancedPaymentData).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL).build();
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductCreateJSON);

    }

    private Integer createLoanProductWithInterestCalculationPeriodTypeDaily(AdvancedPaymentData... advancedPaymentData) {
        String loanProductCreateJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsDays().withAllowPartialPeriodInterestCalculation(false)
                .addAdvancedPaymentAllocation(advancedPaymentData).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL).build();
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductCreateJSON);

    }

    private AdvancedPaymentData createDefaultPaymentAllocation(String futureInstallmentAllocationRule) {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }
}
