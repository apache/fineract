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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/workingdays")
@Component
@Scope("singleton")
@Api(tags = {"Working days"})
@SwaggerDefinition(tags = {
        @Tag(name = "Working days", description = "The days of the week that are workdays.\n" + "\n" + "Rescheduling of repayments when it falls on a non-working is turned on /off by enable/disable reschedule-future-repayments parameter in Global configurations\n" + "\n" + "Allow transactions on non-working days is configurable by enabling/disbaling the allow-transactions-on-non_workingday parameter in Global configurations.")
})
public class WorkingDaysApiResource {

    private final DefaultToApiJsonSerializer<WorkingDaysData> toApiJsonSerializer;
    private final WorkingDaysReadPlatformService workingDaysReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public WorkingDaysApiResource(DefaultToApiJsonSerializer<WorkingDaysData> toApiJsonSerializer,
            WorkingDaysReadPlatformService workingDaysReadPlatformService,
            PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, PlatformSecurityContext context,
            ApiRequestParameterHelper apiRequestParameterHelper) {
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.workingDaysReadPlatformService = workingDaysReadPlatformService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Working days", notes = "Example Requests:\n" + "\n" + "workingdays")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = WorkingDaysApiResourceSwagger.GetWorkingDaysResponse.class, responseContainer = "list")})
    public String retrieveAll(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(WorkingDaysApiConstants.WORKING_DAYS_RESOURCE_NAME);
        final WorkingDaysData workingDaysData = this.workingDaysReadPlatformService.retrieve();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, workingDaysData);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Working Day", notes = "Mandatory Fields\n" + "recurrence,repaymentRescheduleType,extendTermForDailyRepayments,locale")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = WorkingDaysApiResourceSwagger.PutWorkingDaysRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = WorkingDaysApiResourceSwagger.PutWorkingDaysResponse.class)})
    public String update(@ApiParam(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateWorkingDays().withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Working Days Template", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for working days.\n" + "\n" + "Example Request:\n" + "\n" + "workingdays/template")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = WorkingDaysApiResourceSwagger.GetWorkingDaysTemplateResponse.class )})
    public String template(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(WorkingDaysApiConstants.WORKING_DAYS_RESOURCE_NAME);

        final WorkingDaysData repaymentRescheduleOptions = this.workingDaysReadPlatformService.repaymentRescheduleType();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, repaymentRescheduleOptions,
                WorkingDaysApiConstants.WORKING_DAYS_TEMPLATE_PARAMETERS);
    }

}
