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
package org.apache.fineract.infrastructure.bulkimport.populator.fixeddeposits;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.*;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FixedDepositTransactionWorkbookPopulator extends AbstractWorkbookPopulator {
    private OfficeSheetPopulator officeSheetPopulator;
    private ClientSheetPopulator clientSheetPopulator;
    private ExtrasSheetPopulator extrasSheetPopulator;

    private List<SavingsAccountData>savingsAccounts;

    public FixedDepositTransactionWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
            ClientSheetPopulator clientSheetPopulator, ExtrasSheetPopulator extrasSheetPopulator,
            List<SavingsAccountData> savingsAccounts) {
        this.officeSheetPopulator = officeSheetPopulator;
        this.clientSheetPopulator = clientSheetPopulator;
        this.extrasSheetPopulator = extrasSheetPopulator;
        this.savingsAccounts=savingsAccounts;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet savingsTransactionSheet = workbook.createSheet(TemplatePopulateImportConstants.FIXED_DEPOSIT_TRANSACTION_SHEET_NAME);
        setLayout(savingsTransactionSheet);
        officeSheetPopulator.populate(workbook,dateFormat);
        clientSheetPopulator.populate(workbook,dateFormat);
        extrasSheetPopulator.populate(workbook,dateFormat);
        populateSavingsTable(savingsTransactionSheet,dateFormat);
        setRules(savingsTransactionSheet,dateFormat);
        setDefaults(savingsTransactionSheet);
    }

    private void setDefaults(Sheet worksheet) {
        for(Integer rowNo = 1; rowNo < 3000; rowNo++)
        {
            Row row = worksheet.getRow(rowNo);
            if(row == null)
                row = worksheet.createRow(rowNo);
            writeFormula(TransactionConstants.PRODUCT_COL, row, "IF(ISERROR(VLOOKUP($C"+ (rowNo+1) +",$Q$2:$S$" + (savingsAccounts.size() + 1) + ",2,FALSE)),\"\",VLOOKUP($C"+ (rowNo+1) +",$Q$2:$S$" + (savingsAccounts.size() + 1) + ",2,FALSE))");
            writeFormula(TransactionConstants.OPENING_BALANCE_COL, row, "IF(ISERROR(VLOOKUP($C"+ (rowNo+1) +",$Q$2:$S$" + (savingsAccounts.size() + 1) + ",3,FALSE)),\"\",VLOOKUP($C"+ (rowNo+1) +",$Q$2:$S$" + (savingsAccounts.size() + 1) + ",3,FALSE))");
        }
    }


    private void setRules(Sheet worksheet,String dateFormat) {
        CellRangeAddressList officeNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                TransactionConstants.OFFICE_NAME_COL, TransactionConstants.OFFICE_NAME_COL);
        CellRangeAddressList clientNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                TransactionConstants.CLIENT_NAME_COL, TransactionConstants.CLIENT_NAME_COL);
        CellRangeAddressList accountNumberRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                TransactionConstants.SAVINGS_ACCOUNT_NO_COL, TransactionConstants.SAVINGS_ACCOUNT_NO_COL);
        CellRangeAddressList transactionTypeRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                TransactionConstants.TRANSACTION_TYPE_COL, TransactionConstants.TRANSACTION_TYPE_COL);
        CellRangeAddressList paymentTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                TransactionConstants.PAYMENT_TYPE_COL, TransactionConstants.PAYMENT_TYPE_COL);
        CellRangeAddressList transactionDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                TransactionConstants.TRANSACTION_DATE_COL, TransactionConstants.TRANSACTION_DATE_COL);

        DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet)worksheet);

        setNames(worksheet);

        DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
        DataValidationConstraint clientNameConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Client_\",$A1))");
        DataValidationConstraint accountNumberConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Account_\",SUBSTITUTE(SUBSTITUTE(SUBSTITUTE($B1,\" \",\"_\"),\"(\",\"_\"),\")\",\"_\")))");
        DataValidationConstraint transactionTypeConstraint = validationHelper.createExplicitListConstraint(new String[] {"Withdrawal","Deposit"});
        DataValidationConstraint paymentTypeConstraint = validationHelper.createFormulaListConstraint("PaymentTypes");
        DataValidationConstraint transactionDateConstraint = validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN, "=VLOOKUP($C1,$Q$2:$T$" + (savingsAccounts.size() + 1) + ",4,FALSE)", "=TODAY()", dateFormat);

        DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
        DataValidation clientValidation = validationHelper.createValidation(clientNameConstraint, clientNameRange);
        DataValidation accountNumberValidation = validationHelper.createValidation(accountNumberConstraint, accountNumberRange);
        DataValidation transactionTypeValidation = validationHelper.createValidation(transactionTypeConstraint, transactionTypeRange);
        DataValidation paymentTypeValidation = validationHelper.createValidation(paymentTypeConstraint, paymentTypeRange);
        DataValidation transactionDateValidation = validationHelper.createValidation(transactionDateConstraint, transactionDateRange);

        worksheet.addValidationData(officeValidation);
        worksheet.addValidationData(clientValidation);
        worksheet.addValidationData(accountNumberValidation);
        worksheet.addValidationData(transactionTypeValidation);
        worksheet.addValidationData(paymentTypeValidation);
        worksheet.addValidationData(transactionDateValidation);
    }

    private void setNames(Sheet worksheet) {
        Workbook savingsTransactionWorkbook = worksheet.getWorkbook();
        List<String> officeNames = officeSheetPopulator.getOfficeNames();

        //Office Names
        Name officeGroup = savingsTransactionWorkbook.createName();
        officeGroup.setNameName("Office");
        officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (officeNames.size() + 1));

        //Clients Named after Offices
        for(Integer i = 0; i < officeNames.size(); i++) {
            Integer[] officeNameToBeginEndIndexesOfClients = clientSheetPopulator.getOfficeNameToBeginEndIndexesOfClients().get(i);
            Name name = savingsTransactionWorkbook.createName();
            if(officeNameToBeginEndIndexesOfClients != null) {
                name.setNameName("Client_" + officeNames.get(i).trim().replaceAll("[ )(]", "_"));
                name.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfClients[0] + ":$B$" + officeNameToBeginEndIndexesOfClients[1]);
            }
        }

        //Counting clients with active savings and starting and end addresses of cells for naming
        HashMap<String, Integer[]> clientNameToBeginEndIndexes = new HashMap<>();
        ArrayList<String> clientsWithActiveSavings = new ArrayList<>();
        ArrayList<Long> clientIdsWithActiveSavings = new ArrayList<>();
        int startIndex = 1, endIndex = 1;
        String clientName = "";
        Long clientId = null;
        for(int i = 0; i < savingsAccounts.size(); i++){
            if(!clientName.equals(savingsAccounts.get(i).getClientName())) {
                endIndex = i + 1;
                clientNameToBeginEndIndexes.put(clientName, new Integer[]{startIndex, endIndex});
                startIndex = i + 2;
                clientName = savingsAccounts.get(i).getClientName();
                clientId = savingsAccounts.get(i).getClientId();
                clientsWithActiveSavings.add(clientName);
                clientIdsWithActiveSavings.add(clientId);
            }
            if(i == savingsAccounts.size()-1) {
                endIndex = i + 2;
                clientNameToBeginEndIndexes.put(clientName, new Integer[]{startIndex, endIndex});
            }
        }

        //Account Number Named  after Clients
        for(int j = 0; j < clientsWithActiveSavings.size(); j++) {
            Name name = savingsTransactionWorkbook.createName();
            name.setNameName("Account_" + clientsWithActiveSavings.get(j).replaceAll(" ", "_") + "_" + clientIdsWithActiveSavings.get(j) + "_");
            name.setRefersToFormula(TemplatePopulateImportConstants.FIXED_DEPOSIT_TRANSACTION_SHEET_NAME+"!$Q$" + clientNameToBeginEndIndexes.get(clientsWithActiveSavings.get(j))[0] + ":$Q$" + clientNameToBeginEndIndexes.get(clientsWithActiveSavings.get(j))[1]);
        }

        //Payment Type Name
        Name paymentTypeGroup = savingsTransactionWorkbook.createName();
        paymentTypeGroup.setNameName("PaymentTypes");
        paymentTypeGroup.setRefersToFormula(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME+"!$D$2:$D$" + (extrasSheetPopulator.getPaymentTypesSize() + 1));
    }

    private void populateSavingsTable(Sheet savingsTransactionSheet,String dateFormat) {
        Workbook workbook = savingsTransactionSheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();
        short df = workbook.createDataFormat().getFormat(dateFormat);
        dateCellStyle.setDataFormat(df);
        int rowIndex = 1;
        Row row;
        Collections.sort(savingsAccounts, SavingsAccountData.ClientNameComparator);
        for(SavingsAccountData savingsAccount : savingsAccounts) {
            row = savingsTransactionSheet.createRow(rowIndex++);
            writeString(TransactionConstants.LOOKUP_CLIENT_NAME_COL, row, savingsAccount.getClientName()  + "(" + savingsAccount.getClientId() + ")");
            writeLong(TransactionConstants.LOOKUP_ACCOUNT_NO_COL, row, Long.parseLong(savingsAccount.getAccountNo()));
            writeString(TransactionConstants.LOOKUP_PRODUCT_COL, row, savingsAccount.getSavingsProductName());
            if(savingsAccount.getMinRequiredOpeningBalance() != null)
                writeBigDecimal(TransactionConstants.LOOKUP_OPENING_BALANCE_COL, row, savingsAccount.getMinRequiredOpeningBalance());
            writeDate(TransactionConstants.LOOKUP_SAVINGS_ACTIVATION_DATE_COL, row,"" +
                    savingsAccount.getTimeline().getActivatedOnDate().getDayOfMonth() + "/"
                    + savingsAccount.getTimeline().getActivatedOnDate().getMonthOfYear() + "/"
                    + savingsAccount.getTimeline().getActivatedOnDate().getYear() , dateCellStyle,dateFormat);
        }
    }

    private void setLayout(Sheet worksheet) {
        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
        worksheet.setColumnWidth(TransactionConstants.OFFICE_NAME_COL, 4000);
        worksheet.setColumnWidth(TransactionConstants.CLIENT_NAME_COL, 5000);
        worksheet.setColumnWidth(TransactionConstants.SAVINGS_ACCOUNT_NO_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.PRODUCT_COL, 4000);
        worksheet.setColumnWidth(TransactionConstants.OPENING_BALANCE_COL, 4000);
        worksheet.setColumnWidth(TransactionConstants.TRANSACTION_TYPE_COL, 3300);
        worksheet.setColumnWidth(TransactionConstants.AMOUNT_COL, 4000);
        worksheet.setColumnWidth(TransactionConstants.TRANSACTION_DATE_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.PAYMENT_TYPE_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.ACCOUNT_NO_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.CHECK_NO_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.RECEIPT_NO_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.ROUTING_CODE_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.BANK_NO_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.LOOKUP_CLIENT_NAME_COL, 5000);
        worksheet.setColumnWidth(TransactionConstants.LOOKUP_ACCOUNT_NO_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.LOOKUP_PRODUCT_COL, 3000);
        worksheet.setColumnWidth(TransactionConstants.LOOKUP_OPENING_BALANCE_COL, 3700);
        worksheet.setColumnWidth(TransactionConstants.LOOKUP_SAVINGS_ACTIVATION_DATE_COL, 3500);
        writeString(TransactionConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
        writeString(TransactionConstants.CLIENT_NAME_COL, rowHeader, "Client Name*");
        writeString(TransactionConstants.SAVINGS_ACCOUNT_NO_COL, rowHeader, "Account No.*");
        writeString(TransactionConstants.PRODUCT_COL, rowHeader, "Product Name");
        writeString(TransactionConstants.OPENING_BALANCE_COL, rowHeader, "Opening Balance");
        writeString(TransactionConstants.TRANSACTION_TYPE_COL, rowHeader, "Transaction Type*");
        writeString(TransactionConstants.AMOUNT_COL, rowHeader, "Amount*");
        writeString(TransactionConstants.TRANSACTION_DATE_COL, rowHeader, "Date*");
        writeString(TransactionConstants.PAYMENT_TYPE_COL, rowHeader, "Type*");
        writeString(TransactionConstants.ACCOUNT_NO_COL, rowHeader, "Account No");
        writeString(TransactionConstants.CHECK_NO_COL, rowHeader, "Check No");
        writeString(TransactionConstants.RECEIPT_NO_COL, rowHeader, "Receipt No");
        writeString(TransactionConstants.ROUTING_CODE_COL, rowHeader, "Routing Code");
        writeString(TransactionConstants.BANK_NO_COL, rowHeader, "Bank No");
        writeString(TransactionConstants.LOOKUP_CLIENT_NAME_COL, rowHeader, "Lookup Client");
        writeString(TransactionConstants.LOOKUP_ACCOUNT_NO_COL, rowHeader, "Lookup Account");
        writeString(TransactionConstants.LOOKUP_PRODUCT_COL, rowHeader, "Lookup Product");
        writeString(TransactionConstants.LOOKUP_OPENING_BALANCE_COL, rowHeader, "Lookup Opening Balance");
        writeString(TransactionConstants.LOOKUP_SAVINGS_ACTIVATION_DATE_COL, rowHeader, "Lookup Savings Activation Date");
    }
}
