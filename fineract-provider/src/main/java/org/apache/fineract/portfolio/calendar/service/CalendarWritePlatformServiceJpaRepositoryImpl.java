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
package org.apache.fineract.portfolio.calendar.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.calendar.CalendarConstants.CalendarSupportedParameters;
import org.apache.fineract.portfolio.calendar.data.CalendarCreateRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarCreateResponse;
import org.apache.fineract.portfolio.calendar.data.CalendarDeleteRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarDeleteResponse;
import org.apache.fineract.portfolio.calendar.data.CalendarUpdateRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarUpdateResponse;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarHistory;
import org.apache.fineract.portfolio.calendar.domain.CalendarHistoryRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
import org.apache.fineract.portfolio.calendar.exception.CalendarDeleteNotAllowedException;
import org.apache.fineract.portfolio.calendar.exception.CalendarEntityTypeNotSupportedException;
import org.apache.fineract.portfolio.calendar.exception.CalendarNotFoundException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.meeting.domain.MeetingRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
public class CalendarWritePlatformServiceJpaRepositoryImpl implements CalendarWritePlatformService {

    private final CalendarRepository calendarRepository;
    private final CalendarHistoryRepository calendarHistoryRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final LoanWritePlatformService loanWritePlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final GroupRepositoryWrapper groupRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepository;
    private final MeetingRepository meetingRepository;

