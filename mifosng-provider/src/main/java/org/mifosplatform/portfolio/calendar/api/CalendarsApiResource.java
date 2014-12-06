/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.exception.CalendarEntityTypeNotSupportedException;
import org.mifosplatform.portfolio.calendar.service.CalendarDropdownReadPlatformService;
import org.mifosplatform.portfolio.calendar.service.CalendarReadPlatformService;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/{entityType}/{entityId}/calendars")
@Component
@Scope("singleton")
public class CalendarsApiResource {

    /**
     * The set of parameters that are supported in response for {@link Calendar}
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "entityId", "entityType", "title",
            "description", "location", "startDate", "endDate", "duration", "type", "repeating", "recurrence", "frequency", "interval",
            "repeatsOnDay", "remindBy", "firstReminder", "secondReminder", "humanReadable", "createdDate", "lastUpdatedDate",
            "createdByUserId", "createdByUsername", "lastUpdatedByUserId", "lastUpdatedByUsername", "recurringDates",
            "nextTenRecurringDates", "entityTypeOptions", "calendarTypeOptions", "remindByOptions", "frequencyOptions",
            "repeatsOnDayOptions"));
    private final String resourceNameForPermissions = "CALENDAR";

    private final PlatformSecurityContext context;
    private final CalendarReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<CalendarData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CalendarDropdownReadPlatformService dropdownReadPlatformService;

    @Autowired
    public CalendarsApiResource(final PlatformSecurityContext context, final CalendarReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<CalendarData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CalendarDropdownReadPlatformService dropdownReadPlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
    }

    @GET
    @Path("{calendarId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCalendar(@PathParam("calendarId") final Long calendarId, @PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final Integer entityTypeId = CalendarEntityType.valueOf(entityType.toUpperCase()).getValue();
        CalendarData calendarData = this.readPlatformService.retrieveCalendar(calendarId, entityId, entityTypeId);

        // Include recurring date details
        final boolean withHistory = true;
        final LocalDate tillDate = null;
        final Collection<LocalDate> recurringDates = this.readPlatformService.generateRecurringDates(calendarData, withHistory, tillDate);
        final Collection<LocalDate> nextTenRecurringDates = this.readPlatformService.generateNextTenRecurringDates(calendarData);
        final LocalDate recentEligibleMeetingDate = null;
        calendarData = CalendarData.withRecurringDates(calendarData, recurringDates, nextTenRecurringDates, recentEligibleMeetingDate);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            calendarData = handleTemplate(calendarData);
        }
        return this.toApiJsonSerializer.serialize(settings, calendarData, this.RESPONSE_DATA_PARAMETERS);
    }

    /**
     * @param entityType
     * @param entityId
     * @param uriInfo
     * @param calendarType
     * @return
     */
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCalendarsByEntity(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @Context final UriInfo uriInfo, @DefaultValue("all") @QueryParam("calendarType") final String calendarType) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());

        Collection<CalendarData> calendarsData = new ArrayList<>();

        final List<Integer> calendarTypeOptions = CalendarUtils.createIntegerListFromQueryParameter(calendarType);

        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("parentCalendars")) {
                calendarsData.addAll(this.readPlatformService.retrieveParentCalendarsByEntity(entityId,
                        CalendarEntityType.valueOf(entityType.toUpperCase()).getValue(), calendarTypeOptions));
            }
        }

        calendarsData.addAll(this.readPlatformService.retrieveCalendarsByEntity(entityId,
                CalendarEntityType.valueOf(entityType.toUpperCase()).getValue(), calendarTypeOptions));

        // Add recurring dates
        calendarsData = this.readPlatformService.updateWithRecurringDates(calendarsData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, calendarsData, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewCalendarDetails(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        CalendarData calendarData = this.readPlatformService.retrieveNewCalendarDetails();
        calendarData = handleTemplate(calendarData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, calendarData, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCalendar(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            final String apiRequestBodyAsJson) {

        final CalendarEntityType calendarEntityType = CalendarEntityType.getEntityType(entityType);
        if (calendarEntityType == null) { throw new CalendarEntityTypeNotSupportedException(entityType); }

        final CommandWrapper resourceDetails = getResourceDetails(calendarEntityType, entityId);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCalendar(resourceDetails, entityType, entityId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{calendarId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCalendar(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @PathParam("calendarId") final Long calendarId, final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCalendar(entityType, entityId, calendarId)
                .withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{calendarId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteCalendar(@PathParam("entityType") final String entityType, @PathParam("entityId") final Long entityId,
            @PathParam("calendarId") final Long calendarId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCalendar(entityType, entityId, calendarId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private CalendarData handleTemplate(final CalendarData calendarData) {
        final List<EnumOptionData> entityTypeOptions = this.dropdownReadPlatformService.retrieveCalendarEntityTypeOptions();
        final List<EnumOptionData> calendarTypeOptions = this.dropdownReadPlatformService.retrieveCalendarTypeOptions();
        final List<EnumOptionData> remindByOptions = this.dropdownReadPlatformService.retrieveCalendarRemindByOptions();
        final List<EnumOptionData> frequencyOptions = this.dropdownReadPlatformService.retrieveCalendarFrequencyTypeOptions();
        final List<EnumOptionData> repeatsOnDayOptions = this.dropdownReadPlatformService.retrieveCalendarWeekDaysTypeOptions();
        return CalendarData.withTemplateOptions(calendarData, entityTypeOptions, calendarTypeOptions, remindByOptions, frequencyOptions,
                repeatsOnDayOptions);
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