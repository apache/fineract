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
package org.apache.fineract.infrastructure.bulkimport.importhandler.client;

import com.google.gson.GsonBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.ClientPersonConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientPersonImportHandler implements ImportHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ClientPersonImportHandler.class);
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ExternalIdFactory externalIdFactory;

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {
        AtomicInteger errorCount = new AtomicInteger();
        List<ClientData> clients = readExcelFile(workbook, locale, dateFormat, errorCount);
        return importEntity(workbook, clients, dateFormat, locale, errorCount.get());
    }

    public List<ClientData> readExcelFile(final Workbook workbook, final String locale, final String dateFormat,
            final AtomicInteger errorCount) {
        List<ClientData> clients = new ArrayList<>();
        Sheet clientSheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(clientSheet, 0);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = clientSheet.getRow(rowIndex);
            if (ImportHandlerUtils.isNotImported(row, ClientPersonConstants.STATUS_COL)) {
                try {
                    clients.add(readClient(workbook, row, locale, dateFormat));
                } catch (RuntimeException re) {
                    ImportHandlerUtils.logReadExcelError(re, clientSheet, rowIndex, ClientPersonConstants.STATUS_COL, errorCount);
                }
            }
        }
        return clients;
    }

    private ClientData readClient(final Workbook workbook, final Row row, final String locale, final String dateFormat) {
        Long legalFormId = 1L;
        String firstName = ImportHandlerUtils.readAsString(ClientPersonConstants.FIRST_NAME_COL, row);
        String lastName = ImportHandlerUtils.readAsString(ClientPersonConstants.LAST_NAME_COL, row);
        String middleName = ImportHandlerUtils.readAsString(ClientPersonConstants.MIDDLE_NAME_COL, row);
        String officeName = ImportHandlerUtils.readAsString(ClientPersonConstants.OFFICE_NAME_COL, row);
        Long officeId = ImportHandlerUtils.requireLookupId(
                ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), officeName), "Office",
                officeName);
        String staffName = ImportHandlerUtils.readAsString(ClientPersonConstants.STAFF_NAME_COL, row);
        Long staffId = null;
        if (staffName != null) {
            staffId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME), staffName);
        }
        ExternalId externalId = externalIdFactory.create(ImportHandlerUtils.readAsString(ClientPersonConstants.EXTERNAL_ID_COL, row));
        LocalDate submittedOn = ImportHandlerUtils.readAsDate(ClientPersonConstants.SUBMITTED_ON_COL, row, dateFormat, locale);
        LocalDate activationDate = ImportHandlerUtils.readAsDate(ClientPersonConstants.ACTIVATION_DATE_COL, row, dateFormat, locale);
        Boolean active = ImportHandlerUtils.readAsBoolean(ClientPersonConstants.ACTIVE_COL, row);
        if (!active) {
            activationDate = submittedOn;
        }
        String mobileNo = ImportHandlerUtils.readAsDigits(ClientPersonConstants.MOBILE_NO_COL, row, "Mobile number");
        LocalDate dob = ImportHandlerUtils.readAsDate(ClientPersonConstants.DOB_COL, row, dateFormat, locale);

        Long clientTypeId = ImportHandlerUtils.readDropdownId(ClientPersonConstants.CLIENT_TYPE_COL, row);
        Long genderId = ImportHandlerUtils.readDropdownId(ClientPersonConstants.GENDER_COL, row);
        Long clientClassificationId = ImportHandlerUtils.readDropdownId(ClientPersonConstants.CLIENT_CLASSIFICATION_COL, row);
        Boolean isStaff = ImportHandlerUtils.readAsBoolean(ClientPersonConstants.IS_STAFF_COL, row);

        AddressData addressDataObj = null;
        Collection<AddressData> addressList = null;
        if (ImportHandlerUtils.readAsBoolean(ClientPersonConstants.ADDRESS_ENABLED_COL, row)) {
            Long addressTypeId = ImportHandlerUtils.readDropdownId(ClientPersonConstants.ADDRESS_TYPE_COL, row);
            String street = ImportHandlerUtils.readAsString(ClientPersonConstants.STREET_COL, row);
            String addressLine1 = ImportHandlerUtils.readAsString(ClientPersonConstants.ADDRESS_LINE_1_COL, row);
            String addressLine2 = ImportHandlerUtils.readAsString(ClientPersonConstants.ADDRESS_LINE_2_COL, row);
            String addressLine3 = ImportHandlerUtils.readAsString(ClientPersonConstants.ADDRESS_LINE_3_COL, row);
            String city = ImportHandlerUtils.readAsString(ClientPersonConstants.CITY_COL, row);

            String postalCode = ImportHandlerUtils.readAsString(ClientPersonConstants.POSTAL_CODE_COL, row);
            Boolean isActiveAddress = ImportHandlerUtils.readAsBoolean(ClientPersonConstants.IS_ACTIVE_ADDRESS_COL, row);

            Long stateProvinceId = ImportHandlerUtils.readDropdownId(ClientPersonConstants.STATE_PROVINCE_COL, row);
            Long countryId = ImportHandlerUtils.readDropdownId(ClientPersonConstants.COUNTRY_COL, row);
            addressDataObj = new AddressData(addressTypeId, street, addressLine1, addressLine2, addressLine3, city, postalCode,
                    isActiveAddress, stateProvinceId, countryId);
            addressList = new ArrayList<>(List.of(addressDataObj));
        }
        return ClientData.importClientPersonInstance(legalFormId, row.getRowNum(), firstName, lastName, middleName, submittedOn,
                activationDate, active, externalId, officeId, staffId, mobileNo, dob, clientTypeId, genderId, clientClassificationId,
                isStaff, addressList, locale, dateFormat);

    }

    public Count importEntity(final Workbook workbook, final List<ClientData> clients, final String dateFormat, String locale,
            final int readErrorCount) {
        Sheet clientSheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME);
        int successCount = 0;
        int errorCount = readErrorCount;
        String errorMessage;
        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat, locale));
        for (ClientData client : clients) {
            try {
                String payload = gsonBuilder.create().toJson(client);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createClient() //
                        .withJson(payload) //
                        .build(); //
                commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = clientSheet.getRow(client.getRowIndex()).createCell(ClientPersonConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException ex) {
                errorCount++;
                LOG.error("Problem occurred in importEntity function", ex);
                errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(clientSheet, client.getRowIndex(), errorMessage, ClientPersonConstants.STATUS_COL);
            }
        }
        clientSheet.setColumnWidth(ClientPersonConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(ClientPersonConstants.STATUS_COL,
                clientSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX), TemplatePopulateImportConstants.STATUS_COLUMN_HEADER);

        return Count.instance(successCount, errorCount);
    }

}
