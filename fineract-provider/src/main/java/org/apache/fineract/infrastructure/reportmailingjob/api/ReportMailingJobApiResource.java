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
import jakarta.ws.rs.DELETE;
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
import lombok.RequiredArgsConstructor;
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
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.springframework.stereotype.Component;

@Path("/v1/" + ReportMailingJobConstants.REPORT_MAILING_JOB_RESOURCE_NAME)
@Component
@Tag(name = "Report Mailing Jobs", description = "This resource allows you to create a scheduled job that runs a report and sents it by email to specified email addresses.\n\nThe scheduled job can be configured to run once or on a regular basis (once a day, twice a week, etc)")
@RequiredArgsConstructor
public class ReportMailingJobApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<ReportMailingJobData> reportMailingToApiJsonSerializer;
    private final ReportMailingJobReadPlatformService reportMailingJobReadPlatformService;
    private final SqlValidator sqlValidator;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Report Mailing Job", description = "Mandatory Fields: "
            + "name, startDateTime, stretchyReportId, emailRecipients, emailSubject, emailMessage, emailAttachmentFileFormatId, recurrence, isActive\n"
            + "\n" + "Optional Fields: " + "description, stretchyReportParamMap")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ReportMailingJobApiResourceSwagger.PostReportMailingJobsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportMailingJobApiResourceSwagger.PostReportMailingJobsResponse.class))) })
    public String createReportMailingJob(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder()
                .createReportMailingJob(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);

        return this.reportMailingToApiJsonSerializer.serialize(commandProcessingResult);
    }

    @PUT
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Report Mailing Job\n", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ReportMailingJobApiResourceSwagger.PutReportMailingJobsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportMailingJobApiResourceSwagger.PutReportMailingJobsResponse.class))) })
    public String updateReportMailingJob(@PathParam("entityId") @Parameter(description = "entityId") final Long entityId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder()
                .updateReportMailingJob(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME, entityId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);

        return this.reportMailingToApiJsonSerializer.serialize(commandProcessingResult);
    }

    @DELETE
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Report Mailing Job", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ReportMailingJobApiResourceSwagger.DeleteReportMailingJobsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportMailingJobApiResourceSwagger.DeleteReportMailingJobsResponse.class))) })
    public String deleteReportMailingJob(@PathParam("entityId") @Parameter(description = "entityId") final Long entityId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder()
                .deleteReportMailingJob(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME, entityId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);

        return this.reportMailingToApiJsonSerializer.serialize(commandProcessingResult);
    }

    @GET
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Report Mailing Job", description = "Example Requests:\n" + "\n" + "reportmailingjobs/1\n" + "\n" + "\n"
            + "reportmailingjobs/1?template=true")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportMailingJobApiResourceSwagger.GetReportMailingJobsResponse.class))) })
    public String retrieveReportMailingJob(@PathParam("entityId") @Parameter(description = "entityId") final Long entityId,
            @Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser()
                .validateHasReadPermission(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        ReportMailingJobData reportMailingJobData = this.reportMailingJobReadPlatformService.retrieveReportMailingJob(entityId);

        if (settings.isTemplate()) {
            final ReportMailingJobData ReportMailingJobDataOptions = this.reportMailingJobReadPlatformService
                    .retrieveReportMailingJobEnumOptions();
            reportMailingJobData = ReportMailingJobData.newInstance(reportMailingJobData, ReportMailingJobDataOptions);
        }

        return this.reportMailingToApiJsonSerializer.serialize(settings, reportMailingJobData,
                ReportMailingJobConstants.REPORT_MAILING_JOB_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Report Mailing Job Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for report mailing job applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "reportmailingjobs/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportMailingJobApiResourceSwagger.GetReportMailingJobsTemplate.class))) })
    public String retrieveReportMailingJobTemplate(@Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser()
                .validateHasReadPermission(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final ReportMailingJobData ReportMailingJobDataOptions = this.reportMailingJobReadPlatformService
                .retrieveReportMailingJobEnumOptions();

        return this.reportMailingToApiJsonSerializer.serialize(settings, ReportMailingJobDataOptions,
                ReportMailingJobConstants.REPORT_MAILING_JOB_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Report Mailing Jobs", description = "Example Requests:\n" + "\n" + "reportmailingjobs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReportMailingJobApiResourceSwagger.GetReportMailingJobsResponse.class)))) })
    public String retrieveAllReportMailingJobs(@Context final UriInfo uriInfo,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {
        this.platformSecurityContext.authenticatedUser()
                .validateHasReadPermission(ReportMailingJobConstants.REPORT_MAILING_JOB_ENTITY_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();
        final Page<ReportMailingJobData> reportMailingJobData = this.reportMailingJobReadPlatformService
                .retrieveAllReportMailingJobs(searchParameters);

        return this.reportMailingToApiJsonSerializer.serialize(settings, reportMailingJobData,
                ReportMailingJobConstants.REPORT_MAILING_JOB_DATA_PARAMETERS);
    }
}
