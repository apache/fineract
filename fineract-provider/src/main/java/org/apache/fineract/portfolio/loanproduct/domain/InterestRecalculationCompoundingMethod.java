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
 * <li>NONE</li>
 * <li>INTEREST</li>
 * <li>FEE</li>
 * <li>INTEREST_AND_FEE</li>
 * </ul>
 */
public enum InterestRecalculationCompoundingMethod {

    NONE(0, "interestRecalculationCompoundingMethod.none"), //
    INTEREST(1, "interestRecalculationCompoundingMethod.interest"), //
    FEE(2, "interestRecalculationCompoundingMethod.fee"), //
    INTEREST_AND_FEE(3, "interestRecalculationCompoundingMethod.interest.and.fee");

    private final Integer value;
    private final String code;

    private static final Map<Integer, InterestRecalculationCompoundingMethod> intToEnumMap = new HashMap<>();
    static {
        for (final InterestRecalculationCompoundingMethod type : InterestRecalculationCompoundingMethod.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestRecalculationCompoundingMethod fromInt(final Integer ruleTypeValue) {
        final InterestRecalculationCompoundingMethod type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestRecalculationCompoundingMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isFeeCompoundingEnabled() {
        return this.getValue().equals(InterestRecalculationCompoundingMethod.FEE.getValue())
                || this.getValue().equals(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.getValue());
    }

    public boolean isCompoundingEnabled() {
        return !this.getValue().equals(InterestRecalculationCompoundingMethod.NONE.getValue());
    }

    public boolean isInterestCompoundingEnabled() {
        return this.getValue().equals(InterestRecalculationCompoundingMethod.INTEREST.getValue())
                || this.getValue().equals(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE.getValue());
    }
}
