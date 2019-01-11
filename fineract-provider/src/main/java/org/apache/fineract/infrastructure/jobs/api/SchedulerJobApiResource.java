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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.jobs.data.JobDetailData;
import org.apache.fineract.infrastructure.jobs.data.JobDetailHistoryData;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.apache.fineract.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/jobs")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Api(value = "MIFOSX-BATCH JOBS", description = "Batch jobs (also known as cron jobs on Unix-based systems) are a series of back-end jobs executed on a computer at a particular time defined in job's cron expression.\n" + "\n" + "At any point, you can view the list of batch jobs scheduled to run along with other details specific to each job. Manually you can execute the jobs at any point of time.\n" + "\n" + "The scheduler status can be either \"Active\" or \"Standby\". If the scheduler status is Active, it indicates that all batch jobs are running/ will run as per the specified schedule.If the scheduler status is Standby, it will ensure all scheduled batch runs are suspended.")
public class SchedulerJobApiResource {

    private final SchedulerJobRunnerReadService schedulerJobRunnerReadService;
    private final JobRegisterService jobRegisterService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ToApiJsonSerializer<JobDetailData> toApiJsonSerializer;
    private final ToApiJsonSerializer<JobDetailHistoryData> jobHistoryToApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;

    @Autowired
    public SchedulerJobApiResource(final SchedulerJobRunnerReadService schedulerJobRunnerReadService,
            final JobRegisterService jobRegisterService, final ToApiJsonSerializer<JobDetailData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final ToApiJsonSerializer<JobDetailHistoryData> jobHistoryToApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final PlatformSecurityContext context) {
        this.schedulerJobRunnerReadService = schedulerJobRunnerReadService;
        this.jobRegisterService = jobRegisterService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.jobHistoryToApiJsonSerializer = jobHistoryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.context = context;
    }

    @GET
    @ApiOperation(value = "Retrieve Scheduler Jobs", notes = "Returns the list of jobs.\n" + "\n" + "Example Requests:\n" + "\n" + "jobs")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = SchedulerJobApiResourceSwagger.GetJobsResponse.class, responseContainer = "list")})
    public String retrieveAll(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(SchedulerJobApiConstants.SCHEDULER_RESOURCE_NAME);
        final List<JobDetailData> jobDetailDatas = this.schedulerJobRunnerReadService.findAllJobDeatils();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, jobDetailDatas, SchedulerJobApiConstants.JOB_DETAIL_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{" + SchedulerJobApiConstants.JOB_ID + "}")
    @ApiOperation(value = "Retrieve a Job", notes = "Returns the details of a Job.\n" + "\n" + "Example Requests:\n" + "\n" + "jobs/5")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = SchedulerJobApiResourceSwagger.GetJobsResponse.class)})
    public String retrieveOne(@PathParam(SchedulerJobApiConstants.JOB_ID) @ApiParam(value = "jobId") final Long jobId, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(SchedulerJobApiConstants.SCHEDULER_RESOURCE_NAME);
        final JobDetailData jobDetailData = this.schedulerJobRunnerReadService.retrieveOne(jobId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, jobDetailData, SchedulerJobApiConstants.JOB_DETAIL_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{" + SchedulerJobApiConstants.JOB_ID + "}/" + SchedulerJobApiConstants.JOB_RUN_HISTORY)
    @ApiOperation(value = "Retrieve Job Run History", notes = "Example Requests:\n" + "\n" + "jobs/5/runhistory?offset=0&limit=200")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = SchedulerJobApiResourceSwagger.GetJobsJobIDJobRunHistoryResponse.class)})
    public String retrieveHistory(@Context final UriInfo uriInfo, @PathParam(SchedulerJobApiConstants.JOB_ID) @ApiParam(value = "jobId") final Long jobId,
            @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder) {
        this.context.authenticatedUser().validateHasReadPermission(SchedulerJobApiConstants.SCHEDULER_RESOURCE_NAME);
        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);
        final Page<JobDetailHistoryData> jobhistoryDetailData = this.schedulerJobRunnerReadService.retrieveJobHistory(jobId,
                searchParameters);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jobHistoryToApiJsonSerializer.serialize(settings, jobhistoryDetailData,
                SchedulerJobApiConstants.JOB_HISTORY_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Path("{" + SchedulerJobApiConstants.JOB_ID + "}")
    @ApiOperation(value = "Run a Job", notes = "Manually Execute Specific Job.")
    @ApiResponses({@ApiResponse(code = 200, message = "POST: jobs/1?command=executeJob")})
    public Response executeJob(@PathParam(SchedulerJobApiConstants.JOB_ID) @ApiParam(value = "jobId") final Long jobId,
            @QueryParam(SchedulerJobApiConstants.COMMAND) @ApiParam(value = "command") final String commandParam) {
        // check the logged in user have permissions to execute scheduler jobs
        final boolean hasNotPermission = this.context.authenticatedUser().hasNotPermissionForAnyOf("ALL_FUNCTIONS", "EXECUTEJOB_SCHEDULER");
        if (hasNotPermission) {
            final String authorizationMessage = "User has no authority to execute scheduler jobs";
            throw new NoAuthorizationException(authorizationMessage);
        }
        Response response = Response.status(400).build();
        if (is(commandParam, SchedulerJobApiConstants.COMMAND_EXECUTE_JOB)) {
            this.jobRegisterService.executeJob(jobId);
            response = Response.status(202).build();
        } else {
            throw new UnrecognizedQueryParamException(SchedulerJobApiConstants.COMMAND, commandParam);
        }
        return response;
    }

    @PUT
    @Path("{" + SchedulerJobApiConstants.JOB_ID + "}")
    @ApiOperation(value = "Update a Job", notes = "Updates the details of a job.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = SchedulerJobApiResourceSwagger.PutJobsJobIDRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "")})
    public String updateJobDetail(@PathParam(SchedulerJobApiConstants.JOB_ID) @ApiParam(value = "jobId") final Long jobId, @ApiParam(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateJobDetail(jobId) //
                .withJson(jsonRequestBody) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        if (result.getChanges() != null
                && (result.getChanges().containsKey(SchedulerJobApiConstants.jobActiveStatusParamName) || result.getChanges().containsKey(
                        SchedulerJobApiConstants.cronExpressionParamName))) {
            this.jobRegisterService.rescheduleJob(jobId);
        }
        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
}