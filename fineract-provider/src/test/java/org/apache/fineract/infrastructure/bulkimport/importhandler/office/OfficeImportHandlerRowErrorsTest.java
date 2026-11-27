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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.command.OfficeCreateCommand;
import org.apache.fineract.organisation.office.data.OfficeCreateResponse;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OfficeImportHandlerRowErrorsTest {

    private final CommandDispatcher dispatcher = mock(CommandDispatcher.class);
    private final PlatformSecurityContext context = mock(PlatformSecurityContext.class);
    private final AppUser user = mock(AppUser.class);
    private OfficeImportHandler handler;

    @BeforeEach
    void setUp() {
        when(dispatcher.dispatch(any(OfficeCreateCommand.class))).thenReturn(OfficeCreateResponse::new);
        when(context.authenticatedUser()).thenReturn(user);
        when(user.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "CREATE_OFFICE")).thenReturn(false);
        handler = new OfficeImportHandler(dispatcher, context);
    }

    @Test
    void aParentOfficeIdThatIsNotANumberFailsOnlyItsRow() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            Row header = sheet.createRow(0);
            header.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            header.createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue("Parent OfficeId*");
            addOffice(sheet, 1).createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(1);
            addOffice(sheet, 2).createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue("one");
            addOffice(sheet, 3).createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(1);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isEqualTo(1);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo("Parent OfficeId must be a positive number");
            assertThat(status(sheet, 3)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(dispatcher, times(2)).dispatch(any(OfficeCreateCommand.class));
        }
    }

    @Test
    void anOpenedOnThatIsNotADateFailsOnlyItsRow() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            Row header = sheet.createRow(0);
            header.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            header.createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue("Parent OfficeId*");
            addOffice(sheet, 1).createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(1);
            Row unreadable = addOffice(sheet, 2);
            unreadable.createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(1);
            unreadable.createCell(OfficeConstants.OPENED_ON_COL).setCellValue("not a date");
            addOffice(sheet, 3).createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(1);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isEqualTo(1);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).startsWith("Unable to parse date 'not a date'");
            assertThat(status(sheet, 3)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(dispatcher, times(2)).dispatch(any(OfficeCreateCommand.class));
        }
    }

    private static Row addOffice(final Sheet sheet, final int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Branch " + rowIndex);
        row.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL).setCellValue("Head Office");
        row.createCell(OfficeConstants.OPENED_ON_COL).setCellValue(LocalDate.of(2024, 1, 15));
        return row;
    }

    private static String status(final Sheet sheet, final int rowIndex) {
        return sheet.getRow(rowIndex).getCell(OfficeConstants.STATUS_COL).getStringCellValue();
    }
}
