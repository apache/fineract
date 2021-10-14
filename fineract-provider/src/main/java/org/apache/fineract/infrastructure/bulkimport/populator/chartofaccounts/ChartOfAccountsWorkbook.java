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

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.infrastructure.bulkimport.constants.ChartOfAcountsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartOfAccountsWorkbook extends AbstractWorkbookPopulator {

    private static final Logger LOG = LoggerFactory.getLogger(ChartOfAccountsWorkbook.class);
    private final List<GLAccountData> glAccounts;
    private final List<OfficeData> offices; // adding opening balance office tag
    private List<CurrencyData> currencies; // adding opening balance currency code
    private Map<String, List<String>> accountTypeToAccountNameAndTag;
    private Map<Integer, Integer[]> accountTypeToBeginEndIndexesofAccountNames;
    private List<String> accountTypesNoDuplicatesList;

    public ChartOfAccountsWorkbook(List<GLAccountData> glAccounts, List<OfficeData> offices, List<CurrencyData> currencies) {
        this.glAccounts = glAccounts;
        this.offices = offices; // opening balance offices names
        this.currencies = currencies; // opening balance currency codes
    }

    @Override
    public void populate(Workbook workbook, String dateFormat) {
        Sheet chartOfAccountsSheet = workbook.createSheet(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME);
        setLayout(chartOfAccountsSheet);
        setAccountTypeToAccountNameAndTag();
        setLookupTable(chartOfAccountsSheet);
        setRules(chartOfAccountsSheet);
        setDefaults(chartOfAccountsSheet);

    }

    private void setAccountTypeToAccountNameAndTag() {
        accountTypeToAccountNameAndTag = new HashMap<>();
        for (GLAccountData glAccount : glAccounts) {
            addToaccountTypeToAccountNameMap(glAccount.getType().getValue(), glAccount.getName() + "-" + glAccount.getId() + "-"
                    + glAccount.getTagId().getName() + "-" + glAccount.getTagId().getId());
        }
    }

    private void addToaccountTypeToAccountNameMap(String key, String value) {
        List<String> values = accountTypeToAccountNameAndTag.get(key);
        if (values == null) {
            values = new ArrayList<>();
        }
        if (!values.contains(value)) {
            values.add(value);
            accountTypeToAccountNameAndTag.put(key, values);
        }
    }

    private String[] getCurrency() {
        String[] currencyCode = new String[currencies.size()];
        int currencyIndex = 0;
        for (CurrencyData currencies : currencies) {
            currencyCode[currencyIndex] = currencies.code();
            currencyIndex++;
        }
        return currencyCode;
    }

    private void setRules(Sheet chartOfAccountsSheet) {
        CellRangeAddressList accountTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.ACCOUNT_TYPE_COL, ChartOfAcountsConstants.ACCOUNT_TYPE_COL);
        CellRangeAddressList accountUsageRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.ACCOUNT_USAGE_COL, ChartOfAcountsConstants.ACCOUNT_USAGE_COL);
        CellRangeAddressList manualEntriesAllowedRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL, ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL);
        CellRangeAddressList parentRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.PARENT_COL, ChartOfAcountsConstants.PARENT_COL);
        CellRangeAddressList tagRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.TAG_COL, ChartOfAcountsConstants.TAG_COL);
        CellRangeAddressList officeNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.OFFICE_COL, ChartOfAcountsConstants.OFFICE_COL); // validation for opening bal
                                                                                         // office column
        CellRangeAddressList currencyCodeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                ChartOfAcountsConstants.CURRENCY_CODE, ChartOfAcountsConstants.CURRENCY_CODE);// validation for currency
                                                                                              // code for opening
                                                                                              // balance

        DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) chartOfAccountsSheet);
        setNames(chartOfAccountsSheet, accountTypesNoDuplicatesList, offices);

        DataValidationConstraint accountTypeConstraint = validationHelper
                .createExplicitListConstraint(new String[] { GLAccountType.ASSET.toString(), GLAccountType.LIABILITY.toString(),
                        GLAccountType.EQUITY.toString(), GLAccountType.INCOME.toString(), GLAccountType.EXPENSE.toString() });
        DataValidationConstraint accountUsageConstraint = validationHelper
                .createExplicitListConstraint(new String[] { GLAccountUsage.DETAIL.toString(), GLAccountUsage.HEADER.toString() });
        DataValidationConstraint booleanConstraint = validationHelper.createExplicitListConstraint(new String[] { "True", "False" });
        DataValidationConstraint parentConstraint = validationHelper
                .createFormulaListConstraint("INDIRECT(CONCATENATE(\"AccountName_\",$A1))");
        DataValidationConstraint tagConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Tags_\",$A1))");
        DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
        DataValidationConstraint currencyCodeConstraint = validationHelper.createExplicitListConstraint(getCurrency());

        DataValidation accountTypeValidation = validationHelper.createValidation(accountTypeConstraint, accountTypeRange);
        DataValidation accountUsageValidation = validationHelper.createValidation(accountUsageConstraint, accountUsageRange);
        DataValidation manualEntriesValidation = validationHelper.createValidation(booleanConstraint, manualEntriesAllowedRange);
        DataValidation parentValidation = validationHelper.createValidation(parentConstraint, parentRange);
        DataValidation tagValidation = validationHelper.createValidation(tagConstraint, tagRange);
        DataValidation officeNameValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
        DataValidation currencyCodeValidation = validationHelper.createValidation(currencyCodeConstraint, currencyCodeRange);

        chartOfAccountsSheet.addValidationData(accountTypeValidation);
        chartOfAccountsSheet.addValidationData(accountUsageValidation);
        chartOfAccountsSheet.addValidationData(manualEntriesValidation);
        chartOfAccountsSheet.addValidationData(parentValidation);
        chartOfAccountsSheet.addValidationData(tagValidation);
        chartOfAccountsSheet.addValidationData(officeNameValidation);
        chartOfAccountsSheet.addValidationData(currencyCodeValidation);
    }

    private void setNames(Sheet chartOfAccountsSheet, List<String> accountTypesNoDuplicatesList, List<OfficeData> offices) {
        Workbook chartOfAccountsWorkbook = chartOfAccountsSheet.getWorkbook();
        for (Integer i = 0; i < accountTypesNoDuplicatesList.size(); i++) {
            Name tags = chartOfAccountsWorkbook.createName();
            Integer[] tagValueBeginEndIndexes = accountTypeToBeginEndIndexesofAccountNames.get(i);
            if (accountTypeToBeginEndIndexesofAccountNames != null) {
                setSanitized(tags, "Tags_" + accountTypesNoDuplicatesList.get(i));
                tags.setRefersToFormula(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME + "!$V$" + tagValueBeginEndIndexes[0]
                        + ":$V$" + tagValueBeginEndIndexes[1]);
            }
            Name accountNames = chartOfAccountsWorkbook.createName();
            Integer[] accountNamesBeginEndIndexes = accountTypeToBeginEndIndexesofAccountNames.get(i);
            if (accountNamesBeginEndIndexes != null) {
                setSanitized(accountNames, "AccountName_" + accountTypesNoDuplicatesList.get(i));
                accountNames.setRefersToFormula(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME + "!$T$"
                        + accountNamesBeginEndIndexes[0] + ":$T$" + accountNamesBeginEndIndexes[1]);
            }
        }
        Name officeGroup = chartOfAccountsWorkbook.createName();
        officeGroup.setNameName("Office");
        officeGroup.setRefersToFormula(TemplatePopulateImportConstants.CHART_OF_ACCOUNTS_SHEET_NAME + "!$X$2:$X$" + (offices.size() + 1));

    }

    private void setDefaults(Sheet worksheet) {
        try {
            for (Integer rowNo = 1; rowNo < 3000; rowNo++) {
                Row row = worksheet.getRow(rowNo);
                if (row == null) {
                    row = worksheet.createRow(rowNo);
                }
                writeFormula(ChartOfAcountsConstants.PARENT_ID_COL, row,
                        "IF(ISERROR(VLOOKUP($E" + (rowNo + 1) + ",$T$2:$U$" + (glAccounts.size() + 1) + ",2,FALSE))," + "\"\",(VLOOKUP($E"
                                + (rowNo + 1) + ",$T$2:$U$" + (glAccounts.size() + 1) + ",2,FALSE)))");
                writeFormula(ChartOfAcountsConstants.TAG_ID_COL, row,
                        "IF(ISERROR(VLOOKUP($H" + (rowNo + 1) + ",$V$2:$W$" + (glAccounts.size() + 1) + ",2,FALSE))," + "\"\",(VLOOKUP($H"
                                + (rowNo + 1) + ",$V$2:$W$" + (glAccounts.size() + 1) + ",2,FALSE)))");
                // auto populate office id for bulk import of opening balance
                writeFormula(ChartOfAcountsConstants.OFFICE_COL_ID, row,
                        "IF(ISERROR(VLOOKUP($K" + (rowNo + 1) + ",$X$2:$Y$" + (offices.size() + 1) + ",2,FALSE)),\"\",(VLOOKUP($K"
                                + (rowNo + 1) + ",$X$2:$Y$" + (offices.size() + 1) + ",2,FALSE)))");
            }
        } catch (Exception e) {
            LOG.error("Problem occurred in setDefaults function", e);
        }
    }

    private void setLookupTable(Sheet chartOfAccountsSheet) {
        accountTypesNoDuplicatesList = new ArrayList<>();
        for (GLAccountData glAccount : glAccounts) {
            if (!accountTypesNoDuplicatesList.contains(glAccount.getType().getValue())) {
                accountTypesNoDuplicatesList.add(glAccount.getType().getValue());
            }
        }
        int rowIndex = 1;
        int startIndex = 1;
        int accountTypeIndex = 0;
        accountTypeToBeginEndIndexesofAccountNames = new HashMap<>();
        for (String accountType : accountTypesNoDuplicatesList) {
            startIndex = rowIndex + 1;
            Row row = chartOfAccountsSheet.createRow(rowIndex);
            writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_TYPE_COL, row, accountType);
            List<String> accountNamesandTags = accountTypeToAccountNameAndTag.get(accountType);
            if (!accountNamesandTags.isEmpty()) {
                for (String accountNameandTag : accountNamesandTags) {
                    if (chartOfAccountsSheet.getRow(rowIndex) != null) {
                        List<String> accountNameAndTagAr = Splitter.on('-').splitToList(accountNameandTag);
                        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_NAME_COL, row, accountNameAndTagAr.get(0));
                        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_ID_COL, row, accountNameAndTagAr.get(1));
                        writeString(ChartOfAcountsConstants.LOOKUP_TAG_COL, row, accountNameAndTagAr.get(2));
                        writeString(ChartOfAcountsConstants.LOOKUP_TAG_ID_COL, row, accountNameAndTagAr.get(3));
                        rowIndex++;
                    } else {
                        row = chartOfAccountsSheet.createRow(rowIndex);
                        List<String> accountNameAndTagAr = Splitter.on('-').splitToList(accountNameandTag);
                        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_NAME_COL, row, accountNameAndTagAr.get(0));
                        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_ID_COL, row, accountNameAndTagAr.get(1));
                        writeString(ChartOfAcountsConstants.LOOKUP_TAG_COL, row, accountNameAndTagAr.get(2));
                        writeString(ChartOfAcountsConstants.LOOKUP_TAG_ID_COL, row, accountNameAndTagAr.get(3));
                        rowIndex++;
                    }
                }
                accountTypeToBeginEndIndexesofAccountNames.put(accountTypeIndex++, new Integer[] { startIndex, rowIndex });
            } else {
                accountTypeIndex++;
            }
        }
        // opening balance lookup table of offices
        startIndex = 1;
        rowIndex = 1;
        for (OfficeData office : offices) {
            startIndex = rowIndex + 1;
            if (chartOfAccountsSheet.getRow(rowIndex) != null) {
                Row row = chartOfAccountsSheet.getRow(rowIndex);
                writeString(ChartOfAcountsConstants.LOOKUP_OFFICE_COL, row, office.name());
                writeLong(ChartOfAcountsConstants.LOOKUP_OFFICE_ID_COL, row, office.getId());
                rowIndex++;

            } else {
                Row row = chartOfAccountsSheet.createRow(rowIndex);
                writeString(ChartOfAcountsConstants.LOOKUP_OFFICE_COL, row, office.name());
                writeLong(ChartOfAcountsConstants.LOOKUP_OFFICE_ID_COL, row, office.getId());
                rowIndex++;
            }

        }
    }

    private void setLayout(Sheet chartOfAccountsSheet) {

        Row rowHeader = chartOfAccountsSheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.ACCOUNT_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.ACCOUNT_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.ACCOUNT_USAGE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL,
                TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.PARENT_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.PARENT_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.GL_CODE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.TAG_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.TAG_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.DESCRIPTION_COL, TemplatePopulateImportConstants.EXTRALARGE_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.OFFICE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.OFFICE_COL_ID, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.CURRENCY_CODE, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.DEBIT_AMOUNT, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.CREDIT_AMOUNT, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_ACCOUNT_TYPE_COL,
                TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_ACCOUNT_NAME_COL,
                TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_ACCOUNT_ID_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_TAG_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_TAG_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        // adding lookup for opening balance bulk import
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_OFFICE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        chartOfAccountsSheet.setColumnWidth(ChartOfAcountsConstants.LOOKUP_OFFICE_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);

        writeString(ChartOfAcountsConstants.ACCOUNT_TYPE_COL, rowHeader, "Account Type*");
        writeString(ChartOfAcountsConstants.GL_CODE_COL, rowHeader, "GL Code *");
        writeString(ChartOfAcountsConstants.ACCOUNT_USAGE_COL, rowHeader, "Account Usage *");
        writeString(ChartOfAcountsConstants.MANUAL_ENTRIES_ALLOWED_COL, rowHeader, "Manual entries allowed *");
        writeString(ChartOfAcountsConstants.PARENT_COL, rowHeader, "Parent");
        writeString(ChartOfAcountsConstants.PARENT_ID_COL, rowHeader, "Parent Id");
        writeString(ChartOfAcountsConstants.ACCOUNT_NAME_COL, rowHeader, "Account Name");
        writeString(ChartOfAcountsConstants.TAG_COL, rowHeader, "Tag");
        writeString(ChartOfAcountsConstants.TAG_ID_COL, rowHeader, "Tag Id");
        writeString(ChartOfAcountsConstants.DESCRIPTION_COL, rowHeader, "Description *");
        // adding data for opening balance bulk import
        writeString(ChartOfAcountsConstants.OFFICE_COL, rowHeader, "Parent Office for Opening Balance");
        writeString(ChartOfAcountsConstants.OFFICE_COL_ID, rowHeader, "Parent Office Code Opening Balance");
        writeString(ChartOfAcountsConstants.CURRENCY_CODE, rowHeader, "Currency Code");
        writeString(ChartOfAcountsConstants.DEBIT_AMOUNT, rowHeader, "Debit Amount");
        writeString(ChartOfAcountsConstants.CREDIT_AMOUNT, rowHeader, "Credit Amount");

        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_TYPE_COL, rowHeader, "Lookup Account type");
        writeString(ChartOfAcountsConstants.LOOKUP_TAG_COL, rowHeader, "Lookup Tag");
        writeString(ChartOfAcountsConstants.LOOKUP_TAG_ID_COL, rowHeader, "Lookup Tag Id");
        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_NAME_COL, rowHeader, "Lookup Account name *");
        writeString(ChartOfAcountsConstants.LOOKUP_ACCOUNT_ID_COL, rowHeader, "Lookup Account Id");
        // adding lookup for opening balance bulk import
        writeString(ChartOfAcountsConstants.LOOKUP_OFFICE_COL, rowHeader, "Lookup Office Name");
        writeString(ChartOfAcountsConstants.LOOKUP_OFFICE_ID_COL, rowHeader, "Lookup Office Id");

    }

}
