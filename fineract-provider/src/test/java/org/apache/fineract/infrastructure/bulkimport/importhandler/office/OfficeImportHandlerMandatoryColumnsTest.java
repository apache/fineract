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

class OfficeImportHandlerMandatoryColumnsTest {

    private final PortfolioCommandSourceWritePlatformService commandService = mock(PortfolioCommandSourceWritePlatformService.class);
    private final OfficeImportHandler handler = new OfficeImportHandler(commandService);

    @Test
    void rowsWithAnEmptyMandatoryColumnFailOnTheirOwn() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            sheet.createRow(0).createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            addOffice(sheet, 1, "Branch 1", "Head Office", 1L, "EXT-1");
            addOffice(sheet, 2, "Branch 2", "Head Office", 1L, null);
            addOffice(sheet, 3, "Branch 3", null, null, "EXT-3");
            addOffice(sheet, 4, null, "Head Office", 1L, "EXT-4");
            addOffice(sheet, 5, "Branch 5", "Unknown Office", null, "EXT-5");
            addOffice(sheet, 6, "Branch 6", null, 1L, "EXT-6");

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(3);
            assertThat(count.getErrorCount()).isEqualTo(3);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 3)).isEqualTo("Parent Office is mandatory");
            assertThat(status(sheet, 4)).isEqualTo("Office Name is mandatory");
            assertThat(status(sheet, 5)).isEqualTo("Parent Office `Unknown Office` not found");
            assertThat(status(sheet, 6)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(commandService, times(3)).logCommandSource(any());
        }
    }

    private static void addOffice(final Sheet sheet, final int rowIndex, final String name, final String parentOffice,
            final Long parentOfficeId, final String externalId) {
        Row row = sheet.createRow(rowIndex);
        if (name != null) {
            row.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue(name);
        }
        if (parentOffice != null) {
            row.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL).setCellValue(parentOffice);
        }
        if (parentOfficeId != null) {
            row.createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(parentOfficeId);
        }
        row.createCell(OfficeConstants.OPENED_ON_COL).setCellValue(LocalDate.of(2024, 1, 15));
        if (externalId != null) {
            row.createCell(OfficeConstants.EXTERNAL_ID_COL).setCellValue(externalId);
        }
    }

    private static String status(final Sheet sheet, final int rowIndex) {
        return sheet.getRow(rowIndex).getCell(OfficeConstants.STATUS_COL).getStringCellValue();
    }
}
