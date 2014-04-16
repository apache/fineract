/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart;

import java.util.ArrayList;
import java.util.List;


/**
 * An enumeration of supported calendar periods used in savings.
 */
public enum InterestRateChartPeriodType {
    
    DAYS(0, "interestChartPeriodType.days"), //
    WEEKS(1, "interestChartPeriodType.weeks"), //
    MONTHS(2, "interestChartPeriodType.months"), //
    YEARS(3, "interestChartPeriodType.years"), //
    INVALID(4, "interestChartPeriodType.invalid");

    private final Integer value;
    private final String code;

    private InterestRateChartPeriodType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static InterestRateChartPeriodType fromInt(final Integer type) {
        InterestRateChartPeriodType periodType = InterestRateChartPeriodType.INVALID;
        if (type != null) {
            switch (type) {
                case 0:
                    periodType = InterestRateChartPeriodType.DAYS;
                break;
                case 1:
                    periodType = InterestRateChartPeriodType.WEEKS;
                break;
                case 2:
                    periodType = InterestRateChartPeriodType.MONTHS;
                break;
                case 3:
                    periodType = InterestRateChartPeriodType.YEARS;
                break;
            }
        }
        return periodType;
    }
    
    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<Integer>();
        for (final InterestRateChartPeriodType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }
    
    public boolean isInvalid(){
        return this.value.equals(InterestRateChartPeriodType.INVALID.value);
    }
}
