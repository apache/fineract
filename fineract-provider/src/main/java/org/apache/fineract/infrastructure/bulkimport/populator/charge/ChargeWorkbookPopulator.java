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
package org.apache.fineract.infrastructure.bulkimport.populator.charge;

import org.apache.fineract.infrastructure.bulkimport.constants.ChargeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ChargeWorkbookPopulator extends AbstractWorkbookPopulator {

    public ChargeWorkbookPopulator() {
        //
    }

    @Override
    public void populate(Workbook workbook, String dateFormat) {
        Sheet chargeSheet = workbook.createSheet(TemplatePopulateImportConstants.CHARGE_SHEET_NAME);
        setLayout(chargeSheet);
        // setRules(chargeSheet, dateFormat);
    }

    private void setLayout(final Sheet worksheet) {
        Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        worksheet.setColumnWidth(0, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ChargeConstants.CHARGE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(ChargeConstants.CHARGE_AMOUNT_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ChargeConstants.CHARGE_CALCULATION_TYPE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        // worksheet.setColumnWidth(ChargeConstants.CHARGE_DUE_DATE_COL,
        // TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        worksheet.setColumnWidth(ChargeConstants.CHARGE_TIME_TYPE_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        writeString(0, rowHeader, "ID");
        writeString(ChargeConstants.CHARGE_NAME_COL, rowHeader, "Charge Name*");
        writeString(ChargeConstants.CHARGE_AMOUNT_COL, rowHeader, "Charge Amount*");
        writeString(ChargeConstants.CHARGE_CALCULATION_TYPE_COL, rowHeader, "Charge Calculation Type*");
        // writeString(ChargeConstants.CHARGE_DUE_DATE_COL, rowHeader, "Charge Due Date*");
        writeString(ChargeConstants.CHARGE_TIME_TYPE_COL, rowHeader, "Charge Time Type*");
    }

    @SuppressWarnings("unused")
    private void setRules(Sheet workSheet, final String dateFormat) {
        // CellRangeAddressList dueDateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
        // ChargeConstants.CHARGE_DUE_DATE_COL, ChargeConstants.CHARGE_DUE_DATE_COL);

        // DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) workSheet);

        // DataValidationConstraint dueDateConstraint = validationHelper
        // .createDateConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "=TODAY()", null, dateFormat);

        // DataValidation dueDateValidation = validationHelper.createValidation(dueDateConstraint, dueDateRange);

        // workSheet.addValidationData(dueDateValidation);
    }

}
