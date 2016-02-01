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

public enum InterestIncentiveEntityType {

    INVALID(1, "InterestIncentiveEntityType.invalid"), //
    CUSTOMER(2, "InterestIncentiveEntityType.customer"), //
    ACCOUNT(3, "InterestIncentiveEntityType.account"); //

    private final Integer value;
    private final String code;

    private static final Map<Integer, InterestIncentiveEntityType> intToEnumMap = new HashMap<>();
    static {
        for (final InterestIncentiveEntityType type : InterestIncentiveEntityType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestIncentiveEntityType fromInt(final Integer ruleTypeValue) {
        final InterestIncentiveEntityType type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestIncentiveEntityType(final Integer value, final String code) {
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

    public boolean isCustomer() {
        return InterestIncentiveEntityType.CUSTOMER.getValue().equals(this.value);
    }

    public boolean isInvalid() {
        return InterestIncentiveEntityType.INVALID.getValue().equals(this.value);
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final InterestIncentiveEntityType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }
}