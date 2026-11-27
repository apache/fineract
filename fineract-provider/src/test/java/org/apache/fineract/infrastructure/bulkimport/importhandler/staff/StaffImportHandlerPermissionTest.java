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
package org.apache.fineract.infrastructure.bulkimport.importhandler.staff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.bulkimport.constants.StaffConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.data.StaffCreateRequest;
import org.apache.fineract.organisation.staff.data.StaffCreateResponse;
import org.apache.fineract.organisation.staff.mapper.StaffDataMapper;
import org.apache.fineract.organisation.staff.mapper.StaffDateMapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class StaffImportHandlerPermissionTest {

    private static final String HEAD_OFFICE = "Head Office";

    @Test
    void userWithoutPermissionFailsAllRowsAndCreatesNothing() throws Exception {
        CommandDispatcher dispatcher = mock(CommandDispatcher.class);
        PlatformSecurityContext context = mock(PlatformSecurityContext.class);
        AppUser user = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(user);
        when(user.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "CREATE_STAFF")).thenReturn(true);

        StaffImportHandler handler = new StaffImportHandler(dispatcher, mappedStaffDataMapper(), mappedStaffDateMapper(), context);

        try (Workbook workbook = staffWorkbook()) {
            Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isZero();
            assertThat(count.getErrorCount()).isEqualTo(2);
            assertThat(status(sheet, 1)).contains("no authority");
            assertThat(status(sheet, 2)).contains("no authority");
            verify(dispatcher, never()).dispatch(any());
        }
    }

    @Test
    void userWithPermissionImportsAllRows() throws Exception {
        CommandDispatcher dispatcher = mock(CommandDispatcher.class);
        when(dispatcher.dispatch(any())).thenReturn(StaffCreateResponse::new);
        PlatformSecurityContext context = mock(PlatformSecurityContext.class);
        AppUser user = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(user);
        when(user.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "CREATE_STAFF")).thenReturn(false);

        StaffImportHandler handler = new StaffImportHandler(dispatcher, mappedStaffDataMapper(), mappedStaffDateMapper(), context);

        try (Workbook workbook = staffWorkbook()) {
            Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isZero();
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(dispatcher, times(2)).dispatch(any());
        }
    }

    private static StaffDataMapper mappedStaffDataMapper() {
        StaffDataMapper staffDataMapper = mock(StaffDataMapper.class);
        when(staffDataMapper.map(any())).thenReturn(new StaffCreateRequest());
        return staffDataMapper;
    }

    private static StaffDateMapper mappedStaffDateMapper() {
        StaffDateMapper staffDateMapper = mock(StaffDateMapper.class);
        when(staffDateMapper.map(any(LocalDate.class), anyString())).thenReturn("15 January 2024");
        return staffDateMapper;
    }

    private static Workbook staffWorkbook() {
        Workbook workbook = new HSSFWorkbook();
        // the handler resolves the office name of every row against the office sheet of the same workbook
        Sheet officeSheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row officeHeader = officeSheet.createRow(0);
        officeHeader.createCell(0).setCellValue("Id");
        officeHeader.createCell(1).setCellValue("Office Name");
        Row office = officeSheet.createRow(1);
        office.createCell(0).setCellValue(1);
        office.createCell(1).setCellValue(HEAD_OFFICE);

        Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME);
        sheet.createRow(0).createCell(StaffConstants.STATUS_COL).setCellValue("Status");
        addStaff(sheet, 1);
        addStaff(sheet, 2);
        return workbook;
    }

    private static void addStaff(Sheet sheet, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(StaffConstants.OFFICE_NAME_COL).setCellValue(HEAD_OFFICE);
        row.createCell(StaffConstants.FIRST_NAME_COL).setCellValue("First" + rowIndex);
        row.createCell(StaffConstants.LAST_NAME_COL).setCellValue("Last" + rowIndex);
        row.createCell(StaffConstants.JOINED_ON_COL).setCellValue(LocalDate.of(2024, 1, 15));
    }

    private static String status(Sheet sheet, int rowIndex) {
        return sheet.getRow(rowIndex).getCell(StaffConstants.STATUS_COL).getStringCellValue();
    }
}
