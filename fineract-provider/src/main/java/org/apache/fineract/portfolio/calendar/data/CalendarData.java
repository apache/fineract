/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.calendar.data;

import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarRemindBy;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
import org.apache.fineract.portfolio.calendar.service.CalendarEnumerations;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.common.domain.NthDayType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * Immutable data object representing a Calendar.
 */
public class CalendarData {

    private final Long id;
    private final Long calendarInstanceId;
    private final Long entityId;
    private final EnumOptionData entityType;
    private String title;
    private final String description;
    private final String location;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalTime meetingTime;
    private final Integer duration;
    private final EnumOptionData type;
    private final boolean repeating;
    private final String recurrence;
    private final EnumOptionData frequency;
    private final Integer interval;
    private final EnumOptionData repeatsOnDay;
    private final EnumOptionData repeatsOnNthDayOfMonth;
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
    private final Integer repeatsOnDayOfMonth;

    // template related
    final List<EnumOptionData> entityTypeOptions;
    final List<EnumOptionData> calendarTypeOptions;
    final List<EnumOptionData> remindByOptions;
    final List<EnumOptionData> frequencyOptions;
    final List<EnumOptionData> repeatsOnDayOptions;
    final List<EnumOptionData> frequencyNthDayTypeOptions;

    //import fields
    private transient Integer rowIndex;
    private  String dateFormat;
    private  String locale;
    private  String centerId;
    private String typeId;

    public static CalendarData importInstanceNoRepeatsOnDay(LocalDate startDate, boolean repeating,
            EnumOptionData frequency, Integer interval,Integer rowIndex,String locale,String dateFormat){
       return  new CalendarData(startDate, repeating, frequency, interval, rowIndex,locale,dateFormat);

    }
    public static CalendarData importInstanceWithRepeatsOnDay(LocalDate startDate, boolean repeating,
            EnumOptionData frequency,Integer interval,EnumOptionData repeatsOnDay,Integer rowIndex,
            String locale,String dateFormat){
        return new CalendarData(startDate, repeating, frequency, interval, repeatsOnDay, rowIndex,locale,dateFormat);
    }
    private CalendarData(LocalDate startDate, boolean repeating,EnumOptionData frequency,Integer interval,
            Integer rowIndex,String locale,String dateFormat) {
        this.startDate = startDate;
        this.repeating = repeating;
        this.frequency = frequency;
        this.interval = interval;
        this.rowIndex=rowIndex;
        this.dateFormat= dateFormat;
        this.locale= locale;
        this.description = "";
        this.typeId = "1";
        this.id = null;
        this.calendarInstanceId = null;
        this.entityId = null;
        this.entityType = null;
        this.title = null;
        this.location = null;
        this.endDate = null;
        this.meetingTime = null;
        this.type = null;
        this.recurrence = null;
        this.repeatsOnDay = null;
        this.repeatsOnNthDayOfMonth = null;
        this.remindBy = null;
        this.firstReminder = null;
        this.secondReminder = null;
        this.recurringDates = null;
        this.nextTenRecurringDates = null;
        this.humanReadable = null;
        this.recentEligibleMeetingDate =null;
        this.createdDate = null;
        this.lastUpdatedDate = null;
        this.createdByUserId = null;
        this.createdByUsername = null;
        this.lastUpdatedByUserId = null;
        this.lastUpdatedByUsername = null;
        this.repeatsOnDayOfMonth = null;
        this.entityTypeOptions = null;
        this.calendarTypeOptions = null;
        this.remindByOptions = null;
        this.frequencyOptions = null;
        this.repeatsOnDayOptions = null;
        this.frequencyNthDayTypeOptions = null;
        this.duration=null;
    }

