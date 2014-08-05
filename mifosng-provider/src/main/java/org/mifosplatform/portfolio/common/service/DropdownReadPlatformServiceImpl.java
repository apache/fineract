/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.service;

import static org.mifosplatform.portfolio.common.service.CommonEnumerations.conditionType;
import static org.mifosplatform.portfolio.common.service.CommonEnumerations.termFrequencyType;
import static org.mifosplatform.portfolio.common.service.CommonEnumerations.daysInMonthType;
import static org.mifosplatform.portfolio.common.service.CommonEnumerations.daysInYearType;

import java.util.Arrays;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.common.domain.ConditionType;
import org.mifosplatform.portfolio.common.domain.DaysInMonthType;
import org.mifosplatform.portfolio.common.domain.DaysInYearType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.stereotype.Service;

@Service
public class DropdownReadPlatformServiceImpl implements DropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrievePeriodFrequencyTypeOptions() {
        final List<EnumOptionData> loanTermFrequencyOptions = Arrays.asList(termFrequencyType(PeriodFrequencyType.DAYS, "frequency"),
                termFrequencyType(PeriodFrequencyType.WEEKS, "frequency"), termFrequencyType(PeriodFrequencyType.MONTHS, "frequency"),
                termFrequencyType(PeriodFrequencyType.YEARS, "frequency"));
        return loanTermFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveConditionTypeOptions() {
        final List<EnumOptionData> loanTermFrequencyOptions = Arrays.asList(conditionType(ConditionType.EQUAL, "condition"),
                conditionType(ConditionType.NOT_EQUAL, "condition"), conditionType(ConditionType.GRETERTHAN, "condition"),
                conditionType(ConditionType.LESSTHAN, "condition"));
        return loanTermFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveDaysInMonthTypeOptions() {

        final List<EnumOptionData> daysInMonthTypeOptions = Arrays.asList(daysInMonthType(DaysInMonthType.ACTUAL),
                daysInMonthType(DaysInMonthType.DAYS_30));
        return daysInMonthTypeOptions;
    }

    @Override
    public List<EnumOptionData> retrieveDaysInYearTypeOptions() {

        final List<EnumOptionData> daysInYearTypeOptions = Arrays.asList(daysInYearType(DaysInYearType.ACTUAL),
                daysInYearType(DaysInYearType.DAYS_360), daysInYearType(DaysInYearType.DAYS_364), daysInYearType(DaysInYearType.DAYS_365));
        return daysInYearTypeOptions;
    }

}
