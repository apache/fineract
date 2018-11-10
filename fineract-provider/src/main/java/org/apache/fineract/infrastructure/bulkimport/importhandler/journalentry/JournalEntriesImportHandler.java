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

import com.google.gson.GsonBuilder;
import org.apache.fineract.accounting.journalentry.data.CreditDebit;
import org.apache.fineract.accounting.journalentry.data.JournalEntryData;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.ChartOfAcountsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.JournalEntryConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.CurrencyDateCodeSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.*;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class JournalEntriesImportHandler implements ImportHandler {
    private Workbook workbook;
    private List<JournalEntryData> gltransaction;
    private LocalDate transactionDate;

    List<CreditDebit> credits = new ArrayList<>();
    List<CreditDebit> debits = new ArrayList<>();

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public JournalEntriesImportHandler(final PortfolioCommandSourceWritePlatformService
            commandsSourceWritePlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Override
    public Count process(Workbook workbook, String locale, String dateFormat) {
        this.workbook=workbook;
        gltransaction=new ArrayList<>();
        readExcelFile(locale,dateFormat);
        return importEntity(dateFormat);
    }

    public void readExcelFile(final String locale, final String dateFormat) {
        String currentTransactionId = null;
        String prevTransactionId = null;
        JournalEntryData journalEntry = null;

        Sheet addJournalEntriesSheet = workbook.getSheet(TemplatePopulateImportConstants.JOURNAL_ENTRY_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(addJournalEntriesSheet, 4);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
                row = addJournalEntriesSheet.getRow(rowIndex);

                currentTransactionId = ImportHandlerUtils.readAsString(JournalEntryConstants.TRANSACTION_ID_COL, row);

                if (currentTransactionId.equals(prevTransactionId)) {
                    if (journalEntry != null) {

                        String creditGLAcct = ImportHandlerUtils.readAsString(
                                JournalEntryConstants.GL_ACCOUNT_ID_CREDIT_COL, row);
                        Long glAccountIdCredit = ImportHandlerUtils.getIdByName(
                                workbook.getSheet(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME), creditGLAcct);

                        String debitGLAcct = ImportHandlerUtils.readAsString(
                                JournalEntryConstants. GL_ACCOUNT_ID_DEBIT_COL, row);

                        Long glAccountIdDebit = ImportHandlerUtils.getIdByName(
                                workbook.getSheet(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME), debitGLAcct);

                        BigDecimal creditAmt=null;
                        if (ImportHandlerUtils.readAsDouble(JournalEntryConstants.AMOUNT_CREDIT_COL, row)!=null)
                        creditAmt = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(JournalEntryConstants.AMOUNT_CREDIT_COL, row));
                        BigDecimal debitAmount=null;
                        if (ImportHandlerUtils.readAsDouble(JournalEntryConstants.AMOUNT_DEBIT_COL, row)!=null)
                        debitAmount = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(JournalEntryConstants.AMOUNT_DEBIT_COL, row));

                        if (creditGLAcct!=null) {

                            CreditDebit credit = new CreditDebit(
                                    glAccountIdCredit, creditAmt);
                            journalEntry.addCredits(credit);
                        }
                        if (debitGLAcct!=null) {
                            CreditDebit debit = new CreditDebit(
                                    glAccountIdDebit, debitAmount);

                            journalEntry.addDebits(debit);
                        }
                    }
                } else {

                    if (journalEntry != null) {
                        gltransaction.add(journalEntry);
                        journalEntry = null;
                    }

                    journalEntry = readAddJournalEntries(row,locale,dateFormat);

                }
            prevTransactionId = currentTransactionId;
        }
        // Adding last JE
        gltransaction.add(journalEntry);
    }

    private JournalEntryData readAddJournalEntries(Row row,String locale,String dateFormat) {
        LocalDate transactionDateCheck = ImportHandlerUtils.readAsDate(JournalEntryConstants.TRANSACION_ON_DATE_COL, row);
        if (transactionDateCheck!=null)
            transactionDate = transactionDateCheck;

        String officeName = ImportHandlerUtils.readAsString(JournalEntryConstants.OFFICE_NAME_COL, row);
        Long officeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), officeName);
        String paymentType = ImportHandlerUtils.readAsString(JournalEntryConstants.PAYMENT_TYPE_ID_COL, row);
        Long paymentTypeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME),
                paymentType);
        String currencyName = ImportHandlerUtils.readAsString(JournalEntryConstants.CURRENCY_NAME_COL, row);
        String currencyCode = ImportHandlerUtils.getCodeByName(workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME),
                currencyName).toString();
        String glAccountNameCredit = ImportHandlerUtils.readAsString(JournalEntryConstants.GL_ACCOUNT_ID_CREDIT_COL, row);
        Long glAccountIdCredit = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME),
                glAccountNameCredit);
        String glAccountNameDebit = ImportHandlerUtils.readAsString(JournalEntryConstants.GL_ACCOUNT_ID_DEBIT_COL, row);
        Long glAccountIdDebit = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME),
                glAccountNameDebit);

        credits = new ArrayList<>();
        debits = new ArrayList<>();

      //  String credit = readAsString(JournalEntryConstants.GL_ACCOUNT_ID_CREDIT_COL, row);
      //  String debit = readAsString(JournalEntryConstants.GL_ACCOUNT_ID_DEBIT_COL, row);

        if (glAccountNameCredit!=null) {
            if (ImportHandlerUtils.readAsDouble(JournalEntryConstants.AMOUNT_CREDIT_COL, row) != null){
                credits.add(new CreditDebit(glAccountIdCredit, BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(
                        JournalEntryConstants.AMOUNT_CREDIT_COL, row))));
            }else {
                credits.add(new CreditDebit(glAccountIdCredit,null));
            }
        }

        if (glAccountNameDebit!=null) {
            if (ImportHandlerUtils.readAsDouble(JournalEntryConstants.AMOUNT_DEBIT_COL, row)!=null) {
                debits.add(new CreditDebit(glAccountIdDebit, BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(
                        JournalEntryConstants.AMOUNT_DEBIT_COL, row))));
            }else {
                debits.add(new CreditDebit(glAccountIdDebit, null));
            }
        }
        String accountNo=ImportHandlerUtils.readAsString(JournalEntryConstants.ACCOUNT_NO_COL,row);
        String chequeNo=ImportHandlerUtils.readAsString(JournalEntryConstants.CHECK_NO_COL,row);
        String routingCode=ImportHandlerUtils.readAsString(JournalEntryConstants.ROUTING_CODE_COL,row);
        String receiptNo=ImportHandlerUtils.readAsString(JournalEntryConstants.RECEIPT_NO_COL,row);
        String bankNo=ImportHandlerUtils.readAsString(JournalEntryConstants.BANK_NO_COL,row);
        String comments=ImportHandlerUtils.readAsString(JournalEntryConstants.COMMENTS_COL,row);

        return JournalEntryData.importInstance(officeId, transactionDate, currencyCode,
                paymentTypeId, row.getRowNum(), credits, debits,accountNo,chequeNo,routingCode,receiptNo,bankNo,comments,locale,dateFormat);

    }

    public Count importEntity(String dateFormat) {
        Sheet addJournalEntriesSheet = workbook.getSheet(TemplatePopulateImportConstants.JOURNAL_ENTRY_SHEET_NAME);
        int successCount=0;
        int errorCount=0;
        String errorMessage="";
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        gsonBuilder.registerTypeAdapter(CurrencyData.class,new CurrencyDateCodeSerializer());

        for (JournalEntryData transaction : gltransaction) {
            try {
                String payload  =gsonBuilder.create().toJson(transaction);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createJournalEntry() //
                        .withJson(payload) //
                        .build(); //
                final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = addJournalEntriesSheet.getRow(
                        transaction.getRowIndex()).createCell(JournalEntryConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook,
                        IndexedColors.LIGHT_GREEN));
            }catch (RuntimeException ex){
                errorCount++;
                ex.printStackTrace();
                errorMessage=ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(addJournalEntriesSheet,transaction.getRowIndex(),errorMessage, JournalEntryConstants.STATUS_COL);
            }

        }
        addJournalEntriesSheet.setColumnWidth(JournalEntryConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(JournalEntryConstants.STATUS_COL, addJournalEntriesSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX), TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
        return Count.instance(successCount,errorCount);
    }


}
