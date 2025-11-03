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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.dataqueries.service.export.DatatableReportExportService;
import org.apache.fineract.infrastructure.dataqueries.service.export.ResponseHolder;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DatatableReportingProcessServiceTest {

    @Test
    void exportToS3ThrowsGeneralPlatformDomainRuleException() {

        DatatableReportExportService jsonExportService = Mockito.mock(DatatableReportExportService.class);
        Mockito.doReturn(true).when(jsonExportService).supports(DatatableExportTargetParameter.JSON);
        SqlValidator sqlValidator = Mockito.mock(SqlValidator.class);

        DatatableReportingProcessService datatableReportingProcessService = new DatatableReportingProcessService(List.of(jsonExportService),
                sqlValidator);

        MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
        queryParams.put("isSelfServiceUserReport", List.of("false"));
        queryParams.put("R_officeId", List.of("2"));
        queryParams.put("exportS3", List.of("true"));

        GeneralPlatformDomainRuleException exception = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> datatableReportingProcessService.processRequest("clientListing", queryParams));

        assertEquals("error.msg.report.export.mode.unavailable", exception.getGlobalisationMessageCode(),
                "Wrong globalisation message code");
        assertEquals("Export mode S3 unavailable", exception.getDefaultUserMessage(), "Wrong default user message");

    }

    @Test
    void exportToS3ThrowsNoException() {
        DatatableReportExportService jsonExportService = Mockito.mock(DatatableReportExportService.class);
        Mockito.doReturn(true).when(jsonExportService).supports(DatatableExportTargetParameter.S3);

        ResponseHolder responseHolder = new ResponseHolder(Response.Status.CREATED);

        // ContentType.APPLICATION_JSON.toString(), "export.json"
        Mockito.doReturn(responseHolder).when(jsonExportService).export(any(), any(), any(), anyBoolean(), any());
        SqlValidator sqlValidator = Mockito.mock(SqlValidator.class);

        DatatableReportingProcessService datatableReportingProcessService = new DatatableReportingProcessService(List.of(jsonExportService),
                sqlValidator);

        MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
        queryParams.put("isSelfServiceUserReport", List.of("false"));
        queryParams.put("R_officeId", List.of("2"));
        queryParams.put("exportS3", List.of("true"));

        assertDoesNotThrow(() -> datatableReportingProcessService.processRequest("clientListing", queryParams));
    }

}
