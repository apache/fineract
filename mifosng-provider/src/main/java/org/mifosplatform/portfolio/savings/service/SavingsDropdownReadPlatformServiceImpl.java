package org.mifosplatform.portfolio.savings.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestPeriodType;
import org.mifosplatform.portfolio.savings.domain.SavingsPeriodFrequencyType;
import org.springframework.stereotype.Service;

@Service
public class SavingsDropdownReadPlatformServiceImpl implements SavingsDropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrieveInterestRatePeriodFrequencyTypeOptions() {
        List<EnumOptionData> allowedInterestRatePeriodFrequencyTypeOptions = Arrays.asList( //
                SavingsEnumerations.interestRatePeriodFrequencyType(SavingsPeriodFrequencyType.MONTHS), //
                SavingsEnumerations.interestRatePeriodFrequencyType(SavingsPeriodFrequencyType.YEARS) //
                );

        return allowedInterestRatePeriodFrequencyTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveLockinPeriodFrequencyTypeOptions() {
        List<EnumOptionData> allowedLockinPeriodFrequencyTypeOptions = Arrays.asList( //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.DAYS), //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.WEEKS), //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.MONTHS), //
                SavingsEnumerations.lockinPeriodFrequencyType(SavingsPeriodFrequencyType.YEARS) //
                );

        return allowedLockinPeriodFrequencyTypeOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveInterestPeriodTypeOptions() {
        List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.interestPeriodType(SavingsInterestPeriodType.DAILY), //
                SavingsEnumerations.interestPeriodType(SavingsInterestPeriodType.WEEKLY), //
                SavingsEnumerations.interestPeriodType(SavingsInterestPeriodType.BIWEEKLY), //
                SavingsEnumerations.interestPeriodType(SavingsInterestPeriodType.MONTHLY), //
                SavingsEnumerations.interestPeriodType(SavingsInterestPeriodType.QUATERLY), //
                SavingsEnumerations.interestPeriodType(SavingsInterestPeriodType.SEMIANNUAL), //
                SavingsEnumerations.interestPeriodType(SavingsInterestPeriodType.ANNUAL) //
                );

        return allowedOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveInterestCalculationTypeOptions() {
        List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType.DAILY_BALANCE), //
                SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType.AVERAGE_DAILY_BALANCE) //
                );

        return allowedOptions;
    }

    @Override
    public Collection<EnumOptionData> retrieveInterestCalculationDaysInYearTypeOptions() {
        List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.DAYS_360), //
                SavingsEnumerations.interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.DAYS_365) //
                );

        return allowedOptions;
    }
}