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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
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
public class LoanWithAdvancedPaymentAllocationIntegrationTests {

    private static ClientHelper CLIENT_HELPER;
    private static Account ASSET_ACCOUNT;
    private static Account FEE_PENALTY_ACCOUNT;
    private static Account EXPENSE_ACCOUNT;
    private static Account INCOME_ACCOUNT;
    private static Account OVERPAYMENT_ACCOUNT;
    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER;

    @BeforeAll
    public static void setupTests() {
        Utils.initializeRESTAssured();
        RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        AccountHelper accountHelper = new AccountHelper(requestSpec, responseSpec);
        LOAN_TRANSACTION_HELPER = new LoanTransactionHelper(requestSpec, responseSpec);
        CLIENT_HELPER = new ClientHelper(requestSpec, responseSpec);

        ASSET_ACCOUNT = accountHelper.createAssetAccount();
        FEE_PENALTY_ACCOUNT = accountHelper.createAssetAccount();
        EXPENSE_ACCOUNT = accountHelper.createExpenseAccount();
        INCOME_ACCOUNT = accountHelper.createIncomeAccount();
        OVERPAYMENT_ACCOUNT = accountHelper.createLiabilityAccount();
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
        final PostClientsResponse clientResponse = CLIENT_HELPER.createClient(new PostClientsRequest().activationDate("01 January 2022")
                .active(true).dateFormat("dd MMMM yyyy").fullname("fullName").locale("en").legalFormId(1L).officeId(1L));
        Integer loanId = createLoanAccount(LOAN_TRANSACTION_HELPER, clientResponse.getClientId().toString(), loanProductId.toString(),
                "02 January 2022");
        // then
        List<AdvancedPaymentData> allocationRules = LOAN_TRANSACTION_HELPER.getAdvancedPaymentAllocationRules(loanId);
        Assertions.assertNotNull(allocationRules);

        Optional<AdvancedPaymentData> first = allocationRules.stream()
                .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(first.isPresent());
        Assertions.assertEquals(defaultAllocation, first.get());

        Optional<AdvancedPaymentData> second = allocationRules.stream()
                .filter(advancedPaymentData -> "REPAYMENT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(second.isPresent());
        Assertions.assertEquals(repaymentPaymentAllocation, second.get());

        // when
        LOAN_TRANSACTION_HELPER.updateLoanProduct(loanProductId.longValue(), updateLoanProductRequest(defaultAllocation));
        // then
        allocationRules = LOAN_TRANSACTION_HELPER.getAdvancedPaymentAllocationRules(loanId);
        Assertions.assertNotNull(allocationRules);

        first = allocationRules.stream().filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType()))
                .findFirst();
        Assertions.assertTrue(first.isPresent());
        Assertions.assertEquals(defaultAllocation, first.get());

        second = allocationRules.stream().filter(advancedPaymentData -> "REPAYMENT".equals(advancedPaymentData.getTransactionType()))
                .findFirst();
        Assertions.assertTrue(second.isPresent());
        Assertions.assertEquals(repaymentPaymentAllocation, second.get());
    }

    private String createLoanJSON(AdvancedPaymentData... advancedPaymentData) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withAccountingRulePeriodicAccrual(new Account[] { ASSET_ACCOUNT, EXPENSE_ACCOUNT, INCOME_ACCOUNT, OVERPAYMENT_ACCOUNT })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(FEE_PENALTY_ACCOUNT).addAdvancedPaymentAllocation(advancedPaymentData)
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL)
                .build();
        return loanProductJSON;
    }

    private PutLoanProductsProductIdRequest updateLoanProductRequest(AdvancedPaymentData... advancedPaymentData) {
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = new PutLoanProductsProductIdRequest();
        putLoanProductsProductIdRequest.paymentAllocation(Arrays.stream(advancedPaymentData).toList());
        return putLoanProductsProductIdRequest;
    }

    private Integer createLoanAccount(final LoanTransactionHelper loanTransactionHelper, final String clientId, final String loanProductId,
            final String operationDate) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15,000.00").withLoanTermFrequency("4")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("4").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("0") //
                .withExpectedDisbursementDate(operationDate) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY) //
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE.toString()) //
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()) //
                .build(clientId, loanProductId, null);
        return loanTransactionHelper.getLoanId(loanApplicationJSON);
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
}
