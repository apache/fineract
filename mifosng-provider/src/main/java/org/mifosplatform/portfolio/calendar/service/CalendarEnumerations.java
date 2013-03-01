package org.mifosplatform.portfolio.calendar.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarRemindBy;
import org.mifosplatform.portfolio.calendar.domain.CalendarType;

public class CalendarEnumerations {

    public static EnumOptionData calendarEntityType(final int id) {
        return calendarEntityType(CalendarEntityType.fromInt(id));
    }

    public static EnumOptionData calendarEntityType(final CalendarEntityType entityType) {
        final EnumOptionData optionData = new EnumOptionData(entityType.getValue().longValue(), entityType.getCode(), entityType.toString());
        return optionData;
    }

    public static List<EnumOptionData> calendarEntityType(final CalendarEntityType[] entityTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<EnumOptionData>();
        for (final CalendarEntityType entityType : entityTypes) {
            optionDatas.add(calendarEntityType(entityType));
        }
        return optionDatas;
    }

    public static EnumOptionData calendarType(final int id) {
        return calendarType(CalendarType.fromInt(id));
    }

    public static EnumOptionData calendarType(final CalendarType type) {
        final EnumOptionData optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), type.toString());
        return optionData;
    }

    public static List<EnumOptionData> calendarType(final CalendarType[] types) {
        final List<EnumOptionData> optionDatas = new ArrayList<EnumOptionData>();
        for (final CalendarType type : types) {
            optionDatas.add(calendarType(type));
        }
        return optionDatas;
    }

    public static EnumOptionData calendarRemindBy(final int id) {
        return calendarRemindBy(CalendarRemindBy.fromInt(id));
    }

    public static EnumOptionData calendarRemindBy(final CalendarRemindBy remindBy) {
        final EnumOptionData optionData = new EnumOptionData(remindBy.getValue().longValue(), remindBy.getCode(), remindBy.toString());
        return optionData;
    }

    public static List<EnumOptionData> calendarRemindBy(final CalendarRemindBy[] remindBys) {
        final List<EnumOptionData> optionDatas = new ArrayList<EnumOptionData>();
        for (final CalendarRemindBy remindBy : remindBys) {
            optionDatas.add(calendarRemindBy(remindBy));
        }
        return optionDatas;
    }
}
