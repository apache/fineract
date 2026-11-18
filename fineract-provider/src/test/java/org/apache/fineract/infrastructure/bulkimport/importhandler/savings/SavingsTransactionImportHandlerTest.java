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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.fixeddeposits.FixedDepositTransactionImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.recurringdeposit.RecurringDepositTransactionImportHandler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingsTransactionImportHandlerTest {

    @Mock
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    @ParameterizedTest(name = "{0}")
    @MethodSource("transactionImportHandlers")
    void rowWhoseAccountBelongsToAnotherClientFailsOnlyThatRow(final String entity, final String sheetName,
            final HandlerFactory handlerFactory) throws IOException {
        when(savingsAccountRepositoryWrapper.findClientIdById(10L)).thenReturn(1L);
        when(savingsAccountRepositoryWrapper.findClientIdById(20L)).thenReturn(2L);
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = transactionSheet(workbook, sheetName);
            row(sheet, 1, "Client A(1)", "000000010");
            row(sheet, 2, "Client A(1)", "000000020");

            Count count = handlerFactory.create(commandsSourceWritePlatformService, savingsAccountRepositoryWrapper).process(workbook, "en",
                    "dd MMMM yyyy");

            assertThat(count.getSuccessCount()).isEqualTo(1);
            assertThat(count.getErrorCount()).isEqualTo(1);
            ArgumentCaptor<CommandWrapper> command = ArgumentCaptor.forClass(CommandWrapper.class);
            verify(commandsSourceWritePlatformService).logCommandSource(command.capture());
            assertThat(command.getValue().getSavingsId()).isEqualTo(10L);
            assertThat(sheet.getRow(1).getCell(TransactionConstants.STATUS_COL).getStringCellValue())
                    .isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
            assertThat(sheet.getRow(2).getCell(TransactionConstants.STATUS_COL).getStringCellValue())
                    .isEqualTo("Account 000000020 does not belong to client Client A(1)");
        }
    }

    static Stream<Arguments> transactionImportHandlers() {
        return Stream.of(
                Arguments.of("Savings", TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME,
                        (HandlerFactory) SavingsTransactionImportHandler::new),
                Arguments.of("Fixed Deposit", TemplatePopulateImportConstants.FIXED_DEPOSIT_TRANSACTION_SHEET_NAME,
                        (HandlerFactory) FixedDepositTransactionImportHandler::new),
                Arguments.of("Recurring Deposit", TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME,
                        (HandlerFactory) RecurringDepositTransactionImportHandler::new));
    }

    @FunctionalInterface
    private interface HandlerFactory {

        ImportHandler create(PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper);
    }

    @Test
    void rowWhoseAccountDoesNotExistReportsTheMissingAccount() throws IOException {
        when(savingsAccountRepositoryWrapper.findClientIdById(99999L)).thenReturn(null);
        when(savingsAccountRepositoryWrapper.existsById(99999L)).thenReturn(false);
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = transactionSheet(workbook);
            row(sheet, 1, "Client A(1)", "000099999");

            Count count = new SavingsTransactionImportHandler(commandsSourceWritePlatformService, savingsAccountRepositoryWrapper)
                    .process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getErrorCount()).isEqualTo(1);
            verify(commandsSourceWritePlatformService, never()).logCommandSource(any());
            assertThat(sheet.getRow(1).getCell(TransactionConstants.STATUS_COL).getStringCellValue())
                    .isEqualTo("Savings account with identifier 99999 does not exist");
        }
    }

    @Test
    void rowWithoutClientNameIsRejected() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = transactionSheet(workbook);
            row(sheet, 1, null, "000000010");

            Count count = new SavingsTransactionImportHandler(commandsSourceWritePlatformService, savingsAccountRepositoryWrapper)
                    .process(workbook, "en", "dd MMMM yyyy");

            assertThat(count.getErrorCount()).isEqualTo(1);
            verify(commandsSourceWritePlatformService, never()).logCommandSource(any());
            assertThat(sheet.getRow(1).getCell(TransactionConstants.STATUS_COL).getStringCellValue()).isEqualTo("Client Name is required");
        }
    }

    @Test
    void accountOfTheNamedClientIsAccepted() throws IOException {
        when(savingsAccountRepositoryWrapper.findClientIdById(10L)).thenReturn(1L);
        try (Workbook workbook = new HSSFWorkbook()) {
            Row row = row(transactionSheet(workbook), 1, " Client A(1) ", "000000010");

            assertThatCode(() -> SavingsTransactionClientCheck.requireAccountOfClient(row, 10L, savingsAccountRepositoryWrapper))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void accountWithoutClientIsRejected() throws IOException {
        when(savingsAccountRepositoryWrapper.findClientIdById(30L)).thenReturn(null);
        when(savingsAccountRepositoryWrapper.existsById(30L)).thenReturn(true);
        try (Workbook workbook = new HSSFWorkbook()) {
            Row row = row(transactionSheet(workbook), 1, "Client A(1)", "000000030");

            assertThatThrownBy(() -> SavingsTransactionClientCheck.requireAccountOfClient(row, 30L, savingsAccountRepositoryWrapper))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Account 000000030 does not belong to client Client A(1)");
        }
    }

    @Test
    void clientNameWithoutIdIsRejected() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Row row = row(transactionSheet(workbook), 1, "Client A", "000000010");

            assertThatThrownBy(() -> SavingsTransactionClientCheck.requireAccountOfClient(row, 10L, savingsAccountRepositoryWrapper))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Client Name `Client A` not found");
        }
    }

    @Test
    void rowWithoutAccountIsRejected() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Row row = row(transactionSheet(workbook), 1, "Client A(1)", null);

            assertThatThrownBy(() -> SavingsTransactionClientCheck.requireAccountOfClient(row, null, savingsAccountRepositoryWrapper))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Account No. is required");
        }
    }

    private static Sheet transactionSheet(final Workbook workbook) {
        return transactionSheet(workbook, TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME);
    }

    private static Sheet transactionSheet(final Workbook workbook, final String sheetName) {
        workbook.createSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.createRow(0).createCell(TransactionConstants.AMOUNT_COL).setCellValue("Amount*");
        sheet.createRow(TransactionConstants.STATUS_COL);
        return sheet;
    }

    private static Row row(final Sheet sheet, final int rowIndex, final String clientName, final String accountNo) {
        Row row = sheet.createRow(rowIndex);
        if (clientName != null) {
            row.createCell(TransactionConstants.CLIENT_NAME_COL).setCellValue(clientName);
        }
        if (accountNo != null) {
            row.createCell(TransactionConstants.SAVINGS_ACCOUNT_NO_COL).setCellValue(accountNo);
        }
        row.createCell(TransactionConstants.TRANSACTION_TYPE_COL).setCellValue("Deposit");
        row.createCell(TransactionConstants.AMOUNT_COL).setCellValue(10);
        row.createCell(TransactionConstants.TRANSACTION_DATE_COL).setCellValue(LocalDateTime.of(2026, 11, 5, 0, 0));
        return row;
    }
}
