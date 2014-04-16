/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.interestratechart.InterestRateChartPeriodType;

public class InterestRateChartEnumerations {

    public static EnumOptionData periodType(final Integer type) {
        return periodType(InterestRateChartPeriodType.fromInt(type));
    }

    public static EnumOptionData periodType(final InterestRateChartPeriodType type) {
        EnumOptionData optionData = new EnumOptionData(InterestRateChartPeriodType.INVALID.getValue().longValue(),
                InterestRateChartPeriodType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(InterestRateChartPeriodType.DAYS.getValue().longValue(),
                        InterestRateChartPeriodType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(InterestRateChartPeriodType.WEEKS.getValue().longValue(),
                        InterestRateChartPeriodType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(InterestRateChartPeriodType.MONTHS.getValue().longValue(),
                        InterestRateChartPeriodType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(InterestRateChartPeriodType.YEARS.getValue().longValue(),
                        InterestRateChartPeriodType.YEARS.getCode(), "Years");
            break;
        }

        return optionData;
    }

    public static List<EnumOptionData> periodType(final InterestRateChartPeriodType[] periodTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<EnumOptionData>();
        for (final InterestRateChartPeriodType periodType : periodTypes) {
            if (!periodType.isInvalid()) {
                optionDatas.add(periodType(periodType));
            }
        }
        return optionDatas;
    }
}