    private CalendarData(LocalDate startDate, boolean repeating,EnumOptionData frequency,Integer interval,
            EnumOptionData repeatsOnDay,Integer rowIndex,String locale,String dateFormat) {
        this.startDate = startDate;
        this.repeating = repeating;
        this.frequency = frequency;
        this.interval = interval;
        this.repeatsOnDay = repeatsOnDay;
        this.rowIndex=rowIndex;
        this.dateFormat= dateFormat;
        this.locale= locale;
        this.description = "";
        this.typeId = "1";
        this.id = null;
        this.calendarInstanceId = null;
        this.entityId = null;
        this.entityType = null;
        this.title = null;
        this.location = null;
        this.endDate = null;
        this.meetingTime = null;
        this.type = null;
        this.recurrence = null;
        this.repeatsOnNthDayOfMonth = null;
        this.remindBy = null;
        this.firstReminder = null;
        this.secondReminder = null;
        this.recurringDates = null;
        this.nextTenRecurringDates = null;
        this.humanReadable = null;
        this.recentEligibleMeetingDate =null;
        this.createdDate = null;
        this.lastUpdatedDate = null;
        this.createdByUserId = null;
        this.createdByUsername = null;
        this.lastUpdatedByUserId = null;
        this.lastUpdatedByUsername = null;
        this.repeatsOnDayOfMonth = null;
        this.entityTypeOptions = null;
        this.calendarTypeOptions = null;
        this.remindByOptions = null;
        this.frequencyOptions = null;
        this.repeatsOnDayOptions = null;
        this.frequencyNthDayTypeOptions = null;
        this.duration=null;
    }
    public void setCenterId(String centerId) {
        this.centerId = centerId;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public static CalendarData instance(final Long id, final Long calendarInstanceId, final Long entityId, final EnumOptionData entityType,
            final String title, final String description, final String location, final LocalDate startDate, final LocalDate endDate,
            final Integer duration, final EnumOptionData type, final boolean repeating, final String recurrence,
            final EnumOptionData frequency, final Integer interval, final EnumOptionData repeatsOnDay,
            final EnumOptionData repeatsOnNthDayOfMonth, final EnumOptionData remindBy, final Integer firstReminder,
            final Integer secondReminder, final String humanReadable, final LocalDate createdDate, final LocalDate lastUpdatedDate,
            final Long createdByUserId, final String createdByUsername, final Long lastUpdatedByUserId, final String lastUpdatedByUsername,
            final LocalTime meetingTime, final Integer repeatsOnDayOfMonth) {

        final Collection<LocalDate> recurringDates = null;
        final Collection<LocalDate> nextTenRecurringDates = null;
        final LocalDate recentEligibleMeetingDate = null;

        final List<EnumOptionData> entityTypeOptions = null;
        final List<EnumOptionData> calendarTypeOptions = null;
        final List<EnumOptionData> remindByOptions = null;
        final List<EnumOptionData> frequencyOptions = null;
        final List<EnumOptionData> repeatsOnDayOptions = null;
        final List<EnumOptionData> frequencyNthDayTypeOptions = null;

        return new CalendarData(id, calendarInstanceId, entityId, entityType, title, description, location, startDate, endDate, duration,
                type, repeating, recurrence, frequency, interval, repeatsOnDay, repeatsOnNthDayOfMonth, remindBy, firstReminder,
                secondReminder, recurringDates, nextTenRecurringDates, humanReadable, recentEligibleMeetingDate, createdDate,
                lastUpdatedDate, createdByUserId, createdByUsername, lastUpdatedByUserId, lastUpdatedByUsername, repeatsOnDayOfMonth,
                entityTypeOptions, calendarTypeOptions, remindByOptions, frequencyOptions, repeatsOnDayOptions, meetingTime,
                frequencyNthDayTypeOptions);
    }

    public static CalendarData withRecurringDates(final CalendarData calendarData, final Collection<LocalDate> recurringDates,
            final Collection<LocalDate> nextTenRecurringDates, final LocalDate recentEligibleMeetingDate) {
        return new CalendarData(calendarData.id, calendarData.calendarInstanceId, calendarData.entityId, calendarData.entityType,
                calendarData.title, calendarData.description, calendarData.location, calendarData.startDate, calendarData.endDate,
                calendarData.duration, calendarData.type, calendarData.repeating, calendarData.recurrence, calendarData.frequency,
                calendarData.interval, calendarData.repeatsOnDay, calendarData.repeatsOnNthDayOfMonth, calendarData.remindBy, calendarData.firstReminder,
                calendarData.secondReminder, recurringDates, nextTenRecurringDates, calendarData.humanReadable, recentEligibleMeetingDate,
                calendarData.createdDate, calendarData.lastUpdatedDate, calendarData.createdByUserId, calendarData.createdByUsername,
                calendarData.lastUpdatedByUserId, calendarData.lastUpdatedByUsername, calendarData.repeatsOnDayOfMonth, calendarData.entityTypeOptions,
                calendarData.calendarTypeOptions, calendarData.remindByOptions, calendarData.frequencyOptions,
                calendarData.repeatsOnDayOptions, calendarData.meetingTime, calendarData.frequencyNthDayTypeOptions);
    }

    public static CalendarData withRecentEligibleMeetingDate(final CalendarData calendarData, final LocalDate recentEligibleMeetingDate) {
        return new CalendarData(calendarData.id, calendarData.calendarInstanceId, calendarData.entityId, calendarData.entityType,
                calendarData.title, calendarData.description, calendarData.location, calendarData.startDate, calendarData.endDate,
                calendarData.duration, calendarData.type, calendarData.repeating, calendarData.recurrence, calendarData.frequency,
                calendarData.interval, calendarData.repeatsOnDay, calendarData.repeatsOnNthDayOfMonth, calendarData.remindBy, calendarData.firstReminder,
                calendarData.secondReminder, calendarData.recurringDates, calendarData.nextTenRecurringDates, calendarData.humanReadable,
                recentEligibleMeetingDate, calendarData.createdDate, calendarData.lastUpdatedDate, calendarData.createdByUserId,
                calendarData.createdByUsername, calendarData.lastUpdatedByUserId, calendarData.lastUpdatedByUsername,
                calendarData.repeatsOnDayOfMonth, calendarData.entityTypeOptions, calendarData.calendarTypeOptions, calendarData.remindByOptions,
                calendarData.frequencyOptions, calendarData.repeatsOnDayOptions,calendarData.meetingTime, calendarData.frequencyNthDayTypeOptions);
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
        final EnumOptionData repeatsOnNthDayOfMonth = CalendarEnumerations.calendarFrequencyNthDayType(NthDayType.ONE);
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
        final List<EnumOptionData> frequencyNthDayTypeOptions = null;

        final LocalDate createdDate = null;
        final LocalDate lastUpdatedDate = null;
        final Long createdByUserId = null;
        final String createdByUsername = null;
        final Long lastUpdatedByUserId = null;
        final String lastUpdatedByUsername = null;
        final LocalTime meetingTime = null;
        final Integer repeatsOnDayOfMonth = null;

        return new CalendarData(id, calendarInstanceId, entityId, entityType, title, description, location, startDate, endDate, duration,
                type, repeating, recurrence, frequency, interval, repeatsOnDay, repeatsOnNthDayOfMonth, remindBy, firstReminder,
                secondReminder, recurringDates, nextTenRecurringDates, humanReadable, recentEligibleMeetingDate, createdDate,
                lastUpdatedDate, createdByUserId, createdByUsername, lastUpdatedByUserId, lastUpdatedByUsername, repeatsOnDayOfMonth,
                entityTypeOptions, calendarTypeOptions, remindByOptions, frequencyOptions, repeatsOnDayOptions, meetingTime,
                frequencyNthDayTypeOptions);
    }

    public static CalendarData withTemplateOptions(final CalendarData calendarData, final List<EnumOptionData> entityTypeOptions,
            final List<EnumOptionData> calendarTypeOptions, final List<EnumOptionData> remindByOptions,
            final List<EnumOptionData> repeatsOptions, final List<EnumOptionData> repeatsOnDayOptions,
            final List<EnumOptionData> frequencyNthDayTypeOptions) {

        return new CalendarData(calendarData.id, calendarData.calendarInstanceId, calendarData.entityId, calendarData.entityType,
                calendarData.title, calendarData.description, calendarData.location, calendarData.startDate, calendarData.endDate,
                calendarData.duration, calendarData.type, calendarData.repeating, calendarData.recurrence, calendarData.frequency,
                calendarData.interval, calendarData.repeatsOnDay, calendarData.repeatsOnNthDayOfMonth, calendarData.remindBy,
                calendarData.firstReminder, calendarData.secondReminder, calendarData.recurringDates, calendarData.nextTenRecurringDates,
                calendarData.humanReadable, calendarData.recentEligibleMeetingDate, calendarData.createdDate, calendarData.lastUpdatedDate,
                calendarData.createdByUserId, calendarData.createdByUsername, calendarData.lastUpdatedByUserId,
                calendarData.lastUpdatedByUsername, calendarData.repeatsOnDayOfMonth, entityTypeOptions, calendarTypeOptions,
                remindByOptions, repeatsOptions, repeatsOnDayOptions, calendarData.meetingTime, frequencyNthDayTypeOptions);
    }

    private CalendarData(final Long id, final Long calendarInstanceId, final Long entityId, final EnumOptionData entityType,
            final String title, final String description, final String location, final LocalDate startDate, final LocalDate endDate,
            final Integer duration, final EnumOptionData type, final boolean repeating, final String recurrence,
            final EnumOptionData frequency, final Integer interval, final EnumOptionData repeatsOnDay,
            final EnumOptionData repeatsOnNthDayOfMonth, final EnumOptionData remindBy, final Integer firstReminder,
            final Integer secondReminder, final Collection<LocalDate> recurringDates, final Collection<LocalDate> nextTenRecurringDates,
            final String humanReadable, final LocalDate recentEligibleMeetingDate, final LocalDate createdDate,
            final LocalDate lastUpdatedDate, final Long createdByUserId, final String createdByUsername, final Long lastUpdatedByUserId,
            final String lastUpdatedByUsername, final Integer repeatsOnDayOfMonth, final List<EnumOptionData> entityTypeOptions,
            final List<EnumOptionData> calendarTypeOptions, final List<EnumOptionData> remindByOptions,
            final List<EnumOptionData> repeatsOptions, final List<EnumOptionData> repeatsOnDayOptions,final LocalTime meetingTime, 
            final List<EnumOptionData> frequencyNthDayTypeOptions) {
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
        this.repeatsOnNthDayOfMonth = repeatsOnNthDayOfMonth;
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
        this.repeatsOnDayOfMonth = repeatsOnDayOfMonth;
        this.entityTypeOptions = entityTypeOptions;
        this.calendarTypeOptions = calendarTypeOptions;
        this.remindByOptions = remindByOptions;
        this.frequencyOptions = repeatsOptions;
        this.repeatsOnDayOptions = repeatsOnDayOptions;
        this.meetingTime = meetingTime;
        this.frequencyNthDayTypeOptions = frequencyNthDayTypeOptions;
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

    public boolean isValidRecurringDate(final LocalDate compareDate, final Boolean isSkipMeetingOnFirstDay, final Integer numberOfDays) {
        if (isBetweenStartAndEndDate(compareDate)) { return CalendarUtils.isValidRedurringDate(this.getRecurrence(), this.getStartDate(),
                compareDate, isSkipMeetingOnFirstDay, numberOfDays); }
        return false;
    }
    
    public Integer interval(){
        return this.interval;
    }
    
    public EnumOptionData frequencyType(){
        return this.frequency;
    }
	public EnumOptionData getRepeatsOnDay() {
		return this.repeatsOnDay;
	}
	public EnumOptionData getRepeatsOnNthDayOfMonth() {
		return this.repeatsOnNthDayOfMonth;
	}
}
