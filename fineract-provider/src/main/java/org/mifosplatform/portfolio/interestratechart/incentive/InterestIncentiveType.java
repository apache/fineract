/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.incentive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum InterestIncentiveType {

    INVALID(1, "InterestIncentiveType.invalid"), //
    FIXED(2, "InterestIncentiveType.fixed"), //
    INCENTIVE(3, "InterestIncentiveType.incentive"); //

    private final Integer value;
    private final String code;

    private static final Map<Integer, InterestIncentiveType> intToEnumMap = new HashMap<>();
    static {
        for (final InterestIncentiveType type : InterestIncentiveType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestIncentiveType fromInt(final Integer ruleTypeValue) {
        final InterestIncentiveType type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestIncentiveType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    @Override
    public String toString() {
        return name().toString().replaceAll("_", " ");
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isIncentive() {
        return InterestIncentiveType.INCENTIVE.getValue().equals(this.value);
    }

    public boolean isFixed() {
        return InterestIncentiveType.FIXED.getValue().equals(this.value);
    }

    public boolean isInvalid() {
        return InterestIncentiveType.INVALID.getValue().equals(this.value);
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final InterestIncentiveType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

}