/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.domain;

import java.util.ArrayList;
import java.util.List;


public enum PeriodFrequencyType {
    DAYS(0, "periodFrequencyType.days"), //
    WEEKS(1, "periodFrequencyType.weeks"), //
    MONTHS(2, "periodFrequencyType.months"), //
    YEARS(3, "periodFrequencyType.years"), //
    INVALID(4, "periodFrequencyType.invalid");

    private final Integer value;
    private final String code;

    private PeriodFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static PeriodFrequencyType fromInt(final Integer frequency) {
        PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.INVALID;
        if (frequency != null) {
            switch (frequency) {
                case 0:
                    repaymentFrequencyType = PeriodFrequencyType.DAYS;
                break;
                case 1:
                    repaymentFrequencyType = PeriodFrequencyType.WEEKS;
                break;
                case 2:
                    repaymentFrequencyType = PeriodFrequencyType.MONTHS;
                break;
                case 3:
                    repaymentFrequencyType = PeriodFrequencyType.YEARS;
                break;
            }
        }
        return repaymentFrequencyType;
    }

    public boolean isMonthly() {
        return this.value.equals(PeriodFrequencyType.MONTHS.getValue());
    }

    public boolean isYearly() {
        return this.value.equals(PeriodFrequencyType.YEARS.getValue());
    }
    
    public boolean isWeekly() {
        return this.value.equals(PeriodFrequencyType.WEEKS.getValue());
    }

    public boolean isDaily() {
        return this.value.equals(PeriodFrequencyType.DAYS.getValue());
    }
    
    public boolean isInvalid() {
        return this.value.equals(PeriodFrequencyType.INVALID.getValue());
    }
    
    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final PeriodFrequencyType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }
}
