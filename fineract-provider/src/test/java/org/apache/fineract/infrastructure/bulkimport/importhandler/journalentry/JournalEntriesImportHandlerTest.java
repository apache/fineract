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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.JournalEntryConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JournalEntriesImportHandlerTest {

    private JournalEntriesImportHandler handler;
    private PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;

    @BeforeEach
    void setUp() {
        commandSourceWritePlatformService = mock(PortfolioCommandSourceWritePlatformService.class);
        handler = new JournalEntriesImportHandler(commandSourceWritePlatformService);
    }

    @Test
    void skipsAllRowsOfFailedTransactionWithoutPostingPreviousTransactionTwice() {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet journalEntries = workbook.createSheet(TemplatePopulateImportConstants.JOURNAL_ENTRY_SHEET_NAME);
            journalEntries.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
            workbook.createSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            workbook.createSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
            workbook.createSheet(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME);

            createJournalEntryRow(journalEntries, 1, "transaction-1", "2026-11-07");
            createJournalEntryRow(journalEntries, 2, "transaction-2", "not-a-date");
            createJournalEntryRow(journalEntries, 3, "transaction-2", "2026-11-07");

            Count count = handler.process(workbook, "en", "yyyy-MM-dd");

            assertThat(count.getSuccessCount()).isEqualTo(1);
            assertThat(count.getErrorCount()).isEqualTo(1);
            assertThat(journalEntries.getRow(2).getCell(JournalEntryConstants.STATUS_COL).getStringCellValue()).isNotBlank();
            assertThat(journalEntries.getRow(3).getCell(JournalEntryConstants.STATUS_COL).getStringCellValue()).isNotBlank();
            verify(commandSourceWritePlatformService, times(1)).logCommandSource(org.mockito.ArgumentMatchers.any());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void createJournalEntryRow(Sheet sheet, int rowIndex, String transactionId, String transactionDate) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(JournalEntryConstants.TRANSACION_ON_DATE_COL).setCellValue(transactionDate);
        row.createCell(JournalEntryConstants.TRANSACTION_ID_COL).setCellValue(transactionId);
    }
}
