/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.data;

import java.util.Collection;
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
    private final Integer duration;
    private final EnumOptionData type;
    private final boolean repeating;
    private final String recurrence;
    private final EnumOptionData remindBy;
    private final Integer firstReminder;
    private final Integer secondReminder;
    private final Collection<LocalDate> recurringDates;
    private final Collection<LocalDate> nextTenRecurringDates;
    private final String humanReadable;

    private final LocalDate createdDate;
    private final LocalDate lastUpdatedDate;
    private final Long createdByUserId;
    private final String createdByUsername;
    private final Long lastUpdatedByUserId;
    private final String lastUpdatedByUsername;

    // template related
    final List<EnumOptionData> entityTypeOptions;
    final List<EnumOptionData> calendarTypeOptions;
    final List<EnumOptionData> remindByOptions;

    public CalendarData(final Long id, final Long entityId, final EnumOptionData entityType, final String title, final String description,
            final String location, final LocalDate startDate, final LocalDate endDate, final Integer duration, final EnumOptionData type,
            final boolean repeating, final String recurrence, final EnumOptionData remindBy, final Integer firstReminder,
            final Integer secondReminder, final String humanReadable, final LocalDate createdDate, final LocalDate lastUpdatedDate,
            final Long createdByUserId, final String createdByUsername, final Long lastUpdatedByUserId, final String lastUpdatedByUsername) {

        this.id = id;
        this.entityId = entityId;
        this.entityType = entityType;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
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
        this.recurringDates = null;
        this.nextTenRecurringDates = null;
        this.humanReadable = humanReadable;

        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.lastUpdatedByUserId = lastUpdatedByUserId;
        this.lastUpdatedByUsername = lastUpdatedByUsername;
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
        this.recurringDates = calendarData.recurringDates;
        this.nextTenRecurringDates = calendarData.nextTenRecurringDates;
        this.humanReadable = calendarData.humanReadable;
        this.createdDate = calendarData.createdDate;
        this.lastUpdatedDate = calendarData.lastUpdatedDate;
        this.createdByUserId = calendarData.createdByUserId;
        this.createdByUsername = calendarData.createdByUsername;
        this.lastUpdatedByUserId = calendarData.lastUpdatedByUserId;
        this.lastUpdatedByUsername = calendarData.lastUpdatedByUsername;
    }

    public CalendarData(final CalendarData calendarData, final Collection<LocalDate> recurringDates, final Collection<LocalDate> nextTenRecurringDates) {
        this.id = calendarData.id;
        this.entityId = calendarData.entityId;
        this.entityType = calendarData.entityType;
        this.title = calendarData.title;
        this.description = calendarData.description;
        this.location = calendarData.location;
        this.startDate = calendarData.startDate;
        this.endDate = calendarData.endDate;
        this.duration = calendarData.duration;
        this.type = calendarData.type;
        this.repeating = calendarData.repeating;
        this.recurrence = calendarData.recurrence;
        this.remindBy = calendarData.remindBy;
        this.firstReminder = calendarData.firstReminder;
        this.secondReminder = calendarData.secondReminder;
        this.entityTypeOptions = null;
        this.calendarTypeOptions = null;
        this.remindByOptions = null;
        this.recurringDates = recurringDates;
        this.nextTenRecurringDates = nextTenRecurringDates;
        this.humanReadable = calendarData.humanReadable;
        this.createdDate = calendarData.createdDate;
        this.lastUpdatedDate = calendarData.lastUpdatedDate;
        this.createdByUserId = calendarData.createdByUserId;
        this.createdByUsername = calendarData.createdByUsername;
        this.lastUpdatedByUserId = calendarData.lastUpdatedByUserId;
        this.lastUpdatedByUsername = calendarData.lastUpdatedByUsername;

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
        final Integer duration = new Integer(0);
        final EnumOptionData type = CalendarEnumerations.calendarType(CalendarType.COLLECTION);
        final boolean repeating = false;
        final String recurrence = null;
        final EnumOptionData remindBy = CalendarEnumerations.calendarRemindBy(CalendarRemindBy.EMAIL);
        final Integer firstReminder = new Integer(0);
        final Integer secondReminder = new Integer(0);
        final String humanReadable = null;

        return new CalendarData(id, entityId, entityType, title, description, location, startDate, endDate, duration, type, repeating,
                recurrence, remindBy, firstReminder, secondReminder, humanReadable, null, null, null, null, null, null);
    }

    public Long getId() {
        return this.id;
    }

    public Long getEntityId() {
        return this.entityId;
    }

    public EnumOptionData getEntityType() {
        return this.entityType;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLocation() {
        return this.location;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public LocalDate getCreatedDate() {
        return this.createdDate;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public EnumOptionData getType() {
        return this.type;
    }

    public boolean isRepeating() {
        return this.repeating;
    }

    public String getRecurrence() {
        return this.recurrence;
    }

    public EnumOptionData getRemindBy() {
        return this.remindBy;
    }

    public Integer getFirstReminder() {
        return this.firstReminder;
    }

    public Integer getSecondReminder() {
        return this.secondReminder;
    }

    public List<EnumOptionData> getEntityTypeOptions() {
        return this.entityTypeOptions;
    }

    public List<EnumOptionData> getCalendarTypeOptions() {
        return this.calendarTypeOptions;
    }

    public List<EnumOptionData> getRemindByOptions() {
        return this.remindByOptions;
    }

    public String getHumanReadable() {
        return this.humanReadable;
    }

}
