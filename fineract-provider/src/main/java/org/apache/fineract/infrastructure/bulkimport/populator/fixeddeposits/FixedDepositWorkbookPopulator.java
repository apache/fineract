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

import org.apache.fineract.infrastructure.bulkimport.constants.FixedDepositConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.*;
import org.apache.fineract.portfolio.savings.data.FixedDepositProductData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class FixedDepositWorkbookPopulator extends AbstractWorkbookPopulator {

    private OfficeSheetPopulator officeSheetPopulator;
    private ClientSheetPopulator clientSheetPopulator;
    private PersonnelSheetPopulator personnelSheetPopulator;
    private FixedDepositProductSheetPopulator productSheetPopulator;



    public FixedDepositWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
            ClientSheetPopulator clientSheetPopulator, PersonnelSheetPopulator personnelSheetPopulator,
            FixedDepositProductSheetPopulator fixedDepositProductSheetPopulator) {
        this.officeSheetPopulator = officeSheetPopulator;
        this.clientSheetPopulator = clientSheetPopulator;
        this.personnelSheetPopulator = personnelSheetPopulator;
        this.productSheetPopulator = fixedDepositProductSheetPopulator;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet fixedDepositSheet = workbook.createSheet(TemplatePopulateImportConstants.FIXED_DEPOSIT_SHEET_NAME);
        officeSheetPopulator.populate(workbook,dateFormat);
        clientSheetPopulator.populate(workbook,dateFormat);
        personnelSheetPopulator.populate(workbook,dateFormat);
        productSheetPopulator.populate(workbook,dateFormat);
        setRules(fixedDepositSheet,dateFormat);
        setDefaults(fixedDepositSheet,dateFormat);
        setClientAndGroupDateLookupTable(fixedDepositSheet, clientSheetPopulator.getClients(), null,
                FixedDepositConstants.LOOKUP_CLIENT_NAME_COL,FixedDepositConstants.LOOKUP_ACTIVATION_DATE_COL,!TemplatePopulateImportConstants.CONTAINS_CLIENT_EXTERNAL_ID,dateFormat);
        setLayout(fixedDepositSheet);
    }

    private void setRules(Sheet worksheet,String dateFormat) {
            CellRangeAddressList officeNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.OFFICE_NAME_COL,FixedDepositConstants.OFFICE_NAME_COL);
            CellRangeAddressList clientNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.CLIENT_NAME_COL,FixedDepositConstants.CLIENT_NAME_COL);
            CellRangeAddressList productNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.PRODUCT_COL, FixedDepositConstants.PRODUCT_COL);
            CellRangeAddressList fieldOfficerRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.FIELD_OFFICER_NAME_COL, FixedDepositConstants.FIELD_OFFICER_NAME_COL);
            CellRangeAddressList submittedDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.SUBMITTED_ON_DATE_COL, FixedDepositConstants.SUBMITTED_ON_DATE_COL);
            CellRangeAddressList approvedDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.APPROVED_DATE_COL, FixedDepositConstants.APPROVED_DATE_COL);
            CellRangeAddressList activationDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.ACTIVATION_DATE_COL, FixedDepositConstants.ACTIVATION_DATE_COL);
            CellRangeAddressList interestCompudingPeriodRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL, FixedDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL);
            CellRangeAddressList interestPostingPeriodRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.INTEREST_POSTING_PERIOD_COL, FixedDepositConstants.INTEREST_POSTING_PERIOD_COL);
            CellRangeAddressList interestCalculationRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.INTEREST_CALCULATION_COL, FixedDepositConstants.INTEREST_CALCULATION_COL);
            CellRangeAddressList interestCalculationDaysInYearRange = new CellRangeAddressList(1,
                    SpreadsheetVersion.EXCEL97.getLastRowIndex(), FixedDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL,
                    FixedDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL);
            CellRangeAddressList lockinPeriodFrequencyRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL, FixedDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL);
            CellRangeAddressList depositAmountRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.DEPOSIT_AMOUNT_COL, FixedDepositConstants.DEPOSIT_AMOUNT_COL);
            CellRangeAddressList depositPeriodTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                    FixedDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL, FixedDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL);

            DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

            setNames(worksheet);

            DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
            DataValidationConstraint clientNameConstraint = validationHelper
                    .createFormulaListConstraint("INDIRECT(CONCATENATE(\"Client_\",$A1))");
            DataValidationConstraint productNameConstraint = validationHelper.createFormulaListConstraint("Products");
            DataValidationConstraint fieldOfficerNameConstraint = validationHelper
                    .createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$A1))");
            DataValidationConstraint submittedDateConstraint = validationHelper.createDateConstraint(
                    DataValidationConstraint.OperatorType.BETWEEN, "=VLOOKUP($B1,$AF$2:$AG$"
                            + (clientSheetPopulator.getClientsSize() + 1) + ",2,FALSE)", "=TODAY()",
                    dateFormat);
            DataValidationConstraint approvalDateConstraint = validationHelper.createDateConstraint(
                    DataValidationConstraint.OperatorType.BETWEEN, "=$E1", "=TODAY()", dateFormat);
            DataValidationConstraint activationDateConstraint = validationHelper.createDateConstraint(
                    DataValidationConstraint.OperatorType.BETWEEN, "=$F1", "=TODAY()", dateFormat);
            DataValidationConstraint interestCompudingPeriodConstraint = validationHelper.
                    createExplicitListConstraint(new String[] {
                            TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_DAILY,
                            TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_MONTHLY,
                            TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_QUARTERLY,
                            TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_SEMI_ANNUALLY,
                            TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_ANNUALLY });
            DataValidationConstraint interestPostingPeriodConstraint = validationHelper.createExplicitListConstraint(new String[] {
                    TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_MONTHLY,
                    TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_QUARTERLY,
                    TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_BIANUALLY,
                    TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_ANNUALLY });
            DataValidationConstraint interestCalculationConstraint = validationHelper.createExplicitListConstraint(new String[] {
                    TemplatePopulateImportConstants.INTEREST_CAL_DAILY_BALANCE,
                    TemplatePopulateImportConstants.INTEREST_CAL_AVG_BALANCE });
            DataValidationConstraint interestCalculationDaysInYearConstraint = validationHelper.createExplicitListConstraint(new String[] {
                    TemplatePopulateImportConstants.INTEREST_CAL_DAYS_IN_YEAR_360,
                    TemplatePopulateImportConstants.INTEREST_CAL_DAYS_IN_YEAR_365 });
            DataValidationConstraint frequency = validationHelper.createExplicitListConstraint(new String[] {
                    TemplatePopulateImportConstants.FREQUENCY_DAYS,
                    TemplatePopulateImportConstants.FREQUENCY_WEEKS,
                    TemplatePopulateImportConstants.FREQUENCY_MONTHS,
                    TemplatePopulateImportConstants.FREQUENCY_YEARS });
            DataValidationConstraint depositConstraint = validationHelper.createDecimalConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "=INDIRECT(CONCATENATE(\"Min_Deposit_\",$C1))", null);

            DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
            DataValidation clientValidation = validationHelper.createValidation(clientNameConstraint, clientNameRange);
            DataValidation productNameValidation = validationHelper.createValidation(productNameConstraint, productNameRange);
            DataValidation fieldOfficerValidation = validationHelper.createValidation(fieldOfficerNameConstraint, fieldOfficerRange);
            DataValidation interestCompudingPeriodValidation = validationHelper.createValidation(interestCompudingPeriodConstraint,
                    interestCompudingPeriodRange);
            DataValidation interestPostingPeriodValidation = validationHelper.createValidation(interestPostingPeriodConstraint,
                    interestPostingPeriodRange);
            DataValidation interestCalculationValidation = validationHelper.createValidation(interestCalculationConstraint,
                    interestCalculationRange);
            DataValidation interestCalculationDaysInYearValidation = validationHelper.createValidation(
                    interestCalculationDaysInYearConstraint, interestCalculationDaysInYearRange);
            DataValidation lockinPeriodFrequencyValidation = validationHelper.createValidation(frequency,
                    lockinPeriodFrequencyRange);
            DataValidation depositPeriodTypeValidation = validationHelper.createValidation(frequency,
                    depositPeriodTypeRange);
            DataValidation submittedDateValidation = validationHelper.createValidation(submittedDateConstraint, submittedDateRange);
            DataValidation approvalDateValidation = validationHelper.createValidation(approvalDateConstraint, approvedDateRange);
            DataValidation activationDateValidation = validationHelper.createValidation(activationDateConstraint, activationDateRange);
            DataValidation  depositAmountValidation = validationHelper.createValidation(depositConstraint, depositAmountRange);


            worksheet.addValidationData(officeValidation);
            worksheet.addValidationData(clientValidation);
            worksheet.addValidationData(productNameValidation);
            worksheet.addValidationData(fieldOfficerValidation);
            worksheet.addValidationData(submittedDateValidation);
            worksheet.addValidationData(approvalDateValidation);
            worksheet.addValidationData(activationDateValidation);
            worksheet.addValidationData(interestCompudingPeriodValidation);
            worksheet.addValidationData(interestPostingPeriodValidation);
            worksheet.addValidationData(interestCalculationValidation);
            worksheet.addValidationData(interestCalculationDaysInYearValidation);
            worksheet.addValidationData(lockinPeriodFrequencyValidation);
            worksheet.addValidationData(depositPeriodTypeValidation);
            worksheet.addValidationData(depositAmountValidation);

    }

    private void setNames(Sheet worksheet) {
        Workbook savingsWorkbook = worksheet.getWorkbook();
        List<String> officeNames = officeSheetPopulator.getOfficeNames();
        List<FixedDepositProductData> products = productSheetPopulator.getProducts();

        // Office Names
        Name officeGroup = savingsWorkbook.createName();
        officeGroup.setNameName("Office");
        officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (officeNames.size() + 1));

        // Client and Loan Officer Names for each office
        for (Integer i = 0; i < officeNames.size(); i++) {
            Integer[] officeNameToBeginEndIndexesOfClients = clientSheetPopulator.getOfficeNameToBeginEndIndexesOfClients().get(i);
            Integer[] officeNameToBeginEndIndexesOfStaff = personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);
            Name clientName = savingsWorkbook.createName();
            Name fieldOfficerName = savingsWorkbook.createName();
            if (officeNameToBeginEndIndexesOfStaff != null) {
                fieldOfficerName.setNameName("Staff_" + officeNames.get(i).trim().replaceAll("[ )(]", "_"));
                fieldOfficerName.setRefersToFormula(TemplatePopulateImportConstants.STAFF_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfStaff[0] + ":$B$"
                        + officeNameToBeginEndIndexesOfStaff[1]);
            }
            if (officeNameToBeginEndIndexesOfClients != null) {
                clientName.setNameName("Client_" + officeNames.get(i).trim().replaceAll("[ )(]", "_"));
                clientName.setRefersToFormula(TemplatePopulateImportConstants.CLIENT_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfClients[0] + ":$B$"
                        + officeNameToBeginEndIndexesOfClients[1]);
            }
        }

        // Product Name
        Name productGroup = savingsWorkbook.createName();
        productGroup.setNameName("Products");
        productGroup.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$B$2:$B$" + (productSheetPopulator.getProductsSize() + 1));

        // Default Interest Rate, Interest Compounding Period, Interest Posting
        // Period, Interest Calculation, Interest Calculation Days In Year,
        // Minimum Deposit, Lockin Period, Lockin Period Frequency
        // Names for each product
        for (Integer i = 0; i < products.size(); i++) {
            Name interestCompoundingPeriodName = savingsWorkbook.createName();
            Name interestPostingPeriodName = savingsWorkbook.createName();
            Name interestCalculationName = savingsWorkbook.createName();
            Name daysInYearName = savingsWorkbook.createName();
            Name lockinPeriodName = savingsWorkbook.createName();
            Name lockinPeriodFrequencyName = savingsWorkbook.createName();
            Name depositName = savingsWorkbook.createName();
            Name minDepositName = savingsWorkbook.createName();
            Name maxDepositName = savingsWorkbook.createName();
            Name minDepositTermTypeName = savingsWorkbook.createName();

            FixedDepositProductData product = products.get(i);
            String productName = product.getName().replaceAll("[ ]", "_");

            interestCompoundingPeriodName.setNameName("Interest_Compouding_" + productName);
            interestPostingPeriodName.setNameName("Interest_Posting_" + productName);
            interestCalculationName.setNameName("Interest_Calculation_" + productName);
            daysInYearName.setNameName("Days_In_Year_" + productName);
            minDepositName.setNameName("Min_Deposit_" + productName);
            maxDepositName.setNameName("Max_Deposit_" + productName);
            depositName.setNameName("Deposit_" + productName);
            interestCompoundingPeriodName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$E$" + (i + 2));
            interestPostingPeriodName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$F$" + (i + 2));
            interestCalculationName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$G$" + (i + 2));
            daysInYearName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$H$" + (i + 2));
            depositName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$N$" + (i + 2));
            minDepositName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$L$" + (i + 2));
            maxDepositName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$M$" + (i + 2));

            if(product.getMinDepositTermType() != null) {
                minDepositTermTypeName.setNameName("Term_Type_" + productName);
                minDepositTermTypeName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$P$" + (i + 2));
            }
            if (product.getLockinPeriodFrequency() != null) {
                lockinPeriodName.setNameName("Lockin_Period_" + productName);
                lockinPeriodName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$I$" + (i + 2));
            }
            if (product.getLockinPeriodFrequencyType() != null) {
                lockinPeriodFrequencyName.setNameName("Lockin_Frequency_" + productName);
                lockinPeriodFrequencyName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$J$" + (i + 2));
            }
        }
    }

    private void setDefaults(Sheet worksheet,String dateFormat) {
        Workbook workbook = worksheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();
        short df = workbook.createDataFormat().getFormat(dateFormat);
        dateCellStyle.setDataFormat(df);
        try {
            for (Integer rowNo = 1; rowNo < 1000; rowNo++) {
                Row row = worksheet.createRow(rowNo);
                writeFormula(FixedDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Compouding_\",$C"
                        + (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Compouding_\",$C" + (rowNo + 1) + ")))");
                writeFormula(FixedDepositConstants.INTEREST_POSTING_PERIOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Posting_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Posting_\",$C" + (rowNo + 1) + ")))");
                writeFormula(FixedDepositConstants.INTEREST_CALCULATION_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Calculation_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Calculation_\",$C" + (rowNo + 1) + ")))");
                writeFormula(FixedDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Days_In_Year_\",$C"
                        + (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"Days_In_Year_\",$C" + (rowNo + 1) + ")))");
                writeFormula(FixedDepositConstants.LOCKIN_PERIOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Lockin_Period_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Lockin_Period_\",$C" + (rowNo + 1) + ")))");
                writeFormula(FixedDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Lockin_Frequency_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Lockin_Frequency_\",$C" + (rowNo + 1) + ")))");
                writeFormula(FixedDepositConstants.DEPOSIT_AMOUNT_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Deposit_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Deposit_\",$C" + (rowNo + 1) + ")))");
                writeFormula(FixedDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Term_Type_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Term_Type_\",$C" + (rowNo + 1) + ")))");
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }
    private void setLayout(Sheet worksheet) {
        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
        worksheet.setColumnWidth(FixedDepositConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.CLIENT_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.PRODUCT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.FIELD_OFFICER_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.SUBMITTED_ON_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.APPROVED_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.ACTIVATION_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.INTEREST_POSTING_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.INTEREST_CALCULATION_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.LOCKIN_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.DEPOSIT_AMOUNT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.DEPOSIT_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.EXTERNAL_ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);

        worksheet.setColumnWidth(FixedDepositConstants.CHARGE_ID_1, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.CHARGE_AMOUNT_1, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.CHARGE_DUE_DATE_1, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.CHARGE_ID_2, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.CHARGE_AMOUNT_2, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.CHARGE_DUE_DATE_2, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        worksheet.setColumnWidth(FixedDepositConstants.LOOKUP_CLIENT_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(FixedDepositConstants.LOOKUP_ACTIVATION_DATE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        writeString(FixedDepositConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
        writeString(FixedDepositConstants.CLIENT_NAME_COL, rowHeader, "Client Name*");
        writeString(FixedDepositConstants.PRODUCT_COL, rowHeader, "Product*");
        writeString(FixedDepositConstants.FIELD_OFFICER_NAME_COL, rowHeader, "Field Officer*");
        writeString(FixedDepositConstants.SUBMITTED_ON_DATE_COL, rowHeader, "Submitted On*");
        writeString(FixedDepositConstants.APPROVED_DATE_COL, rowHeader, "Approved On*");
        writeString(FixedDepositConstants.ACTIVATION_DATE_COL, rowHeader, "Activation Date*");
        writeString(FixedDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL, rowHeader, "Interest Compounding Period*");
        writeString(FixedDepositConstants.INTEREST_POSTING_PERIOD_COL, rowHeader, "Interest Posting Period*");
        writeString(FixedDepositConstants.INTEREST_CALCULATION_COL, rowHeader, "Interest Calculated*");
        writeString(FixedDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, rowHeader, "# Days in Year*");
        writeString(FixedDepositConstants.LOCKIN_PERIOD_COL, rowHeader, "Locked In For");
        writeString(FixedDepositConstants.DEPOSIT_AMOUNT_COL, rowHeader, "Deposit Amount");
        writeString(FixedDepositConstants.DEPOSIT_PERIOD_COL, rowHeader, "Deposit Period");
        writeString(FixedDepositConstants.EXTERNAL_ID_COL, rowHeader, "External Id");

        writeString(FixedDepositConstants.CHARGE_ID_1,rowHeader,"Charge Id");
        writeString(FixedDepositConstants.CHARGE_AMOUNT_1, rowHeader, "Charged Amount");
        writeString(FixedDepositConstants.CHARGE_DUE_DATE_1, rowHeader, "Charged On Date");
        writeString(FixedDepositConstants.CHARGE_ID_2,rowHeader,"Charge Id");
        writeString(FixedDepositConstants.CHARGE_AMOUNT_2, rowHeader, "Charged Amount");
        writeString(FixedDepositConstants.CHARGE_DUE_DATE_2, rowHeader, "Charged On Date");
        writeString(FixedDepositConstants.CLOSED_ON_DATE, rowHeader, "Close on Date");
        writeString(FixedDepositConstants.ON_ACCOUNT_CLOSURE_ID,rowHeader,"Action(Account Transfer(200) or cash(100) ");
        writeString(FixedDepositConstants.TO_SAVINGS_ACCOUNT_ID,rowHeader, "Transfered Account No.");
        writeString(FixedDepositConstants.LOOKUP_CLIENT_NAME_COL, rowHeader, "Client Name");
        writeString(FixedDepositConstants.LOOKUP_ACTIVATION_DATE_COL, rowHeader, "Client Activation Date");
    }

}
