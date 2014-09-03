/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.service;

import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.amortizationType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.interestCalculationPeriodType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.interestRateFrequencyType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.interestType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.loanCycleValueConditionType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.loanTermFrequencyType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.repaymentFrequencyType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.rescheduleStrategyType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationFrequencyType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductValueConditionType;
import org.mifosplatform.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<EnumOptionData> retrieveInterestRateFrequencyTypeOptions() {
        // support for monthly and annual percentage rate (MPR) and (APR)
        final List<EnumOptionData> interestRateFrequencyTypeOptions = Arrays.asList(interestRateFrequencyType(PeriodFrequencyType.MONTHS),
                interestRateFrequencyType(PeriodFrequencyType.YEARS));
        return interestRateFrequencyTypeOptions;
    }

    @Override
    public Collection<TransactionProcessingStrategyData> retreiveTransactionProcessingStrategies() {

        final Collection<TransactionProcessingStrategyData> strategyOptions = new ArrayList<>();

        final List<LoanTransactionProcessingStrategy> strategies = this.loanTransactionProcessingStrategyRepository.findAll();
        for (final LoanTransactionProcessingStrategy strategy : strategies) {
            strategyOptions.add(strategy.toData());
        }

        return strategyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveLoanCycleValueConditionTypeOptions() {

        final List<EnumOptionData> repaymentFrequencyOptions = Arrays.asList(
                loanCycleValueConditionType(LoanProductValueConditionType.EQUAL),
                loanCycleValueConditionType(LoanProductValueConditionType.GRETERTHAN));
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

}