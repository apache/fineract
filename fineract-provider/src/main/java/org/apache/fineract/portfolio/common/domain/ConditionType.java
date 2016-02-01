/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ConditionType {

    INVALID(0, "ConditionType.invalid"), //
    LESSTHAN(1, "ConditionType.lessthan"), //
    EQUAL(2, "ConditionType.equal"), //
    GRETERTHAN(3, "ConditionType.greterthan"), //
    NOT_EQUAL(4, "ConditionType.notequal");//

    private final Integer value;
    private final String code;

    private static final Map<Integer, ConditionType> intToEnumMap = new HashMap<>();
    static {
        for (final ConditionType type : ConditionType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static ConditionType fromInt(final Integer ruleTypeValue) {
        final ConditionType type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private ConditionType(final Integer value, final String code) {
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

    public boolean isConditionTypeEqual() {
        return ConditionType.EQUAL.getValue().equals(this.value);
    }

    public boolean isConditionTypeGreterThan() {
        return ConditionType.GRETERTHAN.getValue().equals(this.value);
    }

    public boolean isConditionTypeNotEqual() {
        return ConditionType.NOT_EQUAL.getValue().equals(this.value);
    }

    public boolean isConditionTypeLessThan() {
        return ConditionType.LESSTHAN.getValue().equals(this.value);
    }

    public boolean isInvalid() {
        return ConditionType.INVALID.getValue().equals(this.value);
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final ConditionType enumType : values()) {
            if (!enumType.isInvalid()) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }
}