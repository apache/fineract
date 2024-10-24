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
package org.apache.fineract.test.factory;

import static org.apache.fineract.test.data.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeToGLAccountMapper;
import org.apache.fineract.client.models.GetLoanFeeToIncomeAccountMappings;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.test.data.AccountingRule;
import org.apache.fineract.test.data.AdvancePaymentsAdjustmentType;
import org.apache.fineract.test.data.AmortizationType;
import org.apache.fineract.test.data.DaysInMonthType;
import org.apache.fineract.test.data.DaysInYearType;
import org.apache.fineract.test.data.DelinquencyBucket;
import org.apache.fineract.test.data.FundId;
import org.apache.fineract.test.data.InterestCalculationPeriodTime;
import org.apache.fineract.test.data.InterestRateFrequencyType;
import org.apache.fineract.test.data.InterestRecalculationCompoundingMethod;
import org.apache.fineract.test.data.InterestType;
import org.apache.fineract.test.data.PreClosureInterestCalculationRule;
import org.apache.fineract.test.data.RecalculationCompoundingFrequencyType;
import org.apache.fineract.test.data.RecalculationRestFrequencyType;
import org.apache.fineract.test.data.RepaymentFrequencyType;
import org.apache.fineract.test.data.TransactionProcessingStrategyCode;
import org.apache.fineract.test.data.accounttype.AccountTypeResolver;
import org.apache.fineract.test.data.accounttype.DefaultAccountType;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.helper.Utils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanProductsRequestFactory {

    private final PaymentTypeResolver paymentTypeResolver;
    private final AccountTypeResolver accountTypeResolver;

    public static final String NAME_PREFIX = "LP1-";
    public static final String NAME_PREFIX_LP2 = "LP2-";
    public static final String NAME_PREFIX_INTEREST_FLAT = "LP1InterestFlat-";
    public static final String NAME_PREFIX_INTEREST_FLAT_LP2 = "LP2InterestFlat-";
    public static final String NAME_PREFIX_INTEREST_DECLINING = "LP1InterestDeclining-";
    public static final String NAME_PREFIX_INTEREST_DECLINING_RECALCULATION = "LP1InterestDecliningRecalculation-";
    public static final String NAME_PREFIX_INTEREST_RECALCULATION_WITH_DOWN_PAYMENT_LP3 = "LP3InterestRecalculationWithDownPayment-";
    public static final String NAME_PREFIX_LP2_EMI = "LP2Emi-";
    public static final String SHORT_NAME_PREFIX = "p";
    public static final String SHORT_NAME_PREFIX_INTEREST = "i";
    public static final String SHORT_NAME_PREFIX_EMI = "e";
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String LOCALE_EN = "en";
    public static final String DESCRIPTION = "LP1 product";
    public static final String DESCRIPTION_LP2 = "LP2 product";
    public static final String DESCRIPTION_INTEREST_FLAT = "LP1 product with 12% interest - FLAT";
    public static final String DESCRIPTION_INTEREST_FLAT_LP2 = "LP2 product with 12% interest - FLAT";
    public static final String DESCRIPTION_INTEREST_DECLINING = "LP1 product with 12% interest - DECLINING BALANCE";
    public static final String DESCRIPTION_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY = "LP1-1MONTH with 12% DECLINING BALANCE interest, interest period: Daily, Interest recalculation-Monthly, Compounding:Interest";
    public static final String DESCRIPTION_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE = "LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest recalculation-Daily, Compounding:none";
    public static final String DESCRIPTION_LP2_EMI = "LP2 product with EMI";
    public static final Long FUND_ID = FundId.LENDER_A.value;
    public static final String CURRENCY_CODE = "EUR";
    public static final Integer INTEREST_RATE_FREQUENCY_TYPE_MONTH = InterestRateFrequencyType.MONTH.value;
    public static final Integer INTEREST_RATE_FREQUENCY_TYPE_YEAR = InterestRateFrequencyType.YEAR.value;
    public static final Long REPAYMENT_FREQUENCY_TYPE_DAYS = RepaymentFrequencyType.DAYS.value.longValue();
    public static final Long REPAYMENT_FREQUENCY_TYPE_MONTHS = RepaymentFrequencyType.MONTHS.value.longValue();
    public static final Integer AMORTIZATION_TYPE = AmortizationType.EQUAL_INSTALLMENTS.value;
    public static final Integer INTEREST_TYPE_DECLINING_BALANCE = InterestType.DECLINING_BALANCE.value;
    public static final Integer INTEREST_TYPE_FLAT = InterestType.FLAT.value;
    public static final Integer INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT = InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value;
    public static final Integer INTEREST_CALCULATION_PERIOD_TYPE_DAILY = InterestCalculationPeriodTime.DAILY.value;
    public static final String TRANSACTION_PROCESSING_STRATEGY_CODE = TransactionProcessingStrategyCode.PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER.value;
    public static final String TRANSACTION_PROCESSING_STRATEGY_CODE_ADVANCED = TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION.value;
    public static final Integer DAYS_IN_YEAR_TYPE = DaysInYearType.ACTUAL.value;
    public static final Integer DAYS_IN_YEAR_TYPE_360 = DaysInYearType.DAYS360.value;
    public static final Integer DAYS_IN_MONTH_TYPE = DaysInMonthType.ACTUAL.value;
    public static final Integer DAYS_IN_MONTH_TYPE_30 = DaysInMonthType.DAYS30.value;
    public static final Integer LOAN_ACCOUNTING_RULE = AccountingRule.ACCRUAL_PERIODIC.value;
    public static final Integer LOAN_ACCOUNTING_RULE_NONE = AccountingRule.NONE.value;
    public static final String OVER_APPLIED_CALCULATION_TYPE = "percentage";
    public static final Integer OVER_APPLIED_NUMBER = 50;
    public static final Integer DELINQUENCY_BUCKET_ID = DelinquencyBucket.BASIC_DELINQUENCY_BUCKET.value;
    public static final Integer PRE_CLOSURE_INTEREST_CALCULATION_RULE_TILL_PRE_CLOSE_DATE = PreClosureInterestCalculationRule.TILL_PRE_CLOSE_DATE.value;
    public static final Integer ADVANCE_PAYMENT_ADJUSTMENT_TYPE_REDUCE_EMI_AMOUNT = AdvancePaymentsAdjustmentType.REDUCE_EMI_AMOUNT.value;
    public static final Integer INTEREST_RECALCULATION_COMPOUND_METHOD_INTEREST = InterestRecalculationCompoundingMethod.INTEREST.value;
    public static final Integer INTEREST_RECALCULATION_COMPOUND_METHOD_NONE = InterestRecalculationCompoundingMethod.NONE.value;
    public static final Integer FREQUENCY_FOR_COMPOUNDING_SAME_AS_REPAYMENT = RecalculationCompoundingFrequencyType.SAME_AS_REPAYMENT.value;
    public static final Integer FREQUENCY_FOR_COMPOUNDING_MONTHLY = RecalculationCompoundingFrequencyType.MONTHLY.value;
    public static final Integer FREQUENCY_FOR_RECALCULATE_OUTSTANDING_PRINCIPAL_SAME_AS_REPAYMENT = RecalculationRestFrequencyType.SAME_AS_REPAYMENT.value;
    public static final Integer FREQUENCY_FOR_RECALCULATE_OUTSTANDING_DAILY = RecalculationRestFrequencyType.DAILY.value;

    public PostLoanProductsRequest defaultLoanProductsRequestLP1() {
        String name = Utils.randomNameGenerator(NAME_PREFIX, 4);
        String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX, 3);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES));
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER));
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        return new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(DESCRIPTION)//
                .fundId(FUND_ID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 0)//
                .maxInterestRatePerPeriod((double) 0)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_MONTH)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_DAYS)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(AMORTIZATION_TYPE)//
                .interestType(INTEREST_TYPE_DECLINING_BALANCE)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT)//
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .daysInYearType(DAYS_IN_YEAR_TYPE)//
                .daysInMonthType(DAYS_IN_MONTH_TYPE)//
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
                .charges(charges)//
                .accountingRule(LOAN_ACCOUNTING_RULE)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .interestOnLoanAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .receivableInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivableFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivablePenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OVER_APPLIED_CALCULATION_TYPE)//
                .overAppliedNumber(OVER_APPLIED_NUMBER)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF));//
    }

    public PostLoanProductsRequest defaultLoanProductsRequestLP1InterestFlat() {
        String name = Utils.randomNameGenerator(NAME_PREFIX_INTEREST_FLAT, 4);
        String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX_INTEREST, 3);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES));
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER));
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        return new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(DESCRIPTION_INTEREST_FLAT)//
                .fundId(FUND_ID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 12)//
                .maxInterestRatePerPeriod((double) 30)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_YEAR)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_DAYS)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(AMORTIZATION_TYPE)//
                .interestType(INTEREST_TYPE_FLAT)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT)//
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .daysInYearType(DAYS_IN_YEAR_TYPE)//
                .daysInMonthType(DAYS_IN_MONTH_TYPE)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .isInterestRecalculationEnabled(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(false)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(LOAN_ACCOUNTING_RULE)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .interestOnLoanAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .receivableInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivableFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivablePenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF));//
    }

    public PostLoanProductsRequest defaultLoanProductsRequestLP1InterestDeclining() {
        String name = Utils.randomNameGenerator(NAME_PREFIX_INTEREST_DECLINING, 4);
        String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX_INTEREST, 3);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES));
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER));
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        return new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(DESCRIPTION_INTEREST_DECLINING)//
                .fundId(FUND_ID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 12)//
                .maxInterestRatePerPeriod((double) 30)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_YEAR)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_DAYS)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(AMORTIZATION_TYPE)//
                .interestType(INTEREST_TYPE_DECLINING_BALANCE)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT)//
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .daysInYearType(DAYS_IN_YEAR_TYPE)//
                .daysInMonthType(DAYS_IN_MONTH_TYPE)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .isInterestRecalculationEnabled(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(false)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(LOAN_ACCOUNTING_RULE)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .interestOnLoanAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .receivableInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivableFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivablePenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF));//
    }

    public PostLoanProductsRequest defaultLoanProductsRequestLP11MonthInterestDecliningBalanceDailyRecalculationCompoundingMonthly() {
        String name = Utils.randomNameGenerator(NAME_PREFIX_INTEREST_DECLINING_RECALCULATION, 4);
        String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX_INTEREST, 3);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES));
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER));
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);
        return new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(DESCRIPTION_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY)//
                .fundId(FUND_ID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(1)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 12)//
                .maxInterestRatePerPeriod((double) 30)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_YEAR)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_MONTHS)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(AMORTIZATION_TYPE)//
                .interestType(INTEREST_TYPE_DECLINING_BALANCE)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_DAILY)//
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .daysInYearType(DAYS_IN_YEAR_TYPE)//
                .daysInMonthType(DAYS_IN_MONTH_TYPE)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(false)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(LOAN_ACCOUNTING_RULE)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .interestOnLoanAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .receivableInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivableFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivablePenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(PRE_CLOSURE_INTEREST_CALCULATION_RULE_TILL_PRE_CLOSE_DATE)//
                .rescheduleStrategyMethod(ADVANCE_PAYMENT_ADJUSTMENT_TYPE_REDUCE_EMI_AMOUNT)//
                .interestRecalculationCompoundingMethod(INTEREST_RECALCULATION_COMPOUND_METHOD_INTEREST)//
                .recalculationCompoundingFrequencyType(FREQUENCY_FOR_COMPOUNDING_MONTHLY)//
                .recalculationRestFrequencyType(FREQUENCY_FOR_RECALCULATE_OUTSTANDING_DAILY)//
                .recalculationRestFrequencyInterval(1)//
                .recalculationCompoundingFrequencyInterval(1)//
                .recalculationCompoundingFrequencyOnDayType(1);//
    }

    public PostLoanProductsRequest defaultLoanProductsRequestLP1InterestDecliningBalanceDailyRecalculationCompoundingNone() {
        String name = Utils.randomNameGenerator(NAME_PREFIX_INTEREST_DECLINING_RECALCULATION, 4);
        String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX_INTEREST, 3);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES));
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER));
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);
        return new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(DESCRIPTION_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE)//
                .fundId(FUND_ID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(1)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 12)//
                .maxInterestRatePerPeriod((double) 30)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_YEAR)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_DAYS)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(AMORTIZATION_TYPE)//
                .interestType(INTEREST_TYPE_DECLINING_BALANCE)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_DAILY)//
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .daysInYearType(DAYS_IN_YEAR_TYPE)//
                .daysInMonthType(DAYS_IN_MONTH_TYPE)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(false)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(LOAN_ACCOUNTING_RULE)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .interestOnLoanAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .receivableInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivableFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivablePenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(PRE_CLOSURE_INTEREST_CALCULATION_RULE_TILL_PRE_CLOSE_DATE)//
                .rescheduleStrategyMethod(ADVANCE_PAYMENT_ADJUSTMENT_TYPE_REDUCE_EMI_AMOUNT)//
                .interestRecalculationCompoundingMethod(INTEREST_RECALCULATION_COMPOUND_METHOD_NONE)//
                .recalculationRestFrequencyType(FREQUENCY_FOR_RECALCULATE_OUTSTANDING_DAILY)//
                .recalculationRestFrequencyInterval(1);//
    }

    public PostLoanProductsRequest defaultLoanProductsRequestLP2InterestDailyRecalculationWithDownPayment() {
        final String name = Utils.randomNameGenerator(NAME_PREFIX_INTEREST_RECALCULATION_WITH_DOWN_PAYMENT_LP3, 4);
        final String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX_INTEREST, 3);

        return new PostLoanProductsRequest().name(name).shortName(shortName)
                .description(DESCRIPTION_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE).startDate(null).closeDate(null)
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false).accountingRule(LOAN_ACCOUNTING_RULE_NONE)
                .allowApprovedDisbursedAmountsOverApplied(true)
                .allowAttributeOverrides(new AllowAttributeOverrides().amortizationType(true).interestType(true)
                        .transactionProcessingStrategyCode(true).interestCalculationPeriodType(true).inArrearsTolerance(true)
                        .repaymentEvery(true).graceOnPrincipalAndInterestPayment(true).graceOnArrearsAgeing(true))
                .allowPartialPeriodInterestCalcualtion(false).allowVariableInstallments(false).amortizationType(AMORTIZATION_TYPE)
                .canDefineInstallmentAmount(true).canUseForTopup(false).charges(new ArrayList<>()).creditAllocation(new ArrayList<>())
                .currencyCode(CURRENCY_CODE).dateFormat(DATE_FORMAT).daysInMonthType(DAYS_IN_MONTH_TYPE_30)
                .daysInYearType(DAYS_IN_YEAR_TYPE_360).delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue()).digitsAfterDecimal(2)
                .disallowExpectedDisbursements(true).dueDaysForRepaymentEvent(1).enableDownPayment(false)
                .enableInstallmentLevelDelinquency(true).fixedLength(null).holdGuaranteeFunds(false).inMultiplesOf(0)
                .includeInBorrowerCycle(false).interestCalculationPeriodType(0).interestRateFrequencyType(3).interestRatePerPeriod(9.99)
                .interestRateVariationsForBorrowerCycle(new ArrayList<>()).interestRecalculationCompoundingMethod(0).interestType(0)
                .isArrearsBasedOnOriginalSchedule(false).isEqualAmortization(false).isInterestRecalculationEnabled(true)
                .isLinkedToFloatingInterestRates(false).loanScheduleProcessingType("HORIZONTAL").loanScheduleType("PROGRESSIVE")
                .locale(LOCALE_EN).maxInterestRatePerPeriod((double) 50).maxNumberOfRepayments(48).maxPrincipal((double) 10000)
                .maxTrancheCount(10).minInterestRatePerPeriod((double) 0).minNumberOfRepayments(1).minPrincipal((double) 1)
                .multiDisburseLoan(true).numberOfRepaymentVariationsForBorrowerCycle(new ArrayList<>()).numberOfRepayments(3)
                .outstandingLoanBalance((double) 10000).overAppliedCalculationType("flat").overAppliedNumber(10000)
                .overDueDaysForRepaymentEvent(2).preClosureInterestCalculationStrategy(1).principal((double) 40)
                .principalThresholdForLastInstallment(50).principalVariationsForBorrowerCycle(new ArrayList<>())
                .recalculationRestFrequencyInterval(1).recalculationRestFrequencyType(2).repaymentEvery(1).repaymentFrequencyType(2L)
                .repaymentStartDateType(2).rescheduleStrategyMethod(4).supportedInterestRefundTypes(new ArrayList<>())
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE_ADVANCED).useBorrowerCycle(false);
    }

    public PostLoanProductsRequest defaultLoanProductsRequestLP2() {
        String name = Utils.randomNameGenerator(NAME_PREFIX_LP2, 4);
        String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX, 3);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES));
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER));
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        return new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(DESCRIPTION_LP2)//
                .enableDownPayment(true)//
                .enableAutoRepaymentForDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .fundId(FUND_ID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(3)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 0)//
                .maxInterestRatePerPeriod((double) 0)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_MONTH)//
                .repaymentEvery(15)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_DAYS)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(AMORTIZATION_TYPE)//
                .interestType(INTEREST_TYPE_DECLINING_BALANCE)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT)//
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .daysInYearType(DAYS_IN_YEAR_TYPE)//
                .daysInMonthType(DAYS_IN_MONTH_TYPE)//
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
                .charges(charges)//
                .accountingRule(LOAN_ACCOUNTING_RULE)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .interestOnLoanAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .receivableInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivableFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivablePenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OVER_APPLIED_CALCULATION_TYPE)//
                .overAppliedNumber(OVER_APPLIED_NUMBER)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF));//
    }

    public PostLoanProductsRequest defaultLoanProductsRequestLP2NoDown() {
        String name = Utils.randomNameGenerator(NAME_PREFIX, 4);
        String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX, 3);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES));
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER));
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        return new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(DESCRIPTION_LP2)//
                .fundId(FUND_ID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 0)//
                .maxInterestRatePerPeriod((double) 0)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_MONTH)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_DAYS)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(AMORTIZATION_TYPE)//
                .interestType(INTEREST_TYPE_DECLINING_BALANCE)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT)//
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .daysInYearType(DAYS_IN_YEAR_TYPE)//
                .daysInMonthType(DAYS_IN_MONTH_TYPE)//
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
                .charges(charges)//
                .accountingRule(LOAN_ACCOUNTING_RULE)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .interestOnLoanAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .receivableInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivableFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivablePenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType(OVER_APPLIED_CALCULATION_TYPE)//
                .overAppliedNumber(OVER_APPLIED_NUMBER)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF));//
    }

    public PostLoanProductsRequest defaultLoanProductsRequestLP2InterestFlat() {
        String name = Utils.randomNameGenerator(NAME_PREFIX_INTEREST_FLAT_LP2, 4);
        String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX_INTEREST, 3);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES));
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER));
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        return new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(DESCRIPTION_INTEREST_FLAT_LP2)//
                .enableDownPayment(true)//
                .enableAutoRepaymentForDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(25))//
                .fundId(FUND_ID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(3)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 12)//
                .maxInterestRatePerPeriod((double) 30)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_YEAR)//
                .repaymentEvery(15)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_DAYS)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(AMORTIZATION_TYPE)//
                .interestType(INTEREST_TYPE_FLAT)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT)//
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .daysInYearType(DAYS_IN_YEAR_TYPE)//
                .daysInMonthType(DAYS_IN_MONTH_TYPE)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .isInterestRecalculationEnabled(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(false)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(LOAN_ACCOUNTING_RULE)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .interestOnLoanAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .receivableInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivableFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivablePenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF));//
    }

    public PostLoanProductsRequest defaultLoanProductsRequestLP2Emi() {
        String name = Utils.randomNameGenerator(NAME_PREFIX_LP2_EMI, 4);
        String shortName = Utils.randomNameGenerator(SHORT_NAME_PREFIX_EMI, 3);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES));
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER));
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        return new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(DESCRIPTION_LP2_EMI)//
                .loanScheduleType("PROGRESSIVE") //
                .interestCalculationPeriodType(InterestCalculationPeriodTime.DAILY.value)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue())//
                .fundId(FUND_ID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .useBorrowerCycle(false)//
                .minPrincipal(10.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(4)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 12)//
                .maxInterestRatePerPeriod((double) 60)//
                .interestRateFrequencyType(INTEREST_RATE_FREQUENCY_TYPE_YEAR)//
                .repaymentEvery(15)//
                .repaymentStartDateType(1)//
                .repaymentFrequencyType(REPAYMENT_FREQUENCY_TYPE_DAYS)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(AMORTIZATION_TYPE)//
                .interestType(INTEREST_TYPE_DECLINING_BALANCE)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(INTEREST_CALCULATION_PERIOD_TYPE_DAILY)//
                .transactionProcessingStrategyCode(TRANSACTION_PROCESSING_STRATEGY_CODE_ADVANCED)//
                .daysInYearType(DAYS_IN_YEAR_TYPE)//
                .daysInMonthType(DAYS_IN_MONTH_TYPE)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .isInterestRecalculationEnabled(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(false)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))//
                .allowPartialPeriodInterestCalcualtion(false)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(LOAN_ACCOUNTING_RULE)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .interestOnLoanAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .receivableInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivableFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .receivablePenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_FEE_RECEIVABLE))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .disallowExpectedDisbursements(false)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF));//
    }

}
