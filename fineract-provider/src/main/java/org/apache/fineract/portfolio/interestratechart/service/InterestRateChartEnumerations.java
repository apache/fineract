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
package org.apache.fineract.portfolio.interestratechart.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

public class InterestRateChartEnumerations {

    public static EnumOptionData periodType(final Integer type) {
        return periodType(PeriodFrequencyType.fromInt(type));
    }

    public static EnumOptionData periodType(final PeriodFrequencyType type) {
        EnumOptionData optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(),
                PeriodFrequencyType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(),
                        PeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(),
                        PeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(),
                        PeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(),
                        PeriodFrequencyType.YEARS.getCode(), "Years");
            break;
        }

        return optionData;
    }

    public static List<EnumOptionData> periodType(final PeriodFrequencyType[] periodTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final PeriodFrequencyType periodType : periodTypes) {
            if (!periodType.isInvalid()) {
                optionDatas.add(periodType(periodType));
            }
        }
        return optionDatas;
    }
}