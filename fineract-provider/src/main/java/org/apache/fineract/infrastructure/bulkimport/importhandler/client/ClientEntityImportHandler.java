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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecksRepository;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableReadService;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.ClientNonPersonData;
import org.apache.fineract.portfolio.search.service.SearchUtil;
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

    private static final Logger LOG = LoggerFactory.getLogger(ClientEntityImportHandler.class);
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ExternalIdFactory externalIdFactory;
    private final EntityDatatableChecksRepository entityDatatableChecksRepository;
    private final DatatableReadService datatableReadService;

    @Override
    public Count process(final Workbook workbook, final String locale, final String dateFormat) {

        List<ClientData> clients = readExcelFile(workbook, locale, dateFormat);
        return importEntity(workbook, clients, dateFormat, locale);
    }

    private List<ClientData> readExcelFile(final Workbook workbook, final String locale, final String dateFormat) {
        List<ClientData> clients = new ArrayList<>();
        Sheet clientSheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
        Integer noOfEntries = ImportHandlerUtils.getNumberOfRows(clientSheet, 0);
        for (int rowIndex = 1; rowIndex <= noOfEntries; rowIndex++) {
            Row row;
            row = clientSheet.getRow(rowIndex);
            if (row != null) {
                LOG.debug("Processing bulk upload row {} with {} cells", row.getRowNum(), row.getLastCellNum());
            }
            if (ImportHandlerUtils.isNotImported(row, ClientEntityConstants.STATUS_COL)) {
                clients.add(readClient(workbook, row, locale, dateFormat));
            }
        }
        return clients;
    }

    /**
     * Extracts the code value ID out of a "Name (id)" formatted dropdown cell, as written by
     * {@link org.apache.fineract.infrastructure.bulkimport.populator.client.ClientEntityWorkbookPopulator}.
     */
    private Long parseIdFromDisplayValue(final String displayValue, final String fieldName) {
        if (displayValue == null) {
            return null;
        }
        String idPart = SearchUtil.extractIdFromDisplayValue(displayValue);
        if (idPart != null && idPart.matches("\\d+")) {
            return Long.valueOf(idPart);
        }
        LOG.warn("Bulk upload: Could not extract ID from {}. Row data: {}", fieldName, displayValue);
        return null;
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
        Long staffId = null;
        if (staffName != null) {
            staffId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME), staffName);
        }
        LocalDate incorportionDate = ImportHandlerUtils.readAsDate(ClientEntityConstants.INCOPORATION_DATE_COL, row);
        LocalDate incorporationTill = ImportHandlerUtils.readAsDate(ClientEntityConstants.INCOPORATION_VALID_TILL_COL, row);
        String mobileNo = null;
        if (ImportHandlerUtils.readAsLong(ClientEntityConstants.MOBILE_NO_COL, row) != null) {
            mobileNo = Objects.requireNonNull(ImportHandlerUtils.readAsLong(ClientEntityConstants.MOBILE_NO_COL, row)).toString();
        }

        String clientType = ImportHandlerUtils.readAsString(ClientEntityConstants.CLIENT_TYPE_COL, row);
        Long clientTypeId = parseIdFromDisplayValue(clientType, "clientType");
        String clientClassification = ImportHandlerUtils.readAsString(ClientEntityConstants.CLIENT_CLASSIFICATION_COL, row);
        Long clientClassicationId = parseIdFromDisplayValue(clientClassification, "clientClassification");
        String incorporationNo = ImportHandlerUtils.readAsString(ClientEntityConstants.INCOPORATION_NUMBER_COL, row);

        String mainBusinessLine = ImportHandlerUtils.readAsString(ClientEntityConstants.MAIN_BUSINESS_LINE, row);
        Long mainBusinessId = parseIdFromDisplayValue(mainBusinessLine, "mainBusinessLine");
        String constitutionCell = ImportHandlerUtils.readAsString(ClientEntityConstants.CONSTITUTION_COL, row);

        Long constitutionId = null;

        if (constitutionCell != null && constitutionCell.contains("(") && constitutionCell.contains(")")) {
            Pattern pattern = Pattern.compile("\\((\\d+)\\)");
            Matcher matcher = pattern.matcher(constitutionCell);

            if (matcher.find()) {
                constitutionId = Long.valueOf(matcher.group(1));
            }
        }

        if (constitutionId == null && constitutionCell != null && !constitutionCell.trim().isEmpty()) {
            LOG.error("Invalid constitution format in row {}: {}", row.getRowNum(), constitutionCell);
            ApiParameterError error = ApiParameterError.parameterError("error.msg.bulk.import.constitution.invalid",
                    "Invalid Constitution format. Expected format: Name (ID)", "constitution", constitutionCell);
            throw new PlatformApiDataValidationException("error.msg.bulk.import.constitution.invalid",
                    "Invalid Constitution format. Expected format: Name (ID)", Collections.singletonList(error));
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
            Long addressTypeId = parseIdFromDisplayValue(addressType, "addressType");
            String street = ImportHandlerUtils.readAsString(ClientEntityConstants.STREET_COL, row);
            String addressLine1 = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_LINE_1_COL, row);
            String addressLine2 = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_LINE_2_COL, row);
            String addressLine3 = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_LINE_3_COL, row);
            String city = ImportHandlerUtils.readAsString(ClientEntityConstants.CITY_COL, row);

            String postalCode = ImportHandlerUtils.readAsString(ClientEntityConstants.POSTAL_CODE_COL, row);
            Boolean isActiveAddress = ImportHandlerUtils.readAsBoolean(ClientEntityConstants.IS_ACTIVE_ADDRESS_COL, row);

            String stateProvince = ImportHandlerUtils.readAsString(ClientEntityConstants.STATE_PROVINCE_COL, row);
            Long stateProvinceId = parseIdFromDisplayValue(stateProvince, "stateProvince");
            String country = ImportHandlerUtils.readAsString(ClientEntityConstants.COUNTRY_COL, row);
            Long countryId = parseIdFromDisplayValue(country, "country");
            addressDataObj = new AddressData(addressTypeId, street, addressLine1, addressLine2, addressLine3, city, postalCode,
                    isActiveAddress, stateProvinceId, countryId);
            addressList = new ArrayList<>(List.of(addressDataObj));
        }
        return ClientData.importClientEntityInstance(legalFormId, row.getRowNum(), name, officeId, clientTypeId, clientClassicationId,
                staffId, active, activationDate, submittedOn, externalId, incorportionDate, mobileNo, clientNonPersonData, addressList,
                locale, dateFormat);
    }

    private Count importEntity(final Workbook workbook, final List<ClientData> clients, final String dateFormat, final String locale) {
        Sheet clientSheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);

        int successCount = 0;
        int errorCount = 0;
        String errorMessage;

        GsonBuilder gsonBuilder = GoogleGsonSerializerHelper.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat, locale));

        for (ClientData client : clients) {
            try {
                // Get the client row once for validation and datatable reading
                Row clientRow = clientSheet.getRow(client.getRowIndex());
                if (clientRow != null) {
                    // Validate required datatables before attempting client creation
                    String validationError = ImportHandlerUtils.validateRequiredDatatables(workbook, clientSheet, clientRow, "Entity",
                            entityDatatableChecksRepository, datatableReadService);
                    if (validationError != null) {
                        throw new RuntimeException(validationError);
                    }
                }

                String payload = gsonBuilder.create().toJson(client);

                // Read datatables from the Excel row and add to payload
                // Ensure all configured client datatables are included, even if empty
                if (clientRow != null) {
                    List<Map<String, Object>> datatables = ImportHandlerUtils.readDatatablesFromRowWithAllConfigured(clientSheet, clientRow,
                            locale, dateFormat, entityDatatableChecksRepository, datatableReadService, "Entity");
                    if (datatables != null && !datatables.isEmpty()) {
                        // Parse JSON and add datatables array
                        JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject();
                        jsonObject.add("datatables", gsonBuilder.create().toJsonTree(datatables));
                        payload = gsonBuilder.create().toJson(jsonObject);
                    }
                }

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
