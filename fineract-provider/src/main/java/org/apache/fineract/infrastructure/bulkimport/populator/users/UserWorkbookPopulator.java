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
package org.apache.fineract.infrastructure.bulkimport.populator.users;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.UserConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.*;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class UserWorkbookPopulator extends AbstractWorkbookPopulator {

    private OfficeSheetPopulator officeSheetPopulator;
    private PersonnelSheetPopulator personnelSheetPopulator;
    private RoleSheetPopulator roleSheetPopulator;

    public UserWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
            PersonnelSheetPopulator personnelSheetPopulator,
            RoleSheetPopulator roleSheetPopulator) {
        this.officeSheetPopulator=officeSheetPopulator;
        this.personnelSheetPopulator=personnelSheetPopulator;
        this.roleSheetPopulator=roleSheetPopulator;
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet usersheet=workbook.createSheet(TemplatePopulateImportConstants.USER_SHEET_NAME);
        personnelSheetPopulator.populate(workbook,dateFormat);
        officeSheetPopulator.populate(workbook,dateFormat);
        roleSheetPopulator.populate(workbook,dateFormat);
        setLayout(usersheet);
        setRules(usersheet);
    }

    private void setRules(Sheet usersheet) {
        CellRangeAddressList officeNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                UserConstants.OFFICE_NAME_COL, UserConstants.OFFICE_NAME_COL);
        CellRangeAddressList staffNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                UserConstants.STAFF_NAME_COL, UserConstants.STAFF_NAME_COL);
        CellRangeAddressList autoGenPwRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                UserConstants.AUTO_GEN_PW_COL, UserConstants.AUTO_GEN_PW_COL);
        CellRangeAddressList overridePwExpiryPolicyRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),
                UserConstants.OVERRIDE_PW_EXPIRY_POLICY_COL, UserConstants.OVERRIDE_PW_EXPIRY_POLICY_COL);

        DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet)usersheet);
        List<OfficeData> offices = officeSheetPopulator.getOffices();
        setNames(usersheet, offices);

        DataValidationConstraint officeNameConstraint = validationHelper.createFormulaListConstraint("Office");
        DataValidationConstraint staffNameConstraint = validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$A1))");
        DataValidationConstraint booleanConstraint = validationHelper.createExplicitListConstraint(new String[]{"True", "False"});

        DataValidation officeValidation = validationHelper.createValidation(officeNameConstraint, officeNameRange);
        DataValidation staffValidation = validationHelper.createValidation(staffNameConstraint, staffNameRange);
        DataValidation autoGenPwValidation=validationHelper.createValidation(booleanConstraint,autoGenPwRange);
        DataValidation overridePwExpiryPolicyValidation=validationHelper.createValidation(booleanConstraint,overridePwExpiryPolicyRange);

        usersheet.addValidationData(officeValidation);
        usersheet.addValidationData(staffValidation);
        usersheet.addValidationData(autoGenPwValidation);
        usersheet.addValidationData(overridePwExpiryPolicyValidation);
    }

    private void setNames(Sheet usersheet, List<OfficeData> offices) {
        Workbook userWorkbook=usersheet.getWorkbook();
        Name officeUser = userWorkbook.createName();

        officeUser.setNameName("Office");
        officeUser.setRefersToFormula(TemplatePopulateImportConstants.OFFICE_SHEET_NAME+"!$B$2:$B$" + (offices.size() + 1));

        //Staff Names for each office
        for (Integer i=0;i<offices.size();i++){
            Integer[] officeNameToBeginEndIndexesOfStaff = personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);

            Name userOfficeName=userWorkbook.createName();

            if(officeNameToBeginEndIndexesOfStaff != null) {
                userOfficeName.setNameName("Staff_" + offices.get(i).name().trim().replaceAll("[ )(]", "_"));
                userOfficeName.setRefersToFormula(TemplatePopulateImportConstants.STAFF_SHEET_NAME+
                        "!$B$" + officeNameToBeginEndIndexesOfStaff[0] + ":$B$" + officeNameToBeginEndIndexesOfStaff[1]);
            }
        }
    }

    private void setLayout(Sheet usersheet) {
        Row rowHeader = usersheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);

        usersheet.setColumnWidth(UserConstants.OFFICE_NAME_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        usersheet.setColumnWidth(UserConstants.STAFF_NAME_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        usersheet.setColumnWidth(UserConstants.USER_NAME_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        usersheet.setColumnWidth(UserConstants.FIRST_NAME_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        usersheet.setColumnWidth(UserConstants.LAST_NAME_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        usersheet.setColumnWidth(UserConstants.EMAIL_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        usersheet.setColumnWidth(UserConstants.AUTO_GEN_PW_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        usersheet.setColumnWidth(UserConstants.OVERRIDE_PW_EXPIRY_POLICY_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        usersheet.setColumnWidth(UserConstants.STATUS_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        usersheet.setColumnWidth(UserConstants.ROLE_NAME_START_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);

        writeString(UserConstants.OFFICE_NAME_COL,rowHeader,"Office Name *");
        writeString(UserConstants.STAFF_NAME_COL,rowHeader,"Staff Name");
        writeString(UserConstants.USER_NAME_COL,rowHeader,"User name");
        writeString(UserConstants.FIRST_NAME_COL,rowHeader,"First name *");
        writeString(UserConstants.LAST_NAME_COL,rowHeader,"Last name *");
        writeString(UserConstants.EMAIL_COL,rowHeader,"Email *");
        writeString(UserConstants.AUTO_GEN_PW_COL,rowHeader,"Auto Gen. Password");
        writeString(UserConstants.OVERRIDE_PW_EXPIRY_POLICY_COL,rowHeader, "Override pw expiry policy");
        writeString(UserConstants.ROLE_NAME_START_COL,rowHeader,"Role Name *(Enter in consecutive cells horizontally)");
    }

}
