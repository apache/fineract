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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeOffBehaviour;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanProductWithAdvancedPaymentAllocationIntegrationTests extends FeignLoanTestBase {

    private static Account ASSET_ACCOUNT;
    private static Account FEE_PENALTY_ACCOUNT;
    private static Account TRANSFER_ACCOUNT;
    private static Account EXPENSE_ACCOUNT;
    private static Account INCOME_ACCOUNT;
    private static Account OVERPAYMENT_ACCOUNT;
    private static final FinancialActivityAccountHelper FINANCIAL_ACTIVITY_ACCOUNT_HELPER = new FinancialActivityAccountHelper(null);

    @BeforeAll
    public static void setupAccounts() {
        ASSET_ACCOUNT = accountHelper.createAssetAccount("advancedPaymentAsset");
        FEE_PENALTY_ACCOUNT = accountHelper.createAssetAccount("advancedPaymentFeePenalty");
        TRANSFER_ACCOUNT = accountHelper.createAssetAccount("advancedPaymentTransfer");
        EXPENSE_ACCOUNT = accountHelper.createExpenseAccount("advancedPaymentExpense");
        INCOME_ACCOUNT = accountHelper.createIncomeAccount("advancedPaymentIncome");
        OVERPAYMENT_ACCOUNT = accountHelper.createLiabilityAccount("advancedPaymentOverpayment");

        setProperFinancialActivity(TRANSFER_ACCOUNT);
    }

    @Test
    public void testCreateAndReadLoanProductWithAdvancedPayment() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        Long loanProductId = createLoanProductFromJson(loanProductTestBuilder(
                customization -> customization.addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);

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
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        Long loanProductId = createLoanProductFromJson(loanProductTestBuilder(
                customization -> customization.addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());

        // when an allocation is removed
        updateLoanProduct(loanProductId, updateLoanProductRequest(defaultAllocation));

        // then it shall be removed.
        loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(1, loanProduct.getPaymentAllocation().size());
        Assertions.assertEquals(defaultAllocation, loanProduct.getPaymentAllocation().get(0));
    }

    @Test
    public void testUpdateLoanProductOneAllocationIsAdded() {
        // given a loan with one allocation
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        Long loanProductId = createLoanProductFromJson(
                loanProductTestBuilder(customization -> customization.addAdvancedPaymentAllocation(defaultAllocation)));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(1, loanProduct.getPaymentAllocation().size());

        // when a new allocation is added
        updateLoanProduct(loanProductId, updateLoanProductRequest(defaultAllocation, repaymentPaymentAllocation));

        // then it shall be added.
        loanProduct = retrieveLoanProduct(loanProductId);
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
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        Long loanProductId = createLoanProductFromJson(loanProductTestBuilder(
                customization -> customization.addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());

        // when an allocation is removed
        CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                () -> updateLoanProduct(loanProductId, updateLoanProductRequest(repaymentPaymentAllocation)));

        Assertions.assertTrue(callFailedRuntimeException.getMessage()
                .contains("Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided"));
    }

    @Test
    public void testUpdateShouldFailWhenStrategyIsChangedBackButPaymentAllocationsAreNotRemoved() {
        // given a loan with two allocations
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();
        Long loanProductId = createLoanProductFromJson(loanProductTestBuilder(
                customization -> customization.addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());

        // when an allocation is removed
        CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                () -> updateLoanProduct(loanProductId, updateLoanProductRequest("mifos-standard-strategy")));

        Assertions.assertTrue(callFailedRuntimeException.getMessage()
                .contains("In case 'mifos-standard-strategy' payment strategy, payment_allocation must not be provided"));
    }

    @Test
    public void testCreateShouldFailWhenNoAllocationRuleIsProvided() {
        // given
        String loanProduct = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withAccountingRulePeriodicAccrual(new Account[] { ASSET_ACCOUNT, EXPENSE_ACCOUNT, INCOME_ACCOUNT, OVERPAYMENT_ACCOUNT })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(FEE_PENALTY_ACCOUNT).withRepaymentStrategy("advanced-payment-allocation-strategy").build();

        // when
        List<Map<String, String>> loanProductError = getLoanProductError(loanProduct, "errors");
        Assertions.assertEquals("Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided",
                loanProductError.get(0).get("defaultUserMessage"));
    }

    @Test
    public void testCreateShouldFailWhenNoDefaultAllocationIsProvided() {
        // given
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        List<Map<String, String>> loanProductError = getLoanProductError(
                loanProductTestBuilder(customization -> customization.addAdvancedPaymentAllocation(repaymentPaymentAllocation)), "errors");
        Assertions.assertEquals("Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided",
                loanProductError.get(0).get("defaultUserMessage"));
    }

    @Test
    public void testCreateAndReadLoanProductWithAdvancedPaymentAndInterestPaymentWaiverTransaction() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData interestPaymentWaiverAllocation = createInterestPaymentWaiverAllocation();

        // when
        Long loanProductId = createLoanProductFromJson(loanProductTestBuilder(
                customization -> customization.addAdvancedPaymentAllocation(defaultAllocation, interestPaymentWaiverAllocation)));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);

        // then
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());
        Optional<AdvancedPaymentData> first = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(first.isPresent());
        Assertions.assertEquals(defaultAllocation, first.get());

        Optional<AdvancedPaymentData> second = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "INTEREST_PAYMENT_WAIVER".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(second.isPresent());
        Assertions.assertEquals(interestPaymentWaiverAllocation, second.get());
    }

    @Test
    public void testUpdateLoanProductInterestPaymentWaiverAllocationIsAdded() {
        // given a loan with one allocation
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData interestPaymentWaiverAllocation = createInterestPaymentWaiverAllocation();
        Long loanProductId = createLoanProductFromJson(
                loanProductTestBuilder(customization -> customization.addAdvancedPaymentAllocation(defaultAllocation)));
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(1, loanProduct.getPaymentAllocation().size());

        // when a new allocation is added
        updateLoanProduct(loanProductId, updateLoanProductRequest(defaultAllocation, interestPaymentWaiverAllocation));

        // then it shall be added.
        loanProduct = retrieveLoanProduct(loanProductId);
        Assertions.assertNotNull(loanProduct.getPaymentAllocation());
        Assertions.assertEquals(2, loanProduct.getPaymentAllocation().size());
        Optional<AdvancedPaymentData> first = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "DEFAULT".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(first.isPresent());
        Assertions.assertEquals(defaultAllocation, first.get());

        Optional<AdvancedPaymentData> second = loanProduct.getPaymentAllocation().stream()
                .filter(advancedPaymentData -> "INTEREST_PAYMENT_WAIVER".equals(advancedPaymentData.getTransactionType())).findFirst();
        Assertions.assertTrue(second.isPresent());
        Assertions.assertEquals(interestPaymentWaiverAllocation, second.get());
    }

    @Test
    public void testCreateAndReadProgressiveLoanProductWithInterestRefund() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProductRequest = loanProductTestBuilder(
                customization -> customization.addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation));

        Long loanProductId = createLoanProductFromJson(loanProductRequest);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);

        // then
        Assertions.assertNotNull(loanProduct.getSupportedInterestRefundTypes());
        Assertions.assertEquals(0, loanProduct.getSupportedInterestRefundTypes().size());

        // when a new interest refund transaction was added
        updateLoanProduct(loanProductId,
                new PutLoanProductsProductIdRequest().supportedInterestRefundTypes(List.of("MERCHANT_ISSUED_REFUND")));

        loanProduct = retrieveLoanProduct(loanProductId);

        // then
        Assertions.assertNotNull(loanProduct.getSupportedInterestRefundTypes());
        Assertions.assertEquals(1, loanProduct.getSupportedInterestRefundTypes().size());
        Assertions.assertEquals("MERCHANT_ISSUED_REFUND", loanProduct.getSupportedInterestRefundTypes().get(0).getId());

        // Set both of them at creation
        String loanProductRequest2 = loanProductTestBuilder(
                customization -> customization.addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                        .withSupportedInterestRefundTypes("PAYOUT_REFUND", "MERCHANT_ISSUED_REFUND"));
        Long loanProductId2 = createLoanProductFromJson(loanProductRequest2);
        Assertions.assertNotNull(loanProductId2);
        GetLoanProductsProductIdResponse loanProduct2 = retrieveLoanProduct(loanProductId2);

        // then
        Assertions.assertNotNull(loanProduct2.getSupportedInterestRefundTypes());
        Assertions.assertEquals(2, loanProduct2.getSupportedInterestRefundTypes().size());
        Assertions.assertEquals("PAYOUT_REFUND", loanProduct2.getSupportedInterestRefundTypes().get(0).getId());
        Assertions.assertEquals("MERCHANT_ISSUED_REFUND", loanProduct2.getSupportedInterestRefundTypes().get(1).getId());

        // Remove the previously configured transactions
        updateLoanProduct(loanProductId2, new PutLoanProductsProductIdRequest().supportedInterestRefundTypes(List.of()));
        loanProduct2 = retrieveLoanProduct(loanProductId2);
        // then
        Assertions.assertNotNull(loanProduct2.getSupportedInterestRefundTypes());
        Assertions.assertEquals(0, loanProduct2.getSupportedInterestRefundTypes().size());
    }

    @Test
    public void testCreateCumulativeLoanProductWithInterestRefund() {
        // given
        // when
        String loanProductRequest = loanProductTestBuilder(customization -> customization.withSupportedInterestRefundTypes("PAYOUT_REFUND")
                .withLoanScheduleType(LoanScheduleType.CUMULATIVE).withRepaymentStrategy("mifos-standard-strategy"));
        List<Map<String, String>> loanProductError = getLoanProductError(loanProductRequest, "errors");
        Assertions.assertEquals(
                "validation.msg.loanproduct.supportedInterestRefundTypes.supported.only.for.progressive.loan.schedule.handling",
                loanProductError.get(0).get("userMessageGlobalisationCode"));
    }

    @Test
    public void testCreateShouldFailWhenNoNumberOfRepaymentsIsProvided() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProduct = loanProductTestBuilder(customization -> customization
                .addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation).withPrincipal("15,000.00")
                .withNumberOfRepayments(null).withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withAccountingRulePeriodicAccrual(new Account[] { ASSET_ACCOUNT, EXPENSE_ACCOUNT, INCOME_ACCOUNT, OVERPAYMENT_ACCOUNT })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(FEE_PENALTY_ACCOUNT).build());

        // when
        List<Map<String, String>> loanProductError = getLoanProductError(loanProduct, "errors");
        Assertions.assertEquals("The parameter  numberOfRepayments  is mandatory.",
                loanProductError.get(0).get("defaultUserMessage").replace('`', ' '));
    }

    @Test
    public void testCreateShouldFailWhenNoInterestRateIsProvided() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProduct = loanProductTestBuilder(customization -> customization
                .addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation).withPrincipal("15,000.00")
                .withNumberOfRepayments("4").withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod(null)
                .withAccountingRulePeriodicAccrual(new Account[] { ASSET_ACCOUNT, EXPENSE_ACCOUNT, INCOME_ACCOUNT, OVERPAYMENT_ACCOUNT })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(FEE_PENALTY_ACCOUNT).build());

        // when
        List<Map<String, String>> loanProductError = getLoanProductError(loanProduct, "errors");
        Assertions.assertEquals("The parameter  interestRatePerPeriod  is mandatory.",
                loanProductError.get(0).get("defaultUserMessage").replace('`', ' '));
    }

    @Test
    public void testCreateAndReadProgressiveLoanProductWithChargeOffBehaviour() {
        // given
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocationRule();
        AdvancedPaymentData repaymentPaymentAllocation = createRepaymentPaymentAllocation();

        // when
        String loanProductRequest = loanProductTestBuilder(
                customization -> customization.addAdvancedPaymentAllocation(defaultAllocation, repaymentPaymentAllocation)
                        .withChargeOffBehaviour(LoanChargeOffBehaviour.ZERO_INTEREST));

        Long loanProductId = createLoanProductFromJson(loanProductRequest);
        Assertions.assertNotNull(loanProductId);
        GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);

        // then
        Assertions.assertNotNull(loanProduct.getChargeOffBehaviour());
        Assertions.assertEquals(LoanChargeOffBehaviour.ZERO_INTEREST.name(), loanProduct.getChargeOffBehaviour().getId());

        // when a new interest refund transaction was added
        updateLoanProduct(loanProductId, new PutLoanProductsProductIdRequest().chargeOffBehaviour(LoanChargeOffBehaviour.REGULAR.name()));

        loanProduct = retrieveLoanProduct(loanProductId);

        // then
        Assertions.assertNotNull(loanProduct.getChargeOffBehaviour());
        Assertions.assertEquals(LoanChargeOffBehaviour.REGULAR.name(), loanProduct.getChargeOffBehaviour().getId());
    }

    @Test
    public void testCreateCumulativeLoanProductWithChargeOff() {
        // given
        // when
        String loanProductRequest = loanProductTestBuilder(
                customization -> customization.withChargeOffBehaviour(LoanChargeOffBehaviour.ZERO_INTEREST)
                        .withLoanScheduleType(LoanScheduleType.CUMULATIVE).withRepaymentStrategy("mifos-standard-strategy"));
        List<Map<String, String>> loanProductError = getLoanProductError(loanProductRequest, "errors");
        Assertions.assertEquals("validation.msg.loanproduct.chargeOffBehaviour.supported.only.for.progressive.loan.charge.off.behaviour",
                loanProductError.get(0).get("userMessageGlobalisationCode"));
    }

    private String loanProductTestBuilder(Consumer<LoanProductTestBuilder> customization) {
        LoanProductTestBuilder builder = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withAccountingRulePeriodicAccrual(new Account[] { ASSET_ACCOUNT, EXPENSE_ACCOUNT, INCOME_ACCOUNT, OVERPAYMENT_ACCOUNT })
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withFeeAndPenaltyAssetAccount(FEE_PENALTY_ACCOUNT).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL);
        customization.accept(builder);
        return builder.build();
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

    private AdvancedPaymentData createInterestPaymentWaiverAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("INTEREST_PAYMENT_WAIVER");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_FEE,
                PaymentAllocationType.PAST_DUE_PENALTY, PaymentAllocationType.PAST_DUE_INTEREST, PaymentAllocationType.PAST_DUE_PRINCIPAL,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_INTEREST,
                PaymentAllocationType.DUE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);
        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private AdvancedPaymentData createDefaultPaymentAllocationRule() {
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
