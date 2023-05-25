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
package org.apache.fineract.organisation.workingdays.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysData;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/workingdays")
@Component
@Tag(name = "Working days", description = "The days of the week that are workdays.\n" + "\n"
        + "Rescheduling of repayments when it falls on a non-working is turned on /off by enable/disable reschedule-future-repayments parameter in Global configurations\n"
        + "\n"
        + "Allow transactions on non-working days is configurable by enabling/disbaling the allow-transactions-on-non_workingday parameter in Global configurations.")
@RequiredArgsConstructor
public class WorkingDaysApiResource {

    private final DefaultToApiJsonSerializer<WorkingDaysData> toApiJsonSerializer;
    private final WorkingDaysReadPlatformService workingDaysReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Working days", description = "Example Requests:\n" + "\n" + "workingdays")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingDaysApiResourceSwagger.GetWorkingDaysResponse.class)))) })
    public String retrieveAll(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(WorkingDaysApiConstants.WORKING_DAYS_RESOURCE_NAME);
        final WorkingDaysData workingDaysData = this.workingDaysReadPlatformService.retrieve();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, workingDaysData);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Working Day", description = "Mandatory Fields\n"
            + "recurrence,repaymentRescheduleType,extendTermForDailyRepayments,locale")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingDaysApiResourceSwagger.PutWorkingDaysRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingDaysApiResourceSwagger.PutWorkingDaysResponse.class))) })
    public String update(@Parameter(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateWorkingDays().withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Working Days Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for working days.\n"
            + "\n" + "Example Request:\n" + "\n" + "workingdays/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingDaysApiResourceSwagger.GetWorkingDaysTemplateResponse.class))) })
    public String template(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(WorkingDaysApiConstants.WORKING_DAYS_RESOURCE_NAME);

        final WorkingDaysData repaymentRescheduleOptions = this.workingDaysReadPlatformService.repaymentRescheduleType();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, repaymentRescheduleOptions,
                WorkingDaysApiConstants.WORKING_DAYS_TEMPLATE_PARAMETERS);
    }

}
