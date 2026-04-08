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
package org.apache.fineract.portfolio.meeting.listener;

import static java.util.Objects.requireNonNull;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.attendanceTypeParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.calendarIdParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.clientIdParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.clientsAttendanceParamName;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.exception.CalendarInstanceNotFoundException;
import org.apache.fineract.portfolio.calendar.exception.CalendarNotFoundException;
import org.apache.fineract.portfolio.calendar.exception.NotValidRecurringDateException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.apache.fineract.portfolio.meeting.domain.Meeting;
import org.apache.fineract.portfolio.meeting.domain.MeetingAttendance;
import org.apache.fineract.portfolio.meeting.domain.MeetingRepository;
import org.apache.fineract.portfolio.meeting.domain.MeetingRepositoryWrapper;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Deprecated(forRemoval = true)
final class LegacyMeetingAttendanceListener {

    private static final String COLLECTIONSHEET_ENTITY_NAME = "collectionsheet";

    private final ConfigurationDomainService configurationDomainService;
    private final CalendarRepository calendarRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingRepositoryWrapper meetingRepositoryWrapper;
    private final GroupRepository groupRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final FromJsonHelper fromApiJsonHelper;

    @EventListener
    void onUpdate(JsonCommand command) {
        if (COLLECTIONSHEET_ENTITY_NAME.equalsIgnoreCase(command.getEntityName())) {
            updateCollectionSheetAttendance(command);
        }
    }

    private void updateCollectionSheetAttendance(final JsonCommand command) {
        var meetingDate = command.dateValueOfParameterNamed("transactionDate");
        var isTransactionDateOnNonMeetingDate = command.booleanPrimitiveValueOfParameterNamed("isTransactionDateOnNonMeetingDate");

        try {
            var calendarInstance = getCalendarInstance(command);

            var meeting = meetingRepository.findByCalendarInstanceIdAndMeetingDate(calendarInstance.getId(), meetingDate);
            var isSkipRepaymentOnFirstMonth = false;
            var numberOfDays = 0;
            boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
            if (isSkipRepaymentOnFirstMonthEnabled) {
                isSkipRepaymentOnFirstMonth = true;
                if (isSkipRepaymentOnFirstMonth) {
                    numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
                }
            }
            // create new meeting
            var newMeeting = (meeting != null) ? meeting
                    : createNew(calendarInstance, meetingDate, isTransactionDateOnNonMeetingDate, isSkipRepaymentOnFirstMonth,
                            numberOfDays);

            var clientsAttendance = getClientsAttendance(newMeeting, command);

            if (!clientsAttendance.isEmpty()) {
                updateAttendance(newMeeting, clientsAttendance);
            }
            // save meeting details
            this.meetingRepositoryWrapper.save(newMeeting);
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleMeetingDataIntegrityIssues(meetingDate, throwable, dve);
        }
    }

    public void updateAttendance(Meeting meeting, final Collection<MeetingAttendance> clientsAttendance) {
        var result = new ArrayList<MeetingAttendance>();

        // TODO: never use "goto" statemements... ever; leaving it here, because the whole class will eventually
        // disappear
        updateAttendanceLoop: for (final MeetingAttendance clientAttendance : clientsAttendance) {
            if (meeting.getClientsAttendance() == null) {
                meeting.setClientsAttendance(new HashSet<>());
            }
            for (final MeetingAttendance clientAttendanceOriginal : meeting.getClientsAttendance()) {
                if (requireNonNull(clientAttendanceOriginal.getClient().getId()).equals(clientAttendance.getClient().getId())) {
                    if (!clientAttendance.getAttendanceTypeId().equals(clientAttendanceOriginal.getAttendanceTypeId())) {
                        clientAttendanceOriginal.setAttendanceTypeId(clientAttendance.getAttendanceTypeId());
                    }
                    continue updateAttendanceLoop;
                }
            }

            result.add(clientAttendance);
        }

        if (!result.isEmpty()) {
            clientsAttendance.addAll(result);
        }
    }

    private void handleMeetingDataIntegrityIssues(final LocalDate meetingDate, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("unique_calendar_instance_id_meeting_date")) {
            throw new PlatformDataIntegrityException("error.msg.meeting.duplicate",
                    "A meeting with date '" + meetingDate + "' already exists", "meetingDate", meetingDate);
        }

