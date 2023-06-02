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

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableExportTargetParameter;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PdfDatatableReportExportService implements DatatableReportExportService {

    private final ReadReportingService readExtraDataAndReportingService;

    @Override
    public ResponseHolder export(String reportName, MultivaluedMap<String, String> queryParams, Map<String, String> reportParams,
            boolean isSelfServiceUserReport, String parameterTypeValue) {
        final String pdfFileName = this.readExtraDataAndReportingService.retrieveReportPDF(reportName, parameterTypeValue, reportParams,
                isSelfServiceUserReport);

        final File file = new File(pdfFileName);

        return new ResponseHolder(Response.Status.OK).contentType("application/pdf")
                .addHeader("Content-Disposition", "attachment; filename=\"" + pdfFileName + "\"").entity(file);
    }

    @Override
    public boolean supports(DatatableExportTargetParameter exportType) {
        return exportType == DatatableExportTargetParameter.PDF;
    }
}
