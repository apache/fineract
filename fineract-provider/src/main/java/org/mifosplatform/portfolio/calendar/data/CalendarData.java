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
import org.mifosplatform.portfolio.calendar.domain.CalendarFrequencyType;
import org.mifosplatform.portfolio.calendar.domain.CalendarRemindBy;
import org.mifosplatform.portfolio.calendar.domain.CalendarType;
import org.mifosplatform.portfolio.calendar.domain.CalendarWeekDaysType;
import org.mifosplatform.portfolio.calendar.service.CalendarEnumerations;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;

/**
 * Immutable data object representing a Calendar.
 */
public class CalendarData {

    private final Long id;
    private final Long calendarInstanceId;
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
    private final EnumOptionData frequency;
    private final Integer interval;
    private final EnumOptionData repeatsOnDay;
    private final EnumOptionData remindBy;
    private final Integer firstReminder;
    private final Integer secondReminder;
    private final Collection<LocalDate> recurringDates;
    private final Collection<LocalDate> nextTenRecurringDates;
    private final String humanReadable;
    private final LocalDate recentEligibleMeetingDate;

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
    final List<EnumOptionData> frequencyOptions;
    final List<EnumOptionData> repeatsOnDayOptions;

    public static CalendarData instance(final Long id, final Long calendarInstanceId, final Long entityId, final EnumOptionData entityType,
            final String title, final String description, final String location, final LocalDate startDate, final LocalDate endDate,
            final Integer duration, final EnumOptionData type, final boolean repeating, final String recurrence,
            final EnumOptionData frequency, final Integer interval, final EnumOptionData repeatsOnDay, final EnumOptionData remindBy,
            final Integer firstReminder, final Integer secondReminder, final String humanReadable, final LocalDate createdDate,
            final LocalDate lastUpdatedDate, final Long createdByUserId, final String createdByUsername, final Long lastUpdatedByUserId,
            final String lastUpdatedByUsername) {

        final Collection<LocalDate> recurringDates = null;
        final Collection<LocalDate> nextTenRecurringDates = null;
        final LocalDate recentEligibleMeetingDate = null;

        final List<EnumOptionData> entityTypeOptions = null;
        final List<EnumOptionData> calendarTypeOptions = null;
        final List<EnumOptionData> remindByOptions = null;
        final List<EnumOptionData> frequencyOptions = null;
        final List<EnumOptionData> repeatsOnDayOptions = null;

        return new CalendarData(id, calendarInstanceId, entityId, entityType, title, description, location, startDate, endDate, duration,
                type, repeating, recurrence, frequency, interval, repeatsOnDay, remindBy, firstReminder, secondReminder, recurringDates,
                nextTenRecurringDates, humanReadable, recentEligibleMeetingDate, createdDate, lastUpdatedDate, createdByUserId,
                createdByUsername, lastUpdatedByUserId, lastUpdatedByUsername, entityTypeOptions, calendarTypeOptions, remindByOptions,
                frequencyOptions, repeatsOnDayOptions);
    }

    public static CalendarData withRecurringDates(final CalendarData calendarData, final Collection<LocalDate> recurringDates,
            final Collection<LocalDate> nextTenRecurringDates, final LocalDate recentEligibleMeetingDate) {
        return new CalendarData(calendarData.id, calendarData.calendarInstanceId, calendarData.entityId, calendarData.entityType,
                calendarData.title, calendarData.description, calendarData.location, calendarData.startDate, calendarData.endDate,
                calendarData.duration, calendarData.type, calendarData.repeating, calendarData.recurrence, calendarData.frequency,
                calendarData.interval, calendarData.repeatsOnDay, calendarData.remindBy, calendarData.firstReminder,
                calendarData.secondReminder, recurringDates, nextTenRecurringDates, calendarData.humanReadable, recentEligibleMeetingDate,
                calendarData.createdDate, calendarData.lastUpdatedDate, calendarData.createdByUserId, calendarData.createdByUsername,
                calendarData.lastUpdatedByUserId, calendarData.lastUpdatedByUsername, calendarData.entityTypeOptions,
                calendarData.calendarTypeOptions, calendarData.remindByOptions, calendarData.frequencyOptions,
                calendarData.repeatsOnDayOptions);
    }

