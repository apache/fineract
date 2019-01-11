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
package org.apache.fineract.organisation.holiday.api;

import static org.apache.fineract.organisation.holiday.api.HolidayApiConstants.HOLIDAY_RESOURCE_NAME;
import static org.apache.fineract.organisation.holiday.api.HolidayApiConstants.HOLIDAY_RESPONSE_DATA_PARAMETERS;

import java.util.Collection;
import java.util.Date;

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

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.data.HolidayData;
import org.apache.fineract.organisation.holiday.service.HolidayReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/holidays")
@Component
@Scope("singleton")
@Api(value = "Holidays", description = "Some MFI's span large regions where different branch offices might observe different holidays. They need the ability to define holidays for specific branch offices and be able to set the repayment rule to follow during those holidays.\n" + "\n" + "The reschedule of repayments to repaymentsRescheduledTo date during defined holidays is turned on/off by enabling/disabling reschedule-repayments-on-holidays in Global configurations.\n" + "\n" + "Allow Repayment transactions on a defined holidays is turned on/off by enabling/disabling allow-transactions-on-holiday in Global configurations.")
public class HolidaysApiResource {

    private final DefaultToApiJsonSerializer<HolidayData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    private final HolidayReadPlatformService holidayReadPlatformService;

    @Autowired
    public HolidaysApiResource(final DefaultToApiJsonSerializer<HolidayData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, final PlatformSecurityContext context,
            final ApiRequestParameterHelper apiRequestParameterHelper, final HolidayReadPlatformService holidayReadPlatformService) {
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.holidayReadPlatformService = holidayReadPlatformService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Holiday", notes = "Mandatory Fields: " + "name, description, fromDate, toDate, repaymentsRescheduledTo, offices")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = HolidaysApiResourceSwagger.PostHolidaysRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = HolidaysApiResourceSwagger.PostHolidaysResponse.class)})
    public String createNewHoliday(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createHoliday().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{holidayId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Activate a Holiday", notes = "Always Holidays are created in pending state. This API allows to activate a holiday.\n" + "\n" + "Only the active holidays are considered for rescheduling the loan repayment.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = HolidaysApiResourceSwagger.PostHolidaysHolidayIdRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = HolidaysApiResourceSwagger.PostHolidaysHolidayIdResponse.class)})
    public String handleCommands(@PathParam("holidayId") @ApiParam(value = "holidayId") final Long holidayId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.activateHoliday(holidayId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) { throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "activate" }); }

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{holidayId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Holiday", notes = "Example Requests:\n" + "\n" + "holidays/1")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = HolidaysApiResourceSwagger.GetHolidaysResponse.class)})
    public String retrieveOne(@PathParam("holidayId") @ApiParam(value = "holidayId") final Long holidayId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(HOLIDAY_RESOURCE_NAME);

        final HolidayData holidayData = this.holidayReadPlatformService.retrieveHoliday(holidayId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, holidayData, HOLIDAY_RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{holidayId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Holiday", notes = "If a holiday is in pending state (created and not activated) then all fields are allowed to modify. Once holidays become active only name and descriptions are allowed to modify.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = HolidaysApiResourceSwagger.PutHolidaysHolidayIdRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = HolidaysApiResourceSwagger.PutHolidaysHolidayIdResponse.class)})
    public String update(@PathParam("holidayId") @ApiParam(value = "holidayId") final Long holidayId, @ApiParam(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateHoliday(holidayId).withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{holidayId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Holiday", notes = "This API allows to delete a holiday. This is a soft delete the deleted holiday status is marked as deleted.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = HolidaysApiResourceSwagger.DeleteHolidaysHolidayIdResponse.class)})
    public String delete(@PathParam("holidayId") @ApiParam(value = "holidayId") final Long holidayId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteHoliday(holidayId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Holidays", notes = "Example Requests:\n" + "\n" + "holidays?officeId=1")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = HolidaysApiResourceSwagger.GetHolidaysResponse.class, responseContainer = "List")})
    public String retrieveAllHolidays(@Context final UriInfo uriInfo, @QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId,
            @QueryParam("fromDate") @ApiParam(value = "fromDate") final DateParam fromDateParam, @QueryParam("toDate") @ApiParam(value = "toDate") final DateParam toDateParam,
            @QueryParam("locale") @ApiParam(value = "locale") final String locale, @QueryParam("dateFormat") @ApiParam(value = "dateFormat") final String dateFormat) {

        this.context.authenticatedUser().validateHasReadPermission(HOLIDAY_RESOURCE_NAME);

        Date fromDate = null;
        if (fromDateParam != null) {
            fromDate = fromDateParam.getDate("fromDate", dateFormat, locale);
        }
        Date toDate = null;
        if (toDateParam != null) {
            toDate = toDateParam.getDate("toDate", dateFormat, locale);
        }

        final Collection<HolidayData> holidays = this.holidayReadPlatformService.retrieveAllHolidaysBySearchParamerters(officeId, fromDate,
                toDate);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, holidays, HOLIDAY_RESPONSE_DATA_PARAMETERS);
    }
    
    @GET
    @Path("/template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveRepaymentScheduleUpdationTyeOptions(@Context final UriInfo uriInfo){
        this.context.authenticatedUser().validateHasReadPermission(HOLIDAY_RESOURCE_NAME);
        return this.toApiJsonSerializer.serialize(this.holidayReadPlatformService.retrieveRepaymentScheduleUpdationTyeOptions());
    }
}
