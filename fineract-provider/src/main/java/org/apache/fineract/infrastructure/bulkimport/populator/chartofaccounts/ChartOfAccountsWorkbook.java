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
    private List<String> accountTypesNoDuplicateslist;


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


    public ChartOfAccountsWorkbook(List<GLAccountData> glAccounts) {
        this.glAccounts = glAccounts;
    }

    @Override
    public void populate(Workbook workbook) {
        Sheet chartOfAccountsSheet=workbook.createSheet("ChartOfAccounts");
        setLayout(chartOfAccountsSheet);
        setAccountTypeToAccountNameAndTag();
        setLookupTable(chartOfAccountsSheet);
        setRules(chartOfAccountsSheet);
        setDefaults(chartOfAccountsSheet);
    }

    private void setAccountTypeToAccountNameAndTag() {
        accountTypeToAccountNameAndTag=new HashMap<>();
        for (GLAccountData glAccount: glAccounts) {
            addToaccountTypeToAccountNameMap(glAccount.getType().getValue(),glAccount.getName()+"-"+glAccount.getId()+"-"+glAccount.getTagId().getName()+"-"+glAccount.getTagId().getId());
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
        CellRangeAddressList accountTypeRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), ACCOUNT_TYPE_COL,ACCOUNT_TYPE_COL);
        CellRangeAddressList accountUsageRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), ACCOUNT_USAGE_COL,ACCOUNT_USAGE_COL);
        CellRangeAddressList manualEntriesAllowedRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), MANUAL_ENTRIES_ALLOWED_COL,MANUAL_ENTRIES_ALLOWED_COL);
        CellRangeAddressList parentRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), PARENT_COL,PARENT_COL);
        CellRangeAddressList tagRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(), TAG_COL,TAG_COL);

        DataValidationHelper validationHelper=new HSSFDataValidationHelper((HSSFSheet) chartOfAccountsSheet);
        setNames(chartOfAccountsSheet,accountTypesNoDuplicateslist);

        DataValidationConstraint accountTypeConstraint=validationHelper.createExplicitListConstraint(new  String[]{"ASSET","LIABILITY","EQUITY,INCOME,EXPENSE"});
        DataValidationConstraint accountUsageConstraint=validationHelper.createExplicitListConstraint(new String[]{"Detail,Header"});
        DataValidationConstraint booleanConstraint=validationHelper.createExplicitListConstraint(new String[]{"True","False"});
        //"VLOOKUP($A1,$A$2:$B"+ SpreadsheetVersion.EXCEL97.getLastRowIndex()+",2,TRUE)"
        DataValidationConstraint parentConstraint=validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"AccountName_\",$A1))");
        DataValidationConstraint tagConstraint=validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Tags_\",$A1))");

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

    private void setNames(Sheet chartOfAccountsSheet,List<String> accountTypesNoDuplicateslist) {
        Workbook chartOfAccountsWorkbook=chartOfAccountsSheet.getWorkbook();
        for (Integer i=0;i<accountTypesNoDuplicateslist.size();i++){
            Name tags=chartOfAccountsWorkbook.createName();
            Integer [] tagValueBeginEndIndexes=accountTypeToBeginEndIndexesofAccountNames.get(i);
            if(accountTypeToBeginEndIndexesofAccountNames!=null){
                tags.setNameName("Tags_"+accountTypesNoDuplicateslist.get(i));
                tags.setRefersToFormula("ChartOfAccounts!$S$"+tagValueBeginEndIndexes[0]+":$S$"+tagValueBeginEndIndexes[1]);
            }
            Name accountNames=chartOfAccountsWorkbook.createName();
            Integer [] accountNamesBeginEndIndexes=accountTypeToBeginEndIndexesofAccountNames.get(i);
            if (accountNamesBeginEndIndexes!=null){
                accountNames.setNameName("AccountName_"+accountTypesNoDuplicateslist.get(i));
                accountNames.setRefersToFormula("ChartOfAccounts!$Q$"+accountNamesBeginEndIndexes[0]+":$Q$"+accountNamesBeginEndIndexes[1]);
            }
        }
    }
    private void setDefaults(Sheet worksheet){
        try {
            for (Integer rowNo = 1; rowNo < 3000; rowNo++) {
                Row row = worksheet.getRow(rowNo);
                if (row == null)
                    row = worksheet.createRow(rowNo);
                writeFormula(PARENT_ID_COL, row,
                        "IF(ISERROR(VLOOKUP($E"+(rowNo+1)+",$Q$2:$R$"+(glAccounts.size()+1)+",2,FALSE)),\"\",(VLOOKUP($E"+(rowNo+1)+",$Q$2:$R$"+(glAccounts.size()+1)+",2,FALSE)))");
                writeFormula(TAG_ID_COL,row,"IF(ISERROR(VLOOKUP($H"+(rowNo+1)+",$S$2:$T$"+(glAccounts.size()+1)+",2,FALSE)),\"\",(VLOOKUP($H"+(rowNo+1)+",$S$2:$T$"+(glAccounts.size()+1)+",2,FALSE)))");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setLookupTable(Sheet chartOfAccountsSheet) {
        accountTypesNoDuplicateslist=new ArrayList<>();
        for (int i = 0; i <glAccounts.size() ; i++) {
            if (!accountTypesNoDuplicateslist.contains(glAccounts.get(i).getType().getValue())) {
                accountTypesNoDuplicateslist.add(glAccounts.get(i).getType().getValue());
            }
        }
        int rowIndex=1,startIndex=1,accountTypeIndex=0;
        accountTypeToBeginEndIndexesofAccountNames= new HashMap<Integer,Integer[]>();
        for (String accountType:accountTypesNoDuplicateslist) {
             startIndex=rowIndex+1;
             Row row =chartOfAccountsSheet.createRow(rowIndex);
             writeString(LOOKUP_ACCOUNT_TYPE_COL,row,accountType);
             List<String> accountNamesandTags =accountTypeToAccountNameAndTag.get(accountType);
             if (!accountNamesandTags.isEmpty()){
                 for (String accountNameandTag:accountNamesandTags) {
                     if (chartOfAccountsSheet.getRow(rowIndex)!=null){
                         String accountNameAndTagAr[]=accountNameandTag.split("-");
                         writeString(LOOKUP_ACCOUNT_NAME_COL,row,accountNameAndTagAr[0]);
                         writeString(LOOKUP_ACCOUNT_ID_COL,row,accountNameAndTagAr[1]);
                         writeString(LOOKUP_TAG_COL,row,accountNameAndTagAr[2]);
                         writeString(LOOKUP_TAG_ID_COL,row,accountNameAndTagAr[3]);
                         rowIndex++;
                     }else{
                         row =chartOfAccountsSheet.createRow(rowIndex);
                         String accountNameAndTagAr[]=accountNameandTag.split("-");
                         writeString(LOOKUP_ACCOUNT_NAME_COL,row,accountNameAndTagAr[0]);
                         writeString(LOOKUP_ACCOUNT_ID_COL,row,accountNameAndTagAr[1]);
                         writeString(LOOKUP_TAG_COL,row,accountNameAndTagAr[2]);
                         writeString(LOOKUP_TAG_ID_COL,row,accountNameAndTagAr[3]);
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
        Row rowHeader=chartOfAccountsSheet.createRow(0);
        chartOfAccountsSheet.setColumnWidth(ACCOUNT_TYPE_COL,4000);
        chartOfAccountsSheet.setColumnWidth(ACCOUNT_NAME_COL,4000);
        chartOfAccountsSheet.setColumnWidth(ACCOUNT_USAGE_COL,3000);
        chartOfAccountsSheet.setColumnWidth(MANUAL_ENTRIES_ALLOWED_COL,2000);
        chartOfAccountsSheet.setColumnWidth(PARENT_COL,6000);
        chartOfAccountsSheet.setColumnWidth(PARENT_ID_COL,3000);
        chartOfAccountsSheet.setColumnWidth(GL_CODE_COL,3000);
        chartOfAccountsSheet.setColumnWidth(TAG_COL,4000);
        chartOfAccountsSheet.setColumnWidth(TAG_ID_COL,3000);
        chartOfAccountsSheet.setColumnWidth(DESCRIPTION_COL,10000);
        chartOfAccountsSheet.setColumnWidth(LOOKUP_ACCOUNT_TYPE_COL,4000);
        chartOfAccountsSheet.setColumnWidth(LOOKUP_ACCOUNT_NAME_COL,6000);
        chartOfAccountsSheet.setColumnWidth(LOOKUP_ACCOUNT_ID_COL,6000);
        chartOfAccountsSheet.setColumnWidth(LOOKUP_TAG_COL,4000);
        chartOfAccountsSheet.setColumnWidth(LOOKUP_TAG_ID_COL,3000);
        
        writeString(ACCOUNT_TYPE_COL,rowHeader,"Account Type*");
        writeString(GL_CODE_COL,rowHeader,"GL Code *");
        writeString(ACCOUNT_USAGE_COL,rowHeader,"Account Usage *");
        writeString(MANUAL_ENTRIES_ALLOWED_COL,rowHeader,"Manual entries allowed *");
        writeString(PARENT_COL,rowHeader,"Parent");
        writeString(PARENT_ID_COL,rowHeader,"Parent Id");
        writeString(ACCOUNT_NAME_COL,rowHeader,"Account Name");
        writeString(TAG_COL,rowHeader,"Tag *");
        writeString(TAG_ID_COL,rowHeader,"Tag Id");
        writeString(DESCRIPTION_COL,rowHeader,"Description *");
        writeString(LOOKUP_ACCOUNT_TYPE_COL,rowHeader,"Lookup Account type");
        writeString(LOOKUP_TAG_COL,rowHeader,"Lookup Tag");
        writeString(LOOKUP_TAG_ID_COL,rowHeader,"Lookup Tag Id");
        writeString(LOOKUP_ACCOUNT_NAME_COL,rowHeader,"Lookup Account name *");
        writeString(LOOKUP_ACCOUNT_ID_COL,rowHeader,"Lookup Account Id");

    }
}
