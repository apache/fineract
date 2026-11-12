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

import java.io.IOException;
import java.time.LocalDate;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class OfficeImportHandlerRowErrorsTest {

    private final PortfolioCommandSourceWritePlatformService commandService = mock(PortfolioCommandSourceWritePlatformService.class);
    private final OfficeImportHandler handler = new OfficeImportHandler(commandService);

    @Test
    void aRejectedParentOfficeFailsOnlyItsRow() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            sheet.createRow(0).createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            addOffice(sheet, 1, "Head Office", 1);
            addOffice(sheet, 2, "Head Office", 1.5);
            addOffice(sheet, 3, "x", 1);
            addOffice(sheet, 4, "Head Office", 1);
            Row lookup = sheet.getRow(1);
            lookup.createCell(OfficeConstants.LOOKUP_OFFICE_COL).setCellValue("Head Office");
            lookup.createCell(OfficeConstants.LOOKUP_OFFICE_ID_COL).setCellValue(1);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isEqualTo(2);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo("Parent OfficeId must be a whole number");
            assertThat(status(sheet, 3)).isEqualTo("Parent Office 'x' not found");
            assertThat(status(sheet, 4)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(commandService, times(2)).logCommandSource(any());
        }
    }

    private static void addOffice(final Sheet sheet, final int rowIndex, final String parentOffice, final double parentOfficeId) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Branch " + rowIndex);
        row.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL).setCellValue(parentOffice);
        row.createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(parentOfficeId);
        row.createCell(OfficeConstants.OPENED_ON_COL).setCellValue(LocalDate.of(2024, 1, 15));
    }

    private static String status(final Sheet sheet, final int rowIndex) {
        return sheet.getRow(rowIndex).getCell(OfficeConstants.STATUS_COL).getStringCellValue();
    }
}
