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
package org.apache.fineract.infrastructure.bulkimport.importhandler.savings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class SavingsTransactionImportHandlerRowErrorTest {

    @Test
    void blankOrUnknownPaymentTypeFailsOnlyItsOwnRow() throws IOException {
        PortfolioCommandSourceWritePlatformService commands = mock(PortfolioCommandSourceWritePlatformService.class);
        SavingsTransactionImportHandler handler = new SavingsTransactionImportHandler(commands);

        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME);
            for (int rowIndex = 0; rowIndex <= TransactionConstants.STATUS_COL; rowIndex++) {
                sheet.createRow(rowIndex);
            }
            sheet.getRow(0).createCell(TransactionConstants.AMOUNT_COL).setCellValue("Amount*");
            addDeposit(sheet.getRow(1), "Cash");
            addDeposit(sheet.getRow(2), null);
            addDeposit(sheet.getRow(3), "Bitcoin");

            Sheet extras = workbook.createSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
            Row header = extras.createRow(0);
            header.createCell(2).setCellValue("Payment Type ID");
            header.createCell(3).setCellValue("Payment Type Name");
            Row cash = extras.createRow(1);
            cash.createCell(2).setCellValue(1);
            cash.createCell(3).setCellValue("Cash");

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(1);
            assertThat(count.getErrorCount()).isEqualTo(2);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo("Payment Type is mandatory");
            assertThat(status(sheet, 3)).isEqualTo("Payment Type 'Bitcoin' not found");
            verify(commands, times(1)).logCommandSource(any());
        }
    }

    private static void addDeposit(Row row, String paymentType) {
        row.createCell(TransactionConstants.SAVINGS_ACCOUNT_NO_COL).setCellValue(1);
        row.createCell(TransactionConstants.TRANSACTION_TYPE_COL).setCellValue("Deposit");
        row.createCell(TransactionConstants.AMOUNT_COL).setCellValue(100);
        row.createCell(TransactionConstants.TRANSACTION_DATE_COL).setCellValue(LocalDateTime.of(2024, 1, 15, 0, 0));
        if (paymentType != null) {
            row.createCell(TransactionConstants.PAYMENT_TYPE_COL).setCellValue(paymentType);
        }
    }

    private static String status(Sheet sheet, int rowIndex) {
        return sheet.getRow(rowIndex).getCell(TransactionConstants.STATUS_COL).getStringCellValue();
    }
}
