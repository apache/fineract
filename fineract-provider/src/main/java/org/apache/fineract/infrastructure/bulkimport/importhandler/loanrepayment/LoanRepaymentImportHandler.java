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
package org.apache.fineract.infrastructure.bulkimport.importhandler.loanrepayment;

import com.google.common.base.Splitter;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
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
public class LoanRepaymentImportHandler implements ImportHandler {

    public static final String SEPARATOR = "-";
    public static final String EMPTY_STR = "";
    private static final Logger LOG = LoggerFactory.getLogger(LoanRepaymentImportHandler.class);
    private final LoanReadPlatformService loanReadPlatformService;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public LoanRepaymentImportHandler(final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final LoanReadPlatformService loanReadPlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.loanReadPlatformService = loanReadPlatformService;
    }

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {

        List<LoanTransactionData> loanRepayments = readExcelFile(workbook, locale, dateFormat);
        return importEntity(workbook, loanRepayments, dateFormat);
    }

    private List<LoanTransactionData> readExcelFile(final Workbook workbook, final String locale, final String dateFormat) {
        List<LoanTransactionData> loanRepayments = new ArrayList<>();
        Sheet loanRepaymentSheet = workbook.getSheet(TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(loanRepaymentSheet, LoanRepaymentConstants.AMOUNT_COL);
        Long loanAccountId = null;
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = loanRepaymentSheet.getRow(rowIndex);
            if (ImportHandlerUtils.isNotImported(row, LoanRepaymentConstants.STATUS_COL)) {
                loanRepayments.add(readLoanRepayment(workbook, loanAccountId, row, locale, dateFormat));
            }
        }
        return loanRepayments;
    }

    private LoanTransactionData readLoanRepayment(final Workbook workbook, Long loanAccountId, final Row row, final String locale,
            final String dateFormat) {
        String loanaccountInfo = ImportHandlerUtils.readAsString(LoanRepaymentConstants.LOAN_ACCOUNT_NO_COL, row);
        if (loanaccountInfo != null) {
            List<String> loanAccountAr = Splitter.on(SEPARATOR).splitToList(loanaccountInfo);
            loanAccountId = this.loanReadPlatformService.retrieveLoanIdByAccountNumber(loanAccountAr.get(0));
        }
        BigDecimal repaymentAmount = null;
        if (ImportHandlerUtils.readAsDouble(LoanRepaymentConstants.AMOUNT_COL, row) != null) {
            repaymentAmount = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(LoanRepaymentConstants.AMOUNT_COL, row));
        }
        LocalDate repaymentDate = ImportHandlerUtils.readAsDate(LoanRepaymentConstants.REPAID_ON_DATE_COL, row);
        String repaymentType = ImportHandlerUtils.readAsString(LoanRepaymentConstants.REPAYMENT_TYPE_COL, row);
        Long repaymentTypeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME),
                repaymentType);
        String accountNumber = ImportHandlerUtils.readAsString(LoanRepaymentConstants.ACCOUNT_NO_COL, row);
        Integer checkNumber = ImportHandlerUtils.readAsInt(LoanRepaymentConstants.CHECK_NO_COL, row);
        Integer routingCode = ImportHandlerUtils.readAsInt(LoanRepaymentConstants.ROUTING_CODE_COL, row);
        Integer receiptNumber = ImportHandlerUtils.readAsInt(LoanRepaymentConstants.RECEIPT_NO_COL, row);
        Integer bankNumber = ImportHandlerUtils.readAsInt(LoanRepaymentConstants.BANK_NO_COL, row);
        return LoanTransactionData.importInstance(repaymentAmount, repaymentDate, repaymentTypeId, accountNumber, checkNumber, routingCode,
                receiptNumber, bankNumber, loanAccountId, EMPTY_STR, row.getRowNum(), locale, dateFormat);
    }

    private Count importEntity(final Workbook workbook, final List<LoanTransactionData> loanRepayments, final String dateFormat) {
        Sheet loanRepaymentSheet = workbook.getSheet(TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME);
        int successCount = 0;
        int errorCount = 0;
        String errorMessage;
        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));

        for (LoanTransactionData loanRepayment : loanRepayments) {
            try {

                JsonObject loanRepaymentJsonob = gsonBuilder.create().toJsonTree(loanRepayment).getAsJsonObject();
                loanRepaymentJsonob.remove("manuallyReversed");
                String payload = loanRepaymentJsonob.toString();
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .loanRepaymentTransaction(loanRepayment.getAccountId()) //
                        .withJson(payload) //
                        .build(); //
                commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = loanRepaymentSheet.getRow(loanRepayment.getRowIndex()).createCell(LoanRepaymentConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException ex) {
                errorCount++;
                LOG.error("Problem occurred in importEntity function", ex);
                errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(loanRepaymentSheet, loanRepayment.getRowIndex(), errorMessage,
                        LoanRepaymentConstants.STATUS_COL);
            }

        }
        loanRepaymentSheet.setColumnWidth(LoanRepaymentConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(LoanRepaymentConstants.STATUS_COL,
                loanRepaymentSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
        return Count.instance(successCount, errorCount);
    }

}
