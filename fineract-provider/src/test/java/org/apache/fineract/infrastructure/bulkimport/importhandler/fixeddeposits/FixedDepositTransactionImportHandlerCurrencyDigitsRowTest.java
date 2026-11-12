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
package org.apache.fineract.infrastructure.bulkimport.importhandler.fixeddeposits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class FixedDepositTransactionImportHandlerCurrencyDigitsRowTest {

    private final PortfolioCommandSourceWritePlatformService commandService = mock(PortfolioCommandSourceWritePlatformService.class);
    private final SavingsAccountRepositoryWrapper savingsAccounts = mock(SavingsAccountRepositoryWrapper.class);
    private final FixedDepositTransactionImportHandler handler = new FixedDepositTransactionImportHandler(commandService, savingsAccounts);

    @Test
    void anAmountWithTooManyDecimalsFailsOnlyItsRow() throws IOException {
        when(savingsAccounts.findCurrencyDigitsById(1L)).thenReturn(2);
        try (Workbook workbook = new HSSFWorkbook()) {
            Row paymentType = workbook.createSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME).createRow(0);
            paymentType.createCell(0).setCellValue(1);
            paymentType.createCell(1).setCellValue("Cash");
            Sheet sheet = workbook.createSheet(TemplatePopulateImportConstants.FIXED_DEPOSIT_TRANSACTION_SHEET_NAME);
            sheet.createRow(0).createCell(TransactionConstants.OFFICE_NAME_COL).setCellValue("Office Name*");
            addTransaction(sheet, 1, 100.25);
            addTransaction(sheet, 2, 100.255);
            addTransaction(sheet, 3, 50);
            sheet.createRow(TransactionConstants.STATUS_COL);

            Count count = handler.process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(2);
            assertThat(count.getErrorCount()).isEqualTo(1);
            assertThat(status(sheet, 1)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(status(sheet, 2)).isEqualTo("Amount has more decimal places than the currency allows (2)");
            assertThat(status(sheet, 3)).isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            verify(commandService, times(2)).logCommandSource(any());
        }
    }

    private static void addTransaction(final Sheet sheet, final int rowIndex, final double amount) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(TransactionConstants.OFFICE_NAME_COL).setCellValue("Head Office");
        row.createCell(TransactionConstants.CLIENT_NAME_COL).setCellValue("Client(1)");
        row.createCell(TransactionConstants.SAVINGS_ACCOUNT_NO_COL).setCellValue(1);
        row.createCell(TransactionConstants.TRANSACTION_TYPE_COL).setCellValue("Deposit");
        row.createCell(TransactionConstants.AMOUNT_COL).setCellValue(amount);
        row.createCell(TransactionConstants.TRANSACTION_DATE_COL).setCellValue(LocalDate.of(2024, 1, 15));
        row.createCell(TransactionConstants.PAYMENT_TYPE_COL).setCellValue("Cash");
    }

    private static String status(final Sheet sheet, final int rowIndex) {
        return sheet.getRow(rowIndex).getCell(TransactionConstants.STATUS_COL).getStringCellValue();
    }
}
