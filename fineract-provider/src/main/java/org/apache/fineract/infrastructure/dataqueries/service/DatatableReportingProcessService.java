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

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.service.StreamUtil;
import org.apache.fineract.infrastructure.dataqueries.api.RunreportsApiResource;
import org.apache.fineract.infrastructure.dataqueries.data.ReportExportType;
import org.apache.fineract.infrastructure.dataqueries.service.export.DatatableReportExportService;
import org.apache.fineract.infrastructure.dataqueries.service.export.ResponseHolder;
import org.apache.fineract.infrastructure.report.annotation.ReportService;
import org.apache.fineract.infrastructure.report.service.AbstractReportingProcessService;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.springframework.stereotype.Service;

@Service
@ReportService(type = { "Table", "Chart", "SMS" })
@Slf4j
public class DatatableReportingProcessService extends AbstractReportingProcessService {

    private final List<DatatableReportExportService> exportServices;

    public DatatableReportingProcessService(List<DatatableReportExportService> exportServices, SqlValidator sqlValidator) {
        super(sqlValidator);

        this.exportServices = exportServices;
    }

    @Override
    public Response processRequest(String reportName, MultivaluedMap<String, String> queryParams) {
        boolean isSelfServiceUserReport = Boolean.parseBoolean(
                queryParams.getOrDefault(RunreportsApiResource.IS_SELF_SERVICE_USER_REPORT_PARAMETER, List.of("false")).get(0));

        DatatableExportTargetParameter exportMode = DatatableExportTargetParameter.resolverExportTarget(queryParams);
        final String parameterTypeValue = ApiParameterHelper.parameterType(queryParams) ? "parameter" : "report";
        final Map<String, String> reportParams = getReportParams(queryParams);
        ResponseHolder response = findReportExportService(exportMode) //
                .orElseThrow(() -> new IllegalArgumentException("Unsupported export target: " + exportMode)) //
                .export(reportName, queryParams, reportParams, isSelfServiceUserReport, parameterTypeValue);
        Response.ResponseBuilder builder = Response.status(response.status().getStatusCode());
        if (StringUtils.isNotBlank(response.contentType())) {
            builder = builder.type(response.contentType());
        }
        if (StringUtils.isNotBlank(response.fileName())) {
            builder = builder.header("Content-Disposition", "attachment; filename=" + response.fileName());
        }
        if (response.entity() != null) {
            builder = builder.entity(response.entity());
        }
        if (response.headers() != null && !response.headers().isEmpty()) {
            builder = response.headers().stream().collect(StreamUtil.foldLeft(builder, (b, h) -> b.header(h.getKey(), h.getValue())));
        }
        return builder.build();
    }

    @Override
    public List<ReportExportType> getAvailableExportTargets() {
        return Arrays //
                .stream(DatatableExportTargetParameter.values()) //
                .filter(target -> findReportExportService(target).isPresent()) //
                .map(target -> new ReportExportType(target.name(), target.getValue())) //
                .toList();
    }

    private Optional<DatatableReportExportService> findReportExportService(DatatableExportTargetParameter target) {
        return exportServices.stream().filter(service -> service.supports(target)).findFirst();
    }

}
