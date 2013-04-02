package org.mifosplatform.portfolio.savings.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.savings.domain.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.domain.SavingsPeriodFrequencyType;
import org.springframework.stereotype.Service;

@Service
public class SavingsDropdownReadPlatformServiceImpl implements SavingsDropdownReadPlatformService {

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
    public Collection<EnumOptionData> retrieveCompoundingInterestPeriodTypeOptions() {
        List<EnumOptionData> allowedOptions = Arrays.asList( //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.DAILY), //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.WEEKLY), //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.BIWEEKLY), //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.MONTHLY), //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.QUATERLY), //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.BI_ANNUAL), //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.ANNUAL), //
                SavingsEnumerations.compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.NO_COMPOUNDING_SIMPLE_INTEREST) //
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