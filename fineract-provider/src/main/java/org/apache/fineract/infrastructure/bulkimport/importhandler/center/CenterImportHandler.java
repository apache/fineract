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
package org.apache.fineract.infrastructure.bulkimport.importhandler.center;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.IdempotencyKeyGenerator;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.CenterConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.EnumOptionDataValueSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.GroupIdSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.group.data.CenterData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CenterImportHandler implements ImportHandler {

    public static final String EMPTY_STR = "";
    private static final Logger LOG = LoggerFactory.getLogger(CenterImportHandler.class);
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;

    @Autowired
    public CenterImportHandler(final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            IdempotencyKeyGenerator idempotencyKeyGenerator) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.idempotencyKeyGenerator = idempotencyKeyGenerator;
    }

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {
        List<CenterData> centers = new ArrayList<>();
        List<CalendarData> meetings = new ArrayList<>();
        List<String> statuses = new ArrayList<>();
        readExcelFile(workbook, centers, meetings, statuses, locale, dateFormat);
        return importEntity(workbook, centers, meetings, statuses, dateFormat);
    }

    private void readExcelFile(final Workbook workbook, final List<CenterData> centers, final List<CalendarData> meetings,
            final List<String> statuses, final String locale, final String dateFormat) {

        Sheet centersSheet = workbook.getSheet(TemplatePopulateImportConstants.CENTER_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(centersSheet, TemplatePopulateImportConstants.FIRST_COLUMN_INDEX);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = centersSheet.getRow(rowIndex);
            if (ImportHandlerUtils.isNotImported(row, CenterConstants.STATUS_COL)) {
                centers.add(readCenter(workbook, statuses, row, locale, dateFormat));
                meetings.add(readMeeting(row, locale, dateFormat));
            }
        }
    }

    private CalendarData readMeeting(final Row row, final String locale, final String dateFormat) {
        LocalDate meetingStartDate = ImportHandlerUtils.readAsDate(CenterConstants.MEETING_START_DATE_COL, row);
        Boolean isRepeating = ImportHandlerUtils.readAsBoolean(CenterConstants.IS_REPEATING_COL, row);
        String frequency = ImportHandlerUtils.readAsString(CenterConstants.FREQUENCY_COL, row);
        EnumOptionData frequencyEnum = new EnumOptionData(null, null, ImportHandlerUtils.getFrequencyId(frequency));
        Integer interval = ImportHandlerUtils.readAsInt(CenterConstants.INTERVAL_COL, row);
        String repeatsOnDay = ImportHandlerUtils.readAsString(CenterConstants.REPEATS_ON_DAY_COL, row);
        EnumOptionData repeatsOnDayEnum = new EnumOptionData(null, null, ImportHandlerUtils.getRepeatsOnDayId(repeatsOnDay));
        if (meetingStartDate == null) {
            return null;
        } else {
            if (repeatsOnDay == null) {
                return CalendarData.importInstanceNoRepeatsOnDay(meetingStartDate, isRepeating, frequencyEnum, interval, row.getRowNum(),
                        locale, dateFormat);
            } else {
                return CalendarData.importInstanceWithRepeatsOnDay(meetingStartDate, isRepeating, frequencyEnum, interval, repeatsOnDayEnum,
                        row.getRowNum(), locale, dateFormat);
            }
        }
    }

    private CenterData readCenter(final Workbook workbook, final List<String> statuses, final Row row, final String locale,
            final String dateFormat) {
        String status = ImportHandlerUtils.readAsString(CenterConstants.STATUS_COL, row);
        String officeName = ImportHandlerUtils.readAsString(CenterConstants.OFFICE_NAME_COL, row);
        Long officeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), officeName);
        String staffName = ImportHandlerUtils.readAsString(CenterConstants.STAFF_NAME_COL, row);
        Long staffId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME), staffName);

        String externalId = ImportHandlerUtils.readAsString(CenterConstants.EXTERNAL_ID_COL, row);
        Boolean active = ImportHandlerUtils.readAsBoolean(CenterConstants.ACTIVE_COL, row);
        LocalDate submittedOn = ImportHandlerUtils.readAsDate(CenterConstants.SUBMITTED_ON_DATE_COL, row);
        LocalDate activationDate;
        if (Boolean.TRUE.equals(active)) {
            activationDate = ImportHandlerUtils.readAsDate(CenterConstants.ACTIVATION_DATE_COL, row);
        } else {
            activationDate = submittedOn;
        }
        String centerName = ImportHandlerUtils.readAsString(CenterConstants.CENTER_NAME_COL, row);
        if (centerName == null || centerName.equals(EMPTY_STR)) {
            throw new IllegalArgumentException("Name is blank");
        }
        List<GroupGeneralData> groupMembers = new ArrayList<>();
        for (int cellNo = CenterConstants.GROUP_NAMES_STARTING_COL; cellNo < CenterConstants.GROUP_NAMES_ENDING_COL; cellNo++) {
            String groupName = ImportHandlerUtils.readAsString(cellNo, row);
            if (groupName == null || groupName.equals(EMPTY_STR)) {
                break;
            }
            Long groupId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.GROUP_SHEET_NAME), groupName);
            GroupGeneralData group = new GroupGeneralData(groupId);
            if (!containsGroupId(groupMembers, groupId)) {
                groupMembers.add(group);
            }
        }

        statuses.add(status);
        return CenterData.importInstance(centerName, groupMembers, activationDate, active, submittedOn, externalId, officeId, staffId,
                row.getRowNum(), dateFormat, locale);
    }

    private boolean containsGroupId(List<GroupGeneralData> groupMembers, Long groupId) {
        for (GroupGeneralData group : groupMembers) {
            if (group.getId().equals(groupId)) {
                return true;
            }
        }
        return false;
    }

    private Count importEntity(final Workbook workbook, final List<CenterData> centers, final List<CalendarData> meetings,
            final List<String> statuses, final String dateFormat) {
        Sheet centerSheet = workbook.getSheet(TemplatePopulateImportConstants.CENTER_SHEET_NAME);
        int progressLevel = 0;
        String centerId = EMPTY_STR;
        int successCount = 0;
        int errorCount = 0;
        String errorMessage;
        for (int i = 0; i < centers.size(); i++) {
            Row row = centerSheet.getRow(centers.get(i).getRowIndex());
            Cell errorReportCell = row.createCell(CenterConstants.FAILURE_COL);
            Cell statusCell = row.createCell(CenterConstants.STATUS_COL);
            CommandProcessingResult result = null;
            try {
                String status = statuses.get(i);
                progressLevel = getProgressLevel(status);

                if (progressLevel == 0) {
                    result = importCenter(centers, i, dateFormat);
                    centerId = result.getGroupId().toString();
                    progressLevel = 1;
                } else {
                    centerId = Objects.requireNonNull(
                            ImportHandlerUtils.readAsInt(CenterConstants.CENTER_ID_COL, centerSheet.getRow(centers.get(i).getRowIndex())))
                            .toString();
                }

                if (meetings.get(i) != null) {
                    progressLevel = importCenterMeeting(meetings, Objects.requireNonNull(result), i, dateFormat);
                }
                successCount++;
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException ex) {
                errorCount++;
                LOG.error("Runtime Exception occured in importEntity function", ex);
                errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                writeCenterErrorMessage(workbook, centerId, errorMessage, progressLevel, statusCell, errorReportCell, row);
            }
        }
        setReportHeaders(centerSheet);
        return Count.instance(successCount, errorCount);
    }

    private void writeCenterErrorMessage(final Workbook workbook, final String centerId, final String errorMessage, final int progressLevel,
            final Cell statusCell, final Cell errorReportCell, final Row row) {
        String status = EMPTY_STR;
        if (progressLevel == 0) {
            status = TemplatePopulateImportConstants.STATUS_CREATION_FAILED;
        } else if (progressLevel == 1) {
            status = TemplatePopulateImportConstants.STATUS_MEETING_FAILED;
        }
        statusCell.setCellValue(status);
        statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.RED));

        if (progressLevel > 0) {
            row.createCell(CenterConstants.CENTER_ID_COL).setCellValue(Integer.parseInt(centerId));
        }
        errorReportCell.setCellValue(errorMessage);
    }

    private int getProgressLevel(String status) {

        if (status == null || status.equals(TemplatePopulateImportConstants.STATUS_CREATION_FAILED)) {
            return 0;
        } else if (status.equals(TemplatePopulateImportConstants.STATUS_MEETING_FAILED)) {
            return 1;
        }
        return 0;
    }

    private CommandProcessingResult importCenter(final List<CenterData> centers, final int rowIndex, final String dateFormat) {
        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        Type groupCollectionType = new TypeToken<Collection<GroupGeneralData>>() {

        }.getType();
        gsonBuilder.registerTypeAdapter(groupCollectionType, new GroupIdSerializer());
        String payload = gsonBuilder.create().toJson(centers.get(rowIndex));
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createCenter() //
                .withJson(payload) //
                .build(); //
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private void setReportHeaders(final Sheet sheet) {
        ImportHandlerUtils.writeString(CenterConstants.STATUS_COL, sheet.getRow(0),
                TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
        ImportHandlerUtils.writeString(CenterConstants.CENTER_ID_COL, sheet.getRow(0),
                TemplatePopulateImportConstants.CENTERID_COL_REPORT_HEADER);
        ImportHandlerUtils.writeString(CenterConstants.FAILURE_COL, sheet.getRow(0),
                TemplatePopulateImportConstants.FAILURE_COL_REPORT_HEADER);
    }

    private Integer importCenterMeeting(final List<CalendarData> meetings, final CommandProcessingResult result, final int rowIndex,
            final String dateFormat) {
        CalendarData calendarData = meetings.get(rowIndex);
        calendarData.setTitle("centers_" + result.getGroupId().toString() + "_CollectionMeeting");
        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        gsonBuilder.registerTypeAdapter(EnumOptionData.class, new EnumOptionDataValueSerializer());

        String payload = gsonBuilder.create().toJson(calendarData);
        CommandWrapper commandWrapper = new CommandWrapper(result.getOfficeId(), result.getGroupId(), result.getClientId(),
                result.getLoanId(), result.getSavingsId(), null, null, null, null, null, payload, result.getTransactionId(),
                result.getProductId(), null, null, null, null, idempotencyKeyGenerator.create());
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createCalendar(commandWrapper, TemplatePopulateImportConstants.CENTER_ENTITY_TYPE, result.getGroupId()) //
                .withJson(payload) //
                .build(); //
        commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return 2;
    }

}
