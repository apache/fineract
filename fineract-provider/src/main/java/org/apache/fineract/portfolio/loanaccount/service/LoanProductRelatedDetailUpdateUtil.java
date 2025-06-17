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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanProductRelatedDetailUpdateUtil {

    public Map<String, Object> updateLoanRepaymentSchedule(final LoanProductRelatedDetail loanRepaymentScheduleDetail,
            final JsonCommand command, final AprCalculator aprCalculator) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);

        final String localeAsInput = command.locale();

        String currencyCode = loanRepaymentScheduleDetail.getCurrency().getCode();
        Integer digitsAfterDecimal = loanRepaymentScheduleDetail.getCurrency().getDigitsAfterDecimal();
        Integer inMultiplesOf = loanRepaymentScheduleDetail.getCurrency().getCurrencyInMultiplesOf();

        final String digitsAfterDecimalParamName = "digitsAfterDecimal";
        if (command.isChangeInIntegerParameterNamed(digitsAfterDecimalParamName, digitsAfterDecimal)) {
            final Integer newValue = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
            actualChanges.put(digitsAfterDecimalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            digitsAfterDecimal = newValue;
            loanRepaymentScheduleDetail.setCurrency(new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf));
        }

        final String currencyCodeParamName = "currencyCode";
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            currencyCode = newValue;
            loanRepaymentScheduleDetail.setCurrency(new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf));
        }

        final String inMultiplesOfParamName = "inMultiplesOf";
        if (command.isChangeInStringParameterNamed(inMultiplesOfParamName, currencyCode)) {
            final Integer newValue = command.integerValueOfParameterNamed(inMultiplesOfParamName);
            actualChanges.put(inMultiplesOfParamName, newValue);
            inMultiplesOf = newValue;
            loanRepaymentScheduleDetail.setCurrency(new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf));
        }

        final String loanScheduleTypeParamName = LoanProductConstants.LOAN_SCHEDULE_TYPE;
        if (command.isChangeInStringParameterNamed(loanScheduleTypeParamName,
                loanRepaymentScheduleDetail.getLoanScheduleType().toString())) {
            LoanScheduleType newLoanScheduleType = LoanScheduleType.valueOf(command.stringValueOfParameterNamed(loanScheduleTypeParamName));
            actualChanges.put(loanScheduleTypeParamName, newLoanScheduleType);
            loanRepaymentScheduleDetail.setLoanScheduleType(newLoanScheduleType);
        }

        final String loanScheduleProcessingTypeParamName = LoanProductConstants.LOAN_SCHEDULE_PROCESSING_TYPE;
        if (command.isChangeInStringParameterNamed(loanScheduleProcessingTypeParamName,
                loanRepaymentScheduleDetail.getLoanScheduleProcessingType().toString())) {
            LoanScheduleProcessingType newLoanScheduleProcessingType = LoanScheduleProcessingType
                    .valueOf(command.stringValueOfParameterNamed(loanScheduleProcessingTypeParamName));
            actualChanges.put(loanScheduleProcessingTypeParamName, newLoanScheduleProcessingType);
            loanRepaymentScheduleDetail.setLoanScheduleProcessingType(newLoanScheduleProcessingType);
        }

        final String principalParamName = "principal";
        if (command.isChangeInBigDecimalParameterNamed(principalParamName, loanRepaymentScheduleDetail.getPrincipal().getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(principalParamName);
            actualChanges.put(principalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setPrincipal(newValue);
        }

        final String repaymentEveryParamName = "repaymentEvery";
        if (command.isChangeInIntegerParameterNamed(repaymentEveryParamName, loanRepaymentScheduleDetail.getRepayEvery())) {
            final Integer newValue = command.integerValueOfParameterNamed(repaymentEveryParamName);
            actualChanges.put(repaymentEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setRepayEvery(newValue);
        }

        final String repaymentFrequencyTypeParamName = "repaymentFrequencyType";
        if (command.isChangeInIntegerParameterNamed(repaymentFrequencyTypeParamName,
                loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType().getValue())) {
            Integer newValue = command.integerValueOfParameterNamed(repaymentFrequencyTypeParamName);
            actualChanges.put(repaymentFrequencyTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setRepaymentPeriodFrequencyType(PeriodFrequencyType.fromInt(newValue));
        }
        if (loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType() == PeriodFrequencyType.MONTHS) {
            final String repaymentFrequencyNthDayTypeParamName = "repaymentFrequencyNthDayType";
            Integer newValue = command.integerValueOfParameterNamed(repaymentFrequencyNthDayTypeParamName);
            actualChanges.put(repaymentFrequencyNthDayTypeParamName, newValue);

            final String repaymentFrequencyDayOfWeekTypeParamName = "repaymentFrequencyDayOfWeekType";
            newValue = command.integerValueOfParameterNamed(repaymentFrequencyDayOfWeekTypeParamName);
            actualChanges.put(repaymentFrequencyDayOfWeekTypeParamName, newValue);

            actualChanges.put("locale", localeAsInput);
        }

        final String numberOfRepaymentsParamName = "numberOfRepayments";
        if (command.isChangeInIntegerParameterNamed(numberOfRepaymentsParamName, loanRepaymentScheduleDetail.getNumberOfRepayments())) {
            final Integer newValue = command.integerValueOfParameterNamed(numberOfRepaymentsParamName);
            actualChanges.put(numberOfRepaymentsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setNumberOfRepayments(newValue);
        }

        final String amortizationTypeParamName = "amortizationType";
        if (command.isChangeInIntegerParameterNamed(amortizationTypeParamName,
                loanRepaymentScheduleDetail.getAmortizationMethod().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(amortizationTypeParamName);
            actualChanges.put(amortizationTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setAmortizationMethod(AmortizationMethod.fromInt(newValue));
        }

        final String inArrearsToleranceParamName = "inArrearsTolerance";
        if (command.isChangeInBigDecimalParameterNamed(inArrearsToleranceParamName,
                loanRepaymentScheduleDetail.getInArrearsTolerance().getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(inArrearsToleranceParamName);
            actualChanges.put(inArrearsToleranceParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setInArrearsTolerance(newValue);
        }

        final String interestRatePerPeriodParamName = "interestRatePerPeriod";
        if (command.isChangeInBigDecimalParameterNamed(interestRatePerPeriodParamName,
                loanRepaymentScheduleDetail.getNominalInterestRatePerPeriod())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(interestRatePerPeriodParamName);
            actualChanges.put(interestRatePerPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setNominalInterestRatePerPeriod(newValue);
            updateInterestRateDerivedFields(loanRepaymentScheduleDetail, aprCalculator);
        }

        final String interestRateFrequencyTypeParamName = "interestRateFrequencyType";
        final int interestPeriodFrequencyType = loanRepaymentScheduleDetail.getInterestPeriodFrequencyType() == null
                ? PeriodFrequencyType.INVALID.getValue()
                : loanRepaymentScheduleDetail.getInterestPeriodFrequencyType().getValue();
        if (command.isChangeInIntegerParameterNamed(interestRateFrequencyTypeParamName, interestPeriodFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestRateFrequencyTypeParamName);
            actualChanges.put(interestRateFrequencyTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setInterestPeriodFrequencyType(PeriodFrequencyType.fromInt(newValue));
            updateInterestRateDerivedFields(loanRepaymentScheduleDetail, aprCalculator);
        }

        final String interestTypeParamName = "interestType";
        if (command.isChangeInIntegerParameterNamed(interestTypeParamName, loanRepaymentScheduleDetail.getInterestMethod().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestTypeParamName);
            actualChanges.put(interestTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setInterestMethod(InterestMethod.fromInt(newValue));
        }

        final String interestCalculationPeriodTypeParamName = "interestCalculationPeriodType";
        if (command.isChangeInIntegerParameterNamed(interestCalculationPeriodTypeParamName,
                loanRepaymentScheduleDetail.getInterestCalculationPeriodMethod().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationPeriodTypeParamName);
            actualChanges.put(interestCalculationPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setInterestCalculationPeriodMethod(InterestCalculationPeriodMethod.fromInt(newValue));
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
                loanRepaymentScheduleDetail.isAllowPartialPeriodInterestCalcualtion())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME);
            actualChanges.put(LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME, newValue);
            loanRepaymentScheduleDetail.setAllowPartialPeriodInterestCalcualtion(newValue);
        }

        if (loanRepaymentScheduleDetail.getInterestCalculationPeriodMethod().isDaily()) {
            loanRepaymentScheduleDetail.setAllowPartialPeriodInterestCalcualtion(false);
        }

        final String graceOnPrincipalPaymentParamName = "graceOnPrincipalPayment";
        if (command.isChangeInIntegerParameterNamed(graceOnPrincipalPaymentParamName,
                loanRepaymentScheduleDetail.getGraceOnPrincipalPayment())) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnPrincipalPaymentParamName);
            actualChanges.put(graceOnPrincipalPaymentParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setGraceOnPrincipalPayment(newValue);
        }

        final String recurringMoratoriumOnPrincipalPeriodsParamName = "recurringMoratoriumOnPrincipalPeriods";
        if (command.isChangeInIntegerParameterNamed(recurringMoratoriumOnPrincipalPeriodsParamName,
                loanRepaymentScheduleDetail.getRecurringMoratoriumOnPrincipalPeriods())) {
            final Integer newValue = command.integerValueOfParameterNamed(recurringMoratoriumOnPrincipalPeriodsParamName);
            actualChanges.put(recurringMoratoriumOnPrincipalPeriodsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setRecurringMoratoriumOnPrincipalPeriods(newValue);
        }

        final String graceOnInterestPaymentParamName = "graceOnInterestPayment";
        if (command.isChangeInIntegerParameterNamed(graceOnInterestPaymentParamName,
                loanRepaymentScheduleDetail.getGraceOnInterestPayment())) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnInterestPaymentParamName);
            actualChanges.put(graceOnInterestPaymentParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setGraceOnInterestPayment(newValue);
        }

        final String graceOnInterestChargedParamName = "graceOnInterestCharged";
        if (command.isChangeInIntegerParameterNamed(graceOnInterestChargedParamName,
                loanRepaymentScheduleDetail.getGraceOnInterestCharged())) {
            final Integer newValue = command.integerValueOfParameterNamed(graceOnInterestChargedParamName);
            actualChanges.put(graceOnInterestChargedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setGraceOnInterestCharged(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME,
                loanRepaymentScheduleDetail.getGraceOnArrearsAgeing())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.GRACE_ON_ARREARS_AGEING_PARAMETER_NAME, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setGraceOnArrearsAgeing(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME,
                loanRepaymentScheduleDetail.getDaysInMonthType())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.DAYS_IN_MONTH_TYPE_PARAMETER_NAME, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setDaysInMonthType(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME,
                loanRepaymentScheduleDetail.getDaysInYearType())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.DAYS_IN_YEAR_TYPE_PARAMETER_NAME, newValue);
            actualChanges.put("locale", localeAsInput);
            loanRepaymentScheduleDetail.setDaysInYearType(newValue);
        }

        if (command.parameterExists(LoanProductConstants.DAYS_IN_YEAR_CUSTOM_STRATEGY_TYPE_PARAMETER_NAME)) {
            final DaysInYearCustomStrategyType newValue = DaysInYearCustomStrategyType
                    .valueOf(command.stringValueOfParameterNamed(LoanProductConstants.DAYS_IN_YEAR_CUSTOM_STRATEGY_TYPE_PARAMETER_NAME));
            actualChanges.put(LoanProductConstants.DAYS_IN_YEAR_CUSTOM_STRATEGY_TYPE_PARAMETER_NAME, newValue.name());
            loanRepaymentScheduleDetail.setDaysInYearCustomStrategy(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME,
                loanRepaymentScheduleDetail.isInterestRecalculationEnabled())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME);
            actualChanges.put(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME, newValue);
            loanRepaymentScheduleDetail.setInterestRecalculationEnabled(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM,
                loanRepaymentScheduleDetail.isEqualAmortization())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM);
            actualChanges.put(LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, newValue);
            loanRepaymentScheduleDetail.setEqualAmortization(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.INTEREST_RECOGNITION_ON_DISBURSEMENT_DATE,
                loanRepaymentScheduleDetail.isInterestRecognitionOnDisbursementDate())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.INTEREST_RECOGNITION_ON_DISBURSEMENT_DATE);
            actualChanges.put(LoanProductConstants.INTEREST_RECOGNITION_ON_DISBURSEMENT_DATE, newValue);
            loanRepaymentScheduleDetail.updateInterestRecognitionOnDisbursementDate(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_INCOME_CAPITALIZATION_PARAM_NAME,
                loanRepaymentScheduleDetail.isEnableIncomeCapitalization())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_INCOME_CAPITALIZATION_PARAM_NAME);
            actualChanges.put(LoanProductConstants.ENABLE_INCOME_CAPITALIZATION_PARAM_NAME, newValue);
            loanRepaymentScheduleDetail.setEnableIncomeCapitalization(newValue);
        }

        if (command.parameterExists(LoanProductConstants.CAPITALIZED_INCOME_CALCULATION_TYPE_PARAM_NAME)) {
            final LoanCapitalizedIncomeCalculationType newValue = command.enumValueOfParameterNamed(
                    LoanProductConstants.CAPITALIZED_INCOME_CALCULATION_TYPE_PARAM_NAME, LoanCapitalizedIncomeCalculationType.class);
            actualChanges.put(LoanProductConstants.CAPITALIZED_INCOME_CALCULATION_TYPE_PARAM_NAME, newValue);
            loanRepaymentScheduleDetail.setCapitalizedIncomeCalculationType(newValue);
        }

        if (command.parameterExists(LoanProductConstants.CAPITALIZED_INCOME_STRATEGY_PARAM_NAME)) {
            final LoanCapitalizedIncomeStrategy newValue = command.enumValueOfParameterNamed(
                    LoanProductConstants.CAPITALIZED_INCOME_STRATEGY_PARAM_NAME, LoanCapitalizedIncomeStrategy.class);
            actualChanges.put(LoanProductConstants.CAPITALIZED_INCOME_STRATEGY_PARAM_NAME, newValue);
            loanRepaymentScheduleDetail.setCapitalizedIncomeStrategy(newValue);
        }

        if (command.parameterExists(LoanProductConstants.CAPITALIZED_INCOME_TYPE_PARAM_NAME)) {
            final LoanCapitalizedIncomeType newValue = command
                    .enumValueOfParameterNamed(LoanProductConstants.CAPITALIZED_INCOME_TYPE_PARAM_NAME, LoanCapitalizedIncomeType.class);
            actualChanges.put(LoanProductConstants.CAPITALIZED_INCOME_TYPE_PARAM_NAME, newValue);
            loanRepaymentScheduleDetail.setCapitalizedIncomeType(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.ENABLE_BUY_DOWN_FEE_PARAM_NAME,
                loanRepaymentScheduleDetail.isEnableBuyDownFee())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.ENABLE_BUY_DOWN_FEE_PARAM_NAME);
            actualChanges.put(LoanProductConstants.ENABLE_BUY_DOWN_FEE_PARAM_NAME, newValue);
            loanRepaymentScheduleDetail.setEnableBuyDownFee(newValue);
        }

        if (command.parameterExists(LoanProductConstants.BUY_DOWN_FEE_CALCULATION_TYPE_PARAM_NAME)) {
            final LoanBuyDownFeeCalculationType newValue = command.enumValueOfParameterNamed(
                    LoanProductConstants.BUY_DOWN_FEE_CALCULATION_TYPE_PARAM_NAME, LoanBuyDownFeeCalculationType.class);
            actualChanges.put(LoanProductConstants.BUY_DOWN_FEE_CALCULATION_TYPE_PARAM_NAME, newValue);
            loanRepaymentScheduleDetail.setBuyDownFeeCalculationType(newValue);
        }

        if (command.parameterExists(LoanProductConstants.BUY_DOWN_FEE_STRATEGY_PARAM_NAME)) {
            final LoanBuyDownFeeStrategy newValue = command.enumValueOfParameterNamed(LoanProductConstants.BUY_DOWN_FEE_STRATEGY_PARAM_NAME,
                    LoanBuyDownFeeStrategy.class);
            actualChanges.put(LoanProductConstants.BUY_DOWN_FEE_STRATEGY_PARAM_NAME, newValue);
            loanRepaymentScheduleDetail.setBuyDownFeeStrategy(newValue);
        }

        if (command.parameterExists(LoanProductConstants.BUY_DOWN_FEE_INCOME_TYPE_PARAM_NAME)) {
            final LoanBuyDownFeeIncomeType newValue = command
                    .enumValueOfParameterNamed(LoanProductConstants.BUY_DOWN_FEE_INCOME_TYPE_PARAM_NAME, LoanBuyDownFeeIncomeType.class);
            actualChanges.put(LoanProductConstants.BUY_DOWN_FEE_INCOME_TYPE_PARAM_NAME, newValue);
            loanRepaymentScheduleDetail.setBuyDownFeeIncomeType(newValue);
        }

        return actualChanges;
    }

    public void updateInterestRateDerivedFields(final LoanProductRelatedDetail loanRepaymentScheduleDetail,
            final AprCalculator aprCalculator) {
        BigDecimal annualNominalInterestRate = aprCalculator.calculateFrom(loanRepaymentScheduleDetail.getInterestPeriodFrequencyType(),
                loanRepaymentScheduleDetail.getNominalInterestRatePerPeriod(), loanRepaymentScheduleDetail.getNumberOfRepayments(),
                loanRepaymentScheduleDetail.getRepayEvery(), loanRepaymentScheduleDetail.getRepaymentPeriodFrequencyType());
        loanRepaymentScheduleDetail.setAnnualNominalInterestRate(annualNominalInterestRate);
    }
}
