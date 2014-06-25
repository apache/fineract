/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.api;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
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

import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ReportData;
import org.mifosplatform.infrastructure.dataqueries.service.GenericDataService;
import org.mifosplatform.infrastructure.dataqueries.service.ReadReportingService;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
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

    @Autowired
    public RunreportsApiResource(final PlatformSecurityContext context, final ReadReportingService readExtraDataAndReportingService,
            final GenericDataService genericDataService, final ToApiJsonSerializer<ReportData> toApiJsonSerializer) {
        this.context = context;
        this.readExtraDataAndReportingService = readExtraDataAndReportingService;
        this.genericDataService = genericDataService;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @GET
    @Path("{reportName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, "application/x-msdownload", "application/vnd.ms-excel", "application/pdf", "text/html" })
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
            if (this.readExtraDataAndReportingService.getReportType(reportName).equalsIgnoreCase("Pentaho")) {
                final Map<String, String> reportParams = getReportParams(queryParams, true);
                final Locale locale = ApiParameterHelper.extractLocale(queryParams);
                return this.readExtraDataAndReportingService.processPentahoRequest(reportName, queryParams.getFirst("output-type"),
                        reportParams, locale);
            }
        } else {
            parameterTypeValue = "parameter";
        }

        // PDF format

        if (exportPdf) {
            final Map<String, String> reportParams = getReportParams(queryParams, false);
            final String pdfFileName = this.readExtraDataAndReportingService
                    .retrieveReportPDF(reportName, parameterTypeValue, reportParams);

            final File file = new File(pdfFileName);

            final ResponseBuilder response = Response.ok(file);
            response.header("Content-Disposition", "attachment; filename=\"" + pdfFileName + "\"");
            response.header("content-Type", "application/pdf");

            return response.build();

        }

        if (!exportCsv) {
            final Map<String, String> reportParams = getReportParams(queryParams, false);

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
        final Map<String, String> reportParams = getReportParams(queryParams, false);
        final StreamingOutput result = this.readExtraDataAndReportingService
                .retrieveReportCSV(reportName, parameterTypeValue, reportParams);

        return Response.ok().entity(result).type("application/x-msdownload")
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

    private Map<String, String> getReportParams(final MultivaluedMap<String, String> queryParams, final Boolean isPentaho) {

        final Map<String, String> reportParams = new HashMap<>();
        final Set<String> keys = queryParams.keySet();
        String pKey;
        String pValue;
        for (final String k : keys) {

            if (k.startsWith("R_")) {
                if (isPentaho) {
                    pKey = k.substring(2);
                } else {
                    pKey = "${" + k.substring(2) + "}";
                }

                pValue = queryParams.get(k).get(0);
                reportParams.put(pKey, pValue);
            }
        }
        return reportParams;
    }
}