package org.mifosplatform.portfolio.savings.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public interface SavingsDropdownReadPlatformService {

    Collection<EnumOptionData> retrieveInterestRatePeriodFrequencyTypeOptions();

    Collection<EnumOptionData> retrieveLockinPeriodFrequencyTypeOptions();

    Collection<EnumOptionData> retrieveInterestPeriodTypeOptions();

    Collection<EnumOptionData> retrieveInterestCalculationTypeOptions();

    Collection<EnumOptionData> retrieveInterestCalculationDaysInYearTypeOptions();
}