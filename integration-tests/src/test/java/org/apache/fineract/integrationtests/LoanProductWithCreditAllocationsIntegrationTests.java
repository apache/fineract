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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanProductWithCreditAllocationsIntegrationTests extends FeignLoanTestBase {

    @Test
    public void testCreateAndReadLoanProductWithAdvancedPaymentAndCreditAllocations() {
        // given
        AdvancedPaymentData defaultAllocation = createCustomDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                .addCreditAllocations(createChargebackAllocation()).build();
        Long loanProductId = createLoanProductFromJson(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);

        // then
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));
    }

    @Test
    public void testCreateLoanProductAndLaterAddCreditAllocation() {
        // given
        AdvancedPaymentData defaultAllocation = createCustomDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // create empty
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation).build();
        Long loanProductId = createLoanProductFromJson(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertEquals(0, loanProduct.getCreditAllocation().size());

        // add credit allocation
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = updateLoanProductRequest(createChargebackAllocation());
        updateLoanProduct(loanProductId, putLoanProductsProductIdRequest);
        loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));
    }

    @Test
    public void testCreateAndUpdateCreditAllocation() {
        // given
        AdvancedPaymentData defaultAllocation = createCustomDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                .addCreditAllocations(createChargebackAllocation()).build();
        Long loanProductId = createLoanProductFromJson(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));

        CreditAllocationData updated = createChargebackAllocation();
        List<CreditAllocationOrder> updatedOrder = createCreditAllocationOrders("FEE", "INTEREST", "PRINCIPAL", "PENALTY");
        updated.setCreditAllocationOrder(updatedOrder);

        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = updateLoanProductRequest(updated);
        updateLoanProduct(loanProductId, putLoanProductsProductIdRequest);
        loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(updatedOrder, loanProduct.getCreditAllocation().get(0).getCreditAllocationOrder());
    }

    @Test
    public void testCreateAndDeleteCreditAllocation() {
        // given
        AdvancedPaymentData defaultAllocation = createCustomDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                .addCreditAllocations(createChargebackAllocation()).build();
        Long loanProductId = createLoanProductFromJson(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));

        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = updateLoanProductRequest(new CreditAllocationData[] {});
        updateLoanProduct(loanProductId, putLoanProductsProductIdRequest);
        loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertEquals(0, loanProduct.getCreditAllocation().size());
    }

    @Test
    public void testCreditAllocationIsNotAllowedWhenPaymentStrategyIsNotAdvancedPaymentStrategy() {
        // given
        String loanProductJSON = baseLoanProduct().withRepaymentStrategy("mifos-standard-strategy")
                .withLoanScheduleType(LoanScheduleType.CUMULATIVE).addCreditAllocations(createChargebackAllocation()).build();

        // when
        List<Map<String, String>> loanProductError = getLoanProductError(loanProductJSON, "errors");

        // then
        Assertions.assertEquals("In case 'mifos-standard-strategy' payment strategy, creditAllocation must not be provided",
                loanProductError.get(0).get("defaultUserMessage"));
    }

    @Test
    public void testCreateLoanProductWithCreditAllocationThenUpdatePaymentStrategyShouldFail() {
        // given
        AdvancedPaymentData defaultAllocation = createCustomDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                .addCreditAllocations(createChargebackAllocation()).build();
        Long loanProductId = createLoanProductFromJson(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = updateLoanProductRequest("mifos-standard-strategy");
        putLoanProductsProductIdRequest.setPaymentAllocation(List.of());

        // when
        CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class, () -> {
            updateLoanProduct(loanProductId, putLoanProductsProductIdRequest);
        });

        // then
        Assertions.assertTrue(callFailedRuntimeException.getMessage()
                .contains("In case 'mifos-standard-strategy' payment strategy, creditAllocation must not be provided"));
    }

    private CreditAllocationData createChargebackAllocation() {
        CreditAllocationData creditAllocationData = new CreditAllocationData();
        creditAllocationData.setTransactionType("CHARGEBACK");
        creditAllocationData.setCreditAllocationOrder(createCreditAllocationOrders("PENALTY", "FEE", "INTEREST", "PRINCIPAL"));
        return creditAllocationData;
    }

    public List<CreditAllocationOrder> createCreditAllocationOrders(String... allocationRule) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(allocationRule).map(allocation -> {
            CreditAllocationOrder creditAllocationOrder = new CreditAllocationOrder();
            creditAllocationOrder.setCreditAllocationRule(allocation);
            creditAllocationOrder.setOrder(integer.getAndIncrement());
            return creditAllocationOrder;
        }).toList();
    }

    private LoanProductTestBuilder baseLoanProduct() {
        Account assetAccount = getAccounts().getLoansReceivableAccount();
        Account feeReceivableAccount = getAccounts().getFeeReceivableAccount();
        Account expenseAccount = getAccounts().getChargeOffExpenseAccount();
        Account incomeAccount = getAccounts().getInterestIncomeAccount();
        Account overpaymentAccount = getAccounts().getOverpaymentAccount();

        return new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4").withRepaymentAfterEvery("1")
                .withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, expenseAccount, incomeAccount, overpaymentAccount })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(feeReceivableAccount).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL);
    }

    private PutLoanProductsProductIdRequest updateLoanProductRequest(CreditAllocationData... creditAllocationData) {
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = new PutLoanProductsProductIdRequest();
        putLoanProductsProductIdRequest.creditAllocation(Arrays.stream(creditAllocationData).toList());
        return putLoanProductsProductIdRequest;
    }

    private PutLoanProductsProductIdRequest updateLoanProductRequest(String transactionProcessingStrategyCode) {
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = new PutLoanProductsProductIdRequest();
        putLoanProductsProductIdRequest.setTransactionProcessingStrategyCode(transactionProcessingStrategyCode);
        return putLoanProductsProductIdRequest;
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

    private AdvancedPaymentData createCustomDefaultPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

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
