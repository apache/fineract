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
package org.apache.fineract.infrastructure.reportmailingjob.api;

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
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.reportmailingjob.ReportMailingJobConstants;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobData;
import org.apache.fineract.infrastructure.reportmailingjob.service.ReportMailingJobReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/" + ReportMailingJobConstants.REPORT_MAILING_JOB_RESOURCE_NAME)
@Component
@Scope("singleton")
@Api(value = "Report Mailing Jobs", description = "This resource allows you to create a scheduled job that runs a report and sents it by email to specified email addresses.\n" + "\n" + "The scheduled job can be configured to run once or on a regular basis (once a day, twice a week, etc).")
public class ReportMailingJobApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<ReportMailingJobData> reportMailingToApiJsonSerializer;
    private final ReportMailingJobReadPlatformService reportMailingJobReadPlatformService;
    
    @Autowired
    public ReportMailingJobApiResource(final PlatformSecurityContext platformSecurityContext, 
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, 
            final ApiRequestParameterHelper apiRequestParameterHelper, 
            final DefaultToApiJsonSerializer<ReportMailingJobData> reportMailingToApiJsonSerializer, 
            final ReportMailingJobReadPlatformService reportMailingJobReadPlatformService) {
        this.platformSecurityContext = platformSecurityContext;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.reportMailingToApiJsonSerializer = reportMailingToApiJsonSerializer;
        this.reportMailingJobReadPlatformService = reportMailingJobReadPlatformService;
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Report Mailing Job", notes = "Mandatory Fields: " + "name, startDateTime, stretchyReportId, emailRecipients, emailSubject, emailMessage, emailAttachmentFileFormatId, recurrence, isActive\n" + "\n" + "Optional Fields: " + "description, stretchyReportParamMap")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ReportMailingJobApiResourceSwagger.PostReportMailingJobsRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportMailingJobApiResourceSwagger.PostReportMailingJobsResponse.class)})
    public String createReportMailingJob(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().
                createReportMailingJob(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME).
                withJson(apiRequestBodyAsJson).build();
        
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        
        return this.reportMailingToApiJsonSerializer.serialize(commandProcessingResult);
    }
    
    @PUT
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Report Mailing Job\n", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ReportMailingJobApiResourceSwagger.PutReportMailingJobsRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportMailingJobApiResourceSwagger.PutReportMailingJobsResponse.class)})
    public String updateReportMailingJob(@PathParam("entityId") @ApiParam(value = "entityId") final Long entityId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().
                updateReportMailingJob(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME, entityId).
                withJson(apiRequestBodyAsJson).build();
        
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        
        return this.reportMailingToApiJsonSerializer.serialize(commandProcessingResult);
    }
    
    @DELETE
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Report Mailing Job", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ReportMailingJobApiResourceSwagger.DeleteReportMailingJobsRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportMailingJobApiResourceSwagger.DeleteReportMailingJobsResponse.class)})
    public String deleteReportMailingJob(@PathParam("entityId") @ApiParam(value = "entityId") final Long entityId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().
                deleteReportMailingJob(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME, entityId).
                withJson(apiRequestBodyAsJson).build();
        
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        
        return this.reportMailingToApiJsonSerializer.serialize(commandProcessingResult);
    }
    
    @GET
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Report Mailing Job", notes = "Example Requests:\n" + "\n" + "reportmailingjobs/1\n" + "\n" + "\n" + "reportmailingjobs/1?template=true")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportMailingJobApiResourceSwagger.GetReportMailingJobsResponse.class)})
    public String retrieveReportMailingJob(@PathParam("entityId") @ApiParam(value = "entityId") final Long entityId, @Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME);
        
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        ReportMailingJobData reportMailingJobData = this.reportMailingJobReadPlatformService.retrieveReportMailingJob(entityId);
        
        if (settings.isTemplate()) {
            final ReportMailingJobData ReportMailingJobDataOptions = this.reportMailingJobReadPlatformService.retrieveReportMailingJobEnumOptions();
            reportMailingJobData = ReportMailingJobData.newInstance(reportMailingJobData, ReportMailingJobDataOptions);
        }
        
        return this.reportMailingToApiJsonSerializer.serialize(settings, reportMailingJobData, ReportMailingJobConstants.REPORT_MAILING_JOB_DATA_PARAMETERS);
    }
    
    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Report Mailing Job Details Template", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for report mailing job applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "reportmailingjobs/template")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportMailingJobApiResourceSwagger.GetReportMailingJobsTemplate.class)})
    public String retrieveReportMailingJobTemplate(@Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME);
        
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final ReportMailingJobData ReportMailingJobDataOptions = this.reportMailingJobReadPlatformService.retrieveReportMailingJobEnumOptions();
        
        return this.reportMailingToApiJsonSerializer.serialize(settings, ReportMailingJobDataOptions, ReportMailingJobConstants.REPORT_MAILING_JOB_DATA_PARAMETERS);
    }
    
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Report Mailing Jobs", notes = "Example Requests:\n" + "\n" + "reportmailingjobs")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportMailingJobApiResourceSwagger.GetReportMailingJobsResponse.class, responseContainer = "list")})
    public String retrieveAllReportMailingJobs(@Context final UriInfo uriInfo, 
            @QueryParam("offset") @ApiParam(value = "offset") final Integer offset,
            @QueryParam("limit") @ApiParam(value = "limit") final Integer limit, @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME);
        
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final SearchParameters searchParameters = SearchParameters.fromReportMailingJob(offset, limit, orderBy, sortOrder);
        final Page<ReportMailingJobData> reportMailingJobData = this.reportMailingJobReadPlatformService.retrieveAllReportMailingJobs(searchParameters);
        
        return this.reportMailingToApiJsonSerializer.serialize(settings, reportMailingJobData, ReportMailingJobConstants.REPORT_MAILING_JOB_DATA_PARAMETERS);
    }
}
