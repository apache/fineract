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

import com.google.common.base.Splitter;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.ClientEntityConstants;
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
import org.apache.fineract.portfolio.client.data.ClientNonPersonData;
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
public class ClientEntityImportHandler implements ImportHandler {

    public static final String SEPARATOR = "-";
    private static final Logger LOG = LoggerFactory.getLogger(ClientEntityImportHandler.class);
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ExternalIdFactory externalIdFactory;

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {

        List<ClientData> clients = readExcelFile(workbook, locale, dateFormat);
        return importEntity(workbook, clients, dateFormat);
    }

    private List<ClientData> readExcelFile(final Workbook workbook, final String locale, final String dateFormat) {
        List<ClientData> clients = new ArrayList<>();
        Sheet clientSheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(clientSheet, 0);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = clientSheet.getRow(rowIndex);
            if (ImportHandlerUtils.isNotImported(row, ClientEntityConstants.STATUS_COL)) {
                clients.add(readClient(workbook, row, locale, dateFormat));
            }
        }
        return clients;
    }

    private ClientData readClient(final Workbook workbook, final Row row, final String locale, final String dateFormat) {
        Long legalFormId = 2L;
        String name = ImportHandlerUtils.readAsString(ClientEntityConstants.NAME_COL, row);
        String officeName = ImportHandlerUtils.readAsString(ClientEntityConstants.OFFICE_NAME_COL, row);
        Long officeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), officeName);
        if (officeId == 0L) {
            officeId = null;
        }
        String staffName = ImportHandlerUtils.readAsString(ClientEntityConstants.STAFF_NAME_COL, row);
        Long staffId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME), staffName);
        if (staffId == 0L) {
            staffId = null;
        }
        LocalDate incorportionDate = ImportHandlerUtils.readAsDate(ClientEntityConstants.INCOPORATION_DATE_COL, row);
        LocalDate incorporationTill = ImportHandlerUtils.readAsDate(ClientEntityConstants.INCOPORATION_VALID_TILL_COL, row);
        String mobileNo = null;
        if (ImportHandlerUtils.readAsLong(ClientEntityConstants.MOBILE_NO_COL, row) != null) {
            mobileNo = Objects.requireNonNull(ImportHandlerUtils.readAsLong(ClientEntityConstants.MOBILE_NO_COL, row)).toString();
        }

        String clientType = ImportHandlerUtils.readAsString(ClientEntityConstants.CLIENT_TYPE_COL, row);
        Long clientTypeId = null;
        if (clientType != null) {
            List<String> clientTypeAr = Splitter.on(SEPARATOR).splitToList(clientType);
            if (clientTypeAr.get(1) != null) {
                clientTypeId = Long.parseLong(clientTypeAr.get(1));
            }
        }
        String clientClassification = ImportHandlerUtils.readAsString(ClientEntityConstants.CLIENT_CLASSIFICATION_COL, row);
        Long clientClassicationId = null;
        if (clientClassification != null) {
            List<String> clientClassificationAr = Splitter.on(SEPARATOR).splitToList(clientClassification);
            if (clientClassificationAr.get(1) != null) {
                clientClassicationId = Long.parseLong(clientClassificationAr.get(1));
            }
        }
        String incorporationNo = ImportHandlerUtils.readAsString(ClientEntityConstants.INCOPORATION_NUMBER_COL, row);

        String mainBusinessLine = ImportHandlerUtils.readAsString(ClientEntityConstants.MAIN_BUSINESS_LINE, row);
        Long mainBusinessId = null;
        if (mainBusinessLine != null) {
            List<String> mainBusinessLineAr = Splitter.on(SEPARATOR)
                    .splitToList(Objects.requireNonNull(ImportHandlerUtils.readAsString(ClientEntityConstants.MAIN_BUSINESS_LINE, row)));
            if (mainBusinessLineAr.get(1) != null) {
                mainBusinessId = Long.parseLong(mainBusinessLineAr.get(1));
            }
        }
        String constitution = ImportHandlerUtils.readAsString(ClientEntityConstants.CONSTITUTION_COL, row);
        Long constitutionId = null;
        if (constitution != null) {
            List<String> constitutionAr = Splitter.on(SEPARATOR).splitToList(constitution);
            if (constitutionAr.get(1) != null) {
                constitutionId = Long.parseLong(constitutionAr.get(1));
            }
        }
        String remarks = ImportHandlerUtils.readAsString(ClientEntityConstants.REMARKS_COL, row);

        ClientNonPersonData clientNonPersonData = ClientNonPersonData.importInstance(incorporationNo, incorporationTill, remarks,
                mainBusinessId, constitutionId, locale, dateFormat);

        ExternalId externalId = externalIdFactory.create(ImportHandlerUtils.readAsString(ClientEntityConstants.EXTERNAL_ID_COL, row));

        Boolean active = ImportHandlerUtils.readAsBoolean(ClientEntityConstants.ACTIVE_COL, row);

        LocalDate submittedOn = ImportHandlerUtils.readAsDate(ClientEntityConstants.SUBMITTED_ON_COL, row);

        LocalDate activationDate = ImportHandlerUtils.readAsDate(ClientEntityConstants.ACTIVATION_DATE_COL, row);
        if (!active) {
            activationDate = submittedOn;
        }
        AddressData addressDataObj;
        Collection<AddressData> addressList = null;
        if (ImportHandlerUtils.readAsBoolean(ClientEntityConstants.ADDRESS_ENABLED, row)) {
            String addressType = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_TYPE_COL, row);
            Long addressTypeId = null;
            if (addressType != null) {
                List<String> addressTypeAr = Splitter.on(SEPARATOR).splitToList(addressType);
                if (addressTypeAr.get(1) != null) {
                    addressTypeId = Long.parseLong(addressTypeAr.get(1));
                }
            }
            String street = ImportHandlerUtils.readAsString(ClientEntityConstants.STREET_COL, row);
            String addressLine1 = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_LINE_1_COL, row);
            String addressLine2 = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_LINE_2_COL, row);
            String addressLine3 = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_LINE_3_COL, row);
            String city = ImportHandlerUtils.readAsString(ClientEntityConstants.CITY_COL, row);

            String postalCode = ImportHandlerUtils.readAsString(ClientEntityConstants.POSTAL_CODE_COL, row);
            Boolean isActiveAddress = ImportHandlerUtils.readAsBoolean(ClientEntityConstants.IS_ACTIVE_ADDRESS_COL, row);

            String stateProvince = ImportHandlerUtils.readAsString(ClientEntityConstants.STATE_PROVINCE_COL, row);
            Long stateProvinceId = null;
            if (stateProvince != null) {
                List<String> stateProvinceAr = Splitter.on(SEPARATOR).splitToList(stateProvince);
                if (stateProvinceAr.get(1) != null) {
                    stateProvinceId = Long.parseLong(stateProvinceAr.get(1));
                }
            }
            String country = ImportHandlerUtils.readAsString(ClientEntityConstants.COUNTRY_COL, row);
            Long countryId = null;
            if (country != null) {
                List<String> countryAr = Splitter.on(SEPARATOR).splitToList(country);
                if (countryAr.get(1) != null) {
                    countryId = Long.parseLong(countryAr.get(1));
                }
            }
            addressDataObj = new AddressData(addressTypeId, street, addressLine1, addressLine2, addressLine3, city, postalCode,
                    isActiveAddress, stateProvinceId, countryId);
            addressList = new ArrayList<>(List.of(addressDataObj));
        }
        return ClientData.importClientEntityInstance(legalFormId, row.getRowNum(), name, officeId, clientTypeId, clientClassicationId,
                staffId, active, activationDate, submittedOn, externalId, incorportionDate, mobileNo, clientNonPersonData, addressList,
                locale, dateFormat);
    }

    private Count importEntity(final Workbook workbook, final List<ClientData> clients, final String dateFormat) {
        Sheet clientSheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);

        int successCount = 0;
        int errorCount = 0;
        String errorMessage;

        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));

        for (ClientData client : clients) {
            try {
                String payload = gsonBuilder.create().toJson(client);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createClient() //
                        .withJson(payload) //
                        .build(); //
                commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = clientSheet.getRow(client.getRowIndex()).createCell(ClientEntityConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException ex) {
                errorCount++;
                LOG.error("Problem occurred in importEntity function", ex);
                errorMessage = ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(clientSheet, client.getRowIndex(), errorMessage, ClientEntityConstants.STATUS_COL);
            }
        }
        clientSheet.setColumnWidth(ClientEntityConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(ClientEntityConstants.STATUS_COL,
                clientSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX), TemplatePopulateImportConstants.STATUS_COLUMN_HEADER);

        return Count.instance(successCount, errorCount);
    }

}
