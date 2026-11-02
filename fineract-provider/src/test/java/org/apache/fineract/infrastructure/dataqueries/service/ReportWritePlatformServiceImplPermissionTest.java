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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportParameterRepository;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportParameterUsageRepository;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportRepository;
import org.apache.fineract.infrastructure.dataqueries.serialization.ReportCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.Permission;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportWritePlatformServiceImplPermissionTest {

    private static final Long REPORT_ID = 42L;

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
    private Report report;

    @BeforeEach
    void setUp() {
        when(reportingProcessServiceProvider.findAllReportingTypes()).thenReturn(List.of("Table"));
        report = new Report("Old name", "Table", null, "Client", "desc", true, "select 1", List.of("Table"));
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
        service = new ReportWritePlatformServiceImpl(context, fromApiJsonDeserializer, reportRepository, reportParameterRepository,
                reportParameterUsageRepository, permissionRepository, roleRepository, reportingProcessServiceProvider);
    }

    @Test
    void renamingReportRenamesItsReadPermission() {
        final Permission permission = new Permission("report", "Old name", "READ");
        when(permissionRepository.findOneByCode("READ_Old name")).thenReturn(permission);

        service.updateReport(REPORT_ID, command("{\"reportName\": \"New name\"}"));

        assertEquals("New name", report.getReportName());
        assertEquals("READ_New name", permission.getCode());
        verify(permissionRepository).save(permission);
    }

    @Test
    void updatingReportWithoutRenameLeavesPermissionAlone() {
        service.updateReport(REPORT_ID, command("{\"description\": \"other\"}"));

        verify(permissionRepository, never()).findOneByCode(anyString());
    }

    @Test
    void deletingReportWithoutMatchingPermissionStillDeletesReport() {
        when(permissionRepository.findOneByCode("READ_Old name")).thenReturn(null);

        service.deleteReport(REPORT_ID);

        verify(reportRepository).delete(report);
    }

    @Test
    void deletingReportUnassignsItsPermissionFromRolesFirst() {
        final Permission permission = new Permission("report", "Old name", "READ");
        final Role role = new Role("Report reader", "reads reports");
        role.updatePermission(permission, true);
        when(permissionRepository.findOneByCode("READ_Old name")).thenReturn(permission);
        when(roleRepository.findByPermissionId(permission.getId())).thenReturn(List.of(role));

        service.deleteReport(REPORT_ID);

        assertFalse(role.hasPermissionTo("READ_Old name"));
        verify(roleRepository).saveAll(List.of(role));
        verify(permissionRepository).delete(permission);
        verify(reportRepository).delete(report);
        assertTrue(role.getPermissions().isEmpty());
    }

    private JsonCommand command(final String json) {
        final JsonElement parsed = fromJsonHelper.parse(json);
        return JsonCommand.from(json, parsed, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null);
    }
}
