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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/runreports")
@Component
@Scope("singleton")
public class RunreportsApiResource {

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<ReportData> toApiJsonSerializer;
    private final ReadReportingService readExtraDataAndReportingService;
    private final GenericDataService genericDataService;
    private final ReportingProcessServiceProvider reportingProcessServiceProvider;

    @Autowired
    public RunreportsApiResource(final PlatformSecurityContext context, final ReadReportingService readExtraDataAndReportingService,
            final GenericDataService genericDataService, final ToApiJsonSerializer<ReportData> toApiJsonSerializer,
            final ReportingProcessServiceProvider reportingProcessServiceProvider) {
        this.context = context;
        this.readExtraDataAndReportingService = readExtraDataAndReportingService;
        this.genericDataService = genericDataService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.reportingProcessServiceProvider = reportingProcessServiceProvider;
    }

    @GET
    @Path("{reportName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, "text/csv", "application/vnd.ms-excel", "application/pdf", "text/html" })
    public Response runReport(@PathParam("reportName") final String reportName, @Context final UriInfo uriInfo) {

        final MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        final boolean exportCsv = ApiParameterHelper.exportCsv(uriInfo.getQueryParameters());
        final boolean parameterType = ApiParameterHelper.parameterType(uriInfo.getQueryParameters());
        final boolean exportPdf = ApiParameterHelper.exportPdf(uriInfo.getQueryParameters());

        checkUserPermissionForReport(reportName, parameterType);

        String parameterTypeValue = null;
        if (!parameterType) {
            parameterTypeValue = "report";
            String reportType = this.readExtraDataAndReportingService.getReportType(reportName);
            ReportingProcessService reportingProcessService = this.reportingProcessServiceProvider.findReportingProcessService(reportType);
            if (reportingProcessService != null) { return reportingProcessService.processRequest(reportName, queryParams); }
        } else {
            parameterTypeValue = "parameter";
        }

        // PDF format

        if (exportPdf) {
            final Map<String, String> reportParams = getReportParams(queryParams);
            final String pdfFileName = this.readExtraDataAndReportingService
                    .retrieveReportPDF(reportName, parameterTypeValue, reportParams);

            final File file = new File(pdfFileName);

            final ResponseBuilder response = Response.ok(file);
            response.header("Content-Disposition", "attachment; filename=\"" + pdfFileName + "\"");
            response.header("content-Type", "application/pdf");

            return response.build();

        }

        if (!exportCsv) {
            final Map<String, String> reportParams = getReportParams(queryParams);

            final GenericResultsetData result = this.readExtraDataAndReportingService.retrieveGenericResultset(reportName,
                    parameterTypeValue, reportParams);

            String json = "";
            final boolean genericResultSetIsPassed = ApiParameterHelper.genericResultSetPassed(uriInfo.getQueryParameters());
            final boolean genericResultSet = ApiParameterHelper.genericResultSet(uriInfo.getQueryParameters());
            if (genericResultSetIsPassed) {
                if (genericResultSet) {
                    json = this.toApiJsonSerializer.serializePretty(prettyPrint, result);
                } else {
                    json = this.genericDataService.generateJsonFromGenericResultsetData(result);
                }
            } else {
                json = this.toApiJsonSerializer.serializePretty(prettyPrint, result);
            }

            return Response.ok().entity(json).type(MediaType.APPLICATION_JSON).build();
        }

        // CSV Export
        final Map<String, String> reportParams = getReportParams(queryParams);
        final StreamingOutput result = this.readExtraDataAndReportingService
                .retrieveReportCSV(reportName, parameterTypeValue, reportParams);

        return Response.ok().entity(result).type("text/csv")
                .header("Content-Disposition", "attachment;filename=" + reportName.replaceAll(" ", "") + ".csv").build();
    }

    private void checkUserPermissionForReport(final String reportName, final boolean parameterType) {

        // Anyone can run a 'report' that is simply getting possible parameter
        // (dropdown listbox) values.
        if (!parameterType) {
            final AppUser currentUser = this.context.authenticatedUser();
            if (currentUser.hasNotPermissionForReport(reportName)) { throw new NoAuthorizationException("Not authorised to run report: "
                    + reportName); }
        }
    }

    private Map<String, String> getReportParams(final MultivaluedMap<String, String> queryParams) {

        final Map<String, String> reportParams = new HashMap<>();
        final Set<String> keys = queryParams.keySet();
        String pKey;
        String pValue;
        for (final String k : keys) {

            if (k.startsWith("R_")) {
                pKey = "${" + k.substring(2) + "}";
                pValue = queryParams.get(k).get(0);
                reportParams.put(pKey, pValue);
            }
        }
        return reportParams;
    }
}