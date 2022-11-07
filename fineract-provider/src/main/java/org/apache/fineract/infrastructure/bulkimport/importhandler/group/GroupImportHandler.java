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
package org.apache.fineract.infrastructure.bulkimport.importhandler.group;

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
import org.apache.fineract.infrastructure.bulkimport.constants.GroupConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.ClientIdSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.EnumOptionDataValueSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.client.data.ClientData;
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
public class GroupImportHandler implements ImportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GroupImportHandler.class);

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;

    @Autowired
    public GroupImportHandler(final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            IdempotencyKeyGenerator idempotencyKeyGenerator) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.idempotencyKeyGenerator = idempotencyKeyGenerator;
    }

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {

        List<GroupGeneralData> groups = new ArrayList<>();
        List<CalendarData> meetings = new ArrayList<>();
        List<String> statuses = new ArrayList<>();
        readExcelFile(workbook, groups, meetings, statuses, locale, dateFormat);
        return importEntity(workbook, groups, meetings, statuses, dateFormat);
    }

    private void readExcelFile(final Workbook workbook, final List<GroupGeneralData> groups, final List<CalendarData> meetings,
            final List<String> statuses, final String locale, final String dateFormat) {
        Sheet groupsSheet = workbook.getSheet(TemplatePopulateImportConstants.GROUP_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(groupsSheet, TemplatePopulateImportConstants.FIRST_COLUMN_INDEX);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = groupsSheet.getRow(rowIndex);
            if (ImportHandlerUtils.isNotImported(row, GroupConstants.STATUS_COL)) {
                groups.add(readGroup(workbook, row, statuses, locale, dateFormat));
                meetings.add(readMeeting(row, locale, dateFormat));
            }
        }
    }

    private CalendarData readMeeting(Row row, String locale, String dateFormat) {
        LocalDate meetingStartDate = ImportHandlerUtils.readAsDate(GroupConstants.MEETING_START_DATE_COL, row);
        Boolean isRepeating = ImportHandlerUtils.readAsBoolean(GroupConstants.IS_REPEATING_COL, row);
        String frequency = ImportHandlerUtils.readAsString(GroupConstants.FREQUENCY_COL, row);
        EnumOptionData frequencyEnum = new EnumOptionData(null, null, ImportHandlerUtils.getFrequencyId(frequency));
        Integer interval = ImportHandlerUtils.readAsInt(GroupConstants.INTERVAL_COL, row);
        String repeatsOnDay = ImportHandlerUtils.readAsString(GroupConstants.REPEATS_ON_DAY_COL, row);
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

    private GroupGeneralData readGroup(final Workbook workbook, final Row row, final List<String> statuses, final String locale,
            final String dateFormat) {
        String status = ImportHandlerUtils.readAsString(GroupConstants.STATUS_COL, row);
        String officeName = ImportHandlerUtils.readAsString(GroupConstants.OFFICE_NAME_COL, row);
        Long officeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), officeName);
        String staffName = ImportHandlerUtils.readAsString(GroupConstants.STAFF_NAME_COL, row);
        Long staffId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME), staffName);
        String centerName = ImportHandlerUtils.readAsString(GroupConstants.CENTER_NAME_COL, row);
        Long centerId = null;
        if (centerName != null) {
            centerId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.CENTER_SHEET_NAME), centerName);
        }
        String externalId = ImportHandlerUtils.readAsString(GroupConstants.EXTERNAL_ID_COL, row);
        Boolean active = ImportHandlerUtils.readAsBoolean(GroupConstants.ACTIVE_COL, row);
        LocalDate submittedOnDate = ImportHandlerUtils.readAsDate(GroupConstants.SUBMITTED_ON_DATE_COL, row);
        LocalDate activationDate = null;
        if (active) {
            activationDate = ImportHandlerUtils.readAsDate(GroupConstants.ACTIVATION_DATE_COL, row);
        } else {
            activationDate = submittedOnDate;
        }
        String groupName = ImportHandlerUtils.readAsString(GroupConstants.NAME_COL, row);
        if (groupName == null || groupName.equals("")) {
            throw new IllegalArgumentException("Name is blank");
        }
        List<ClientData> clientMembers = new ArrayList<>();
        for (int cellNo = GroupConstants.CLIENT_NAMES_STARTING_COL; cellNo < GroupConstants.CLIENT_NAMES_ENDING_COL; cellNo++) {
            String clientName = ImportHandlerUtils.readAsString(cellNo, row);
            if (clientName == null) {
                break;
            }
            Long clientId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME),
                    clientName);
            ClientData clientData = ClientData.emptyInstance(clientId);
            if (!containsClientId(clientMembers, clientId)) {
                clientMembers.add(clientData);
            }
        }
        statuses.add(status);
        return GroupGeneralData.importInstance(groupName, clientMembers, activationDate, submittedOnDate, active, externalId, officeId,
                staffId, centerId, row.getRowNum(), locale, dateFormat);
    }

    private boolean containsClientId(List<ClientData> clientMembers, Long clientId) {
        for (ClientData client : clientMembers) {
            if (client.getId().equals(clientId)) {
                return true;
            }
        }
        return false;
    }

    private Count importEntity(final Workbook workbook, final List<GroupGeneralData> groups, final List<CalendarData> meetings,
            final List<String> statuses, final String dateFormat) {
        Sheet groupSheet = workbook.getSheet(TemplatePopulateImportConstants.GROUP_SHEET_NAME);
        int successCount = 0;
        int errorCount = 0;
        int progressLevel = 0;
        String groupId = "";
        String errorMessage = "";
        for (int i = 0; i < groups.size(); i++) {
            Row row = groupSheet.getRow(groups.get(i).getRowIndex());
            Cell errorReportCell = row.createCell(GroupConstants.FAILURE_COL);
            Cell statusCell = row.createCell(GroupConstants.STATUS_COL);
            CommandProcessingResult result = null;
            try {
                String status = statuses.get(i);
                progressLevel = getProgressLevel(status);

                if (progressLevel == 0) {
                    result = importGroup(groups, i, dateFormat);
                    groupId = result.getGroupId().toString();
                    progressLevel = 1;
                } else {
                    groupId = Objects.requireNonNull(
                            ImportHandlerUtils.readAsInt(GroupConstants.GROUP_ID_COL, groupSheet.getRow(groups.get(i).getRowIndex())))
                            .toString();
                }

                if (meetings.get(i) != null && groups.get(i).getCenterId() == null) {
                    progressLevel = importGroupMeeting(meetings, Objects.requireNonNull(result), i, dateFormat);
                }

                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException ex) {
                errorCount++;
                LOG.error("Problem occurred in importEntity function", ex);
                errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                writeGroupErrorMessage(workbook, groupId, errorMessage, progressLevel, statusCell, errorReportCell, row);
            }
        }
        setReportHeaders(groupSheet);
        return Count.instance(successCount, errorCount);
    }

    private void writeGroupErrorMessage(final Workbook workbook, final String groupId, final String errorMessage, final int progressLevel,
            final Cell statusCell, final Cell errorReportCell, final Row row) {
        String status = "";
        if (progressLevel == 0) {
            status = TemplatePopulateImportConstants.STATUS_CREATION_FAILED;
        } else if (progressLevel == 1) {
            status = TemplatePopulateImportConstants.STATUS_MEETING_FAILED;
        }
        statusCell.setCellValue(status);
        statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.RED));

        if (progressLevel > 0) {
            row.createCell(GroupConstants.GROUP_ID_COL).setCellValue(Integer.parseInt(groupId));
        }
        errorReportCell.setCellValue(errorMessage);
    }

    private void setReportHeaders(Sheet groupSheet) {
        ImportHandlerUtils.writeString(GroupConstants.STATUS_COL, groupSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                TemplatePopulateImportConstants.STATUS_COL_REPORT_HEADER);
        ImportHandlerUtils.writeString(GroupConstants.GROUP_ID_COL, groupSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                TemplatePopulateImportConstants.GROUP_ID_COL_REPORT_HEADER);
        ImportHandlerUtils.writeString(GroupConstants.FAILURE_COL, groupSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                TemplatePopulateImportConstants.FAILURE_COL_REPORT_HEADER);
    }

    private Integer importGroupMeeting(final List<CalendarData> meetings, CommandProcessingResult result, int rowIndex, String dateFormat) {
        CalendarData calendarData = meetings.get(rowIndex);
        calendarData.setTitle("group_" + result.getGroupId().toString() + "_CollectionMeeting");
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

    private CommandProcessingResult importGroup(final List<GroupGeneralData> groups, final int rowIndex, final String dateFormat) {
        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        Type clientCollectionType = new TypeToken<Collection<ClientData>>() {

        }.getType();
        gsonBuilder.registerTypeAdapter(clientCollectionType, new ClientIdSerializer());
        String payload = gsonBuilder.create().toJson(groups.get(rowIndex));
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createGroup() //
                .withJson(payload) //
                .build(); //
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private int getProgressLevel(String status) {
        if (status == null || status.equals(TemplatePopulateImportConstants.STATUS_CREATION_FAILED)) {
            return 0;
        } else if (status.equals(TemplatePopulateImportConstants.STATUS_MEETING_FAILED)) {
            return 1;
        }
        return 0;
    }

}
