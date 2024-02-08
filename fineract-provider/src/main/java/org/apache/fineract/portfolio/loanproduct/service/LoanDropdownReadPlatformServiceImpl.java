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
package org.apache.fineract.portfolio.loanproduct.service;

import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.amortizationType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestCalculationPeriodType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRateFrequencyType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingDayOfWeekType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingNthDayType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationFrequencyType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.loanCycleValueConditionType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.loanTermFrequencyType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.preCloseInterestCalculationStrategy;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.repaymentFrequencyDayOfWeekType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.repaymentFrequencyNthDayType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.repaymentFrequencyType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.repaymentStartDateType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.rescheduleStrategyType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.common.domain.DayOfWeekType;
import org.apache.fineract.portfolio.common.domain.NthDayType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductValueConditionType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;

@RequiredArgsConstructor
public class LoanDropdownReadPlatformServiceImpl implements LoanDropdownReadPlatformService {

    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;

    @Override
    public List<EnumOptionData> retrieveLoanAmortizationTypeOptions() {

        return Arrays.asList(amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS), amortizationType(AmortizationMethod.EQUAL_PRINCIPAL));
    }

    @Override
    public List<EnumOptionData> retrieveLoanInterestTypeOptions() {

        return Arrays.asList(interestType(InterestMethod.FLAT), interestType(InterestMethod.DECLINING_BALANCE));
    }

    @Override
    public List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions() {

        return Arrays.asList(interestCalculationPeriodType(InterestCalculationPeriodMethod.DAILY),
                interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD));
    }

    @Override
    public List<EnumOptionData> retrieveLoanTermFrequencyTypeOptions() {
        return Arrays.asList(loanTermFrequencyType(PeriodFrequencyType.DAYS), loanTermFrequencyType(PeriodFrequencyType.WEEKS),
                loanTermFrequencyType(PeriodFrequencyType.MONTHS), loanTermFrequencyType(PeriodFrequencyType.YEARS));
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentFrequencyTypeOptions() {

        return Arrays.asList(repaymentFrequencyType(PeriodFrequencyType.DAYS), repaymentFrequencyType(PeriodFrequencyType.WEEKS),
                repaymentFrequencyType(PeriodFrequencyType.MONTHS));
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentFrequencyOptionsForNthDayOfMonth() {
        return Arrays.asList(repaymentFrequencyNthDayType(NthDayType.ONE), repaymentFrequencyNthDayType(NthDayType.TWO),
                repaymentFrequencyNthDayType(NthDayType.THREE), repaymentFrequencyNthDayType(NthDayType.FOUR),
                repaymentFrequencyNthDayType(NthDayType.LAST));
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentFrequencyOptionsForDaysOfWeek() {

        return Arrays.asList(repaymentFrequencyDayOfWeekType(DayOfWeekType.SUNDAY), repaymentFrequencyDayOfWeekType(DayOfWeekType.MONDAY),
                repaymentFrequencyDayOfWeekType(DayOfWeekType.TUESDAY), repaymentFrequencyDayOfWeekType(DayOfWeekType.WEDNESDAY),
                repaymentFrequencyDayOfWeekType(DayOfWeekType.THURSDAY), repaymentFrequencyDayOfWeekType(DayOfWeekType.FRIDAY),
                repaymentFrequencyDayOfWeekType(DayOfWeekType.SATURDAY));
    }

    @Override
    public List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions() {
        // support for monthly and annual percentage rate (MPR) and (APR)
        return Arrays.asList(interestRateFrequencyType(PeriodFrequencyType.MONTHS), interestRateFrequencyType(PeriodFrequencyType.YEARS),
                interestRateFrequencyType(PeriodFrequencyType.WHOLE_TERM));
    }

    @Override
    public Collection<TransactionProcessingStrategyData> retrieveTransactionProcessingStrategies() {
        return loanRepaymentScheduleTransactionProcessorFactory.getStrategies();
    }

    @Override
    public List<EnumOptionData> retrieveLoanCycleValueConditionTypeOptions() {

        return Arrays.asList(loanCycleValueConditionType(LoanProductValueConditionType.EQUAL),
                loanCycleValueConditionType(LoanProductValueConditionType.GREATERTHAN));
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationCompoundingTypeOptions() {

        return Arrays.asList(interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.NONE),
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.FEE),
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.INTEREST),
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE));
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationNthDayTypeOptions() {
        return Arrays.asList(interestRecalculationCompoundingNthDayType(NthDayType.ONE),
                interestRecalculationCompoundingNthDayType(NthDayType.TWO), interestRecalculationCompoundingNthDayType(NthDayType.THREE),
                interestRecalculationCompoundingNthDayType(NthDayType.FOUR), interestRecalculationCompoundingNthDayType(NthDayType.LAST));
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationDayOfWeekTypeOptions() {
        return Arrays.asList(interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.SUNDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.MONDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.TUESDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.WEDNESDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.THURSDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.FRIDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.SATURDAY));
    }

    @Override
    public List<EnumOptionData> retrieveRescheduleStrategyTypeOptions() {

        return Arrays.asList(rescheduleStrategyType(LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT),
                rescheduleStrategyType(LoanRescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS),
                rescheduleStrategyType(LoanRescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS));
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationFrequencyTypeOptions() {

        return Arrays.asList(interestRecalculationFrequencyType(RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD),
                interestRecalculationFrequencyType(RecalculationFrequencyType.DAILY),
                interestRecalculationFrequencyType(RecalculationFrequencyType.WEEKLY),
                interestRecalculationFrequencyType(RecalculationFrequencyType.MONTHLY));
    }

    @Override
    public List<EnumOptionData> retrievePreCloseInterestCalculationStrategyOptions() {

        return Arrays.asList(preCloseInterestCalculationStrategy(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE),
                preCloseInterestCalculationStrategy(LoanPreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE));
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentStartDateTypeOptions() {
        return Arrays.asList(repaymentStartDateType(RepaymentStartDateType.DISBURSEMENT_DATE),
                repaymentStartDateType(RepaymentStartDateType.SUBMITTED_ON_DATE));
    }
}
