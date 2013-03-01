package org.mifosplatform.portfolio.calendar.domain;

import java.util.HashMap;
import java.util.Map;

public enum CalendarType {

    COLLECTION(1, "calendarType.collection"), TRAINING(2, "calendarType.training"), AUDIT(3, "calendarType.audit"), GENERAL(4,
            "calendarType.general");

    private final Integer value;
    private final String code;

    private CalendarType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, CalendarType> intToEnumMap = new HashMap<Integer, CalendarType>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (final CalendarType type : CalendarType.values()) {
            if (i == 0) {
                minValue = type.value;
            }
            intToEnumMap.put(type.value, type);
            if (minValue >= type.value) {
                minValue = type.value;
            }
            if (maxValue < type.value) {
                maxValue = type.value;
            }
            i = i + 1;
        }
    }

    public static CalendarType fromInt(final int i) {
        final CalendarType type = intToEnumMap.get(Integer.valueOf(i));
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
}
