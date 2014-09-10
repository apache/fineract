/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.HashMap;
import java.util.Map;

public enum RecalculationFrequencyType {
    INVALID(0, "interestRecalculationFrequencyType.invalid"), //
    SAME_AS_REPAYMENT_PERIOD(1, "interestRecalculationFrequencyType.same.as.repayment.period"), //
    DAILY(2, "interestRecalculationFrequencyType.daily"), //
    WEEKLY(3, "interestRecalculationFrequencyType.weekly"), //
    MONTHLY(4, "interestRecalculationFrequencyType.monthly");

    private final Integer value;
    private final String code;
    private static final Map<Integer, RecalculationFrequencyType> intToEnumMap = new HashMap<>();

    static {
        for (final RecalculationFrequencyType type : RecalculationFrequencyType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static RecalculationFrequencyType fromInt(final Integer ruleTypeValue) {
        if (ruleTypeValue == null) { return RecalculationFrequencyType.INVALID; }
        final RecalculationFrequencyType type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private RecalculationFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isSameAsRepayment() {
        return this.value.equals(RecalculationFrequencyType.SAME_AS_REPAYMENT_PERIOD.getValue());
    }

    public boolean isDaily() {
        return this.value.equals(RecalculationFrequencyType.DAILY.getValue());
    }

    public boolean isWeekly() {
        return this.value.equals(RecalculationFrequencyType.WEEKLY.getValue());
    }

    public boolean isMonthly() {
        return this.value.equals(RecalculationFrequencyType.MONTHLY.getValue());
    }
}
