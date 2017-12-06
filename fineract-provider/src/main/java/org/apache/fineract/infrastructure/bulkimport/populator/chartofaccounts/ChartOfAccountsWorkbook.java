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
package org.apache.fineract.infrastructure.bulkimport.populator.chartofaccounts;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.infrastructure.bulkimport.constants.ChartOfAcountsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartOfAccountsWorkbook extends AbstractWorkbookPopulator {
    private List<GLAccountData> glAccounts;
    private Map<String,List<String>> accountTypeToAccountNameAndTag;
    private Map<Integer,Integer[]>accountTypeToBeginEndIndexesofAccountNames;
    private List<String> accountTypesNoDuplicatesList;


    public ChartOfAccountsWorkbook(List<GLAccountData> glAccounts) {
        this.glAccounts = glAccounts;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet chartOfAccountsSheet=workbook.createSheet(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME);
        setLayout(chartOfAccountsSheet);
        setAccountTypeToAccountNameAndTag();
        setLookupTable(chartOfAccountsSheet);
        setRules(chartOfAccountsSheet);
        setDefaults(chartOfAccountsSheet);
    }

    private void setAccountTypeToAccountNameAndTag() {
        accountTypeToAccountNameAndTag=new HashMap<>();
        for (GLAccountData glAccount: glAccounts) {
            addToaccountTypeToAccountNameMap(glAccount.getType().getValue(),glAccount.getName()+
                    "-"+glAccount.getId()+"-"+glAccount.getTagId().getName()+"-"+glAccount.getTagId().getId());
        }
    }

    private void addToaccountTypeToAccountNameMap(String key, String value) {
        List<String> values=accountTypeToAccountNameAndTag.get(key);
        if (values==null){
            values=new ArrayList<String>();
        }
        if (!values.contains(value)){
            values.add(value);
            accountTypeToAccountNameAndTag.put(key,values);
        }
    }

    private void setRules(Sheet chartOfAccountsSheet) {
        CellRangeAddressList accountTypeRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants. ACCOUNT_TYPE_COL,ChartOfAcountsConstants.ACCOUNT_TYPE_COL);
        CellRangeAddressList accountUsageRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.ACCOUNT_USAGE_COL,ChartOfAcountsConstants.ACCOUNT_USAGE_COL);
        CellRangeAddressList manualEntriesAllowedRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL,ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL);
        CellRangeAddressList parentRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.PARENT_COL,ChartOfAcountsConstants.PARENT_COL);
        CellRangeAddressList tagRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.TAG_COL,ChartOfAcountsConstants.TAG_COL);

        DataValidationHelper validationHelper=new HSSFDataValidationHelper((HSSFSheet) chartOfAccountsSheet);
        setNames(chartOfAccountsSheet, accountTypesNoDuplicatesList);

        DataValidationConstraint accountTypeConstraint=validationHelper.
                createExplicitListConstraint(new  String[]{
                        GLAccountType.ASSET.toString(),
                        GLAccountType.LIABILITY.toString(),
                        GLAccountType.EQUITY.toString(),
                        GLAccountType.INCOME.toString(),
                        GLAccountType.EXPENSE.toString()});
        DataValidationConstraint accountUsageConstraint=validationHelper.
                createExplicitListConstraint(new String[]{
                        GLAccountUsage.DETAIL.toString(),
                        GLAccountUsage.HEADER.toString()});
        DataValidationConstraint booleanConstraint=validationHelper.
                createExplicitListConstraint(new String[]{"True","False"});
        DataValidationConstraint parentConstraint=validationHelper.
                createFormulaListConstraint("INDIRECT(CONCATENATE(\"AccountName_\",$A1))");
        DataValidationConstraint tagConstraint=validationHelper.
                createFormulaListConstraint("INDIRECT(CONCATENATE(\"Tags_\",$A1))");

        DataValidation accountTypeValidation=validationHelper.createValidation(accountTypeConstraint,accountTypeRange);
        DataValidation accountUsageValidation=validationHelper.createValidation(accountUsageConstraint,accountUsageRange);
        DataValidation manualEntriesValidation=validationHelper.createValidation(booleanConstraint,manualEntriesAllowedRange);
        DataValidation parentValidation=validationHelper.createValidation(parentConstraint,parentRange);
        DataValidation tagValidation=validationHelper.createValidation(tagConstraint,tagRange);

        chartOfAccountsSheet.addValidationData(accountTypeValidation);
        chartOfAccountsSheet.addValidationData(accountUsageValidation);
        chartOfAccountsSheet.addValidationData(manualEntriesValidation);
        chartOfAccountsSheet.addValidationData(parentValidation);
        chartOfAccountsSheet.addValidationData(tagValidation);
    }

    private void setNames(Sheet chartOfAccountsSheet,List<String> accountTypesNoDuplicatesList) {
        Workbook chartOfAccountsWorkbook=chartOfAccountsSheet.getWorkbook();
        for (Integer i=0;i<accountTypesNoDuplicatesList.size();i++){
            Name tags=chartOfAccountsWorkbook.createName();
            Integer [] tagValueBeginEndIndexes=accountTypeToBeginEndIndexesofAccountNames.get(i);
            if(accountTypeToBeginEndIndexesofAccountNames!=null){
                tags.setNameName("Tags_"+accountTypesNoDuplicatesList.get(i));
                tags.setRefersToFormula(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME+
                        "!$S$"+tagValueBeginEndIndexes[0]+":$S$"+tagValueBeginEndIndexes[1]);
            }
            Name accountNames=chartOfAccountsWorkbook.createName();
            Integer [] accountNamesBeginEndIndexes=accountTypeToBeginEndIndexesofAccountNames.get(i);
            if (accountNamesBeginEndIndexes!=null){
                accountNames.setNameName("AccountName_"+accountTypesNoDuplicatesList.get(i));
                accountNames.setRefersToFormula(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME+
                        "!$Q$"+accountNamesBeginEndIndexes[0]+":$Q$"+accountNamesBeginEndIndexes[1]);
            }
        }
    }
    private void setDefaults(Sheet worksheet){
        try {
            for (Integer rowNo = 1; rowNo < 3000; rowNo++) {
                Row row = worksheet.getRow(rowNo);
                if (row == null)
                    row = worksheet.createRow(rowNo);
                writeFormula(ChartOfAcountsConstants.PARENT_ID_COL, row,
                        "IF(ISERROR(VLOOKUP($E"+(rowNo+1)+",$Q$2:$R$"+(glAccounts.size()+1)+",2,FALSE))," +
                                "\"\",(VLOOKUP($E"+(rowNo+1)+",$Q$2:$R$"+(glAccounts.size()+1)+",2,FALSE)))");
                writeFormula(ChartOfAcountsConstants.TAG_ID_COL,row,
                        "IF(ISERROR(VLOOKUP($H"+(rowNo+1)+",$S$2:$T$"+(glAccounts.size()+1)+",2,FALSE))," +
                                "\"\",(VLOOKUP($H"+(rowNo+1)+",$S$2:$T$"+(glAccounts.size()+1)+",2,FALSE)))");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setLookupTable(Sheet chartOfAccountsSheet) {
        accountTypesNoDuplicatesList =new ArrayList<>();
        for (int i = 0; i <glAccounts.size() ; i++) {
            if (!accountTypesNoDuplicatesList.contains(glAccounts.get(i).getType().getValue())) {
                accountTypesNoDuplicatesList.add(glAccounts.get(i).getType().getValue());
            }
        }
        int rowIndex=1,startIndex=1,accountTypeIndex=0;
        accountTypeToBeginEndIndexesofAccountNames= new HashMap<Integer,Integer[]>();
        for (String accountType: accountTypesNoDuplicatesList) {
             startIndex=rowIndex+1;
             Row row =chartOfAccountsSheet.createRow(rowIndex);
             writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_TYPE_COL,row,accountType);
             List<String> accountNamesandTags =accountTypeToAccountNameAndTag.get(accountType);
             if (!accountNamesandTags.isEmpty()){
                 for (String accountNameandTag:accountNamesandTags) {
                     if (chartOfAccountsSheet.getRow(rowIndex)!=null){
                         String accountNameAndTagAr[]=accountNameandTag.split("-");
                         writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_NAME_COL,row,accountNameAndTagAr[0]);
                         writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_ID_COL,row,accountNameAndTagAr[1]);
                         writeString(ChartOfAcountsConstants.LOOKUP_TAG_COL,row,accountNameAndTagAr[2]);
                         writeString(ChartOfAcountsConstants.LOOKUP_TAG_ID_COL,row,accountNameAndTagAr[3]);
                         rowIndex++;
                     }else{
                         row =chartOfAccountsSheet.createRow(rowIndex);
                         String accountNameAndTagAr[]=accountNameandTag.split("-");
                         writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_NAME_COL,row,accountNameAndTagAr[0]);
                         writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_ID_COL,row,accountNameAndTagAr[1]);
                         writeString(ChartOfAcountsConstants.LOOKUP_TAG_COL,row,accountNameAndTagAr[2]);
                         writeString(ChartOfAcountsConstants.LOOKUP_TAG_ID_COL,row,accountNameAndTagAr[3]);
                         rowIndex++;
                     }
                 }
                 accountTypeToBeginEndIndexesofAccountNames.put(accountTypeIndex++,new Integer[]{startIndex,rowIndex});
             }else {
                 accountTypeIndex++;
             }
        }
    }

    private void setLayout(Sheet chartOfAccountsSheet) {
        Row rowHeader=chartOfAccountsSheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.ACCOUNT_TYPE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.ACCOUNT_NAME_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.ACCOUNT_USAGE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.PARENT_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.PARENT_ID_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.GL_CODE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.TAG_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.TAG_ID_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.DESCRIPTION_COL,TemplatePopulateImportConstants.EXTRALARGE_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_ACCOUNT_TYPE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_ACCOUNT_NAME_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_ACCOUNT_ID_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_TAG_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_TAG_ID_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        
        writeString(ChartOfAcountsConstants.ACCOUNT_TYPE_COL,rowHeader,"Account Type*");
        writeString(ChartOfAcountsConstants.GL_CODE_COL,rowHeader,"GL Code *");
        writeString(ChartOfAcountsConstants.ACCOUNT_USAGE_COL,rowHeader,"Account Usage *");
        writeString(ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL,rowHeader,"Manual entries allowed *");
        writeString(ChartOfAcountsConstants.PARENT_COL,rowHeader,"Parent");
        writeString(ChartOfAcountsConstants.PARENT_ID_COL,rowHeader,"Parent Id");
        writeString(ChartOfAcountsConstants.ACCOUNT_NAME_COL,rowHeader,"Account Name");
        writeString(ChartOfAcountsConstants.TAG_COL,rowHeader,"Tag *");
        writeString(ChartOfAcountsConstants.TAG_ID_COL,rowHeader,"Tag Id");
        writeString(ChartOfAcountsConstants.DESCRIPTION_COL,rowHeader,"Description *");
        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_TYPE_COL,rowHeader,"Lookup Account type");
        writeString(ChartOfAcountsConstants.LOOKUP_TAG_COL,rowHeader,"Lookup Tag");
        writeString(ChartOfAcountsConstants.LOOKUP_TAG_ID_COL,rowHeader,"Lookup Tag Id");
        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_NAME_COL,rowHeader,"Lookup Account name *");
        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_ID_COL,rowHeader,"Lookup Account Id");

    }
}
