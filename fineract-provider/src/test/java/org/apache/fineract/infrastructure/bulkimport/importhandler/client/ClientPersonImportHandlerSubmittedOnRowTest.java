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
package org.apache.fineract.infrastructure.bulkimport.importhandler.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.ClientPersonConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class ClientPersonImportHandlerSubmittedOnRowTest {

    private final PortfolioCommandSourceWritePlatformService commandService = mock(PortfolioCommandSourceWritePlatformService.class);
    private final ExternalIdFactory externalIdFactory = mock(ExternalIdFactory.class);
    private final ClientPersonImportHandler handler = new ClientPersonImportHandler(commandService, externalIdFactory);

    @Test
    void aLateSubmittedOnDateFailsOnlyItsRow() throws IOException {
        when(externalIdFactory.create(any())).thenReturn(ExternalId.empty());
        try (Workbook workbook = new HSSFWorkbook()) {
            Row office = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME).createRow(0);
            office.createCell(0).setCellValue(1);
            office.createCell(1).setCellValue("Head Office");
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME);
            sheet.createRow(0).createCell(ClientPersonConstants.FIRST_NAME_COL).setCellValue("First Name*");
            addClient(sheet, 1, LocalDate.of(2024, 1, 15));
            addClient(sheet, 2, LocalDate.of(2024, 2, 15));
            addClient(sheet, 3, LocalDate.of(2024, 1, 10));

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isEqualTo(1);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo("Submitted On date cannot be after the Activation date");
            assertThat(status(sheet, 3)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(commandService, times(2)).logCommandSource(any());
        }
    }

    private static void addClient(final Sheet sheet, final int rowIndex, final LocalDate submittedOn) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(ClientPersonConstants.FIRST_NAME_COL).setCellValue("First " + rowIndex);
        row.createCell(ClientPersonConstants.LAST_NAME_COL).setCellValue("Last " + rowIndex);
        row.createCell(ClientPersonConstants.OFFICE_NAME_COL).setCellValue("Head Office");
        row.createCell(ClientPersonConstants.ACTIVE_COL).setCellValue(true);
        row.createCell(ClientPersonConstants.SUBMITTED_ON_COL).setCellValue(submittedOn);
        row.createCell(ClientPersonConstants.ACTIVATION_DATE_COL).setCellValue(LocalDate.of(2024, 1, 15));
    }

    private static String status(final Sheet sheet, final int rowIndex) {
        return sheet.getRow(rowIndex).getCell(ClientPersonConstants.STATUS_COL).getStringCellValue();
    }
}
