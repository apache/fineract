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
package org.apache.fineract.portfolio.loanproduct;

import java.math.BigDecimal;

public interface LoanProductConstants {

    String USE_BORROWER_CYCLE_PARAMETER_NAME = "useBorrowerCycle";

    String PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME = "principalVariationsForBorrowerCycle";
    String INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME = "interestRateVariationsForBorrowerCycle";
    String NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME = "numberOfRepaymentVariationsForBorrowerCycle";

    String DEFAULT_VALUE_PARAMETER_NAME = "defaultValue";
    String MIN_VALUE_PARAMETER_NAME = "minValue";
    String MAX_VALUE_PARAMETER_NAME = "maxValue";
    String VALUE_CONDITION_TYPE_PARAM_NAME = "valueConditionType";
    String BORROWER_CYCLE_NUMBER_PARAM_NAME = "borrowerCycleNumber";
    String BORROWER_CYCLE_ID_PARAMETER_NAME = "id";

    String PRINCIPAL_PER_CYCLE_PARAMETER_NAME = "principalPerCycle";
    String MIN_PRINCIPAL_PER_CYCLE_PARAMETER_NAME = "minPrincipalPerCycle";
    String MAX_PRINCIPAL_PER_CYCLE_PARAMETER_NAME = "maxPrincipalPerCycle";
    String PRINCIPAL_VALUE_USAGE_CONDITION_PARAM_NAME = "principalValueUsageCondition";
    String PRINCIPAL_CYCLE_NUMBERS_PARAM_NAME = "principalCycleNumbers";

    String NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME = "numberOfRepaymentsPerCycle";
    String MIN_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME = "minNumberOfRepaymentsPerCycle";
    String MAX_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME = "maxNumberOfRepaymentsPerCycle";
    String REPAYMENT_VALUE_USAGE_CONDITION_PARAM_NAME = "repaymentValueUsageCondition";
    String REPAYMENT_CYCLE_NUMBER_PARAM_NAME = "repaymentCycleNumber";

    String INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME = "interestRatePerPeriodPerCycle";
    String MIN_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME = "minInterestRatePerPeriodPerCycle";
    String MAX_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME = "maxInterestRatePerPeriodPerCycle";
    String INTEREST_RATE_VALUE_USAGE_CONDITION_PARAM_NAME = "interestRateValueUsageCondition";
    String INTEREST_RATE_CYCLE_NUMBER_PARAM_NAME = "interestRateCycleNumber";

    String PRINCIPAL = "principal";
    String MIN_PRINCIPAL = "minPrincipal";
    String MAX_PRINCIPAL = "maxPrincipalValue";

    String INTEREST_RATE_PER_PERIOD = "interestRatePerPeriod";
    String MIN_INTEREST_RATE_PER_PERIOD = "minInterestRatePerPeriod";
    String MAX_INTEREST_RATE_PER_PERIOD = "maxInterestRatePerPeriod";

    String NUMBER_OF_REPAYMENTS = "numberOfRepayments";
    String MIN_NUMBER_OF_REPAYMENTS = "minNumberOfRepayments";
    String MAX_NUMBER_OF_REPAYMENTS = "maxNumberOfRepayments";

    String VALUE_CONDITION_END_WITH_ERROR = "condition.type.must.end.with.greater.than";
    String VALUE_CONDITION_START_WITH_ERROR = "condition.type.must.start.with.equal";
    String SHORT_NAME = "shortName";

    String MULTI_DISBURSE_LOAN_PARAMETER_NAME = "multiDisburseLoan";
    String MAX_TRANCHE_COUNT_PARAMETER_NAME = "maxTrancheCount";
    String OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME = "outstandingLoanBalance";

    String GRACE_ON_ARREARS_AGEING_PARAMETER_NAME = "graceOnArrearsAgeing";
    String OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME = "overdueDaysForNPA";
    String MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT = "minimumDaysBetweenDisbursalAndFirstRepayment";
    String ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME = "accountMovesOutOfNPAOnlyOnArrearsCompletion";

