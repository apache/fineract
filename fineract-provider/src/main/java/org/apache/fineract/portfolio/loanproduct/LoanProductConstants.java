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

    public static final String USE_BORROWER_CYCLE_PARAMETER_NAME = "useBorrowerCycle";

    public static final String PRINCIPAL_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME = "principalVariationsForBorrowerCycle";
    public static final String INTEREST_RATE_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME = "interestRateVariationsForBorrowerCycle";
    public static final String NUMBER_OF_REPAYMENT_VARIATIONS_FOR_BORROWER_CYCLE_PARAMETER_NAME = "numberOfRepaymentVariationsForBorrowerCycle";

    public static final String DEFAULT_VALUE_PARAMETER_NAME = "defaultValue";
    public static final String MIN_VALUE_PARAMETER_NAME = "minValue";
    public static final String MAX_VALUE_PARAMETER_NAME = "maxValue";
    public static final String VALUE_CONDITION_TYPE_PARAM_NAME = "valueConditionType";
    public static final String BORROWER_CYCLE_NUMBER_PARAM_NAME = "borrowerCycleNumber";
    public static final String BORROWER_CYCLE_ID_PARAMETER_NAME = "id";

    public static final String PRINCIPAL_PER_CYCLE_PARAMETER_NAME = "principalPerCycle";
    public static final String MIN_PRINCIPAL_PER_CYCLE_PARAMETER_NAME = "minPrincipalPerCycle";
    public static final String MAX_PRINCIPAL_PER_CYCLE_PARAMETER_NAME = "maxPrincipalPerCycle";
    public static final String PRINCIPAL_VALUE_USAGE_CONDITION_PARAM_NAME = "principalValueUsageCondition";
    public static final String PRINCIPAL_CYCLE_NUMBERS_PARAM_NAME = "principalCycleNumbers";

    public static final String NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME = "numberOfRepaymentsPerCycle";
    public static final String MIN_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME = "minNumberOfRepaymentsPerCycle";
    public static final String MAX_NUMBER_OF_REPAYMENTS_PER_CYCLE_PARAMETER_NAME = "maxNumberOfRepaymentsPerCycle";
    public static final String REPAYMENT_VALUE_USAGE_CONDITION_PARAM_NAME = "repaymentValueUsageCondition";
    public static final String REPAYMENT_CYCLE_NUMBER_PARAM_NAME = "repaymentCycleNumber";

    public static final String INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME = "interestRatePerPeriodPerCycle";
    public static final String MIN_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME = "minInterestRatePerPeriodPerCycle";
    public static final String MAX_INTEREST_RATE_PER_PERIOD_PER_CYCLE_PARAMETER_NAME = "maxInterestRatePerPeriodPerCycle";
    public static final String INTEREST_RATE_VALUE_USAGE_CONDITION_PARAM_NAME = "interestRateValueUsageCondition";
    public static final String INTEREST_RATE_CYCLE_NUMBER_PARAM_NAME = "interestRateCycleNumber";

    public static final String PRINCIPAL = "principal";
    public static final String MIN_PRINCIPAL = "minPrincipal";
    public static final String MAX_PRINCIPAL = "maxPrincipalValue";

    public static final String INTEREST_RATE_PER_PERIOD = "interestRatePerPeriod";
    public static final String MIN_INTEREST_RATE_PER_PERIOD = "minInterestRatePerPeriod";
    public static final String MAX_INTEREST_RATE_PER_PERIOD = "maxInterestRatePerPeriod";

    public static final String NUMBER_OF_REPAYMENTS = "numberOfRepayments";
    public static final String MIN_NUMBER_OF_REPAYMENTS = "minNumberOfRepayments";
    public static final String MAX_NUMBER_OF_REPAYMENTS = "maxNumberOfRepayments";

    public static final String VALUE_CONDITION_END_WITH_ERROR = "condition.type.must.end.with.greterthan";
    public static final String VALUE_CONDITION_START_WITH_ERROR = "condition.type.must.start.with.equal";
    public static final String SHORT_NAME = "shortName";

    public static final String MULTI_DISBURSE_LOAN_PARAMETER_NAME = "multiDisburseLoan";
    public static final String MAX_TRANCHE_COUNT_PARAMETER_NAME = "maxTrancheCount";
    public static final String OUTSTANDING_LOAN_BALANCE_PARAMETER_NAME = "outstandingLoanBalance";

    public static final String GRACE_ON_ARREARS_AGEING_PARAMETER_NAME = "graceOnArrearsAgeing";
    public static final String OVERDUE_DAYS_FOR_NPA_PARAMETER_NAME = "overdueDaysForNPA";
    public static final String MINIMUM_DAYS_BETWEEN_DISBURSAL_AND_FIRST_REPAYMENT = "minimumDaysBetweenDisbursalAndFirstRepayment";
    public static final String ACCOUNT_MOVES_OUT_OF_NPA_ONLY_ON_ARREARS_COMPLETION_PARAM_NAME = "accountMovesOutOfNPAOnlyOnArrearsCompletion";

    // Interest recalculation related
    public static final String IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME = "isInterestRecalculationEnabled";
    public static final String DAYS_IN_YEAR_TYPE_PARAMETER_NAME = "daysInYearType";
    public static final String DAYS_IN_MONTH_TYPE_PARAMETER_NAME = "daysInMonthType";
    public static final String interestRecalculationCompoundingMethodParameterName = "interestRecalculationCompoundingMethod";
    public static final String rescheduleStrategyMethodParameterName = "rescheduleStrategyMethod";
    public static final String recalculationRestFrequencyTypeParameterName = "recalculationRestFrequencyType";
    public static final String recalculationRestFrequencyIntervalParameterName = "recalculationRestFrequencyInterval";
    public static final String recalculationRestFrequencyWeekdayParamName = "recalculationRestFrequencyDayOfWeekType";
    public static final String recalculationRestFrequencyNthDayParamName = "recalculationRestFrequencyNthDayType";
    public static final String recalculationRestFrequencyOnDayParamName = "recalculationRestFrequencyOnDayType";
    public static final String isArrearsBasedOnOriginalScheduleParamName = "isArrearsBasedOnOriginalSchedule";
    public static final String preClosureInterestCalculationStrategyParamName = "preClosureInterestCalculationStrategy";
    public static final String recalculationCompoundingFrequencyTypeParameterName = "recalculationCompoundingFrequencyType";
    public static final String recalculationCompoundingFrequencyIntervalParameterName = "recalculationCompoundingFrequencyInterval";
    public static final String recalculationCompoundingFrequencyWeekdayParamName = "recalculationCompoundingFrequencyDayOfWeekType";
    public static final String recalculationCompoundingFrequencyNthDayParamName = "recalculationCompoundingFrequencyNthDayType";
    public static final String recalculationCompoundingFrequencyOnDayParamName = "recalculationCompoundingFrequencyOnDayType";
    public static final String isCompoundingToBePostedAsTransactionParamName = "isCompoundingToBePostedAsTransaction";

    // Guarantee related
    public static final String holdGuaranteeFundsParamName = "holdGuaranteeFunds";
    public static final String mandatoryGuaranteeParamName = "mandatoryGuarantee";
    public static final String minimumGuaranteeFromOwnFundsParamName = "minimumGuaranteeFromOwnFunds";
    public static final String minimumGuaranteeFromGuarantorParamName = "minimumGuaranteeFromGuarantor";

    public static final String principalThresholdForLastInstallmentParamName = "principalThresholdForLastInstallment";
    public static final BigDecimal DEFAULT_PRINCIPAL_THRESHOLD_FOR_MULTI_DISBURSE_LOAN = BigDecimal.valueOf(50);
    public static final BigDecimal DEFAULT_PRINCIPAL_THRESHOLD_FOR_SINGLE_DISBURSE_LOAN = BigDecimal.valueOf(0);
    // Fixed installment configuration related
    public static final String canDefineEmiAmountParamName = "canDefineInstallmentAmount";
    public static final String installmentAmountInMultiplesOfParamName = "installmentAmountInMultiplesOf";

    //Loan Configurable Attributes
    public static final String allowAttributeOverridesParamName = "allowAttributeOverrides";
    public static final String amortizationTypeParamName = "amortizationType";
    public static final String interestTypeParamName = "interestType";
    public static final String transactionProcessingStrategyIdParamName = "transactionProcessingStrategyId";
    public static final String interestCalculationPeriodTypeParamName = "interestCalculationPeriodType";
    public static final String inArrearsToleranceParamName = "inArrearsTolerance";
    public static final String repaymentEveryParamName = "repaymentEvery";
    public static final String graceOnPrincipalAndInterestPaymentParamName = "graceOnPrincipalAndInterestPayment";
    public static final String allowCompoundingOnEodParamName = "allowCompoundingOnEod";

    //Variable Installments Settings
    public static final String allowVariableInstallmentsParamName = "allowVariableInstallments" ;
    public static final String minimumGapBetweenInstallments = "minimumGap" ;
    public static final String maximumGapBetweenInstallments = "maximumGap" ;


    public static final String ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME = "allowPartialPeriodInterestCalcualtion";

    public static final String CAN_USE_FOR_TOPUP = "canUseForTopup";

    public static final String IS_EQUAL_AMORTIZATION_PARAM = "isEqualAmortization";

    public static final String RATES_PARAM_NAME = "rates";

}
