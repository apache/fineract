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
package org.apache.fineract.integrationtests.client.feign.modules;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.AmortizationType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInMonthType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInYearType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestCalculationPeriodType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestRateFrequencyType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RescheduleStrategyMethod;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;

public interface LoanProductTemplates {

    Long getAssetAccountId(String accountName);

    Long getLiabilityAccountId(String accountName);

    Long getIncomeAccountId(String accountName);

    Long getExpenseAccountId(String accountName);

    default PostLoanProductsRequest onePeriod30DaysNoInterest() {
        return new PostLoanProductsRequest().name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Loan Product Description")//
                .includeInBorrowerCycle(false)//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(100000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod(0.0)//
                .interestRatePerPeriod(0.0)//
                .maxInterestRatePerPeriod(100.0)//
                .interestRateFrequencyType(InterestRateFrequencyType.MONTHS)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(RepaymentFrequencyType.DAYS_L)//
                .amortizationType(AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(InterestType.DECLINING_BALANCE)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(
                        LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.toString())//
                .daysInYearType(DaysInYearType.ACTUAL)//
                .daysInMonthType(DaysInMonthType.ACTUAL)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .isInterestRecalculationEnabled(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(true)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))//
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(Collections.emptyList())//
                .accountingRule(3)//
                .fundSourceAccountId(getLiabilityAccountId("fundSource"))//
                .loanPortfolioAccountId(getAssetAccountId("loansReceivable"))//
                .transfersInSuspenseAccountId(getAssetAccountId("suspense"))//
                .interestOnLoanAccountId(getIncomeAccountId("interestIncome"))//
                .incomeFromFeeAccountId(getIncomeAccountId("feeIncome"))//
                .incomeFromPenaltyAccountId(getIncomeAccountId("penaltyIncome"))//
                .incomeFromRecoveryAccountId(getIncomeAccountId("recoveries"))//
                .writeOffAccountId(getExpenseAccountId("writtenOff"))//
                .overpaymentLiabilityAccountId(getLiabilityAccountId("overpayment"))//
                .receivableInterestAccountId(getAssetAccountId("interestReceivable"))//
                .receivableFeeAccountId(getAssetAccountId("feeReceivable"))//
                .receivablePenaltyAccountId(getAssetAccountId("penaltyReceivable"))//
                .goodwillCreditAccountId(getExpenseAccountId("goodwillExpense"))//
                .incomeFromGoodwillCreditInterestAccountId(getIncomeAccountId("interestIncomeChargeOff"))//
                .incomeFromGoodwillCreditFeesAccountId(getIncomeAccountId("feeChargeOff"))//
                .incomeFromGoodwillCreditPenaltyAccountId(getIncomeAccountId("feeChargeOff"))//
                .incomeFromChargeOffInterestAccountId(getIncomeAccountId("interestIncomeChargeOff"))//
                .incomeFromChargeOffFeesAccountId(getIncomeAccountId("feeChargeOff"))//
                .incomeFromChargeOffPenaltyAccountId(getIncomeAccountId("penaltyChargeOff"))//
                .chargeOffExpenseAccountId(getExpenseAccountId("chargeOff"))//
                .chargeOffFraudExpenseAccountId(getExpenseAccountId("chargeOffFraud"))//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale("en_GB")//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50);
    }

    default PostLoanProductsRequest fourInstallmentsCumulative() {
        return fourInstallmentsCumulativeTemplate().loanScheduleType(LoanScheduleType.CUMULATIVE.toString());
    }

    default PostLoanProductsRequest fourInstallmentsProgressive() {
        return fourInstallmentsCumulativeTemplate().loanScheduleType("PROGRESSIVE").loanScheduleProcessingType("HORIZONTAL")
                .rescheduleStrategyMethod(RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD);
    }

