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
package org.apache.fineract.infrastructure.jobs.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.jobs.data.SchedulerDetailData;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/v1/scheduler")
@Component
@Tag(name = "Scheduler", description = "")
public class SchedulerApiResource {

    private final PlatformSecurityContext context;
    private final JobRegisterService jobRegisterService;
    private final ToApiJsonSerializer<SchedulerDetailData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public SchedulerApiResource(final PlatformSecurityContext context, final JobRegisterService jobRegisterService,
            final ToApiJsonSerializer<SchedulerDetailData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.context = context;
        this.jobRegisterService = jobRegisterService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Scheduler Status", description = "Returns the scheduler status.\n" + "\n" + "Example Requests:\n" + "\n"
            + "scheduler")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SchedulerApiResourceSwagger.GetSchedulerResponse.class))) })
    public String retrieveStatus(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(SchedulerJobApiConstants.SCHEDULER_RESOURCE_NAME);
        final boolean isSchedulerRunning = this.jobRegisterService.isSchedulerRunning();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final SchedulerDetailData schedulerDetailData = new SchedulerDetailData().setActive(isSchedulerRunning);
        return this.toApiJsonSerializer.serialize(settings, schedulerDetailData,
                SchedulerJobApiConstants.SCHEDULER_DETAIL_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Activate Scheduler Jobs | Suspend Scheduler Jobs", description = "Activates the scheduler job service. | Suspends the scheduler job service.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "POST :  scheduler?command=start\n\n" + "\n"
            + "POST : scheduler?command=stop") })
    public Response changeSchedulerStatus(
            @QueryParam(SchedulerJobApiConstants.COMMAND) @Parameter(description = "command") final String commandParam) {
        // check the logged in user have permissions to update scheduler status
        final boolean hasNotPermission = this.context.authenticatedUser().hasNotPermissionForAnyOf("ALL_FUNCTIONS", "UPDATE_SCHEDULER");
        if (hasNotPermission) {
            final String authorizationMessage = "User has no authority to update scheduler status";
            throw new NoAuthorizationException(authorizationMessage);
        }
        Response response = Response.status(400).build();
        if (is(commandParam, SchedulerJobApiConstants.COMMAND_START_SCHEDULER)) {
            this.jobRegisterService.startScheduler();
            response = Response.status(202).build();
        } else if (is(commandParam, SchedulerJobApiConstants.COMMAND_STOP_SCHEDULER)) {
            this.jobRegisterService.pauseScheduler();
            response = Response.status(202).build();
        } else {
            throw new UnrecognizedQueryParamException(SchedulerJobApiConstants.COMMAND, commandParam);
        }
        return response;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
}
