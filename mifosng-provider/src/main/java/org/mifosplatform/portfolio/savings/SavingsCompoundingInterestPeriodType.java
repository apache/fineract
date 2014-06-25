/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * The compounding interest period is the span of time at the end of which
 * savings in a client's account earn interest.
 * </p>
 */
public enum SavingsCompoundingInterestPeriodType {

    INVALID(0, "savingsCompoundingInterestPeriodType.invalid"), //
    DAILY(1, "savingsCompoundingInterestPeriodType.daily"), //
    // WEEKLY(2, "savingsCompoundingInterestPeriodType.weekly"), //
    // BIWEEKLY(3, "savingsCompoundingInterestPeriodType.biweekly"), //
    MONTHLY(4, "savingsCompoundingInterestPeriodType.monthly"),

    QUATERLY(5, "savingsCompoundingInterestPeriodType.quarterly"), //
    BI_ANNUAL(6, "savingsCompoundingInterestPeriodType.biannual"), //
    ANNUAL(7, "savingsCompoundingInterestPeriodType.annual"); //

    // NO_COMPOUNDING_SIMPLE_INTEREST(8,
    // "savingsCompoundingInterestPeriodType.nocompounding");

    private final Integer value;
    private final String code;

    private SavingsCompoundingInterestPeriodType(final Integer value, final String code) {
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
        for (final SavingsCompoundingInterestPeriodType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public static SavingsCompoundingInterestPeriodType fromInt(final Integer type) {
        SavingsCompoundingInterestPeriodType repaymentFrequencyType = SavingsCompoundingInterestPeriodType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    repaymentFrequencyType = SavingsCompoundingInterestPeriodType.DAILY;
                break;
                case 2:
                // repaymentFrequencyType =
                // SavingsCompoundingInterestPeriodType.WEEKLY;
                break;
                case 3:
                // repaymentFrequencyType =
                // SavingsCompoundingInterestPeriodType.BIWEEKLY;
                break;
                case 4:
                    repaymentFrequencyType = SavingsCompoundingInterestPeriodType.MONTHLY;
                break;
                case 5:
                    repaymentFrequencyType = SavingsCompoundingInterestPeriodType.QUATERLY;
                break;
                case 6:
                    repaymentFrequencyType = SavingsCompoundingInterestPeriodType.BI_ANNUAL;
                break;
                case 7:
                    repaymentFrequencyType = SavingsCompoundingInterestPeriodType.ANNUAL;
                break;
                case 8:
                // repaymentFrequencyType =
                // SavingsCompoundingInterestPeriodType.NO_COMPOUNDING_SIMPLE_INTEREST;
                break;
            }
        }
        return repaymentFrequencyType;
    }
}