/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.domain;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

public enum CalendarWeekDaysType {

    INVALID(0, "calendarWeekDaysType.invalid"), MO(1, "calendarWeekDaysType.monday"), TU(2, "calendarWeekDaysType.tuesday"), WE(3,
            "calendarWeekDaysType.wednesday"), TH(4, "calendarWeekDaysType.thursday"), FR(5, "calendarWeekDaysType.friday"), SA(6,
            "calendarWeekDaysType.saturday"), SU(7, "calendarWeekDaysType.sunday");

    private final Integer value;
    private final String code;

    private CalendarWeekDaysType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, CalendarWeekDaysType> intToEnumMap = new HashMap<>();
    private static int minValue = CalendarWeekDaysType.MO.value;
    private static int maxValue = CalendarWeekDaysType.SU.value;

    static {
        for (final CalendarWeekDaysType type : CalendarWeekDaysType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static CalendarWeekDaysType fromInt(final int i) {
        final CalendarWeekDaysType type = intToEnumMap.get(Integer.valueOf(i));
        return type;
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

    public boolean isInvalid() {
        return this.value.equals(CalendarWeekDaysType.INVALID.value);
    }

    public static CalendarWeekDaysType fromString(final String weekDayString) {
        CalendarWeekDaysType weekDay = CalendarWeekDaysType.INVALID;

        if (StringUtils.isEmpty(weekDayString)) return weekDay;

        if (weekDayString.equalsIgnoreCase(CalendarWeekDaysType.MO.toString())) {
            weekDay = CalendarWeekDaysType.MO;
        } else if (weekDayString.equalsIgnoreCase(CalendarWeekDaysType.TU.toString())) {
            weekDay = CalendarWeekDaysType.TU;
        } else if (weekDayString.equalsIgnoreCase(CalendarWeekDaysType.WE.toString())) {
            weekDay = CalendarWeekDaysType.WE;
        } else if (weekDayString.equalsIgnoreCase(CalendarWeekDaysType.TH.toString())) {
            weekDay = CalendarWeekDaysType.TH;
        } else if (weekDayString.equalsIgnoreCase(CalendarWeekDaysType.FR.toString())) {
            weekDay = CalendarWeekDaysType.FR;
        } else if (weekDayString.equalsIgnoreCase(CalendarWeekDaysType.SA.toString())) {
            weekDay = CalendarWeekDaysType.SA;
        } else if (weekDayString.equalsIgnoreCase(CalendarWeekDaysType.SU.toString())) {
            weekDay = CalendarWeekDaysType.SU;
        }

        return weekDay;
    }
}
