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
package org.apache.fineract.infrastructure.bulkimport.importhandler.journalentry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDate;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.JournalEntryConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class JournalEntriesImportHandlerTest {

    private final PortfolioCommandSourceWritePlatformService commandService = mock(PortfolioCommandSourceWritePlatformService.class);
    private final JournalEntriesImportHandler handler = new JournalEntriesImportHandler(commandService);

    @Test
    void rowsWithAnEmptyMandatoryColumnFailOnTheirOwn() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            addLookupSheets(workbook);
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.JOURNAL_ENTRY_SHEET_NAME);
            sheet.createRow(0).createCell(0).setCellValue("Office Name*");
            addEntry(sheet, 1, "T1", "Cash");
            addEntry(sheet, 2, null, "Cash");
            addEntry(sheet, 3, "T2", "Cash");
            addEntry(sheet, 4, "T3", null);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(3);
            assertThat(count.getErrorCount()).isEqualTo(1);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo("Transaction Id is mandatory");
            assertThat(status(sheet, 3)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 4)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(commandService, times(3)).logCommandSource(any());
        }
    }

    private static void addLookupSheets(final Workbook workbook) {
        Row office = workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME).createRow(0);
        office.createCell(0).setCellValue(1);
        office.createCell(1).setCellValue("Head Office");
        Sheet extras = workbook.createSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
        Row paymentType = extras.createRow(0);
        paymentType.createCell(0).setCellValue(1);
        paymentType.createCell(1).setCellValue("Cash");
        Row currency = extras.createRow(1);
        currency.createCell(0).setCellValue("USD");
        currency.createCell(1).setCellValue("US Dollar");
        Sheet glAccounts = workbook.createSheet(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME);
        Row assets = glAccounts.createRow(0);
        assets.createCell(0).setCellValue(1);
        assets.createCell(1).setCellValue("Cash Account");
        Row income = glAccounts.createRow(1);
        income.createCell(0).setCellValue(2);
        income.createCell(1).setCellValue("Income Account");
    }

    private static void addEntry(final Sheet sheet, final int rowIndex, final String transactionId, final String paymentType) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(JournalEntryConstants.OFFICE_NAME_COL).setCellValue("Head Office");
        row.createCell(JournalEntryConstants.TRANSACION_ON_DATE_COL).setCellValue(LocalDate.of(2024, 1, 15));
        row.createCell(JournalEntryConstants.CURRENCY_NAME_COL).setCellValue("US Dollar");
        if (paymentType != null) {
            row.createCell(JournalEntryConstants.PAYMENT_TYPE_ID_COL).setCellValue(paymentType);
        }
        if (transactionId != null) {
            row.createCell(JournalEntryConstants.TRANSACTION_ID_COL).setCellValue(transactionId);
        }
        row.createCell(JournalEntryConstants.GL_ACCOUNT_ID_CREDIT_COL).setCellValue("Income Account");
        row.createCell(JournalEntryConstants.AMOUNT_CREDIT_COL).setCellValue(10);
        row.createCell(JournalEntryConstants.GL_ACCOUNT_ID_DEBIT_COL).setCellValue("Cash Account");
        row.createCell(JournalEntryConstants.AMOUNT_DEBIT_COL).setCellValue(10);
    }

    private static String status(final Sheet sheet, final int rowIndex) {
        return sheet.getRow(rowIndex).getCell(JournalEntryConstants.STATUS_COL).getStringCellValue();
    }
}
