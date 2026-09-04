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
import java.time.format.DateTimeFormatter;
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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.calendar.CalendarConstants.CalendarSupportedParameters;
import org.apache.fineract.portfolio.calendar.data.CalendarCreateRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarCreateResponse;
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
import org.apache.fineract.portfolio.calendar.exception.CalendarEntityTypeNotSupportedException;
import org.apache.fineract.portfolio.calendar.exception.CalendarNotFoundException;
import org.apache.fineract.portfolio.calendar.serialization.CalendarCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
public class CalendarWritePlatformServiceJpaRepositoryImpl implements CalendarWritePlatformService {

    private final CalendarRepository calendarRepository;
    private final CalendarHistoryRepository calendarHistoryRepository;
    private final CalendarCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final LoanWritePlatformService loanWritePlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final GroupRepositoryWrapper groupRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepository;

    @Transactional
    @Override
    public CalendarCreateResponse createCalendar(final CalendarCreateRequest request) {
        CalendarEntityType entityType;
        try {
            entityType = CalendarEntityType.valueOf(request.getEntityType().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CalendarEntityTypeNotSupportedException(request.getEntityType(), e);
        }
        if (entityType == CalendarEntityType.INVALID) {
            throw new CalendarEntityTypeNotSupportedException(request.getEntityType());
        }

        Long clientId = null;
        Long groupId = null;
        Long loanId = null;
        LocalDate entityActivationDate = null;
        Group centerOrGroup = null;

        switch (entityType) {
            case CENTERS:
            case GROUPS:
                centerOrGroup = this.groupRepository.findOneWithNotFoundDetection(request.getEntityId());
                entityActivationDate = centerOrGroup.getActivationDate();
                entityType = centerOrGroup.isCenter() ? CalendarEntityType.CENTERS : CalendarEntityType.GROUPS;
                groupId = request.getEntityId();
            break;
            case LOANS:
                final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(request.getEntityId(), true);
                entityActivationDate = loan.getApprovedOnDate() == null ? loan.getSubmittedOnDate() : loan.getApprovedOnDate();
                loanId = request.getEntityId();
            break;
            case CLIENTS:
                final Client client = this.clientRepository.findOneWithNotFoundDetection(request.getEntityId());
                entityActivationDate = client.getActivationDate();
                clientId = request.getEntityId();
            break;
            default:
            break;
        }

        final LocalDate startDate = parseDate(request.getStartDate(), request.getDateFormat(), request.getLocale());
        final boolean repeating = Boolean.parseBoolean(StringUtils.defaultIfEmpty(request.getRepeating(), "false"));

        Calendar newCalendar;
        if (repeating && StringUtils.isNotBlank(request.getFrequency())) {
            final CalendarFrequencyType frequencyType = CalendarFrequencyType.fromInt(Integer.parseInt(request.getFrequency()));
            final Integer interval = request.getInterval();
            Integer repeatsOnDay = null;
            if (StringUtils.isNotBlank(request.getRepeatsOnDay())) {
                repeatsOnDay = Integer.parseInt(request.getRepeatsOnDay());
            }
            newCalendar = Calendar.createRepeatingCalendar(request.getTitle(), startDate, request.getTypeId(), frequencyType, interval,
                    repeatsOnDay, null);
        } else {
            newCalendar = new Calendar(request.getTitle(), request.getDescription(), null, startDate, null, null, request.getTypeId(),
                    false, null, request.getRemindBy(), null, null, null);
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("calendar");
        if (entityActivationDate == null || DateUtils.isBefore(newCalendar.getStartDateLocalDate(), entityActivationDate)) {
            String dateAsString = "";
            if (entityActivationDate != null) {
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(request.getDateFormat())
                        .withLocale(Locale.forLanguageTag(request.getLocale()));
                dateAsString = formatter.format(entityActivationDate);
            }
            final String errorMessage = "cannot.be.before." + entityType.name().toLowerCase(Locale.ROOT) + ".activation.date";
            baseDataValidator.reset().parameter(CalendarSupportedParameters.START_DATE.getValue()).value(dateAsString)
                    .failWithCodeNoParameterAddedToErrorCode(errorMessage);
        }

        if (centerOrGroup != null) {
            Long checkId = centerOrGroup.getId();
            Integer checkEntityTypeId = entityType.getValue();
            final Group parent = centerOrGroup.getParent();
            if (parent != null) {
                checkId = parent.getId();
                checkEntityTypeId = CalendarEntityType.CENTERS.getValue();
            }
            final CalendarInstance existing = this.calendarInstanceRepository.findByEntityIdAndEntityTypeIdAndCalendarTypeId(checkId,
                    checkEntityTypeId, CalendarType.COLLECTION.getValue());
            if (existing != null) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("multiple.collection.calendar.not.supported");
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

    private LocalDate parseDate(String date, String dateFormat, String locale) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat).withLocale(Locale.forLanguageTag(locale));
        return LocalDate.parse(date, formatter);
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
        this.validateIsEditMeetingAllowed(request.getEntityId() != null && request.getEntityType() != null
                && (request.getEntityType().equalsIgnoreCase("groups") || request.getEntityType().equalsIgnoreCase("centers"))
                        ? request.getEntityId()
                        : null);

        final Long calendarId = request.getCalendarId();
        Boolean areActiveEntitiesSynced = false;

        final Collection<LoanStatus> loanStatuses = new ArrayList<>(
                Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.APPROVED, LoanStatus.ACTIVE));
        final Integer numberOfActiveLoansSyncedWithThisCalendar = this.calendarInstanceRepository.countOfLoansSyncedWithCalendar(calendarId,
                loanStatuses);
        if (numberOfActiveLoansSyncedWithThisCalendar > 0) {
            areActiveEntitiesSynced = true;
        }

        final Calendar calendarForUpdate = this.calendarRepository.findById(calendarId)
                .orElseThrow(() -> new CalendarNotFoundException(calendarId));

        final LocalDate oldStartDate = calendarForUpdate.getStartDate();
        final CalendarHistory calendarHistory = new CalendarHistory(calendarForUpdate, oldStartDate);

        Map<String, Object> changes;
        LocalDate newMeetingDate = null;
        LocalDate presentMeetingDate = null;
        final Boolean reschedule = request.getRescheduleBasedOnMeetingDates();

        if (Boolean.TRUE.equals(reschedule)) {
            newMeetingDate = parseDate(request.getNewMeetingDate(), request.getDateFormat(), request.getLocale());
            presentMeetingDate = parseDate(request.getPresentMeetingDate(), request.getDateFormat(), request.getLocale());
            changes = calendarForUpdate.updateStartDateAndDerivedFeilds(newMeetingDate);
        } else {
            changes = calendarForUpdate.update(request, areActiveEntitiesSynced, request.getDateFormat(), request.getLocale());
        }

        if (!changes.isEmpty()) {
            if (reschedule == null && request.getStartDate() != null) {
                presentMeetingDate = parseDate(request.getStartDate(), request.getDateFormat(), request.getLocale());
            }
            if (newMeetingDate != null && presentMeetingDate != null) {
                calendarHistory.updateEndDate(presentMeetingDate.minusDays(1));
            }
            this.calendarHistoryRepository.save(calendarHistory);
            Set<CalendarHistory> history = calendarForUpdate.getCalendarHistory();
            history.add(calendarHistory);
            calendarForUpdate.updateCalendarHistory(history);
            this.calendarRepository.saveAndFlush(calendarForUpdate);

            if (this.configurationDomainService.isRescheduleFutureRepaymentsEnabled() && calendarForUpdate.isRepeating()) {
                final Collection<CalendarInstance> loanCalendarInstances = this.calendarInstanceRepository
                        .findByCalendarIdAndEntityTypeId(calendarId, CalendarEntityType.LOANS.getValue());
                if (!CollectionUtils.isEmpty(loanCalendarInstances)) {
                    this.loanWritePlatformService.applyMeetingDateChanges(calendarForUpdate, loanCalendarInstances, reschedule,
                            presentMeetingDate, newMeetingDate);
                }
            }
        }

        return CalendarUpdateResponse.builder().resourceId(calendarForUpdate.getId()).changes(changes).build();
    }

    @Override
    public CommandProcessingResult deleteCalendar(final Long calendarId) {
        final Calendar calendarForDelete = this.calendarRepository.findById(calendarId)
                .orElseThrow(() -> new CalendarNotFoundException(calendarId));

        this.calendarRepository.delete(calendarForDelete);
        return new CommandProcessingResultBuilder() //
                .withCommandId(null) //
                .withEntityId(calendarId) //
                .build();
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
