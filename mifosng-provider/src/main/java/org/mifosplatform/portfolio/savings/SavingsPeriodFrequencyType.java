/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import java.util.ArrayList;
import java.util.List;

/**
 * An enumeration of supported calendar periods used in savings.
 */
public enum SavingsPeriodFrequencyType {
    DAYS(0, "savingsPeriodFrequencyType.days"), //
    WEEKS(1, "savingsPeriodFrequencyType.weeks"), //
    MONTHS(2, "savingsPeriodFrequencyType.months"), //
    YEARS(3, "savingsPeriodFrequencyType.years"), //
    INVALID(4, "savingsPeriodFrequencyType.invalid");

    private final Integer value;
    private final String code;

    private SavingsPeriodFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static SavingsPeriodFrequencyType fromInt(final Integer type) {
        SavingsPeriodFrequencyType repaymentFrequencyType = SavingsPeriodFrequencyType.INVALID;
        if (type != null) {
            switch (type) {
                case 0:
                    repaymentFrequencyType = SavingsPeriodFrequencyType.DAYS;
                break;
                case 1:
                    repaymentFrequencyType = SavingsPeriodFrequencyType.WEEKS;
                break;
                case 2:
                    repaymentFrequencyType = SavingsPeriodFrequencyType.MONTHS;
                break;
                case 3:
                    repaymentFrequencyType = SavingsPeriodFrequencyType.YEARS;
                break;
            }
        }
        return repaymentFrequencyType;
    }

    public boolean isInvalid() {
        return this.value.equals(SavingsPeriodFrequencyType.INVALID.value);
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final SavingsPeriodFrequencyType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }
}
