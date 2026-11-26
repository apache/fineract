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
import java.time.LocalDate;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class SavingsTransactionImportHandlerRowErrorsTest {

    private final PortfolioCommandSourceWritePlatformService commandService = mock(PortfolioCommandSourceWritePlatformService.class);
    private final SavingsTransactionImportHandler handler = new SavingsTransactionImportHandler(commandService);

    @Test
    void anAmountThatIsNotANumberFailsOnlyItsRow() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Row paymentType = workbook.createSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME).createRow(0);
            paymentType.createCell(0).setCellValue(1);
            paymentType.createCell(1).setCellValue("Cash");
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME);
            Row header = sheet.createRow(0);
            header.createCell(TransactionConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            header.createCell(TransactionConstants.AMOUNT_COL).setCellValue("Amount*");
            addTransaction(sheet, 1).createCell(TransactionConstants.AMOUNT_COL).setCellValue(100);
            addTransaction(sheet, 2).createCell(TransactionConstants.AMOUNT_COL).setCellValue("ten");
            addTransaction(sheet, 3).createCell(TransactionConstants.AMOUNT_COL).setCellValue(50);
            sheet.createRow(TransactionConstants.STATUS_COL);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isEqualTo(1);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo("Amount must be a number");
            assertThat(status(sheet, 3)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(commandService, times(2)).logCommandSource(any());
        }
    }

    private static Row addTransaction(final Sheet sheet, final int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(TransactionConstants.OFFICE_NAME_COL).setCellValue("Head Office");
        row.createCell(TransactionConstants.CLIENT_NAME_COL).setCellValue("Client(1)");
        row.createCell(TransactionConstants.SAVINGS_ACCOUNT_NO_COL).setCellValue("000000001");
        row.createCell(TransactionConstants.TRANSACTION_TYPE_COL).setCellValue("Deposit");
        row.createCell(TransactionConstants.TRANSACTION_DATE_COL).setCellValue(LocalDate.of(2024, 1, 15));
        row.createCell(TransactionConstants.PAYMENT_TYPE_COL).setCellValue("Cash");
        return row;
    }

    private static String status(final Sheet sheet, final int rowIndex) {
        return sheet.getRow(rowIndex).getCell(TransactionConstants.STATUS_COL).getStringCellValue();
    }
}
