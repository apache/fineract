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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportParameter;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportParameterRepository;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportParameterUsage;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportParameterUsageRepository;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportRepository;
import org.apache.fineract.infrastructure.dataqueries.serialization.ReportCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportWritePlatformServiceImplTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ReportCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ReportParameterRepository reportParameterRepository;
    @Mock
    private ReportParameterUsageRepository reportParameterUsageRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private ReportingProcessServiceProvider reportingProcessServiceProvider;

    private ReportWritePlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        when(reportingProcessServiceProvider.findAllReportingTypes()).thenReturn(List.of("Table"));
        when(reportParameterRepository.findById(1L)).thenReturn(Optional.of(mock(ReportParameter.class)));
        service = new ReportWritePlatformServiceImpl(context, fromApiJsonDeserializer, reportRepository, reportParameterRepository,
                reportParameterUsageRepository, permissionRepository, reportingProcessServiceProvider);
    }

    @Test
    void createReportAcceptsExplicitNullReportParameterName() {
        final Report saved = createReport("{\"id\": \"\", \"parameterId\": 1, \"reportParameterName\": null}");

        assertEquals(1, saved.getReportParameterUsages().size());
        final ReportParameterUsage usage = saved.getReportParameterUsages().iterator().next();
        assertNull(usage.getReportParameterName());
    }

    @Test
    void createReportAcceptsExplicitNullId() {
        final Report saved = createReport("{\"id\": null, \"parameterId\": 1, \"reportParameterName\": \"officeId\"}");

        assertEquals(1, saved.getReportParameterUsages().size());
        assertEquals("officeId", saved.getReportParameterUsages().iterator().next().getReportParameterName());
    }

    private Report createReport(final String reportParameter) {
        final String json = "{\"reportName\": \"Null param test\", \"reportType\": \"Table\", \"reportCategory\": \"Client\","
                + " \"reportSql\": \"select 1\", \"reportParameters\": [" + reportParameter + "]}";
        final JsonElement parsed = fromJsonHelper.parse(json);
        final JsonCommand command = JsonCommand.from(json, parsed, fromJsonHelper, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);

        service.createReport(command);

        verify(fromApiJsonDeserializer).validate(anyString());
        final ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }
}
