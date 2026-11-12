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
package org.apache.fineract.infrastructure.bulkimport.importhandler.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.List;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.LoanConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LoanImportHandlerRepaymentStrategyTest {

    private static final String LOCALE = "en";
    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String STRATEGY_NAME = "Creocore Unique";
    private static final String STRATEGY_CODE = "creocore-strategy";

    private final PortfolioCommandSourceWritePlatformService commandService = mock(PortfolioCommandSourceWritePlatformService.class);
    private final LoanRepaymentScheduleTransactionProcessorFactory processorFactory = mock(
            LoanRepaymentScheduleTransactionProcessorFactory.class);
    private final LoanImportHandler handler = new LoanImportHandler(commandService, processorFactory);

    private Workbook workbook;
    private Sheet loanSheet;

    @BeforeEach
    void setUp() {
        workbook = new HSSFWorkbook();
        loanSheet = workbook.createSheet(TemplatePopulateImportConstants.LOANS_SHEET_NAME);
        loanSheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        workbook.createSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
        workbook.createSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
        workbook.createSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME);
        workbook.createSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);

        when(processorFactory.determineProcessor(any())).thenAnswer(invocation -> {
            throw new LoanTransactionProcessingStrategyNotFoundException(invocation.getArgument(0));
        });
        LoanRepaymentScheduleTransactionProcessor creocore = mock(LoanRepaymentScheduleTransactionProcessor.class);
        when(creocore.getCode()).thenReturn(STRATEGY_CODE);
        doReturn(creocore).when(processorFactory).determineProcessor(STRATEGY_NAME);
        when(commandService.logCommandSource(any())).thenReturn(new CommandProcessingResultBuilder().withLoanId(1L).build());
    }

    @AfterEach
    void tearDown() throws IOException {
        workbook.close();
    }

    @Test
    void repaymentStrategyNameIsSentAsItsCode() {
        loanRow(1, STRATEGY_NAME);

        Count count = handler.process(workbook, LOCALE, DATE_FORMAT);

        assertThat(count.getSuccessCount()).isEqualTo(1);
        assertThat(sentStrategyCodes(1)).containsExactly(STRATEGY_CODE);
    }

    @Test
    void blankRepaymentStrategyFailsOnlyItsOwnRow() {
        Row valid = loanRow(1, STRATEGY_NAME);
        Row blank = loanRow(2, null);

        Count count = handler.process(workbook, LOCALE, DATE_FORMAT);

        assertThat(count.getSuccessCount()).isEqualTo(1);
        assertThat(count.getErrorCount()).isEqualTo(1);
        assertThat(sentStrategyCodes(1)).containsExactly(STRATEGY_CODE);
        assertThat(ImportHandlerUtils.readAsString(LoanConstants.STATUS_COL, valid))
                .isEqualTo(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
        assertThat(ImportHandlerUtils.readAsString(LoanConstants.FAILURE_REPORT_COL, blank)).isEqualTo("Repayment Strategy is mandatory");
    }

    @Test
    void unknownRepaymentStrategyFailsOnlyItsOwnRowNamingTheValue() {
        Row unknown = loanRow(1, "Bogus Strategy");
        loanRow(2, STRATEGY_NAME);

        Count count = handler.process(workbook, LOCALE, DATE_FORMAT);

        assertThat(count.getSuccessCount()).isEqualTo(1);
        assertThat(count.getErrorCount()).isEqualTo(1);
        assertThat(ImportHandlerUtils.readAsString(LoanConstants.FAILURE_REPORT_COL, unknown))
                .isEqualTo("Repayment Strategy 'Bogus Strategy' not recognised");
    }

    private Row loanRow(final int rowIndex, final String repaymentStrategy) {
        Row row = loanSheet.createRow(rowIndex);
        row.createCell(LoanConstants.OFFICE_NAME_COL).setCellValue("Head Office");
        row.createCell(LoanConstants.LOAN_TYPE_COL).setCellValue("Individual");
        row.createCell(LoanConstants.PRINCIPAL_COL).setCellValue(1000d);
        if (repaymentStrategy != null) {
            row.createCell(LoanConstants.REPAYMENT_STRATEGY_COL).setCellValue(repaymentStrategy);
        }
        return row;
    }

    private List<String> sentStrategyCodes(final int expectedCommands) {
        ArgumentCaptor<CommandWrapper> commands = ArgumentCaptor.forClass(CommandWrapper.class);
        verify(commandService, times(expectedCommands)).logCommandSource(commands.capture());
        return commands.getAllValues().stream().map(command -> JsonParser.parseString(command.getJson()).getAsJsonObject()
                .get("transactionProcessingStrategyCode").getAsString()).toList();
    }
}
