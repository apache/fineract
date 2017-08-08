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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.importhandler.AbstractImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.CodeValueDataIdSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.EnumOptionDataIdSerializer;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.List;

public class ChartOfAccountsImportHandler extends AbstractImportHandler {
    private final List<GLAccountData> glAccounts;
    private final Workbook workbook;

    private static final int ACCOUNT_TYPE_COL=0;//A
    private static final int ACCOUNT_NAME_COL=1;//B
    private static final int ACCOUNT_USAGE_COL=2;//C
    private static final int MANUAL_ENTRIES_ALLOWED_COL=3;//D
    private static final int PARENT_COL=4;//E
    private static final int PARENT_ID_COL=5;//F
    private static final int GL_CODE_COL=6;//G
    private static final int TAG_COL=7;//H
    private static final int TAG_ID_COL=8;//I
    private static final int DESCRIPTION_COL=9;//J
    private static final int LOOKUP_ACCOUNT_TYPE_COL=15;// P
    private static final int LOOKUP_ACCOUNT_NAME_COL=16; //Q
    private static final int LOOKUP_ACCOUNT_ID_COL=17;//R
    private static final int LOOKUP_TAG_COL=18;    //S
    private static final int LOOKUP_TAG_ID_COL=19;  //T
    private static final int STATUS_COL=20;

    public ChartOfAccountsImportHandler(Workbook workbook) {
        this.glAccounts=new ArrayList<GLAccountData>();
        this.workbook=workbook;
    }

    @Override
    public void readExcelFile() {
        Sheet chartOfAccountsSheet=workbook.getSheet("ChartOfAccounts");
        Integer noOfEntries=getNumberOfRows(chartOfAccountsSheet,0);
        for (int rowIndex=1;rowIndex<noOfEntries;rowIndex++){
            Row row;
            try {
                row=chartOfAccountsSheet.getRow(rowIndex);
                if (isNotImported(row,STATUS_COL)){
                    glAccounts.add(readGlAccounts(row));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private GLAccountData readGlAccounts(Row row) {
        String accountType=readAsString(ACCOUNT_TYPE_COL,row);
        Long accountTypeId=null;
        EnumOptionData accountTypeEnum=null;
        if (accountType!=null && accountType.equalsIgnoreCase("ASSET")){
            accountTypeId=1L;
            accountTypeEnum=new EnumOptionData(accountTypeId,null,null);
        }else if(accountType!=null && accountType.equalsIgnoreCase("LIABILITY")){
            accountTypeId=2L;
            accountTypeEnum=new EnumOptionData(accountTypeId,null,null);
        }else if(accountType!=null && accountType.equalsIgnoreCase("EQUITY")){
            accountTypeId=3L;
            accountTypeEnum=new EnumOptionData(accountTypeId,null,null);
        }
        else if(accountType!=null && accountType.equalsIgnoreCase("INCOME")){
            accountTypeId=4L;
            accountTypeEnum=new EnumOptionData(accountTypeId,null,null);
        }else if(accountType!=null && accountType.equalsIgnoreCase("EXPENSE")){
            accountTypeId=5L;
            accountTypeEnum=new EnumOptionData(accountTypeId,null,null);
        }
        String accountName=readAsString(ACCOUNT_NAME_COL,row);
        String usage=readAsString(ACCOUNT_USAGE_COL,row);
        Long usageId=null;
        EnumOptionData usageEnum=null;
        if (usage!=null&& usage.equals("Detail")){
            usageId=1L;
            usageEnum=new EnumOptionData(usageId,null,null);
        }else if (usage!=null&&usage.equals("Header")){
            usageId=2L;
            usageEnum=new EnumOptionData(usageId,null,null);
        }
        Boolean manualEntriesAllowed=readAsBoolean(MANUAL_ENTRIES_ALLOWED_COL,row);
        Long parentId=Long.parseLong(readAsString(PARENT_ID_COL,row));
        String glCode=readAsString(GL_CODE_COL,row);
        Long tagId=Long.parseLong(readAsString(TAG_ID_COL,row));
        CodeValueData tagIdCodeValueData=new CodeValueData(tagId);
        String description=readAsString(DESCRIPTION_COL,row);
        return new GLAccountData(accountName,parentId,glCode,manualEntriesAllowed,accountTypeEnum,usageEnum,description,tagIdCodeValueData,row.getRowNum());
    }

    @Override
    public void Upload(PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        Sheet chartOfAccountsSheet=workbook.getSheet("ChartOfAccounts");
        for (GLAccountData glAccount: glAccounts) {
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(EnumOptionData.class, new EnumOptionDataIdSerializer());
                gsonBuilder.registerTypeAdapter(CodeValueData.class, new CodeValueDataIdSerializer());
                String payload=gsonBuilder.create().toJson(glAccount);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createGLAccount() //
                        .withJson(payload) //
                        .build(); //
                final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
                Cell statusCell = chartOfAccountsSheet.getRow(glAccount.getRowIndex()).createCell(STATUS_COL);
                statusCell.setCellValue("Imported");
                statusCell.setCellStyle(getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException e) {
                e.printStackTrace();
                String message="";
                if (e.getMessage()!=null)
                    message = parseStatus(e.getMessage());
                Cell statusCell = chartOfAccountsSheet.getRow(glAccount.getRowIndex()).createCell(STATUS_COL);
                statusCell.setCellValue(message);
                statusCell.setCellStyle(getCellStyle(workbook, IndexedColors.RED));

            }
        }
        chartOfAccountsSheet.setColumnWidth(STATUS_COL, 15000);
        writeString(STATUS_COL, chartOfAccountsSheet.getRow(0), "Status");
    }
}
