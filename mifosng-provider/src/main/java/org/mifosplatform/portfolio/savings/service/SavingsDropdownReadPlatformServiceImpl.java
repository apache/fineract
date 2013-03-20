package org.mifosplatform.portfolio.savings.service;

import java.util.Arrays;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.springframework.stereotype.Service;

@Service
public class SavingsDropdownReadPlatformServiceImpl implements SavingsDropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrieveInterestRatePeriodFrequencyTypeOptions() {
        List<EnumOptionData> allowedInterestRatePeriodFrequencyTypeOptions = Arrays.asList( //
                SavingsEnumerations.interestRatePeriodFrequencyType(PeriodFrequencyType.MONTHS), //
                SavingsEnumerations.interestRatePeriodFrequencyType(PeriodFrequencyType.YEARS) //
                );

        return allowedInterestRatePeriodFrequencyTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveLockinPeriodFrequencyTypeOptions() {
        List<EnumOptionData> allowedLockinPeriodFrequencyTypeOptions = Arrays.asList( //
                SavingsEnumerations.lockinPeriodFrequencyType(PeriodFrequencyType.DAYS), //
                SavingsEnumerations.lockinPeriodFrequencyType(PeriodFrequencyType.WEEKS), //
                SavingsEnumerations.lockinPeriodFrequencyType(PeriodFrequencyType.MONTHS), //
                SavingsEnumerations.lockinPeriodFrequencyType(PeriodFrequencyType.YEARS) //
                );

        return allowedLockinPeriodFrequencyTypeOptions;
    }
}