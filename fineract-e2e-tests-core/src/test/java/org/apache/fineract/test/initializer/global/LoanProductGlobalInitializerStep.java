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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.apache.fineract.client.models.LoanProductRelatedDetail.DaysInYearCustomStrategyEnum.FEB_29_PERIOD_ONLY;
import static org.apache.fineract.test.data.ChargeOffBehaviour.ZERO_INTEREST;
import static org.apache.fineract.test.data.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.INTEREST_RATE_FREQUENCY_TYPE_MONTH;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.INTEREST_RATE_FREQUENCY_TYPE_WHOLE_TERM;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.INTEREST_RATE_FREQUENCY_TYPE_YEAR;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.INTEREST_TYPE_FLAT;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.LOAN_ACCOUNTING_RULE_NONE;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.REPAYMENT_FREQUENCY_TYPE_MONTHS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
import org.apache.fineract.client.models.GetLoanProductsResponse;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.LoanProductPaymentAllocationRule;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostClassificationToIncomeAccountMappings;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostWriteOffReasonToExpenseAccountMappings;
import org.apache.fineract.test.data.AdvancePaymentsAdjustmentType;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.DaysInMonthType;
import org.apache.fineract.test.data.DaysInYearType;
import org.apache.fineract.test.data.InterestCalculationPeriodTime;
import org.apache.fineract.test.data.InterestRecalculationCompoundingMethod;
import org.apache.fineract.test.data.InterestType;
import org.apache.fineract.test.data.OverAppliedCalculationType;
import org.apache.fineract.test.data.PreClosureInterestCalculationRule;
import org.apache.fineract.test.data.RecalculationRestFrequencyType;
import org.apache.fineract.test.data.TransactionProcessingStrategyCode;
import org.apache.fineract.test.data.codevalue.CodeValue;
import org.apache.fineract.test.data.codevalue.CodeValueResolver;
import org.apache.fineract.test.data.codevalue.DefaultCodeValue;
import org.apache.fineract.test.data.loanproduct.DefaultLoanProduct;
import org.apache.fineract.test.factory.LoanProductsRequestFactory;
import org.apache.fineract.test.helper.CodeHelper;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoanProductGlobalInitializerStep implements FineractGlobalInitializerStep {

    private final FineractFeignClient fineractClient;
    private final LoanProductsRequestFactory loanProductsRequestFactory;
    private final CodeHelper codeHelper;
    private final CodeValueResolver codeValueResolver;

    @Override
    public void initialize() throws Exception {
        // LP1
        String name = DefaultLoanProduct.LP1.getName();
        PostLoanProductsRequest loanProductsRequest = loanProductsRequestFactory.defaultLoanProductsRequestLP1().name(name);
        PostLoanProductsResponse response = createLoanProductIdempotent(loanProductsRequest);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1, response);

        // LP1 product with due date and overdue date for repayment in config
        // (LP1_DUE_DATE)
        PostLoanProductsRequest loanProductsRequestDueDate = loanProductsRequestFactory.defaultLoanProductsRequestLP1()//
                .name(DefaultLoanProduct.LP1_DUE_DATE.getName())//
                .dueDaysForRepaymentEvent(3)//
                .overDueDaysForRepaymentEvent(3);//
        PostLoanProductsResponse responseDueDate = createLoanProductIdempotent(loanProductsRequestDueDate);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_DUE_DATE, responseDueDate);

        // LP1 with 12% FLAT interest
        // (LP1_INTEREST_FLAT)
        String name2 = DefaultLoanProduct.LP1_INTEREST_FLAT.getName();
        PostLoanProductsRequest loanProductsRequestInterestFlat = loanProductsRequestFactory.defaultLoanProductsRequestLP1InterestFlat()
                .name(name2);
        PostLoanProductsResponse responseInterestFlat = createLoanProductIdempotent(loanProductsRequestInterestFlat);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT, responseInterestFlat);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Same as payment period
        // (LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT)
        String name3 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_PERIOD_SAME_AS_PAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningPeriodSameAsPayment = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDeclining().name(name3);
        PostLoanProductsResponse responseInterestDecliningPeriodSameAsPayment = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningPeriodSameAsPayment);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_PERIOD_SAME_AS_PAYMENT,
                responseInterestDecliningPeriodSameAsPayment);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily
        // (LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY)
        String name4 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningPeriodDaily = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDeclining().name(name4)
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value).allowPartialPeriodInterestCalcualtion(false);
        PostLoanProductsResponse responseInterestDecliningPeriodDaily = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningPeriodDaily);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_PERIOD_DAILY,
                responseInterestDecliningPeriodDaily);

        // LP1-1MONTH with 12% DECLINING BALANCE interest, interest period: Daily, Interest recalculation-Monthly,
        // Compounding:Interest
        // (LP1_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY)
        String name5 = DefaultLoanProduct.LP1_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingMonthly = loanProductsRequestFactory
                .defaultLoanProductsRequestLP11MonthInterestDecliningBalanceDailyRecalculationCompoundingMonthly().name(name5);
        PostLoanProductsResponse responseInterestDecliningBalanceDailyRecalculationCompoundingMonthly = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingMonthly);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY,
                responseInterestDecliningBalanceDailyRecalculationCompoundingMonthly);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none
        // (LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE)
        String name6 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNone = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone().name(name6);
        PostLoanProductsResponse responseInterestDecliningBalanceDailyRecalculationCompoundingNone = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNone);
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
        PostLoanProductsResponse responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments);
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
        PostLoanProductsResponse responseInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleRescheduleNextRepayments);
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
        PostLoanProductsResponse responseInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone);
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
        PostLoanProductsResponse responseInterestDecliningBalanceSaRRecalculationSameAsRepaymentCompoundingNoneMultiDisbursement = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningBalanceSaRRecalculationSameAsRepaymentCompoundingNoneMultiDisbursement);
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
        PostLoanProductsResponse responseDueInAdvance = createLoanProductIdempotent(loanProductsRequestDueInAdvance);
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
        PostLoanProductsResponse responseDueInAdvanceInterestFlat = createLoanProductIdempotent(
                loanProductsRequestDueInAdvanceInterestFlat);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT,
                responseDueInAdvanceInterestFlat);

        // LP1 with new due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy payment
        // strategy
        // (LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE)
        PostLoanProductsRequest loanProductsRequestDueInAdvance2 = loanProductsRequestFactory.defaultLoanProductsRequestLP1()//
                .name(DefaultLoanProduct.LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE.getName())//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE.value);//
        PostLoanProductsResponse responseDueInAdvance2 = createLoanProductIdempotent(loanProductsRequestDueInAdvance2);
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
        PostLoanProductsResponse responseDueInAdvanceInterestFlat2 = createLoanProductIdempotent(
                loanProductsRequestDueInAdvanceInterestFlat2);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT,
                responseDueInAdvanceInterestFlat2);

        // LP1 with 12% FLAT interest with % overdue fee for amount
        // (LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT)
        String name13 = DefaultLoanProduct.LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT.getName();
        List<LoanProductChargeData> charges = new ArrayList<>();
        charges.add(new LoanProductChargeData().id(ChargeProductType.LOAN_PERCENTAGE_LATE_FEE.value));
        PostLoanProductsRequest loanProductsRequestInterestFlatOverdueFeeAmount = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestFlat()//
                .name(name13)//
                .charges(charges);//
        PostLoanProductsResponse responseInterestFlatOverdueFeeAmount = createLoanProductIdempotent(
                loanProductsRequestInterestFlatOverdueFeeAmount);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT,
                responseInterestFlatOverdueFeeAmount);

        // LP1 with 12% FLAT interest with % overdue fee for amount+interest
        // (LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST)
        String name14 = DefaultLoanProduct.LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST.getName();
        List<LoanProductChargeData> chargesInterest = new ArrayList<>();
        chargesInterest.add(new LoanProductChargeData().id(ChargeProductType.LOAN_PERCENTAGE_LATE_FEE_AMOUNT_PLUS_INTEREST.value));
        PostLoanProductsRequest loanProductsRequestInterestFlatOverdueFeeAmountInterest = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestFlat()//
                .name(name14)//
                .charges(chargesInterest);//
        PostLoanProductsResponse responseInterestFlatOverdueFeeAmountInterest = createLoanProductIdempotent(
                loanProductsRequestInterestFlatOverdueFeeAmountInterest);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST,
                responseInterestFlatOverdueFeeAmountInterest);

        // LP2 with Down-payment
        // (LP2_DOWNPAYMENT)
        String name15 = DefaultLoanProduct.LP2_DOWNPAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestDownPayment = loanProductsRequestFactory.defaultLoanProductsRequestLP2()//
                .name(name15)//
                .enableAutoRepaymentForDownPayment(false);//
        PostLoanProductsResponse responseDownPayment = createLoanProductIdempotent(loanProductsRequestDownPayment);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT, responseDownPayment);

        // LP2 with Down-payment+autopayment
        // (LP2_DOWNPAYMENT_AUTO)
        String name16 = DefaultLoanProduct.LP2_DOWNPAYMENT_AUTO.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAuto = loanProductsRequestFactory.defaultLoanProductsRequestLP2()
                .name(name16);
        PostLoanProductsResponse responseDownPaymentAuto = createLoanProductIdempotent(loanProductsRequestDownPaymentAuto);
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
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAutoAdvPaymentAllocation = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAutoAdvPaymentAllocation);
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
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAdvPaymentAllocation = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAdvPaymentAllocation);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocation);

        // LP2 with Down-payment and interest
        // (LP2_DOWNPAYMENT_INTEREST)
        String name18 = DefaultLoanProduct.LP2_DOWNPAYMENT_INTEREST.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentInterest = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat()//
                .name(name18)//
                .enableAutoRepaymentForDownPayment(false);//
        PostLoanProductsResponse responseDownPaymentInterest = createLoanProductIdempotent(loanProductsRequestDownPaymentInterest);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_INTEREST, responseDownPaymentInterest);

        // LP2 with Down-payment and interest
        // (LP2_DOWNPAYMENT_INTEREST_AUTO)
        String name19 = DefaultLoanProduct.LP2_DOWNPAYMENT_INTEREST_AUTO.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentInterestAuto = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat().name(name19);
        PostLoanProductsResponse responseDownPaymentInterestAuto = createLoanProductIdempotent(loanProductsRequestDownPaymentInterestAuto);
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
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanSchedule = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanSchedule);
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
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleVertical = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleVertical);
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
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleInstLvlDelinquency = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAdvPaymentAllocationProgressiveLoanScheduleInstLvlDelinquency);
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
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAdvPmtAllocProgSchedInstLvlDelinquencyCreditAllocation = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAdvPmtAllocProgSchedInstLvlDelinquencyCreditAllocation);
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
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAdvPmtAllocFixedLength = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAdvPmtAllocFixedLength);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADV_PMT_ALLOC_FIXED_LENGTH,
                responseLoanProductsRequestDownPaymentAdvPmtAllocFixedLength);

        // LP2 with Down-payment+autopayment + advanced payment allocation + repayment start date SUBMITTED ON DATE
        // (LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_REPAYMENT_START_SUBMITTED)
        String name26 = DefaultLoanProduct.LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION_REPAYMENT_START_SUBMITTED.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAutoAdvPaymentAllocationRepaymentStartSubmitted = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name26)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .repaymentStartDateType(2)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAutoAdvPaymentAllocationRepaymentStartSubmitted = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAutoAdvPaymentAllocationRepaymentStartSubmitted);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_AUTO_ADVANCED_REPAYMENT_ALLOCATION_PAYMENT_START_SUBMITTED,
                responseLoanProductsRequestDownPaymentAutoAdvPaymentAllocationRepaymentStartSubmitted);

        // LP2 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal + interest Flat
        // + Multi-disbursement
        // (LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE)
        final String name27 = DefaultLoanProduct.LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE.getName();
        final PostLoanProductsRequest loanProductsRequestDownPaymentAdvPaymentAllocationInterestFlatMultiDisbursement = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat()//
                .name(name27)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .enableAutoRepaymentForDownPayment(false)//
                .installmentAmountInMultiplesOf(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAdvPaymentAllocationInterestFlatMultiDisbursement = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAdvPaymentAllocationInterestFlatMultiDisbursement);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE,
                responseLoanProductsRequestDownPaymentAdvPaymentAllocationInterestFlatMultiDisbursement);

        // LP2 with progressive loan schedule + horizontal + interest EMI + actual/actual
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL)
        String name28 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmiActualActual = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name28)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActual = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmiActualActual);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActual);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30)
        String name29 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030 = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name29)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030 = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + multidisbursement
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE)
        String name36 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030MultiDisburse = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name36)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030MultiDisburse = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030MultiDisburse);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030MultiDisburse);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + multidisbursement + downpayment
        // 25%, auto disabled
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT)
        String name37 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030MultiDisburseDownPayment = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name37)//
                .enableDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030MultiDisburseDownPayment = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030MultiDisburseDownPayment);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030MultiDisburseDownPayment);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 365/Actual
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_365_ACTUAL)
        String name30 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_365_ACTUAL.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterest365Actual = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name30)//
                .daysInYearType(DaysInYearType.DAYS365.value)//
                .daysInMonthType(DaysInMonthType.ACTUAL.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi365Actual = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterest365Actual);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_365_ACTUAL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi365Actual);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + downpayment 25%
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_DOWNPAYMENT)
        String name31 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_DOWNPAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterest36030Downpayment = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name31)//
                .enableDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030Downpayment = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterest36030Downpayment);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_DOWNPAYMENT,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030Downpayment);

        // LP2 with progressive loan schedule + horizontal + interest EMI + actual/actual +
        // enableAccrualActivityPosting
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY)
        String name32 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualAccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name32)//
                .enableAccrualActivityPosting(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualAccrualActivity = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualAccrualActivity);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualAccrualActivity);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily + enableAccrualActivityPosting
        // (LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY)
        String name33 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_ACCRUAL_ACTIVITY.getName();
        PostLoanProductsRequest loanProductsRequestInterestDecliningPeriodDailyAccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDeclining()//
                .name(name33)//
                .enableAccrualActivityPosting(true)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value)//
                .allowPartialPeriodInterestCalcualtion(false);//
        PostLoanProductsResponse responseInterestDecliningPeriodDailyAccrualActivity = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningPeriodDailyAccrualActivity);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_PERIOD_DAILY_ACCRUAL_ACTIVITY,
                responseInterestDecliningPeriodDailyAccrualActivity);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest
        // recalculation-Daily, Compounding:none + enableAccrualActivityPosting
        // (LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_ACCRUAL_ACTIVITY)
        String name34 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_ACCRUAL_ACTIVITY.getName();
        PostLoanProductsRequest loanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNoneAccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name34)//
                .enableAccrualActivityPosting(true)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value)//
                .allowPartialPeriodInterestCalcualtion(false);//
        PostLoanProductsResponse responseLP1InterestDecliningBalanceDailyRecalculationCompoundingNoneAccrualActivity = createLoanProductIdempotent(
                loanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNoneAccrualActivity);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_ACCRUAL_ACTIVITY,
                responseLP1InterestDecliningBalanceDailyRecalculationCompoundingNoneAccrualActivity);

        // LP2 with progressive loan schedule + horizontal + interest EMI + actual/actual + interest refund with
        // Merchant issued and Payment refund
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND)
        String name35 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND.getName();
        List<String> supportedInterestRefundTypes = Arrays.asList("MERCHANT_ISSUED_REFUND", "PAYOUT_REFUND");
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefund = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name35)//
                .supportedInterestRefundTypes(supportedInterestRefundTypes).paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("INTEREST_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefund = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefund);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefund);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE)
        String name38 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPreclose = loanProductsRequestFactory
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
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcDailyTillPreCloese = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPreclose);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcDailyTillPreCloese);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till rest frequency date,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_REST_FREQUENCY_DATE)
        String name39 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_REST_FREQUENCY_DATE
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillRestFrequencyDate = loanProductsRequestFactory
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
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcDailyTillRestFrequencyDate = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillRestFrequencyDate);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_REST_FREQUENCY_DATE,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcDailyTillRestFrequencyDate);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Same as repayment period, Frequency Interval for
        // recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_SARP_TILL_PRECLOSE)
        String name40 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_SARP_TILL_PRECLOSE.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcSameAsRepTillPreclose = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name40)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(1)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcSameAsRepTillPreCloese = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcSameAsRepTillPreclose);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_SAME_AS_REP_TILL_PRECLOSE,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcSameAsRepTillPreCloese);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till rest frequency date,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Same as repayment period, Frequency Interval for
        // recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_SARP_TILL_REST_FREQUENCY_DATE)
        String name41 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_SARP_TILL_REST_FREQUENCY_DATE
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcSameAsRepTillRestFrequencyDate = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name41)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(2)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(1)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcSameAsRepTillRestFrequencyDate = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcSameAsRepTillRestFrequencyDate);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_SAME_AS_REP_TILL_REST_FREQUENCY_DATE,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcSameAsRepTillRestFrequencyDate);

        // LP1 advanced payment allocation + progressive loan schedule + horizontal
        // (LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL)
        String name42 = DefaultLoanProduct.LP1_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL.getName();
        PostLoanProductsRequest loanProductsRequestLP1AdvPmtAllocProgressiveLoanScheduleHorizontal = loanProductsRequestFactory//
                .defaultLoanProductsRequestLP1()//
                .name(name42)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLP1AdvPmtAllocProgressiveLoanScheduleHorizontal = createLoanProductIdempotent(
                loanProductsRequestLP1AdvPmtAllocProgressiveLoanScheduleHorizontal);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL,
                responseLP1AdvPmtAllocProgressiveLoanScheduleHorizontal);

        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // Frequency for Interest rate - Whole Year
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_WHOLE_TERM)
        String name43 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_WHOLE_TERM
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseWholeTerm = loanProductsRequestFactory//
                .defaultLoanProductsRequestLP2Emi()//
                .name(name43)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .interestRatePerPeriod((double) 4)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_WHOLE_TERM)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseWholeTerm = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseWholeTerm);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_WHOLE_TERM,
                responseLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseWholeTerm);

        // LP2 + interest recalculation + advanced custom payment allocation + progressive loan schedule + horizontal
        // (LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL)
        String name44 = DefaultLoanProduct.LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL.getName();

        PostLoanProductsRequest loanProductsRequestAdvCustomPaymentAllocationProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name44)//
                .supportedInterestRefundTypes(Arrays.asList("MERCHANT_ISSUED_REFUND", "PAYOUT_REFUND"))//
                .enableAccrualActivityPosting(true) //
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
                        createPaymentAllocation("GOODWILL_CREDIT", "REAMORTIZATION"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestAdvCustomPaymentAllocationProgressiveLoanSchedule = createLoanProductIdempotent(
                loanProductsRequestAdvCustomPaymentAllocationProgressiveLoanSchedule);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADVANCED_CUSTOM_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE,
                responseLoanProductsRequestAdvCustomPaymentAllocationProgressiveLoanSchedule);

        // LP2 + interest recalculation + horizontal + interest refund
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL)
        String name45 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundFull = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name45)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .supportedInterestRefundTypes(supportedInterestRefundTypes).paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("INTEREST_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundFull = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundFull);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_FULL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundFull);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // interestRecalculationCompoundingMethod = none
        // payment allocation order: penalty-fee-interest-principal
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1)
        String name46 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPreclosePmtAlloc1 = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name46)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocationPenFeeIntPrincipal("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocationPenFeeIntPrincipal("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocationPenFeeIntPrincipal("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocationPenFeeIntPrincipal("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcDailyTillPreCloesePmtAlloc1 = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPreclosePmtAlloc1);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_PMT_ALLOC_1,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcDailyTillPreCloesePmtAlloc1);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30, LAST INSTALLMENT strategy
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY)
        String name47 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT_STRATEGY
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseLastInstallment = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name47)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "LAST_INSTALLMENT")));//
        PostLoanProductsResponse loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseLastInstallmentResponse = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseLastInstallment);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_LAST_INSTALLMENT,
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseLastInstallmentResponse);

        // LP2 with progressive loan schedule + horizontal + interest EMI + actual/actual + interest refund with
        // Merchant issued and Payment refund + interest recalculation
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION)
        String name48 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundRecalculation = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .name(name48)//
                .supportedInterestRefundTypes(Arrays.asList("MERCHANT_ISSUED_REFUND", "PAYOUT_REFUND"))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundInterestRecalculation = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundRecalculation);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundInterestRecalculation);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + multidisbursement + downpayment +
        // interest recalculation
        // 25%, auto disabled
        // (LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT)
        String name49 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestRecalculationEmi36030MultiDisburseDownPayment = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name49)//
                .enableDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestRecalculation36030MultiDisburseDownPayment = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestRecalculationEmi36030MultiDisburseDownPayment);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_DOWNPAYMENT,
                responseLoanProductsRequestLP2AdvancedpaymentInterestRecalculation36030MultiDisburseDownPayment);

        // LP2 with progressive loan schedule + horizontal + interest recalculation daily EMI + 360/30 + multi
        // disbursement + custom default payment allocation order
        // (LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE)
        String name50 = DefaultLoanProduct.LP2_ADV_CUSTOM_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvCustomPaymentAllocationInterestRecalculationDailyEmi36030MultiDisburse = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name50)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvCustomPaymentAllocationInterestRecalculationDaily36030MultiDisburse = createLoanProductIdempotent(
                loanProductsRequestLP2AdvCustomPaymentAllocationInterestRecalculationDailyEmi36030MultiDisburse);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADVANCED_CUSTOM_PAYMENT_ALLOCATION_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE,
                responseLoanProductsRequestLP2AdvCustomPaymentAllocationInterestRecalculationDaily36030MultiDisburse);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, no interest on past due principal balances,
        // preClosureInterestCalculationStrategy= till preclose,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_NO_CALC_ON_PAST_DUE_TILL_PRECLOSE)
        String name51 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_NO_CALC_ON_PAST_DUE_TILL_PRECLOSE
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedPaymentInterestEmi36030InterestRecalcDailyNoCalcOnPastDueTillPreclose = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name51)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .disallowInterestCalculationOnPastDue(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcNoCalcOnPastDueDailyTillPreClose = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedPaymentInterestEmi36030InterestRecalcDailyNoCalcOnPastDueTillPreclose);
        TestContext.INSTANCE.set(TestContextKey.temp,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcNoCalcOnPastDueDailyTillPreClose);

        // LP2 with progressive loan schedule + horizontal + interest recalculation daily EMI + 360/30
        // + multi disbursement + no interest on past due principal balances,
        // (LP2_ADV_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_NO_CALC_ON_PAST_DUE_EMI_360_30_MULTIDISBURSE)
        String name52 = DefaultLoanProduct.LP2_ADV_PAYMENT_ALLOC_INTEREST_RECALCULATION_DAILY_NO_CALC_ON_PAST_DUE_EMI_360_30_MULTIDISBURSE
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvPaymentAllocationInterestRecalculationDailyNoCalcOnPastDueEmi36030MultiDisburse = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name52)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .disallowInterestCalculationOnPastDue(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentAllocationInterestRecalculationDailyNoCalcOnPastDue36030MultiDisburse = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentAllocationInterestRecalculationDailyNoCalcOnPastDueEmi36030MultiDisburse);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADVANCED_PAYMENT_ALLOCATION_INTEREST_RECALCULATION_DAILY_NO_CALC_ON_PAST_DUE_EMI_360_30_MULTIDISBURSE,
                responseLoanProductsRequestLP2AdvPaymentAllocationInterestRecalculationDailyNoCalcOnPastDue36030MultiDisburse);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + multidisbursement + downpayment +
        // interest recalculation
        // 25%, auto enabled
        // (LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT)
        String name53 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestRecalculationEmi36030MultiDisburseAutoDownPayment = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name53)//
                .enableDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .enableAutoRepaymentForDownPayment(true).paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestRecalculation36030MultiDisburseAutoDownPayment = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestRecalculationEmi36030MultiDisburseAutoDownPayment);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_RECALCULATION_DAILY_EMI_360_30_MULTIDISBURSE_AUTO_DOWNPAYMENT,
                responseLoanProductsRequestLP2AdvancedpaymentInterestRecalculation36030MultiDisburseAutoDownPayment);

        // LP2 + interest recalculation + zero-interest chargeOff behaviour + progressive loan schedule + horizontal
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR)
        final String name54 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR
                .getName();

        final PostLoanProductsRequest loanProductsRequestAdvInterestRecalculationZeroInterestChargeOffBehaviourProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name54)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT")))
                .chargeOffBehaviour("ZERO_INTEREST");//
        final PostLoanProductsResponse responseLoanProductsRequestAdvInterestRecalculationZeroInterestChargeOffBehaviourProgressiveLoanSchedule = createLoanProductIdempotent(
                loanProductsRequestAdvInterestRecalculationZeroInterestChargeOffBehaviourProgressiveLoanSchedule);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR,
                responseLoanProductsRequestAdvInterestRecalculationZeroInterestChargeOffBehaviourProgressiveLoanSchedule);

        // LP2 + zero-interest chargeOff behaviour + progressive loan schedule + horizontal
        // (LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR)
        final String name55 = DefaultLoanProduct.LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR.getName();

        final PostLoanProductsRequest loanProductsRequestAdvZeroInterestChargeOffBehaviourProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name55)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE,
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .chargeOffBehaviour("ZERO_INTEREST");//
        PostLoanProductsResponse responseLoanProductsRequestAdvZeroInterestChargeOffBehaviourProgressiveLoanSchedule = createLoanProductIdempotent(
                loanProductsRequestAdvZeroInterestChargeOffBehaviourProgressiveLoanSchedule);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR,
                responseLoanProductsRequestAdvZeroInterestChargeOffBehaviourProgressiveLoanSchedule);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + multidisbursement +
        // accelerate-maturity chargeOff behaviour
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR)
        final String name56 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR
                .getName();

        final PostLoanProductsRequest loanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name56)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .chargeOffBehaviour("ACCELERATE_MATURITY");//
        PostLoanProductsResponse responseLoanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourProgressiveLoanSchedule = createLoanProductIdempotent(
                loanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourProgressiveLoanSchedule);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR,
                responseLoanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourProgressiveLoanSchedule);

        // + interest recalculation, allowPartialPeriodInterestCalculation = true
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 2
        // Frequency for Interest rate - Whole Year
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ALLOW_PARTIAL_PERIOD)
        String name57 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ALLOW_PARTIAL_PERIOD
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedPaymentInterestEmi36030InterestRecalculationDailyAllowPartialPeriod = loanProductsRequestFactory//
                .defaultLoanProductsRequestLP2Emi()//
                .name(name57)//
                .loanScheduleProcessingType("HORIZONTAL")//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT).preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(1)//
                .recalculationRestFrequencyInterval(1)//
                .repaymentEvery(1)//
                .interestRatePerPeriod((double) 7.0)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_MONTH)//
                .enableDownPayment(false)//
                .interestRecalculationCompoundingMethod(0)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_MONTHS)//
                .allowPartialPeriodInterestCalcualtion(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLP2AdvancedPaymentInterestEmi36030InterestRecalculationDailyAllowPartialPeriod = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedPaymentInterestEmi36030InterestRecalculationDailyAllowPartialPeriod);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ALLOW_PARTIAL_PERIOD,
                responseLP2AdvancedPaymentInterestEmi36030InterestRecalculationDailyAllowPartialPeriod);

        // LP2 + interest recalculation + zero-interest chargeOff behaviour + progressive loan schedule + horizontal
        // interest EMI + 360/30, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF)
        final String name58 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF.getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvancedPaymentInterestEmi36030InterestRecalculationDailyChargeOff = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiWithChargeOff()//
                .name(name58)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .chargeOffBehaviour("ZERO_INTEREST");//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedPaymentInterestEmi36030InterestRecalculationDailyChargeOff = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedPaymentInterestEmi36030InterestRecalculationDailyChargeOff);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF,
                responseLoanProductsRequestLP2AdvancedPaymentInterestEmi36030InterestRecalculationDailyChargeOff);

        // LP2 + NO interest recalculation + zero-interest chargeOff behaviour + progressive loan schedule + horizontal
        // (LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF)
        final String name59 = DefaultLoanProduct.LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF.getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvancedPaymentNoInterestInterestRecalculationChargeOff = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiWithChargeOff()//
                .name(name59)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"))) //
                .chargeOffBehaviour("ZERO_INTEREST");//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedPaymentNoInterestInterestRecalculationChargeOff = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedPaymentNoInterestInterestRecalculationChargeOff);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF,
                responseLoanProductsRequestLP2AdvancedPaymentNoInterestInterestRecalculationChargeOff);

        // LP2 with progressive loan schedule + horizontal + interest EMI + actual/actual + accrual activity posting +
        // down payment
        // enableAccrualActivityPosting
        // (LP2_ADV_PYMNT_INTEREST_DAILY_AUTO_DOWNPAYMENT_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY)
        String name60 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_AUTO_DOWNPAYMENT_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestAutoDownpaymentEmiActualActualAccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name60)//
                .enableDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .enableAutoRepaymentForDownPayment(true)//
                .enableAccrualActivityPosting(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestAutoDownpaymentEmiActualActualAccrualActivity = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestAutoDownpaymentEmiActualActualAccrualActivity);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestAutoDownpaymentEmiActualActualAccrualActivity);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // AccrualActivityPostingEnabled = true
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ACCRUAL_ACTIVITY_POSTING)
        String name61 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ACCRUAL_ACTIVITY_POSTING
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyAccrualActivityPosting = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name61)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .enableAccrualActivityPosting(true).paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseloanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyAccrualActivityPosting = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyAccrualActivityPosting);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_ACCRUAL_ACTIVITY_POSTING,
                responseloanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyAccrualActivityPosting);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/Actual
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_ACTUAL)
        String name62 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_ACTUAL.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterest360Actual = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name62)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.ACTUAL.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi360Actual = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterest360Actual);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_ACTUAL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi360Actual);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // Chargeback: Interest, Fee, Principal
        // + interest recalculation DISABLED
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL)
        String name63 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackInterestFeePrincipal = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name63)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("INTEREST", "FEE", "PRINCIPAL", "PENALTY"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackInterestFeePrincipal = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackInterestFeePrincipal);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackInterestFeePrincipal);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // Chargeback: Principal, Interest, Fee
        // + interest recalculation DISABLED
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE)
        String name64 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackPrincipalInterestFee = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name64)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("PRINCIPAL", "INTEREST", "FEE", "PENALTY"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackPrincipalInterestFee = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackPrincipalInterestFee);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE,
                responseLoanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackPrincipalInterestFee);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // Chargeback: Interest, Penalty, Fee, Principal
        // + interest recalculation DISABLED
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL)
        String name65 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackInterestPenaltyFeePrincipal = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name65)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("INTEREST", "PENALTY", "FEE", "PRINCIPAL"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackInterestPenaltyFeePrincipal = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackInterestPenaltyFeePrincipal);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestDailyEmi36030ChargebackInterestPenaltyFeePrincipal);

        // LP2 with progressive loan schedule + horizontal + interest EMI + actual/actual
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY)
        String name66 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRecalculationDaily = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name66)//
                .maxPrincipal(1000000.0)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRecalculationDaily = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRecalculationDaily);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRecalculationDaily);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + accrual activity
        String name67 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedPaymentInterestEmi36030AccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name67)//
                .enableAccrualActivityPosting(true)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedPaymentInterestEmi36030AccrualActivity = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedPaymentInterestEmi36030AccrualActivity);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_ACCRUAL_ACTIVITY,
                responseLoanProductsRequestLP2AdvancedPaymentInterestEmi36030AccrualActivity);

        // LP2 with progressive loan schedule + horizontal + accelerate-maturity chargeOff behaviour
        // (LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR)
        final String name68 = DefaultLoanProduct.LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR.getName();

        final PostLoanProductsRequest loanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourProgressiveLoanSchedule2 = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name68)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .chargeOffBehaviour("ACCELERATE_MATURITY");//
        PostLoanProductsResponse responseLoanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourProgressiveLoanSchedule2 = createLoanProductIdempotent(
                loanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourProgressiveLoanSchedule2);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR,
                responseLoanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourProgressiveLoanSchedule2);

        // LP2 with disabled interest recalculation + chargeback allocation(INTEREST, PENALTY, FEE, PRINCIPAL)
        // (LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_INTEREST_FIRST)
        String name69 = DefaultLoanProduct.LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_INTEREST_FIRST.getName();
        PostLoanProductsRequest loanProductsRequestChargebackAllocation = loanProductsRequestFactory.defaultLoanProductsRequestLP2Emi()//
                .name(name69)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("INTEREST", "PENALTY", "FEE", "PRINCIPAL"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse loanProductsResponseChargebackAllocation = createLoanProductIdempotent(
                loanProductsRequestChargebackAllocation);
        TestContext.INSTANCE.set(TestContextKey.LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_INTEREST_FIRST_RESPONSE,
                loanProductsResponseChargebackAllocation);

        // LP2 with disabled interest recalculation + chargeback allocation(PRINCIPAL, INTEREST, PENALTY, FEE)
        // (LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_FIRST)
        String name70 = DefaultLoanProduct.LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_FIRST.getName();
        PostLoanProductsRequest loanProductsRequestChargebackAllocationPrincipalFirst = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name70)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("PRINCIPAL", "INTEREST", "PENALTY", "FEE"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse loanProductsResponseChargebackAllocationPrincipalFirst = createLoanProductIdempotent(
                loanProductsRequestChargebackAllocationPrincipalFirst);
        TestContext.INSTANCE.set(TestContextKey.LP2_NO_INTEREST_RECALCULATION_CHARGEBACK_ALLOCATION_PRINCIPAL_FIRST_RESPONSE,
                loanProductsResponseChargebackAllocationPrincipalFirst);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // Chargeback: Interest, Penalty, Fee, Principal
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL)
        String name71 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackInterestPenaltyFeePrincipal = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name71)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("INTEREST", "PENALTY", "FEE", "PRINCIPAL"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackInterestPenaltyFeePrincipal = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackInterestPenaltyFeePrincipal);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_PENALTY_FEE_PRINCIPAL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackInterestPenaltyFeePrincipal);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // Chargeback: Interest, Fee, Principal, Penalty
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL)
        String name72 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackInterestFeePrincipal = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name72)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("INTEREST", "FEE", "PRINCIPAL", "PENALTY"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackInterestFeePrincipal = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackInterestFeePrincipal);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_INTEREST_FEE_PRINCIPAL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackInterestFeePrincipal);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // Chargeback: Principal, Interest, Fee, Penalty
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE)
        String name73 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackPrincipalInterestFee = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name73)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("PRINCIPAL", "INTEREST", "FEE", "PENALTY"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackPrincipalInterestFee = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackPrincipalInterestFee);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_EMI_360_30_CHARGEBACK_PRINCIPAL_INTEREST_FEE,
                responseLoanProductsRequestLP2AdvancedpaymentInterestDailyInterestRecalcEmi36030ChargebackPrincipalInterestFee);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 +
        // accelerate-maturity chargeOff behaviour + last installment strategy
        // (LP2_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY)
        final String name74 = DefaultLoanProduct.LP2_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY
                .getName();

        final PostLoanProductsRequest loanProductsRequestAdvCustomInterestRecalculationAccelerateMaturityChargeOffBehaviourLastInstallmentStrategyProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name74)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "LAST_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL))) //
                .chargeOffBehaviour("ACCELERATE_MATURITY");//
        final PostLoanProductsResponse responseLoanProductsRequestAdvCustomInterestRecalculationAccelerateMaturityChargeOffBehaviourLastInstallmentStrategyProgressiveLoanSchedule = createLoanProductIdempotent(
                loanProductsRequestAdvCustomInterestRecalculationAccelerateMaturityChargeOffBehaviourLastInstallmentStrategyProgressiveLoanSchedule);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY,
                responseLoanProductsRequestAdvCustomInterestRecalculationAccelerateMaturityChargeOffBehaviourLastInstallmentStrategyProgressiveLoanSchedule);

        // LP2 with progressive loan schedule + horizontal + accelerate-maturity chargeOff behaviour + last installment
        // strategy
        // (LP2_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY)
        final String name75 = DefaultLoanProduct.LP2_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY.getName();

        final PostLoanProductsRequest loanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourLastInstallmentStrategyProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name75)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "LAST_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL))) //
                .chargeOffBehaviour("ACCELERATE_MATURITY");//
        final PostLoanProductsResponse responseLoanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourLastInstallmentStrategyProgressiveLoanSchedule = createLoanProductIdempotent(
                loanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourLastInstallmentStrategyProgressiveLoanSchedule);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY,
                responseLoanProductsRequestAdvCustomAccelerateMaturityChargeOffBehaviourLastInstallmentStrategyProgressiveLoanSchedule);

        // LP2 with progressive loan schedule + horizontal + interest EMI + interestRecognitionOnDisbursementDate = true
        // + 360/30 + accrual activity
        String name76 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedPaymentInterestRecognitionOnDisbursementEmi36030AccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name76)//
                .enableAccrualActivityPosting(true)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .interestRecognitionOnDisbursementDate(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedPaymentInterestInterestRecognitionOnDisbursementEmi36030AccrualActivity = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedPaymentInterestRecognitionOnDisbursementEmi36030AccrualActivity);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_360_30_ACCRUAL_ACTIVITY,
                responseLoanProductsRequestLP2AdvancedPaymentInterestInterestRecognitionOnDisbursementEmi36030AccrualActivity);

        // LP2 with progressive loan schedule + horizontal + interest EMI + interestRecognitionOnDisbursementDate = true
        // + 360/30 + accrual activity
        String name77 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedPaymentInterestRecognitionOnDisbursementEmiActualActual30AccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name77)//
                .enableAccrualActivityPosting(true)//
                .daysInYearType(DaysInYearType.ACTUAL.value)//
                .daysInMonthType(DaysInMonthType.ACTUAL.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .interestRecognitionOnDisbursementDate(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedPaymentInterestInterestRecognitionOnDisbursementEmiActualActualAccrualActivity = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedPaymentInterestRecognitionOnDisbursementEmiActualActual30AccrualActivity);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_RECOGNITION_DISBURSEMENT_DAILY_EMI_ACTUAL_ACTUAL_ACCRUAL_ACTIVITY,
                responseLoanProductsRequestLP2AdvancedPaymentInterestInterestRecognitionOnDisbursementEmiActualActualAccrualActivity);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose, accountingRule = NONE
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_ACCOUNTING_RULE_NONE)
        String name78 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_ACCOUNTING_RULE_NONE.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcAccountingRuleNone = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name78)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .accountingRule(LOAN_ACCOUNTING_RULE_NONE).chargeOffBehaviour("REGULAR").paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcAccountingRuleNone = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcAccountingRuleNone);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_ACCOUNTING_RULE_NONE,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcAccountingRuleNone);

        // LP2 with progressive loan schedule + horizontal
        // + interest recalculation + interest recognition from disbursement date enabled
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INT_RECALCULATION_ZERO_INT_CHARGE_OFF_INT_RECOGNITION_FROM_DISB_DATE)
        String name79 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INT_RECALCULATION_ZERO_INT_CHARGE_OFF_INT_RECOGNITION_FROM_DISB_DATE
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvPaymentInterestRecalcDailyZeroIntChargeOffIntRecognitionFromDisbDate = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name79)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .interestRecognitionOnDisbursementDate(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .chargeOffBehaviour("ZERO_INTEREST");//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentInterestRecalcDailyZeroIntChargeOffIntRecognitionFromDisbDate = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentInterestRecalcDailyZeroIntChargeOffIntRecognitionFromDisbDate);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INT_RECALCULATION_ZERO_INT_CHARGE_OFF_INT_RECOGNITION_FROM_DISB_DATE,
                responseLoanProductsRequestLP2AdvPaymentInterestRecalcDailyZeroIntChargeOffIntRecognitionFromDisbDate);

        // LP2 with progressive loan schedule + horizontal + interest EMI + actual/actual + leap year
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALCULATION_DAILY)
        String name80 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_LEAP_YEAR_INTEREST_RECALCULATION_DAILY.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedPaymentInterestEmiActualActualLeapYearInterestRecalculationDaily = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name80)//
                .maxPrincipal(1000000.0)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .daysInYearType(DaysInYearType.ACTUAL.value)//
                .daysInYearCustomStrategy(FEB_29_PERIOD_ONLY.getValue()).paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedPaymentInterestEmiActualActualLeapYearInterestRecalculationDaily = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedPaymentInterestEmiActualActualLeapYearInterestRecalculationDaily);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_LEAP_YEAR_INTEREST_RECALCULATION_DAILY,
                responseLoanProductsRequestLP2AdvancedPaymentInterestEmiActualActualLeapYearInterestRecalculationDaily);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily, interest recalculation enabled
        // (LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_INT_RECALC)
        final String name81 = DefaultLoanProduct.LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_INT_RECALC.getName();
        final PostLoanProductsRequest loanProductsRequestInterestDecliningPeriodDailyIntRecalc = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDeclining().name(name81).isInterestRecalculationEnabled(true)
                .preClosureInterestCalculationStrategy(1).rescheduleStrategyMethod(1).interestRecalculationCompoundingMethod(0)
                .recalculationRestFrequencyType(2).recalculationRestFrequencyInterval(1)
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value).allowPartialPeriodInterestCalcualtion(false);
        final PostLoanProductsResponse responseInterestDecliningPeriodDailyIntRecalc = createLoanProductIdempotent(
                loanProductsRequestInterestDecliningPeriodDailyIntRecalc);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_PERIOD_DAILY_INT_RECALC,
                responseInterestDecliningPeriodDailyIntRecalc);

        // LP1 with 12% DECLINING BALANCE interest, interest period: Daily, interest recalculation enabled, 360/30
        // (LP1_INTEREST_DECLINING_BALANCE_PERIOD_DAILY_INT_RECALC)
        final String name82 = DefaultLoanProduct.LP1_INTEREST_360_30_DECLINING_BALANCE_PERIOD_DAILY_INT_RECALC.getName();
        final PostLoanProductsRequest loanProductsRequestInterest36030DecliningPeriodDailyIntRecalc = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDeclining().name(name82).isInterestRecalculationEnabled(false)
                .daysInYearType(DaysInYearType.DAYS360.value).daysInMonthType(DaysInMonthType.DAYS30.value)
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value).allowPartialPeriodInterestCalcualtion(false);
        final PostLoanProductsResponse responseInterest36030DecliningPeriodDailyIntRecalc = createLoanProductIdempotent(
                loanProductsRequestInterest36030DecliningPeriodDailyIntRecalc);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_360_30__DECLINING_PERIOD_DAILY_INT_RECALC,
                responseInterest36030DecliningPeriodDailyIntRecalc);

        // LP2 + zero-interest chargeOff behaviour + progressive loan schedule + horizontal + delinquent charge-off
        // reason to GL account mapping
        // (LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON)
        final String name83 = DefaultLoanProduct.LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON.getName();

        final PostLoanProductsRequest loanProductsRequestAdvZeroInterestChargeOffProgressiveDelinquentReason = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2ChargeOffReasonToExpenseAccountMappings()//
                .name(name83)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .chargeOffBehaviour("ZERO_INTEREST");//
        PostLoanProductsResponse responseLoanProductsRequestAdvZeroInterestChargeOffProgressiveDelinquentReason = createLoanProductIdempotent(
                loanProductsRequestAdvZeroInterestChargeOffProgressiveDelinquentReason);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON,
                responseLoanProductsRequestAdvZeroInterestChargeOffProgressiveDelinquentReason);

        // LP2 advanced custom payment allocation + progressive loan schedule + horizontal + down payment
        // (LP2_ADV_DP_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL)
        String name84 = DefaultLoanProduct.LP2_ADV_DP_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL.getName();

        PostLoanProductsRequest loanProductsRequestAdvDPCustomPaymentAllocationProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name84)//
                .enableAccrualActivityPosting(false) //
                .enableDownPayment(true) //
                .enableAutoRepaymentForDownPayment(true) //
                .multiDisburseLoan(true) //
                .disallowExpectedDisbursements(true) //
                .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25.0)) //
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "REAMORTIZATION"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "LAST_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE), //
                        createPaymentAllocation("DOWN_PAYMENT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE) //
                ));//
        PostLoanProductsResponse responseLoanProductsResponseAdvDPCustomPaymentAllocationProgressiveLoanSchedule = createLoanProductIdempotent(
                loanProductsRequestAdvDPCustomPaymentAllocationProgressiveLoanSchedule);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADVANCED_DP_CUSTOM_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE,
                responseLoanProductsResponseAdvDPCustomPaymentAllocationProgressiveLoanSchedule);

        // LP2 advanced custom payment allocation + progressive loan schedule + horizontal + down payment + interest
        // recalculation
        // (LP2_ADV_DP_IR_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL)
        String name85 = DefaultLoanProduct.LP2_ADV_DP_IR_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL.getName();

        PostLoanProductsRequest loanProductsRequestAdvDPIRCustomPaymentAllocationProgressiveLoanSchedule = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name85)//
                .enableAccrualActivityPosting(false) //
                .enableDownPayment(true) //
                .enableAutoRepaymentForDownPayment(true) //
                .multiDisburseLoan(true) //
                .disallowExpectedDisbursements(true) //
                .installmentAmountInMultiplesOf(null) //
                .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25.0)) //
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "REAMORTIZATION"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "LAST_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE), //
                        createPaymentAllocation("DOWN_PAYMENT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE) //
                ));//
        PostLoanProductsResponse responseLoanProductsResponseAdvDPIRCustomPaymentAllocationProgressiveLoanSchedule = createLoanProductIdempotent(
                loanProductsRequestAdvDPIRCustomPaymentAllocationProgressiveLoanSchedule);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADVANCED_DP_IR_CUSTOM_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE,
                responseLoanProductsResponseAdvDPIRCustomPaymentAllocationProgressiveLoanSchedule);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + interest recalculation
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // charges - Disbursement Charge
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES)
        String name86 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES.getName();
        List<LoanProductChargeData> chargesDisbursement = new ArrayList<>();
        chargesDisbursement.add(new LoanProductChargeData().id(ChargeProductType.LOAN_DISBURSEMENT_CHARGE.value));
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyDisbursementCharge = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name86)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .charges(chargesDisbursement)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyDisbursementCharge = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyDisbursementCharge);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_DISBURSEMENT_CHARGES,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyDisbursementCharge);

        // LP2 with progressive loan schedule + horizontal + interest recalculation daily EMI + 360/30 +
        // multidisbursement
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE)
        String name87 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburse = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name87)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(false)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburse = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburse);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_EXPECT_TRANCHE,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburse);

        // LP2 with progressive loan schedule + horizontal + interest recalculation daily EMI + 360/30 +
        // multidisbursement
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // charges - Disbursement Charge
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE)
        String name88 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburseDisbursementCharge = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name88)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburseDisbursementCharge = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburseDisbursementCharge);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburseDisbursementCharge);

        // LP2 with progressive loan schedule + horizontal + interest recalculation daily EMI + 360/30 + cash based
        // accounting
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // charges - Disbursement Charge
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CASH_ACCOUNTING_DISBURSEMENT_CHARGES)
        String name89 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CASH_ACCOUNTING_DISBURSEMENT_CHARGES
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyCashAccountingDisbursementCharge = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCashAccounting()//
                .name(name89)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .charges(chargesDisbursement)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyCashAccountingDisbursementCharge = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyCashAccountingDisbursementCharge);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CASH_ACCOUNTING_DISBURSEMENT_CHARGES,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyCashAccountingDisbursementCharge);

        // LP2 + zero-interest chargeOff behaviour + progressive loan schedule + horizontal + delinquent charge-off
        // reason to GL account mapping + interest recalculation
        // (LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON_INTEREST_RECALC)
        final String name90 = DefaultLoanProduct.LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON_INTEREST_RECALC.getName();

        final PostLoanProductsRequest loanProductsRequestAdvZeroInterestChargeOffProgressiveDelinquentReasonIntRecalc = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2ChargeOffReasonToExpenseAccountMappings()//
                .name(name90)//
                .enableDownPayment(false)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .chargeOffBehaviour("ZERO_INTEREST");//
        PostLoanProductsResponse responseLoanProductsRequestAdvZeroInterestChargeOffProgressiveDelinquentReasonIntRecalc = createLoanProductIdempotent(
                loanProductsRequestAdvZeroInterestChargeOffProgressiveDelinquentReasonIntRecalc);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON_INTEREST_RECALC,
                responseLoanProductsRequestAdvZeroInterestChargeOffProgressiveDelinquentReasonIntRecalc);

        // LP2 + zero-interest chargeOff behaviour + progressive loan schedule + horizontal + interest recalculation
        // (LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF)
        final String name91 = DefaultLoanProduct.LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF.getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentIntEmiActualActualIntRefundFullZeroIntChargeOff = loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundFull
                .name(name91)//
                .shortName(loanProductsRequestFactory.generateShortNameSafely())//
                .chargeOffBehaviour("ZERO_INTEREST");//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentIntEmiActualActualIntRefundFullZeroIntChargeOff = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentIntEmiActualActualIntRefundFullZeroIntChargeOff);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF,
                responseLoanProductsRequestLP2AdvPaymentIntEmiActualActualIntRefundFullZeroIntChargeOff);

        // LP2 + accelerate maturity chargeOff behaviour + progressive loan schedule + horizontal + interest
        // recalculation
        // (LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ACCELERATE_MATURITY_CHARGE_OFF)
        final String name92 = DefaultLoanProduct.LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ACCELERATE_MATURITY_CHARGE_OFF
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentIntEmiActualActualIntRefundFullAccelerateMaturityChargeOff = loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundFull
                .name(name92)//
                .shortName(loanProductsRequestFactory.generateShortNameSafely())//
                .chargeOffBehaviour("ACCELERATE_MATURITY");//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentIntEmiActualActualIntRefundFullAccelerateMaturityChargeOff = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentIntEmiActualActualIntRefundFullAccelerateMaturityChargeOff);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_INT_REFUND_FULL_ACCELERATE_MATURITY_CHARGE_OFF,
                responseLoanProductsRequestLP2AdvPaymentIntEmiActualActualIntRefundFullAccelerateMaturityChargeOff);

        // LP2 + no interest recalculation + horizontal + interest refund
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_NO_INTEREST_RECALC_REFUND_FULL)
        String name93 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_NO_INTEREST_RECALC_REFUND_FULL.getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedPaymentInterestEmiActualActualNoInterestRecalcRefundFull = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name93)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .supportedInterestRefundTypes(supportedInterestRefundTypes).paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("INTEREST_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualNoInterestRecalcRefundFull = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedPaymentInterestEmiActualActualNoInterestRecalcRefundFull);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_NO_INTEREST_RECALC_REFUND_FULL,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualNoInterestRecalcRefundFull);

        // LP2 + zero-interest chargeOff behaviour + progressive loan schedule + horizontal + no interest recalculation
        // (LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_NO_INTEREST_RECALC_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF)
        final String name94 = DefaultLoanProduct.LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_NO_INTEREST_RECALC_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentIntEmiActualActualNoInterestRecalcIntRefundFullZeroIntChargeOff = loanProductsRequestLP2AdvancedPaymentInterestEmiActualActualNoInterestRecalcRefundFull
                .name(name94)//
                .shortName(loanProductsRequestFactory.generateShortNameSafely())//
                .chargeOffBehaviour("ZERO_INTEREST");//
        final PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentIntEmiActualActualNoInterestRecalcIntRefundFullZeroIntChargeOff = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentIntEmiActualActualNoInterestRecalcIntRefundFullZeroIntChargeOff);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_NO_INTEREST_RECALC_INT_REFUND_FULL_ZERO_INT_CHARGE_OFF,
                responseLoanProductsRequestLP2AdvPaymentIntEmiActualActualNoInterestRecalcIntRefundFullZeroIntChargeOff);

        // LP2 + accelerate maturity chargeOff behaviour + progressive loan schedule + horizontal + no interest
        // recalculation
        // (LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_NO_INTEREST_RECALC_INT_REFUND_FULL_ACC_MATUR_CHARGE_OFF)
        final String name95 = DefaultLoanProduct.LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_NO_INTEREST_RECALC_INT_REFUND_FULL_ACC_MATUR_CHARGE_OFF
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentIntEmiActualActualNoInterestRecalcIntRefundFullAccelerateMaturityChargeOff = loanProductsRequestLP2AdvancedPaymentInterestEmiActualActualNoInterestRecalcRefundFull
                .name(name95)//
                .shortName(loanProductsRequestFactory.generateShortNameSafely())//
                .chargeOffBehaviour("ACCELERATE_MATURITY");//
        final PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentIntEmiActualActualNoInterestRecalcIntRefundFullAccelerateMaturityChargeOff = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentIntEmiActualActualNoInterestRecalcIntRefundFullAccelerateMaturityChargeOff);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INT_DAILY_EMI_ACTUAL_ACTUAL_NO_INTEREST_RECALC_INT_REFUND_FULL_ACC_MATUR_CHARGE_OFF,
                responseLoanProductsRequestLP2AdvPaymentIntEmiActualActualNoInterestRecalcIntRefundFullAccelerateMaturityChargeOff);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till rest frequency date,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_REST_FREQUENCY_DATE_LAST_INSTALLMENT)
        String name96 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_REST_FREQUENCY_DATE_LAST_INSTALLMENT
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillRestFrequencyDateLastInstallment = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name96)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(2)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcDailyTillRestFrequencyDateLastInstallment = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillRestFrequencyDateLastInstallment);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_IR_DAILY_TILL_REST_FREQUENCY_DATE_LAST_INSTALLMENT,
                responseLoanProductsRequestLP2AdvancedpaymentInterest36030InterestRecalcDailyTillRestFrequencyDateLastInstallment);

        final String name97 = DefaultLoanProduct.LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_CAPITALIZED_INCOME.getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPaymentCapitalizedIncome = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2CapitalizedIncome()//
                .name(name97)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .overAppliedCalculationType(null)//
                .overAppliedNumber(null)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPaymentCapitalizedIncome = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPaymentCapitalizedIncome);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_PROGRESSIVE_ADV_PYMNT_CAPITALIZED_INCOME,
                responseLoanProductsRequestLP2ProgressiveAdvPaymentCapitalizedIncome);

        // LP2 with progressive loan schedule + horizontal + interest EMI + actual/actual + interest refund with
        // Merchant issued and Payment refund + interest recalculation + Multidisbursement
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION_MULTIDISB)
        String name98 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION_MULTIDISB
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundRecalculationMultiDisb = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .name(name98)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .supportedInterestRefundTypes(Arrays.asList("MERCHANT_ISSUED_REFUND", "PAYOUT_REFUND"))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundInterestRecalculationMultidisb = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundRecalculationMultiDisb);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_REFUND_INTEREST_RECALCULATION_MULTIDISB,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmiActualActualInterestRefundInterestRecalculationMultidisb);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // capitalized income enabled
        final String name99 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME.getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncome = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome().name(name99)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncome = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncome);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME,
                responseLoanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncome);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // multidisbursal
        // capitalized income enabled
        final String name100 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSAL_CAPITALIZED_INCOME
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcMultidisbursalCapitalizedIncome = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name100)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcMultidisbursalCapitalizedIncome = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcMultidisbursalCapitalizedIncome);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSAL_CAPITALIZED_INCOME,
                responseLoanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcMultidisbursalCapitalizedIncome);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // capitalized income enabled, capitalized income type: FEE
        final String name101 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_FEE
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncomeFee = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name101)//
                .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncomeFee = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncomeFee);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_FEE,
                responseLoanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncomeFee);

        // LP2 + zero-interest chargeOff behaviour + progressive loan schedule + horizontal + delinquent charge-off
        // reason to GL account mapping + interest recalculation
        // capitalized income enabled
        final String name102 = DefaultLoanProduct.LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_DELINQUENT_REASON_INTEREST_RECALC_CAPITALIZED_INCOME
                .getName();
        final PostLoanProductsRequest loanProductsRequestPrgAdvZeroIntChargeOffDelinquentReasonIntRecalcCapitalizedIncome = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2ChargeOffReasonToExpenseAccountMappingsWithCapitalizedIncome()//
                .name(name102)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestPrgAdvZeroIntChargeOffDelinquentReasonIntRecalcCapitalizedIncome = createLoanProductIdempotent(
                loanProductsRequestPrgAdvZeroIntChargeOffDelinquentReasonIntRecalcCapitalizedIncome);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_ZERO_INT_CHARGE_OFF_DELINQUENT_REASON_INT_RECALC_CAPITALIZED_INCOME,
                responseLoanProductsRequestPrgAdvZeroIntChargeOffDelinquentReasonIntRecalcCapitalizedIncome);

        // Merchant issued with Interest refund + interest recalculation, 360/30
        // accrual activity enabled
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY)
        String name103 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRefundRecalculationAccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name103)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .supportedInterestRefundTypes(Arrays.asList("MERCHANT_ISSUED_REFUND", "PAYOUT_REFUND"))//
                .enableAccrualActivityPosting(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("INTEREST_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRefundRecalculationAccrualActivity = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRefundRecalculationAccrualActivity);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_ACCRUAL_ACTIVITY,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRefundRecalculationAccrualActivity);

        // Merchant issued with Interest refund + interest recalculation, 360/30
        // accrual activity enabled
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY)
        String name104 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRefundRecalculation = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name104)//
                .enableDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .enableAutoRepaymentForDownPayment(true)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .supportedInterestRefundTypes(Arrays.asList("MERCHANT_ISSUED_REFUND", "PAYOUT_REFUND"))//
                .enableAccrualActivityPosting(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("INTEREST_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRefundInterestRecalculation = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRefundRecalculation);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_REFUND_INTEREST_RECALC_DOWNPAYMENT_ACCRUAL_ACTIVITY,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRefundInterestRecalculation);

        // LP2 + interest recalculation + zero-interest chargeOff behaviour + progressive loan schedule + horizontal
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR)
        final String name105 = DefaultLoanProduct.LP2_ADV_PYMNT_360_30_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY
                .getName();
        final PostLoanProductsRequest loanProductsRequestAdvInterestRecalculationZeroInterestChargeOffBehaviourAccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name105)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT")))
                .enableAccrualActivityPosting(true)//
                .chargeOffBehaviour("ZERO_INTEREST");//
        PostLoanProductsResponse responseLoanProductsRequestAdvInterestRecalculationZeroInterestChargeOffBehaviourAccrualActivity = createLoanProductIdempotent(
                loanProductsRequestAdvInterestRecalculationZeroInterestChargeOffBehaviourAccrualActivity);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_360_30_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY,
                responseLoanProductsRequestAdvInterestRecalculationZeroInterestChargeOffBehaviourAccrualActivity);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + custom allocation capital
        // adjustment
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // capitalized income enabled + income type - fee
        final String name106 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncomeAdjCustomAlloc = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name106)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("CAPITALIZED_INCOME_ADJUSTMENT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//

        PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncomeAdjCustomAlloc = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncomeAdjCustomAlloc);

        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC,
                responseLoanProductsRequestLP2ProgressiveAdvPayment36030InterestRecalcCapitalizedIncomeAdjCustomAlloc);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + multidisbursement +
        // contract termination
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION)
        final String name107 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION.getName();

        final PostLoanProductsRequest loanProductsRequestAdvCustomContractTerminationProgressiveLoanScheduleIntRecalc = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name107)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestAdvCustomContractTerminationProgressiveLoanScheduleIntRecalc = createLoanProductIdempotent(
                loanProductsRequestAdvCustomContractTerminationProgressiveLoanScheduleIntRecalc);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION,
                responseLoanProductsRequestAdvCustomContractTerminationProgressiveLoanScheduleIntRecalc);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // multidisbursal that doesn't expect tranches with allowed approved/disbursed amount over applied amount
        // capitalized income enabled; approver over applied amount enabled with percentage type
        final String name108 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_OVER_APPLIED_PERCENTAGE_CAPITALIZED_INCOME
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalApprovedOverAppliedCapitalizedIncome = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name108)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OverAppliedCalculationType.PERCENTAGE.value)//
                .overAppliedNumber(50);//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalApprovedOverAppliedCapitalizedIncome = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalApprovedOverAppliedCapitalizedIncome);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_OVER_APPLIED_PERCENTAGE_CAPITALIZED_INCOME,
                responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalApprovedOverAppliedCapitalizedIncome);

        // LP2 with Down-payment+autopayment + custom advanced payment allocation
        // (LP2_DOWNPAYMENT_AUTO_ADVANCED_CUSTOM_PAYMENT_ALLOCATION)
        String name109 = DefaultLoanProduct.LP2_DOWNPAYMENT_AUTO_ADVANCED_CUSTOM_PAYMENT_ALLOCATION.getName();
        PostLoanProductsRequest loanProductsRequestDownPaymentAutoAdvCustomPaymentAllocation = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name109)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "REAMORTIZATION"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "LAST_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE), //
                        createPaymentAllocation("DOWN_PAYMENT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAutoAdvCustomPaymentAllocation = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAutoAdvCustomPaymentAllocation);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_AUTO_ADVANCED_CUSTOM_PAYMENT_ALLOCATION,
                responseLoanProductsRequestDownPaymentAutoAdvCustomPaymentAllocation);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // multidisbursal
        // capitalized income enabled
        final String name110 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalCapitalizedIncomeAdjCustomAlloc = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name110)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("CAPITALIZED_INCOME_ADJUSTMENT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalCapitalizedIncomeAdjCustomAlloc = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalCapitalizedIncomeAdjCustomAlloc);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC,
                responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalCapitalizedIncomeAdjCustomAlloc);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // multidisbursal that doesn't expect tranches with allowed approved/disbursed amount over applied amount
        // capitalized income enabled; approver over applied amount enabled with percentage type
        final String name111 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_OVER_APPLIED_FLAT_CAPITALIZED_INCOME
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalApprovedOverAppliedFlatCapitalizedIncome = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name111)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OverAppliedCalculationType.FIXED_SIZE.value)//
                .overAppliedNumber(1000);//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalApprovedOverAppliedFlatCapitalizedIncome = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalApprovedOverAppliedFlatCapitalizedIncome);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_OVER_APPLIED_FLAT_CAPITALIZED_INCOME,
                responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbursalApprovedOverAppliedFlatCapitalizedIncome);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // multidisbursal that doesn't expect tranches with allowed approved/disbursed amount over applied amount
        // capitalized income enabled; approver over applied amount enabled with percentage type
        final String name112 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_APPROVED_OVER_APPLIED_PERCENTAGE_CAPITALIZED_INCOME
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcApprovedOverAppliedCapitalizedIncome = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name112)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OverAppliedCalculationType.PERCENTAGE.value)//
                .overAppliedNumber(50);//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcApprovedOverAppliedCapitalizedIncome = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcApprovedOverAppliedCapitalizedIncome);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_APPROVED_OVER_APPLIED_PERCENTAGE_CAPITALIZED_INCOME,
                responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcApprovedOverAppliedCapitalizedIncome);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // multidisbursal that doesn't expect tranches with allowed approved/disbursed amount over applied amount
        // capitalized income enabled; approver over applied amount enabled with fixed-size(flat) type
        final String name113 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_APPROVED_OVER_APPLIED_FLAT_CAPITALIZED_INCOME
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcApprovedOverAppliedFlatCapitalizedIncome = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name113)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OverAppliedCalculationType.FIXED_SIZE.value)//
                .overAppliedNumber(1000);//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcApprovedOverAppliedFlatCapitalizedIncome = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcApprovedOverAppliedFlatCapitalizedIncome);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_APPROVED_OVER_APPLIED_FLAT_CAPITALIZED_INCOME,
                responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcApprovedOverAppliedFlatCapitalizedIncome);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, buy down fees enabled
        final String name114 = DefaultLoanProduct.LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES.getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFees = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2BuyDownFees()//
                .name(name114)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFees = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFees);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_PROGRESSIVE_ADV_PYMNT_BUYDOWN_FEES,
                responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFees);

        // LP2 + interest recalculation + zero-interest chargeOff behaviour + progressive loan schedule + horizontal
        // (LP2_ADV_PYMNT_360_30_INTEREST_RECALC_AUTO_DOWNPAYMENT_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY)
        final String name115 = DefaultLoanProduct.LP2_ADV_PYMNT_360_30_INTEREST_RECALC_AUTO_DOWNPAYMENT_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY
                .getName();
        final PostLoanProductsRequest loanProductsRequestAdvInterestRecalculationAutoDownpaymentZeroInterestChargeOffBehaviourAccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name115)//
                .enableDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .enableAutoRepaymentForDownPayment(true)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT")))
                .enableAccrualActivityPosting(true)//
                .chargeOffBehaviour("ZERO_INTEREST");//
        final PostLoanProductsResponse responseLoanProductsRequestAdvInterestRecalculationAutoDownpaymentZeroInterestChargeOffBehaviourAccrualActivity = createLoanProductIdempotent(
                loanProductsRequestAdvInterestRecalculationAutoDownpaymentZeroInterestChargeOffBehaviourAccrualActivity);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_360_30_INTEREST_RECALC_AUTO_DOWNPAYMENT_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY,
                responseLoanProductsRequestAdvInterestRecalculationAutoDownpaymentZeroInterestChargeOffBehaviourAccrualActivity);

        // LP2 with progressive loan schedule + horizontal + interest recalculation
        // charges - Installment Fee Flat
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_DAILY_INSTALLMENT_FEE_FLAT_CHARGES)
        final String name116 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_DAILY_INSTALLMENT_FEE_FLAT_CHARGES.getName();
        final List<LoanProductChargeData> chargesInstallmentFeeFlat = new ArrayList<>();
        chargesInstallmentFeeFlat.add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_FLAT.value));
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentInterestRecalcDailyInstallmentFeeFlatCharges = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name116)//
                .charges(chargesInstallmentFeeFlat)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentInterestRecalcDailyInstallmentFeeFlatCharges = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentInterestRecalcDailyInstallmentFeeFlatCharges);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALC_DAILY_INSTALLMENT_FEE_FLAT_CHARGES,
                responseLoanProductsRequestLP2AdvPaymentInterestRecalcDailyInstallmentFeeFlatCharges);

        // LP2 with progressive loan schedule + horizontal
        // charges - Installment Fee Percentage Amount
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_CHARGES)
        final String name117 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_CHARGES.getName();
        final List<LoanProductChargeData> chargesInstallmentFeePercentAmount = new ArrayList<>();
        chargesInstallmentFeePercentAmount
                .add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT.value));
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentInstallmentFeePercentAmountCharges = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name117)//
                .charges(chargesInstallmentFeePercentAmount)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        final PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentInstallmentFeePercentAmountCharges = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentInstallmentFeePercentAmountCharges);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_CHARGES,
                responseLoanProductsRequestLP2AdvPaymentInstallmentFeePercentAmountCharges);

        // LP2 with progressive loan schedule + horizontal
        // charges - Installment Fee Percentage Interest
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_INTEREST_CHARGES)
        final String name118 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_INTEREST_CHARGES.getName();
        final List<LoanProductChargeData> chargesInstallmentFeePercentInterest = new ArrayList<>();
        chargesInstallmentFeePercentInterest
                .add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST.value));
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentInstallmentFeePercentInterestCharges = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name118)//
                .charges(chargesInstallmentFeePercentInterest)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentInstallmentFeePercentInterestCharges = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentInstallmentFeePercentInterestCharges);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_INTEREST_CHARGES,
                responseLoanProductsRequestLP2AdvPaymentInstallmentFeePercentInterestCharges);

        // LP2 with progressive loan schedule + horizontal
        // charges - Installment Fee Percentage Amount + Interest
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_INTEREST_CHARGES)
        final String name119 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_INTEREST_CHARGES.getName();
        final List<LoanProductChargeData> chargesInstallmentFeePercentAmountPlusInterest = new ArrayList<>();
        chargesInstallmentFeePercentAmountPlusInterest
                .add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST.value));
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentInstallmentFeePercentAmountPlusInterestCharges = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name119)//
                .charges(chargesInstallmentFeePercentAmountPlusInterest)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentInstallmentFeePercentAmountPlusInterestCharges = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentInstallmentFeePercentAmountPlusInterestCharges);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_PERCENT_AMOUNT_INTEREST_CHARGES,
                responseLoanProductsRequestLP2AdvPaymentInstallmentFeePercentAmountPlusInterestCharges);

        // LP2 with progressive loan schedule + horizontal
        // charges - Installment Fee All
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_ALL_CHARGES)
        final String name120 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_ALL_CHARGES.getName();
        final List<LoanProductChargeData> chargesInstallmentFeeAll = new ArrayList<>();
        chargesInstallmentFeeAll.add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_FLAT.value));
        chargesInstallmentFeeAll.add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT.value));
        chargesInstallmentFeeAll.add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST.value));
        chargesInstallmentFeeAll
                .add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_AMOUNT_PLUS_INTEREST.value));
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentInstallmentFeeAllCharges = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name120)//
                .charges(chargesInstallmentFeeAll)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestRateFrequencyType(3)//
                .maxInterestRatePerPeriod(10.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        final PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentInstallmentFeeAllCharges = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentInstallmentFeeAllCharges);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_ALL_CHARGES,
                responseLoanProductsRequestLP2AdvPaymentInstallmentFeeAllCharges);

        // LP2 with progressive loan schedule + horizontal + multidisbursal
        // charges - Installment Fee Flat + Interest %
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_FLAT_INTEREST_CHARGES_TRANCHE)
        final String name121 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_FLAT_INTEREST_CHARGES_TRANCHE.getName();
        final List<LoanProductChargeData> chargesInstallmentFeeFlatPlusInterest = new ArrayList<>();
        chargesInstallmentFeeFlatPlusInterest.add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_FLAT.value));
        chargesInstallmentFeeFlatPlusInterest
                .add(new LoanProductChargeData().id(ChargeProductType.LOAN_INSTALLMENT_FEE_PERCENTAGE_INTEREST.value));
        final PostLoanProductsRequest loanProductsRequestLP2AdvPaymentInstallmentFeeFlatPlusInterestChargesMultiDisburse = loanProductsRequestLP2AdvPaymentInstallmentFeePercentInterestCharges//
                .name(name121)//
                .shortName(loanProductsRequestFactory.generateShortNameSafely())//
                .charges(chargesInstallmentFeeFlatPlusInterest)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPaymentInstallmentFeeFlatPlusInterestChargesMultiDisburse = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPaymentInstallmentFeeFlatPlusInterestChargesMultiDisburse);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INSTALLMENT_FEE_FLAT_INTEREST_CHARGES_TRANCHE,
                responseLoanProductsRequestLP2AdvPaymentInstallmentFeeFlatPlusInterestChargesMultiDisburse);

        // LP2 with advanced payment allocation + progressive loan schedule + horizontal + interest Flat +
        // Multi-disbursement + 360/30
        // (LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE)
        final String name122 = DefaultLoanProduct.LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE.getName();
        final PostLoanProductsRequest loanProductsRequestAdvPaymentAllocationInterestFlat36030MultiDisbursement = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat()//
                .name(name122)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .installmentAmountInMultiplesOf(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestAdvPaymentAllocationInterestFlat36030MultiDisbursement = createLoanProductIdempotent(
                loanProductsRequestAdvPaymentAllocationInterestFlat36030MultiDisbursement);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE,
                responseLoanProductsRequestAdvPaymentAllocationInterestFlat36030MultiDisbursement);

        // LP2 with advanced payment allocation + progressive loan schedule + horizontal + interest Flat +
        // Multi-disbursement + allowPartialPeriodInterestCalculation disabled
        // (LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED)
        final String name123 = DefaultLoanProduct.LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED.getName();
        final PostLoanProductsRequest loanProductsRequestAdvInterestFlatMultiDisbPartialPeriodInterestCalculationDisabled = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat()//
                .name(name123)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.SAME_AS_REPAYMENT.value)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .installmentAmountInMultiplesOf(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .allowPartialPeriodInterestCalcualtion(false)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestAdvInterestFlatMultiDisbPartialPeriodInterestCalculationDisabled = createLoanProductIdempotent(
                loanProductsRequestAdvInterestFlatMultiDisbPartialPeriodInterestCalculationDisabled);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED,
                responseLoanProductsRequestAdvInterestFlatMultiDisbPartialPeriodInterestCalculationDisabled);

        // LP2 with advanced payment allocation + progressive loan schedule + horizontal + interest Flat +
        // Multi-disbursement + 360/30 + allowPartialPeriodInterestCalculation disabled
        // (LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED)
        final String name124 = DefaultLoanProduct.LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED.getName();
        final PostLoanProductsRequest loanProductsRequestAdvInterestFlat36030MultiDisbPartialPeriodInterestCalculationDisabled = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat()//
                .name(name124)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.SAME_AS_REPAYMENT.value)//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .installmentAmountInMultiplesOf(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .allowPartialPeriodInterestCalcualtion(false)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestAdvInterestFlat36030MultiDisbPartialPeriodInterestCalculationDisabled = createLoanProductIdempotent(
                loanProductsRequestAdvInterestFlat36030MultiDisbPartialPeriodInterestCalculationDisabled);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_INTEREST_FLAT_360_30_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED,
                responseLoanProductsRequestAdvInterestFlat36030MultiDisbPartialPeriodInterestCalculationDisabled);

        // LP2 with advanced payment allocation + progressive loan schedule + horizontal + interest Flat +
        // Multi-disbursement
        // (LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE)
        final String name125 = DefaultLoanProduct.LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE.getName();
        final PostLoanProductsRequest loanProductsRequestAdvPaymentAllocationInterestFlatMultiDisbursement = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat()//
                .name(name125)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .installmentAmountInMultiplesOf(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestAdvPaymentAllocationInterestFlatMultiDisbursement = createLoanProductIdempotent(
                loanProductsRequestAdvPaymentAllocationInterestFlatMultiDisbursement);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE,
                responseLoanProductsRequestAdvPaymentAllocationInterestFlatMultiDisbursement);

        // LP2 with Down-payment + advanced payment allocation + progressive loan schedule + horizontal + interest Flat
        // + Multi-disbursement + allowPartialPeriodInterestCalculation disabled
        // (LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED)
        final String name126 = DefaultLoanProduct.LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED
                .getName();
        final PostLoanProductsRequest loanProductsRequestDownPaymentAdvInterestFlatMultiDisbPartialPeriodInterestCalcDisabled = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestFlat()//
                .name(name126)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .enableAutoRepaymentForDownPayment(false)//
                .installmentAmountInMultiplesOf(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .allowPartialPeriodInterestCalcualtion(false)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestDownPaymentAdvInterestFlatMultiDisbPartPeriodIntCalcDisabled = createLoanProductIdempotent(
                loanProductsRequestDownPaymentAdvInterestFlatMultiDisbPartialPeriodInterestCalcDisabled);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_INTEREST_FLAT_ADV_PMT_ALLOC_MULTIDISBURSE_PART_PERIOD_CALC_DISABLED,
                responseLoanProductsRequestDownPaymentAdvInterestFlatMultiDisbPartPeriodIntCalcDisabled);

        // LP2 without Down-payment + interest recalculation disabled + advanced payment allocation + progressive loan
        // schedule + horizontal + allocation penalty first
        // (LP2_NO_INTEREST_RECALCULATION_ALLOCATION_PENALTY_FIRST)
        String name127 = DefaultLoanProduct.LP2_NO_INTEREST_RECALCULATION_ALLOCATION_PENALTY_FIRST.getName();
        PostLoanProductsRequest loanProductsRequestNoInterestRecalculationAllocationPenaltyFirst = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2()//
                .name(name127)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL")//
                .enableDownPayment(false)//
                .enableAutoRepaymentForDownPayment(null)//
                .disbursedAmountPercentageForDownPayment(null)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "LAST_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE)));//
        PostLoanProductsResponse responseLoanProductsRequestNoInterestRecalculationAllocationPenaltyFirst = createLoanProductIdempotent(
                loanProductsRequestNoInterestRecalculationAllocationPenaltyFirst);
        TestContext.INSTANCE.set(TestContextKey.LP2_NO_INTEREST_RECALCULATION_ALLOCATION_PENALTY_FIRST_RESPONSE,
                responseLoanProductsRequestNoInterestRecalculationAllocationPenaltyFirst);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // charge-off reasons to GL account mapping
        // + interest recalculation, buy down fees enabled
        final String name128 = DefaultLoanProduct.LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_CHARGE_OFF_REASON.getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesWithChargeOffReason = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2ChargeOffReasonToExpenseAccountMappingsWithBuyDownFee()//
                .name(name128)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesWithChargeOffReason = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesWithChargeOffReason);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_PROGRESSIVE_ADV_PYMNT_BUYDOWN_FEES_CHARGE_OFF_REASON,
                responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesWithChargeOffReason);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // capitalized income enabled; allow approved/disbursed amount over applied amount is enabled with percentage
        // type
        final String name129 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_APPROVED_OVER_APPLIED_CAPITALIZED_INCOME
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbApprovedOverAppliedCapitalizedIncome = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name129)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OverAppliedCalculationType.PERCENTAGE.value)//
                .overAppliedNumber(50)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbApprovedOverAppliedCapitalizedIncome = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbApprovedOverAppliedCapitalizedIncome);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_RECALC_EMI_360_30_MULTIDISB_APPROVED_OVER_APPLIED_CAPITALIZED_INCOME,
                responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbApprovedOverAppliedCapitalizedIncome);

        // LP1 with new due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy payment
        // strategy and with 12% FLAT interest
        // multidisbursal that expects tranche(s)
        // (LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT)
        String name130 = DefaultLoanProduct.LP1_MULTIDISBURSAL_EXPECTS_TRANCHES.getName();
        PostLoanProductsRequest loanProductsRequestMultidisbursalExpectTranches = loanProductsRequestFactory
                // .defaultLoanProductsRequestLP1InterestFlat()//
                // .interestType(INTEREST_TYPE_DECLINING_BALANCE)//
                .defaultLoanProductsRequestLP1() //
                .interestCalculationPeriodType(0)//
                .allowPartialPeriodInterestCalcualtion(false)//
                // .allowApprovedDisbursedAmountsOverApplied(false)//
                .name(name130)//
                .transactionProcessingStrategyCode(
                        TransactionProcessingStrategyCode.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST.value)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(false)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductMultidisbursalExpectTranches = createLoanProductIdempotent(
                loanProductsRequestMultidisbursalExpectTranches);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_MULTIDISBURSAL_EXPECTS_TRANCHES,
                responseLoanProductMultidisbursalExpectTranches);

        // LP2 with progressive loan schedule + horizontal +
        // interest Declining balance, Same as repayment period
        // interest recalculation enabled - daily
        // EMI + 360/30
        // multidisbursement - calculate partial period enabled
        // (LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_PARTIAL_PERIOD)
        final String name131 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_PARTIAL_PERIOD
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvPmtIntDeclSarpEmi3630IntRecalcDailyMutiDisbPartial = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name131)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .interestType(InterestType.DECLINING_BALANCE.getValue())//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.getValue())//
                .isInterestRecalculationEnabled(true)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY.value)//
                .preClosureInterestCalculationStrategy(PreClosureInterestCalculationRule.TILL_PRE_CLOSE_DATE.value)//
                .rescheduleStrategyMethod(AdvancePaymentsAdjustmentType.ADJUST_LAST_UNPAID_PERIOD.value)//
                .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE.value)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .allowPartialPeriodInterestCalcualtion(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPmtIntDeclSarpEmi3630IntRecalcDailyMutiDisbPartial = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPmtIntDeclSarpEmi3630IntRecalcDailyMutiDisbPartial);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_PARTIAL_PERIOD,
                responseLoanProductsRequestLP2AdvPmtIntDeclSarpEmi3630IntRecalcDailyMutiDisbPartial);

        // LP2 with progressive loan schedule + horizontal +
        // interest Declining balance, Same as repayment period
        // interest recalculation disabled
        // EMI + 360/30
        // multidisbursement - calculate partial period enabled
        // (LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_PARTIAL_PERIOD)
        final String name132 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_PARTIAL_PERIOD
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvPmtIntDeclSarpEmi3630NoIntRecalcMutiDisbPartial = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name132)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .interestType(InterestType.DECLINING_BALANCE.getValue())//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.getValue())//
                .isInterestRecalculationEnabled(false)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .allowPartialPeriodInterestCalcualtion(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPmtIntDeclSarpEmi3630NoIntRecalcMutiDisbPartial = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPmtIntDeclSarpEmi3630NoIntRecalcMutiDisbPartial);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_PARTIAL_PERIOD,
                responseLoanProductsRequestLP2AdvPmtIntDeclSarpEmi3630NoIntRecalcMutiDisbPartial);

        // LP2 with progressive loan schedule + horizontal +
        // interest Declining balance, Same as repayment period
        // interest recalculation disabled
        // EMI + 360/30
        // multidisbursement - calculate partial period disabled
        // (LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_NO_PARTIAL_PERIOD)
        final String name133 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_NO_PARTIAL_PERIOD
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvPmtIntDeclSarpEmi3630NoIntRecalcMutiDisbNoPartial = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name133)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .interestType(InterestType.DECLINING_BALANCE.getValue())//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.getValue())//
                .isInterestRecalculationEnabled(false)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .allowPartialPeriodInterestCalcualtion(false)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvPmtIntDeclSarpEmi3630NoIntRecalcMutiDisbNoPartial = createLoanProductIdempotent(
                loanProductsRequestLP2AdvPmtIntDeclSarpEmi3630NoIntRecalcMutiDisbNoPartial);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DECL_BAL_SARP_EMI_360_30_NO_INT_RECALC_MULTIDISB_NO_PARTIAL_PERIOD,
                responseLoanProductsRequestLP2AdvPmtIntDeclSarpEmi3630NoIntRecalcMutiDisbNoPartial);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // allow approved/disbursed amount over applied amount is enabled with percentage
        // multidisbursal loan that expects tranches
        // type
        final String name134 = DefaultLoanProduct.LP2_PROGRESSIVE_ADV_PYMNT_INTEREST_RECALC_360_30_MULTIDISB_OVER_APPLIED_EXPECTED_TRANCHES
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbApprovedOverAppliedExpectTranches = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name134)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OverAppliedCalculationType.PERCENTAGE.value)//
                .overAppliedNumber(50)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(false)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbApprovedOverAppliedExpectTranches = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbApprovedOverAppliedExpectTranches);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_PROGRESSIVE_ADV_PYMNT_INTEREST_RECALC_360_30_MULTIDISB_OVER_APPLIED_EXPECTED_TRANCHES,
                responseLoanProductsRequestLP2ProgressiveAdvPymnt36030InterestRecalcMultidisbApprovedOverAppliedExpectTranches);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // interestRecalculationCompoundingMethod = none
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // (LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE)
        // min interest rate / year 3 and max interest rate / year is 20
        String name135 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_MIN_INT_3_MAX_INT_20
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseMinInt3MaxInt20 = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name135)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .minInterestRatePerPeriod(3D)//
                .interestRatePerPeriod(12D) //
                .maxInterestRatePerPeriod(20D)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_YEAR).paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedPaymentInterest36030InterestRecalcDailyTillPreCloseMinInt3MaxInt20 = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyTillPrecloseMinInt3MaxInt20);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY_TILL_PRECLOSE_MIN_INT_3_MAX_INT_20,
                responseLoanProductsRequestLP2AdvancedPaymentInterest36030InterestRecalcDailyTillPreCloseMinInt3MaxInt20);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, buy down fees enabled, non-merchant
        final String name136 = DefaultLoanProduct.LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_NON_MERCHANT.getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesNonMerchant = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2BuyDownFees()//
                .name(name136)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .merchantBuyDownFee(false).buyDownExpenseAccountId(null);//
        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesNonMerchant = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesNonMerchant);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_PROGRESSIVE_ADV_PYMNT_BUYDOWN_FEES_NON_MERCHANT,
                responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesNonMerchant);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // charge-off reasons to GL account mapping
        // + interest recalculation, buy down fees enabled, non-merchant
        final String name137 = DefaultLoanProduct.LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_NON_MERCHANT_CHARGE_OFF_REASON
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesNonMerchantWithChargeOffReason = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2ChargeOffReasonToExpenseAccountMappingsWithBuyDownFee()//
                .name(name137)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .merchantBuyDownFee(false).buyDownExpenseAccountId(null);//
        PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesNonMerchantWithChargeOffReason = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesNonMerchantWithChargeOffReason);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_PROGRESSIVE_ADV_PYMNT_BUYDOWN_FEES_NON_MERCHANT_CHARGE_OFF_REASON,
                responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesNonMerchantWithChargeOffReason);

        // LP2 with progressive loan schedule + horizontal + interest recalculation daily EMI + 360/30 +
        // multidisbursement
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // allow approved/disbursed amount over applied amount is enabled with percentage type
        // (LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_EXPECT_TRANCHE_APPROVED_OVER_APPLIED)
        final String name138 = DefaultLoanProduct.LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_EXPECT_TRANCHE_APPROVED_OVER_APPLIED
                .getName();
        final PostLoanProductsRequest loanProductsRequestLP2AdvEmi36030IntRecalcDailyMultiDisbApprovedOverApplied = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name138)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OverAppliedCalculationType.PERCENTAGE.value)//
                .overAppliedNumber(50)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvEmi36030IntRecalcDailyMultiDisbApprovedOverApplied = createLoanProductIdempotent(
                loanProductsRequestLP2AdvEmi36030IntRecalcDailyMultiDisbApprovedOverApplied);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INT_DAILY_EMI_360_30_INT_RECALC_DAILY_MULTIDISB_EXPECT_TRANCHE_APPROVED_OVER_APPLIED,
                responseLoanProductsRequestLP2AdvEmi36030IntRecalcDailyMultiDisbApprovedOverApplied);

        // LP2 + interest recalculation + advanced custom payment allocation + progressive loan schedule + horizontal
        // charge-off behaviour - zero interest
        // LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_ZERO_CHARGE_OFF
        String name139 = DefaultLoanProduct.LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL_ZERO_CHARGE_OFF.getName();

        PostLoanProductsRequest loanProductsRequestAdvCustomPaymentAllocationProgressiveLoanScheduleZeroChargeOff = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .name(name139)//
                .supportedInterestRefundTypes(Arrays.asList("MERCHANT_ISSUED_REFUND", "PAYOUT_REFUND"))//
                .enableAccrualActivityPosting(true) //
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
                        createPaymentAllocation("GOODWILL_CREDIT", "REAMORTIZATION"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))) //
                .chargeOffBehaviour("ZERO_INTEREST");//
        PostLoanProductsResponse responseLoanProductsRequestAdvCustomPaymentAllocationProgressiveLoanScheduleZeroChargeOff = createLoanProductIdempotent(
                loanProductsRequestAdvCustomPaymentAllocationProgressiveLoanScheduleZeroChargeOff);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADVANCED_CUSTOM_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE_ZERO_CHARGE_OFF,
                responseLoanProductsRequestAdvCustomPaymentAllocationProgressiveLoanScheduleZeroChargeOff);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, buy down fees enabled
        // + Classification income map
        final String name140 = DefaultLoanProduct.LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_BUYDOWN_FEES_CLASSIFICATION_INCOME_MAP
                .getName();

        List<PostClassificationToIncomeAccountMappings> buydownfeeClassificationToIncomeAccountMappings = new ArrayList<>();
        PostClassificationToIncomeAccountMappings classificationToIncomeAccountMappings = new PostClassificationToIncomeAccountMappings();
        classificationToIncomeAccountMappings.setClassificationCodeValueId(25L);
        classificationToIncomeAccountMappings.setIncomeAccountId(10L);
        buydownfeeClassificationToIncomeAccountMappings.add(classificationToIncomeAccountMappings);

        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesClassificationIncomeMap = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2BuyDownFees()//
                .name(name140)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .buydownfeeClassificationToIncomeAccountMappings(buydownfeeClassificationToIncomeAccountMappings);//

        PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesClassificationIncomeMap = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesClassificationIncomeMap);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_PROGRESSIVE_ADV_PYMNT_BUYDOWN_FEES_CLASSIFICATION_INCOME_MAP,
                responseLoanProductsRequestLP2ProgressiveAdvPaymentBuyDownFeesClassificationIncomeMap);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + custom allocation capital
        // adjustment
        // + interest recalculation, preClosureInterestCalculationStrategy= till preclose,
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // capitalized income enabled + income type - fee
        // + Classification income map
        final String name141 = DefaultLoanProduct.LP2_PROGRESSIVE_ADV_PMNT_ALLOCATION_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC_CLASSIFICATION_INCOME_MAP
                .getName();

        List<PostClassificationToIncomeAccountMappings> capitalizedIncomeClassificationToIncomeAccountMappings = new ArrayList<>();
        PostClassificationToIncomeAccountMappings classificationToIncomeAccountMappingsCapitalizedIncome = new PostClassificationToIncomeAccountMappings();
        classificationToIncomeAccountMappingsCapitalizedIncome.setClassificationCodeValueId(24L);
        classificationToIncomeAccountMappingsCapitalizedIncome.setIncomeAccountId(15L);
        capitalizedIncomeClassificationToIncomeAccountMappings.add(classificationToIncomeAccountMappingsCapitalizedIncome);

        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPaymAllocCapitaizedIncomeClassificationIncomeMap = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2EmiCapitalizedIncome()//
                .name(name141)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("CAPITALIZED_INCOME_ADJUSTMENT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .capitalizedIncomeClassificationToIncomeAccountMappings(capitalizedIncomeClassificationToIncomeAccountMappings);//

        PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPaymAllocCapitaizedIncomeClassificationIncomeMap = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPaymAllocCapitaizedIncomeClassificationIncomeMap);

        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_PROGRESSIVE_ADV_PMNT_ALLOCATION_CAPITALIZED_INCOME_ADJ_CUSTOM_ALLOC_CLASSIFICATION_INCOME_MAP,
                responseLoanProductsRequestLP2ProgressiveAdvPaymAllocCapitaizedIncomeClassificationIncomeMap);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30
        // + interest recalculation, buy down fees enabled
        // + Write off reason expense map
        final String name142 = DefaultLoanProduct.LP2_PROGRESSIVE_ADVANCED_PAYMENT_ALLOCATION_WRITE_OFF_REASON_MAP.getName();
        final Long writeOffReasonCodeId = codeHelper.retrieveCodeByName("WriteOffReasons").getId();
        final CodeValue writeOffReasonCodeValueBadDebt = DefaultCodeValue.valueOf("BAD_DEBT");
        final CodeValue writeOffReasonCodeValueForgiven = DefaultCodeValue.valueOf("FORGIVEN");
        final CodeValue writeOffReasonCodeValueTest = DefaultCodeValue.valueOf("TEST");
        long writeOffReasonIdBadDebt = codeValueResolver.resolve(writeOffReasonCodeId, writeOffReasonCodeValueBadDebt);
        long writeOffReasonIdForgiven = codeValueResolver.resolve(writeOffReasonCodeId, writeOffReasonCodeValueForgiven);
        long writeOffReasonIdTest = codeValueResolver.resolve(writeOffReasonCodeId, writeOffReasonCodeValueTest);

        List<PostWriteOffReasonToExpenseAccountMappings> writeOffReasonToExpenseAccountMappings = new ArrayList<>();
        PostWriteOffReasonToExpenseAccountMappings writeOffReasonToExpenseAccountMappingsBadDebt = new PostWriteOffReasonToExpenseAccountMappings();
        writeOffReasonToExpenseAccountMappingsBadDebt.setWriteOffReasonCodeValueId(String.valueOf(writeOffReasonIdBadDebt));
        writeOffReasonToExpenseAccountMappingsBadDebt.setExpenseAccountId("12"); // Credit Loss/Bad Debt
        PostWriteOffReasonToExpenseAccountMappings writeOffReasonToExpenseAccountMappingsForgiven = new PostWriteOffReasonToExpenseAccountMappings();
        writeOffReasonToExpenseAccountMappingsForgiven.setWriteOffReasonCodeValueId(String.valueOf(writeOffReasonIdForgiven));
        writeOffReasonToExpenseAccountMappingsForgiven.setExpenseAccountId("23"); // Buy Down Expense
        PostWriteOffReasonToExpenseAccountMappings writeOffReasonToExpenseAccountMappingsTest = new PostWriteOffReasonToExpenseAccountMappings();
        writeOffReasonToExpenseAccountMappingsTest.setWriteOffReasonCodeValueId(String.valueOf(writeOffReasonIdTest));
        writeOffReasonToExpenseAccountMappingsTest.setExpenseAccountId("16"); // Written off

        writeOffReasonToExpenseAccountMappings.add(writeOffReasonToExpenseAccountMappingsBadDebt);
        writeOffReasonToExpenseAccountMappings.add(writeOffReasonToExpenseAccountMappingsForgiven);
        writeOffReasonToExpenseAccountMappings.add(writeOffReasonToExpenseAccountMappingsTest);

        final PostLoanProductsRequest loanProductsRequestLP2ProgressiveAdvPaymentWriteOffReasonMap = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2BuyDownFees()//
                .name(name142)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .loanScheduleType("PROGRESSIVE") //
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .writeOffReasonsToExpenseMappings(writeOffReasonToExpenseAccountMappings);//

        final PostLoanProductsResponse responseLoanProductsRequestLP2ProgressiveAdvPaymentWriteOffReasonMap = createLoanProductIdempotent(
                loanProductsRequestLP2ProgressiveAdvPaymentWriteOffReasonMap);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_PROGRESSIVE_ADV_PYMNT_WRITE_OFF_REASON_MAP,
                responseLoanProductsRequestLP2ProgressiveAdvPaymentWriteOffReasonMap);

        // LP1 with 12% Flat interest, interest period: Same as repayment,
        // Interest recalculation-Same as repayment, Multi-disbursement
        String name143 = DefaultLoanProduct.LP1_INTEREST_FLAT_SAR_RECALCULATION_SAME_AS_REPAYMENT_ACTUAL_ACTUAL_MULTIDISB.getName();
        PostLoanProductsRequest loanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursement = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name143)//
                .interestType(INTEREST_TYPE_FLAT)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.SAME_AS_REPAYMENT.value)//
                .installmentAmountInMultiplesOf(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OverAppliedCalculationType.PERCENTAGE.value)//
                .overAppliedNumber(50);//
        PostLoanProductsResponse responseInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursement = createLoanProductIdempotent(
                loanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursement);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_SAR_RECALCULATION_SAME_AS_REPAYMENT_ACTUAL_ACTUAL_MULTIDISB,
                responseInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursement);

        // LP1 with 12% Flat interest, interest period: Same as repayment,
        // Interest recalculation-Daily, Multi-disbursement
        String name144 = DefaultLoanProduct.LP1_INTEREST_FLAT_SAR_RECALCULATION_DAILY_360_30_APPROVED_OVER_APPLIED_MULTIDISB.getName();
        PostLoanProductsRequest loanProductsRequestInterestFlatSaRRecalculationDailyMultiDisbursement = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name144)//
                .interestType(INTEREST_TYPE_FLAT)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY.value)//
                .recalculationRestFrequencyInterval(1)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .installmentAmountInMultiplesOf(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OverAppliedCalculationType.PERCENTAGE.value)//
                .overAppliedNumber(50);//
        PostLoanProductsResponse responseLoanProductsRequestInterestFlatSaRRecalculationDailyMultiDisbursement = createLoanProductIdempotent(
                loanProductsRequestInterestFlatSaRRecalculationDailyMultiDisbursement);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_SAR_RECALCULATION_DAILY_360_30_APPROVED_OVER_APPLIED_MULTIDISB,
                responseLoanProductsRequestInterestFlatSaRRecalculationDailyMultiDisbursement);

        // LP1 with 12% Flat interest, interest period: Daily, Interest recalculation-Daily,
        // Multi-disbursement
        String name145 = DefaultLoanProduct.LP1_INTEREST_FLAT_DAILY_RECALCULATION_DAILY_360_30_MULTIDISB.getName();
        PostLoanProductsRequest loanProductsRequestInterestFlatDailyRecalculationDSameAsRepaymentMultiDisbursement = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name145)//
                .interestType(INTEREST_TYPE_FLAT)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value)//
                .allowPartialPeriodInterestCalcualtion(false)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY.value)//
                .recalculationRestFrequencyInterval(1)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .installmentAmountInMultiplesOf(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestInterestFlatDailyRecalculationDSameAsRepaymentMultiDisbursement = createLoanProductIdempotent(
                loanProductsRequestInterestFlatDailyRecalculationDSameAsRepaymentMultiDisbursement);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_DAILY_RECALCULATION_DAILY_360_30_MULTIDISB,
                responseLoanProductsRequestInterestFlatDailyRecalculationDSameAsRepaymentMultiDisbursement);

        // LP1 with 12% Flat interest, interest period: Daily, Interest recalculation-Daily
        // Multi-disbursement with auto down payment
        String name146 = DefaultLoanProduct.LP1_INTEREST_FLAT_SAR_RECALCULATION_SAME_AS_REPAYMENT_MULTIDISB_AUTO_DOWNPAYMENT.getName();
        PostLoanProductsRequest loanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursementAUtoDownPayment = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(name146)//
                .interestType(INTEREST_TYPE_FLAT)//
                .installmentAmountInMultiplesOf(null)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.SAME_AS_REPAYMENT.value)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .enableDownPayment(true)//
                .enableAutoRepaymentForDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursementAUtoDownPayment = createLoanProductIdempotent(
                loanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursementAUtoDownPayment);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_SAR_RECALCULATION_SAME_AS_REPAYMENT_MULTIDISB_AUTO_DOWNPAYMENT,
                responseLoanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursementAUtoDownPayment);

        // LP2 advanced custom payment allocation + progressive loan schedule + horizontal + interest recalculation
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        String name147 = DefaultLoanProduct.LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY
                .getName();
        PostLoanProductsRequest loanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmi36030InterestRecalculationDaily = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name147)//
                .supportedInterestRefundTypes(supportedInterestRefundTypes) //
                .installmentAmountInMultiplesOf(null) //
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .enableAccrualActivityPosting(true) //
                .chargeOffBehaviour(ZERO_INTEREST.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "LAST_INSTALLMENT",
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
                        createPaymentAllocation("GOODWILL_CREDIT", "REAMORTIZATION",
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
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT",
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
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE) //
                ));//
        PostLoanProductsResponse responseLoanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmi36030InterestRecalculationDaily = createLoanProductIdempotent(
                loanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmi36030InterestRecalculationDaily);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_CUSTOM_PMT_ALLOC_PROGRESSIVE_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_DAILY,
                responseLoanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmi36030InterestRecalculationDaily);

        // LP2 with progressive loan schedule + horizontal + interest EMI + 360/30 + multidisbursement +
        // contract termination with interest recognition
        // (LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION_INT_RECOGNITION)
        final String name148 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION_INT_RECOGNITION
                .getName();

        final PostLoanProductsRequest loanProductsRequestAdvCustomContractTerminationProgressiveLoanScheduleIntRecalcRecog = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2InterestDailyRecalculation()//
                .interestRecognitionOnDisbursementDate(true) //
                .name(name148)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT",
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL, //
                                LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")));//
        PostLoanProductsResponse responseLoanProductsRequestAdvCustomContractTerminationProgressiveLoanScheduleIntRecalcRecog = createLoanProductIdempotent(
                loanProductsRequestAdvCustomContractTerminationProgressiveLoanScheduleIntRecalcRecog);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_CONTRACT_TERMINATION_INT_RECOGNITION,
                responseLoanProductsRequestAdvCustomContractTerminationProgressiveLoanScheduleIntRecalcRecog);

        // (LP1_WITH_OVERRIDES) - Loan product with all attribute overrides ENABLED
        final String nameWithOverrides = DefaultLoanProduct.LP1_WITH_OVERRIDES.getName();
        final PostLoanProductsRequest loanProductsRequestWithOverrides = loanProductsRequestFactory.defaultLoanProductsRequestLP1() //
                .name(nameWithOverrides) //
                .interestRatePerPeriod(1.0) //
                .maxInterestRatePerPeriod(30.0) //
                .inArrearsTolerance(10) //
                .graceOnPrincipalPayment(1) //
                .graceOnInterestPayment(1) //
                .graceOnArrearsAgeing(3) //
                .numberOfRepayments(6) //
                .allowAttributeOverrides(new AllowAttributeOverrides() //
                        .amortizationType(true) //
                        .interestType(true) //
                        .transactionProcessingStrategyCode(true) //
                        .interestCalculationPeriodType(true) //
                        .inArrearsTolerance(true) //
                        .repaymentEvery(true) //
                        .graceOnPrincipalAndInterestPayment(true) //
                        .graceOnArrearsAgeing(true));
        final PostLoanProductsResponse responseWithOverrides = createLoanProductIdempotent(loanProductsRequestWithOverrides);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_WITH_OVERRIDES, responseWithOverrides);

        // (LP1_NO_OVERRIDES) - Loan product with all attribute overrides DISABLED
        final String nameNoOverrides = DefaultLoanProduct.LP1_NO_OVERRIDES.getName();
        final PostLoanProductsRequest loanProductsRequestNoOverrides = loanProductsRequestFactory.defaultLoanProductsRequestLP1() //
                .name(nameNoOverrides) //
                .interestRatePerPeriod(1.0) //
                .maxInterestRatePerPeriod(30.0) //
                .inArrearsTolerance(10) //
                .graceOnPrincipalPayment(1) //
                .graceOnInterestPayment(1) //
                .graceOnArrearsAgeing(3) //
                .numberOfRepayments(6) //
                .allowAttributeOverrides(new AllowAttributeOverrides() //
                        .amortizationType(false) //
                        .interestType(false) //
                        .transactionProcessingStrategyCode(false) //
                        .interestCalculationPeriodType(false) //
                        .inArrearsTolerance(false) //
                        .repaymentEvery(false) //
                        .graceOnPrincipalAndInterestPayment(false) //
                        .graceOnArrearsAgeing(false));
        final PostLoanProductsResponse responseNoOverrides = createLoanProductIdempotent(loanProductsRequestNoOverrides);
        TestContext.INSTANCE.set(TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_NO_OVERRIDES, responseNoOverrides);

        // LP2 advanced custom payment allocation + progressive loan schedule + horizontal + interest recalculation
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        String name149 = DefaultLoanProduct.LP2_ADV_CUSTOM_PMT_ALLOC_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALC_ZERO_CHARGE_OFF_ACCRUAL
                .getName();
        PostLoanProductsRequest loanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmiActualInterestRecalcZeroChargeOffAccruals = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name149)//
                .supportedInterestRefundTypes(supportedInterestRefundTypes).installmentAmountInMultiplesOf(null) //
                .daysInYearType(DaysInYearType.ACTUAL.value)//
                .daysInMonthType(DaysInMonthType.ACTUAL.value)//
                .daysInYearCustomStrategy(FEB_29_PERIOD_ONLY.getValue()).isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .enableAccrualActivityPosting(true) //
                .chargeOffBehaviour(ZERO_INTEREST.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"))); //
        PostLoanProductsResponse responseLoanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmiActualInterestRecalcZeroChargeOffAccruals = createLoanProductIdempotent(
                loanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmiActualInterestRecalcZeroChargeOffAccruals);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_CUSTOM_PMT_ALLOC_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALC_ZERO_CHARGE_OFF_ACCRUAL,
                responseLoanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmiActualInterestRecalcZeroChargeOffAccruals);

        // LP2 advanced + progressive loan schedule + horizontal + interest recalculation
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        PostLoanProductsRequest loanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmiActualInterestRecalcZeroChargeOffChargebackAccruals = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(DefaultLoanProduct.LP2_ADV_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALC_ZERO_CHARGE_OF_ACCRUAL.getName())//
                .supportedInterestRefundTypes(supportedInterestRefundTypes).installmentAmountInMultiplesOf(null) //
                .daysInYearType(DaysInYearType.ACTUAL.value)//
                .daysInMonthType(DaysInMonthType.ACTUAL.value)//
                .daysInYearCustomStrategy(FEB_29_PERIOD_ONLY.getValue()).isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .enableAccrualActivityPosting(true) //
                .chargeOffBehaviour(ZERO_INTEREST.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"))); //
        PostLoanProductsResponse responseLoanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmiActualInterestRecalcZeroChargeOffChargebackAccruals = createLoanProductIdempotent(
                loanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmiActualInterestRecalcZeroChargeOffChargebackAccruals);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_INTEREST_DAILY_EMI_ACTUAL_ACTUAL_INTEREST_RECALC_ZERO_CHARGE_OF_ACCRUAL,
                responseLoanProductsResponseAdvCustomPaymentAllocationProgressiveLoanInterestDailyEmiActualInterestRecalcZeroChargeOffChargebackAccruals);

        // LP1 with 12% Flat interest, interest period: Daily, Interest recalculation- Same as repayment
        // Multi-disbursement that expects tranches
        PostLoanProductsRequest loanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursementExpectsTranches = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone()//
                .name(DefaultLoanProduct.LP1_INTEREST_FLAT_DAILY_RECALCULATION_SAR_MULTIDISB_EXPECT_TRANCHES.getName())//
                .interestType(INTEREST_TYPE_FLAT)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value)//
                .allowPartialPeriodInterestCalcualtion(false)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.SAME_AS_REPAYMENT.value)//
                .recalculationRestFrequencyInterval(1)//
                .installmentAmountInMultiplesOf(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(false)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursementExpectsTranches = createLoanProductIdempotent(
                loanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursementExpectsTranches);
        TestContext.INSTANCE.set(TestContextKey.LP1_INTEREST_FLAT_DAILY_RECALCULATION_SAR_MULTIDISB_EXPECT_TRANCHES,
                responseLoanProductsRequestInterestFlatSaRRecalculationSameAsRepaymentMultiDisbursementExpectsTranches);

        // LP2 + zero-interest chargeOff behaviour + progressive loan schedule + horizontal
        // (LP2_ADV_PYMNT_360_30_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY)
        final PostLoanProductsRequest loanProductsRequestAdvZeroInterestChargeOffBehaviourAccrualActivity = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(DefaultLoanProduct.LP2_ADV_PYMNT_360_30_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY.getName())//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT")))
                .enableAccrualActivityPosting(true)//
                .chargeOffBehaviour("ZERO_INTEREST");//
        final PostLoanProductsResponse responseLoanProductsRequestAdvZeroInterestChargeOffBehaviourAccrualActivity = createLoanProductIdempotent(
                loanProductsRequestAdvZeroInterestChargeOffBehaviourAccrualActivity);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_360_30_ZERO_INTEREST_CHARGE_OFF_ACCRUAL_ACTIVITY,
                responseLoanProductsRequestAdvZeroInterestChargeOffBehaviourAccrualActivity);

        // LP1 with 12% Flat interest, interest period: Daily, Interest recalculation- Actual
        // Multi-disbursement that expects tranches
        PostLoanProductsRequest loanProductsRequestInterestFlatActualActualMultiDisbursementExpectsTranches = loanProductsRequestFactory
                .defaultLoanProductsRequestLP1InterestFlat()//
                .name(DefaultLoanProduct.LP1_INTEREST_FLAT_DAILY_ACTUAL_ACTUAL_MULTIDISB_EXPECT_TRANCHES.getName())//
                .interestType(INTEREST_TYPE_FLAT)//
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value)//
                .allowPartialPeriodInterestCalcualtion(false)//
                .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY.value)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyInterval(1)//
                .installmentAmountInMultiplesOf(null)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(false)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestInterestFlatActualActualMultiDisbursementExpectsTranches = createLoanProductIdempotent(
                loanProductsRequestInterestFlatActualActualMultiDisbursementExpectsTranches);
        TestContext.INSTANCE.set(TestContextKey.LP1_INTEREST_FLAT_DAILY_ACTUAL_ACTUAL_MULTIDISB_EXPECT_TRANCHES,
                responseLoanProductsRequestInterestFlatActualActualMultiDisbursementExpectsTranches);

        // LP2 with progressive loan schedule + horizontal + interest recalculation daily EMI + 360/30 +
        // multidisbursement
        // Frequency for recalculate Outstanding Principal: Daily, Frequency Interval for recalculation: 1
        // chargeback - interest, fee, principal, penalty
        String name151 = DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_CHARGEBACK
                .getName();
        PostLoanProductsRequest loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburseChargeback = loanProductsRequestFactory
                .defaultLoanProductsRequestLP2Emi()//
                .name(name151)//
                .daysInYearType(DaysInYearType.DAYS360.value)//
                .daysInMonthType(DaysInMonthType.DAYS30.value)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(4)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .creditAllocation(List.of(//
                        createCreditAllocation("CHARGEBACK", List.of("INTEREST", "FEE", "PRINCIPAL", "PENALTY"))//
                ))//
                .paymentAllocation(List.of(//
                        createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT")))//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0);//
        PostLoanProductsResponse responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburseChargeback = createLoanProductIdempotent(
                loanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburseChargeback);
        TestContext.INSTANCE.set(
                TestContextKey.DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALC_DAILY_MULTIDISBURSE_CHARGEBACK,
                responseLoanProductsRequestLP2AdvancedpaymentInterestEmi36030InterestRecalcDailyMultiDisburseChargeback);
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

    public static AdvancedPaymentData createPaymentAllocationPenFeeIntPrincipal(String transactionType,
            String futureInstallmentAllocationRule, LoanProductPaymentAllocationRule.AllocationTypesEnum... rules) {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType(transactionType);
        advancedPaymentData.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);

        List<PaymentAllocationOrder> paymentAllocationOrders;
        if (rules.length == 0) {
            paymentAllocationOrders = getPaymentAllocationOrder(//
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PENALTY, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_FEE, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_INTEREST, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.PAST_DUE_PRINCIPAL, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PENALTY, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_FEE, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_INTEREST, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.DUE_PRINCIPAL, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PENALTY, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_FEE, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_INTEREST, //
                    LoanProductPaymentAllocationRule.AllocationTypesEnum.IN_ADVANCE_PRINCIPAL);//
        } else {
            paymentAllocationOrders = getPaymentAllocationOrder(rules);
        }

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);

        return advancedPaymentData;
    }

    public static AdvancedPaymentData editPaymentAllocationFutureInstallment(String transactionType, String futureInstallmentAllocationRule,
            List<PaymentAllocationOrder> paymentAllocationOrder) {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType(transactionType);
        advancedPaymentData.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);
        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrder);

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

    private PostLoanProductsResponse createLoanProductIdempotent(PostLoanProductsRequest loanProductRequest) {
        String productName = loanProductRequest.getName();
        log.debug("Attempting to create loan product: {}", productName);
        try {
            List<GetLoanProductsResponse> existingProducts = fineractClient.loanProducts().retrieveAllLoanProducts(Map.of());
            GetLoanProductsResponse existingProduct = existingProducts.stream().filter(p -> productName.equals(p.getName())).findFirst()
                    .orElse(null);

            if (existingProduct != null) {
                log.debug("Loan product '{}' already exists with ID: {}", productName, existingProduct.getId());
                PostLoanProductsResponse response = new PostLoanProductsResponse();
                response.setResourceId(existingProduct.getId());
                return response;
            }
        } catch (Exception e) {
            log.warn("Error checking if loan product '{}' exists", productName, e);
        }

        log.debug("Creating new loan product: {}", productName);
        try {
            PostLoanProductsResponse response = ok(() -> fineractClient.loanProducts().createLoanProduct(loanProductRequest, Map.of()));
            log.debug("Successfully created loan product '{}' with ID: {}", productName, response.getResourceId());
            return response;
        } catch (Exception e) {
            log.error("FAILED to create loan product '{}'", productName, e);
            throw e;
        }
    }
}
