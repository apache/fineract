package org.mifosplatform.portfolio.calendar.domain;

import java.util.HashMap;
import java.util.Map;

public enum CalendarEntityType {

    CLIENTS(1, "calendarEntityType.clients"), GROUPS(2, "calendarEntityType.groups"), LOANS(3, "calendarEntityType.loans"), CENTERS(4,
            "calendarEntityType.centers");

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

    private static final Map<Integer, CalendarEntityType> intToEnumMap = new HashMap<Integer, CalendarEntityType>();
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
}