    default PostLoanProductsRequest fourInstallmentsProgressiveWithCapitalizedIncome() {
        return fourInstallmentsProgressive().enableIncomeCapitalization(true)//
                .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)//
                .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)//
                .deferredIncomeLiabilityAccountId(getLiabilityAccountId("deferredIncomeLiability"))//
                .incomeFromCapitalizationAccountId(getIncomeAccountId("feeIncome"))//
                .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE);
    }

    default PostLoanProductsRequest fourInstallmentsCumulativeTemplate() {
        return new PostLoanProductsRequest().name(Utils.uniqueRandomStringGenerator("4I_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("4 installment product")//
                .includeInBorrowerCycle(false)//
                .useBorrowerCycle(false)//
                .currencyCode("EUR")//
                .digitsAfterDecimal(2)//
                .principal(1000.0)//
                .minPrincipal(100.0)//
                .maxPrincipal(10000.0)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(10.0)//
                .minInterestRatePerPeriod(0.0)//
                .maxInterestRatePerPeriod(120.0)//
                .interestRateFrequencyType(InterestRateFrequencyType.YEARS)//
                .isLinkedToFloatingInterestRates(false)//
                .allowVariableInstallments(false)//
                .amortizationType(AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .allowPartialPeriodInterestCalcualtion(false)//
                .creditAllocation(List.of())//
                .overdueDaysForNPA(179)//
                .daysInMonthType(DaysInMonthType.DAYS_30)//
                .daysInYearType(DaysInYearType.DAYS_360)//
                .isInterestRecalculationEnabled(false)//
                .canDefineInstallmentAmount(true)//
                .repaymentStartDateType(1)//
                .charges(List.of())//
                .principalVariationsForBorrowerCycle(List.of())//
                .interestRateVariationsForBorrowerCycle(List.of())//
                .numberOfRepaymentVariationsForBorrowerCycle(List.of())//
                .accountingRule(3)//
                .canUseForTopup(false)//
                .fundSourceAccountId(getLiabilityAccountId("fundSource"))//
                .loanPortfolioAccountId(getAssetAccountId("loansReceivable"))//
                .transfersInSuspenseAccountId(getAssetAccountId("suspense"))//
                .interestOnLoanAccountId(getIncomeAccountId("interestIncome"))//
                .incomeFromFeeAccountId(getIncomeAccountId("feeIncome"))//
                .incomeFromPenaltyAccountId(getIncomeAccountId("penaltyIncome"))//
                .incomeFromRecoveryAccountId(getIncomeAccountId("recoveries"))//
                .writeOffAccountId(getExpenseAccountId("writtenOff"))//
                .overpaymentLiabilityAccountId(getLiabilityAccountId("overpayment"))//
                .receivableInterestAccountId(getAssetAccountId("interestReceivable"))//
                .receivableFeeAccountId(getAssetAccountId("feeReceivable"))//
                .receivablePenaltyAccountId(getAssetAccountId("penaltyReceivable"))//
                .goodwillCreditAccountId(getExpenseAccountId("goodwillExpense"))//
                .incomeFromGoodwillCreditInterestAccountId(getIncomeAccountId("interestIncomeChargeOff"))//
                .incomeFromGoodwillCreditFeesAccountId(getIncomeAccountId("feeChargeOff"))//
                .incomeFromGoodwillCreditPenaltyAccountId(getIncomeAccountId("feeChargeOff"))//
                .incomeFromChargeOffInterestAccountId(getIncomeAccountId("interestIncomeChargeOff"))//
                .incomeFromChargeOffFeesAccountId(getIncomeAccountId("feeChargeOff"))//
                .incomeFromChargeOffPenaltyAccountId(getIncomeAccountId("penaltyChargeOff"))//
                .chargeOffExpenseAccountId(getExpenseAccountId("chargeOff"))//
                .chargeOffFraudExpenseAccountId(getExpenseAccountId("chargeOffFraud"))//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale("en")//
                .enableAccrualActivityPosting(false)//
                .multiDisburseLoan(false)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50)//
                .principalThresholdForLastInstallment(50)//
                .holdGuaranteeFunds(false)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))//
                .isEqualAmortization(false)//
                .enableDownPayment(false)//
                .enableInstallmentLevelDelinquency(false)//
                .transactionProcessingStrategyCode(
                        LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY);
    }

    default PostLoanProductsRequest customizeProduct(PostLoanProductsRequest template,
            Function<PostLoanProductsRequest, PostLoanProductsRequest> customizer) {
        return customizer.apply(template);
    }
}
