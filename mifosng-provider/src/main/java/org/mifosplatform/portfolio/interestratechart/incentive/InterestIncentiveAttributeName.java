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

public enum InterestIncentiveAttributeName {

    INVALID(1, "InterestIncentiveAttributeName.invalid"), //
    GENDER(2, "InterestIncentiveAttributeName.gender"), //
    AGE(3, "InterestIncentiveAttributeName.age"), //
    CLIENT_TYPE(4, "InterestIncentiveAttributeName.clientType"), //
    CLIENT_CLASSIFICATION(5, "InterestIncentiveAttributeName.clientClassification"); //

    private final Integer value;
    private final String code;

    private static final Map<Integer, InterestIncentiveAttributeName> intToEnumMap = new HashMap<>();
    static {
        for (final InterestIncentiveAttributeName type : InterestIncentiveAttributeName.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static InterestIncentiveAttributeName fromInt(final Integer ruleTypeValue) {
        final InterestIncentiveAttributeName type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private InterestIncentiveAttributeName(final Integer value, final String code) {
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

    public boolean isGender() {
        return InterestIncentiveAttributeName.GENDER.getValue().equals(this.value);
    }

    public boolean isAge() {
        return InterestIncentiveAttributeName.AGE.getValue().equals(this.value);
    }

    public boolean isClientType() {
        return InterestIncentiveAttributeName.CLIENT_TYPE.getValue().equals(this.value);
    }

    public boolean isClientClassification() {
        return InterestIncentiveAttributeName.CLIENT_CLASSIFICATION.getValue().equals(this.value);
    }

    public boolean isInvalid() {
        return InterestIncentiveAttributeName.INVALID.getValue().equals(this.value);
    }

    public static boolean isCodeValueAttribute(InterestIncentiveAttributeName attributeName) {
        boolean isCodeValue = false;
        switch (attributeName) {
            case GENDER:
            case CLIENT_TYPE:
            case CLIENT_CLASSIFICATION:
                isCodeValue = true;
            break;
            default:
            break;
        }
        return isCodeValue;
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final InterestIncentiveAttributeName enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

}