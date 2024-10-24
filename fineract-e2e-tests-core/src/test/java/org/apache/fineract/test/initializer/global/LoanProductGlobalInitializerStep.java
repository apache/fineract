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
package org.apache.fineract.test.initializer.global;

import static org.apache.fineract.test.data.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
import org.apache.fineract.client.models.LoanProductPaymentAllocationRule;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.services.LoanProductsApi;
import org.apache.fineract.test.data.AdvancePaymentsAdjustmentType;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.DaysInMonthType;
import org.apache.fineract.test.data.DaysInYearType;
import org.apache.fineract.test.data.InterestCalculationPeriodTime;
import org.apache.fineract.test.data.RecalculationRestFrequencyType;
import org.apache.fineract.test.data.TransactionProcessingStrategyCode;
import org.apache.fineract.test.data.loanproduct.DefaultLoanProduct;
import org.apache.fineract.test.factory.LoanProductsRequestFactory;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@RequiredArgsConstructor
@Component
public class LoanProductGlobalInitializerStep implements FineractGlobalInitializerStep {

    private final LoanProductsApi loanProductsApi;
    private final LoanProductsRequestFactory loanProductsRequestFactory;

    @Override
    public void initialize() throws Exception {
        // LP1
        String name = DefaultLoanProduct.LP1.getName();
        PostLoanProductsRequest loanProductsRequest = loanProductsRequestFactory.defaultLoanProductsRequestLP1().name(name);
        Response<PostLoanProductsResponse> response = loanProductsApi.createLoanProduct(loanProductsRequest).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1, response);

