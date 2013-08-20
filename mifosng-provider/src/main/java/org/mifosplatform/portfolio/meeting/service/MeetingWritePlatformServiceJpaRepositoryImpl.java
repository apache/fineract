/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.service;

import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.attendanceTypeParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.calendarIdParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.clientIdParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.clientsAttendanceParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.meetingDateParamName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

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
import org.mifosplatform.portfolio.meeting.domain.MeetingRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class MeetingWritePlatformServiceJpaRepositoryImpl implements MeetingWritePlatformService {

    private final MeetingRepositoryWrapper meetingRepository;
    private final MeetingDataValidator meetingDataValidator;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final CalendarRepository calendarRepository;
    private final ClientRepository clientRepository;
    private final GroupRepository groupRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public MeetingWritePlatformServiceJpaRepositoryImpl(MeetingRepositoryWrapper meetingRepository,
            MeetingDataValidator meetingDataValidator, CalendarInstanceRepository calendarInstanceRepository,
            CalendarRepository calendarRepository, final ClientRepository clientRepository, final GroupRepository groupRepository,
            final FromJsonHelper fromApiJsonHelper) {
        this.meetingRepository = meetingRepository;
        this.meetingDataValidator = meetingDataValidator;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.calendarRepository = calendarRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CommandProcessingResult createMeeting(JsonCommand command) {

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
            this.meetingRepository.save(newMeeting);
            final Long groupId = newMeeting.isGroupEntity() ? newMeeting.entityId() : null;
            return new CommandProcessingResultBuilder() //
                    .withEntityId(newMeeting.getId()) //
                    .withGroupId(groupId).build();

        } catch (DataIntegrityViolationException dve) {
            handleMeetingDataIntegrityIssues(meetingDate, dve);
            return new CommandProcessingResultBuilder() //
                    .build();
        }
    }

    private CalendarInstance getCalendarInstance(final JsonCommand command) {
        final Long calendarId = command.longValueOfParameterNamed(calendarIdParamName);
        final Long entityId = command.getSupportedEntityId();
        final Calendar calendarForUpdate = this.calendarRepository.findOne(calendarId);
        if (calendarForUpdate == null) { throw new CalendarNotFoundException(calendarId); }

        final Integer entityTypeId = CalendarEntityType.valueOf(command.getSupportedEntityType().toUpperCase()).getValue();
        final CalendarInstance calendarInstance = calendarInstanceRepository.findByCalendarIdAndEntityIdAndEntityTypeId(
                calendarForUpdate.getId(), entityId, entityTypeId);
        if(calendarInstance == null) {
            final String postFix = "for." + command.getSupportedEntityType() + "not.found";
            final String defaultUserMessage = "No Calendar Instance details found for group with identifier " + entityId + " and calendar with identifier " + calendarId;
            throw new CalendarInstanceNotFoundException(postFix, defaultUserMessage, entityId, calendarId);
        }
        return calendarInstance;
    }

    private Collection<ClientAttendance> getClientsAttendance(final Meeting meeting, final JsonCommand command) {
        final Collection<ClientAttendance> clientsAttendance = new ArrayList<ClientAttendance>();

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
                            for (Group group : childGroups) {
                                if (group.isChildClient(clientId)){
                                    isChildClient = true;
                                    break;
                                }
                            }
                            if(!isChildClient){
                                final String defaultUserMessage = "Client with identifier " + clientId + " is not in center "
                                        + meeting.entityId();
                                throw new ClientNotInGroupException("client.not.in.center", defaultUserMessage, clientId, meeting.entityId());
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
    public CommandProcessingResult updateMeeting(JsonCommand command) {
        this.meetingDataValidator.validateForUpdate(command);

        final Meeting meetingForUpdate = this.meetingRepository.findOneWithNotFoundDetection(command.entityId());
        final Map<String, Object> changes = meetingForUpdate.update(command);

        try {
            if (!changes.isEmpty()) {
                this.meetingRepository.saveAndFlush(meetingForUpdate);
            }
        } catch (DataIntegrityViolationException dve) {
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
    public CommandProcessingResult deleteMeeting(Long meetingId) {
        final Meeting meetingForDelete = this.meetingRepository.findOneWithNotFoundDetection(meetingId);
        this.meetingRepository.delete(meetingForDelete);
        return new CommandProcessingResultBuilder() //
                .withEntityId(meetingId) //
                .build();
    }

    @Override
    public CommandProcessingResult saveOrUpdateAttendance(JsonCommand command) {
        this.meetingDataValidator.validateForUpdateAttendance(command);

        final Meeting meetingForUpdate = this.meetingRepository.findOneWithNotFoundDetection(command.entityId());
        final Collection<ClientAttendance> clientsAttendance = getClientsAttendance(meetingForUpdate, command);
        final Map<String, Object> changes = meetingForUpdate.updateAttendance(clientsAttendance);

        this.meetingRepository.saveAndFlush(meetingForUpdate);
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

        throw new PlatformDataIntegrityException("error.msg.meeting.duplicate", "A meeting with date '" + meetingDate + "' already exists",
                meetingDateParamName, meetingDate); }

        throw new PlatformDataIntegrityException("error.msg.meeting.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
