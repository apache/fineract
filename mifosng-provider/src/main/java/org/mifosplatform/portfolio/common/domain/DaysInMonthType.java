/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.domain;
/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 * People typically use either of the following settings when calculating there
 * interest using the daily method:
 * <li>Actual or</li>
 * <li>30</li>
 * </ul>
 */
public enum DaysInMonthType {

    INVALID(0, "DaysInMonthType.invalid"), //
    ACTUAL(1, "DaysInMonthType.actual"), //
    DAYS_30(30, "DaysInMonthType.days360");

    private final Integer value;
    private final String code;

    private DaysInMonthType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final DaysInMonthType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public static DaysInMonthType fromInt(final Integer type) {
        DaysInMonthType repaymentFrequencyType = DaysInMonthType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    repaymentFrequencyType = DaysInMonthType.ACTUAL;
                break;
                case 30:
                    repaymentFrequencyType = DaysInMonthType.DAYS_30;
                break;

            }
        }
        return repaymentFrequencyType;
    }

    public boolean isDaysInMonth_30() {
        return DaysInMonthType.DAYS_30.getValue().equals(this.value);
    }
}