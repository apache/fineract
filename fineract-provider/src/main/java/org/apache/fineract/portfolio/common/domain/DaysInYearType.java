/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 * People typically use either of the following settings when calculating there
 * interest using the daily method:
 * <li>Actual or</li>
 * <li>360 or</li>
 * <li>364 or</li>
 * <li>365</li>
 * </ul>
 */
public enum DaysInYearType {

    INVALID(0, "DaysInYearType.invalid"), //
    ACTUAL(1, "DaysInYearType.actual"), //
    DAYS_360(360, "DaysInYearType.days360"), //
    DAYS_364(364, "DaysInYearType.days364"), //
    DAYS_365(365, "DaysInYearType.days365");

    private final Integer value;
    private final String code;

    private DaysInYearType(final Integer value, final String code) {
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
        for (final DaysInYearType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public static DaysInYearType fromInt(final Integer type) {
        DaysInYearType repaymentFrequencyType = DaysInYearType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    repaymentFrequencyType = DaysInYearType.ACTUAL;
                break;
                case 360:
                    repaymentFrequencyType = DaysInYearType.DAYS_360;
                break;
                case 364:
                    repaymentFrequencyType = DaysInYearType.DAYS_364;
                break;
                case 365:
                    repaymentFrequencyType = DaysInYearType.DAYS_365;
                break;
            }
        }
        return repaymentFrequencyType;
    }

    public boolean isActual() {
        return DaysInYearType.ACTUAL.getValue().equals(this.value);
    }
}