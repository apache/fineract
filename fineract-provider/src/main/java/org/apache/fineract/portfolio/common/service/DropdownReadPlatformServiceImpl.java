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
package org.apache.fineract.portfolio.common.service;

import static org.apache.fineract.portfolio.common.service.CommonEnumerations.conditionType;
import static org.apache.fineract.portfolio.common.service.CommonEnumerations.daysInMonthType;
import static org.apache.fineract.portfolio.common.service.CommonEnumerations.daysInYearType;
import static org.apache.fineract.portfolio.common.service.CommonEnumerations.termFrequencyType;

import java.util.Arrays;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.common.domain.ConditionType;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.stereotype.Service;

@Service
public class DropdownReadPlatformServiceImpl implements DropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrievePeriodFrequencyTypeOptions() {
        final List<EnumOptionData> loanTermFrequencyOptions = Arrays.asList(termFrequencyType(PeriodFrequencyType.DAYS, "frequency"),
                termFrequencyType(PeriodFrequencyType.WEEKS, "frequency"), termFrequencyType(PeriodFrequencyType.MONTHS, "frequency"),
                 termFrequencyType(PeriodFrequencyType.YEARS, "frequency"), termFrequencyType(PeriodFrequencyType.WHOLE_TERM, "frequency"));
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
