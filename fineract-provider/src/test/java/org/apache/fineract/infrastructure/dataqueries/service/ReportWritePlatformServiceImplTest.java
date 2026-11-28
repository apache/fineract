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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
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
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportWritePlatformServiceImplTest {

    private static final Long REPORT_ID = 9L;

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
    private RoleRepository roleRepository;
    @Mock
    private ReportingProcessServiceProvider reportingProcessServiceProvider;

    private ReportWritePlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        when(reportingProcessServiceProvider.findAllReportingTypes()).thenReturn(List.of("Table"));
        when(reportParameterRepository.findById(1L)).thenReturn(Optional.of(mock(ReportParameter.class)));
        service = new ReportWritePlatformServiceImpl(context, fromApiJsonDeserializer, reportRepository, reportParameterRepository,
                reportParameterUsageRepository, permissionRepository, roleRepository, reportingProcessServiceProvider);
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

    @Test
    void createReportTakesItsValuesFromTheCommand() {
        service.createReport(command("""
                {"reportName": "Client list", "reportType": "Table", "reportCategory": "Client", "description": "All clients",
                 "useReport": true, "reportSql": "select 1"}
                """));

        final Report saved = savedReport();
        assertEquals("Client list", saved.getReportName());
        assertEquals("Table", saved.getReportType());
        assertNull(saved.getReportSubType());
        assertEquals("Client", saved.getReportCategory());
        assertEquals("All clients", saved.getDescription());
        assertTrue(saved.isUseReport());
        assertEquals("select 1", saved.getReportSql());
        assertFalse(saved.isCoreReport());
    }

    @Test
    void createReportLeavesMissingValuesUnset() {
        service.createReport(command("""
                {"reportName": "Client list", "reportType": "Table", "reportSql": "select 1"}
                """));

        final Report saved = savedReport();
        // missing text values are stored as null, not as an empty string
        assertNull(saved.getReportCategory());
        assertNull(saved.getDescription());
        assertFalse(saved.isUseReport());
    }

    @Test
    void createReportRejectsAnUnknownReportType() {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.createReport(command("""
                        {"reportName": "Client list", "reportType": "Pivot", "reportSql": "select 1"}
                        """)));

        assertEquals("reportType", exception.getErrors().get(0).getParameterName());
        verify(reportRepository, never()).saveAndFlush(any(Report.class));
    }

    @Test
    void updateReportAppliesAndReportsTheChangedValuesInOrder() {
        final Report report = existingReport();

        final CommandProcessingResult result = service.updateReport(REPORT_ID, command("""
                {"reportSql": "select 2", "useReport": true, "description": "Active clients", "reportName": "Active clients"}
                """));

        assertEquals(List.of("reportName", "description", "useReport", "reportSql"), List.copyOf(result.getChanges().keySet()));
        assertEquals(Map.of("reportName", "Active clients", "description", "Active clients", "useReport", true, "reportSql", "select 2"),
                result.getChanges());
        assertEquals("Active clients", report.getReportName());
        assertEquals("Active clients", report.getDescription());
        assertTrue(report.isUseReport());
        assertEquals("select 2", report.getReportSql());
        verify(reportRepository).saveAndFlush(report);
    }

    @Test
    void updateReportStoresAnEmptyValueAsNull() {
        final Report report = existingReport();

        final CommandProcessingResult result = service.updateReport(REPORT_ID, command("""
                {"description": ""}
                """));

        // the change set reports the value as sent, while the report stores an empty value as null
        assertEquals(Map.of("description", ""), result.getChanges());
        assertNull(report.getDescription());
        verify(reportRepository).saveAndFlush(report);
    }

    @Test
    void updateReportWithoutChangesDoesNotSave() {
        existingReport();

        final CommandProcessingResult result = service.updateReport(REPORT_ID, command("""
                {"reportName": "Client list", "reportType": "Table", "useReport": false}
                """));

        // CommandProcessingResult reports an empty change set as null
        assertNull(result.getChanges());
        verify(reportRepository, never()).saveAndFlush(any(Report.class));
    }

    @Test
    void updateReportValidatesTheUpdatedReport() {
        existingReport();

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.updateReport(REPORT_ID, command("""
                        {"reportSql": ""}
                        """)));

        assertEquals("reportSql", exception.getErrors().get(0).getParameterName());
        verify(reportRepository, never()).saveAndFlush(any(Report.class));
    }

    @Test
    void updateOfACoreReportRejectsAChangeOtherThanUseReport() {
        final Report report = existingReport();
        ReflectionTestUtils.setField(report, "coreReport", true);

        final PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class,
                () -> service.updateReport(REPORT_ID, command("""
                        {"description": "Active clients"}
                        """)));

        assertEquals("error.msg.only.use.report.can.be.updated.for.core.report", exception.getGlobalisationMessageCode());
        verify(reportRepository, never()).saveAndFlush(any(Report.class));
    }

    @Test
    void updateOfACoreReportAllowsChangingUseReport() {
        final Report report = existingReport();
        ReflectionTestUtils.setField(report, "coreReport", true);

        final CommandProcessingResult result = service.updateReport(REPORT_ID, command("""
                {"useReport": true}
                """));

        assertEquals(Map.of("useReport", true), result.getChanges());
        verify(reportRepository).saveAndFlush(report);
    }

    /**
     * Pins an existing behaviour: report parameters are added to the change set before the core report check, and only
     * removed again later when they turn out to be unchanged, so even unchanged parameters are rejected for a core
     * report.
     */
    @Test
    void updateOfACoreReportRejectsEvenUnchangedParameters() {
        final Report report = existingReport();
        ReflectionTestUtils.setField(report, "coreReport", true);

        final PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class,
                () -> service.updateReport(REPORT_ID, command("""
                        {"reportParameters": []}
                        """)));

        assertEquals("error.msg.only.use.report.can.be.updated.for.core.report", exception.getGlobalisationMessageCode());
        verify(reportRepository, never()).saveAndFlush(any(Report.class));
    }

    @Test
    void updateReportWithANewParameterReportsAndAppliesIt() {
        final Report report = existingReport();

        final CommandProcessingResult result = service.updateReport(REPORT_ID, command("""
                {"reportParameters": [{"parameterId": 1, "reportParameterName": "officeId"}]}
                """));

        assertEquals(List.of("reportParameters"), List.copyOf(result.getChanges().keySet()));
        assertEquals(1, report.getReportParameterUsages().size());
        verify(reportRepository).saveAndFlush(report);
    }

    @Test
    void updateReportWithUnchangedParametersDropsThemFromTheChanges() {
        existingReport();

        final CommandProcessingResult result = service.updateReport(REPORT_ID, command("""
                {"reportParameters": []}
                """));

        assertNull(result.getChanges());
        verify(reportRepository, never()).saveAndFlush(any(Report.class));
    }

    private Report existingReport() {
        final Report report = new Report("Client list", "Table", null, "Client", "All clients", false, "select 1", List.of("Table"));
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
        return report;
    }

    private Report savedReport() {
        final ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    private JsonCommand command(final String json) {
        return JsonCommand.from(json, fromJsonHelper.parse(json), fromJsonHelper, null, REPORT_ID, null, null, null, null, null, null, null,
                null, null, null, null, null);
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
