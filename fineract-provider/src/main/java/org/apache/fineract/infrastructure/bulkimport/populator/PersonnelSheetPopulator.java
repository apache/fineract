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
package org.apache.fineract.infrastructure.bulkimport.populator;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.commons.collections.CollectionUtils;

public class PersonnelSheetPopulator extends AbstractWorkbookPopulator {

  private List<StaffData> personnel;
  private List<OfficeData> offices;

  // Maintaining the one to many relationship
  private Map<String, List<StaffData>> officeToPersonnel;

  /*
   * Guava Multimap would make this more readable. The value Integer[] contains the beginIndex and
   * endIndex for the staff list of each office. Required for applying names in excel.
   */
  private Map<Integer, Integer[]> officeNameToBeginEndIndexesOfStaff;

  private static final int OFFICE_NAME_COL = 0;
  private static final int STAFF_NAME_COL = 1;
  private static final int STAFF_ID_COL = 2;

  public PersonnelSheetPopulator(List<StaffData> personnel, List<OfficeData> offices) {
    this.personnel = personnel;
    this.offices = offices;
  }


  @Override
  public void populate(Workbook workbook,String dateFormat) {
    Sheet staffSheet = workbook.createSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
    setLayout(staffSheet);

    /*
     * This piece of code could have been avoided by making multiple trips to the database for the
     * staff of each office but this is more performance efficient
     */
    setOfficeToPersonnelMap();

    populateStaffByOfficeName(staffSheet);
    staffSheet.protectSheet("");
  }


  private void populateStaffByOfficeName(Sheet staffSheet) {
    int rowIndex = 1, startIndex = 1, officeIndex = 0;
    officeNameToBeginEndIndexesOfStaff = new HashMap<>();
    Row row = staffSheet.createRow(rowIndex);
    for (OfficeData office : offices) {
      startIndex = rowIndex + 1;
      writeString(OFFICE_NAME_COL, row, office.name().trim().replaceAll("[ )(]", "_"));

      List<StaffData> staffList =
              officeToPersonnel.get(office.name().trim().replaceAll("[ )(]", "_"));

    if (staffList!=null){
      if (!staffList.isEmpty()) {
        for (StaffData staff : staffList) {
          writeString(STAFF_NAME_COL, row, staff.getDisplayName());
          writeLong(STAFF_ID_COL, row, staff.getId());
          row = staffSheet.createRow(++rowIndex);
        }
        officeNameToBeginEndIndexesOfStaff.put(officeIndex++, new Integer[]{startIndex, rowIndex});
      }
    }else
        officeIndex++;
    }
  }

  private void setOfficeToPersonnelMap() {
    officeToPersonnel = new HashMap<>();
    for (StaffData person : personnel) {
      add(person.getOfficeName().trim().replaceAll("[ )(]", "_"), person);
    }
  }

  // Guava Multi-map can reduce this.
  private void add(String key, StaffData value) {
    List<StaffData> values = officeToPersonnel.get(key);
    if (values == null) {
      values = new ArrayList<>();
    }
    values.add(value);
    officeToPersonnel.put(key, values);
  }

  private void setLayout(Sheet worksheet) {
    for (Integer i = 0; i < 3; i++)
      worksheet.setColumnWidth(i, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
    Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
    rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
    writeString(OFFICE_NAME_COL, rowHeader, "Office Name");
    writeString(STAFF_NAME_COL, rowHeader, "Staff List");
    writeString(STAFF_ID_COL, rowHeader, "Staff ID");
  }

  public Map<Integer, Integer[]> getOfficeNameToBeginEndIndexesOfStaff() {
    return officeNameToBeginEndIndexesOfStaff;
  }

}