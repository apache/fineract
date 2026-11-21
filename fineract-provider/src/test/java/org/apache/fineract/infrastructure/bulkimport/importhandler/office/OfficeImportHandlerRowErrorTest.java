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
import java.time.LocalDateTime;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class OfficeImportHandlerRowErrorTest {

    @Test
    void invalidParentOfficeIdFailsOnlyItsOwnRow() throws IOException {
        PortfolioCommandSourceWritePlatformService commands = mock(PortfolioCommandSourceWritePlatformService.class);
        OfficeImportHandler handler = new OfficeImportHandler(commands);

        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            sheet.createRow(0).createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            addOffice(sheet, 1, "Valid").createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(1);
            addOffice(sheet, 2, "Negative").createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(-3);
            addOffice(sheet, 3, "Text").createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue("abc");
            addOffice(sheet, 4, "Decimal").createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(1.5);
            addOffice(sheet, 5, "Zero").createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue("0");

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(1);
            assertThat(count.getErrorCount()).isEqualTo(4);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            for (int rowIndex = 2; rowIndex <= 5; rowIndex++) {
                assertThat(status(sheet, rowIndex)).isEqualTo("Parent OfficeId must be a positive number");
            }
            verify(commands, times(1)).logCommandSource(any());
        }
    }

    private static Row addOffice(Sheet sheet, int rowIndex, String name) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue(name);
        row.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL).setCellValue("Head Office");
        row.createCell(OfficeConstants.OPENED_ON_COL).setCellValue(LocalDateTime.of(2024, 1, 15, 0, 0));
        return row;
    }

    private static String status(Sheet sheet, int rowIndex) {
        return sheet.getRow(rowIndex).getCell(OfficeConstants.STATUS_COL).getStringCellValue();
    }
}
