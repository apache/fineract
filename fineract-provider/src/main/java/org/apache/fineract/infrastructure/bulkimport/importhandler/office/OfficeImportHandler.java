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
package org.apache.fineract.infrastructure.bulkimport.importhandler.office;

import com.google.gson.GsonBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OfficeImportHandler implements ImportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OfficeImportHandler.class);
    static final String PARENT_OFFICE_ID_NOT_POSITIVE = "Parent OfficeId must be a positive number";
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public OfficeImportHandler(final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {
        Map<Integer, String> rowErrors = new HashMap<>();
        List<OfficeData> offices = readExcelFile(workbook, locale, dateFormat, rowErrors);
        return importEntity(workbook, offices, dateFormat, locale, rowErrors);
    }

    private List<OfficeData> readExcelFile(final Workbook workbook, final String locale, final String dateFormat,
            final Map<Integer, String> rowErrors) {
        List<OfficeData> offices = new ArrayList<>();
        Sheet officeSheet = workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(officeSheet, 0);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = officeSheet.getRow(rowIndex);
            if (ImportHandlerUtils.isNotImported(row, OfficeConstants.STATUS_COL)) {
                offices.add(readOffice(row, locale, dateFormat, rowErrors));
            }
        }
        return offices;
    }

    private OfficeData readOffice(Row row, final String locale, final String dateFormat, final Map<Integer, String> rowErrors) {
        String officeName = ImportHandlerUtils.readAsString(OfficeConstants.OFFICE_NAME_COL, row);
        Long parentId = readParentOfficeId(row, rowErrors);
        LocalDate openedDate = ImportHandlerUtils.readAsDate(OfficeConstants.OPENED_ON_COL, row);
        String externalId = ImportHandlerUtils.readAsString(OfficeConstants.EXTERNAL_ID_COL, row);
        OfficeData office = OfficeData.importInstance(officeName, parentId, openedDate, ExternalIdFactory.produce(externalId));
        office.setImportFields(row.getRowNum(), locale, dateFormat);
        return office;
    }

    private static Long readParentOfficeId(final Row row, final Map<Integer, String> rowErrors) {
        final Cell cell = row.getCell(OfficeConstants.PARENT_OFFICE_ID_COL);
        if (cell != null && cell.getCellType() == CellType.NUMERIC) {
            final double value = cell.getNumericCellValue();
            if (value >= 1 && value == Math.rint(value) && value <= Long.MAX_VALUE) {
                return (long) value;
            }
            rowErrors.putIfAbsent(row.getRowNum(), PARENT_OFFICE_ID_NOT_POSITIVE);
            return null;
        }
        final String text = ImportHandlerUtils.readAsString(OfficeConstants.PARENT_OFFICE_ID_COL, row);
        if (text == null) {
            return null;
        }
        if (text.matches("0*[1-9][0-9]{0,17}")) {
            return Long.parseLong(text);
        }
        rowErrors.putIfAbsent(row.getRowNum(), PARENT_OFFICE_ID_NOT_POSITIVE);
        return null;
    }

    private Count importEntity(final Workbook workbook, final List<OfficeData> offices, final String dateFormat, final String locale,
            final Map<Integer, String> rowErrors) {
        Sheet officeSheet = workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat, locale));

        int successCount = 0;
        int errorCount = 0;
        String errorMessage;
        for (OfficeData office : offices) {
            try {
                final String rowError = rowErrors.get(office.getRowIndex());
                if (rowError != null) {
                    throw new IllegalArgumentException(rowError);
                }
                String payload = gsonBuilder.create().toJson(office);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createOffice() //
                        .withJson(payload) //
                        .build(); //
                commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = officeSheet.getRow(office.getRowIndex()).createCell(OfficeConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException ex) {
                errorCount++;
                LOG.error("Problem occurred in importEntity function", ex);
                errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(officeSheet, office.getRowIndex(), errorMessage, OfficeConstants.STATUS_COL);
            }
        }
        officeSheet.setColumnWidth(OfficeConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(OfficeConstants.STATUS_COL, officeSheet.getRow(0),
                TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
        return Count.instance(successCount, errorCount);
    }
}
