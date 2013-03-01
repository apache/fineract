/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.data;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.calendar.domain.CalendarRemindBy;
import org.mifosplatform.portfolio.calendar.domain.CalendarType;
import org.mifosplatform.portfolio.calendar.service.CalendarEnumerations;

/**
 * Immutable data object representing a Calendar.
 */
public class CalendarData {

    private final Long id;
    private final Long entityId;
    private final EnumOptionData entityType;
    private final String title;
    private final String description;
    private final String location;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDate createdDate;
    private final Integer duration;
    private final EnumOptionData type;
    private final boolean repeating;
    private final String recurrence;
    private final EnumOptionData remindBy;
    private final Integer firstReminder;
    private final Integer secondReminder;

    // template related
    final List<EnumOptionData> entityTypeOptions;
    final List<EnumOptionData> calendarTypeOptions;
    final List<EnumOptionData> remindByOptions;

    public CalendarData(final Long id, final Long entityId, final EnumOptionData entityType, final String title, final String description,
            final String location, final LocalDate startDate, final LocalDate endDate, final LocalDate createdDate, final Integer duration,
            final EnumOptionData type, final boolean repeating, final String recurrence, final EnumOptionData remindBy,
            final Integer firstReminder, final Integer secondReminder) {

        this.id = id;
        this.entityId = entityId;
        this.entityType = entityType;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdDate = createdDate;
        this.duration = duration;
        this.type = type;
        this.repeating = repeating;
        this.recurrence = recurrence;
        this.remindBy = remindBy;
        this.firstReminder = firstReminder;
        this.secondReminder = secondReminder;
        this.entityTypeOptions = null;
        this.calendarTypeOptions = null;
        this.remindByOptions = null;
    }

    public CalendarData(final CalendarData calendarData, final List<EnumOptionData> entityTypeOptions,
            final List<EnumOptionData> calendarTypeOptions, final List<EnumOptionData> remindByOptions) {
        this.id = calendarData.id;
        this.entityId = calendarData.entityId;
        this.entityType = calendarData.entityType;
        this.title = calendarData.title;
        this.description = calendarData.description;
        this.location = calendarData.location;
        this.startDate = calendarData.startDate;
        this.endDate = calendarData.endDate;
        this.createdDate = calendarData.createdDate;
        this.duration = calendarData.duration;
        this.type = calendarData.type;
        this.repeating = calendarData.repeating;
        this.recurrence = calendarData.recurrence;
        this.remindBy = calendarData.remindBy;
        this.firstReminder = calendarData.firstReminder;
        this.secondReminder = calendarData.secondReminder;
        this.entityTypeOptions = entityTypeOptions;
        this.calendarTypeOptions = calendarTypeOptions;
        this.remindByOptions = remindByOptions;
    }

    public static CalendarData sensibleDefaultsForNewCalendarCreation() {
        final Long id = null;
        final Long entityId = null;
        final EnumOptionData entityType = null;
        final String title = null;
        final String description = null;
        final String location = null;
        final LocalDate startDate = null;
        final LocalDate endDate = null;
        final LocalDate createdDate = null;
        final Integer duration = new Integer(0);
        final EnumOptionData type = CalendarEnumerations.calendarType(CalendarType.COLLECTION);
        final boolean repeating = false;
        final String recurrence = null;
        final EnumOptionData remindBy = CalendarEnumerations.calendarRemindBy(CalendarRemindBy.EMAIL);
        final Integer firstReminder = new Integer(0);
        final Integer secondReminder = new Integer(0);

        return new CalendarData(id, entityId, entityType, title, description, location, startDate, endDate, createdDate, duration, type,
                repeating, recurrence, remindBy, firstReminder, secondReminder);
    }
}
