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
import org.apache.fineract.infrastructure.dataqueries.data.ReportParameters;
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
@Tag(name = "Run Reports", description = "API for executing predefined reports with dynamic parameters")
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
    @Operation(summary = "Return all available export types for the specific report", description = "Returns the list of all available export types for a given report.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReportExportType.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid report name or parameters"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    public Response retrieveAllAvailableExports(
            @PathParam("reportName") @Parameter(description = "Name of the report to get available export types for", example = "Client Listing", required = true) final String reportName,
            @Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam(IS_SELF_SERVICE_USER_REPORT_PARAMETER) @Parameter(description = "Indicates if this is a self-service user report", example = "false") final boolean isSelfServiceUserReport) {

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
    @Operation(summary = "Run a predefined report", description = ReportParameters.FULL_DESCRIPTION)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK - Report executed successfully", content = @Content(schema = @Schema(implementation = RunreportsApiResourceSwagger.RunReportsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Missing or invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Not authorized to run this report"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    public Response runReport(
            @PathParam("reportName") @Parameter(description = "The name of the report to execute (e.g., 'Client Listing', 'Expected Payments By Date')", example = "Client Listing", required = true) final String reportName,
            @Context final UriInfo uriInfo,

            @DefaultValue("false") @QueryParam(IS_SELF_SERVICE_USER_REPORT_PARAMETER) @Parameter(description = "Whether this is a self-service user report", example = "false") final boolean isSelfServiceUserReport,

            @DefaultValue("false") @QueryParam("exportCSV") @Parameter(description = "Set to true to export results as CSV", example = "false") final Boolean exportCSV,

            @DefaultValue("false") @QueryParam("parameterType") @Parameter(description = "Indicates if this is a parameter type request", example = "false") final Boolean parameterType,

            @QueryParam("output-type") @Parameter(description = "Output format type (HTML, XLS, CSV, PDF)", example = "HTML") final String outputType,

            @QueryParam("R_officeId") @Parameter(description = "Office ID filter", example = "1") final String rOfficeId,

            @QueryParam("R_loanOfficerId") @Parameter(description = "Loan officer ID filter", example = "5") final String rLoanOfficerId,

            @QueryParam("R_fromDate") @Parameter(description = "Start date filter (yyyy-MM-dd)", example = "2023-01-01") final String rFromDate,

            @QueryParam("R_toDate") @Parameter(description = "End date filter (yyyy-MM-dd)", example = "2023-12-31") final String rToDate,

            @QueryParam("R_currencyId") @Parameter(description = "Currency ID filter", example = "USD") final String rCurrencyId,

            @QueryParam("R_accountNo") @Parameter(description = "Account number filter", example = "00010001") final String rAccountNo) {

        return processReportRequest(reportName, uriInfo, isSelfServiceUserReport);
    }

    public Response runReport(final String reportName, final UriInfo uriInfo, final boolean isSelfServiceUserReport) {

        return processReportRequest(reportName, uriInfo, isSelfServiceUserReport);
    }

    private Response processReportRequest(final String reportName, final UriInfo uriInfo, final boolean isSelfServiceUserReport) {
        MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
        queryParams.putAll(uriInfo.getQueryParameters());

        final boolean parameterTypeValue = ApiParameterHelper.parameterType(queryParams);

        checkUserPermissionForReport(reportName, parameterTypeValue);

        // Pass through isSelfServiceUserReport so that ReportingProcessService implementations can use it
        queryParams.putSingle(IS_SELF_SERVICE_USER_REPORT_PARAMETER, Boolean.toString(isSelfServiceUserReport));

        String reportType = readExtraDataAndReportingService.getReportType(reportName, isSelfServiceUserReport, parameterTypeValue);
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
