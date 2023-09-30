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
package org.apache.fineract.infrastructure.bulkimport.importhandler.chartofaccounts;

import com.google.gson.GsonBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.accounting.glaccount.exception.GLAccountNotFoundException;
import org.apache.fineract.accounting.journalentry.data.CreditDebit;
import org.apache.fineract.accounting.journalentry.data.JournalEntryData;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.ChartOfAcountsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.CodeValueDataIdSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.CurrencyDateCodeSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.EnumOptionDataIdSerializer;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChartOfAccountsImportHandler implements ImportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ChartOfAccountsImportHandler.class);

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final GLAccountRepositoryWrapper glAccountRepository;

    @Autowired
    public ChartOfAccountsImportHandler(final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            GLAccountRepositoryWrapper glAccountRepository) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.glAccountRepository = glAccountRepository;
    }

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {
        List<GLAccountData> glAccounts = new ArrayList<>();
        // for opening bal
        List<JournalEntryData> glTransactions = new ArrayList<>();
        List<CreditDebit> credits = new ArrayList<>();
        List<CreditDebit> debits = new ArrayList<>();

        boolean flagForOpBal = readExcelFile(workbook, glAccounts);
        return importEntity(workbook, glAccounts, glTransactions, credits, debits, flagForOpBal, locale, dateFormat);
    }

    private boolean readExcelFile(final Workbook workbook, final List<GLAccountData> glAccounts) {
        Sheet chartOfAccountsSheet = workbook.getSheet(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(chartOfAccountsSheet, TemplatePopulateImportConstants.FIRST_COLUMN_INDEX);
        boolean flagForOpBal = false;
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = chartOfAccountsSheet.getRow(rowIndex);
            if (ImportHandlerUtils.isNotImported(row, ChartOfAcountsConstants.STATUS_COL)) {
                glAccounts.add(readGlAccounts(row));
                if (ImportHandlerUtils.readAsString(ChartOfAcountsConstants.OFFICE_COL, row) != null) {
                    flagForOpBal = Boolean.TRUE;
                } else {
                    flagForOpBal = Boolean.FALSE;
                }
            }
        }

        return flagForOpBal;
    }

    private GLAccountData readGlAccounts(final Row row) {

        String accountType = ImportHandlerUtils.readAsString(ChartOfAcountsConstants.ACCOUNT_TYPE_COL, row);
        EnumOptionData accountTypeEnum = GLAccountType.fromString(accountType);
        String accountName = ImportHandlerUtils.readAsString(ChartOfAcountsConstants.ACCOUNT_NAME_COL, row);
        String usage = ImportHandlerUtils.readAsString(ChartOfAcountsConstants.ACCOUNT_USAGE_COL, row);
        Long usageId = null;
        EnumOptionData usageEnum = null;
        if (usage != null && usage.equals(GLAccountUsage.DETAIL.toString())) {
            usageId = 1L;
            usageEnum = new EnumOptionData(usageId, null, null);
        } else if (usage != null && usage.equals(GLAccountUsage.HEADER.toString())) {
            usageId = 2L;
            usageEnum = new EnumOptionData(usageId, null, null);
        }
        Boolean manualEntriesAllowed = ImportHandlerUtils.readAsBoolean(ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL, row);
        Long parentId = null;
        if (ImportHandlerUtils.readAsString(ChartOfAcountsConstants.PARENT_ID_COL, row) != null) {
            parentId = Long.parseLong(Objects.requireNonNull(ImportHandlerUtils.readAsString(ChartOfAcountsConstants.PARENT_ID_COL, row)));
        }
        String glCode = ImportHandlerUtils.readAsString(ChartOfAcountsConstants.GL_CODE_COL, row);
        Long tagId = null;
        CodeValueData tagIdCodeValueData = null;
        if (ImportHandlerUtils.readAsString(ChartOfAcountsConstants.TAG_ID_COL, row) != null) {
            tagId = Long.parseLong(Objects.requireNonNull(ImportHandlerUtils.readAsString(ChartOfAcountsConstants.TAG_ID_COL, row)));
            tagIdCodeValueData = new CodeValueData().setId(tagId);
        }
        String description = ImportHandlerUtils.readAsString(ChartOfAcountsConstants.DESCRIPTION_COL, row);
        return new GLAccountData().setName(accountName).setParentId(parentId).setGlCode(glCode)
                .setManualEntriesAllowed(manualEntriesAllowed).setType(accountTypeEnum).setUsage(usageEnum).setDescription(description)
                .setTagId(tagIdCodeValueData).setRowIndex(row.getRowNum());
    }

    private Count importEntity(final Workbook workbook, final List<GLAccountData> glAccounts, final List<JournalEntryData> glTransactions,
            final List<CreditDebit> credits, final List<CreditDebit> debits, final boolean flagForOpBal, final String locale,
            final String dateFormat) {
        Sheet chartOfAccountsSheet = workbook.getSheet(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME);

        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(EnumOptionData.class, new EnumOptionDataIdSerializer());
        gsonBuilder.registerTypeAdapter(CodeValueData.class, new CodeValueDataIdSerializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        gsonBuilder.registerTypeAdapter(CurrencyData.class, new CurrencyDateCodeSerializer());
        int successCount = 0;
        int errorCount = 0;
        String errorMessage = "";

        if (glAccounts != null) {
            for (GLAccountData glAccount : glAccounts) {
                try {
                    String payload = gsonBuilder.create().toJson(glAccount);
                    final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                            .createGLAccount() //
                            .withJson(payload) //
                            .build(); //
                    commandsSourceWritePlatformService.logCommandSource(commandRequest);
                    successCount++;
                    Cell statusCell = chartOfAccountsSheet.getRow(glAccount.getRowIndex()).createCell(ChartOfAcountsConstants.STATUS_COL);
                    statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                    statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
                } catch (RuntimeException ex) {
                    errorCount++;
                    LOG.error("Problem occurred in importEntity function", ex);
                    errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                    ImportHandlerUtils.writeErrorMessage(chartOfAccountsSheet, glAccount.getRowIndex(), errorMessage,
                            ChartOfAcountsConstants.STATUS_COL);
                }
            }
            if (flagForOpBal) {
                try {
                    readExcelFileForOpBal(workbook, glTransactions, credits, debits, locale, dateFormat);
                    JournalEntryData transaction = glTransactions.get(glTransactions.size() - 1);
                    String payload = gsonBuilder.create().toJson(transaction);

                    final CommandWrapper commandRequest = new CommandWrapperBuilder().defineOpeningBalanceForJournalEntry()
                            .withJson(payload).build();
                    commandsSourceWritePlatformService.logCommandSource(commandRequest);
                    successCount++;
                    Cell statusCell = chartOfAccountsSheet.getRow(1).createCell(ChartOfAcountsConstants.STATUS_COL);
                    statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                    statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
                } catch (RuntimeException ex) {
                    errorCount++;
                    LOG.error("Problem occurred in importEntity function", ex);
                    errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                    ImportHandlerUtils.writeErrorMessage(chartOfAccountsSheet, 1, errorMessage, ChartOfAcountsConstants.STATUS_COL);
                }
            }
            chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
            ImportHandlerUtils.writeString(ChartOfAcountsConstants.STATUS_COL,
                    chartOfAccountsSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                    TemplatePopulateImportConstants.STATUS_COLUMN_HEADER);
            return Count.instance(successCount, errorCount);
        }

        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(ChartOfAcountsConstants.STATUS_COL,
                chartOfAccountsSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                TemplatePopulateImportConstants.STATUS_COLUMN_HEADER);
        return Count.instance(successCount, errorCount);

    }

    // for opening balance
    private void readExcelFileForOpBal(final Workbook workbook, final List<JournalEntryData> glTransactions,
            final List<CreditDebit> credits, final List<CreditDebit> debits, final String locale, final String dateFormat) {

        Sheet chartOfAccountsSheet = workbook.getSheet(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(chartOfAccountsSheet, TemplatePopulateImportConstants.FIRST_COLUMN_INDEX);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = chartOfAccountsSheet.getRow(rowIndex);

            //
            JournalEntryData journalEntry;
            journalEntry = readAddJournalEntries(row, credits, debits, locale, dateFormat);
            glTransactions.add(journalEntry);
        }

    }

    // for opening balance
    private JournalEntryData readAddJournalEntries(final Row row, final List<CreditDebit> credits, final List<CreditDebit> debits,
            final String locale, String dateFormat) {
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();

        Long officeId = ImportHandlerUtils.readAsLong(ChartOfAcountsConstants.OFFICE_COL_ID, row);

        String currencyCode = ImportHandlerUtils.readAsString(ChartOfAcountsConstants.CURRENCY_CODE, row);
        String accountToBeDebitedCredited = ImportHandlerUtils.readAsString(ChartOfAcountsConstants.ACCOUNT_NAME_COL, row);
        String glCode = ImportHandlerUtils.readAsString(ChartOfAcountsConstants.GL_CODE_COL, row);
        GLAccount glAccount = this.glAccountRepository.findOneByGlCodeWithNotFoundDetection(glCode);
        Long glAccountIdToDebitedCredited = glAccount.getId();
        if (glAccountIdToDebitedCredited == null) {
            throw new GLAccountNotFoundException("Account does not exist");
        }

        // String credit =
        // readAsString(JournalEntryConstants.GL_ACCOUNT_ID_CREDIT_COL, row);
        // String debit =
        // readAsString(JournalEntryConstants.GL_ACCOUNT_ID_DEBIT_COL, row);

        if (accountToBeDebitedCredited != null) {
            if (ImportHandlerUtils.readAsLong(ChartOfAcountsConstants.CREDIT_AMOUNT, row) != null) {
                credits.add(new CreditDebit(glAccountIdToDebitedCredited,
                        BigDecimal.valueOf(ImportHandlerUtils.readAsLong(ChartOfAcountsConstants.CREDIT_AMOUNT, row))));

            } else if (ImportHandlerUtils.readAsLong(ChartOfAcountsConstants.DEBIT_AMOUNT, row) != null) {
                debits.add(new CreditDebit(glAccountIdToDebitedCredited,
                        BigDecimal.valueOf(ImportHandlerUtils.readAsLong(ChartOfAcountsConstants.DEBIT_AMOUNT, row))));
            }
        }

        return JournalEntryData.importInstance1(officeId, transactionDate, currencyCode, credits, debits, locale, dateFormat);

    }
}
