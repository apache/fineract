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
package org.apache.fineract.infrastructure.dataqueries.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.api.RunreportsApiResource;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.report.annotation.ReportService;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@ReportService(type = { "Table", "Chart", "SMS" })
public class DatatableReportingProcessService implements ReportingProcessService {

    private final ReadReportingService readExtraDataAndReportingService;
    private final ToApiJsonSerializer<ReportData> toApiJsonSerializer;
    private final GenericDataService genericDataService;

    @Autowired
    public DatatableReportingProcessService(final ReadReportingService readExtraDataAndReportingService,
            final GenericDataService genericDataService, final ToApiJsonSerializer<ReportData> toApiJsonSerializer) {
        this.readExtraDataAndReportingService = readExtraDataAndReportingService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.genericDataService = genericDataService;
    }

    @Override
    public Response processRequest(String reportName, MultivaluedMap<String, String> queryParams) {
        boolean isSelfServiceUserReport = Boolean.parseBoolean(
                queryParams.getOrDefault(RunreportsApiResource.IS_SELF_SERVICE_USER_REPORT_PARAMETER, List.of("false")).get(0));

        DatatableExportTargetParameter exportMode = DatatableExportTargetParameter.checkTarget(queryParams);
        final String parameterTypeValue = ApiParameterHelper.parameterType(queryParams) ? "parameter" : "report";
        final Map<String, String> reportParams = getReportParams(queryParams);
        return switch (exportMode) {
            case CSV -> exportCSV(reportName, queryParams, reportParams, isSelfServiceUserReport, parameterTypeValue);
            case PDF -> exportPDF(reportName, queryParams, reportParams, isSelfServiceUserReport, parameterTypeValue);
            case S3 -> exportS3(reportName, queryParams, reportParams, isSelfServiceUserReport, parameterTypeValue);
            default -> exportJSON(reportName, queryParams, reportParams, isSelfServiceUserReport, parameterTypeValue,
                    exportMode == DatatableExportTargetParameter.PRETTY_JSON);
        };
    }

    private Response exportJSON(String reportName, MultivaluedMap<String, String> queryParams, Map<String, String> reportParams,
            boolean isSelfServiceUserReport, String parameterTypeValue, boolean prettyPrint) {
        final GenericResultsetData result = this.readExtraDataAndReportingService.retrieveGenericResultset(reportName, parameterTypeValue,
                reportParams, isSelfServiceUserReport);

        String json;
        final boolean genericResultSetIsPassed = ApiParameterHelper.genericResultSetPassed(queryParams);
        final boolean genericResultSet = ApiParameterHelper.genericResultSet(queryParams);
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

    private Response exportS3(String reportName, MultivaluedMap<String, String> queryParams, Map<String, String> reportParams,
            boolean isSelfServiceUserReport, String parameterTypeValue) {
        throw new UnsupportedOperationException("S3 export not supported for datatables");
    }

    private Response exportPDF(String reportName, MultivaluedMap<String, String> queryParams, Map<String, String> reportParams,
            boolean isSelfServiceUserReport, String parameterTypeValue) {

        final String pdfFileName = this.readExtraDataAndReportingService.retrieveReportPDF(reportName, parameterTypeValue, reportParams,
                isSelfServiceUserReport);

        final File file = new File(pdfFileName);

        final ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=\"" + pdfFileName + "\"");
        response.header("content-Type", "application/pdf");

        return response.build();
    }

    private Response exportCSV(String reportName, MultivaluedMap<String, String> queryParams, Map<String, String> reportParams,
            boolean isSelfServiceUserReport, String parameterTypeValue) {
        final StreamingOutput result = this.readExtraDataAndReportingService.retrieveReportCSV(reportName, parameterTypeValue, reportParams,
                isSelfServiceUserReport);

        return Response.ok().entity(result).type("text/csv")
                .header("Content-Disposition", "attachment;filename=" + reportName.replaceAll(" ", "") + ".csv").build();

    }
}
