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
package org.apache.fineract.portfolio.meeting.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.exception.CalendarEntityTypeNotSupportedException;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.meeting.command.MeetingAttendanceUpdateCommand;
import org.apache.fineract.portfolio.meeting.command.MeetingCreateCommand;
import org.apache.fineract.portfolio.meeting.command.MeetingDeleteCommand;
import org.apache.fineract.portfolio.meeting.command.MeetingUpdateCommand;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceUpdateRequest;
import org.apache.fineract.portfolio.meeting.data.MeetingAttendanceUpdateResponse;
import org.apache.fineract.portfolio.meeting.data.MeetingCreateRequest;
import org.apache.fineract.portfolio.meeting.data.MeetingCreateResponse;
import org.apache.fineract.portfolio.meeting.data.MeetingData;
import org.apache.fineract.portfolio.meeting.data.MeetingDeleteRequest;
import org.apache.fineract.portfolio.meeting.data.MeetingDeleteResponse;
import org.apache.fineract.portfolio.meeting.data.MeetingUpdateRequest;
import org.apache.fineract.portfolio.meeting.data.MeetingUpdateResponse;
import org.apache.fineract.portfolio.meeting.exception.MeetingNotSupportedResourceException;
import org.apache.fineract.portfolio.meeting.service.MeetingAttendanceDropdownReadService;
import org.apache.fineract.portfolio.meeting.service.MeetingAttendanceReadService;
import org.apache.fineract.portfolio.meeting.service.MeetingReadService;
import org.springframework.stereotype.Component;

@Path("/v1/{entityType}/{entityId}/meetings")
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Meetings")
@RequiredArgsConstructor
public class MeetingsApiResource {

    private final MeetingReadService meetingReadService;
    private final MeetingAttendanceReadService meetingAttendanceReadService;
    private final MeetingAttendanceDropdownReadService meetingAttendanceDropdownReadService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final CommandDispatcher dispatcher;

    @GET
    @Path("template")
    public MeetingData template(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @QueryParam("calendarId") final Long calendarId) {

        CalendarData calendarData = null;

        if (calendarId != null) {
            calendarData = calendarReadPlatformService.retrieveCalendar(calendarId, entityId,
                    CalendarEntityType.valueOf(entityType.toUpperCase()).getValue());

            var recurringDates = calendarReadPlatformService.generateRecurringDates(calendarData, true, DateUtils.getBusinessLocalDate());
            var nextTenRecurringDates = calendarReadPlatformService.generateNextTenRecurringDates(calendarData);

            calendarData = CalendarData.withRecurringDates(calendarData, recurringDates, nextTenRecurringDates, null);
        }

        if (CalendarEntityType.isGroup(entityType)) {
            return MeetingData.builder().clients(clientReadPlatformService.retrieveActiveClientMembersOfGroup(entityId))
                    .calendarData(calendarData).attendanceTypeOptions(meetingAttendanceDropdownReadService.retrieveAttendanceTypeOptions())
                    .build();
        } else if (CalendarEntityType.isCenter(entityType)) {
            return MeetingData.builder().clients(clientReadPlatformService.retrieveActiveClientMembersOfCenter(entityId))
                    .calendarData(calendarData).attendanceTypeOptions(meetingAttendanceDropdownReadService.retrieveAttendanceTypeOptions())
                    .build();
        } else {
            throw new MeetingNotSupportedResourceException(
                    "Meeting attendance is not supported for the resource " + entityType + ". The supported resources are ["
                            + CalendarEntityType.GROUPS.name() + ", " + CalendarEntityType.CENTERS.name() + "]",
                    CalendarEntityType.GROUPS.name(), CalendarEntityType.CENTERS.name());
        }

    }

    @GET
    public Collection<MeetingData> retrieveMeetings(@PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId, @QueryParam("limit") final Integer limit) {

        return meetingReadService.retrieveMeetingsByEntity(entityId, CalendarEntityType.valueOf(entityType.toUpperCase()).getValue(),
                limit);
    }

    @GET
    @Path("{meetingId}")
    public MeetingData retrieveMeeting(@PathParam("meetingId") final Long meetingId, @PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId) {

        var meetingData = meetingReadService.retrieveMeeting(meetingId, entityId,
                CalendarEntityType.valueOf(entityType.toUpperCase()).getValue());
        var clientsAttendance = meetingAttendanceReadService.retrieveClientAttendanceByMeetingId(meetingId);

        return MeetingData.builder().id(meetingData.getId()).meetingDate(meetingData.getMeetingDate()).clients(meetingData.getClients())
                .calendarData(meetingData.getCalendarData()).clientsAttendance(clientsAttendance)
                .attendanceTypeOptions(meetingAttendanceDropdownReadService.retrieveAttendanceTypeOptions()).build();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    public MeetingCreateResponse createMeeting(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            final MeetingCreateRequest request) {

        var calendarEntityType = CalendarEntityType.getEntityType(entityType);

        if (calendarEntityType == null) {
            throw new CalendarEntityTypeNotSupportedException(entityType);
        }

        request.setEntityId(entityId);
        request.setEntityType(calendarEntityType);

        final var command = new MeetingCreateCommand();

        command.setPayload(request);

        final Supplier<MeetingCreateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @PUT
    @Path("{meetingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public MeetingUpdateResponse updateMeeting(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @PathParam("meetingId") final Long meetingId, final MeetingUpdateRequest request) {

        var calendarEntityType = CalendarEntityType.getEntityType(entityType);

        if (calendarEntityType == null) {
            throw new CalendarEntityTypeNotSupportedException(entityType);
        }

        request.setEntityId(entityId);
        request.setEntityType(calendarEntityType);
        request.setId(meetingId);

        final var command = new MeetingUpdateCommand();

        command.setPayload(request);

        final Supplier<MeetingUpdateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @DELETE
    @Path("{meetingId}")
    public MeetingDeleteResponse deleteMeeting(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @PathParam("meetingId") final Long meetingId) {

        var request = MeetingDeleteRequest.builder().id(meetingId).entityId(entityId).entityType(entityType).build();

        final var command = new MeetingDeleteCommand();

        command.setPayload(request);

        final Supplier<MeetingDeleteResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @POST
    @Path("{meetingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    public MeetingAttendanceUpdateResponse updateMeetingAttendance(@PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId, @PathParam("meetingId") final Long meetingId,
            @QueryParam("command") final String commandParam, final MeetingAttendanceUpdateRequest request) {

        final CalendarEntityType calendarEntityType = CalendarEntityType.getEntityType(entityType);

        if (calendarEntityType == null) {
            throw new CalendarEntityTypeNotSupportedException(entityType);
        }

        request.setEntityId(entityId);
        request.setEntityType(calendarEntityType);
        request.setId(meetingId);

        final var command = new MeetingAttendanceUpdateCommand();

        command.setPayload(request);

        final Supplier<MeetingAttendanceUpdateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }
}
