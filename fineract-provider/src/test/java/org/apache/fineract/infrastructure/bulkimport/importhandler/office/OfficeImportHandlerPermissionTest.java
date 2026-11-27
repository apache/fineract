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
package org.apache.fineract.infrastructure.bulkimport.importhandler.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.command.OfficeCreateCommand;
import org.apache.fineract.organisation.office.data.OfficeCreateResponse;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Permission;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

class OfficeImportHandlerPermissionTest {

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 11, 28))));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void userWithoutPermissionFailsAllRowsAndCreatesNothing() throws Exception {
        CommandDispatcher dispatcher = mock(CommandDispatcher.class);
        PlatformSecurityContext context = mock(PlatformSecurityContext.class);
        AppUser user = appUserWith(new Permission("organisation", "OFFICE", "READ"));
        when(context.authenticatedUser()).thenReturn(user);

        OfficeImportHandler handler = new OfficeImportHandler(dispatcher, context);

        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            sheet.createRow(0).createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            addOffice(sheet, 1);
            addOffice(sheet, 2);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isZero();
            assertThat(count.getErrorCount()).isEqualTo(2);
            assertThat(status(sheet, 1)).isEqualTo("User has no authority to create offices");
            assertThat(status(sheet, 2)).isEqualTo("User has no authority to create offices");
            verify(context, times(1)).authenticatedUser();
            verify(dispatcher, never()).dispatch(any(OfficeCreateCommand.class));
        }
    }

    @Test
    void userWithoutPermissionRejectsInvalidRowsWithAuthorityErrorBeforeParsing() throws Exception {
        CommandDispatcher dispatcher = mock(CommandDispatcher.class);
        PlatformSecurityContext context = mock(PlatformSecurityContext.class);
        AppUser user = appUserWith();
        when(context.authenticatedUser()).thenReturn(user);

        OfficeImportHandler handler = new OfficeImportHandler(dispatcher, context);

        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            sheet.createRow(0).createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            Row badRow = addOffice(sheet, 1);
            badRow.createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(-3);
            badRow.createCell(OfficeConstants.OPENED_ON_COL).setCellValue("not a date");
            Row alreadyImported = addOffice(sheet, 2);
            alreadyImported.createCell(OfficeConstants.STATUS_COL).setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isZero();
            assertThat(count.getErrorCount()).isEqualTo(1);
            assertThat(status(sheet, 1)).isEqualTo("User has no authority to create offices");
            assertThat(status(sheet, 2)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(context, times(1)).authenticatedUser();
            verify(dispatcher, never()).dispatch(any(OfficeCreateCommand.class));
        }
    }

    @Test
    void userWithPermissionImportsAllRows() throws Exception {
        CommandDispatcher dispatcher = mock(CommandDispatcher.class);
        when(dispatcher.dispatch(any(OfficeCreateCommand.class))).thenReturn(OfficeCreateResponse::new);
        PlatformSecurityContext context = mock(PlatformSecurityContext.class);
        AppUser user = appUserWith(new Permission("organisation", "OFFICE", "CREATE"));
        when(context.authenticatedUser()).thenReturn(user);

        OfficeImportHandler handler = new OfficeImportHandler(dispatcher, context);

        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            sheet.createRow(0).createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            addOffice(sheet, 1);
            addOffice(sheet, 2);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isZero();
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(context, times(1)).authenticatedUser();
            verify(dispatcher, times(2)).dispatch(any(OfficeCreateCommand.class));
        }
    }

    @Test
    void superUserWithAllFunctionsImportsAllRows() throws Exception {
        CommandDispatcher dispatcher = mock(CommandDispatcher.class);
        when(dispatcher.dispatch(any(OfficeCreateCommand.class))).thenReturn(OfficeCreateResponse::new);
        PlatformSecurityContext context = mock(PlatformSecurityContext.class);
        AppUser user = appUserWith(new Permission("special", "FUNCTIONS", "ALL"));
        when(context.authenticatedUser()).thenReturn(user);

        OfficeImportHandler handler = new OfficeImportHandler(dispatcher, context);

        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            sheet.createRow(0).createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            addOffice(sheet, 1);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(1);
            assertThat(count.getErrorCount()).isZero();
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(dispatcher, times(1)).dispatch(any(OfficeCreateCommand.class));
        }
    }

    private static AppUser appUserWith(Permission... permissions) {
        Role role = new Role("test-role", "test role");
        for (Permission permission : permissions) {
            role.updatePermission(permission, true);
        }
        User springUser = new User("importer", "pass", List.of());
        return new AppUser(null, springUser, new HashSet<>(Set.of(role)), "importer@example.org", "Import", "User", null, false, false);
    }

    private static Row addOffice(Sheet sheet, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Branch " + rowIndex);
        row.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL).setCellValue("Head Office");
        row.createCell(OfficeConstants.OPENED_ON_COL).setCellValue(LocalDate.of(2024, 1, 15));
        return row;
    }

    private static String status(Sheet sheet, int rowIndex) {
        return sheet.getRow(rowIndex).getCell(OfficeConstants.STATUS_COL).getStringCellValue();
    }
}
