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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/reports")
@Component
@Scope("singleton")
@Api(value = "Reports", description = "Non-core reports can be added, updated and deleted.")
public class ReportsApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "reportName", "reportType", "reportSubType",
            "reportCategory", "description", "reportSql", "coreReport", "useReport", "reportParameters"));

    private final String resourceNameForPermissions = "REPORT";
    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<ReportData> toApiJsonSerializer;
    private final ReadReportingService readReportingService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public ReportsApiResource(final PlatformSecurityContext context, final ReadReportingService readReportingService,
            final ToApiJsonSerializer<ReportData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.context = context;
        this.readReportingService = readReportingService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Reports", notes = "Lists all reports and their parameters.\n" + "\n" + "Example Request:\n" + "\n" + "reports", responseContainer = "List", response = ReportsApiResourceSwagger.GetReportsResponse.class)
    @ApiResponses({@ApiResponse(code = 200, message = "")})
    public String retrieveReportList(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ReportData> result = this.readReportingService.retrieveReportList();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, result, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Report\n", notes = "Example Requests:\n" + "\n" + "reports/1\n" + "\n" + "\n" + "reports/1?template=true")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportsApiResourceSwagger.GetReportsResponse.class)})
    public String retrieveReport(@PathParam("id") @ApiParam(value = "id") final Long id, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ReportData result = this.readReportingService.retrieveReport(id);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (settings.isTemplate()) {
            result.appendedTemplate(this.readReportingService.getAllowedParameters(), this.readReportingService.getAllowedReportTypes());
        }
        return this.toApiJsonSerializer.serialize(settings, result, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Report Template", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "\n" + "Example Request : \n" + "\n" + "reports/template")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportsApiResourceSwagger.GetReportsTemplateResponse.class)})
    public String retrieveOfficeTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ReportData result = new ReportData();
        result.appendedTemplate(this.readReportingService.getAllowedParameters(), this.readReportingService.getAllowedReportTypes());

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, result, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Report", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ReportsApiResourceSwagger.PostRepostRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportsApiResourceSwagger.PostReportsResponse.class)})
    public String createReport(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createReport().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Report", notes = "Only the useReport value can be updated for core reports.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ReportsApiResourceSwagger.PutReportRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportsApiResourceSwagger.PutReportResponse.class)})
    public String updateReport(@PathParam("id") @ApiParam(value = "id") final Long id, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateReport(id).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Report", notes = "Only non-core reports can be deleted.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ReportsApiResourceSwagger.DeleteReportsResponse.class)})
    public String deleteReport(@PathParam("id") @ApiParam(value = "id") final Long id) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteReport(id).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}