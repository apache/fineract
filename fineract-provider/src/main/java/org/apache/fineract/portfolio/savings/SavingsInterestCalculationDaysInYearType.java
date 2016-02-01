/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import java.util.ArrayList;
import java.util.List;

/**
 * <ul>
 * People typically use either of the following settings when calculating there
 * interest using the daily method:
 * <li>360 and</li>
 * <li>365</li>
 * </ul>
 */
public enum SavingsInterestCalculationDaysInYearType {

    INVALID(0, "savingsInterestCalculationDaysInYearType.invalid"), //
    DAYS_360(360, "savingsInterestCalculationDaysInYearType.days360"), //
    DAYS_365(365, "savingsInterestCalculationDaysInYearType.days365");

    private final Integer value;
    private final String code;

    private SavingsInterestCalculationDaysInYearType(final Integer value, final String code) {
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
        for (final SavingsInterestCalculationDaysInYearType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public static SavingsInterestCalculationDaysInYearType fromInt(final Integer type) {
        SavingsInterestCalculationDaysInYearType repaymentFrequencyType = SavingsInterestCalculationDaysInYearType.INVALID;
        if (type != null) {
            switch (type) {
                case 360:
                    repaymentFrequencyType = SavingsInterestCalculationDaysInYearType.DAYS_360;
                break;
                case 365:
                    repaymentFrequencyType = SavingsInterestCalculationDaysInYearType.DAYS_365;
                break;
            }
        }
        return repaymentFrequencyType;
    }
}