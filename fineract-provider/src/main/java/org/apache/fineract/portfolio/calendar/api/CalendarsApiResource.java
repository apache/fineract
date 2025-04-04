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
package org.apache.fineract.portfolio.calendar.api;

import static org.apache.fineract.portfolio.calendar.domain.CalendarEntityType.CENTERS;
import static org.apache.fineract.portfolio.calendar.domain.CalendarEntityType.CLIENTS;
import static org.apache.fineract.portfolio.calendar.domain.CalendarEntityType.GROUPS;
import static org.apache.fineract.portfolio.calendar.domain.CalendarEntityType.LOANS;
import static org.apache.fineract.portfolio.calendar.domain.CalendarEntityType.SAVINGS;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.data.request.CalendarRequest;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.exception.CalendarEntityTypeNotSupportedException;
import org.apache.fineract.portfolio.calendar.service.CalendarDropdownReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.springframework.stereotype.Component;

@Path("/v1/{entityType}/{entityId}/calendars")
@Component
@Tag(name = "Calendar", description = "")
@RequiredArgsConstructor
public class CalendarsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "CALENDAR";

    private final PlatformSecurityContext context;
    private final CalendarReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<CalendarData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CalendarDropdownReadPlatformService dropdownReadPlatformService;

    @GET
    @Path("{calendarId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CalendarData retrieveCalendar(@PathParam("calendarId") final Long calendarId, @PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final Integer entityTypeId = CalendarEntityType.valueOf(entityType.toUpperCase()).getValue();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        CalendarData calendarData = readPlatformService.retrieveCalendar(calendarId, entityId, entityTypeId);
        final Collection<LocalDate> recurringDates = readPlatformService.generateRecurringDates(calendarData, true, null);
        final Collection<LocalDate> nextTenRecurringDates = readPlatformService.generateNextTenRecurringDates(calendarData);
        calendarData.setRecurringDates(recurringDates);
        calendarData.setNextTenRecurringDates(nextTenRecurringDates);

        if (settings.isTemplate()) {
            calendarData = handleTemplate(calendarData);
        }
        return calendarData;
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
    public List<CalendarData> retrieveCalendarsByEntity(@PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId, @Context final UriInfo uriInfo,
            @DefaultValue("all") @QueryParam("calendarType") final String calendarType) {

        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        final List<CalendarData> calendarsData = new ArrayList<>();

        final List<Integer> calendarTypeOptions = CalendarUtils.createIntegerListFromQueryParameter(calendarType);

        if (!associationParameters.isEmpty() && associationParameters.contains("parentCalendars")) {
            calendarsData.addAll(readPlatformService.retrieveParentCalendarsByEntity(entityId,
                    CalendarEntityType.valueOf(entityType.toUpperCase()).getValue(), calendarTypeOptions));
        }

        calendarsData.addAll(readPlatformService.retrieveCalendarsByEntity(entityId,
                CalendarEntityType.valueOf(entityType.toUpperCase()).getValue(), calendarTypeOptions));

        return readPlatformService.updateWithRecurringDates(calendarsData);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CalendarData retrieveNewCalendarDetails(@Context final UriInfo uriInfo, @PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return handleTemplate(readPlatformService.retrieveNewCalendarDetails());
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult createCalendar(@PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId, CalendarRequest calendarRequest) {

        final CalendarEntityType calendarEntityType = CalendarEntityType.getEntityType(entityType);
        if (calendarEntityType == null) {
            throw new CalendarEntityTypeNotSupportedException(entityType);
        }

        final CommandWrapper resourceDetails = getResourceDetails(calendarEntityType, entityId);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCalendar(resourceDetails, entityType, entityId)
                .withJson(toApiJsonSerializer.serialize(calendarRequest)).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);

    }

    @PUT
    @Path("{calendarId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult updateCalendar(@PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId, @PathParam("calendarId") final Long calendarId, final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCalendar(entityType, entityId, calendarId)
                .withJson(jsonRequestBody).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{calendarId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult deleteCalendar(@PathParam("entityType") final String entityType,
            @PathParam("entityId") final Long entityId, @PathParam("calendarId") final Long calendarId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCalendar(entityType, entityId, calendarId).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private CalendarData handleTemplate(final CalendarData calendarData) {
        final List<EnumOptionData> entityTypeOptions = dropdownReadPlatformService.retrieveCalendarEntityTypeOptions();
        final List<EnumOptionData> calendarTypeOptions = dropdownReadPlatformService.retrieveCalendarTypeOptions();
        final List<EnumOptionData> remindByOptions = dropdownReadPlatformService.retrieveCalendarRemindByOptions();
        final List<EnumOptionData> frequencyOptions = dropdownReadPlatformService.retrieveCalendarFrequencyTypeOptions();
        final List<EnumOptionData> repeatsOnDayOptions = dropdownReadPlatformService.retrieveCalendarWeekDaysTypeOptions();
        final List<EnumOptionData> frequencyNthDayTypeOptions = dropdownReadPlatformService.retrieveCalendarFrequencyNthDayTypeOptions();

        return calendarData.setEntityTypeOptions(entityTypeOptions).setCalendarTypeOptions(calendarTypeOptions)
                .setRemindByOptions(remindByOptions).setFrequencyOptions(frequencyOptions).setRepeatsOnDayOptions(repeatsOnDayOptions)
                .setFrequencyNthDayTypeOptions(frequencyNthDayTypeOptions);
    }

    private CommandWrapper getResourceDetails(final CalendarEntityType type, final Long entityId) {
        final CommandWrapperBuilder resourceDetails = new CommandWrapperBuilder();
        return Map.of(CENTERS, resourceDetails.withGroupId(entityId).build(), CLIENTS, resourceDetails.withClientId(entityId).build(),
                GROUPS, resourceDetails.withGroupId(entityId).build(), LOANS, resourceDetails.withLoanId(entityId).build(), SAVINGS,
                resourceDetails.withSavingsId(entityId).build()).getOrDefault(type, resourceDetails.build());
    }

}
