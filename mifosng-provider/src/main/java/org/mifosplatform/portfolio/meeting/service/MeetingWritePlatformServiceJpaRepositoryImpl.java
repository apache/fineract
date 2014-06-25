/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.service;

import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.transactionDateParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.attendanceTypeParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.calendarIdParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.clientIdParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.clientsAttendanceParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.meetingDateParamName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.calendar.domain.CalendarRepository;
import org.mifosplatform.portfolio.calendar.exception.CalendarInstanceNotFoundException;
import org.mifosplatform.portfolio.calendar.exception.CalendarNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.ClientNotInGroupException;
import org.mifosplatform.portfolio.meeting.attendance.domain.ClientAttendance;
import org.mifosplatform.portfolio.meeting.data.MeetingDataValidator;
import org.mifosplatform.portfolio.meeting.domain.Meeting;
import org.mifosplatform.portfolio.meeting.domain.MeetingRepository;
import org.mifosplatform.portfolio.meeting.domain.MeetingRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class MeetingWritePlatformServiceJpaRepositoryImpl implements MeetingWritePlatformService {

    private final MeetingRepositoryWrapper meetingRepositoryWrapper;
    private final MeetingRepository meetingRepository;
    private final MeetingDataValidator meetingDataValidator;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final CalendarRepository calendarRepository;
    private final ClientRepository clientRepository;
    private final GroupRepository groupRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public MeetingWritePlatformServiceJpaRepositoryImpl(final MeetingRepositoryWrapper meetingRepositoryWrapper,
            final MeetingRepository meetingRepository, final MeetingDataValidator meetingDataValidator,
            final CalendarInstanceRepository calendarInstanceRepository, final CalendarRepository calendarRepository,
            final ClientRepository clientRepository, final GroupRepository groupRepository, final FromJsonHelper fromApiJsonHelper) {
        this.meetingRepositoryWrapper = meetingRepositoryWrapper;
        this.meetingRepository = meetingRepository;
        this.meetingDataValidator = meetingDataValidator;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.calendarRepository = calendarRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CommandProcessingResult createMeeting(final JsonCommand command) {

        this.meetingDataValidator.validateForCreate(command);

        final Date meetingDate = command.DateValueOfParameterNamed(meetingDateParamName);

        try {
            final CalendarInstance calendarInstance = getCalendarInstance(command);
            // create new meeting
            final Meeting newMeeting = Meeting.createNew(calendarInstance, meetingDate);

            final Collection<ClientAttendance> clientsAttendance = getClientsAttendance(newMeeting, command);
            if (clientsAttendance != null && !clientsAttendance.isEmpty()) {
                newMeeting.associateClientsAttendance(clientsAttendance);
            }
            // save meeting details
            this.meetingRepositoryWrapper.save(newMeeting);
            final Long groupId = newMeeting.isGroupEntity() ? newMeeting.entityId() : null;
            return new CommandProcessingResultBuilder() //
                    .withEntityId(newMeeting.getId()) //
                    .withGroupId(groupId).build();

        } catch (final DataIntegrityViolationException dve) {
            handleMeetingDataIntegrityIssues(meetingDate, dve);
            return new CommandProcessingResultBuilder() //
                    .build();
        }
    }

    private CalendarInstance getCalendarInstance(final JsonCommand command) {

        final Long calendarId = command.longValueOfParameterNamed(calendarIdParamName);
        final Calendar calendarForUpdate = this.calendarRepository.findOne(calendarId);
        if (calendarForUpdate == null) { throw new CalendarNotFoundException(calendarId); }

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
             * If group is within a center then center entityType should be
             * passed for retrieving CalendarInstance.
             */
            final Group group = this.groupRepository.findOne(entityId);
            if (group.isCenter()) {
                entityType = CalendarEntityType.CENTERS;
            } else if (group.isChildGroup()) {
                entityType = CalendarEntityType.CENTERS;
                entityId = group.getParent().getId();
            }
        }

        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findByCalendarIdAndEntityIdAndEntityTypeId(
                calendarForUpdate.getId(), entityId, entityType.getValue());
        if (calendarInstance == null) {
            final String postFix = "for." + entityType.name().toLowerCase() + "not.found";
            final String defaultUserMessage = "No Calendar Instance details found for group with identifier " + entityId
                    + " and calendar with identifier " + calendarId;
            throw new CalendarInstanceNotFoundException(postFix, defaultUserMessage, entityId, calendarId);
        }
        return calendarInstance;
    }

    private Collection<ClientAttendance> getClientsAttendance(final Meeting meeting, final JsonCommand command) {
        final Collection<ClientAttendance> clientsAttendance = new ArrayList<>();

        Collection<Group> childGroups = null;
        if (meeting.isCenterEntity()) {
            childGroups = this.groupRepository.findByParentId(meeting.entityId());
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

                    final Client client = this.clientRepository.findOne(clientId);

                    if (meeting.isGroupEntity() && !client.isChildOfGroup(meeting.entityId())) {
                        throw new ClientNotInGroupException(clientId, meeting.entityId());
                    } else if (meeting.isCenterEntity()) {
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
                                        + meeting.entityId();
                                throw new ClientNotInGroupException("client.not.in.center", defaultUserMessage, clientId,
                                        meeting.entityId());
                            }
                        }
                    }

                    final ClientAttendance clientAttendance = ClientAttendance.createClientAttendance(client, meeting, attendanceTypeId);
                    clientsAttendance.add(clientAttendance);
                }
            }
        }
        return clientsAttendance;
    }

    @Override
    public CommandProcessingResult updateMeeting(final JsonCommand command) {
        this.meetingDataValidator.validateForUpdate(command);

        final Meeting meetingForUpdate = this.meetingRepositoryWrapper.findOneWithNotFoundDetection(command.entityId());
        final Map<String, Object> changes = meetingForUpdate.update(command);

        try {
            if (!changes.isEmpty()) {
                this.meetingRepositoryWrapper.saveAndFlush(meetingForUpdate);
            }
        } catch (final DataIntegrityViolationException dve) {
            handleMeetingDataIntegrityIssues(meetingForUpdate.getMeetingDate(), dve);
            return new CommandProcessingResultBuilder() //
                    .build();
        }
        final Long groupId = meetingForUpdate.isGroupEntity() ? meetingForUpdate.entityId() : null;
        return new CommandProcessingResultBuilder() //
                .withEntityId(meetingForUpdate.getId()) //
                .withGroupId(groupId) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult deleteMeeting(final Long meetingId) {
        final Meeting meetingForDelete = this.meetingRepositoryWrapper.findOneWithNotFoundDetection(meetingId);
        this.meetingRepositoryWrapper.delete(meetingForDelete);
        return new CommandProcessingResultBuilder() //
                .withEntityId(meetingId) //
                .build();
    }

    @Override
    public CommandProcessingResult saveOrUpdateAttendance(final JsonCommand command) {
        this.meetingDataValidator.validateForUpdateAttendance(command);

        final Meeting meetingForUpdate = this.meetingRepositoryWrapper.findOneWithNotFoundDetection(command.entityId());
        final Collection<ClientAttendance> clientsAttendance = getClientsAttendance(meetingForUpdate, command);
        final Map<String, Object> changes = meetingForUpdate.updateAttendance(clientsAttendance);

        this.meetingRepositoryWrapper.saveAndFlush(meetingForUpdate);
        final Long groupId = meetingForUpdate.isGroupEntity() ? meetingForUpdate.entityId() : null;
        return new CommandProcessingResultBuilder() //
                .withEntityId(meetingForUpdate.getId()) //
                .withGroupId(groupId) //
                .with(changes) //
                .build();
    }

    private void handleMeetingDataIntegrityIssues(final Date meetingDate, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("unique_calendar_instance_id_meeting_date")) {
            final LocalDate meetingDateLocal = LocalDate.fromDateFields(meetingDate);
            throw new PlatformDataIntegrityException("error.msg.meeting.duplicate", "A meeting with date '" + meetingDateLocal
                    + "' already exists", meetingDateParamName, meetingDateLocal);
        }

        throw new PlatformDataIntegrityException("error.msg.meeting.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    @Override
    public void updateCollectionSheetAttendance(final JsonCommand command) {
        final Date meetingDate = command.DateValueOfParameterNamed(transactionDateParamName);

        try {
            final CalendarInstance calendarInstance = getCalendarInstance(command);
            final Meeting meeting = this.meetingRepository.findByCalendarInstanceIdAndMeetingDate(calendarInstance.getId(), meetingDate);

            // create new meeting
            final Meeting newMeeting = (meeting != null) ? meeting : Meeting.createNew(calendarInstance, meetingDate);

            final Collection<ClientAttendance> clientsAttendance = getClientsAttendance(newMeeting, command);
            if (clientsAttendance != null && !clientsAttendance.isEmpty()) {
                newMeeting.updateAttendance(clientsAttendance);
            }
            // save meeting details
            this.meetingRepositoryWrapper.save(newMeeting);
        } catch (final DataIntegrityViolationException dve) {
            handleMeetingDataIntegrityIssues(meetingDate, dve);
        }

    }

}
