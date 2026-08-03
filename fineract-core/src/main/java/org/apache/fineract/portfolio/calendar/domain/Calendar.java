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
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.calendar.CalendarConstants.CalendarSupportedParameters;
import org.apache.fineract.portfolio.calendar.data.CalendarUpdateRequest;
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

    /**
     * Applies a partial update. A {@code null} field in the request leaves the stored value unchanged; an empty
     * {@code description} or {@code location} clears it. The dates and the meeting time are parsed by the caller, which
     * knows the request's {@code dateFormat}, {@code locale} and {@code timeFormat}.
     */
    public Map<String, Object> update(final CalendarUpdateRequest request, final LocalDate newStartDate, final LocalDate newEndDate,
            final LocalTime newMeetingTime, final boolean areActiveEntitiesSynced) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (request.getTitle() != null && !request.getTitle().equals(this.title)) {
            actualChanges.put(CalendarSupportedParameters.TITLE.getValue(), request.getTitle());
            this.title = StringUtils.defaultIfEmpty(request.getTitle(), null);
        }
        if (request.getDescription() != null && !request.getDescription().equals(StringUtils.defaultString(this.description))) {
            actualChanges.put(CalendarSupportedParameters.DESCRIPTION.getValue(), request.getDescription());
            this.description = StringUtils.defaultIfEmpty(request.getDescription(), null);
        }
        if (request.getLocation() != null && !request.getLocation().equals(StringUtils.defaultString(this.location))) {
            actualChanges.put(CalendarSupportedParameters.LOCATION.getValue(), request.getLocation());
            this.location = StringUtils.defaultIfEmpty(request.getLocation(), null);
        }

        final String startDateParamName = CalendarSupportedParameters.START_DATE.getValue();
        if (newStartDate != null && !newStartDate.equals(getStartDateLocalDate())) {
            final LocalDate currentDate = DateUtils.getLocalDateOfTenant();

            if (DateUtils.isBefore(newStartDate, currentDate)) {
                final String defaultUserMessage = "New meeting effective from date cannot be in past";
                throw new CalendarDateException("new.start.date.cannot.be.in.past", defaultUserMessage, newStartDate,
                        getStartDateLocalDate());
            } else if (isStartDateAfter(newStartDate) && isStartDateBeforeOrEqual(currentDate)) {
                // new meeting date should be on or after start date or current
                // date
                final String defaultUserMessage = "New meeting effective from date cannot be a date before existing meeting start date";
                throw new CalendarDateException("new.start.date.before.existing.date", defaultUserMessage, newStartDate,
                        getStartDateLocalDate());
            } else {
                actualChanges.put(startDateParamName, request.getStartDate());
                actualChanges.put(CalendarSupportedParameters.DATE_FORMAT.getValue(), request.getDateFormat());
                actualChanges.put(CalendarSupportedParameters.LOCALE.getValue(), request.getLocale());
                this.startDate = newStartDate;
            }
        }

        final String endDateParamName = CalendarSupportedParameters.END_DATE.getValue();
        if (newEndDate != null && !newEndDate.equals(getEndDateLocalDate())) {
            actualChanges.put(endDateParamName, request.getEndDate());
            actualChanges.put(CalendarSupportedParameters.DATE_FORMAT.getValue(), request.getDateFormat());
            actualChanges.put(CalendarSupportedParameters.LOCALE.getValue(), request.getLocale());
            this.endDate = newEndDate;
        }

        final String durationParamName = CalendarSupportedParameters.DURATION.getValue();
        if (request.getDuration() != null && !request.getDuration().equals(this.duration)) {
            actualChanges.put(durationParamName, request.getDuration());
            this.duration = request.getDuration();
        }

        // Do not allow to change calendar type
        // TODO: AA Instead of throwing an exception, do not allow meeting
        // calendar type to update.
        if (request.getTypeId() != null && !request.getTypeId().equals(this.typeId)) {
            final String defaultUserMessage = "Meeting calendar type update is not supported";
            final String oldMeeingType = CalendarType.fromInt(this.typeId).name();
            final String newMeetingType = CalendarType.fromInt(request.getTypeId()).name();

            throw new CalendarParameterUpdateNotSupportedException("meeting.type", defaultUserMessage, newMeetingType, oldMeeingType);
        }

        final String repeatingParamName = CalendarSupportedParameters.REPEATING.getValue();
        if (request.getRepeating() != null && request.getRepeating() != this.repeating) {
            actualChanges.put(repeatingParamName, request.getRepeating());
            this.repeating = request.getRepeating();
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

        final String newRecurrence = this.repeating ? mergeRecurrence(request) : null;
        if (newRecurrence != null && !newRecurrence.equalsIgnoreCase(this.recurrence)) {
            if (!StringUtils.isBlank(this.recurrence)) {
                /*
                 * If active entities like JLG loan or RD accounts are synced to the calendar then do not allow to
                 * change meeting frequency
                 */

                if (areActiveEntitiesSynced && !CalendarUtils.isFrequencySame(this.recurrence, newRecurrence)) {
                    final String defaultUserMessage = "Update of meeting frequency is not supported";
                    throw new CalendarParameterUpdateNotSupportedException("meeting.frequency", defaultUserMessage);
                }

                /*
                 * If active entities like JLG loan or RD accounts are synced to the calendar then do not allow to
                 * change meeting interval
                 */

                if (areActiveEntitiesSynced && !CalendarUtils.isIntervalSame(this.recurrence, newRecurrence)) {
                    final String defaultUserMessage = "Update of meeting interval is not supported";
                    throw new CalendarParameterUpdateNotSupportedException("meeting.interval", defaultUserMessage);
                }
            }

            actualChanges.put("recurrence", newRecurrence);
            this.recurrence = newRecurrence;
        }

        final String remindByParamName = CalendarSupportedParameters.REMIND_BY_ID.getValue();
        if (request.getRemindById() != null && !request.getRemindById().equals(this.remindById)) {
            actualChanges.put(remindByParamName, request.getRemindById());
            this.remindById = request.getRemindById();
        }

        final String firstRemindarParamName = CalendarSupportedParameters.FIRST_REMINDER.getValue();
        if (request.getFirstReminder() != null && !request.getFirstReminder().equals(this.firstReminder)) {
            actualChanges.put(firstRemindarParamName, request.getFirstReminder());
            this.firstReminder = request.getFirstReminder();
        }

        final String secondRemindarParamName = CalendarSupportedParameters.SECOND_REMINDER.getValue();
        if (request.getSecondReminder() != null && !request.getSecondReminder().equals(this.secondReminder)) {
            actualChanges.put(secondRemindarParamName, request.getSecondReminder());
            this.secondReminder = request.getSecondReminder();
        }

        if (newMeetingTime != null && !newMeetingTime.equals(this.meetingtime)) {
            actualChanges.put(CalendarSupportedParameters.MEETING_TIME.getValue(), request.getMeetingtime());
            this.meetingtime = newMeetingTime;
        }

        return actualChanges;
    }

    /**
     * Builds the recurrence of a repeating calendar from the update request. A recurrence field that is not sent keeps
     * its stored value, so a request with only {@code interval}, only {@code repeatsOnDay} or only one of the monthly
     * day fields changes just that part. Returns {@code null} when nothing about the recurrence was sent and the stored
     * one stays as it is.
     */
    private String mergeRecurrence(final CalendarUpdateRequest request) {
        final boolean recurrenceSent = request.getFrequency() != null || request.getInterval() != null || request.getRepeatsOnDay() != null
                || request.getRepeatsOnNthDayOfMonth() != null || request.getRepeatsOnLastWeekdayOfMonth() != null
                || request.getRepeatsOnDayOfMonth() != null;
        final boolean hasStoredRecurrence = StringUtils.isNotBlank(this.recurrence);
        if (!recurrenceSent && hasStoredRecurrence) {
            return null;
        }

        final CalendarFrequencyType storedFrequency = hasStoredRecurrence ? CalendarUtils.getFrequency(this.recurrence)
                : CalendarFrequencyType.INVALID;
        final CalendarFrequencyType frequencyType = request.getFrequency() != null ? CalendarFrequencyType.fromInt(request.getFrequency())
                : storedFrequency;
        if (frequencyType == CalendarFrequencyType.INVALID) {
            return null;
        }
        final Integer interval = request.getInterval() != null ? request.getInterval()
                : hasStoredRecurrence ? Integer.valueOf(CalendarUtils.getInterval(this.recurrence)) : null;
        // the stored days only carry over while the frequency stays the same
        final boolean keepStoredDays = hasStoredRecurrence && frequencyType == storedFrequency;

        if (frequencyType.isWeekly()) {
            Integer repeatsOnDay = request.getRepeatsOnDay();
            if (repeatsOnDay == null && keepStoredDays) {
                final CalendarWeekDaysType storedDay = CalendarUtils.getRepeatsOnDay(this.recurrence);
                repeatsOnDay = storedDay == CalendarWeekDaysType.INVALID ? null : storedDay.getValue();
            }
            return constructRecurrence(frequencyType, interval, repeatsOnDay, null);
        }
        if (frequencyType.isMonthly()) {
            Integer nthDay = request.getRepeatsOnNthDayOfMonth();
            Integer weekDay = request.getRepeatsOnLastWeekdayOfMonth();
            Integer dayOfMonth = request.getRepeatsOnDayOfMonth();
            if (keepStoredDays) {
                // read the stored rule back as the three request fields and fill in the ones that were not sent
                final NthDayType storedNthDay = CalendarUtils.getRepeatsOnNthDayOfMonth(this.recurrence);
                final Integer storedMonthDay = CalendarUtils.getMonthOnDay(this.recurrence);
                if (storedNthDay == NthDayType.ONDAY && NthDayType.LAST.getValue().equals(storedMonthDay)) {
                    // BYMONTHDAY=-1 is how "last day of the month" (LAST without a weekday) is stored
                    nthDay = nthDay == null ? NthDayType.LAST.getValue() : nthDay;
                } else if (storedNthDay == NthDayType.ONDAY) {
                    nthDay = nthDay == null ? NthDayType.ONDAY.getValue() : nthDay;
                    dayOfMonth = dayOfMonth == null ? storedMonthDay : dayOfMonth;
                } else if (storedNthDay != NthDayType.INVALID) {
                    // a sent LAST without a weekday means the last day of the month, so the stored weekday only carries
                    // over to a kept nth day or a sent first to fourth one
                    final boolean weekDayCarriesOver = nthDay == null
                            || (nthDay >= NthDayType.ONE.getValue() && nthDay <= NthDayType.FOUR.getValue());
                    nthDay = nthDay == null ? storedNthDay.getValue() : nthDay;
                    final CalendarWeekDaysType storedDay = CalendarUtils.getRepeatsOnDay(this.recurrence);
                    if (weekDay == null && weekDayCarriesOver && storedDay != CalendarWeekDaysType.INVALID) {
                        weekDay = storedDay.getValue();
                    }
                }
            }
            validateMonthlyDay(nthDay, weekDay, dayOfMonth);
            return constructRecurrence(frequencyType, interval, null, nthDay, weekDay, dayOfMonth);
        }
        return constructRecurrence(frequencyType, interval, null, null);
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

    /**
     * Checks the monthly day after the stored values have been merged in, so a partial update cannot leave a rule that
     * names an nth weekday without the weekday, or "on day" without the day.
     */
    private static void validateMonthlyDay(final Integer nthDay, final Integer weekDay, final Integer dayOfMonth) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CALENDAR_RESOURCE_NAME);
        CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator, CalendarSupportedParameters.REPEATS_ON_NTH_DAY_OF_MONTH.getValue(),
                CalendarSupportedParameters.REPEATS_ON_LAST_WEEKDAY_OF_MONTH.getValue(), nthDay, weekDay);
        if (nthDay != null && NthDayType.fromInt(nthDay).isOnDay()) {
            baseDataValidator.reset().parameter(CalendarSupportedParameters.REPEATS_ON_DAY_OF_MONTH.getValue()).value(dayOfMonth).notNull();
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    /**
     * Builds a recurrence rule from the request parameters of a repeating calendar. For a monthly frequency
     * {@code repeatsOnNthDayOfMonth} is the week of the month and {@code repeatsOnLastWeekdayOfMonth} the weekday, or,
     * when {@code repeatsOnNthDayOfMonth} is {@link NthDayType#ONDAY}, {@code repeatsOnDayOfMonth} is the day of the
     * month.
     */
    public static String constructRecurrence(final CalendarFrequencyType frequencyType, final Integer interval, final Integer repeatsOnDay,
            final Integer repeatsOnNthDayOfMonth, final Integer repeatsOnLastWeekdayOfMonth, final Integer repeatsOnDayOfMonth) {
        Integer weekDay = null;
        if (frequencyType.isWeekly()) {
            weekDay = repeatsOnDay;
        }
        Integer nthDayOfMonth = null;
        if (frequencyType.isMonthly()) {
            nthDayOfMonth = repeatsOnNthDayOfMonth;
            weekDay = repeatsOnLastWeekdayOfMonth;
            if (NthDayType.fromInt(repeatsOnNthDayOfMonth).isOnDay()) {
                nthDayOfMonth = repeatsOnDayOfMonth;
                weekDay = null;
            }
        }
        return constructRecurrence(frequencyType, interval, weekDay, nthDayOfMonth);
    }

    private static String constructRecurrence(final CalendarFrequencyType frequencyType, final Integer interval, final Integer repeatsOnDay,
            final Integer repeatsOnNthDayOfMonth) {
        final StringBuilder recurrenceBuilder = new StringBuilder(200);

        recurrenceBuilder.append("FREQ=");
        recurrenceBuilder.append(frequencyType.toString().toUpperCase(java.util.Locale.ROOT));
        if (interval != null && interval > 1) {
            recurrenceBuilder.append(";INTERVAL=");
            recurrenceBuilder.append(interval);
        }
        if (frequencyType.isWeekly()) {
            if (repeatsOnDay != null) {
                final CalendarWeekDaysType weekDays = CalendarWeekDaysType.fromInt(repeatsOnDay);
                if (weekDays != CalendarWeekDaysType.INVALID) {
                    recurrenceBuilder.append(";BYDAY=");
                    recurrenceBuilder.append(weekDays.toString().toUpperCase(java.util.Locale.ROOT));
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
                if (nthDay != NthDayType.INVALID) {
                    recurrenceBuilder.append(";BYSETPOS=");
                    recurrenceBuilder.append(nthDay.getValue());
                }
                final CalendarWeekDaysType weekday = CalendarWeekDaysType.fromInt(repeatsOnDay);
                if (weekday != CalendarWeekDaysType.INVALID) {
                    recurrenceBuilder.append(";BYDAY=");
                    recurrenceBuilder.append(weekday.toString().toUpperCase(java.util.Locale.ROOT));
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
