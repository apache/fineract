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
package org.apache.fineract.infrastructure.bulkimport.populator.staff;

import org.apache.fineract.infrastructure.bulkimport.constants.StaffConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.template.domain.Template;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class StaffWorkbookPopulator extends AbstractWorkbookPopulator {

    private OfficeSheetPopulator officeSheetPopulator;

    public StaffWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator) {
        this.officeSheetPopulator=officeSheetPopulator;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet staffSheet=workbook.createSheet(TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME);
        officeSheetPopulator.populate(workbook,dateFormat);
        setLayout(staffSheet);
        setRules(staffSheet,dateFormat);
    }


    private void setRules(Sheet staffSheet,String dateFormat) {
        CellRangeAddressList officeNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                StaffConstants.OFFICE_NAME_COL,  StaffConstants.OFFICE_NAME_COL);
        CellRangeAddressList isLoanOfficerNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                StaffConstants.IS_LOAN_OFFICER,  StaffConstants.IS_LOAN_OFFICER);
        CellRangeAddressList joinedOnNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                StaffConstants.JOINED_ON_COL,  StaffConstants.JOINED_ON_COL);
        CellRangeAddressList isActiveNameRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                StaffConstants.IS_ACTIVE_COL,  StaffConstants.IS_ACTIVE_COL);

        DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) staffSheet);

        List<OfficeData> offices = officeSheetPopulator.getOffices();
        setNames(staffSheet, offices);

        DataValidationConstraint officeNameConstraint =
                validationHelper.createFormulaListConstraint("Office");
        DataValidationConstraint isLoanOfficerConstraint =
                validationHelper.createExplicitListConstraint(new String[] {"True", "False"});
        DataValidationConstraint joinedOnConstraint =
                validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,
                        "=TODAY()",null, dateFormat);
        DataValidationConstraint isActiveConstraint =
                validationHelper.createExplicitListConstraint(new String[] {"True", "False"});

        DataValidation officeValidation =
                validationHelper.createValidation(officeNameConstraint, officeNameRange);
        DataValidation isLoanOfficerValidation =
                validationHelper.createValidation(isLoanOfficerConstraint, isLoanOfficerNameRange);
        DataValidation joinedOnValidation =
                validationHelper.createValidation(joinedOnConstraint, joinedOnNameRange);
        DataValidation isActiveValidation =
                validationHelper.createValidation(isActiveConstraint, isActiveNameRange);

        staffSheet.addValidationData(officeValidation);
        staffSheet.addValidationData(isLoanOfficerValidation);
        staffSheet.addValidationData(joinedOnValidation);
        staffSheet.addValidationData(isActiveValidation);

    }

    private void setNames(Sheet staffSheet, List<OfficeData> offices) {
        Workbook staffWorkBook=staffSheet.getWorkbook();
        Name officeGroup=staffWorkBook.createName();
        officeGroup.setNameName("Office");
        officeGroup.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (offices.size() + 1));
    }

    private void setLayout(Sheet staffSheet) {
        Row rowHeader = staffSheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
        staffSheet.setColumnWidth(StaffConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        staffSheet.setColumnWidth(StaffConstants.FIRST_NAME_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        staffSheet.setColumnWidth(StaffConstants.LAST_NAME_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        staffSheet.setColumnWidth(StaffConstants.IS_LOAN_OFFICER,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        staffSheet.setColumnWidth(StaffConstants.MOBILE_NO_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        staffSheet.setColumnWidth(StaffConstants.JOINED_ON_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        staffSheet.setColumnWidth(StaffConstants.EXTERNAL_ID_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        staffSheet.setColumnWidth(StaffConstants.IS_ACTIVE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(StaffConstants.OFFICE_NAME_COL,rowHeader,"Office Name *");
        writeString(StaffConstants.FIRST_NAME_COL,rowHeader,"First Name *");
        writeString(StaffConstants.LAST_NAME_COL,rowHeader,"Last Name *");
        writeString(StaffConstants.IS_LOAN_OFFICER,rowHeader,"Is Loan Officer *");
        writeString(StaffConstants.MOBILE_NO_COL,rowHeader, "Mobile no");
        writeString(StaffConstants.JOINED_ON_COL,rowHeader,"Joined on *");
        writeString(StaffConstants.EXTERNAL_ID_COL,rowHeader,"External Id *");
        writeString(StaffConstants.IS_ACTIVE_COL,rowHeader,"Is Active *");
    }
}
