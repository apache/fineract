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
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.ChartOfAcountsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.CodeValueDataIdSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.EnumOptionDataIdSerializer;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.exception.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class ChartOfAccountsImportHandler implements ImportHandler {
    private  List<GLAccountData> glAccounts;
    private  Workbook workbook;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ChartOfAccountsImportHandler(final PortfolioCommandSourceWritePlatformService
            commandsSourceWritePlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Override
    public Count process(Workbook workbook, String locale, String dateFormat) {
        this.glAccounts=new ArrayList<>();
        this.workbook=workbook;
        readExcelFile();
        return importEntity();
    }

    public void readExcelFile() {

        Sheet chartOfAccountsSheet=workbook.getSheet(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME);
        Integer noOfEntries= ImportHandlerUtils.getNumberOfRows(chartOfAccountsSheet,TemplatePopulateImportConstants.FIRST_COLUMN_INDEX);
        for (int rowIndex=1;rowIndex<=noOfEntries;rowIndex++){
            Row row;
                row=chartOfAccountsSheet.getRow(rowIndex);
                if (ImportHandlerUtils.isNotImported(row, ChartOfAcountsConstants.STATUS_COL)){
                    glAccounts.add(readGlAccounts(row));
                }
        }
    }

    private GLAccountData readGlAccounts(Row row) {
        String accountType=ImportHandlerUtils.readAsString(ChartOfAcountsConstants.ACCOUNT_TYPE_COL,row);
        EnumOptionData accountTypeEnum=GLAccountType.fromString(accountType);
        String accountName=ImportHandlerUtils.readAsString(ChartOfAcountsConstants.ACCOUNT_NAME_COL,row);
        String usage=ImportHandlerUtils.readAsString(ChartOfAcountsConstants.ACCOUNT_USAGE_COL,row);
        Long usageId=null;
        EnumOptionData usageEnum=null;
        if (usage!=null&& usage.equals(GLAccountUsage.DETAIL.toString())){
            usageId=1L;
            usageEnum=new EnumOptionData(usageId,null,null);
        }else if (usage!=null&&usage.equals(GLAccountUsage.HEADER.toString())){
            usageId=2L;
            usageEnum=new EnumOptionData(usageId,null,null);
        }
        Boolean manualEntriesAllowed=ImportHandlerUtils.readAsBoolean(ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL,row);
        Long parentId=null;
        if (ImportHandlerUtils.readAsString(ChartOfAcountsConstants.PARENT_ID_COL,row)!=null) {
            parentId = Long.parseLong(ImportHandlerUtils.readAsString(ChartOfAcountsConstants.PARENT_ID_COL,row));
        }
        String glCode=ImportHandlerUtils.readAsString(ChartOfAcountsConstants.GL_CODE_COL,row);
        Long tagId=null;
        if(ImportHandlerUtils.readAsString(ChartOfAcountsConstants.TAG_ID_COL,row)!=null)
            tagId=Long.parseLong(ImportHandlerUtils.readAsString(ChartOfAcountsConstants.TAG_ID_COL,row));
        CodeValueData tagIdCodeValueData=new CodeValueData(tagId);
        String description=ImportHandlerUtils.readAsString(ChartOfAcountsConstants.DESCRIPTION_COL,row);
        return GLAccountData.importInstance(accountName,parentId,glCode,manualEntriesAllowed,accountTypeEnum,
                usageEnum,description,tagIdCodeValueData,row.getRowNum());
    }

    public Count importEntity() {
        Sheet chartOfAccountsSheet=workbook.getSheet(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EnumOptionData.class, new EnumOptionDataIdSerializer());
        gsonBuilder.registerTypeAdapter(CodeValueData.class, new CodeValueDataIdSerializer());
        int successCount = 0;
        int errorCount = 0;
        String errorMessage = "";
        for (GLAccountData glAccount: glAccounts) {
            try {
                String payload=gsonBuilder.create().toJson(glAccount);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createGLAccount() //
                        .withJson(payload) //
                        .build(); //
                final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = chartOfAccountsSheet.getRow(glAccount.getRowIndex()).createCell(ChartOfAcountsConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            }catch (RuntimeException ex){
                errorCount++;
                ex.printStackTrace();
                errorMessage=ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(chartOfAccountsSheet,glAccount.getRowIndex(),errorMessage,ChartOfAcountsConstants.STATUS_COL);
            }
        }
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(ChartOfAcountsConstants.STATUS_COL, chartOfAccountsSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                TemplatePopulateImportConstants.STATUS_COLUMN_HEADER);
        return Count.instance(successCount,errorCount);
    }


}
