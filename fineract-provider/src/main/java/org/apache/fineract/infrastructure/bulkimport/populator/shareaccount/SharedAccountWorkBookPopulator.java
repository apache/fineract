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
package org.apache.fineract.infrastructure.bulkimport.populator.shareaccount;

import org.apache.fineract.infrastructure.bulkimport.constants.SharedAccountsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ClientSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.SavingsAccountSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.SharedProductsSheetPopulator;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class SharedAccountWorkBookPopulator extends AbstractWorkbookPopulator {

    private  SharedProductsSheetPopulator sharedProductsSheetPopulator;
    private  ClientSheetPopulator clientSheetPopulator;
    private  SavingsAccountSheetPopulator savingsAccountSheetPopulator;

    public SharedAccountWorkBookPopulator(SharedProductsSheetPopulator sharedProductsSheetPopulator,
            ClientSheetPopulator clientSheetPopulator,SavingsAccountSheetPopulator savingsAccountSheetPopulator) {
        this.sharedProductsSheetPopulator=sharedProductsSheetPopulator;
        this.clientSheetPopulator=clientSheetPopulator;
        this.savingsAccountSheetPopulator=savingsAccountSheetPopulator;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet sharedAccountSheet= workbook.createSheet(TemplatePopulateImportConstants.SHARED_ACCOUNTS_SHEET_NAME);
        sharedProductsSheetPopulator.populate(workbook,dateFormat);
        clientSheetPopulator.populate(workbook,dateFormat);
        savingsAccountSheetPopulator.populate(workbook,dateFormat);
        setLayout(sharedAccountSheet);
        setRules(sharedAccountSheet,dateFormat);
        setDefaults(sharedAccountSheet);
    }

    private void setDefaults(Sheet sharedAccountSheet) {
        for (Integer rowNo=1;rowNo<3000;rowNo++){
            Row row=sharedAccountSheet.createRow(rowNo);
            writeFormula(SharedAccountsConstants.CURRENCY_COL,row,"IF(ISERROR(INDIRECT(CONCATENATE(\"CURRENCY_\",$B" + (rowNo + 1)
                    + "))),\"\",INDIRECT(CONCATENATE(\"CURRENCY_\",$B" + (rowNo + 1) + ")))");
            writeFormula(SharedAccountsConstants.DECIMAL_PLACES_COL,row,"IF(ISERROR(INDIRECT(CONCATENATE(\"DECIMAL_PLACES_\",$B" + (rowNo + 1)
                    + "))),\"\",INDIRECT(CONCATENATE(\"DECIMAL_PLACES_\",$B" + (rowNo + 1) + ")))");
            writeFormula(SharedAccountsConstants.TODAYS_PRICE_COL,row,"IF(ISERROR(INDIRECT(CONCATENATE(\"TODAYS_PRICE_\",$B" + (rowNo + 1)
                    + "))),\"\",INDIRECT(CONCATENATE(\"TODAYS_PRICE_\",$B" + (rowNo + 1) + ")))");
            writeFormula(SharedAccountsConstants.CURRENCY_IN_MULTIPLES_COL,row,"IF(ISERROR(INDIRECT(CONCATENATE(\"CURRENCY_IN_MULTIPLES_\",$B" + (rowNo + 1)
                    + "))),\"\",INDIRECT(CONCATENATE(\"CURRENCY_IN_MULTIPLES_\",$B" + (rowNo + 1) + ")))");
            writeFormula(SharedAccountsConstants.CHARGES_NAME_1_COL,row,"IF(ISERROR(INDIRECT(CONCATENATE(\"CHARGES_NAME_1_\",$B" + (rowNo + 1)
                + "))),\"\",INDIRECT(CONCATENATE(\"CHARGES_NAME_1_\",$B" + (rowNo + 1) + ")))");
            writeFormula(SharedAccountsConstants.CHARGES_NAME_2_COL,row,"IF(ISERROR(INDIRECT(CONCATENATE(\"CHARGES_NAME_2_\",$B" + (rowNo + 1)
                    + "))),\"\",INDIRECT(CONCATENATE(\"CHARGES_NAME_2_\",$B" + (rowNo + 1) + ")))");
            writeFormula(SharedAccountsConstants.CHARGES_NAME_3_COL,row,"IF(ISERROR(INDIRECT(CONCATENATE(\"CHARGES_NAME_3_\",$B" + (rowNo + 1)
                    + "))),\"\",INDIRECT(CONCATENATE(\"CHARGES_NAME_3_\",$B" + (rowNo + 1) + ")))");
    }
    }

    private void setRules(Sheet sharedAccountSheet,String dateFormat) {
        CellRangeAddressList clientNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SharedAccountsConstants.CLIENT_NAME_COL,SharedAccountsConstants.CLIENT_NAME_COL);
        CellRangeAddressList productRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SharedAccountsConstants.PRODUCT_COL,SharedAccountsConstants.PRODUCT_COL);
        CellRangeAddressList submittedDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SharedAccountsConstants.SUBMITTED_ON_COL, SharedAccountsConstants.SUBMITTED_ON_COL);
        CellRangeAddressList lockingFrequencyTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SharedAccountsConstants.LOCK_IN_PERIOD_FREQUENCY_TYPE,SharedAccountsConstants. LOCK_IN_PERIOD_FREQUENCY_TYPE);
        CellRangeAddressList applicationDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SharedAccountsConstants.APPLICATION_DATE_COL, SharedAccountsConstants.APPLICATION_DATE_COL);
        CellRangeAddressList allowDividendCalcRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SharedAccountsConstants.ALLOW_DIVIDEND_CALCULATION_FOR_INACTIVE_CLIENTS_COL, SharedAccountsConstants.ALLOW_DIVIDEND_CALCULATION_FOR_INACTIVE_CLIENTS_COL);

        DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) sharedAccountSheet);
        setNames(sharedAccountSheet);

        DataValidationConstraint clientNameConstraint = validationHelper.createFormulaListConstraint("Clients");
        DataValidationConstraint productNameConstraint = validationHelper.createFormulaListConstraint("Products");
        DataValidationConstraint dateConstraint = validationHelper.createDateConstraint
                (DataValidationConstraint.OperatorType.LESS_OR_EQUAL,"=TODAY()",null, dateFormat);
        DataValidationConstraint frequencyConstraint = validationHelper.createExplicitListConstraint(new String[] {
                        TemplatePopulateImportConstants.FREQUENCY_DAYS,
                        TemplatePopulateImportConstants.FREQUENCY_WEEKS,
                        TemplatePopulateImportConstants.FREQUENCY_MONTHS,
                        TemplatePopulateImportConstants.FREQUENCY_YEARS });
        DataValidationConstraint booleanConstraint=validationHelper.createExplicitListConstraint(new String[]{"True","False"});
        DataValidation clientValidation = validationHelper.createValidation(clientNameConstraint, clientNameRange);
        DataValidation productValidation=validationHelper.createValidation(productNameConstraint,productRange);
        DataValidation submittedOnValidation=validationHelper.createValidation(dateConstraint,submittedDateRange);
        DataValidation frequencyValidation=validationHelper.createValidation(frequencyConstraint,lockingFrequencyTypeRange);
        DataValidation applicationDateValidation=validationHelper.createValidation(dateConstraint,applicationDateRange);
        DataValidation allowDividendValidation=validationHelper.createValidation(booleanConstraint,allowDividendCalcRange);

        sharedAccountSheet.addValidationData(clientValidation);
        sharedAccountSheet.addValidationData(productValidation);
        sharedAccountSheet.addValidationData(submittedOnValidation);
        sharedAccountSheet.addValidationData(frequencyValidation);
        sharedAccountSheet.addValidationData(applicationDateValidation);
        sharedAccountSheet.addValidationData(allowDividendValidation);

    }

    private void setNames(Sheet sharedAccountSheet) {
        List<ClientData> clients=clientSheetPopulator.getClients();
        List<ShareProductData> products=sharedProductsSheetPopulator.getSharedProductDataList();
        Workbook sharedAccountWorkbook=sharedAccountSheet.getWorkbook();

        Name clientsGroup=sharedAccountWorkbook.createName();
        clientsGroup.setNameName("Clients");
        clientsGroup.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_SHEET_NAME+"!$B$2:$B$"+clients.size()+1);

        Name productGroup=sharedAccountWorkbook.createName();
        productGroup.setNameName("Products");
        productGroup.setRefersToFormula(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME+"!$B$2:$B$"+products.size()+1);

        for (Integer i=0;i<products.size();i++) {
            Name currecyName=sharedAccountWorkbook.createName();
            Name decimalPlacesName=sharedAccountWorkbook.createName();
            Name todaysPriceName=sharedAccountWorkbook.createName();
            Name currencyInMultiplesName=sharedAccountWorkbook.createName();
            Name chargesName1=sharedAccountWorkbook.createName();
            Name chargesName2=sharedAccountWorkbook.createName();
            Name chargesName3=sharedAccountWorkbook.createName();

            String productName=products.get(i).getName().replaceAll("[ ]", "_");

            currecyName.setNameName("CURRENCY_"+productName);
            currecyName.setRefersToFormula(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME+"!$C$"+(i+2));

            decimalPlacesName.setNameName("DECIMAL_PLACES_"+productName);
            decimalPlacesName.setRefersToFormula(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME+"!$D$"+(i+2));

            todaysPriceName.setNameName("TODAYS_PRICE_"+productName);
            todaysPriceName.setRefersToFormula(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME+"!$E$"+(i+2));

            currencyInMultiplesName.setNameName("CURRENCY_IN_MULTIPLES_"+productName);
            currencyInMultiplesName.setRefersToFormula(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME+"!$F$"+(i+2));

            chargesName1.setNameName("CHARGES_NAME_1_"+productName);
            chargesName1.setRefersToFormula(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME+"!$I$"+(i+2));

            chargesName2.setNameName("CHARGES_NAME_2_"+productName);
            chargesName2.setRefersToFormula(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME+"!$K$"+(i+2));

            chargesName3.setNameName("CHARGES_NAME_3_"+productName);
            chargesName3.setRefersToFormula(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME+"!$M$"+(i+2));
        }

    }

    private void setLayout(Sheet worksheet) {
        Row rowHeader=worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);

        worksheet.setColumnWidth(SharedAccountsConstants.CLIENT_NAME_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.CLIENT_NAME_COL,rowHeader,"Client Name *");

        worksheet.setColumnWidth(SharedAccountsConstants.PRODUCT_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.PRODUCT_COL,rowHeader,"Shared Product *");

        worksheet.setColumnWidth(SharedAccountsConstants.SUBMITTED_ON_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.SUBMITTED_ON_COL,rowHeader,"Submitted On Date *");

        worksheet.setColumnWidth(SharedAccountsConstants.EXTERNAL_ID_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.EXTERNAL_ID_COL,rowHeader,"External Id ");

        worksheet.setColumnWidth(SharedAccountsConstants.CURRENCY_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(SharedAccountsConstants.CURRENCY_COL,rowHeader,"Currency ");

        worksheet.setColumnWidth(SharedAccountsConstants.DECIMAL_PLACES_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.DECIMAL_PLACES_COL,rowHeader,"Decimal places ");

        worksheet.setColumnWidth(SharedAccountsConstants.TOTAL_NO_SHARES_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(SharedAccountsConstants.TOTAL_NO_SHARES_COL,rowHeader,"Total No. of Shares *");

        worksheet.setColumnWidth(SharedAccountsConstants.TODAYS_PRICE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.TODAYS_PRICE_COL,rowHeader,"Today's price *");

        worksheet.setColumnWidth(SharedAccountsConstants.CURRENCY_IN_MULTIPLES_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(SharedAccountsConstants.CURRENCY_IN_MULTIPLES_COL,rowHeader,"Currency in multiples ");

        worksheet.setColumnWidth(SharedAccountsConstants.DEFAULT_SAVINGS_AC_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.DEFAULT_SAVINGS_AC_COL,rowHeader,"Savings account *");

        worksheet.setColumnWidth(SharedAccountsConstants.MINIMUM_ACTIVE_PERIOD_IN_DAYS_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(SharedAccountsConstants.MINIMUM_ACTIVE_PERIOD_IN_DAYS_COL,rowHeader,"Minimum active period(in days) ");

        worksheet.setColumnWidth(SharedAccountsConstants.LOCK_IN_PERIOD_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.LOCK_IN_PERIOD_COL,rowHeader,"Lock in period ");

        worksheet.setColumnWidth(SharedAccountsConstants.LOCK_IN_PERIOD_FREQUENCY_TYPE,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(SharedAccountsConstants.LOCK_IN_PERIOD_FREQUENCY_TYPE,rowHeader,"Lock in Period Frequency ");

        worksheet.setColumnWidth(SharedAccountsConstants.APPLICATION_DATE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.APPLICATION_DATE_COL,rowHeader,"Application Date *");

        worksheet.setColumnWidth(SharedAccountsConstants.ALLOW_DIVIDEND_CALCULATION_FOR_INACTIVE_CLIENTS_COL,TemplatePopulateImportConstants.LARGE_COL_SIZE);
        writeString(SharedAccountsConstants.ALLOW_DIVIDEND_CALCULATION_FOR_INACTIVE_CLIENTS_COL,rowHeader,"Allow dividends for inactive clients");

        worksheet.setColumnWidth(SharedAccountsConstants.CHARGES_NAME_1_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.CHARGES_NAME_1_COL,rowHeader,"Charges 1 ");

        worksheet.setColumnWidth(SharedAccountsConstants.CHARGES_AMOUNT_1_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.CHARGES_AMOUNT_1_COL,rowHeader,"Amount 1 ");

        worksheet.setColumnWidth(SharedAccountsConstants.CHARGES_NAME_2_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.CHARGES_NAME_2_COL,rowHeader,"Charge 2");

        worksheet.setColumnWidth(SharedAccountsConstants.CHARGES_AMOUNT_2_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.CHARGES_AMOUNT_2_COL,rowHeader,"Amount 2");

        worksheet.setColumnWidth(SharedAccountsConstants.CHARGES_NAME_3_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.CHARGES_NAME_3_COL,rowHeader,"Charge 3");

        worksheet.setColumnWidth(SharedAccountsConstants.CHARGES_AMOUNT_3_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(SharedAccountsConstants.CHARGES_AMOUNT_3_COL,rowHeader,"Amount 3 ");

    }
}
