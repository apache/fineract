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
package org.apache.fineract.infrastructure.bulkimport.populator.office;

import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class OfficeWorkbookPopulator extends AbstractWorkbookPopulator {
    private  List<OfficeData> offices;


    public OfficeWorkbookPopulator(List<OfficeData> offices) {
      this.offices=offices;
    }

    @Override
    public void populate(final Workbook workbook, final String dateFormat) {
        Sheet officeSheet=workbook.createSheet(OfficeConstants.OFFICE_WORKBOOK_SHEET_NAME);
        setLayout(officeSheet);
        setLookupTable(officeSheet);
        setRules(officeSheet, dateFormat);
        setDefaults(officeSheet);
    }

    private void setLookupTable(final Sheet officeSheet) {
        int rowIndex=1;
        for (OfficeData office:offices) {
            Row row=officeSheet.createRow(rowIndex);
            writeString(OfficeConstants.LOOKUP_OFFICE_COL,row,office.name());
            writeLong(OfficeConstants.LOOKUP_OFFICE_ID_COL,row,office.getId());
            rowIndex++;
        }
    }

    private void setLayout(final Sheet worksheet){
        Row rowHeader=worksheet.createRow(0);
        worksheet.setColumnWidth(OfficeConstants.OFFICE_NAME_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(OfficeConstants.PARENT_OFFICE_NAME_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(OfficeConstants.PARENT_OFFICE_ID_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(OfficeConstants.OPENED_ON_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(OfficeConstants.EXTERNAL_ID_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(OfficeConstants.LOOKUP_OFFICE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        worksheet.setColumnWidth(OfficeConstants.LOOKUP_OFFICE_ID_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);

        writeString(OfficeConstants.OFFICE_NAME_COL, rowHeader, OfficeConstants.OFFICE_NAME_COL_HEADER_NAME);
        writeString(OfficeConstants.PARENT_OFFICE_NAME_COL, rowHeader, OfficeConstants.PARENT_OFFICE_NAME_COL_HEADER_NAME);
        writeString(OfficeConstants.PARENT_OFFICE_ID_COL,rowHeader,OfficeConstants.PARENT_OFFICE_ID_COL_HEADER_NAME);
        writeString(OfficeConstants.OPENED_ON_COL, rowHeader, OfficeConstants.OPENED_ON_COL_HEADER_NAME);
        writeString(OfficeConstants.EXTERNAL_ID_COL, rowHeader, OfficeConstants.EXTERNAL_ID_COL_HEADER_NAME);
        writeString(OfficeConstants.LOOKUP_OFFICE_COL, rowHeader, OfficeConstants.LOOKUP_OFFICE_COL_HEADER_NAME);
        writeString(OfficeConstants.LOOKUP_OFFICE_ID_COL,rowHeader, OfficeConstants.LOOKUP_OFFICE_ID_COL_HEADER_NAME);
    }

    private void setRules(final Sheet workSheet, final String dateFormat){
        CellRangeAddressList parentOfficeNameRange = new  CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),OfficeConstants. PARENT_OFFICE_NAME_COL, OfficeConstants. PARENT_OFFICE_NAME_COL);
        CellRangeAddressList openedOndateRange = new CellRangeAddressList(1, SpreadsheetVersion.EXCEL97.getLastRowIndex(),OfficeConstants. OPENED_ON_COL,OfficeConstants. OPENED_ON_COL);

        DataValidationHelper validationHelper=new HSSFDataValidationHelper((HSSFSheet) workSheet);
        setNames(workSheet);

        DataValidationConstraint parentOfficeNameConstraint=validationHelper.createFormulaListConstraint("Office");
        DataValidationConstraint openDateConstraint=validationHelper.createDateConstraint(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,"=TODAY()",null, dateFormat);

        DataValidation parentOfficeValidation=validationHelper.createValidation(parentOfficeNameConstraint,parentOfficeNameRange);
        DataValidation openDateValidation=validationHelper.createValidation(openDateConstraint, openedOndateRange);

        workSheet.addValidationData(parentOfficeValidation);
        workSheet.addValidationData(openDateValidation);
    }

    private void setNames(final Sheet workSheet) {
        Workbook officeWorkbook=workSheet.getWorkbook();
        Name parentOffice=officeWorkbook.createName();
        parentOffice.setNameName("Office");
        parentOffice.setRefersToFormula("Offices!$H$2:$H$"+(offices.size()+1));
    }
    private void setDefaults(final Sheet worksheet) {
        try {
            for (Integer rowNo = 1; rowNo < 3000; rowNo++) {
                Row row = worksheet.getRow(rowNo);
                if (row == null)
                    row = worksheet.createRow(rowNo);
                writeFormula(OfficeConstants. PARENT_OFFICE_ID_COL, row,
                        "IF(ISERROR(VLOOKUP($B"+(rowNo+1)+",$H$2:$I$"+(offices.size()+1)+",2,FALSE)),\"\",(VLOOKUP($B"+(rowNo+1)+",$H$2:$I$"+(offices.size()+1)+",2,FALSE)))");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}