    // Interest recalculation related
    String IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME = "isInterestRecalculationEnabled";
    String DAYS_IN_YEAR_TYPE_PARAMETER_NAME = "daysInYearType";
    String DAYS_IN_MONTH_TYPE_PARAMETER_NAME = "daysInMonthType";
    String interestRecalculationCompoundingMethodParameterName = "interestRecalculationCompoundingMethod";
    String rescheduleStrategyMethodParameterName = "rescheduleStrategyMethod";
    String recalculationRestFrequencyTypeParameterName = "recalculationRestFrequencyType";
    String recalculationRestFrequencyIntervalParameterName = "recalculationRestFrequencyInterval";
    String recalculationRestFrequencyWeekdayParamName = "recalculationRestFrequencyDayOfWeekType";
    String recalculationRestFrequencyNthDayParamName = "recalculationRestFrequencyNthDayType";
    String recalculationRestFrequencyOnDayParamName = "recalculationRestFrequencyOnDayType";
    String isArrearsBasedOnOriginalScheduleParamName = "isArrearsBasedOnOriginalSchedule";
    String preClosureInterestCalculationStrategyParamName = "preClosureInterestCalculationStrategy";
    String recalculationCompoundingFrequencyTypeParameterName = "recalculationCompoundingFrequencyType";
    String recalculationCompoundingFrequencyIntervalParameterName = "recalculationCompoundingFrequencyInterval";
    String recalculationCompoundingFrequencyWeekdayParamName = "recalculationCompoundingFrequencyDayOfWeekType";
    String recalculationCompoundingFrequencyNthDayParamName = "recalculationCompoundingFrequencyNthDayType";
    String recalculationCompoundingFrequencyOnDayParamName = "recalculationCompoundingFrequencyOnDayType";
    String isCompoundingToBePostedAsTransactionParamName = "isCompoundingToBePostedAsTransaction";

    // Guarantee related
    String holdGuaranteeFundsParamName = "holdGuaranteeFunds";
    String mandatoryGuaranteeParamName = "mandatoryGuarantee";
    String minimumGuaranteeFromOwnFundsParamName = "minimumGuaranteeFromOwnFunds";
    String minimumGuaranteeFromGuarantorParamName = "minimumGuaranteeFromGuarantor";

    String principalThresholdForLastInstallmentParamName = "principalThresholdForLastInstallment";
    BigDecimal DEFAULT_PRINCIPAL_THRESHOLD_FOR_MULTI_DISBURSE_LOAN = BigDecimal.valueOf(50);
    BigDecimal DEFAULT_PRINCIPAL_THRESHOLD_FOR_SINGLE_DISBURSE_LOAN = BigDecimal.valueOf(0);
    // Fixed installment configuration related
    String canDefineEmiAmountParamName = "canDefineInstallmentAmount";
    String fixedPrincipalPercentagePerInstallmentParamName = "fixedPrincipalPercentagePerInstallment";

    // Loan Configurable Attributes
    String allowAttributeOverridesParamName = "allowAttributeOverrides";
    String amortizationTypeParamName = "amortizationType";
    String interestTypeParamName = "interestType";
    String transactionProcessingStrategyCodeParamName = "transactionProcessingStrategyCode";
    String interestCalculationPeriodTypeParamName = "interestCalculationPeriodType";
    String inArrearsToleranceParamName = "inArrearsTolerance";
    String repaymentEveryParamName = "repaymentEvery";
    String graceOnPrincipalAndInterestPaymentParamName = "graceOnPrincipalAndInterestPayment";
    String allowCompoundingOnEodParamName = "allowCompoundingOnEod";

    // Variable Installments Settings
    String allowVariableInstallmentsParamName = "allowVariableInstallments";
    String minimumGapBetweenInstallments = "minimumGap";
    String maximumGapBetweenInstallments = "maximumGap";

    String ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME = "allowPartialPeriodInterestCalcualtion";

    String CAN_USE_FOR_TOPUP = "canUseForTopup";

    String IS_EQUAL_AMORTIZATION_PARAM = "isEqualAmortization";

    String RATES_PARAM_NAME = "rates";

    // Multiple disbursement related
    String installmentAmountInMultiplesOfParamName = "installmentAmountInMultiplesOf";
    String DISALLOW_EXPECTED_DISBURSEMENTS = "disallowExpectedDisbursements";
    String ALLOW_APPROVED_DISBURSED_AMOUNTS_OVER_APPLIED = "allowApprovedDisbursedAmountsOverApplied";
    String OVER_APPLIED_CALCULATION_TYPE = "overAppliedCalculationType";
    String OVER_APPLIED_NUMBER = "overAppliedNumber";
    String DELINQUENCY_BUCKET_PARAM_NAME = "delinquencyBucketId";

    // repayment events related
    String DUE_DAYS_FOR_REPAYMENT_EVENT = "dueDaysForRepaymentEvent";
    String OVER_DUE_DAYS_FOR_REPAYMENT_EVENT = "overDueDaysForRepaymentEvent";

    // down-payment related
    String ENABLE_DOWN_PAYMENT = "enableDownPayment";
    String DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT = "disbursedAmountPercentageForDownPayment";
    String ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT = "enableAutoRepaymentForDownPayment";
    String REPAYMENT_START_DATE_TYPE = "repaymentStartDateType";

    String ENABLE_INSTALLMENT_LEVEL_DELINQUENCY = "enableInstallmentLevelDelinquency";

    // loan schedule type
    String LOAN_SCHEDULE_TYPE = "loanScheduleType";
    String LOAN_SCHEDULE_PROCESSING_TYPE = "loanScheduleProcessingType";

    // Repayment Strategies
    String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";
}
