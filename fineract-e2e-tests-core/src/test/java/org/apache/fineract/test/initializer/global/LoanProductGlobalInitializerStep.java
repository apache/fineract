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
        // PIN30
        String name = DefaultLoanProduct.PIN30.getName();
        PostLoanProductsRequest loanProductsRequest = loanProductsRequestFactory.defaultLoanProductsRequestPin30().name(name);
        Response<PostLoanProductsResponse> response = loanProductsApi.createLoanProduct(loanProductsRequest).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30, response);

        // PIN30 product with due date and overdue date for repayment in config
        // (PIN30_DUE_DATE)
        PostLoanProductsRequest loanProductsRequestDueDate = loanProductsRequestFactory.defaultLoanProductsRequestPin30()//
                .name(DefaultLoanProduct.PIN30_DUE_DATE.getName())//
                .dueDaysForRepaymentEvent(3)//
                .overDueDaysForRepaymentEvent(3);//
        Response<PostLoanProductsResponse> responseDueDate = loanProductsApi.createLoanProduct(loanProductsRequestDueDate).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_DUE_DATE, responseDueDate);

        // PIN30 with 12% FLAT interest
        // (PIN30_INTEREST_FLAT)
        String name2 = DefaultLoanProduct.PIN30_INTEREST_FLAT.getName();
        PostLoanProductsRequest loanProductsRequestInterestFlat = loanProductsRequestFactory.defaultLoanProductsRequestPin30InterestFlat()
                .name(name2);
        Response<PostLoanProductsResponse> responseInterestFlat = loanProductsApi.createLoanProduct(loanProductsRequestInterestFlat)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_FLAT, responseInterestFlat);

        // PIN30 with 12% DECLINING BALANCE interest, interest period: Same as payment period
        // (PIN30_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT)
        String name3 = DefaultLoanProduct.PIN30_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningPeriodSameAsPayment = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestDeclining().name(name3);
        Response<PostLoanProductsResponse> responseInterestDecliningPeriodSameAsPayment = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningPeriodSameAsPayment).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_DECLINING_PERIOD_SAME_AS_PAYMENT,
                responseInterestDecliningPeriodSameAsPayment);

        // PIN30 with 12% DECLINING BALANCE interest, interest period: Daily
        // (PIN30_INTEREST_DECLINING_BALANCE_PERIOD_DAILY)
        String name4 = DefaultLoanProduct.PIN30_INTEREST_DECLINING_BALANCE_PERIOD_DAILY.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningPeriodDaily = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestDeclining().name(name4)
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value).allowPartialPeriodInterestCalcualtion(false);
        Response<PostLoanProductsResponse> responseInterestDecliningPeriodDaily = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningPeriodDaily).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_DECLINING_PERIOD_DAILY,
                responseInterestDecliningPeriodDaily);

        // PIN30-1MONTH with 12% DECLINING BALANCE interest, interest period: Daily, Interest recalculation-Monthly,
        // Compounding:Interest
        // (PIN30_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY)
        String name5 = DefaultLoanProduct.PIN30_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingMonthly = loanProductsRequestFactory
                .defaultLoanProductsRequestPin301MonthInterestDecliningBalanceDailyRecalculationCompoundingMonthly().name(name5);
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationCompoundingMonthly = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingMonthly).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY,
                responseInterestDecliningBalanceDailyRecalculationCompoundingMonthly);

        // PIN30 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none
        // (PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE)
        String name6 = DefaultLoanProduct.PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNone = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestDecliningBalanceDailyRecalculationCompoundingNone().name(name6);
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationCompoundingNone = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNone).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE,
                responseInterestDecliningBalanceDailyRecalculationCompoundingNone);

        // PIN30 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none, rescheduleStrategyMethod:Reduce number of installments
        // (PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_REDUCE_NR_INST)
        String name7 = DefaultLoanProduct.PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_REDUCE_NR_INST
                .getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name7)//
                .rescheduleStrategyMethod(AdvancePaymentsAdjustmentType.REDUCE_NUMBER_OF_INSTALLMENTS.value);//
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments = loanProductsApi
                .createLoanProduct(
                        loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments)
                .execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_REDUCE_NR_INSTALLMENTS,
                responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments);

        // PIN30 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none, rescheduleStrategyMethod:Reschedule next repayments
        // (PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_RESCH_NEXT_REP)
        String name8 = DefaultLoanProduct.PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_RESCH_NEXT_REP
                .getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name8)//
                .rescheduleStrategyMethod(AdvancePaymentsAdjustmentType.RESCHEDULE_NEXT_REPAYMENTS.value);//
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments = loanProductsApi
                .createLoanProduct(
                        loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments)
                .execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_NEXT_REPAYMENTS,
                responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments);

        // PIN30 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none, Interest Recalculation Frequency: Same as Repayment Period
        // (PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE)
        String name9 = DefaultLoanProduct.PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name9)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.SAME_AS_REPAYMENT.value);//
        Response<PostLoanProductsResponse> responseInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE,
                responseInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone);

        // PIN30 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none, Interest Recalculation Frequency: Same as Repayment Period,
        // Multi-disbursement
        // (PIN30_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTIDISB)
        String name10 = DefaultLoanProduct.PIN30_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTIDISB
                .getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceSaRRecalculationSameAsRepaymentCompoundingNoneMultiDisbursement = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestDecliningBalanceDailyRecalculationCompoundingNone()//
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
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTI_DISBURSEMENT,
                responseInterestDecliningBalanceSaRRecalculationSameAsRepaymentCompoundingNoneMultiDisbursement);

        // PIN30 with new due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment
        // strategy
        // (PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE)
        String name11 = DefaultLoanProduct.PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE.getName();
        PostLoanProductsRequest loanProductsRequestDueInAdvance = loanProductsRequestFactory.defaultLoanProductsRequestPin30()//
                .name(name11)//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST.value);//
        Response<PostLoanProductsResponse> responseDueInAdvance = loanProductsApi.createLoanProduct(loanProductsRequestDueInAdvance)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE,
                responseDueInAdvance);

        // PIN30 with new due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment
        // strategy and with 12% FLAT interest
        // (PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT)
        String name12 = DefaultLoanProduct.PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT.getName();
        PostLoanProductsRequest loanProductsRequestDueInAdvanceInterestFlat = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestFlat()//
                .name(name12)//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST.value);//
        Response<PostLoanProductsResponse> responseDueInAdvanceInterestFlat = loanProductsApi
                .createLoanProduct(loanProductsRequestDueInAdvanceInterestFlat).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT,
                responseDueInAdvanceInterestFlat);

        // PIN30 with new due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment
        // strategy
        // (PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE)
        PostLoanProductsRequest loanProductsRequestDueInAdvance2 = loanProductsRequestFactory.defaultLoanProductsRequestPin30()//
                .name(DefaultLoanProduct.PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE.getName())//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE.value);//
        Response<PostLoanProductsResponse> responseDueInAdvance2 = loanProductsApi.createLoanProduct(loanProductsRequestDueInAdvance2)
                .execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE,
                responseDueInAdvance2);

        // PIN30 with new due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment
        // strategy and with 12% FLAT interest
        // (PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT)
        PostLoanProductsRequest loanProductsRequestDueInAdvanceInterestFlat2 = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestFlat()//
                .name(DefaultLoanProduct.PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT.getName())//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE.value);//
        Response<PostLoanProductsResponse> responseDueInAdvanceInterestFlat2 = loanProductsApi
                .createLoanProduct(loanProductsRequestDueInAdvanceInterestFlat2).execute();
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT,
                responseDueInAdvanceInterestFlat2);

        // PIN30 with 12% FLAT interest with % overdue fee for amount
        // (PIN30_INTEREST_FLAT_OVERDUE_FROM_AMOUNT)
        String name13 = DefaultLoanProduct.PIN30_INTEREST_FLAT_OVERDUE_FROM_AMOUNT.getName();
        List<ChargeData> charges = new ArrayList<>();
        charges.add(new ChargeData().id(ChargeProductType.LOAN_PERCENTAGE_LATE_FEE.value));
        PostLoanProductsRequest loanProductsRequestInterestFlatOverdueFeeAmount = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestFlat()//
                .name(name13)//
                .charges(charges);//
        Response<PostLoanProductsResponse> responseInterestFlatOverdueFeeAmount = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestFlatOverdueFeeAmount).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_FLAT_OVERDUE_FROM_AMOUNT,
                responseInterestFlatOverdueFeeAmount);

        // PIN30 with 12% FLAT interest with % overdue fee for amount+interest
        // (PIN30_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST)
        String name14 = DefaultLoanProduct.PIN30_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST.getName();
        List<ChargeData> chargesInterest = new ArrayList<>();
        chargesInterest.add(new ChargeData().id(ChargeProductType.LOAN_PERCENTAGE_LATE_FEE_AMOUNT_PLUS_INTEREST.value));
        PostLoanProductsRequest loanProductsRequestInterestFlatOverdueFeeAmountInterest = loanProductsRequestFactory
                .defaultLoanProductsRequestPin30InterestFlat()//
                .name(name14)//
                .charges(chargesInterest);//
        Response<PostLoanProductsResponse> responseInterestFlatOverdueFeeAmountInterest = loanProductsApi
                .createLoanProduct(loanProductsRequestInterestFlatOverdueFeeAmountInterest).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN30_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST,
                responseInterestFlatOverdueFeeAmountInterest);

        // PIN4 with Down-payment
        // (PIN4_DOWNPAYMENT)
        String name15 = DefaultLoanProduct.PIN4_DOWNPAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestDownPayment = loanProductsRequestFactory.defaultLoanProductsRequestPin4()//
                .name(name15)//
                .enableAutoRepaymentForDownPayment(false);//
        Response<PostLoanProductsResponse> responseDownPayment = loanProductsApi.createLoanProduct(loanProductsRequestDownPayment)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT, responseDownPayment);

        // PIN4 with Down-payment+autopayment
        // (PIN4_DOWNPAYMENT_AUTO)
        String name16 = DefaultLoanProduct.PIN4_DOWNPAYMENT_AUTO.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAuto = loanProductsRequestFactory.defaultLoanProductsRequestPin4()
                .name(name16);
        Response<PostLoanProductsResponse> responseDownPaymentAuto = loanProductsApi.createLoanProduct(loanProductsRequestDownPaymentAuto)
                .execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_AUTO, responseDownPaymentAuto);

        // PIN4 with Down-payment+autopayment + advanced payment allocation
        // (PIN4_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION)
        String name17 = DefaultLoanProduct.PIN4_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAutoAdvPaymentAllocation = loanProductsRequestFactory
                .defaultLoanProductsRequestPin4()//
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
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION,
                responseLoanProductsRequestDownPaymentAutoAdvPaymentAllocation);

        // PIN4 with Down-payment + advanced payment allocation - no auto downpayment
        // (PIN4_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION)
        String name24 = DefaultLoanProduct.PIN4_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPaymentAllocation = loanProductsRequestFactory
                .defaultLoanProductsRequestPin4()//
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
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocation);

        // PIN4 with Down-payment and interest
        // (PIN4_DOWNPAYMENT_INTEREST)
        String name18 = DefaultLoanProduct.PIN4_DOWNPAYMENT_INTEREST.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentInterest = loanProductsRequestFactory
                .defaultLoanProductsRequestPin4InterestFlat()//
                .name(name18)//
                .enableAutoRepaymentForDownPayment(false);//
        Response<PostLoanProductsResponse> responseDownPaymentInterest = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentInterest).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_INTEREST,
                responseDownPaymentInterest);

        // PIN4 with Down-payment and interest
        // (PIN4_DOWNPAYMENT_INTEREST_AUTO)
        String name19 = DefaultLoanProduct.PIN4_DOWNPAYMENT_INTEREST_AUTO.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentInterestAuto = loanProductsRequestFactory
                .defaultLoanProductsRequestPin4InterestFlat().name(name19);
        Response<PostLoanProductsResponse> responseDownPaymentInterestAuto = loanProductsApi
                .createLoanProduct(loanProductsRequestDownPaymentInterestAuto).execute();
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_INTEREST_AUTO,
                responseDownPaymentInterestAuto);

        // PIN4 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal
        // (PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL)
        String name20 = DefaultLoanProduct.PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestPin4()//
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
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanSchedule);

        // PIN4 with Down-payment + advanced payment allocation + progressive loan schedule + vertical
        // (PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL)
        String name21 = DefaultLoanProduct.PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleVertical = loanProductsRequestFactory
                .defaultLoanProductsRequestPin4()//
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
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleVertical);

        // PIN4 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal + installment
        // level delinquency
        // (PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY)
        String name22 = DefaultLoanProduct.PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY
                .getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleInstLvlDelinquency = loanProductsRequestFactory
                .defaultLoanProductsRequestPin4()//
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
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE_INSTALLMENT_LEVEL_DELINQUENCY,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleInstLvlDelinquency);

        // PIN4 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal + installment
        // level delinquency + creditAllocation
        // (PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_INSTALLMENT_LEVEL_DELINQUENCY)
        String name23 = DefaultLoanProduct.PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION
                .getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPmtAllocProgSchedInstLvlDelinquencyCreditAllocation = loanProductsRequestFactory
                .defaultLoanProductsRequestPin4()//
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
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION,
                responseLoanProductsRequestDownPaymentAdvPmtAllocProgSchedInstLvlDelinquencyCreditAllocation);

        // PIN4 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal + installment
        // level delinquency + creditAllocation + fixed length (90)
        // (PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_FIXED_LENGTH)
        String name25 = DefaultLoanProduct.PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_FIXED_LENGTH.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAdvPmtAllocFixedLength = loanProductsRequestFactory
                .defaultLoanProductsRequestPin4()//
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
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_PIN4_DOWNPAYMENT_ADV_PMT_ALLOC_FIXED_LENGTH,
                responseLoanProductsRequestDownPaymentAdvPmtAllocFixedLength);
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
