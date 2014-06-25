/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

import java.util.HashMap;
import java.util.Map;

public enum GroupTypes {

    INVALID(0l, "lendingStrategy.invalid", "invalid"), //
    CENTER(1l, "groupTypes.center", "center"), //
    GROUP(2l, "groupTypes.group", "group"); //

    private Long id;
    private String code;
    private String value;

    private GroupTypes(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }

    private static final Map<Long, GroupTypes> intToEnumMap = new HashMap<>();
    private static long minValue;
    private static long maxValue;
    static {
        int i = 0;
        for (final GroupTypes type : GroupTypes.values()) {
            if (i == 0) {
                minValue = type.id;
            }
            intToEnumMap.put(type.id, type);
            if (minValue >= type.id) {
                minValue = type.id;
            }
            if (maxValue < type.id) {
                maxValue = type.id;
            }
            i = i + 1;
        }
    }

    public static GroupTypes fromInt(final int i) {
        final GroupTypes type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

    public static long getMinValue() {
        return minValue;
    }

    public static long getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return name().toString();
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }
}