/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;

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