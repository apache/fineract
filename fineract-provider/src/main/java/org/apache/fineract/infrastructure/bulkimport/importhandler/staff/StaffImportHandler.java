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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.bulkimport.constants.StaffConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.organisation.staff.command.StaffCreateCommand;
import org.apache.fineract.organisation.staff.data.StaffCreateResponse;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.mapper.StaffDataMapper;
import org.apache.fineract.organisation.staff.mapper.StaffDateMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class StaffImportHandler implements ImportHandler {

    private final CommandDispatcher dispatcher;
    private final StaffDataMapper staffDataMapper;
    private final StaffDateMapper staffDateMapper;

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {
        List<StaffData> staffList = readExcelFile(workbook, locale, dateFormat);
        return importEntity(workbook, staffList, dateFormat);
    }

    private List<StaffData> readExcelFile(final Workbook workbook, final String locale, final String dateFormat) {
        List<StaffData> staffList = new ArrayList<>();
        Sheet staffSheet = workbook.getSheet(TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(staffSheet, TemplatePopulateImportConstants.FIRST_COLUMN_INDEX);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = staffSheet.getRow(rowIndex);
            if (ImportHandlerUtils.isNotImported(row, StaffConstants.STATUS_COL)) {
                staffList.add(readStaff(workbook, row, locale, dateFormat));
            }
        }
        return staffList;
    }

    private StaffData readStaff(final Workbook workbook, final Row row, final String locale, final String dateFormat) {
        String officeName = ImportHandlerUtils.readAsString(StaffConstants.OFFICE_NAME_COL, row);
        Long officeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), officeName);
        String firstName = ImportHandlerUtils.readAsString(StaffConstants.FIRST_NAME_COL, row);
        String lastName = ImportHandlerUtils.readAsString(StaffConstants.LAST_NAME_COL, row);
        Boolean isLoanOfficer = ImportHandlerUtils.readAsBoolean(StaffConstants.IS_LOAN_OFFICER, row);
        String mobileNo = null;
        if (ImportHandlerUtils.readAsLong(StaffConstants.MOBILE_NO_COL, row) != null) {
            mobileNo = ImportHandlerUtils.readAsLong(StaffConstants.MOBILE_NO_COL, row).toString();
        }
        LocalDate joinedOnDate = ImportHandlerUtils.readAsDate(StaffConstants.JOINED_ON_COL, row);
        String externalId = ImportHandlerUtils.readAsString(StaffConstants.EXTERNAL_ID_COL, row);
        Boolean isActive = ImportHandlerUtils.readAsBoolean(StaffConstants.IS_ACTIVE_COL, row);

        return StaffData.builder().externalId(externalId).firstname(firstName).lastname(lastName).mobileNo(mobileNo).officeId(officeId)
                .isLoanOfficer(isLoanOfficer).isActive(isActive).joiningDate(joinedOnDate).dateFormat(dateFormat).locale(locale)
                .rowIndex(row.getRowNum()).build();
    }

    private Count importEntity(final Workbook workbook, final List<StaffData> staffList, final String dateFormat) {
        Sheet staffSheet = workbook.getSheet(TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME);
        int successCount = 0;
        int errorCount = 0;
        String errorMessage;
        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        for (var staff : staffList) {
            try {
                final var command = new StaffCreateCommand();

                var request = staffDataMapper.map(staff);
                request.setJoiningDate(staffDateMapper.map(staff.getJoiningDate(), staff.getDateFormat()));

                command.setPayload(request);

                final Supplier<StaffCreateResponse> response = dispatcher.dispatch(command);

                response.get();

                successCount++;
                Cell statusCell = staffSheet.getRow(staff.getRowIndex()).createCell(StaffConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException ex) {
                errorCount++;
                log.error("Problem occurred in importEntity function", ex);
                errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(staffSheet, staff.getRowIndex(), errorMessage, StaffConstants.STATUS_COL);
            }
        }
        staffSheet.setColumnWidth(StaffConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(StaffConstants.STATUS_COL, staffSheet.getRow(0),
                TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
        return Count.instance(successCount, errorCount);
    }

}
