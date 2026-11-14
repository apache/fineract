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
package org.apache.fineract.infrastructure.dataqueries.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/reports")
@Component
@Tag(name = "Reports", description = "Non-core reports can be added, updated and deleted.")
@RequiredArgsConstructor
public class ReportsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "REPORT";
    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<ReportData> toApiJsonSerializer;
    private final ReadReportingService readReportingService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ReportingProcessServiceProvider reportingProcessServiceProvider;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Reports", operationId = "retrieveAllReports", description = "Lists all reports and their parameters.\n"
            + "\n" + "Example Request:\n" + "\n" + "reports")
    @AlternativeOperationId("retrieveReportList")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReportsApiResourceSwagger.GetReportsResponse.class))))
    public Collection<ReportData> retrieveReportList(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return this.readReportingService.retrieveReportList();
    }

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Report\n", operationId = "retrieveOneReport", description = "Example Requests:\n" + "\n"
            + "reports/1\n" + "\n" + "\n" + "reports/1?template=true")
    @AlternativeOperationId("retrieveReport")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportsApiResourceSwagger.GetReportsResponse.class)))
    public ReportData retrieveReport(@PathParam("id") @Parameter(description = "id") final Long id, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ReportData result = this.readReportingService.retrieveReport(id);

        if (ApiParameterHelper.template(uriInfo.getQueryParameters())) {
            result.appendedTemplate(this.readReportingService.getAllowedParameters(),
                    this.reportingProcessServiceProvider.findAllReportingTypes());
        }
        return result;
    }

    @GET
    @Path("template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Report Template", operationId = "retrieveTemplateReport", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "\n" + "Example Request : \n" + "\n" + "reports/template")
    @AlternativeOperationId("retrieveOfficeTemplate")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportsApiResourceSwagger.GetReportsTemplateResponse.class)))
    public ReportData retrieveOfficeTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ReportData result = new ReportData();
        result.appendedTemplate(this.readReportingService.getAllowedParameters(),
                this.reportingProcessServiceProvider.findAllReportingTypes());

        return result;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Report", operationId = "createReport", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ReportsApiResourceSwagger.PostRepostRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportsApiResourceSwagger.PostReportsResponse.class)))
    public CommandProcessingResult createReport(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createReport().withJson(apiRequestBodyAsJson).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Report", operationId = "updateReport", description = "Only the useReport description can be updated for core reports.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ReportsApiResourceSwagger.PutReportRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportsApiResourceSwagger.PutReportResponse.class)))
    public String updateReport(@PathParam("id") @Parameter(description = "id") final Long id,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateReport(id).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Report", operationId = "deleteReport", description = "Only non-core reports can be deleted.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReportsApiResourceSwagger.DeleteReportsResponse.class)))
    public CommandProcessingResult deleteReport(@PathParam("id") @Parameter(description = "id") final Long id) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteReport(id).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

}