        throw ErrorHandler.getMappable(dve, "error.msg.meeting.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    private Collection<MeetingAttendance> getClientsAttendance(final Meeting meeting, final JsonCommand command) {
        // TODO: maybe slightly more modern approach could be to introduce Jackson's ObjectMapper to avoid all these
        // magic strings; not sure if it's worth it though

        final Collection<MeetingAttendance> clientsAttendance = new ArrayList<>();

        Collection<Group> childGroups = null;
        if (CalendarEntityType.isCenter(meeting.getCalendarInstance().getEntityTypeId())) {
            childGroups = this.groupRepository.findByParentId(meeting.getCalendarInstance().getEntityId());
        }

        final String json = command.json();
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(clientsAttendanceParamName) && topLevelJsonElement.get(clientsAttendanceParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(clientsAttendanceParamName).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject attendanceElement = array.get(i).getAsJsonObject();
                    final Long clientId = this.fromApiJsonHelper.extractLongNamed(clientIdParamName, attendanceElement);
                    final Integer attendanceTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(attendanceTypeParamName,
                            attendanceElement);

                    final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId, true);

                    if (CalendarEntityType.isGroup(meeting.getCalendarInstance().getEntityTypeId())
                            && !client.isChildOfGroup(meeting.getCalendarInstance().getEntityId())) {
                        throw new ClientNotInGroupException(clientId, meeting.getCalendarInstance().getEntityId());
                    } else if (CalendarEntityType.isCenter(meeting.getCalendarInstance().getEntityTypeId())) {
                        if (childGroups != null && !childGroups.isEmpty()) {
                            boolean isChildClient = false;
                            for (final Group group : childGroups) {
                                if (group.isChildClient(clientId)) {
                                    isChildClient = true;
                                    break;
                                }
                            }
                            if (!isChildClient) {
                                final String defaultUserMessage = "Client with identifier " + clientId + " is not in center "
                                        + meeting.getCalendarInstance().getEntityId();
                                throw new ClientNotInGroupException("client.not.in.center", defaultUserMessage, clientId,
                                        meeting.getCalendarInstance().getEntityId());
                            }
                        }
                    }

                    var clientAttendance = new MeetingAttendance(client, meeting, attendanceTypeId);
                    clientsAttendance.add(clientAttendance);
                }
            }
        }
        return clientsAttendance;
    }

    private Meeting createNew(final CalendarInstance calendarInstance, final LocalDate meetingDate,
            Boolean isTransactionDateOnNonMeetingDate, final boolean isSkipRepaymentOnFirstMonth, final int numberOfDays) {

        if (!isTransactionDateOnNonMeetingDate && (meetingDate == null
                || !calendarInstance.getCalendar().isValidRecurringDate(meetingDate, isSkipRepaymentOnFirstMonth, numberOfDays))) {
            throw new NotValidRecurringDateException("meeting", "The date '" + meetingDate + "' is not a valid meeting date.", meetingDate);
        }

        return new Meeting(calendarInstance, meetingDate, Set.of());
    }

    private CalendarInstance getCalendarInstance(final JsonCommand command) {

        var calendarId = command.longValueOfParameterNamed(calendarIdParamName);
        var calendarForUpdate = this.calendarRepository.findById(calendarId).orElseThrow(() -> new CalendarNotFoundException(calendarId));

        Long entityId = null;// command.getSupportedEntityId();
        CalendarEntityType entityType = CalendarEntityType.INVALID;// CalendarEntityType.valueOf(command.getSupportedEntityType().toUpperCase());
        if (command.getLoanId() != null) {
            entityId = command.getLoanId();
            entityType = CalendarEntityType.LOANS;
        } else if (command.getClientId() != null) {
            entityId = command.getClientId();
            entityType = CalendarEntityType.CLIENTS;
        } else if (command.getGroupId() != null) {
            entityId = command.getGroupId();
            entityType = CalendarEntityType.GROUPS;
            /*
             * If group is within a center then center entityType should be passed for retrieving CalendarInstance.
             */
            final Group group = this.groupRepository.findById(entityId).orElseThrow();
            if (group.isCenter()) {
                entityType = CalendarEntityType.CENTERS;
            } else if (group.isChildGroup()) {
                entityType = CalendarEntityType.CENTERS;
                entityId = group.getParent().getId();
            }
        }

        final CalendarInstance calendarInstance = this.calendarInstanceRepository
                .findByCalendarIdAndEntityIdAndEntityTypeId(calendarForUpdate.getId(), entityId, entityType.getValue());
        if (calendarInstance == null) {
            final String postFix = "for." + entityType.name().toLowerCase() + "not.found";
            final String defaultUserMessage = "No Calendar Instance details found for group with identifier " + entityId
                    + " and calendar with identifier " + calendarId;
            throw new CalendarInstanceNotFoundException(postFix, defaultUserMessage, entityId, calendarId);
        }
        return calendarInstance;
    }
}
