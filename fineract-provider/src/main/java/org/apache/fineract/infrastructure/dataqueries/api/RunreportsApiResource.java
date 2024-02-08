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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.dataqueries.data.ReportExportType;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.springframework.stereotype.Component;

@Path("/v1/runreports")
@Component
@Tag(name = "Run Reports", description = "")
@RequiredArgsConstructor
public class RunreportsApiResource {

    public static final String IS_SELF_SERVICE_USER_REPORT_PARAMETER = "isSelfServiceUserReport";

    private final PlatformSecurityContext context;
    private final ReadReportingService readExtraDataAndReportingService;
    private final ReportingProcessServiceProvider reportingProcessServiceProvider;

    @GET
    @Path("/availableExports/{reportName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Return all available export types for the specific report", description = "Returns the list of all available export types.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReportExportType.class)))) })
    public Response retrieveAllAvailableExports(@PathParam("reportName") @Parameter(description = "reportName") final String reportName,
            @Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam(IS_SELF_SERVICE_USER_REPORT_PARAMETER) @Parameter(description = IS_SELF_SERVICE_USER_REPORT_PARAMETER) final boolean isSelfServiceUserReport) {
        MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
        queryParams.putAll(uriInfo.getQueryParameters());

        final boolean parameterType = ApiParameterHelper.parameterType(queryParams);
        String reportType = readExtraDataAndReportingService.getReportType(reportName, isSelfServiceUserReport, parameterType);
        ReportingProcessService reportingProcessService = reportingProcessServiceProvider.findReportingProcessService(reportType);
        if (reportingProcessService == null) {
            throw new PlatformServiceUnavailableException("err.msg.report.service.implementation.missing",
                    ReportingProcessServiceProvider.SERVICE_MISSING + reportType, reportType);
        }
        return Response.ok().entity(reportingProcessService.getAvailableExportTargets()).build();
    }

    @GET
    @Path("{reportName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, "text/csv", "application/vnd.ms-excel", "application/pdf", "text/html" })
    @Operation(summary = "Running a Report", description = "This resource allows you to run and receive output from pre-defined Apache Fineract reports.\n"
            + "\n" + "Reports can also be used to provide data for searching and workflow functionality.\n" + "\n"
            + "The default output is a JSON formatted \"Generic Resultset\". The Generic Resultset contains Column Heading as well as Data information. However, you can export to CSV format by simply adding \"&exportCSV=true\" to the end of your URL.\n"
            + "\n"
            + "If Pentaho reports have been pre-defined, they can also be run through this resource. Pentaho reports can return HTML, PDF or CSV formats.\n"
            + "\n"
            + "The Apache Fineract reference application uses a JQuery plugin called stretchy reporting which, itself, uses this reports resource to provide a pretty flexible reporting User Interface (UI).\n\n"
            + "\n" + "\n" + "Example Requests:\n" + "\n" + "runreports/Client%20Listing?R_officeId=1\n" + "\n" + "\n"
            + "runreports/Client%20Listing?R_officeId=1&exportCSV=true\n" + "\n" + "\n"
            + "runreports/OfficeIdSelectOne?R_officeId=1&parameterType=true\n" + "\n" + "\n"
            + "runreports/OfficeIdSelectOne?R_officeId=1&parameterType=true&exportCSV=true\n" + "\n" + "\n"
            + "runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=HTML&R_officeId=1\n"
            + "\n" + "\n"
            + "runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=XLS&R_officeId=1\n"
            + "\n" + "\n"
            + "runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=CSV&R_officeId=1\n"
            + "\n" + "\n"
            + "runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=PDF&R_officeId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RunreportsApiResourceSwagger.RunReportsResponse.class))) })
    public Response runReport(@PathParam("reportName") @Parameter(description = "reportName") final String reportName,
            @Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam(IS_SELF_SERVICE_USER_REPORT_PARAMETER) @Parameter(description = IS_SELF_SERVICE_USER_REPORT_PARAMETER) final boolean isSelfServiceUserReport) {

        MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
        queryParams.putAll(uriInfo.getQueryParameters());

        final boolean parameterType = ApiParameterHelper.parameterType(queryParams);

        checkUserPermissionForReport(reportName, parameterType);

        // Pass through isSelfServiceUserReport so that ReportingProcessService implementations can use it
        queryParams.putSingle(IS_SELF_SERVICE_USER_REPORT_PARAMETER, Boolean.toString(isSelfServiceUserReport));

        String reportType = readExtraDataAndReportingService.getReportType(reportName, isSelfServiceUserReport, parameterType);
        ReportingProcessService reportingProcessService = reportingProcessServiceProvider.findReportingProcessService(reportType);
        if (reportingProcessService == null) {
            throw new PlatformServiceUnavailableException("err.msg.report.service.implementation.missing",
                    ReportingProcessServiceProvider.SERVICE_MISSING + reportType, reportType);
        }
        return reportingProcessService.processRequest(reportName, queryParams);
    }

    private void checkUserPermissionForReport(final String reportName, final boolean parameterType) {
        // Anyone can run a 'report' that is simply getting possible parameter
        // (dropdown listbox) values.
        if (!parameterType) {
            final AppUser currentUser = this.context.authenticatedUser();
            if (currentUser.hasNotPermissionForReport(reportName)) {
                throw new NoAuthorizationException("Not authorised to run report: " + reportName);
            }
        }
    }
}
