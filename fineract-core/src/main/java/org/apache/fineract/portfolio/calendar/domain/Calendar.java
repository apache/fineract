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
package org.apache.fineract.portfolio.calendar.domain;

import static org.apache.fineract.portfolio.calendar.CalendarConstants.CALENDAR_RESOURCE_NAME;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.calendar.CalendarConstants.CalendarSupportedParameters;
import org.apache.fineract.portfolio.calendar.exception.CalendarDateException;
import org.apache.fineract.portfolio.calendar.exception.CalendarParameterUpdateNotSupportedException;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.common.domain.NthDayType;

@Entity
@Table(name = "m_calendar")
public class Calendar extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Column(name = "description", length = 100, nullable = true)
    private String description;

    @Column(name = "location", length = 100, nullable = true)
    private String location;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = true)
    private LocalDate endDate;

    @Column(name = "duration", nullable = true)
    private Integer duration;

    @Column(name = "calendar_type_enum", nullable = false)
    private Integer typeId;

    @Column(name = "repeating", nullable = false)
    private boolean repeating = false;

    @Column(name = "recurrence", length = 100, nullable = true)
    private String recurrence;

    @Column(name = "remind_by_enum", nullable = true)
    private Integer remindById;

    @Column(name = "first_reminder", nullable = true)
    private Integer firstReminder;

    @Column(name = "second_reminder", nullable = true)
    private Integer secondReminder;

    @Column(name = "meeting_time", nullable = true)
    private LocalTime meetingtime;

    @OneToMany(mappedBy = "calendar", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CalendarHistory> calendarHistory = new HashSet<>();

    protected Calendar() {

    }

    public Calendar(final String title, final String description, final String location, final LocalDate startDate, final LocalDate endDate,
            final Integer duration, final Integer typeId, final boolean repeating, final String recurrence, final Integer remindById,
            final Integer firstReminder, final Integer secondReminder, final LocalTime meetingtime) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CALENDAR_RESOURCE_NAME);

        final CalendarType calendarType = CalendarType.fromInt(typeId);
        if (calendarType.isCollection() && !repeating) {
            baseDataValidator.reset().parameter(CalendarSupportedParameters.REPEATING.getValue())
                    .failWithCodeNoParameterAddedToErrorCode("must.repeat.for.collection.calendar");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        this.title = StringUtils.defaultIfEmpty(title, null);
        this.description = StringUtils.defaultIfEmpty(description, null);
        this.location = StringUtils.defaultIfEmpty(location, null);
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.typeId = typeId;
        this.repeating = repeating;
        this.recurrence = StringUtils.defaultIfEmpty(recurrence, null);
        this.remindById = remindById;
        this.firstReminder = firstReminder;
        this.secondReminder = secondReminder;
        this.meetingtime = meetingtime;
    }

    public static Calendar createRepeatingCalendar(final String title, final LocalDate startDate, final Integer typeId,
            final CalendarFrequencyType frequencyType, final Integer interval, final Integer repeatsOnDay,
            final Integer repeatsOnNthDayOfMonth) {
        final String recurrence = constructRecurrence(frequencyType, interval, repeatsOnDay, repeatsOnNthDayOfMonth);
        return createRepeatingCalendar(title, startDate, typeId, recurrence);
    }

    public static Calendar createRepeatingCalendar(final String title, final LocalDate startDate, final Integer typeId,
            final String recurrence) {
        final String description = null;
        final String location = null;
        final LocalDate endDate = null;
        final Integer duration = null;
        final boolean repeating = true;
        final Integer remindById = null;
        final Integer firstReminder = null;
        final Integer secondReminder = null;
        final LocalTime meetingtime = null;
        return new Calendar(title, description, location, startDate, endDate, duration, typeId, repeating, recurrence, remindById,
                firstReminder, secondReminder, meetingtime);
    }

    public static Calendar fromJson(final JsonCommand command) {

        LocalTime meetingtime;
        final String title = command.stringValueOfParameterNamed(CalendarSupportedParameters.TITLE.getValue());
        final String description = command.stringValueOfParameterNamed(CalendarSupportedParameters.DESCRIPTION.getValue());
        final String location = command.stringValueOfParameterNamed(CalendarSupportedParameters.LOCATION.getValue());
        final LocalDate startDate = command.localDateValueOfParameterNamed(CalendarSupportedParameters.START_DATE.getValue());
        final LocalDate endDate = command.localDateValueOfParameterNamed(CalendarSupportedParameters.END_DATE.getValue());
        final Integer duration = command.integerValueSansLocaleOfParameterNamed(CalendarSupportedParameters.DURATION.getValue());
        final Integer typeId = command.integerValueSansLocaleOfParameterNamed(CalendarSupportedParameters.TYPE_ID.getValue());
        final boolean repeating = command.booleanPrimitiveValueOfParameterNamed(CalendarSupportedParameters.REPEATING.getValue());
        final Integer remindById = command.integerValueSansLocaleOfParameterNamed(CalendarSupportedParameters.REMIND_BY_ID.getValue());
        final Integer firstReminder = command.integerValueSansLocaleOfParameterNamed(CalendarSupportedParameters.FIRST_REMINDER.getValue());
        final Integer secondReminder = command
                .integerValueSansLocaleOfParameterNamed(CalendarSupportedParameters.SECOND_REMINDER.getValue());
        meetingtime = command.localTimeValueOfParameterNamed(CalendarSupportedParameters.MEETING_TIME.getValue());

        final String recurrence = Calendar.constructRecurrence(command, null);

        return new Calendar(title, description, location, startDate, endDate, duration, typeId, repeating, recurrence, remindById,
                firstReminder, secondReminder, meetingtime);
    }

    public Map<String, Object> updateStartDateAndDerivedFeilds(final LocalDate newMeetingStartDate) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        final LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        if (DateUtils.isBefore(newMeetingStartDate, currentDate)) {
            final String defaultUserMessage = "New meeting effective from date cannot be in past";
            throw new CalendarDateException("new.start.date.cannot.be.in.past", defaultUserMessage, newMeetingStartDate,
                    getStartDateLocalDate());
        } else if (isStartDateAfter(newMeetingStartDate) && isStartDateBeforeOrEqual(currentDate)) {
            // new meeting date should be on or after start date or current date
            final String defaultUserMessage = "New meeting effective from date cannot be a date before existing meeting start date";
            throw new CalendarDateException("new.start.date.before.existing.date", defaultUserMessage, newMeetingStartDate,
                    getStartDateLocalDate());
        } else {
            actualChanges.put(CalendarSupportedParameters.START_DATE.getValue(), newMeetingStartDate.toString());
            this.startDate = newMeetingStartDate;

            /*
             * If meeting start date is changed then there is possibilities of recurring day may change, so derive the
             * recurring day and update it if it is changed. For weekly type is weekday and for monthly type it is day
             * of the month
             */
            CalendarFrequencyType calendarFrequencyType = CalendarUtils.getFrequency(this.recurrence);
            Integer interval = CalendarUtils.getInterval(this.recurrence);
            Integer repeatsOnDay = null;

            // Repeats on day, need to derive based on the start date
            if (calendarFrequencyType.isWeekly()) {
                repeatsOnDay = newMeetingStartDate.get(ChronoField.DAY_OF_WEEK);
            } else if (calendarFrequencyType.isMonthly()) {
                repeatsOnDay = newMeetingStartDate.getDayOfMonth();
            }
            // TODO cover other recurrence also

            this.recurrence = constructRecurrence(calendarFrequencyType, interval, repeatsOnDay, null);
        }

        return actualChanges;
    }

    public Map<String, Object> update(final JsonCommand command, final Boolean areActiveEntitiesSynced) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (command.isChangeInStringParameterNamed(CalendarSupportedParameters.TITLE.getValue(), this.title)) {
            final String newValue = command.stringValueOfParameterNamed(CalendarSupportedParameters.TITLE.getValue());
            actualChanges.put(CalendarSupportedParameters.TITLE.getValue(), newValue);
            this.title = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(CalendarSupportedParameters.DESCRIPTION.getValue(), this.description)) {
            final String newValue = command.stringValueOfParameterNamed(CalendarSupportedParameters.DESCRIPTION.getValue());
            actualChanges.put(CalendarSupportedParameters.DESCRIPTION.getValue(), newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(CalendarSupportedParameters.LOCATION.getValue(), this.location)) {
            final String newValue = command.stringValueOfParameterNamed(CalendarSupportedParameters.LOCATION.getValue());
            actualChanges.put(CalendarSupportedParameters.LOCATION.getValue(), newValue);
            this.location = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();
        final String startDateParamName = CalendarSupportedParameters.START_DATE.getValue();
        if (command.isChangeInLocalDateParameterNamed(startDateParamName, getStartDateLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(startDateParamName);
            final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
            final LocalDate currentDate = DateUtils.getLocalDateOfTenant();

            if (DateUtils.isBefore(newValue, currentDate)) {
                final String defaultUserMessage = "New meeting effective from date cannot be in past";
                throw new CalendarDateException("new.start.date.cannot.be.in.past", defaultUserMessage, newValue, getStartDateLocalDate());
            } else if (isStartDateAfter(newValue) && isStartDateBeforeOrEqual(currentDate)) {
                // new meeting date should be on or after start date or current
                // date
                final String defaultUserMessage = "New meeting effective from date cannot be a date before existing meeting start date";
                throw new CalendarDateException("new.start.date.before.existing.date", defaultUserMessage, newValue,
                        getStartDateLocalDate());
            } else {
                actualChanges.put(startDateParamName, valueAsInput);
                actualChanges.put("dateFormat", dateFormatAsInput);
                actualChanges.put("locale", localeAsInput);
                this.startDate = newValue;
            }
        }

        final String endDateParamName = CalendarSupportedParameters.END_DATE.getValue();
        if (command.isChangeInLocalDateParameterNamed(endDateParamName, getEndDateLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(endDateParamName);
            actualChanges.put(endDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            this.endDate = command.localDateValueOfParameterNamed(endDateParamName);
        }

        final String durationParamName = CalendarSupportedParameters.DURATION.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(durationParamName, this.duration)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(durationParamName);
            actualChanges.put(durationParamName, newValue);
            this.duration = newValue;
        }

        // Do not allow to change calendar type
        // TODO: AA Instead of throwing an exception, do not allow meeting
        // calendar type to update.
        final String typeParamName = CalendarSupportedParameters.TYPE_ID.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(typeParamName, this.typeId)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(typeParamName);
            final String defaultUserMessage = "Meeting calendar type update is not supported";
            final String oldMeeingType = CalendarType.fromInt(this.typeId).name();
            final String newMeetingType = CalendarType.fromInt(newValue).name();

            throw new CalendarParameterUpdateNotSupportedException("meeting.type", defaultUserMessage, newMeetingType, oldMeeingType);
        }

        final String repeatingParamName = CalendarSupportedParameters.REPEATING.getValue();
        if (command.isChangeInBooleanParameterNamed(repeatingParamName, this.repeating)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(repeatingParamName);
            actualChanges.put(repeatingParamName, newValue);
            this.repeating = newValue;
        }

        // if repeating is false then update recurrence to NULL
        if (!this.repeating) {
            this.recurrence = null;
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CALENDAR_RESOURCE_NAME);

        final CalendarType calendarType = CalendarType.fromInt(this.typeId);
        if (calendarType.isCollection() && !this.repeating) {
            baseDataValidator.reset().parameter(CalendarSupportedParameters.REPEATING.getValue())
                    .failWithCodeNoParameterAddedToErrorCode("must.repeat.for.collection.calendar");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final String newRecurrence = Calendar.constructRecurrence(command, this);
        if (!StringUtils.isBlank(this.recurrence) && !newRecurrence.equalsIgnoreCase(this.recurrence)) {
            /*
             * If active entities like JLG loan or RD accounts are synced to the calendar then do not allow to change
             * meeting frequency
             */

            if (areActiveEntitiesSynced && !CalendarUtils.isFrequencySame(this.recurrence, newRecurrence)) {
                final String defaultUserMessage = "Update of meeting frequency is not supported";
                throw new CalendarParameterUpdateNotSupportedException("meeting.frequency", defaultUserMessage);
            }

            /*
             * If active entities like JLG loan or RD accounts are synced to the calendar then do not allow to change
             * meeting interval
             */

            if (areActiveEntitiesSynced && !CalendarUtils.isIntervalSame(this.recurrence, newRecurrence)) {
                final String defaultUserMessage = "Update of meeting interval is not supported";
                throw new CalendarParameterUpdateNotSupportedException("meeting.interval", defaultUserMessage);
            }

            actualChanges.put("recurrence", newRecurrence);
            this.recurrence = StringUtils.defaultIfEmpty(newRecurrence, null);
        }

        final String remindByParamName = CalendarSupportedParameters.REMIND_BY_ID.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(remindByParamName, this.remindById)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(remindByParamName);
            actualChanges.put(remindByParamName, newValue);
            this.remindById = newValue;
        }

        final String firstRemindarParamName = CalendarSupportedParameters.FIRST_REMINDER.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(firstRemindarParamName, this.firstReminder)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(firstRemindarParamName);
            actualChanges.put(firstRemindarParamName, newValue);
            this.firstReminder = newValue;
        }

        final String secondRemindarParamName = CalendarSupportedParameters.SECOND_REMINDER.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(secondRemindarParamName, this.secondReminder)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(secondRemindarParamName);
            actualChanges.put(secondRemindarParamName, newValue);
            this.secondReminder = newValue;
        }

        final String timeFormat = command.stringValueOfParameterNamed(CalendarSupportedParameters.Time_Format.getValue());
        final String time = CalendarSupportedParameters.MEETING_TIME.getValue();
        if (command.isChangeInTimeParameterNamed(CalendarSupportedParameters.MEETING_TIME.getValue(), this.meetingtime, timeFormat)) {
            final String newValue = command.stringValueOfParameterNamed(CalendarSupportedParameters.MEETING_TIME.getValue());
            actualChanges.put(CalendarSupportedParameters.MEETING_TIME.getValue(), newValue);
            LocalTime timeInLocalDateTimeFormat = command.localTimeValueOfParameterNamed(time);
            if (timeInLocalDateTimeFormat != null) {
                this.meetingtime = timeInLocalDateTimeFormat;
            }

        }

        return actualChanges;
    }

    @SuppressWarnings("null")
    public Map<String, Object> updateRepeatingCalendar(final LocalDate calendarStartDate, final CalendarFrequencyType frequencyType,
            final Integer interval, final Integer repeatsOnDay, final Integer repeatsOnNthDay) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (calendarStartDate != null && this.startDate != null && !calendarStartDate.equals(this.getStartDateLocalDate())) {
            actualChanges.put("startDate", calendarStartDate);
            this.startDate = calendarStartDate;
        }

        final String newRecurrence = Calendar.constructRecurrence(frequencyType, interval, repeatsOnDay, repeatsOnNthDay);
        if (!StringUtils.isBlank(this.recurrence) && !newRecurrence.equalsIgnoreCase(this.recurrence)) {
            actualChanges.put("recurrence", newRecurrence);
            this.recurrence = newRecurrence;
        }
        return actualChanges;
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

    public Integer getDuration() {
        return this.duration;
    }

    public Integer getTypeId() {
        return this.typeId;
    }

    public boolean isRepeating() {
        return this.repeating;
    }

    public String getRecurrence() {
        return this.recurrence;
    }

    public Integer getRemindById() {
        return this.remindById;
    }

    public Integer getFirstReminder() {
        return this.firstReminder;
    }

    public Integer getSecondReminder() {
        return this.secondReminder;
    }

    public LocalTime getMeetingTime() {
        return this.meetingtime;
    }

    public LocalDate getStartDateLocalDate() {
        return this.startDate;
    }

    public LocalDate getEndDateLocalDate() {
        return this.endDate;
    }

    public Set<CalendarHistory> history() {
        return this.calendarHistory;
    }

    public boolean isStartDateBefore(final LocalDate compareDate) {
        return startDate != null && DateUtils.isBefore(startDate, compareDate);
    }

    public boolean isStartDateBeforeOrEqual(final LocalDate compareDate) {
        return startDate != null && !DateUtils.isAfter(startDate, compareDate);
    }

    public boolean isStartDateAfter(final LocalDate compareDate) {
        return compareDate != null && DateUtils.isAfter(startDate, compareDate);
    }

    public boolean isEndDateAfterOrEqual(final LocalDate compareDate) {
        return compareDate != null && !DateUtils.isBefore(endDate, compareDate);
    }

    public boolean isBetweenStartAndEndDate(final LocalDate compareDate) {
        return isStartDateBeforeOrEqual(compareDate) && (getEndDateLocalDate() == null || isEndDateAfterOrEqual(compareDate));
    }

    private static String constructRecurrence(final JsonCommand command, final Calendar calendar) {
        final boolean repeating;
        if (command.parameterExists(CalendarSupportedParameters.REPEATING.getValue())) {
            repeating = command.booleanPrimitiveValueOfParameterNamed(CalendarSupportedParameters.REPEATING.getValue());
        } else if (calendar != null) {
            repeating = calendar.isRepeating();
        } else {
            repeating = false;
        }

        if (repeating) {
            final Integer frequency = command.integerValueOfParameterNamed(CalendarSupportedParameters.FREQUENCY.getValue());
            final CalendarFrequencyType frequencyType = CalendarFrequencyType.fromInt(frequency);
            final Integer interval = command.integerValueOfParameterNamed(CalendarSupportedParameters.INTERVAL.getValue());
            Integer repeatsOnDay = null;
            if (frequencyType.isWeekly()) {
                repeatsOnDay = command.integerValueOfParameterNamed(CalendarSupportedParameters.REPEATS_ON_DAY.getValue());
            }
            Integer repeatsOnNthDayOfMonth = null;
            if (frequencyType.isMonthly()) {
                repeatsOnNthDayOfMonth = command
                        .integerValueOfParameterNamed(CalendarSupportedParameters.REPEATS_ON_NTH_DAY_OF_MONTH.getValue());
                final NthDayType nthDay = NthDayType.fromInt(repeatsOnNthDayOfMonth);
                repeatsOnDay = command
                        .integerValueOfParameterNamed(CalendarSupportedParameters.REPEATS_ON_LAST_WEEKDAY_OF_MONTH.getValue());
                if (nthDay.isOnDay()) {
                    repeatsOnNthDayOfMonth = command
                            .integerValueOfParameterNamed(CalendarSupportedParameters.REPEATS_ON_DAY_OF_MONTH.getValue());
                    repeatsOnDay = null;
                }
            }

            return constructRecurrence(frequencyType, interval, repeatsOnDay, repeatsOnNthDayOfMonth);
        }
        return "";
    }

    private static String constructRecurrence(final CalendarFrequencyType frequencyType, final Integer interval, final Integer repeatsOnDay,
            final Integer repeatsOnNthDayOfMonth) {
        final StringBuilder recurrenceBuilder = new StringBuilder(200);

        recurrenceBuilder.append("FREQ=");
        recurrenceBuilder.append(frequencyType.toString().toUpperCase());
        if (interval > 1) {
            recurrenceBuilder.append(";INTERVAL=");
            recurrenceBuilder.append(interval);
        }
        if (frequencyType.isWeekly()) {
            if (repeatsOnDay != null) {
                final CalendarWeekDaysType weekDays = CalendarWeekDaysType.fromInt(repeatsOnDay);
                if (!weekDays.isInvalid()) {
                    recurrenceBuilder.append(";BYDAY=");
                    recurrenceBuilder.append(weekDays.toString().toUpperCase());
                }
            }
        }
        if (frequencyType.isMonthly()) {
            if (repeatsOnNthDayOfMonth != null && (repeatsOnDay == null || repeatsOnDay.equals(CalendarWeekDaysType.INVALID.getValue()))) {
                if (repeatsOnNthDayOfMonth >= -1 && repeatsOnNthDayOfMonth <= 28) {
                    recurrenceBuilder.append(";BYMONTHDAY=");
                    recurrenceBuilder.append(repeatsOnNthDayOfMonth);
                }
            } else if (repeatsOnNthDayOfMonth != null && repeatsOnDay != null
                    && !repeatsOnDay.equals(CalendarWeekDaysType.INVALID.getValue())) {
                final NthDayType nthDay = NthDayType.fromInt(repeatsOnNthDayOfMonth);
                if (!nthDay.isInvalid()) {
                    recurrenceBuilder.append(";BYSETPOS=");
                    recurrenceBuilder.append(nthDay.getValue());
                }
                final CalendarWeekDaysType weekday = CalendarWeekDaysType.fromInt(repeatsOnDay);
                if (!weekday.isInvalid()) {
                    recurrenceBuilder.append(";BYDAY=");
                    recurrenceBuilder.append(weekday.toString().toUpperCase());
                }
            }
        }
        return recurrenceBuilder.toString();
    }

    public boolean isValidRecurringDate(final LocalDate compareDate, Boolean isSkipRepaymentOnFirstMonth, Integer numberOfDays) {

        if (isBetweenStartAndEndDate(compareDate)) {
            return CalendarUtils.isValidRecurringDate(getRecurrence(), getStartDateLocalDate(), compareDate, isSkipRepaymentOnFirstMonth,
                    numberOfDays);
        }

        // validate with history details.
        for (CalendarHistory history : history()) {
            if (history.isBetweenStartAndEndDate(compareDate)) {
                return CalendarUtils.isValidRecurringDate(history.getRecurrence(), history.getStartDate(), compareDate,
                        isSkipRepaymentOnFirstMonth, numberOfDays);
            }
        }

        return false;
    }

    public void updateStartAndEndDate(final LocalDate startDate, final LocalDate endDate) {

        final CalendarFrequencyType frequencyType = CalendarUtils.getFrequency(this.recurrence);
        final Integer interval = Integer.valueOf(CalendarUtils.getInterval(this.recurrence));
        final String newRecurrence = Calendar.constructRecurrence(frequencyType, interval, startDate.get(ChronoField.DAY_OF_WEEK), null);

        this.recurrence = newRecurrence;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Set<CalendarHistory> getCalendarHistory() {
        return this.calendarHistory;
    }

    public void updateCalendarHistory(final Set<CalendarHistory> calendarHistory) {
        this.calendarHistory = calendarHistory;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }
}
