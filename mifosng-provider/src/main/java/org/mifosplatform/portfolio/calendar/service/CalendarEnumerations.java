/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarFrequencyType;
import org.mifosplatform.portfolio.calendar.domain.CalendarRemindBy;
import org.mifosplatform.portfolio.calendar.domain.CalendarType;
import org.mifosplatform.portfolio.calendar.domain.CalendarWeekDaysType;

public class CalendarEnumerations {

    public static EnumOptionData calendarEntityType(final int id) {
        return calendarEntityType(CalendarEntityType.fromInt(id));
    }

    public static EnumOptionData calendarEntityType(final CalendarEntityType entityType) {
        final EnumOptionData optionData = new EnumOptionData(entityType.getValue().longValue(), entityType.getCode(), entityType.toString());
        return optionData;
    }

    public static List<EnumOptionData> calendarEntityType(final CalendarEntityType[] entityTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
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
        final List<EnumOptionData> optionDatas = new ArrayList<>();
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
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final CalendarRemindBy remindBy : remindBys) {
            optionDatas.add(calendarRemindBy(remindBy));
        }
        return optionDatas;
    }

    public static EnumOptionData calendarFrequencyType(final int id) {
        return calendarFrequencyType(CalendarFrequencyType.fromInt(id));
    }

    public static EnumOptionData calendarFrequencyType(final CalendarFrequencyType calendarFrequencyType) {
        final EnumOptionData optionData = new EnumOptionData(calendarFrequencyType.getValue().longValue(), calendarFrequencyType.getCode(),
                calendarFrequencyType.toString());
        return optionData;
    }

    public static List<EnumOptionData> calendarFrequencyType(final CalendarFrequencyType[] calendarFrequencyTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final CalendarFrequencyType calendarFrequencyType : calendarFrequencyTypes) {
            if (!calendarFrequencyType.isInvalid()) {
                optionDatas.add(calendarFrequencyType(calendarFrequencyType));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData calendarWeekDaysType(final int id) {
        return calendarWeekDaysType(CalendarWeekDaysType.fromInt(id));
    }

    public static EnumOptionData calendarWeekDaysType(final CalendarWeekDaysType calendarWeekDaysType) {
        final EnumOptionData optionData = new EnumOptionData(calendarWeekDaysType.getValue().longValue(), calendarWeekDaysType.getCode(),
                calendarWeekDaysType.toString());
        return optionData;
    }

    public static List<EnumOptionData> calendarWeekDaysType(final CalendarWeekDaysType[] calendarWeekDaysTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final CalendarWeekDaysType calendarWeekDaysType : calendarWeekDaysTypes) {
            if (!calendarWeekDaysType.isInvalid()) {
                optionDatas.add(calendarWeekDaysType(calendarWeekDaysType));
            }
        }
        return optionDatas;
    }
}
