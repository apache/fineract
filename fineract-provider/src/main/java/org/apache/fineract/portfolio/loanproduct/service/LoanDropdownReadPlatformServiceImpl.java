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
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationFrequencyType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.loanCycleValueConditionType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.loanTermFrequencyType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.preCloseInterestCalculationStrategy;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.repaymentFrequencyDayOfWeekType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.repaymentFrequencyNthDayType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.repaymentFrequencyType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.rescheduleStrategyType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingNthDayType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingDayOfWeekType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.common.domain.DayOfWeekType;
import org.apache.fineract.portfolio.common.domain.NthDayType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductValueConditionType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class LoanDropdownReadPlatformServiceImpl implements LoanDropdownReadPlatformService {

    private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;

    @Autowired
    public LoanDropdownReadPlatformServiceImpl(final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository) {
        this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
    }

    @Override
    public List<EnumOptionData> retrieveLoanAmortizationTypeOptions() {

        final List<EnumOptionData> allowedAmortizationMethods = Arrays.asList(amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS),
                amortizationType(AmortizationMethod.EQUAL_PRINCIPAL));

        return allowedAmortizationMethods;
    }

    @Override
    public List<EnumOptionData> retrieveLoanInterestTypeOptions() {
        final List<EnumOptionData> allowedRepaymentScheduleCalculationMethods = Arrays.asList(interestType(InterestMethod.FLAT),
                interestType(InterestMethod.DECLINING_BALANCE));

        return allowedRepaymentScheduleCalculationMethods;
    }

    @Override
    public List<EnumOptionData> retrieveLoanInterestRateCalculatedInPeriodOptions() {

        final List<EnumOptionData> allowedOptions = Arrays.asList(interestCalculationPeriodType(InterestCalculationPeriodMethod.DAILY),
                interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD));

        return allowedOptions;
    }

    @Override
    public List<EnumOptionData> retrieveLoanTermFrequencyTypeOptions() {
        final List<EnumOptionData> loanTermFrequencyOptions = Arrays.asList(loanTermFrequencyType(PeriodFrequencyType.DAYS),
                loanTermFrequencyType(PeriodFrequencyType.WEEKS), loanTermFrequencyType(PeriodFrequencyType.MONTHS),
                loanTermFrequencyType(PeriodFrequencyType.YEARS));
        return loanTermFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentFrequencyTypeOptions() {

        final List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(repaymentFrequencyType(PeriodFrequencyType.DAYS),
                repaymentFrequencyType(PeriodFrequencyType.WEEKS), repaymentFrequencyType(PeriodFrequencyType.MONTHS));
        return repaymentFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentFrequencyOptionsForNthDayOfMonth() {
        final List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(repaymentFrequencyNthDayType(NthDayType.ONE),
                repaymentFrequencyNthDayType(NthDayType.TWO), repaymentFrequencyNthDayType(NthDayType.THREE),
                repaymentFrequencyNthDayType(NthDayType.FOUR), repaymentFrequencyNthDayType(NthDayType.LAST));
        return repaymentFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveRepaymentFrequencyOptionsForDaysOfWeek() {

        final List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(repaymentFrequencyDayOfWeekType(DayOfWeekType.SUNDAY),
                repaymentFrequencyDayOfWeekType(DayOfWeekType.MONDAY), repaymentFrequencyDayOfWeekType(DayOfWeekType.TUESDAY),
                repaymentFrequencyDayOfWeekType(DayOfWeekType.WEDNESDAY), repaymentFrequencyDayOfWeekType(DayOfWeekType.THURSDAY),
                repaymentFrequencyDayOfWeekType(DayOfWeekType.FRIDAY), repaymentFrequencyDayOfWeekType(DayOfWeekType.SATURDAY));
        return repaymentFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions() {
        // support for monthly and annual percentage rate (MPR) and (APR)
        final List<EnumOptionData> interestRateFrequencyTypeOptions = Arrays.asList(interestRateFrequencyType(PeriodFrequencyType.MONTHS),
                  interestRateFrequencyType(PeriodFrequencyType.YEARS), interestRateFrequencyType(PeriodFrequencyType.WHOLE_TERM));
        return interestRateFrequencyTypeOptions;
    }

    @Override
    public Collection<TransactionProcessingStrategyData> retreiveTransactionProcessingStrategies() {

        final Collection<TransactionProcessingStrategyData> strategyOptions = new ArrayList<>();
        Sort sort = new Sort("sortOrder");
        final List<LoanTransactionProcessingStrategy> strategies = this.loanTransactionProcessingStrategyRepository.findAll(sort);
        for (final LoanTransactionProcessingStrategy strategy : strategies) {
            strategyOptions.add(strategy.toData());
        }

        return strategyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveLoanCycleValueConditionTypeOptions() {

        final List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(
                loanCycleValueConditionType(LoanProductValueConditionType.EQUAL),
                loanCycleValueConditionType(LoanProductValueConditionType.GREATERTHAN));
        return repaymentFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationCompoundingTypeOptions() {

        final List<EnumOptionData> interestRecalculationCompoundingTypeOptions = Arrays.asList(
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.NONE),
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.FEE),
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.INTEREST),
                interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE));
        return interestRecalculationCompoundingTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationNthDayTypeOptions() {
        final List<EnumOptionData> interestRecalculationCompoundingNthDayTypeOptions = Arrays.asList(
                interestRecalculationCompoundingNthDayType(NthDayType.ONE), interestRecalculationCompoundingNthDayType(NthDayType.TWO),
                interestRecalculationCompoundingNthDayType(NthDayType.THREE), interestRecalculationCompoundingNthDayType(NthDayType.FOUR),
                interestRecalculationCompoundingNthDayType(NthDayType.LAST));
        return interestRecalculationCompoundingNthDayTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationDayOfWeekTypeOptions() {
        final List<EnumOptionData> interestRecalculationCompoundingNthDayTypeOptions = Arrays.asList(
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.SUNDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.MONDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.TUESDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.WEDNESDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.THURSDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.FRIDAY),
                interestRecalculationCompoundingDayOfWeekType(DayOfWeekType.SATURDAY));
        return interestRecalculationCompoundingNthDayTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveRescheduleStrategyTypeOptions() {

        final List<EnumOptionData> rescheduleStrategyTypeOptions = Arrays.asList(
                rescheduleStrategyType(LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT),
                rescheduleStrategyType(LoanRescheduleStrategyMethod.REDUCE_NUMBER_OF_INSTALLMENTS),
                rescheduleStrategyType(LoanRescheduleStrategyMethod.RESCHEDULE_NEXT_REPAYMENTS));
        return rescheduleStrategyTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveInterestRecalculationFrequencyTypeOptions() {

        final List<EnumOptionData> interestRecalculationFrequencyTypeOptions = Arrays.asList(
                interestRecalculationFrequencyType(RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD),
                interestRecalculationFrequencyType(RecalculationFrequencyType.DAILY),
                interestRecalculationFrequencyType(RecalculationFrequencyType.WEEKLY),
                interestRecalculationFrequencyType(RecalculationFrequencyType.MONTHLY));
        return interestRecalculationFrequencyTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrivePreCloseInterestCalculationStrategyOptions() {

        final List<EnumOptionData> preCloseInterestCalculationStrategyOptions = Arrays.asList(
                preCloseInterestCalculationStrategy(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE),
                preCloseInterestCalculationStrategy(LoanPreClosureInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE));
        return preCloseInterestCalculationStrategyOptions;
    }
}
