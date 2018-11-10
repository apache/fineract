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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.LoanRepaymentConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.*;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class LoanRepaymentImportHandler implements ImportHandler {
    private  Workbook workbook;
    private  List<LoanTransactionData> loanRepayments;
    private Integer loanAccountId;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Autowired
    public LoanRepaymentImportHandler(final PortfolioCommandSourceWritePlatformService
            commandsSourceWritePlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Override
    public Count process(Workbook workbook, String locale, String dateFormat) {
        this.workbook=workbook;
        this.loanRepayments=new ArrayList<>();
        readExcelFile(locale,dateFormat);
        return importEntity(dateFormat);
    }
    public void readExcelFile(String locale, String dateFormat) {
        Sheet loanRepaymentSheet = workbook.getSheet(TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(loanRepaymentSheet, LoanRepaymentConstants.AMOUNT_COL);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
                row = loanRepaymentSheet.getRow(rowIndex);
                if(ImportHandlerUtils.isNotImported(row, LoanRepaymentConstants.STATUS_COL))
                    loanRepayments.add(readLoanRepayment(row,locale,dateFormat));
        }
    }

    private LoanTransactionData readLoanRepayment(Row row,String locale, String dateFormat) {
        String loanaccountInfo=ImportHandlerUtils.readAsString(LoanRepaymentConstants.LOAN_ACCOUNT_NO_COL, row);
        if (loanaccountInfo!=null){
            String loanAccountAr[]=loanaccountInfo.split("-");
            loanAccountId=Integer.parseInt(loanAccountAr[0]);
        }
        BigDecimal repaymentAmount=null;
        if (ImportHandlerUtils.readAsDouble(LoanRepaymentConstants.AMOUNT_COL, row)!=null)
         repaymentAmount = BigDecimal.valueOf(ImportHandlerUtils.readAsDouble(LoanRepaymentConstants.AMOUNT_COL, row));
        LocalDate repaymentDate = ImportHandlerUtils.readAsDate(LoanRepaymentConstants.REPAID_ON_DATE_COL, row);
        String repaymentType = ImportHandlerUtils.readAsString(LoanRepaymentConstants.REPAYMENT_TYPE_COL, row);
        Long repaymentTypeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME), repaymentType);
        String accountNumber = ImportHandlerUtils.readAsString(LoanRepaymentConstants.ACCOUNT_NO_COL, row);
        Integer checkNumber = ImportHandlerUtils.readAsInt(LoanRepaymentConstants.CHECK_NO_COL, row);
        Integer routingCode = ImportHandlerUtils.readAsInt(LoanRepaymentConstants.ROUTING_CODE_COL, row);
        Integer receiptNumber = ImportHandlerUtils.readAsInt(LoanRepaymentConstants.RECEIPT_NO_COL, row);
        Integer bankNumber = ImportHandlerUtils.readAsInt(LoanRepaymentConstants.BANK_NO_COL, row);
        return LoanTransactionData.importInstance(repaymentAmount, repaymentDate, repaymentTypeId, accountNumber,
                checkNumber, routingCode, receiptNumber, bankNumber, loanAccountId, "", row.getRowNum(),locale,dateFormat);
    }

    public Count importEntity(String dateFormat) {
        Sheet loanRepaymentSheet = workbook.getSheet(TemplatePopulateImportConstants.LOAN_REPAYMENT_SHEET_NAME);
        int successCount=0;
        int errorCount=0;
        String errorMessage="";
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));

        for (LoanTransactionData loanRepayment : loanRepayments) {
            try {

                JsonObject loanRepaymentJsonob=gsonBuilder.create().toJsonTree(loanRepayment).getAsJsonObject();
                loanRepaymentJsonob.remove("manuallyReversed");
                String payload=loanRepaymentJsonob.toString();
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .loanRepaymentTransaction(loanRepayment.getAccountId().longValue()) //
                        .withJson(payload) //
                        .build(); //
                final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = loanRepaymentSheet.getRow(loanRepayment.getRowIndex()).createCell(LoanRepaymentConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            }catch (RuntimeException ex){
                errorCount++;
                ex.printStackTrace();
                errorMessage=ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(loanRepaymentSheet,loanRepayment.getRowIndex(),errorMessage,LoanRepaymentConstants.STATUS_COL);
            }

        }
        loanRepaymentSheet.setColumnWidth(LoanRepaymentConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(LoanRepaymentConstants.STATUS_COL,
                loanRepaymentSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
        return Count.instance(successCount,errorCount);
    }


}
