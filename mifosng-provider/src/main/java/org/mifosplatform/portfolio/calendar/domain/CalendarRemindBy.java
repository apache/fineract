package org.mifosplatform.portfolio.calendar.domain;

import java.util.HashMap;
import java.util.Map;

public enum CalendarRemindBy {

    SMS(1, "calendarRemindBy.sms"), EMAIL(2, "calendarRemindBy.email"), SYSTEMALERT(3, "calendarRemindBy.systemalert");

    private final Integer value;
    private final String code;

    private CalendarRemindBy(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, CalendarRemindBy> intToEnumMap = new HashMap<Integer, CalendarRemindBy>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (final CalendarRemindBy remindBy : CalendarRemindBy.values()) {
            if (i == 0) {
                minValue = remindBy.value;
            }
            intToEnumMap.put(remindBy.value, remindBy);
            if (minValue >= remindBy.value) {
                minValue = remindBy.value;
            }
            if (maxValue < remindBy.value) {
                maxValue = remindBy.value;
            }
            i = i + 1;
        }
    }

    public static CalendarRemindBy fromInt(final int i) {
        final CalendarRemindBy remindBy = intToEnumMap.get(Integer.valueOf(i));
        return remindBy;
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
