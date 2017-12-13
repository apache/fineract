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
package org.apache.fineract.infrastructure.bulkimport.populator.recurringdeposit;

import org.apache.fineract.infrastructure.bulkimport.constants.RecurringDepositConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.*;
import org.apache.fineract.portfolio.savings.data.RecurringDepositProductData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class RecurringDepositWorkbookPopulator extends AbstractWorkbookPopulator {

    private OfficeSheetPopulator officeSheetPopulator;
    private ClientSheetPopulator clientSheetPopulator;
    private PersonnelSheetPopulator personnelSheetPopulator;
    private RecurringDepositProductSheetPopulator productSheetPopulator;


    public RecurringDepositWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
            ClientSheetPopulator clientSheetPopulator, PersonnelSheetPopulator personnelSheetPopulator,
            RecurringDepositProductSheetPopulator recurringDepositProductSheetPopulator) {

        this.officeSheetPopulator = officeSheetPopulator;
        this.clientSheetPopulator = clientSheetPopulator;
        this.personnelSheetPopulator = personnelSheetPopulator;
        this.productSheetPopulator = recurringDepositProductSheetPopulator;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet recurringDepositSheet = workbook.createSheet(TemplatePopulateImportConstants.RECURRING_DEPOSIT_SHEET_NAME);
        officeSheetPopulator.populate(workbook,dateFormat);
        clientSheetPopulator.populate(workbook,dateFormat);
        personnelSheetPopulator.populate(workbook,dateFormat);
        productSheetPopulator.populate(workbook,dateFormat);
        setRules(recurringDepositSheet,dateFormat);
        setDefaults(recurringDepositSheet,dateFormat);
        setClientAndGroupDateLookupTable(recurringDepositSheet, clientSheetPopulator.getClients(), null,
                RecurringDepositConstants.LOOKUP_CLIENT_NAME_COL,  RecurringDepositConstants.LOOKUP_ACTIVATION_DATE_COL,!TemplatePopulateImportConstants.CONTAINS_CLIENT_EXTERNAL_ID,dateFormat);
        setLayout(recurringDepositSheet);

    }

    private void setLayout(Sheet worksheet) {
        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
        worksheet.setColumnWidth(RecurringDepositConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.CLIENT_NAME_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.PRODUCT_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.FIELD_OFFICER_NAME_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.SUBMITTED_ON_DATE_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.APPROVED_DATE_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.ACTIVATION_DATE_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.INTEREST_POSTING_PERIOD_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.INTEREST_CALCULATION_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.LOCKIN_PERIOD_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.RECURRING_DEPOSIT_AMOUNT_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.DEPOSIT_PERIOD_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.DEPOSIT_FREQUENCY_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.DEPOSIT_FREQUENCY_TYPE_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.DEPOSIT_START_DATE_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.IS_MANDATORY_DEPOSIT_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.ALLOW_WITHDRAWAL_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.ADJUST_ADVANCE_PAYMENTS_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.FREQ_SAME_AS_GROUP_CENTER_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.EXTERNAL_ID_COL,  TemplatePopulateImportConstants.SMALL_COL_SIZE);

        worksheet.setColumnWidth(RecurringDepositConstants.CHARGE_ID_1, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.CHARGE_AMOUNT_1, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.CHARGE_DUE_DATE_1, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.CHARGE_ID_2, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.CHARGE_AMOUNT_2, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.CHARGE_DUE_DATE_2, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        worksheet.setColumnWidth(RecurringDepositConstants.LOOKUP_CLIENT_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(RecurringDepositConstants.LOOKUP_ACTIVATION_DATE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        writeString(RecurringDepositConstants.OFFICE_NAME_COL, rowHeader, "Office Name*");
        writeString(RecurringDepositConstants.CLIENT_NAME_COL, rowHeader, "Client Name*");
        writeString(RecurringDepositConstants.PRODUCT_COL, rowHeader, "Product*");
        writeString(RecurringDepositConstants.FIELD_OFFICER_NAME_COL, rowHeader, "Field Officer*");
        writeString(RecurringDepositConstants.SUBMITTED_ON_DATE_COL, rowHeader, "Submitted On*");
        writeString(RecurringDepositConstants.APPROVED_DATE_COL, rowHeader, "Approved On*");
        writeString(RecurringDepositConstants.ACTIVATION_DATE_COL, rowHeader, "Activation Date*");
        writeString(RecurringDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL, rowHeader, "Interest Compounding Period*");
        writeString(RecurringDepositConstants.INTEREST_POSTING_PERIOD_COL, rowHeader, "Interest Posting Period*");
        writeString(RecurringDepositConstants.INTEREST_CALCULATION_COL, rowHeader, "Interest Calculated*");
        writeString(RecurringDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, rowHeader, "# Days in Year*");
        writeString(RecurringDepositConstants.LOCKIN_PERIOD_COL, rowHeader, "Locked In For");
        writeString(RecurringDepositConstants.RECURRING_DEPOSIT_AMOUNT_COL, rowHeader, "Recurring Deposit Amount");
        writeString(RecurringDepositConstants.DEPOSIT_PERIOD_COL, rowHeader, "Deposit Period");
        writeString(RecurringDepositConstants.DEPOSIT_FREQUENCY_COL, rowHeader, "Deposit Frequency");
        writeString(RecurringDepositConstants.DEPOSIT_START_DATE_COL, rowHeader, "Deposit Start Date");
        writeString(RecurringDepositConstants.IS_MANDATORY_DEPOSIT_COL, rowHeader, "Is Mandatory Deposit?");
        writeString(RecurringDepositConstants.ALLOW_WITHDRAWAL_COL, rowHeader, "Allow Withdrawal?");
        writeString(RecurringDepositConstants.ADJUST_ADVANCE_PAYMENTS_COL, rowHeader, "Adjust Advance Payments Toward Future Installments ");
        writeString(RecurringDepositConstants.FREQ_SAME_AS_GROUP_CENTER_COL, rowHeader, "Deposit Frequency Same as Group/Center meeting");
        writeString(RecurringDepositConstants.EXTERNAL_ID_COL, rowHeader, "External Id");

        writeString(RecurringDepositConstants.CHARGE_ID_1,rowHeader,"Charge Id");
        writeString(RecurringDepositConstants.CHARGE_AMOUNT_1, rowHeader, "Charged Amount");
        writeString(RecurringDepositConstants.CHARGE_DUE_DATE_1, rowHeader, "Charged On Date");
        writeString(RecurringDepositConstants.CHARGE_ID_2,rowHeader,"Charge Id");
        writeString(RecurringDepositConstants.CHARGE_AMOUNT_2, rowHeader, "Charged Amount");
        writeString(RecurringDepositConstants.CHARGE_DUE_DATE_2, rowHeader, "Charged On Date");

        writeString(RecurringDepositConstants.LOOKUP_CLIENT_NAME_COL, rowHeader, "Client Name");
        writeString(RecurringDepositConstants.LOOKUP_ACTIVATION_DATE_COL, rowHeader, "Client Activation Date");
    }

    private void setDefaults(Sheet worksheet,String dateFormat) {
        Workbook workbook = worksheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();
        short df = workbook.createDataFormat().getFormat(dateFormat);
        dateCellStyle.setDataFormat(df);
            for (Integer rowNo = 1; rowNo < 1000; rowNo++) {
                Row row = worksheet.createRow(rowNo);
                writeFormula(RecurringDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Compouding_\",$C"
                        + (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Compouding_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.INTEREST_POSTING_PERIOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Posting_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Posting_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.INTEREST_CALCULATION_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Interest_Calculation_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Interest_Calculation_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Days_In_Year_\",$C"
                        + (rowNo + 1) + "))),\"\",INDIRECT(CONCATENATE(\"Days_In_Year_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.LOCKIN_PERIOD_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Lockin_Period_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Lockin_Period_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Lockin_Frequency_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Lockin_Frequency_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.RECURRING_DEPOSIT_AMOUNT_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Deposit_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Deposit_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Term_Type_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Term_Type_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.IS_MANDATORY_DEPOSIT_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Mandatory_Deposit_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Mandatory_Deposit_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.ALLOW_WITHDRAWAL_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Allow_Withdrawal_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Allow_Withdrawal_\",$C" + (rowNo + 1) + ")))");
                writeFormula(RecurringDepositConstants.ADJUST_ADVANCE_PAYMENTS_COL, row, "IF(ISERROR(INDIRECT(CONCATENATE(\"Adjust_Advance_\",$C" + (rowNo + 1)
                        + "))),\"\",INDIRECT(CONCATENATE(\"Adjust_Advance_\",$C" + (rowNo + 1) + ")))");
            }
    }

    private void setRules(Sheet worksheet,String dateFormat) {
        CellRangeAddressList officeNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.OFFICE_NAME_COL, RecurringDepositConstants.OFFICE_NAME_COL);
        CellRangeAddressList clientNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.CLIENT_NAME_COL, RecurringDepositConstants.CLIENT_NAME_COL);
        CellRangeAddressList productNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.PRODUCT_COL, RecurringDepositConstants.PRODUCT_COL);
        CellRangeAddressList fieldOfficerRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.FIELD_OFFICER_NAME_COL, RecurringDepositConstants.FIELD_OFFICER_NAME_COL);
        CellRangeAddressList submittedDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.SUBMITTED_ON_DATE_COL, RecurringDepositConstants.SUBMITTED_ON_DATE_COL);
        CellRangeAddressList approvedDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.APPROVED_DATE_COL, RecurringDepositConstants.APPROVED_DATE_COL);
        CellRangeAddressList activationDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.ACTIVATION_DATE_COL, RecurringDepositConstants.ACTIVATION_DATE_COL);
        CellRangeAddressList interestCompudingPeriodRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL, RecurringDepositConstants.INTEREST_COMPOUNDING_PERIOD_COL);
        CellRangeAddressList interestPostingPeriodRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.INTEREST_POSTING_PERIOD_COL, RecurringDepositConstants.INTEREST_POSTING_PERIOD_COL);
        CellRangeAddressList interestCalculationRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.INTEREST_CALCULATION_COL, RecurringDepositConstants.INTEREST_CALCULATION_COL);
        CellRangeAddressList interestCalculationDaysInYearRange = new CellRangeAddressList(1,
                SpreadsheetVersion.EXCEL97.getLastRowIndex(), RecurringDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL,
                RecurringDepositConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL);
        CellRangeAddressList lockinPeriodFrequencyRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL, RecurringDepositConstants.LOCKIN_PERIOD_FREQUENCY_COL);
        CellRangeAddressList depositAmountRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.RECURRING_DEPOSIT_AMOUNT_COL,RecurringDepositConstants. RECURRING_DEPOSIT_AMOUNT_COL);
        CellRangeAddressList depositPeriodTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL, RecurringDepositConstants.DEPOSIT_PERIOD_FREQUENCY_COL);
        CellRangeAddressList depositFrequencyTypeRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.DEPOSIT_FREQUENCY_TYPE_COL, RecurringDepositConstants.DEPOSIT_FREQUENCY_TYPE_COL);
        CellRangeAddressList isMandatoryDepositRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants. IS_MANDATORY_DEPOSIT_COL, RecurringDepositConstants.IS_MANDATORY_DEPOSIT_COL);
        CellRangeAddressList allowWithdrawalRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.ALLOW_WITHDRAWAL_COL, RecurringDepositConstants.ALLOW_WITHDRAWAL_COL);
        CellRangeAddressList adjustAdvancePaymentRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants. ADJUST_ADVANCE_PAYMENTS_COL, RecurringDepositConstants.ADJUST_ADVANCE_PAYMENTS_COL);
        CellRangeAddressList sameFreqAsGroupRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.FREQ_SAME_AS_GROUP_CENTER_COL, RecurringDepositConstants.FREQ_SAME_AS_GROUP_CENTER_COL);
        CellRangeAddressList depositStartDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                RecurringDepositConstants.DEPOSIT_START_DATE_COL, RecurringDepositConstants.DEPOSIT_START_DATE_COL);

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
        DataValidationConstraint interestCompudingPeriodConstraint = validationHelper.createExplicitListConstraint(new String[] {
               TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_DAILY ,
                TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_MONTHLY,
                TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_QUARTERLY,
                TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_SEMI_ANNUALLY,
                TemplatePopulateImportConstants.INTEREST_COMPOUNDING_PERIOD_ANNUALLY});
        DataValidationConstraint interestPostingPeriodConstraint = validationHelper.createExplicitListConstraint(new String[] {
               TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_MONTHLY ,
                TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_QUARTERLY,
                TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_BIANUALLY,
                TemplatePopulateImportConstants.INTEREST_POSTING_PERIOD_ANNUALLY });
        DataValidationConstraint interestCalculationConstraint = validationHelper.createExplicitListConstraint(new String[] {
                TemplatePopulateImportConstants.INTEREST_CAL_DAILY_BALANCE,
                TemplatePopulateImportConstants.INTEREST_CAL_AVG_BALANCE});

        DataValidationConstraint interestCalculationDaysInYearConstraint = validationHelper.createExplicitListConstraint(new String[] {
                TemplatePopulateImportConstants.INTEREST_CAL_DAYS_IN_YEAR_360,
                TemplatePopulateImportConstants.INTEREST_CAL_DAYS_IN_YEAR_365});
        DataValidationConstraint frequency = validationHelper.createExplicitListConstraint(new String[] {
                TemplatePopulateImportConstants.FREQUENCY_DAYS,
                TemplatePopulateImportConstants.FREQUENCY_WEEKS,
                TemplatePopulateImportConstants.FREQUENCY_MONTHS,
                TemplatePopulateImportConstants.FREQUENCY_YEARS});
        DataValidationConstraint depositConstraint = validationHelper.createDecimalConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "=INDIRECT(CONCATENATE(\"Min_Deposit_\",$C1))", null);
        DataValidationConstraint booleanConstraint = validationHelper.createExplicitListConstraint(new String[] {
                "True", "False" });
        DataValidationConstraint depositStartDateConstraint = validationHelper.createDateConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "=$G1", "=TODAY()", "dd/mm/yy");

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
        DataValidation depositFrequencyTypeValidation = validationHelper.createValidation(frequency,
                depositFrequencyTypeRange);
        DataValidation submittedDateValidation = validationHelper.createValidation(submittedDateConstraint, submittedDateRange);
        DataValidation approvalDateValidation = validationHelper.createValidation(approvalDateConstraint, approvedDateRange);
        DataValidation activationDateValidation = validationHelper.createValidation(activationDateConstraint, activationDateRange);
        DataValidation  depositAmountValidation = validationHelper.createValidation(depositConstraint, depositAmountRange);
        DataValidation isMandatoryDepositValidation = validationHelper.createValidation(
                booleanConstraint, isMandatoryDepositRange);
        DataValidation allowWithdrawalValidation = validationHelper.createValidation(
                booleanConstraint, allowWithdrawalRange);
        DataValidation adjustAdvancePaymentValidation = validationHelper.createValidation(
                booleanConstraint, adjustAdvancePaymentRange);
        DataValidation sameFreqAsGroupValidation = validationHelper.createValidation(
                booleanConstraint, sameFreqAsGroupRange);
        DataValidation depositStartDateValidation = validationHelper.createValidation(
                depositStartDateConstraint, depositStartDateRange);

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
        worksheet.addValidationData(depositFrequencyTypeValidation);
        worksheet.addValidationData(isMandatoryDepositValidation);
        worksheet.addValidationData(allowWithdrawalValidation);
        worksheet.addValidationData(adjustAdvancePaymentValidation);
        worksheet.addValidationData(sameFreqAsGroupValidation);
        worksheet.addValidationData(depositStartDateValidation);
    }

    private void setNames(Sheet worksheet) {
        Workbook savingsWorkbook = worksheet.getWorkbook();
        List<String> officeNames = officeSheetPopulator.getOfficeNames();
        List<RecurringDepositProductData> products = productSheetPopulator.getProducts();

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
            Name allowWithdrawalName = savingsWorkbook.createName();
            Name mandatoryDepositName = savingsWorkbook.createName();
            Name adjustAdvancePaymentsName = savingsWorkbook.createName();

            RecurringDepositProductData product = products.get(i);
            String productName = product.getName().replaceAll("[ ]", "_");

            interestCompoundingPeriodName.setNameName("Interest_Compouding_" + productName);
            interestPostingPeriodName.setNameName("Interest_Posting_" + productName);
            interestCalculationName.setNameName("Interest_Calculation_" + productName);
            daysInYearName.setNameName("Days_In_Year_" + productName);
            minDepositName.setNameName("Min_Deposit_" + productName);
            maxDepositName.setNameName("Max_Deposit_" + productName);
            depositName.setNameName("Deposit_" + productName);
            allowWithdrawalName.setNameName("Allow_Withdrawal_" + productName);
            mandatoryDepositName.setNameName("Mandatory_Deposit_" + productName);
            adjustAdvancePaymentsName.setNameName("Adjust_Advance_" + productName);
            interestCompoundingPeriodName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$E$" + (i + 2));
            interestPostingPeriodName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$F$" + (i + 2));
            interestCalculationName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$G$" + (i + 2));
            daysInYearName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$H$" + (i + 2));
            depositName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$N$" + (i + 2));
            minDepositName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$L$" + (i + 2));
            maxDepositName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$M$" + (i + 2));
            allowWithdrawalName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$Y$" + (i + 2));
            mandatoryDepositName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$X$" + (i + 2));
            adjustAdvancePaymentsName.setRefersToFormula(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME+"!$Z$" + (i + 2));

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
}