    public static CalendarData withRecentEligibleMeetingDate(final CalendarData calendarData, final LocalDate recentEligibleMeetingDate) {
        return new CalendarData(calendarData.id, calendarData.calendarInstanceId, calendarData.entityId, calendarData.entityType,
                calendarData.title, calendarData.description, calendarData.location, calendarData.startDate, calendarData.endDate,
                calendarData.duration, calendarData.type, calendarData.repeating, calendarData.recurrence, calendarData.frequency,
                calendarData.interval, calendarData.repeatsOnDay, calendarData.remindBy, calendarData.firstReminder,
                calendarData.secondReminder, calendarData.recurringDates, calendarData.nextTenRecurringDates, calendarData.humanReadable,
                recentEligibleMeetingDate, calendarData.createdDate, calendarData.lastUpdatedDate, calendarData.createdByUserId,
                calendarData.createdByUsername, calendarData.lastUpdatedByUserId, calendarData.lastUpdatedByUsername,
                calendarData.entityTypeOptions, calendarData.calendarTypeOptions, calendarData.remindByOptions,
                calendarData.frequencyOptions, calendarData.repeatsOnDayOptions);
    }

    public static CalendarData sensibleDefaultsForNewCalendarCreation() {
        final Long id = null;
        final Long calendarInstanceId = null;
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
        final EnumOptionData frequency = CalendarEnumerations.calendarFrequencyType(CalendarFrequencyType.DAILY);
        final Integer interval = new Integer(1);
        final EnumOptionData repeatsOnDay = CalendarEnumerations.calendarWeekDaysType(CalendarWeekDaysType.MO);
        final EnumOptionData remindBy = CalendarEnumerations.calendarRemindBy(CalendarRemindBy.EMAIL);
        final Integer firstReminder = new Integer(0);
        final Integer secondReminder = new Integer(0);
        final String humanReadable = null;
        final Collection<LocalDate> recurringDates = null;
        final Collection<LocalDate> nextTenRecurringDates = null;
        final LocalDate recentEligibleMeetingDate = null;

        final List<EnumOptionData> entityTypeOptions = null;
        final List<EnumOptionData> calendarTypeOptions = null;
        final List<EnumOptionData> remindByOptions = null;
        final List<EnumOptionData> frequencyOptions = null;
        final List<EnumOptionData> repeatsOnDayOptions = null;

        final LocalDate createdDate = null;
        final LocalDate lastUpdatedDate = null;
        final Long createdByUserId = null;
        final String createdByUsername = null;
        final Long lastUpdatedByUserId = null;
        final String lastUpdatedByUsername = null;

        return new CalendarData(id, calendarInstanceId, entityId, entityType, title, description, location, startDate, endDate, duration,
                type, repeating, recurrence, frequency, interval, repeatsOnDay, remindBy, firstReminder, secondReminder, recurringDates,
                nextTenRecurringDates, humanReadable, recentEligibleMeetingDate, createdDate, lastUpdatedDate, createdByUserId,
                createdByUsername, lastUpdatedByUserId, lastUpdatedByUsername, entityTypeOptions, calendarTypeOptions, remindByOptions,
                frequencyOptions, repeatsOnDayOptions);
    }

