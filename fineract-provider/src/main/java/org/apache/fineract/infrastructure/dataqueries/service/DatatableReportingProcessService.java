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
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(queryParams);
        final boolean exportCsv = ApiParameterHelper.exportCsv(queryParams);
        final boolean exportPdf = ApiParameterHelper.exportPdf(queryParams);
        final String parameterTypeValue = ApiParameterHelper.parameterType(queryParams) ? "parameter" : "report";

        // PDF format
        if (exportPdf) {
            final Map<String, String> reportParams = getReportParams(queryParams);
            final String pdfFileName = this.readExtraDataAndReportingService.retrieveReportPDF(reportName, parameterTypeValue, reportParams,
                    isSelfServiceUserReport);

            final File file = new File(pdfFileName);

            final ResponseBuilder response = Response.ok(file);
            response.header("Content-Disposition", "attachment; filename=\"" + pdfFileName + "\"");
            response.header("content-Type", "application/pdf");

            return response.build();
        }

        // JSON format
        if (!exportCsv) {
            final Map<String, String> reportParams = getReportParams(queryParams);

            final GenericResultsetData result = this.readExtraDataAndReportingService.retrieveGenericResultset(reportName,
                    parameterTypeValue, reportParams, isSelfServiceUserReport);

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

        // CSV format
        final Map<String, String> reportParams = getReportParams(queryParams);
        final StreamingOutput result = this.readExtraDataAndReportingService.retrieveReportCSV(reportName, parameterTypeValue, reportParams,
                isSelfServiceUserReport);

        return Response.ok().entity(result).type("text/csv")
                .header("Content-Disposition", "attachment;filename=" + reportName.replaceAll(" ", "") + ".csv").build();
    }
}
