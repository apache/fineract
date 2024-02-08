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
package org.apache.fineract.infrastructure.dataqueries.service.export;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableExportTargetParameter;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonDatatableReportExportService implements DatatableReportExportService {

    private final ReadReportingService readExtraDataAndReportingService;
    private final ToApiJsonSerializer<ReportData> toApiJsonSerializer;
    private final GenericDataService genericDataService;

    @Override
    public ResponseHolder export(String reportName, MultivaluedMap<String, String> queryParams, Map<String, String> reportParams,
            boolean isSelfServiceUserReport, String parameterTypeValue) {

        final GenericResultsetData result = this.readExtraDataAndReportingService.retrieveGenericResultset(reportName, parameterTypeValue,
                reportParams, isSelfServiceUserReport);
        DatatableExportTargetParameter exportMode = DatatableExportTargetParameter.resolverExportTarget(queryParams);
        boolean prettyPrint = exportMode == DatatableExportTargetParameter.PRETTY_JSON;
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
        return new ResponseHolder(Response.Status.OK).entity(json).contentType(MediaType.APPLICATION_JSON);

    }

    @Override
    public boolean supports(DatatableExportTargetParameter exportType) {
        return exportType == DatatableExportTargetParameter.JSON || exportType == DatatableExportTargetParameter.PRETTY_JSON;
    }
}
