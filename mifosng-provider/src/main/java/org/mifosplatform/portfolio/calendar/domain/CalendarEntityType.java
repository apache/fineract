/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.domain;

import java.util.HashMap;
import java.util.Map;

public enum CalendarEntityType {

    INVALID(0, "calendarEntityType.invalid"), //
    CLIENTS(1, "calendarEntityType.clients"), //
    GROUPS(2, "calendarEntityType.groups"), //
    LOANS(3, "calendarEntityType.loans"), //
    CENTERS(4, "calendarEntityType.centers"), //
    SAVINGS(5, "calendarEntityType.savings"), //
    LOAN_RECALCULATION_DETAIL(6, "calendarEntityType.loan.recalculation.detail");

    private final Integer value;
    private final String code;

    private CalendarEntityType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, CalendarEntityType> intToEnumMap = new HashMap<>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (final CalendarEntityType entityType : CalendarEntityType.values()) {
            if (i == 0) {
                minValue = entityType.value;
            }
            intToEnumMap.put(entityType.value, entityType);
            if (minValue >= entityType.value) {
                minValue = entityType.value;
            }
            if (maxValue < entityType.value) {
                maxValue = entityType.value;
            }
            i = i + 1;
        }
    }

    public static CalendarEntityType fromInt(final int i) {
        final CalendarEntityType entityType = intToEnumMap.get(Integer.valueOf(i));
        return entityType;
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

    public static boolean isGroup(final Integer value) {
        return CalendarEntityType.GROUPS.value.equals(value) ? true : false;
    }

    public static boolean isGroup(final String name) {
        return CalendarEntityType.GROUPS.name().equalsIgnoreCase(name) ? true : false;
    }

    public static boolean isCenter(final Integer value) {
        return CalendarEntityType.CENTERS.value.equals(value) ? true : false;
    }

    public static boolean isCenter(final String name) {
        return CalendarEntityType.CENTERS.name().equalsIgnoreCase(name) ? true : false;
    }

    public static boolean isLoan(final Integer value) {
        return CalendarEntityType.LOANS.value.equals(value) ? true : false;
    }

    public static boolean isLoan(final String name) {
        return CalendarEntityType.LOANS.name().equalsIgnoreCase(name) ? true : false;
    }

    public static boolean isClient(final Integer value) {
        return CalendarEntityType.CLIENTS.value.equals(value) ? true : false;
    }

    public static boolean isClient(final String name) {
        return CalendarEntityType.CLIENTS.name().equalsIgnoreCase(name) ? true : false;
    }

    public boolean isCenter() {
        return this.value.equals(CalendarEntityType.CENTERS.getValue());
    }

    public boolean isGroup() {
        return this.value.equals(CalendarEntityType.GROUPS.getValue());
    }

    public boolean isLoan() {
        return this.value.equals(CalendarEntityType.LOANS.getValue());
    }

    public boolean isClient() {
        return this.value.equals(CalendarEntityType.CLIENTS.getValue());
    }

    private static final Map<String, CalendarEntityType> entityNameToEnumMap = new HashMap<>();

    static {
        for (final CalendarEntityType entityType : CalendarEntityType.values()) {
            entityNameToEnumMap.put(entityType.name().toLowerCase(), entityType);
        }
    }

    public static CalendarEntityType getEntityType(String entityType) {
        return entityNameToEnumMap.get(entityType.toLowerCase());
    }

    public static boolean isSavings(final Integer value) {
        return CalendarEntityType.SAVINGS.value.equals(value) ? true : false;
    }

    public static boolean isSavings(final String name) {
        return CalendarEntityType.SAVINGS.name().equalsIgnoreCase(name) ? true : false;
    }

    public boolean isSavings() {
        return this.value.equals(CalendarEntityType.SAVINGS.getValue());
    }

}
