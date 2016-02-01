/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.HashMap;
import java.util.Map;

/***
 * <ul>
 * People typically use either of the following settings when defining interest
 * recalculation method:
 * <li>SAME_AS_REPAYMENT_PERIOD</li>
 * <li>DAILY</li>
 * <li>WEEKLY</li>
 * <li>FORTNIGHTLY</li>
 * <li>MONTHLY</li>
 * </ul>
 */

public enum InterestRecalculationPeriodMethod {
    INVALID(0, "interestRecalculationPeriodMethod.invalid"), //
    DAILY(1, "interestRecalculationPeriodMethod.daily"), //
    WEEKLY(2, "interestRecalculationPeriodMethod.weekly"), //
    FORTNIGHTLY(3, "interestRecalculationPeriodMethod.fortnightly"), //
    MONTHLY(4, "interestRecalculationPeriodMethod.monthly"), //
    SAME_AS_REPAYMENT_PERIOD(5, "interestRecalculationPeriodMethod.same.as.repayment.period");

    private final Integer value;
    private final String code;
    private static final Map<Integer, InterestRecalculationPeriodMethod> intToEnumMap = new HashMap<>();

    static {
        for (final InterestRecalculationPeriodMethod type : InterestRecalculationPeriodMethod.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestRecalculationPeriodMethod fromInt(final Integer ruleTypeValue) {
        final InterestRecalculationPeriodMethod type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestRecalculationPeriodMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }
}
