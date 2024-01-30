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
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
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
public class LoanProductWithCreditAllocationsIntegrationTests {

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
    public void testCreateAndReadLoanProductWithAdvancedPaymentAndCreditAllocations() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                .addCreditAllocations(createChargebackAllocation()).build();
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);

        // then
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));
    }

    @Test
    public void testCreateLoanProductAndLaterAddCreditAllocation() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // create empty
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation).build();
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertEquals(0, loanProduct.getCreditAllocation().size());

        // add credit allocation
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = updateLoanProductRequest(createChargebackAllocation());
        LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(), putLoanProductsProductIdRequest);
        loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));
    }

    @Test
    public void testCreateAndUpdateCreditAllocation() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                .addCreditAllocations(createChargebackAllocation()).build();
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));

        CreditAllocationData updated = createChargebackAllocation();
        List<CreditAllocationOrder> updatedOrder = createCreditAllocationOrders("FEE", "INTEREST", "PRINCIPAL", "PENALTY");
        updated.setCreditAllocationOrder(updatedOrder);

        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = updateLoanProductRequest(updated);
        LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(), putLoanProductsProductIdRequest);
        loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(updatedOrder, loanProduct.getCreditAllocation().get(0).getCreditAllocationOrder());
    }

    @Test
    public void testCreateAndDeleteCreditAllocation() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                .addCreditAllocations(createChargebackAllocation()).build();
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));

        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = updateLoanProductRequest(new CreditAllocationData[] {});
        LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(), putLoanProductsProductIdRequest);
        loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertEquals(0, loanProduct.getCreditAllocation().size());
    }

    @Test
    public void testCreditAllocationIsNotAllowedWhenPaymentStrategyIsNotAdvancedPaymentStrategy() {
        // given
        String loanProductJSON = baseLoanProduct().withRepaymentStrategy("mifos-standard-strategy")
                .addCreditAllocations(createChargebackAllocation()).build();
        ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
        LoanTransactionHelper validationErrorHelper = new LoanTransactionHelper(REQUEST_SPEC, errorResponse);

        // when
        List<Map<String, String>> loanProductError = validationErrorHelper.getLoanProductError(loanProductJSON, "errors");

        // then
        Assertions.assertEquals("In case 'mifos-standard-strategy' payment strategy, creditAllocation must not be provided",
                loanProductError.get(0).get("defaultUserMessage"));
    }

    @Test
    public void testCreateLoanProductWithCreditAllocationThenUpdatePaymentStrategyShouldFail() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        String loanProductJSON = baseLoanProduct().addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                .addCreditAllocations(createChargebackAllocation()).build();
        Integer loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = LOAN_TRANSACTION_HELPER.getLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getCreditAllocation());
        Assertions.assertEquals(1, loanProduct.getCreditAllocation().size());
        Assertions.assertEquals(createChargebackAllocation(), loanProduct.getCreditAllocation().get(0));
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = updateLoanProductRequest("mifos-standard-strategy");
        putLoanProductsProductIdRequest.setPaymentAllocation(List.of());

        // when
        CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class, () -> {
            LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(), putLoanProductsProductIdRequest);
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
        return new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4").withRepaymentAfterEvery("1")
                .withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withAccountingRulePeriodicAccrual(new Account[] { ASSET_ACCOUNT, EXPENSE_ACCOUNT, INCOME_ACCOUNT, OVERPAYMENT_ACCOUNT })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(FEE_PENALTY_ACCOUNT).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
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
