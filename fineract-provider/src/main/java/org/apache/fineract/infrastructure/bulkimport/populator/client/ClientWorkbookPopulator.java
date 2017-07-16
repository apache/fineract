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
package org.apache.fineract.infrastructure.bulkimport.populator.client;

import java.util.List;

import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
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

public class ClientWorkbookPopulator extends AbstractWorkbookPopulator {

  private static final int FIRST_NAME_COL = 0;
  private static final int LAST_NAME_COL = 1;
  private static final int MIDDLE_NAME_COL = 2;
  private static final int OFFICE_NAME_COL = 3;
  private static final int STAFF_NAME_COL = 4;
  private static final int EXTERNAL_ID_COL = 5;
  private static final int ACTIVATION_DATE_COL = 6;
  private static final int ACTIVE_COL = 7;
  private static final int WARNING_COL = 9;
  private static final int RELATIONAL_OFFICE_NAME_COL = 16;
  private static final int RELATIONAL_OFFICE_OPENING_DATE_COL = 17;

  private OfficeSheetPopulator officeSheetPopulator;
  private PersonnelSheetPopulator personnelSheetPopulator;

  public ClientWorkbookPopulator(OfficeSheetPopulator officeSheetPopulator,
      PersonnelSheetPopulator personnelSheetPopulator) {
    this.officeSheetPopulator = officeSheetPopulator;
    this.personnelSheetPopulator = personnelSheetPopulator;
  }


  @Override
  public void populate(Workbook workbook) {
    Sheet clientSheet = workbook.createSheet("Clients");
    personnelSheetPopulator.populate(workbook);
    officeSheetPopulator.populate(workbook);
    setLayout(clientSheet);
    setOfficeDateLookupTable(clientSheet, officeSheetPopulator.getOffices(),
        RELATIONAL_OFFICE_NAME_COL, RELATIONAL_OFFICE_OPENING_DATE_COL);
    setRules(clientSheet);
  }

  private void setLayout(Sheet worksheet) {
    Row rowHeader = worksheet.createRow(0);
    rowHeader.setHeight((short) 500);
    worksheet.setColumnWidth(FIRST_NAME_COL, 6000);
    worksheet.setColumnWidth(LAST_NAME_COL, 6000);
    worksheet.setColumnWidth(MIDDLE_NAME_COL, 6000);
    writeString(FIRST_NAME_COL, rowHeader, "First Name*");
    writeString(LAST_NAME_COL, rowHeader, "Last Name*");
    writeString(MIDDLE_NAME_COL, rowHeader, "Middle Name");
    worksheet.setColumnWidth(OFFICE_NAME_COL, 5000);
    worksheet.setColumnWidth(STAFF_NAME_COL, 5000);
    worksheet.setColumnWidth(EXTERNAL_ID_COL, 3500);
    worksheet.setColumnWidth(ACTIVATION_DATE_COL, 4000);
    worksheet.setColumnWidth(ACTIVE_COL, 2000);
    worksheet.setColumnWidth(RELATIONAL_OFFICE_NAME_COL, 6000);
    worksheet.setColumnWidth(RELATIONAL_OFFICE_OPENING_DATE_COL, 4000);
    writeString(OFFICE_NAME_COL, rowHeader, "Office Name*");
    writeString(STAFF_NAME_COL, rowHeader, "Staff Name*");
    writeString(EXTERNAL_ID_COL, rowHeader, "External ID");
    writeString(ACTIVATION_DATE_COL, rowHeader, "Activation Date*");
    writeString(ACTIVE_COL, rowHeader, "Active*");
    writeString(WARNING_COL, rowHeader, "All * marked fields are compulsory.");
    writeString(RELATIONAL_OFFICE_NAME_COL, rowHeader, "Office Name");
    writeString(RELATIONAL_OFFICE_OPENING_DATE_COL, rowHeader, "Opening Date");

  }

  private void setRules(Sheet worksheet) {
    CellRangeAddressList officeNameRange = new CellRangeAddressList(1,
        SpreadsheetVersion.EXCEL97.getLastRowIndex(), OFFICE_NAME_COL, OFFICE_NAME_COL);
    CellRangeAddressList staffNameRange = new CellRangeAddressList(1,
        SpreadsheetVersion.EXCEL97.getLastRowIndex(), STAFF_NAME_COL, STAFF_NAME_COL);
    CellRangeAddressList dateRange = new CellRangeAddressList(1,
        SpreadsheetVersion.EXCEL97.getLastRowIndex(), ACTIVATION_DATE_COL, ACTIVATION_DATE_COL);
    CellRangeAddressList activeRange = new CellRangeAddressList(1,
        SpreadsheetVersion.EXCEL97.getLastRowIndex(), ACTIVE_COL, ACTIVE_COL);

    DataValidationHelper validationHelper = new HSSFDataValidationHelper((HSSFSheet) worksheet);

    List<OfficeData> offices = officeSheetPopulator.getOffices();
    setNames(worksheet, offices);

    DataValidationConstraint officeNameConstraint =
        validationHelper.createFormulaListConstraint("Office");
    DataValidationConstraint staffNameConstraint =
        validationHelper.createFormulaListConstraint("INDIRECT(CONCATENATE(\"Staff_\",$D1))");
    DataValidationConstraint activationDateConstraint =
        validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN,
            "=VLOOKUP($D1,$Q$2:$R" + (offices.size() + 1) + ",2,FALSE)", "=TODAY()", "dd/mm/yy");
    DataValidationConstraint activeConstraint =
        validationHelper.createExplicitListConstraint(new String[] {"True", "False"});


    DataValidation officeValidation =
        validationHelper.createValidation(officeNameConstraint, officeNameRange);
    DataValidation staffValidation =
        validationHelper.createValidation(staffNameConstraint, staffNameRange);
    DataValidation activationDateValidation =
        validationHelper.createValidation(activationDateConstraint, dateRange);
    DataValidation activeValidation =
        validationHelper.createValidation(activeConstraint, activeRange);

    worksheet.addValidationData(activeValidation);
    worksheet.addValidationData(officeValidation);
    worksheet.addValidationData(staffValidation);
    worksheet.addValidationData(activationDateValidation);
  }

  private void setNames(Sheet worksheet, List<OfficeData> offices) {
    Workbook clientWorkbook = worksheet.getWorkbook();
    Name officeGroup = clientWorkbook.createName();
    officeGroup.setNameName("Office");
    officeGroup.setRefersToFormula("Offices!$B$2:$B$" + (offices.size() + 1));

    for (Integer i = 0; i < offices.size(); i++) {
      Integer[] officeNameToBeginEndIndexesOfStaff =
          personnelSheetPopulator.getOfficeNameToBeginEndIndexesOfStaff().get(i);
      if (officeNameToBeginEndIndexesOfStaff != null) {
        Name name = clientWorkbook.createName();
        name.setNameName("Staff_" + offices.get(i).name().trim().replaceAll("[ )(]", "_"));
        name.setRefersToFormula("Staff!$B$" + officeNameToBeginEndIndexesOfStaff[0] + ":$B$"
            + officeNameToBeginEndIndexesOfStaff[1]);
      }
    }
  }

}
