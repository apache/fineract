package org.mifosplatform.infrastructure.dataqueries.api;

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

import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.service.ReadReportingService;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/reports")
@Component
@Scope("singleton")
public class ReportsApiResource {

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<GenericResultsetData> toApiJsonSerializer;
    private final ReadReportingService readExtraDataAndReportingService;

    @Autowired
    public ReportsApiResource(final PlatformSecurityContext context, final ReadReportingService readExtraDataAndReportingService,
            final ToApiJsonSerializer<GenericResultsetData> toApiJsonSerializer) {
        this.context = context;
        this.readExtraDataAndReportingService = readExtraDataAndReportingService;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, "application/x-msdownload" })
    public Response retrieveReportList(@Context final UriInfo uriInfo) {

        Map<String, String> extractedQueryParams = new HashMap<String, String>();

        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        boolean exportCsv = ApiParameterHelper.exportCsv(uriInfo.getQueryParameters());

        boolean exportPdf = ApiParameterHelper.exportPdf(uriInfo.getQueryParameters());

        if (exportPdf) {
            String result = this.readExtraDataAndReportingService.retrieveReportPDF(".", ".", extractedQueryParams);
            return Response.ok().entity(result).header("Content-Disposition", "attachment;filename=ReportList.pdf").build();
        }

        if (!exportCsv) {
            GenericResultsetData result = this.readExtraDataAndReportingService.retrieveGenericResultset(".", ".", extractedQueryParams);

            final String json = this.toApiJsonSerializer.serializePretty(prettyPrint, result);

            return Response.ok().entity(json).build();
        }

        StreamingOutput result = this.readExtraDataAndReportingService.retrieveReportCSV(".", ".", extractedQueryParams);

        return Response.ok().entity(result).header("Content-Disposition", "attachment;filename=ReportList.csv").build();
    }

    @GET
    @Path("{reportName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, "application/x-msdownload", "application/vnd.ms-excel", "application/pdf", "text/html" })
    public Response retrieveReport(@PathParam("reportName") final String reportName, @Context final UriInfo uriInfo) {

        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        boolean exportCsv = ApiParameterHelper.exportCsv(uriInfo.getQueryParameters());
        boolean parameterType = ApiParameterHelper.parameterType(uriInfo.getQueryParameters());
        boolean exportPdf = ApiParameterHelper.exportPdf(uriInfo.getQueryParameters());

        checkUserPermissionForReport(reportName, parameterType);

        String parameterTypeValue = null;
        if (!parameterType) {
            parameterTypeValue = "report";
            if (this.readExtraDataAndReportingService.getReportType(reportName).equalsIgnoreCase("Pentaho")) {
                Map<String, String> reportParams = getReportParams(queryParams, true);
                return this.readExtraDataAndReportingService.processPentahoRequest(reportName, queryParams.getFirst("output-type"),
                        reportParams);
            }
        } else {
            parameterTypeValue = "parameter";
        }

        // PDF format

        if (exportPdf) {
            Map<String, String> reportParams = getReportParams(queryParams, false);
            String pdfFileName = this.readExtraDataAndReportingService.retrieveReportPDF(reportName, parameterTypeValue, reportParams);

            File file = new File(pdfFileName);

            ResponseBuilder response = Response.ok(file);
            response.header("Content-Disposition", "attachment; filename=\"" + pdfFileName + "\"");
            response.header("content-Type", "application/pdf");

            return response.build();

        }

        if (!exportCsv) {
            Map<String, String> reportParams = getReportParams(queryParams, false);

            GenericResultsetData result = this.readExtraDataAndReportingService.retrieveGenericResultset(reportName, parameterTypeValue,
                    reportParams);

            final String json = this.toApiJsonSerializer.serializePretty(prettyPrint, result);

            return Response.ok().entity(json).type(MediaType.APPLICATION_JSON).build();
        }

        // CSV Export
        Map<String, String> reportParams = getReportParams(queryParams, false);
        StreamingOutput result = this.readExtraDataAndReportingService.retrieveReportCSV(reportName, parameterTypeValue, reportParams);

        return Response.ok().entity(result).type("application/x-msdownload")
                .header("Content-Disposition", "attachment;filename=" + reportName.replaceAll(" ", "") + ".csv").build();
    }

    private void checkUserPermissionForReport(final String reportName, final boolean parameterType) {

        // Anyone can run a 'report' that is simply getting possible parameter
        // (dropdown listbox) values.

        if (!parameterType) {
            AppUser currentUser = context.authenticatedUser();
            if (currentUser.hasNotPermissionForReport(reportName)) {
                // FIXME - JW - message isnt passing back the message in the
                // string in
                // the json just a generalised not authorised message
                throw new NoAuthorizationException("Not Authorised to Run Report: " + reportName);
            }
        }
    }

    private Map<String, String> getReportParams(final MultivaluedMap<String, String> queryParams, final Boolean isPentaho) {

        Map<String, String> reportParams = new HashMap<String, String>();
        Set<String> keys = queryParams.keySet();
        String pKey;
        String pValue;
        for (String k : keys) {

            if (k.startsWith("R_")) {
                if (isPentaho)
                    pKey = k.substring(2);
                else
                    pKey = "${" + k.substring(2) + "}";

                pValue = queryParams.get(k).get(0);
                reportParams.put(pKey, pValue);
            }
        }
        return reportParams;
    }
}