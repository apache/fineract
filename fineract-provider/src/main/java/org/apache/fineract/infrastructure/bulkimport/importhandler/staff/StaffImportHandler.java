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
package org.apache.fineract.infrastructure.bulkimport.importhandler.staff;

import com.google.gson.GsonBuilder;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.StaffConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.*;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StaffImportHandler implements ImportHandler {
    private List<StaffData> staffList;
    private Workbook workbook;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public StaffImportHandler(final PortfolioCommandSourceWritePlatformService
            commandsSourceWritePlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Override
    public Count process(Workbook workbook, String locale, String dateFormat) {
        this.workbook=workbook;
        this.staffList=new ArrayList<>();
        readExcelFile(locale,dateFormat);
        return importEntity(dateFormat);
    }
    public void readExcelFile(String locale, String dateFormat) {
        Sheet staffSheet=workbook.getSheet(TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME);
        Integer noOfEntries= ImportHandlerUtils.getNumberOfRows(staffSheet,TemplatePopulateImportConstants.FIRST_COLUMN_INDEX);
        for (int rowIndex=1;rowIndex<=noOfEntries;rowIndex++){
            Row row;
                row=staffSheet.getRow(rowIndex);
                if ( ImportHandlerUtils.isNotImported(row, StaffConstants.STATUS_COL)){
                    staffList.add(readStaff(row,locale,dateFormat));
                }

        }
    }

    private StaffData readStaff(Row row,String locale, String dateFormat) {
        String officeName = ImportHandlerUtils.readAsString( StaffConstants.OFFICE_NAME_COL, row);
        Long officeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), officeName);
        String firstName = ImportHandlerUtils.readAsString(StaffConstants.FIRST_NAME_COL, row);
        String lastName = ImportHandlerUtils.readAsString(StaffConstants.LAST_NAME_COL, row);
        Boolean isLoanOfficer=ImportHandlerUtils.readAsBoolean(StaffConstants.IS_LOAN_OFFICER,row);
        String mobileNo=null;
        if (ImportHandlerUtils.readAsLong(StaffConstants.MOBILE_NO_COL,row)!=null)
            mobileNo=ImportHandlerUtils.readAsLong(StaffConstants.MOBILE_NO_COL,row).toString();
        LocalDate joinedOnDate=ImportHandlerUtils.readAsDate(StaffConstants.JOINED_ON_COL,row);
        String externalId=ImportHandlerUtils.readAsString(StaffConstants.EXTERNAL_ID_COL,row);
        Boolean isActive=ImportHandlerUtils.readAsBoolean(StaffConstants.IS_ACTIVE_COL,row);

        return StaffData.importInstance(externalId,firstName,lastName,mobileNo,officeId,isLoanOfficer,isActive,
                joinedOnDate,row.getRowNum(),locale,dateFormat);
    }

    public Count importEntity(String dateFormat) {
        Sheet staffSheet=workbook.getSheet(TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME);
        int successCount=0;
        int errorCount=0;
        String errorMessage="";
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        for (StaffData staff: staffList) {
            try {
                String payload=gsonBuilder.create().toJson(staff);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createStaff()//
                        .withJson(payload) //
                        .build(); //
                final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = staffSheet.getRow(staff.getRowIndex()).createCell(StaffConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            }catch (RuntimeException ex){
                errorCount++;
                ex.printStackTrace();
                errorMessage=ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(staffSheet,staff.getRowIndex(),errorMessage,StaffConstants.STATUS_COL);
            }
        }
        staffSheet.setColumnWidth(StaffConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(StaffConstants.STATUS_COL, staffSheet.getRow(0), TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
        return Count.instance(successCount,errorCount);
    }


}
