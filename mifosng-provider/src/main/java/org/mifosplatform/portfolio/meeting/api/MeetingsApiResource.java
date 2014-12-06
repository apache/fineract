/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.api;

import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.MEETING_RESOURCE_NAME;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.MEETING_RESPONSE_DATA_PARAMETERS;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.exception.CalendarEntityTypeNotSupportedException;
import org.mifosplatform.portfolio.calendar.service.CalendarReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.meeting.attendance.data.ClientAttendanceData;
import org.mifosplatform.portfolio.meeting.attendance.service.AttendanceDropdownReadPlatformService;
import org.mifosplatform.portfolio.meeting.attendance.service.ClientAttendanceReadPlatformService;
import org.mifosplatform.portfolio.meeting.data.MeetingData;
import org.mifosplatform.portfolio.meeting.exception.MeetingNotSupportedResourceException;
import org.mifosplatform.portfolio.meeting.service.MeetingReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/{entityType}/{entityId}/meetings")
@Component
@Scope("singleton")
public class MeetingsApiResource {

    private final PlatformSecurityContext context;
    private final MeetingReadPlatformService readPlatformService;
    private final ClientAttendanceReadPlatformService attendanceReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final AttendanceDropdownReadPlatformService attendanceDropdownReadPlatformService;
    private final DefaultToApiJsonSerializer<MeetingData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public MeetingsApiResource(final PlatformSecurityContext context, final MeetingReadPlatformService readPlatformService,
            final ClientAttendanceReadPlatformService attendanceReadPlatformService,
            final ClientReadPlatformService clientReadPlatformService, final CalendarReadPlatformService calendarReadPlatformService,
            final AttendanceDropdownReadPlatformService attendanceDropdownReadPlatformService,
            final DefaultToApiJsonSerializer<MeetingData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.attendanceReadPlatformService = attendanceReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.attendanceDropdownReadPlatformService = attendanceDropdownReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String template(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @QueryParam("calendarId") final Long calendarId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(MEETING_RESOURCE_NAME);
        final Integer entityTypeId = CalendarEntityType.valueOf(entityType.toUpperCase()).getValue();
        Collection<ClientData> clients = null;
        CalendarData calendarData = null;

        if (CalendarEntityType.isGroup(entityType)) {
            clients = this.clientReadPlatformService.retrieveActiveClientMembersOfGroup(entityId);
        } else if (CalendarEntityType.isCenter(entityType)) {
            clients = this.clientReadPlatformService.retrieveActiveClientMembersOfCenter(entityId);
        } else {
            final String defaultUserMessage = "Meeting attendance is not supported for the resource " + entityType
                    + ". The supported resources are [" + CalendarEntityType.GROUPS.name() + ", " + CalendarEntityType.CENTERS.name() + "]";
            throw new MeetingNotSupportedResourceException(defaultUserMessage, CalendarEntityType.GROUPS.name(),
                    CalendarEntityType.CENTERS.name());
        }

        if (calendarId != null) {
            calendarData = this.calendarReadPlatformService.retrieveCalendar(calendarId, entityId, entityTypeId);
            final boolean withHistory = true;
            final Collection<LocalDate> recurringDates = this.calendarReadPlatformService.generateRecurringDates(calendarData, withHistory,
                    DateUtils.getLocalDateOfTenant());
            final Collection<LocalDate> nextTenRecurringDates = this.calendarReadPlatformService
                    .generateNextTenRecurringDates(calendarData);
            final LocalDate recentEligibleMeetingDate = null;
            calendarData = CalendarData.withRecurringDates(calendarData, recurringDates, nextTenRecurringDates, recentEligibleMeetingDate);
        }

        final MeetingData meetingData = MeetingData.template(clients, calendarData,
                this.attendanceDropdownReadPlatformService.retrieveAttendanceTypeOptions());

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, meetingData, MEETING_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveMeetings(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @QueryParam("limit") final Integer limit, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(MEETING_RESOURCE_NAME);

        final Collection<MeetingData> meetingsData = this.readPlatformService.retrieveMeetingsByEntity(entityId, CalendarEntityType
                .valueOf(entityType.toUpperCase()).getValue(), limit);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, meetingsData, MEETING_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{meetingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveMeeting(@PathParam("meetingId") final Long meetingId, @PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(MEETING_RESOURCE_NAME);
        final Integer entityTypeId = CalendarEntityType.valueOf(entityType.toUpperCase()).getValue();
        MeetingData meetingData = this.readPlatformService.retrieveMeeting(meetingId, entityId, entityTypeId);
        final Collection<ClientAttendanceData> clientsAttendance = this.attendanceReadPlatformService
                .retrieveClientAttendanceByMeetingId(meetingId);
        meetingData = MeetingData.withClientsAttendanceAndAttendanceTypeOptions(meetingData, clientsAttendance,
                this.attendanceDropdownReadPlatformService.retrieveAttendanceTypeOptions());
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, meetingData, MEETING_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createMeeting(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            final String apiRequestBodyAsJson) {

        final CalendarEntityType calendarEntityType = CalendarEntityType.getEntityType(entityType);
        if (calendarEntityType == null) { throw new CalendarEntityTypeNotSupportedException(entityType); }

        final CommandWrapper resourceDetails = getResourceDetails(calendarEntityType, entityId);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createMeeting(resourceDetails, entityType, entityId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @POST
    @Path("{meetingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String performMeetingCommands(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @PathParam("meetingId") final Long meetingId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        if (is(commandParam, "saveOrUpdateAttendance")) {
            final CommandWrapper commandRequest = builder.saveOrUpdateAttendance(meetingId, entityType, entityId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "saveOrUpdateAttendance" });
        }

        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{meetingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateMeeting(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @PathParam("meetingId") final Long meetingId, final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateMeeting(entityType, entityId, meetingId)
                .withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{meetingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteMeeting(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @PathParam("meetingId") final Long meetingId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteMeeting(entityType, entityId, meetingId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    private CommandWrapper getResourceDetails(final CalendarEntityType type, final Long entityId) {
        CommandWrapperBuilder resourceDetails = new CommandWrapperBuilder();
        switch (type) {
            case CENTERS:
                resourceDetails.withGroupId(entityId);
            break;
            case CLIENTS:
                resourceDetails.withClientId(entityId);
            break;
            case GROUPS:
                resourceDetails.withGroupId(entityId);
            break;
            case LOANS:
                resourceDetails.withLoanId(entityId);
            break;
            case SAVINGS:
                resourceDetails.withSavingsId(entityId);
            break;
            case INVALID:
            break;
            case LOAN_RECALCULATION_DETAIL:
            break;
            default:
            break;
        }
        return resourceDetails.build();
    }

}