        // LP1 product with due date and overdue date for repayment in config
        // (LP1_DUE_DATE)
        PostLoanProductsRequest loanProductsRequestDueDate = loanProductsRequestFactory.defaultLoanProductsRequestLP1()//
                .name(DefaultLoanProduct.LP1_DUE_DATE.getName())//
                .dueDaysForRepaymentEvent(3)//
                .overDueDaysForRepaymentEvent(3);//
        Response<PostLoanProductsResponse> responseDueDate = loanProductsApi.createLoanProduct(loanProductsRequestDueDate).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_DUE_DATE, responseDueDate);

        // LP1 with 12% FLAT interest
        // (LP1_INTEREST_FLAT)
        String name2 = DefaultLoanProduct.LP1_INTEREST_FLAT.getName();
        PostLoanProductsRequest loanProductsRequestInterestFlat = loanProductsRequestFactory.defaultLoanProductsRequestLP1InterestFlat()
                .name(name2);
        Response<PostLoanProductsResponse> responseInterestFlat = loanProductsApi.createLoanProduct(loanProductsRequestInterestFlat)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT, responseInterestFlat);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Same as payment period
        // (LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT)
        String name3 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningPeriodSameAsPayment = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDeclining().name(name3);
        Response<PostLoanProductsResponse> responseInterestDecliningPeriodSameAsPayment = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningPeriodSameAsPayment).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_PERIOD_SAME_AS_PAYMENT,
                responseInterestDecliningPeriodSameAsPayment);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily
        // (LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY)
        String name4 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningPeriodDaily = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDeclining().name(name4)
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value).allowPartialPeriodInterestCalcualtion(false);
        Response<PostLoanProductsResponse> responseInterestDecliningPeriodDaily = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningPeriodDaily).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_PERIOD_DAILY,
                responseInterestDecliningPeriodDaily);

        // LP1-1MONTH with 12% DECLINING BALANCE interest, interest period: Daily, Interest recalculation-Monthly,
        // Compounding:Interest
        // (LP1_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY)
        String name5 = DefaultLoanProduct.LP1_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingMonthly = loanProductsRequestFactory
                .defaultLoanProductsRequestLP11MonthInterestDecliningBalanceDailyRecalculationCompoundingMonthly().name(name5);
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationCompoundingMonthly = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingMonthly).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY,
                responseInterestDecliningBalanceDailyRecalculationCompoundingMonthly);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none
        // (LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE)
        String name6 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNone = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone().name(name6);
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationCompoundingNone = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNone).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE,
                responseInterestDecliningBalanceDailyRecalculationCompoundingNone);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none, rescheduleStrategyMethod:Reduce number of installments
        // (LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_REDUCE_NR_INST)
        String name7 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_REDUCE_NR_INST
                .getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name7)//
                .rescheduleStrategyMethod(AdvancePaymentsAdjustmentType.REDUCE_NUMBER_OF_INSTALLMENTS.value);//
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments = loanProductsApi
                .createLoanProduct(
                        loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments)
                .execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_REDUCE_NR_INSTALLMENTS,
                responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none, rescheduleStrategyMethod:Reschedule next repayments
        // (LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_RESCH_NEXT_REP)
        String name8 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_RESCH_NEXT_REP
                .getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name8)//
                .rescheduleStrategyMethod(AdvancePaymentsAdjustmentType.RESCHEDULE_NEXT_REPAYMENTS.value);//
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments = loanProductsApi
                .createLoanProduct(
                        loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments)
                .execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_NEXT_REPAYMENTS,
                responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none, Interest Recalculation Frequency: Same as Repayment Period
        // (LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE)
        String name9 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name9)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.SAME_AS_REPAYMENT.value);//
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE,
                responseInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none, Interest Recalculation Frequency: Same as Repayment Period,
        // Multi-disbursement
        // (LP1_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTIDISB)
        String name10 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTIDISB
                .getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceSaRRecalculationSameAsRepaymentCompoundingNoneMultiDisbursement = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name10)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.SAME_AS_REPAYMENT.value)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceSaRRecalculationSameAsRepaymentCompoundingNoneMultiDisbursement = loanProductsApi
                .createLoanProduct(
                        loanProductsRequestInterestDecliningBalanceSaRRecalculationSameAsRepaymentCompoundingNoneMultiDisbursement)
                .execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTI_DISBURSEMENT,
                responseInterestDecliningBalanceSaRRecalculationSameAsRepaymentCompoundingNoneMultiDisbursement);

        // LP1 with new due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment
        // strategy
        // (LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE)
        String name11 = DefaultLoanProduct.LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE.getName();
        PostLoanProductsRequest loanProductsRequestDueInAdvance = loanProductsRequestFactory.defaultLoanProductsRequestLP1()//
                .name(name11)//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST.value);//
        Response<PostLoanProductsResponse> responseDueInAdvance = loanProductsApi.createLoanProduct(loanProductsRequestDueInAdvance)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE,
                responseDueInAdvance);

        // LP1 with new due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment
        // strategy and with 12% FLAT interest
        // (LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT)
        String name12 = DefaultLoanProduct.LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT.getName();
        PostLoanProductsRequest loanProductsRequestDueInAdvanceInterestFlat = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestFlat()//
                .name(name12)//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST.value);//
        Response<PostLoanProductsResponse> responseDueInAdvanceInterestFlat = loanProductsApi
                .createLoanProduct(loanProductsRequestDueInAdvanceInterestFlat).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT,
                responseDueInAdvanceInterestFlat);

        // LP1 with new due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment
        // strategy
        // (LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE)
        PostLoanProductsRequest loanProductsRequestDueInAdvance2 = loanProductsRequestFactory.defaultLoanProductsRequestLP1()//
                .name(DefaultLoanProduct.LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE.getName())//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE.value);//
        Response<PostLoanProductsResponse> responseDueInAdvance2 = loanProductsApi.createLoanProduct(loanProductsRequestDueInAdvance2)
                .execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE,
                responseDueInAdvance2);

        // LP1 with new due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment
        // strategy and with 12% FLAT interest
        // (LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT)
        PostLoanProductsRequest loanProductsRequestDueInAdvanceInterestFlat2 = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestFlat()//
                .name(DefaultLoanProduct.LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT.getName())//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE.value);//
        Response<PostLoanProductsResponse> responseDueInAdvanceInterestFlat2 = loanProductsApi
                .createLoanProduct(loanProductsRequestDueInAdvanceInterestFlat2).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT,
                responseDueInAdvanceInterestFlat2);

        // LP1 with 12% FLAT interest with % overdue fee for amount
        // (LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT)
        String name13 = DefaultLoanProduct.LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT.getName();
        List<ChargeData> charges = new ArrayList<>();
        charges.add(new ChargeData().id(ChargeProductType.LOAN_PERCENTAGE_LATE_FEE.value));
        PostLoanProductsRequest loanProductsRequestInterestFlatOverdueFeeAmount = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestFlat()//
                .name(name13)//
                .charges(charges);//
        Response<PostLoanProductsResponse> responseInterestFlatOverdueFeeAmount = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestFlatOverdueFeeAmount).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT,
                responseInterestFlatOverdueFeeAmount);

        // LP1 with 12% FLAT interest with % overdue fee for amount+interest
        // (LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST)
        String name14 = DefaultLoanProduct.LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST.getName();
        List<ChargeData> chargesInterest = new ArrayList<>();
        chargesInterest.add(new ChargeData().id(ChargeProductType.LOAN_PERCENTAGE_LATE_FEE_AMOUNT_PLUS_INTEREST.value));
        PostLoanProductsRequest loanProductsRequestInterestFlatOverdueFeeAmountInterest = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestFlat()//
                .name(name14)//
                .charges(chargesInterest);//
        Response<PostLoanProductsResponse> responseInterestFlatOverdueFeeAmountInterest = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestFlatOverdueFeeAmountInterest).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST,
                responseInterestFlatOverdueFeeAmountInterest);

        // LP2 with Down-payment
        // (LP2_DOWNPAYMENT)
        String name15 = DefaultLoanProduct.LP2_DOWNPAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestDownPayment = loanProductsRequestFactory.defaultLoanProductsRequestLP2()//
                .name(name15)//
                .enableAutoRepaymentForDownPayment(false);//
        Response<PostLoanProductsResponse> responseDownPayment = loanProductsApi.createLoanProduct(loanProductsRequestDownPayment)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT, responseDownPayment);

        // LP2 with Down-payment+autopayment
        // (LP2_DOWNPAYMENT_AUTO)
        String name16 = DefaultLoanProduct.LP2_DOWNPAYMENT_AUTO.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAuto = loanProductsRequestFactory.defaultLoanProductsRequestLP2()
                .name(name16);
        Response<PostLoanProductsResponse> responseDownPaymentAuto = loanProductsApi.createLoanProduct(loanProductsRequestDownPaymentAuto)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_AUTO, responseDownPaymentAuto);

        // LP2 with Down-payment+autopayment + advanced payment allocation
        // (LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION)
        String name17 = DefaultLoanProduct.LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAutoAdvPaymentAllocation = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name17)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestDownPaymentAutoAdvPaymentAllocation = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentAutoAdvPaymentAllocation).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION,
                responseLoanProductsRequestDownPaymentAutoAdvPaymentAllocation);

        // LP2 with Down-payment + advanced payment allocation - no auto downpayment
        // (LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION)
        String name24 = DefaultLoanProduct.LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPaymentAllocation = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name24)//
                .enableAutoRepaymentForDownPayment(false)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestDownPaymentAdvPaymentAllocation = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentAdvPaymentAllocation).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocation);

        // LP2 with Down-payment and interest
        // (LP2_DOWNPAYMENT_INTEREST)
        String name18 = DefaultLoanProduct.LP2_DOWNPAYMENT_INTEREST.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentInterest = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat()//
                .name(name18)//
                .enableAutoRepaymentForDownPayment(false);//
        Response<PostLoanProductsResponse> responseDownPaymentInterest = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentInterest).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_INTEREST, responseDownPaymentInterest);

        // LP2 with Down-payment and interest
        // (LP2_DOWNPAYMENT_INTEREST_AUTO)
        String name19 = DefaultLoanProduct.LP2_DOWNPAYMENT_INTEREST_AUTO.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentInterestAuto = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat().name(name19);
        Response<PostLoanProductsResponse> responseDownPaymentInterestAuto = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentInterestAuto).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_INTEREST_AUTO,
                responseDownPaymentInterestAuto);

        // LP2 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal
        // (LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL)
        String name20 = DefaultLoanProduct.LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name20)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .enableAutoRepaymentForDownPayment(false)//
                .installmentAmountInMultiplesOf(null)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanSchedule = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanSchedule).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanSchedule);

        // LP2 with Down-payment + advanced payment allocation + progressive loan schedule + vertical
        // (LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL)
        String name21 = DefaultLoanProduct.LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleVertical = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name21)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("VERTICAL")//
                .enableAutoRepaymentForDownPayment(false)//
                .installmentAmountInMultiplesOf(null)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleVertical = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleVertical).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleVertical);

        // LP2 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal + installment
        // level delinquency
        // (LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY)
        String name22 = DefaultLoanProduct.LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY
                .getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleInstLvlDelinquency = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name22)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .enableInstallmentLevelDelinquency(true)//
                .enableAutoRepaymentForDownPayment(false)//
                .installmentAmountInMultiplesOf(null)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleInstLvlDelinquency = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleInstLvlDelinquency).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE_INSTALLMENT_LEVEL_DELINQUENCY,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleInstLvlDelinquency);

        // LP2 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal + installment
        // level delinquency + creditAllocation
        // (LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY)
        String name23 = DefaultLoanProduct.LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPmtAllocProgSchedInstLvlDelinquencyCreditAllocation = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name23)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .enableInstallmentLevelDelinquency(true)//
                .enableAutoRepaymentForDownPayment(false)//
                .installmentAmountInMultiplesOf(null)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("PENALTY", "FEE", "INTEREST", "PRINCIPAL"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestDownPaymentAdvPmtAllocProgSchedInstLvlDelinquencyCreditAllocation = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentAdvPmtAllocProgSchedInstLvlDelinquencyCreditAllocation).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION,
                responseLoanProductsRequestDownPaymentAdvPmtAllocProgSchedInstLvlDelinquencyCreditAllocation);

        // LP2 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal + installment
        // level delinquency + creditAllocation + fixed length (90)
        // (LP2_DOWNPAYMENT_ADV_PMT_ALLOC_FIXED_LENGTH)
        String name25 = DefaultLoanProduct.LP2_DOWNPAYMENT_ADV_PMT_ALLOC_FIXED_LENGTH.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPmtAllocFixedLength = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name25)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .enableInstallmentLevelDelinquency(true)//
                .enableAutoRepaymentForDownPayment(false)//
                .installmentAmountInMultiplesOf(null)//
                .fixedLength(90).creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("PENALTY", "FEE", "INTEREST", "PRINCIPAL"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestDownPaymentAdvPmtAllocFixedLength = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentAdvPmtAllocFixedLength).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADV_PMT_ALLOC_FIXED_LENGTH,
                responseLoanProductsRequestDownPaymentAdvPmtAllocFixedLength);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_TILL_PRECLOSE)
        String name38 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_TILL_PRECLOSE.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcTillPreclose = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name38)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcTillPreClose = loanProductsApi
                .createLoanProduct(loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcTillPreclose).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_TILL_PRECLOSE,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcTillPreClose);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till rest frequency,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_TILL_REST_FREQUENCY)
        String name39 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_TILL_REST_FREQUENCY.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcTillRestFrequency = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name39)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(2)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcTillRestFrequency = loanProductsApi
                .createLoanProduct(loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcTillRestFrequency).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_TILL_REST_FREQUENCY,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcTillRestFrequency);

        String name40 = DefaultLoanProduct.LP2_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvPmtAllocProgressiveLoanScheduleHorizontal = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2NoDown()//
                .name(name40)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLP2AdvPmtAllocProgressiveLoanScheduleHorizontal = loanProductsApi
                .createLoanProduct(loanProductsRequestLP2AdvPmtAllocProgressiveLoanScheduleHorizontal).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL,
                responseLP2AdvPmtAllocProgressiveLoanScheduleHorizontal);

        // LP2 with Down-payment + advanced custom payment allocation + progressive loan schedule + horizontal
        // (LP2_DOWNPAYMENT_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL)
        String name44 = DefaultLoanProduct.LP2_ADV_CUST_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL.getName();

        PostLoanProductsRequest loanProductsRequestAdvCustomPaymentAllocationProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculationWithDownPayment()//
                .name(name44)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        Response<PostLoanProductsResponse> responseLoanProductsRequestAdvCustomPaymentAllocationProgressiveLoanSchedule = loanProductsApi
                .createLoanProduct(loanProductsRequestAdvCustomPaymentAllocationProgressiveLoanSchedule).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADVANCED_CUSTOM_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE,
                responseLoanProductsRequestAdvCustomPaymentAllocationProgressiveLoanSchedule);
    }

    public static AdvancedPaymentData createPaymentAllocation(String transactionType, String futureInstallmentAllocationRule,
            LoanProductPaymentAllocationRule.AllocationTypesEnum... rules) {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType(transactionType);
        advancedPaymentData.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);

        List<PaymentAllocationOrder> paymentAllocationOrders;
        if (rules.length == 0) {
            paymentAllocationOrders = getPaymentAllocationOrder(//
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST);//
        } else {
            paymentAllocationOrders = getPaymentAllocationOrder(rules);
        }

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);

        return advancedPaymentData;
    }

    private static CreditAllocationData createCreditAllocation(String transactionType, List<String> creditAllocationRules) {
        CreditAllocationData creditAllocationData = new CreditAllocationData();
        creditAllocationData.setTransactionType(transactionType);

        List<CreditAllocationOrder> creditAllocationOrders = new ArrayList<>();
        for (int i = 0; i < creditAllocationRules.size(); i++) {
            CreditAllocationOrder e = new CreditAllocationOrder();
            e.setOrder(i + 1);
            e.setCreditAllocationRule(creditAllocationRules.get(i));
            creditAllocationOrders.add(e);
        }

        creditAllocationData.setCreditAllocationOrder(creditAllocationOrders);
        return creditAllocationData;
    }

    private static List<PaymentAllocationOrder> getPaymentAllocationOrder(
            LoanProductPaymentAllocationRule.AllocationTypesEnum... paymentAllocations) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocations).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }
}