    @Transactional
    @Override
    public CalendarCreateResponse createCalendar(final CalendarCreateRequest request) {
        CalendarEntityType entityType = resolveEntityType(request.getEntityType());

        final Locale locale = toLocale(request.getLocale());
        final LocalDate startDate = parseDate(request.getStartDate(), CalendarSupportedParameters.START_DATE, request.getDateFormat(),
                locale);
        final LocalDate endDate = parseDate(request.getEndDate(), CalendarSupportedParameters.END_DATE, request.getDateFormat(), locale);
        final LocalTime meetingTime = parseTime(request.getMeetingtime(), request.getTimeFormat());
        validateRecurrence(Boolean.TRUE.equals(request.getRepeating()), request.getFrequency(), request.getRepeatsOnDay(), true,
                request.getRepeatsOnNthDayOfMonth(), request.getRepeatsOnLastWeekdayOfMonth());

        Long clientId = null;
        Long groupId = null;
        Long loanId = null;
        LocalDate entityActivationDate = null;
        Group centerOrGroup = null;
        switch (entityType) {
            case CENTERS, GROUPS -> {
                centerOrGroup = this.groupRepository.findOneWithNotFoundDetection(request.getEntityId());
                entityActivationDate = centerOrGroup.getActivationDate();
                entityType = centerOrGroup.isCenter() ? CalendarEntityType.CENTERS : CalendarEntityType.GROUPS;
                groupId = request.getEntityId();
            }
            case LOANS -> {
                final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(request.getEntityId(), true);
                entityActivationDate = (loan.getApprovedOnDate() == null) ? loan.getSubmittedOnDate() : loan.getApprovedOnDate();
                loanId = request.getEntityId();
            }
            case CLIENTS -> {
                final Client client = this.clientRepository.findOneWithNotFoundDetection(request.getEntityId());
                entityActivationDate = client.getActivationDate();
                clientId = request.getEntityId();
            }
            default -> {
                // no activation date, so the start date check below rejects the calendar
            }
        }

        final boolean repeating = Boolean.TRUE.equals(request.getRepeating());
        final String recurrence = repeating ? Calendar.constructRecurrence(CalendarFrequencyType.fromInt(request.getFrequency()),
                request.getInterval(), request.getRepeatsOnDay(), request.getRepeatsOnNthDayOfMonth(),
                request.getRepeatsOnLastWeekdayOfMonth(), request.getRepeatsOnDayOfMonth()) : null;
        final Calendar newCalendar = new Calendar(request.getTitle(), request.getDescription(), request.getLocation(), startDate, endDate,
                request.getDuration(), request.getTypeId(), repeating, recurrence, request.getRemindById(), request.getFirstReminder(),
                request.getSecondReminder(), meetingTime);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("calendar");
        if (entityActivationDate == null || DateUtils.isBefore(newCalendar.getStartDateLocalDate(), entityActivationDate)) {
            String dateAsString = "";
            if (entityActivationDate != null) {
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(request.getDateFormat()).withLocale(locale);
                dateAsString = formatter.format(entityActivationDate);
            }

            final String errorMessage = "cannot.be.before." + entityType.name().toLowerCase(Locale.ROOT) + ".activation.date";
            baseDataValidator.reset().parameter(CalendarSupportedParameters.START_DATE.getValue()).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode(errorMessage);
        }

        if (centerOrGroup != null) {
            Long centerOrGroupId = centerOrGroup.getId();
            Integer centerOrGroupEntityTypeId = entityType.getValue();

            final Group parent = centerOrGroup.getParent();
            if (parent != null) {
                centerOrGroupId = parent.getId();
                centerOrGroupEntityTypeId = CalendarEntityType.CENTERS.getValue();
            }

            final CalendarInstance collectionCalendarInstance = this.calendarInstanceRepository
                    .findByEntityIdAndEntityTypeIdAndCalendarTypeId(centerOrGroupId, centerOrGroupEntityTypeId,
                            CalendarType.COLLECTION.getValue());
            if (collectionCalendarInstance != null) {
                final String errorMessage = "multiple.collection.calendar.not.supported";
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(errorMessage);
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        this.calendarRepository.saveAndFlush(newCalendar);

        final CalendarInstance newCalendarInstance = CalendarInstance.from(newCalendar, request.getEntityId(), entityType.getValue());
        this.calendarInstanceRepository.save(newCalendarInstance);

        return CalendarCreateResponse.builder().resourceId(newCalendar.getId()).entityType(request.getEntityType())
                .entityId(request.getEntityId()).clientId(clientId).groupId(groupId).loanId(loanId).build();
    }

    public void validateIsEditMeetingAllowed(Long groupId) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("calendar");
        Group centerOrGroup = null;

        if (groupId != null) {
            centerOrGroup = this.groupRepository.findOneWithNotFoundDetection(groupId);
            final Group parent = centerOrGroup.getParent();
            /* Check if it is a Group and belongs to a center */
            if (centerOrGroup.isGroup() && parent != null) {

                Integer centerEntityTypeId = CalendarEntityType.CENTERS.getValue();
                /* Check if calendar is created at center */
                final CalendarInstance collectionCalendarInstance = this.calendarInstanceRepository
                        .findByEntityIdAndEntityTypeIdAndCalendarTypeId(parent.getId(), centerEntityTypeId,
                                CalendarType.COLLECTION.getValue());
                /*
                 * If calendar is created by parent group, then it cannot be edited by the child group
                 */
                if (collectionCalendarInstance != null) {
                    final String errorMessage = "meeting.created.at.center.cannot.be.edited.at.group.level";
                    baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(errorMessage);
                }
            }

        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

    }

    @Transactional
    @Override
    public CalendarUpdateResponse updateCalendar(final CalendarUpdateRequest request) {

        /** Validate to check if Edit is Allowed **/
        final CalendarEntityType entityType = request.getEntityType() == null ? null
                : CalendarEntityType.getEntityType(request.getEntityType());
        if (entityType == CalendarEntityType.GROUPS || entityType == CalendarEntityType.CENTERS) {
            this.validateIsEditMeetingAllowed(request.getEntityId());
        }

        /*
         * Validate all the data for updating the calendar
         */
        final Locale locale = toLocale(request.getLocale());
        final LocalDate startDate = parseDate(request.getStartDate(), CalendarSupportedParameters.START_DATE, request.getDateFormat(),
                locale);
        final LocalDate endDate = parseDate(request.getEndDate(), CalendarSupportedParameters.END_DATE, request.getDateFormat(), locale);
        final LocalTime meetingTime = parseTime(request.getMeetingtime(), request.getTimeFormat());
        validateForUpdate(request);

        Boolean areActiveEntitiesSynced = false;
        final Long calendarId = request.getCalendarId();

        final Collection<LoanStatus> loanStatuses = new ArrayList<>(
                Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.APPROVED, LoanStatus.ACTIVE));

        final Integer numberOfActiveLoansSyncedWithThisCalendar = this.calendarInstanceRepository.countOfLoansSyncedWithCalendar(calendarId,
                loanStatuses);

        /*
         * areActiveEntitiesSynced is set to true, if there are any active loans synced to this calendar.
         */

        if (numberOfActiveLoansSyncedWithThisCalendar > 0) {
            areActiveEntitiesSynced = true;
        }

        final Calendar calendarForUpdate = this.calendarRepository.findById(calendarId)
                .orElseThrow(() -> new CalendarNotFoundException(calendarId));

        final LocalDate oldStartDate = calendarForUpdate.getStartDate();
        // create calendar history before updating calendar
        final CalendarHistory calendarHistory = new CalendarHistory(calendarForUpdate, oldStartDate);

        Map<String, Object> changes = null;

        final Boolean reschedulebasedOnMeetingDates = request.getReschedulebasedOnMeetingDates();

        /*
         * System allows to change the meeting date by two means,
         *
         * Option 1: reschedulebasedOnMeetingDates = false or reschedulebasedOnMeetingDates is not passed By directly
         * editing the recurring day with effective from date and system decides the next meeting date based on some
         * sensible logic (i.e., number of minimum days between two repayments)
         *
         *
         * Option 2: reschedulebasedOnMeetingDates = true By providing alternative meeting date for one of future
         * meeting date and derive the day of recurrence from the new meeting date. Ex: User proposes new meeting date
         * say "14/Nov/2014" for present meeting date "12/Nov/2014", based on this input other values re derived and
         * loans are rescheduled
         *
         */

        LocalDate newMeetingDate = null;
        LocalDate presentMeetingDate = null;

        if (reschedulebasedOnMeetingDates != null && reschedulebasedOnMeetingDates) {

            newMeetingDate = parseDate(request.getNewMeetingDate(), CalendarSupportedParameters.NEW_MEETING_DATE, request.getDateFormat(),
                    locale);
            presentMeetingDate = parseDate(request.getPresentMeetingDate(), CalendarSupportedParameters.PRESENT_MEETING_DATE,
                    request.getDateFormat(), locale);

            /*
             * New meeting date proposed will become the new start date for the updated calendar
             */

            changes = calendarForUpdate.updateStartDateAndDerivedFeilds(newMeetingDate);

        } else {
            changes = calendarForUpdate.update(request, startDate, endDate, meetingTime, areActiveEntitiesSynced);
        }

        if (!changes.isEmpty()) {
            // update calendar history table only if there is a change in
            // calendar start date.
            if (reschedulebasedOnMeetingDates == null) {
                presentMeetingDate = startDate;
            }
            if (null != newMeetingDate) {
                final LocalDate endDateOfHistory = presentMeetingDate.minusDays(1);
                calendarHistory.updateEndDate(endDateOfHistory);
            }
            this.calendarHistoryRepository.save(calendarHistory);
            Set<CalendarHistory> history = calendarForUpdate.getCalendarHistory();
            history.add(calendarHistory);
            calendarForUpdate.updateCalendarHistory(history);
            this.calendarRepository.saveAndFlush(calendarForUpdate);

            if (this.configurationDomainService.isRescheduleFutureRepaymentsEnabled() && calendarForUpdate.isRepeating()) {
                // fetch all loan calendar instances associated with modifying
                // calendar.
                final Collection<CalendarInstance> loanCalendarInstances = this.calendarInstanceRepository
                        .findByCalendarIdAndEntityTypeId(calendarId, CalendarEntityType.LOANS.getValue());

                if (!CollectionUtils.isEmpty(loanCalendarInstances)) {
                    // update all loans associated with modifying calendar
                    this.loanWritePlatformService.applyMeetingDateChanges(calendarForUpdate, loanCalendarInstances,
                            reschedulebasedOnMeetingDates, presentMeetingDate, newMeetingDate);

                }
            }
        }

        return CalendarUpdateResponse.builder().resourceId(calendarForUpdate.getId()).changes(changes).build();
    }

    private static CalendarEntityType resolveEntityType(final String entityType) {
        final CalendarEntityType calendarEntityType = entityType == null ? null : CalendarEntityType.getEntityType(entityType);
        if (calendarEntityType == null || calendarEntityType == CalendarEntityType.INVALID) {
            throw new CalendarEntityTypeNotSupportedException(entityType);
        }
        return calendarEntityType;
    }

    private static void validateForUpdate(final CalendarUpdateRequest request) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("calendar");

        if (request.getTitle() != null) {
            baseDataValidator.reset().parameter(CalendarSupportedParameters.TITLE.getValue()).value(request.getTitle()).notBlank();
        }
        if (Boolean.TRUE.equals(request.getReschedulebasedOnMeetingDates())) {
            baseDataValidator.reset().parameter(CalendarSupportedParameters.PRESENT_MEETING_DATE.getValue())
                    .value(request.getPresentMeetingDate()).notBlank();
            baseDataValidator.reset().parameter(CalendarSupportedParameters.NEW_MEETING_DATE.getValue()).value(request.getNewMeetingDate())
                    .notBlank();
        }
        throwIfErrors(dataValidationErrors);

        validateRecurrence(Boolean.TRUE.equals(request.getRepeating()), request.getFrequency(), request.getRepeatsOnDay(), false,
                request.getRepeatsOnNthDayOfMonth(), request.getRepeatsOnLastWeekdayOfMonth());
    }

