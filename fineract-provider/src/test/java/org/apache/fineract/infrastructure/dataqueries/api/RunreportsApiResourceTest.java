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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RunreportsApiResourceTest {

    @Mock
    private PlatformSecurityContext platformSecurityContext;

    @Mock
    private ReadReportingService readReportingService;

    @Mock
    private ReportingProcessServiceProvider reportingProcessServiceProvider;

    @Mock
    private ReportingProcessService reportingProcessService;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private RunreportsApiResource runreportsApiResource;

    @Test
    void templateQueryParamIsTreatedAsParameterTypeRequest() {
        // given
        String reportName = "Active Loans - Details";
        MultivaluedStringMap params = new MultivaluedStringMap();
        params.putSingle("template", "true");
        given(uriInfo.getQueryParameters()).willReturn(params);

        // Reporting process service wiring
        given(readReportingService.getReportType(reportName, false, true)).willReturn("Table");
        given(reportingProcessServiceProvider.findReportingProcessService("Table")).willReturn(reportingProcessService);
        given(reportingProcessService.processRequest(any(), any())).willReturn(Response.ok().build());

        // when
        Response response = runreportsApiResource.runReport(reportName, uriInfo, false);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // and the downstream services receive parameterType propagated from template
        ArgumentCaptor<jakarta.ws.rs.core.MultivaluedMap<String, String>> captor = ArgumentCaptor.forClass(jakarta.ws.rs.core.MultivaluedMap.class);
        verify(reportingProcessService).processRequest(any(), captor.capture());
        assertThat(captor.getValue().getFirst("parameterType")).isEqualTo("true");
    }
}
