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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanProductWithAdvancedPaymentAllocationIntegrationTests {

    private static ResponseSpecification RESPONSE_SPEC;
    private static RequestSpecification REQUEST_SPEC;
    private static Account ASSET_ACCOUNT;
    private static Account FEE_PENALTY_ACCOUNT;
    private static Account TRANSFER_ACCOUNT;
    private static Account EXPENSE_ACCOUNT;
    private static Account INCOME_ACCOUNT;
    private static Account OVERPAYMENT_ACCOUNT;
    private static FinancialActivityAccountHelper FINANCIAL_ACTIVITY_ACCOUNT_HELPER;
    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER;

    @BeforeAll
    public static void setupTests() {
        Utils.initializeRESTAssured();
        REQUEST_SPEC = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        REQUEST_SPEC.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        RESPONSE_SPEC = new ResponseSpecBuilder().expectStatusCode(200).build();
        AccountHelper accountHelper = new AccountHelper(REQUEST_SPEC, RESPONSE_SPEC);
        FINANCIAL_ACTIVITY_ACCOUNT_HELPER = new FinancialActivityAccountHelper(REQUEST_SPEC);
        LOAN_TRANSACTION_HELPER = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC);

        ASSET_ACCOUNT = accountHelper.createAssetAccount();
        FEE_PENALTY_ACCOUNT = accountHelper.createAssetAccount();
        TRANSFER_ACCOUNT = accountHelper.createAssetAccount();
        EXPENSE_ACCOUNT = accountHelper.createExpenseAccount();
        INCOME_ACCOUNT = accountHelper.createIncomeAccount();
        OVERPAYMENT_ACCOUNT = accountHelper.createLiabilityAccount();

        setProperFinancialActivity(TRANSFER_ACCOUNT);
    }

    @Test
    public void testCreateAndReadLoanProductWithAdvancedPayment() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(createLoanJSON(defaultAllocation, repaymentPaymentAllocation));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);

        // then
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());
        Optional<AdvancedPaymentData> first = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(first.isPresent());
        Assertions.assertEquals(defaultAllocation, first.get());

        Optional<AdvancedPaymentData> second = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "REPAYMENT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(second.isPresent());
        Assertions.assertEquals(repaymentPaymentAllocation, second.get());
    }

    @Test
    public void testUpdateLoanProductOneAllocationIsRemoved() {
        // given a loan with two allocations
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(createLoanJSON(defaultAllocation, repaymentPaymentAllocation));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());

        // when an allocation is removed
        LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(), updateLoanProductRequest(defaultAllocation));

        // then it shall be removed.
        loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(1, loanProduct.getPaymentAllocation().size());
        Assertions.assertEquals(defaultAllocation, loanProduct.getPaymentAllocation().get(0));
    }

    @Test
    public void testUpdateLoanProductOneAllocationIsAdded() {
        // given a loan with one allocation
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(createLoanJSON(defaultAllocation));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(1, loanProduct.getPaymentAllocation().size());

        // when a new allocation is added
        LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(),
                updateLoanProductRequest(defaultAllocation, repaymentPaymentAllocation));

        // then it shall be added.
        loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());
        Optional<AdvancedPaymentData> first = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(first.isPresent());
        Assertions.assertEquals(defaultAllocation, first.get());

        Optional<AdvancedPaymentData> second = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "REPAYMENT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(second.isPresent());
        Assertions.assertEquals(repaymentPaymentAllocation, second.get());
    }

    @Test
    public void testUpdateShouldFailWhenNoDefaultAllocationIsProvided() {
        // given a loan with two allocations
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(createLoanJSON(defaultAllocation, repaymentPaymentAllocation));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());

        // when an allocation is removed
        CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                () -> LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(),
                        updateLoanProductRequest(repaymentPaymentAllocation)));

        Assertions.assertTrue(callFailedRuntimeException.getMessage()
                .contains("Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided"));
    }

    @Test
    public void testUpdateShouldFailWhenStrategyIsChangedBackButPaymentAllocationsAreNotRemoved() {
        // given a loan with two allocations
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(createLoanJSON(defaultAllocation, repaymentPaymentAllocation));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());

        // when an allocation is removed
        CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                () -> LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(),
                        updateLoanProductRequest("mifos-standard-strategy")));

        Assertions.assertTrue(callFailedRuntimeException.getMessage()
                .contains("In case 'mifos-standard-strategy' payment strategy, payment_allocation must not be provided"));
    }

    @Test
    public void testCreateShouldFailWhenNoAllocationRuleIsProvided() {
        // given
        ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        LoanTransactionHelper validationErrorHelper = new LoanTransactionHelper(REQUEST_SPEC, errorResponse);

        String loanProduct = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withAccountingRulePeriodicAccrual(new Account[] { ASSET_ACCOUNT, EXPENSE_ACCOUNT, INCOME_ACCOUNT, OVERPAYMENT_ACCOUNT })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(FEE_PENALTY_ACCOUNT).withRepaymentStrategy("advanced-payment-allocation-strategy").build();

        // when
        List<Map<String, String>> loanProductError = validationErrorHelper.getLoanProductError(loanProduct, "errors");
        Assertions.assertEquals("Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided",
                loanProductError.get(0).get("defaultUserMessage"));
    }

    @Test
    public void testCreateShouldFailWhenNoDefaultAllocationIsProvided() {
        // given
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        LoanTransactionHelper validationErrorHelper = new LoanTransactionHelper(REQUEST_SPEC, errorResponse);

        // when
        List<Map<String, String>> loanProductError = validationErrorHelper.getLoanProductError(createLoanJSON(repaymentPaymentAllocation),
                "errors");
        Assertions.assertEquals("Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided",
                loanProductError.get(0).get("defaultUserMessage"));
    }

    private String createLoanJSON(AdvancedPaymentData... advancedPaymentData) {
        return new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4").withRepaymentAfterEvery("1")
                .withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withAccountingRulePeriodicAccrual(new Account[] { ASSET_ACCOUNT, EXPENSE_ACCOUNT, INCOME_ACCOUNT, OVERPAYMENT_ACCOUNT })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(FEE_PENALTY_ACCOUNT).addAdvancedPaymentAllocation(advancedPaymentData)
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL)
                .build();
    }

    private PutLoanProductsProductIdRequest updateLoanProductRequest(AdvancedPaymentData... advancedPaymentData) {
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = new PutLoanProductsProductIdRequest();
        putLoanProductsProductIdRequest.paymentAllocation(Arrays.stream(advancedPaymentData).toList());
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

    private AdvancedPaymentData createDefaultPaymentAllocation() {
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

    private static void setProperFinancialActivity(Account transferAccount) {
        List<GetFinancialActivityAccountsResponse> financialMappings = FINANCIAL_ACTIVITY_ACCOUNT_HELPER.getAllFinancialActivityAccounts();
        financialMappings.forEach(mapping -> FINANCIAL_ACTIVITY_ACCOUNT_HELPER.deleteFinancialActivityAccount(mapping.getId()));
        FINANCIAL_ACTIVITY_ACCOUNT_HELPER.createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                .financialActivityId((long) AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue())
                .glAccountId((long) transferAccount.getAccountID()));
    }

}
