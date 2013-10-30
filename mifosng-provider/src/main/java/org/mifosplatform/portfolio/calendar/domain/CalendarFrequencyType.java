/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.domain;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

public enum CalendarFrequencyType {

    INVALID(0, "calendarFrequencyType.invalid"), DAILY(1, "calendarFrequencyType.daily"), WEEKLY(2, "calendarFrequencyType.weekly"), MONTHLY(
            3, "calendarFrequencyType.monthly"), YEARLY(4, "calendarFrequencyType.yearly");

    private final Integer value;
    private final String code;

    private CalendarFrequencyType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, CalendarFrequencyType> intToEnumMap = new HashMap<Integer, CalendarFrequencyType>();
    private static int minValue = CalendarFrequencyType.DAILY.value;
    private static int maxValue = CalendarFrequencyType.YEARLY.value;

    static {
        for (final CalendarFrequencyType type : CalendarFrequencyType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }
    
    public static CalendarFrequencyType fromInt(final int i) {
        final CalendarFrequencyType type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

    public static CalendarFrequencyType fromString(final String frequencyString) {
        CalendarFrequencyType frequency = CalendarFrequencyType.INVALID;

        if (StringUtils.isEmpty(frequencyString)) return frequency;

        if (frequencyString.equalsIgnoreCase(CalendarFrequencyType.DAILY.toString())) {
            frequency = CalendarFrequencyType.DAILY;
        } else if (frequencyString.equalsIgnoreCase(CalendarFrequencyType.WEEKLY.toString())) {
            frequency = CalendarFrequencyType.WEEKLY;
        } else if (frequencyString.equalsIgnoreCase(CalendarFrequencyType.MONTHLY.toString())) {
            frequency = CalendarFrequencyType.MONTHLY;
        } else if (frequencyString.equalsIgnoreCase(CalendarFrequencyType.YEARLY.toString())) {
            frequency = CalendarFrequencyType.YEARLY;
        }

        return frequency;
    }
    
    public static int getMinValue() {
        return minValue;
    }

    public static int getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return name().toString();
    }
    
    public boolean isWeekly(){
        return this.value.equals(CalendarFrequencyType.WEEKLY.value);
    }
    
    public boolean isInvalid(){
        return this.value.equals(CalendarFrequencyType.INVALID.value);
    }
}
