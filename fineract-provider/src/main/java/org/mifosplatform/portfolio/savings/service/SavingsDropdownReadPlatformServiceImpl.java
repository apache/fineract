/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsWithdrawalFeesType;
import org.springframework.stereotype.Service;

@Service
public class SavingsDropdownReadPlatformServiceImpl implements SavingsDropdownReadPlatformService {

    @Override
    public Collection<EnumOptionData> retrievewithdrawalFeeTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.withdrawalFeeType(SavingsWithdrawalFeesType.FLAT), //
                SavingsEnumerations.withdrawalFeeType(SavingsWithdrawalFeesType.PERCENT_OF_AMOUNT) //
                );

        return allowedOptions;
    }

    @Override
    public List<EnumOptionData> retrieveLockinPeriodFrequencyTypeOptions() {
        final List<EnumOptionData> allowedLockinPeriodFrequencyTypeOptions = Arrays.asList( //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.DAYS), //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.WEEKS), //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.MONTHS), //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.YEARS) //
                );

        return allowedLockinPeriodFrequencyTypeOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveCompoundingInterestPeriodTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList(
                //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.DAILY), //
                // SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.WEEKLY),
                // //
                // SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.BIWEEKLY),
                // //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.MONTHLY),
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.QUATERLY),
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.BI_ANNUAL),
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.ANNUAL)
        // //
        // SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.NO_COMPOUNDING_SIMPLE_INTEREST)
        // //
                );

        return allowedOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveInterestPostingPeriodTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType.MONTHLY), //
                SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType.QUATERLY), //
                SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType.BIANNUAL), //
                SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType.ANNUAL) //
                );

        return allowedOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveInterestCalculationTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType.DAILY_BALANCE), //
                SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType.AVERAGE_DAILY_BALANCE) //
                );

        return allowedOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveInterestCalculationDaysInYearTypeOptions() {
        final List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.DAYS_360), //
                SavingsEnumerations.interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.DAYS_365) //
                );

        return allowedOptions;
    }
}