    /**
     * The rules a repeating calendar's recurrence must follow; the same checks the legacy JSON validation made.
     */
    private static void validateRecurrence(final boolean repeating, final Integer frequency, final Integer repeatsOnDay,
            final boolean repeatsOnDayRequiredForWeekly, final Integer repeatsOnNthDayOfMonth, final Integer repeatsOnLastWeekdayOfMonth) {
        if (!repeating) {
            return;
        }
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("calendar");

        baseDataValidator.reset().parameter(CalendarSupportedParameters.FREQUENCY.getValue()).value(frequency).notNull()
                .inMinMaxRange(CalendarFrequencyType.getMinValue(), CalendarFrequencyType.getMaxValue());
        if (frequency != null) {
            final CalendarFrequencyType frequencyType = CalendarFrequencyType.fromInt(frequency);
            if (frequencyType.isWeekly() && (repeatsOnDayRequiredForWeekly || repeatsOnDay != null)) {
                baseDataValidator.reset().parameter(CalendarSupportedParameters.REPEATS_ON_DAY.getValue()).value(repeatsOnDay).notNull()
                        .inMinMaxRange(CalendarWeekDaysType.getMinValue(), CalendarWeekDaysType.getMaxValue());
            } else if (frequencyType.isMonthly()) {
                CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator,
                        CalendarSupportedParameters.REPEATS_ON_NTH_DAY_OF_MONTH.getValue(),
                        CalendarSupportedParameters.REPEATS_ON_LAST_WEEKDAY_OF_MONTH.getValue(), repeatsOnNthDayOfMonth,
                        repeatsOnLastWeekdayOfMonth);
            }
        }
        throwIfErrors(dataValidationErrors);
    }

    private static void throwIfErrors(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private static Locale toLocale(final String locale) {
        return StringUtils.isBlank(locale) ? null : JsonParserHelper.localeFromString(locale);
    }

    /**
     * Parses a date the way the legacy JSON parsing did: a missing {@code dateFormat} or {@code locale}, or a value
     * that does not match them, is a validation error rather than a server error.
     */
    private static LocalDate parseDate(final String value, final CalendarSupportedParameters parameter, final String dateFormat,
            final Locale locale) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return JsonParserHelper.convertFrom(value, parameter.getValue(), dateFormat, locale);
    }

    private static LocalTime parseTime(final String value, final String timeFormat) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        final String parameterName = CalendarSupportedParameters.MEETING_TIME.getValue();
        if (StringUtils.isBlank(timeFormat)) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    List.of(ApiParameterError.parameterError("validation.msg.missing.timeFormat.parameter",
                            "The parameter `" + parameterName + "` requires a `timeFormat` parameter to be passed with it.",
                            parameterName)));
        }
        try {
            return LocalTime.parse(value, DateTimeFormatter.ofPattern(timeFormat));
        } catch (final IllegalArgumentException | DateTimeParseException e) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    List.of(ApiParameterError.parameterError("validation.msg.invalid.TimeFormat",
                            "The parameter `" + value + "` is not in correct format.", parameterName)),
                    e);
        }
    }

    @Transactional
    @Override
    public CalendarDeleteResponse deleteCalendar(final CalendarDeleteRequest request) {
        final Long calendarId = request.getCalendarId();
        final Calendar calendarForDelete = this.calendarRepository.findById(calendarId)
                .orElseThrow(() -> new CalendarNotFoundException(calendarId));

        final List<CalendarInstance> calendarInstances = this.calendarInstanceRepository.findByCalendarId(calendarId);
        final boolean linkedToAccount = calendarInstances.stream().map(CalendarInstance::getEntityTypeId)
                .anyMatch(entityTypeId -> !isCalendarOwnerEntityType(entityTypeId));
        if (linkedToAccount) {
            throw new CalendarDeleteNotAllowedException("linked.to.account",
                    "Calendar with identifier " + calendarId + " is linked to a loan or savings account and cannot be deleted", calendarId);
        }
        if (!calendarInstances.isEmpty()
                && this.meetingRepository.existsByCalendarInstanceIdIn(calendarInstances.stream().map(CalendarInstance::getId).toList())) {
            throw new CalendarDeleteNotAllowedException("has.meetings",
                    "Calendar with identifier " + calendarId + " has meetings recorded and cannot be deleted", calendarId);
        }

        this.calendarInstanceRepository.deleteAll(calendarInstances);
        this.calendarRepository.delete(calendarForDelete);
        return CalendarDeleteResponse.builder().resourceId(calendarId).build();
    }

    private static boolean isCalendarOwnerEntityType(final Integer entityTypeId) {
        return CalendarEntityType.isClient(entityTypeId) || CalendarEntityType.isGroup(entityTypeId)
                || CalendarEntityType.isCenter(entityTypeId);
    }

    @Override
    public CommandProcessingResult createCalendarInstance(final Long calendarId, final Long entityId, final Integer entityTypeId) {
        final Calendar calendarForUpdate = this.calendarRepository.findById(calendarId)
                .orElseThrow(() -> new CalendarNotFoundException(calendarId));

        final CalendarInstance newCalendarInstance = new CalendarInstance(calendarForUpdate, entityId, entityTypeId);
        this.calendarInstanceRepository.save(newCalendarInstance);

        return new CommandProcessingResultBuilder() //
                .withCommandId(null) //
                .withEntityId(calendarForUpdate.getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult updateCalendarInstance(final Long calendarId, final Long entityId, final Integer entityTypeId) {
        final Calendar calendarForUpdate = this.calendarRepository.findById(calendarId)
                .orElseThrow(() -> new CalendarNotFoundException(calendarId));

        final CalendarInstance calendarInstanceForUpdate = this.calendarInstanceRepository
                .findByCalendarIdAndEntityIdAndEntityTypeId(calendarId, entityId, entityTypeId);
        this.calendarInstanceRepository.saveAndFlush(calendarInstanceForUpdate);
        return new CommandProcessingResultBuilder() //
                .withCommandId(null) //
                .withEntityId(calendarForUpdate.getId()) //
                .build();
    }
}
