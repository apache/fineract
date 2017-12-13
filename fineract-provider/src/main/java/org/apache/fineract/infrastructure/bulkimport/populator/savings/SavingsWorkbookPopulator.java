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
package org.apache.fineract.infrastructure.bulkimport.populator.savings;

import org.apache.fineract.infrastructure.bulkimport.constants.SavingsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.*;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class SavingsWorkbookPopulator extends AbstractWorkbookPopulator {

    private OfficeSheetPopulator officeSheetPopulator;
    private ClientSheetPopulator clientSheetPopulator;
    private GroupSheetPopulator groupSheetPopulator;
    private PersonnelSheetPopulator personnelSheetPopulator;
    private SavingsProductSheetPopulator productSheetPopulator;



    public SavingsWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator, ClientSheetPopulator clientSheetPopulator,
            GroupSheetPopulator groupSheetPopulator, PersonnelSheetPopulator personnelSheetPopulator,
            SavingsProductSheetPopulator savingsProductSheetPopulator) {
        this.officeSheetPopulator=officeSheetPopulator;
        this.clientSheetPopulator=clientSheetPopulator;
        this.groupSheetPopulator=groupSheetPopulator;
        this.personnelSheetPopulator=personnelSheetPopulator;
        this.productSheetPopulator=savingsProductSheetPopulator;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet savingsSheet = workbook.createSheet(TemplatePopulateImportConstants.SAVINGS_ACCOUNTS_SHEET_NAME);
        officeSheetPopulator.populate(workbook,dateFormat);
        clientSheetPopulator.populate(workbook,dateFormat);
        groupSheetPopulator.populate(workbook,dateFormat);
        personnelSheetPopulator.populate(workbook,dateFormat);
        productSheetPopulator.populate(workbook,dateFormat);
        setRules(savingsSheet,dateFormat);
        setDefaults(savingsSheet,dateFormat);
        setClientAndGroupDateLookupTable(savingsSheet, clientSheetPopulator.getClients(), groupSheetPopulator.getGroups(),
                SavingsConstants.LOOKUP_CLIENT_NAME_COL, SavingsConstants.LOOKUP_ACTIVATION_DATE_COL,!TemplatePopulateImportConstants.CONTAINS_CLIENT_EXTERNAL_ID,dateFormat);
        setLayout(savingsSheet);
    }

    private void setLayout(Sheet worksheet) {
        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
        worksheet.setColumnWidth(SavingsConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.SAVINGS_TYPE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.CLIENT_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.PRODUCT_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.FIELD_OFFICER_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.SUBMITTED_ON_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.APPROVED_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.ACTIVATION_DATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.CURRENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.DECIMAL_PLACES_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.IN_MULTIPLES_OF_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);

        worksheet.setColumnWidth(SavingsConstants.NOMINAL_ANNUAL_INTEREST_RATE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.INTEREST_COMPOUNDING_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.INTEREST_POSTING_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.INTEREST_CALCULATION_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.MIN_OPENING_BALANCE_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.LOCKIN_PERIOD_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.LOCKIN_PERIOD_FREQUENCY_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.APPLY_WITHDRAWAL_FEE_FOR_TRANSFERS, TemplatePopulateImportConstants.SMALL_COL_SIZE);

        worksheet.setColumnWidth(SavingsConstants.LOOKUP_CLIENT_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.LOOKUP_ACTIVATION_DATE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.EXTERNAL_ID_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        worksheet.setColumnWidth(SavingsConstants.ALLOW_OVER_DRAFT_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.OVER_DRAFT_LIMIT_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        worksheet.setColumnWidth(SavingsConstants.CHARGE_ID_1, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.CHARGE_AMOUNT_1, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.CHARGE_DUE_DATE_1, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.CHARGE_ID_2, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.CHARGE_AMOUNT_2, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(SavingsConstants.CHARGE_DUE_DATE_2, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        writeString(SavingsConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
        writeString(SavingsConstants.SAVINGS_TYPE_COL, rowHeader, "Individual/Group*");
        writeString(SavingsConstants.CLIENT_NAME_COL, rowHeader, "Client Name*");
        writeString(SavingsConstants.PRODUCT_COL, rowHeader, "Product*");
        writeString(SavingsConstants.FIELD_OFFICER_NAME_COL, rowHeader, "Field Officer*");
        writeString(SavingsConstants.SUBMITTED_ON_DATE_COL, rowHeader, "Submitted On*");
        writeString(SavingsConstants.APPROVED_DATE_COL, rowHeader, "Approved On*");
        writeString(SavingsConstants.ACTIVATION_DATE_COL, rowHeader, "Activation Date*");
        writeString(SavingsConstants.CURRENCY_COL, rowHeader, "Currency");
        writeString(SavingsConstants.DECIMAL_PLACES_COL, rowHeader, "Decimal Places");
        writeString(SavingsConstants.IN_MULTIPLES_OF_COL, rowHeader, "In Multiples Of");
        writeString(SavingsConstants.NOMINAL_ANNUAL_INTEREST_RATE_COL, rowHeader, "Interest Rate %*");
        writeString(SavingsConstants.INTEREST_COMPOUNDING_PERIOD_COL, rowHeader, "Interest Compounding Period*");
        writeString(SavingsConstants.INTEREST_POSTING_PERIOD_COL, rowHeader, "Interest Posting Period*");
        writeString(SavingsConstants.INTEREST_CALCULATION_COL, rowHeader, "Interest Calculated*");
        writeString(SavingsConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, rowHeader, "# Days in Year*");
        writeString(SavingsConstants.MIN_OPENING_BALANCE_COL, rowHeader, "Min Opening Balance");
        writeString(SavingsConstants.LOCKIN_PERIOD_COL, rowHeader, "Locked In For");
        writeString(SavingsConstants.APPLY_WITHDRAWAL_FEE_FOR_TRANSFERS, rowHeader, "Apply Withdrawal Fee For Transfers");

        writeString(SavingsConstants.LOOKUP_CLIENT_NAME_COL, rowHeader, "Client Name");
        writeString(SavingsConstants.LOOKUP_ACTIVATION_DATE_COL, rowHeader, "Client Activation Date");
        writeString(SavingsConstants.EXTERNAL_ID_COL, rowHeader, "External Id");

        writeString(SavingsConstants.ALLOW_OVER_DRAFT_COL, rowHeader, "Is Overdraft Allowed ");
        writeString(SavingsConstants.OVER_DRAFT_LIMIT_COL, rowHeader,"  Maximum Overdraft Amount Limit ");

        writeString(SavingsConstants.CHARGE_ID_1,rowHeader,"Charge Id");
        writeString(SavingsConstants.CHARGE_AMOUNT_1, rowHeader, "Charged Amount");
        writeString(SavingsConstants.CHARGE_DUE_DATE_1, rowHeader, "Charged On Date");
        writeString(SavingsConstants.CHARGE_ID_2,rowHeader,"Charge Id");
        writeString(SavingsConstants.CHARGE_AMOUNT_2, rowHeader, "Charged Amount");
        writeString(SavingsConstants.CHARGE_DUE_DATE_2, rowHeader, "Charged On Date");

    }

    private void setDefaults(Sheet worksheet,String dateFormat) {
        Workbook workbook = worksheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();
        short df = workbook.createDataFormat().getFormat(dateFormat);
        dateCellStyle.setDataFormat(df);
            for (Integer rowNo = 1; rowNo < 1000; rowNo++) {
                Row row = worksheet.createRow(rowNo);
                writeFormula(SavingsConstants.CURRENCY_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Currency_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Currency_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.DECIMAL_PLACES_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Decimal_Places_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Decimal_Places_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.IN_MULTIPLES_OF_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"In_Multiples_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"In_Multiples_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.NOMINAL_ANNUAL_INTEREST_RATE_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Rate_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Rate_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.INTEREST_COMPOUNDING_PERIOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Compouding_\",$D"
                        + (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Compouding_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.INTEREST_POSTING_PERIOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Posting_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Posting_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.INTEREST_CALCULATION_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Calculation_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Calculation_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Days_In_Year_\",$D"
                        + (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"Days_In_Year_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.MIN_OPENING_BALANCE_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Min_Balance_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Min_Balance_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.LOCKIN_PERIOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Lockin_Period_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Lockin_Period_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.LOCKIN_PERIOD_FREQUENCY_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Lockin_Frequency_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Lockin_Frequency_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.APPLY_WITHDRAWAL_FEE_FOR_TRANSFERS, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Withdrawal_Fee_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Withdrawal_Fee_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.ALLOW_OVER_DRAFT_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Overdraft_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Overdraft_\",$D" + (rowNo + 1) + ")))");
                writeFormula(SavingsConstants.OVER_DRAFT_LIMIT_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Overdraft_Limit_\",$D" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Overdraft_Limit_\",$D" + (rowNo + 1) + ")))");
            }
        }

    private void setRules(Sheet worksheet,String dateFormat) {
        CellRangeAddressList officeNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.OFFICE_NAME_COL, SavingsConstants.OFFICE_NAME_COL);
        CellRangeAddressList savingsTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.SAVINGS_TYPE_COL, SavingsConstants.SAVINGS_TYPE_COL);
        CellRangeAddressList clientNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.CLIENT_NAME_COL, SavingsConstants.CLIENT_NAME_COL);
        CellRangeAddressList productNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.PRODUCT_COL, SavingsConstants.PRODUCT_COL);
        CellRangeAddressList fieldOfficerRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.FIELD_OFFICER_NAME_COL, SavingsConstants.FIELD_OFFICER_NAME_COL);
        CellRangeAddressList submittedDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.SUBMITTED_ON_DATE_COL, SavingsConstants.SUBMITTED_ON_DATE_COL);
        CellRangeAddressList approvedDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.APPROVED_DATE_COL, SavingsConstants.APPROVED_DATE_COL);
        CellRangeAddressList activationDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.ACTIVATION_DATE_COL, SavingsConstants.ACTIVATION_DATE_COL);
        CellRangeAddressList interestCompudingPeriodRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.INTEREST_COMPOUNDING_PERIOD_COL, SavingsConstants.INTEREST_COMPOUNDING_PERIOD_COL);
        CellRangeAddressList interestPostingPeriodRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.INTEREST_POSTING_PERIOD_COL, SavingsConstants.INTEREST_POSTING_PERIOD_COL);
        CellRangeAddressList interestCalculationRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.INTEREST_CALCULATION_COL, SavingsConstants.INTEREST_CALCULATION_COL);
        CellRangeAddressList interestCalculationDaysInYearRange = new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), SavingsConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL,
                SavingsConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL);
        CellRangeAddressList lockinPeriodFrequencyRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                SavingsConstants.LOCKIN_PERIOD_FREQUENCY_COL, SavingsConstants.LOCKIN_PERIOD_FREQUENCY_COL);
        CellRangeAddressList applyWithdrawalFeeForTransfersRange = new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), SavingsConstants.APPLY_WITHDRAWAL_FEE_FOR_TRANSFERS, SavingsConstants.APPLY_WITHDRAWAL_FEE_FOR_TRANSFERS);
        CellRangeAddressList allowOverdraftRange = new CellRangeAddressList(1,SpreadsheetVersion.EXCEL97.getLastRowIndex(),SavingsConstants.ALLOW_OVER_DRAFT_COL,SavingsConstants.ALLOW_OVER_DRAFT_COL);



        DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

        setNames(worksheet);

        DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
        DataValidationConstraint savingsTypeConstraint = validationHelper.createExplicitListConstraint(new String[] { "Individual",
                "Group" });
        DataValidationConstraint clientNameConstraint = validationHelper
                .createFormulaListConstraint("IF($B1=\"Individual\",INDIRECT(CONCATENATE(\"Client_\",$A1)),INDIRECT(CONCATENATE(\"Group_\",$A1)))");
        DataValidationConstraint productNameConstraint = validationHelper.createFormulaListConstraint("Products");
        DataValidationConstraint fieldOfficerNameConstraint = validationHelper
                .createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$A1))");
        DataValidationConstraint submittedDateConstraint = validationHelper.createDateConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "=VLOOKUP($C1,$AF$2:$AG$"
                        + (clientSheetPopulator.getClientsSize() + groupSheetPopulator.getGroupsSize() + 1) + ",2,FALSE)", "=TODAY()",
                dateFormat);
        DataValidationConstraint approvalDateConstraint = validationHelper.createDateConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "=$F1", "=TODAY()", dateFormat);
        DataValidationConstraint activationDateConstraint = validationHelper.createDateConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "=$G1", "=TODAY()", dateFormat);
        DataValidationConstraint interestCompudingPeriodConstraint = validationHelper.createExplicitListConstraint(new String[] {
                TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_DAILY ,
                TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_MONTHLY,
                TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_QUARTERLY,
                TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_SEMI_ANNUALLY,
                TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_ANNUALLY });
        DataValidationConstraint interestPostingPeriodConstraint = validationHelper.createExplicitListConstraint(new String[] {
                TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_MONTHLY ,
                TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_QUARTERLY,
                TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_BIANUALLY,
                TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_ANNUALLY  });
        DataValidationConstraint interestCalculationConstraint = validationHelper.createExplicitListConstraint(new String[] {
                TemplatePopulateImportConstants.INTEREST_CAL_DAILY_BALANCE,
                TemplatePopulateImportConstants.INTEREST_CAL_AVG_BALANCE });
        DataValidationConstraint interestCalculationDaysInYearConstraint = validationHelper.createExplicitListConstraint(new String[] {
                TemplatePopulateImportConstants.INTEREST_CAL_DAYS_IN_YEAR_360,
                TemplatePopulateImportConstants.INTEREST_CAL_DAYS_IN_YEAR_365 });
        DataValidationConstraint lockinPeriodFrequencyConstraint = validationHelper.createExplicitListConstraint(new String[] {
                TemplatePopulateImportConstants.FREQUENCY_DAYS,
                TemplatePopulateImportConstants.FREQUENCY_WEEKS,
                TemplatePopulateImportConstants.FREQUENCY_MONTHS,
                TemplatePopulateImportConstants.FREQUENCY_YEARS });
        DataValidationConstraint applyWithdrawalFeeForTransferConstraint = validationHelper.createExplicitListConstraint(new String[] {
                "True", "False" });
        DataValidationConstraint allowOverdraftConstraint = validationHelper.createExplicitListConstraint(new String[] {
                "True", "False" });

        DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
        DataValidation savingsTypeValidation = validationHelper.createValidation(savingsTypeConstraint, savingsTypeRange);
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
        DataValidation lockinPeriodFrequencyValidation = validationHelper.createValidation(lockinPeriodFrequencyConstraint,
                lockinPeriodFrequencyRange);
        DataValidation applyWithdrawalFeeForTransferValidation = validationHelper.createValidation(
                applyWithdrawalFeeForTransferConstraint, applyWithdrawalFeeForTransfersRange);
        DataValidation submittedDateValidation = validationHelper.createValidation(submittedDateConstraint, submittedDateRange);
        DataValidation approvalDateValidation = validationHelper.createValidation(approvalDateConstraint, approvedDateRange);
        DataValidation activationDateValidation = validationHelper.createValidation(activationDateConstraint, activationDateRange);
        DataValidation allowOverdraftValidation = validationHelper.createValidation(
                allowOverdraftConstraint, allowOverdraftRange);

        worksheet.addValidationData(officeValidation);
        worksheet.addValidationData(savingsTypeValidation);
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
        worksheet.addValidationData(applyWithdrawalFeeForTransferValidation);
        worksheet.addValidationData(allowOverdraftValidation);
    }

    private void setNames(Sheet worksheet) {
        Workbook savingsWorkbook = worksheet.getWorkbook();
        List<String> officeNames = officeSheetPopulator.getOfficeNames();
        List<SavingsProductData> products = productSheetPopulator.getProducts();

        // Office Names
        Name officeGroup = savingsWorkbook.createName();
        officeGroup.setNameName("Office");
        officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (officeNames.size() + 1));

        // Client and Loan Officer Names for each office
        for (Integer i = 0; i < officeNames.size(); i++) {
            Integer[] officeNameToBeginEndIndexesOfClients = clientSheetPopulator.getOfficeNameToBeginEndIndexesOfClients().get(i);
            Integer[] officeNameToBeginEndIndexesOfStaff = personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);
            Integer[] officeNameToBeginEndIndexesOfGroups = groupSheetPopulator.getOfficeNameToBeginEndIndexesOfGroups().get(i);
            Name clientName = savingsWorkbook.createName();
            Name fieldOfficerName = savingsWorkbook.createName();
            Name groupName = savingsWorkbook.createName();
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
            if (officeNameToBeginEndIndexesOfGroups != null) {
                groupName.setNameName("Group_" + officeNames.get(i).trim().replaceAll("[ )(]", "_"));
                groupName.setRefersToFormula(TemplatePopulateImportConstants.GROUP_SHEET_NAME+"!$B$" + officeNameToBeginEndIndexesOfGroups[0] + ":$B$"
                        + officeNameToBeginEndIndexesOfGroups[1]);
            }
        }

        // Product Name
        Name productGroup = savingsWorkbook.createName();
        productGroup.setNameName("Products");
        productGroup.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$B$2:$B$" + (productSheetPopulator.getProductsSize() + 1));

        // Default Interest Rate, Interest Compounding Period, Interest Posting
        // Period, Interest Calculation, Interest Calculation Days In Year,
        // Minimum Opening Balance, Lockin Period, Lockin Period Frequency,
        // Withdrawal Fee Amount, Withdrawal Fee Type, Annual Fee, Annual Fee on
        // Date
        // Names for each product
        for (Integer i = 0; i < products.size(); i++) {
            Name interestRateName = savingsWorkbook.createName();
            Name interestCompoundingPeriodName = savingsWorkbook.createName();
            Name interestPostingPeriodName = savingsWorkbook.createName();
            Name interestCalculationName = savingsWorkbook.createName();
            Name daysInYearName = savingsWorkbook.createName();
            Name minOpeningBalanceName = savingsWorkbook.createName();
            Name lockinPeriodName = savingsWorkbook.createName();
            Name lockinPeriodFrequencyName = savingsWorkbook.createName();
            Name currencyName = savingsWorkbook.createName();
            Name decimalPlacesName = savingsWorkbook.createName();
            Name inMultiplesOfName = savingsWorkbook.createName();
            Name withdrawalFeeName = savingsWorkbook.createName();
            Name allowOverdraftName = savingsWorkbook.createName();
            Name overdraftLimitName = savingsWorkbook.createName();
            SavingsProductData product = products.get(i);
            String productName = product.getName().replaceAll("[ ]", "_");
            if (product.getNominalAnnualInterestRate() != null) {
                interestRateName.setNameName("Interest_Rate_" + productName);
                interestRateName.setRefersToFormula("Products!$C$" + (i + 2));
            }
            interestCompoundingPeriodName.setNameName("Interest_Compouding_" + productName);
            interestPostingPeriodName.setNameName("Interest_Posting_" + productName);
            interestCalculationName.setNameName("Interest_Calculation_" + productName);
            daysInYearName.setNameName("Days_In_Year_" + productName);
            currencyName.setNameName("Currency_" + productName);
            decimalPlacesName.setNameName("Decimal_Places_" + productName);
            withdrawalFeeName.setNameName("Withdrawal_Fee_" + productName);
            allowOverdraftName.setNameName("Overdraft_" + productName);

            interestCompoundingPeriodName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$D$" + (i + 2));
            interestPostingPeriodName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$E$" + (i + 2));
            interestCalculationName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$F$" + (i + 2));
            daysInYearName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$G$" + (i + 2));
            currencyName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$K$" + (i + 2));
            decimalPlacesName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$L$" + (i + 2));
            withdrawalFeeName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$N$" + (i + 2));
            allowOverdraftName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$O$" + (i + 2));
            if (product.getOverdraftLimit() != null) {
                overdraftLimitName.setNameName("Overdraft_Limit_" + productName);
                overdraftLimitName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$P$" + (i + 2));
            }
            if (product.getMinRequiredOpeningBalance() != null) {
                minOpeningBalanceName.setNameName("Min_Balance_" + productName);
                minOpeningBalanceName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$H$" + (i + 2));
            }
            if (product.getLockinPeriodFrequency() != null) {
                lockinPeriodName.setNameName("Lockin_Period_" + productName);
                lockinPeriodName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$I$" + (i + 2));
            }
            if (product.getLockinPeriodFrequencyType() != null) {
                lockinPeriodFrequencyName.setNameName("Lockin_Frequency_" + productName);
                lockinPeriodFrequencyName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$J$" + (i + 2));
            }
            if (product.getCurrency().currencyInMultiplesOf() != null) {
                inMultiplesOfName.setNameName("In_Multiples_" + productName);
                inMultiplesOfName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$M$" + (i + 2));
            }
        }
    }

}