    public static CalendarData withTemplateOptions(final CalendarData calendarData, final List<EnumOptionData> entityTypeOptions,
            final List<EnumOptionData> calendarTypeOptions, final List<EnumOptionData> remindByOptions,
            final List<EnumOptionData> repeatsOptions, final List<EnumOptionData> repeatsOnDayOptions) {

        return new CalendarData(calendarData.id, calendarData.calendarInstanceId, calendarData.entityId, calendarData.entityType,
                calendarData.title, calendarData.description, calendarData.location, calendarData.startDate, calendarData.endDate,
                calendarData.duration, calendarData.type, calendarData.repeating, calendarData.recurrence, calendarData.frequency,
                calendarData.interval, calendarData.repeatsOnDay, calendarData.remindBy, calendarData.firstReminder,
                calendarData.secondReminder, calendarData.recurringDates, calendarData.nextTenRecurringDates, calendarData.humanReadable,
                calendarData.recentEligibleMeetingDate, calendarData.createdDate, calendarData.lastUpdatedDate,
                calendarData.createdByUserId, calendarData.createdByUsername, calendarData.lastUpdatedByUserId,
                calendarData.lastUpdatedByUsername, entityTypeOptions, calendarTypeOptions, remindByOptions, repeatsOptions,
                repeatsOnDayOptions);
    }

    private CalendarData(final Long id, final Long calendarInstanceId, final Long entityId, final EnumOptionData entityType,
            final String title, final String description, final String location, final LocalDate startDate, final LocalDate endDate,
            final Integer duration, final EnumOptionData type, final boolean repeating, final String recurrence,
            final EnumOptionData frequency, final Integer interval, final EnumOptionData repeatsOnDay, final EnumOptionData remindBy,
            final Integer firstReminder, final Integer secondReminder, final Collection<LocalDate> recurringDates,
            final Collection<LocalDate> nextTenRecurringDates, final String humanReadable, final LocalDate recentEligibleMeetingDate,
            final LocalDate createdDate, final LocalDate lastUpdatedDate, final Long createdByUserId, final String createdByUsername,
            final Long lastUpdatedByUserId, final String lastUpdatedByUsername, final List<EnumOptionData> entityTypeOptions,
            final List<EnumOptionData> calendarTypeOptions, final List<EnumOptionData> remindByOptions,
            final List<EnumOptionData> repeatsOptions, final List<EnumOptionData> repeatsOnDayOptions) {
        this.id = id;
        this.calendarInstanceId = calendarInstanceId;
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
        this.frequency = frequency;
        this.interval = interval;
        this.repeatsOnDay = repeatsOnDay;
        this.remindBy = remindBy;
        this.firstReminder = firstReminder;
        this.secondReminder = secondReminder;
        this.recurringDates = recurringDates;
        this.nextTenRecurringDates = nextTenRecurringDates;
        this.humanReadable = humanReadable;
        this.recentEligibleMeetingDate = recentEligibleMeetingDate;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.lastUpdatedByUserId = lastUpdatedByUserId;
        this.lastUpdatedByUsername = lastUpdatedByUsername;
        this.entityTypeOptions = entityTypeOptions;
        this.calendarTypeOptions = calendarTypeOptions;
        this.remindByOptions = remindByOptions;
        this.frequencyOptions = repeatsOptions;
        this.repeatsOnDayOptions = repeatsOnDayOptions;
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

    public Long getCalendarInstanceId() {
        return this.calendarInstanceId;
    }

    public boolean isStartDateBeforeOrEqual(final LocalDate compareDate) {
        if (this.startDate != null && compareDate != null) {
            if (this.startDate.isBefore(compareDate) || this.startDate.equals(compareDate)) { return true; }
        }
        return false;
    }

    public boolean isEndDateAfterOrEqual(final LocalDate compareDate) {
        if (this.endDate != null && compareDate != null) {
            if (this.endDate.isAfter(compareDate) || this.endDate.isEqual(compareDate)) { return true; }
        }
        return false;
    }

    public boolean isBetweenStartAndEndDate(final LocalDate compareDate) {
        if (isStartDateBeforeOrEqual(compareDate)) {
            if (this.endDate == null || isEndDateAfterOrEqual(compareDate)) { return true; }
        }
        return false;
    }

    public boolean isValidRecurringDate(final LocalDate compareDate) {
        if (isBetweenStartAndEndDate(compareDate)) { return CalendarUtils.isValidRedurringDate(this.getRecurrence(), this.getStartDate(),
                compareDate); }
        return false;
    }
    
    public Integer interval(){
        return this.interval;
    }
    
    public EnumOptionData frequencyType(){
        return this.frequency;
    }
}
