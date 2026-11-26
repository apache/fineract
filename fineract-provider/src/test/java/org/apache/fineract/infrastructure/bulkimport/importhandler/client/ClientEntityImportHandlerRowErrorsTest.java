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
import org.apache.fineract.infrastructure.bulkimport.constants.ClientEntityConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class ClientEntityImportHandlerRowErrorsTest {

    private final PortfolioCommandSourceWritePlatformService commandService = mock(PortfolioCommandSourceWritePlatformService.class);
    private final ExternalIdFactory externalIdFactory = mock(ExternalIdFactory.class);
    private final ClientEntityImportHandler handler = new ClientEntityImportHandler(commandService, externalIdFactory);

    @Test
    void aRowThatCannotBeReadFailsOnItsOwnAndTheOtherRowsStillImport() throws IOException {
        when(externalIdFactory.create(any())).thenReturn(ExternalId.empty());
        try (Workbook workbook = new HSSFWorkbook()) {
            Row office = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME).createRow(0);
            office.createCell(0).setCellValue(1);
            office.createCell(1).setCellValue("Head Office");
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
            sheet.createRow(0).createCell(ClientEntityConstants.NAME_COL).setCellValue("Name*");
            addClient(sheet, 1, "Head Office", null);
            addClient(sheet, 2, "Unknown Office", null);
            addClient(sheet, 3, "Head Office", "70-123-456");
            addClient(sheet, 4, "Head Office", "+96170123456");

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isEqualTo(2);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo("Office 'Unknown Office' not found");
            assertThat(status(sheet, 3)).isEqualTo("Mobile number must contain digits only");
            assertThat(status(sheet, 4)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(commandService, times(2)).logCommandSource(any());
        }
    }

    @Test
    void aClientTypeTypedWithoutItsIdFailsOnlyItsRow() throws IOException {
        when(externalIdFactory.create(any())).thenReturn(ExternalId.empty());
        try (Workbook workbook = new HSSFWorkbook()) {
            Row office = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME).createRow(0);
            office.createCell(0).setCellValue(1);
            office.createCell(1).setCellValue("Head Office");
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
            Row header = sheet.createRow(0);
            header.createCell(ClientEntityConstants.NAME_COL).setCellValue("Name*");
            header.createCell(ClientEntityConstants.CLIENT_TYPE_COL).setCellValue("Client Type");
            addClient(sheet, 1, "Head Office", null);
            addClient(sheet, 2, "Head Office", null);
            sheet.getRow(2).createCell(ClientEntityConstants.CLIENT_TYPE_COL).setCellValue("Retail");
            addClient(sheet, 3, "Head Office", null);
            sheet.getRow(3).createCell(ClientEntityConstants.CLIENT_TYPE_COL).setCellValue("Retail-12");

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isEqualTo(1);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo("Client Type 'Retail' not found");
            assertThat(status(sheet, 3)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(commandService, times(2)).logCommandSource(any());
        }
    }

    private static void addClient(final Sheet sheet, final int rowIndex, final String office, final String mobileNo) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(ClientEntityConstants.NAME_COL).setCellValue("Entity " + rowIndex);
        row.createCell(ClientEntityConstants.OFFICE_NAME_COL).setCellValue(office);
        row.createCell(ClientEntityConstants.ACTIVE_COL).setCellValue(true);
        row.createCell(ClientEntityConstants.SUBMITTED_ON_COL).setCellValue(LocalDate.of(2024, 1, 15));
        row.createCell(ClientEntityConstants.ACTIVATION_DATE_COL).setCellValue(LocalDate.of(2024, 1, 15));
        if (mobileNo != null) {
            row.createCell(ClientEntityConstants.MOBILE_NO_COL).setCellValue(mobileNo);
        }
    }

    private static String status(final Sheet sheet, final int rowIndex) {
        return sheet.getRow(rowIndex).getCell(ClientEntityConstants.STATUS_COL).getStringCellValue();
    }
}
