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

import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanWithAdvancedPaymentAllocationIntegrationTests extends FeignLoanTestBase {

    @Test
    public void testCreateAndReadLoanProductWithAdvancedPayment() {
        runAt("02 January 2022", () -> {
            Account assetAccount = accountHelper.createAssetAccount("apaAsset");
            Account expenseAccount = accountHelper.createExpenseAccount("apaExpense");
            Account incomeAccount = accountHelper.createIncomeAccount("apaIncome");
            Account overpaymentAccount = accountHelper.createLiabilityAccount("apaOverpayment");
            Account feePenaltyAccount = accountHelper.createAssetAccount("apaFeePenalty");

            AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation("NEXT_INSTALLMENT");
            AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

            Long loanProductId = createLoanProduct(createLoanProductRequest(assetAccount, expenseAccount, incomeAccount, overpaymentAccount,
                    feePenaltyAccount, defaultAllocation, repaymentPaymentAllocation));
            Assertions.assertNotNull(loanProductId);
            GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);

            Long clientId = createClient("01 January 2022");
            Long loanId = createLoanAccount(clientId, loanProductId, "02 January 2022");

            List<AdvancedPaymentData> allocationRules = getAdvancedPaymentAllocationRules(loanId);
            Assertions.assertNotNull(allocationRules);

            Optional<AdvancedPaymentData> first = allocationRules.stream()
                    .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
            Assertions.assertTrue(first.isPresent());
            Assertions.assertEquals(defaultAllocation, first.get());

            Optional<AdvancedPaymentData> second = allocationRules.stream()
                    .filter(advancedPaymentData -> "REPAYMENT".equals(advancedPaymentData.getTransactionType())).findFirst();
            Assertions.assertTrue(second.isPresent());
            Assertions.assertEquals(repaymentPaymentAllocation, second.get());

            updateLoanProduct(loanProductId, updateLoanProductRequest(defaultAllocation));

            allocationRules = getAdvancedPaymentAllocationRules(loanId);
            Assertions.assertNotNull(allocationRules);

            first = allocationRules.stream().filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType()))
                    .findFirst();
            Assertions.assertTrue(first.isPresent());
            Assertions.assertEquals(defaultAllocation, first.get());

            second = allocationRules.stream().filter(advancedPaymentData -> "REPAYMENT".equals(advancedPaymentData.getTransactionType()))
                    .findFirst();
            Assertions.assertTrue(second.isPresent());
            Assertions.assertEquals(repaymentPaymentAllocation, second.get());
        });
    }

    private PostLoanProductsRequest createLoanProductRequest(Account assetAccount, Account expenseAccount, Account incomeAccount,
            Account overpaymentAccount, Account feePenaltyAccount, AdvancedPaymentData... advancedPaymentData) {
        return new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4").withRepaymentAfterEvery("1")
                .withRepaymentTypeAsMonth().withinterestRatePerPeriod("1").withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, expenseAccount, incomeAccount, overpaymentAccount })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(feePenaltyAccount).addAdvancedPaymentAllocation(advancedPaymentData)
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL)
                .buildRequest();
    }

    private PutLoanProductsProductIdRequest updateLoanProductRequest(AdvancedPaymentData... advancedPaymentData) {
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = new PutLoanProductsProductIdRequest();
        putLoanProductsProductIdRequest.paymentAllocation(Arrays.stream(advancedPaymentData).toList());
        return putLoanProductsProductIdRequest;
    }

    private Long createLoanAccount(Long clientId, Long loanProductId, String operationDate) {
        return applyForLoan(new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal("15000.00"))//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString())//
                .loanType("individual")//
                .expectedDisbursementDate(operationDate)//
                .submittedOnDate(operationDate)//
                .maxOutstandingLoanBalance(new BigDecimal("36000"))//
                .collateral(List.of())//
                .locale("en_GB")//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
    }

    private AdvancedPaymentData createRepaymentPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("REPAYMENT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_INTEREST, PaymentAllocationType.PAST_DUE_PRINCIPAL,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_INTEREST,
                PaymentAllocationType.DUE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
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
