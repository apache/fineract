package org.mifosplatform.portfolio.common.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public interface DropdownReadPlatformService {

    List<EnumOptionData> retrievePeriodFrequencyTypeOptions();

    List<EnumOptionData> retrieveConditionTypeOptions();

    List<EnumOptionData> retrieveDaysInMonthTypeOptions();

    List<EnumOptionData> retrieveDaysInYearTypeOptions();
}
