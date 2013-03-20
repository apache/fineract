/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

public class SavingsEnumerations {

    public static EnumOptionData interestRatePeriodFrequencyType(final int id) {
        return interestRatePeriodFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData interestRatePeriodFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "savings.interest.rate.";
        EnumOptionData optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(),
                PeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.DAYS.getCode(), "Per day");
            break;
            case WEEKS:
                optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.WEEKS.getCode(), "Per week");
            break;
            case MONTHS:
                optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.MONTHS.getCode(), "Per month");
            break;
            case YEARS:
                optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.YEARS.getCode(), "Per year");
            break;
        }
        return optionData;
    }

    public static EnumOptionData lockinPeriodFrequencyType(final int id) {
        return lockinPeriodFrequencyType(PeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData lockinPeriodFrequencyType(final PeriodFrequencyType type) {
        final String codePrefix = "savings.lockin.";
        EnumOptionData optionData = new EnumOptionData(PeriodFrequencyType.INVALID.getValue().longValue(),
                PeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(PeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(PeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(PeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(PeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + PeriodFrequencyType.YEARS.getCode(), "Years");
            break;
        }
        return optionData;
    }
}