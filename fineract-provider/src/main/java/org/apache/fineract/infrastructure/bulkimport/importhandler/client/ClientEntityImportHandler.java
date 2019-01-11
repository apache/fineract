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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.ClientEntityConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.ClientNonPersonData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.GsonBuilder;
@Service
public class ClientEntityImportHandler implements ImportHandler {

    private Workbook workbook;
    private List<ClientData> clients;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientEntityImportHandler(final PortfolioCommandSourceWritePlatformService
            commandsSourceWritePlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Override
    public Count process(Workbook workbook, String locale, String dateFormat) {
        this.workbook = workbook;
        this.clients=new ArrayList<>();
        readExcelFile(locale,dateFormat);
        return importEntity(dateFormat);
    }

    public void readExcelFile(final String locale, final String dateFormat) {
        Sheet clientSheet=workbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
        Integer noOfEntries= ImportHandlerUtils.getNumberOfRows(clientSheet,0);
        for (int rowIndex=1;rowIndex<=noOfEntries;rowIndex++){
            Row row;
                row=clientSheet.getRow(rowIndex);
                if (ImportHandlerUtils.isNotImported(row, ClientEntityConstants.STATUS_COL)){
                    clients.add(readClient(row,locale,dateFormat));
                }
        }
    }

    private ClientData readClient(Row row,final String locale, final String dateFormat) {
        Long legalFormId=2L;
        String name = ImportHandlerUtils.readAsString(ClientEntityConstants.NAME_COL, row);
        String officeName = ImportHandlerUtils.readAsString(ClientEntityConstants.OFFICE_NAME_COL, row);
        Long officeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), officeName);
        String staffName = ImportHandlerUtils.readAsString(ClientEntityConstants.STAFF_NAME_COL, row);
        Long staffId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME), staffName);
        LocalDate incorportionDate=ImportHandlerUtils.readAsDate(ClientEntityConstants.INCOPORATION_DATE_COL,row);
        LocalDate incorporationTill=ImportHandlerUtils.readAsDate(ClientEntityConstants.INCOPORATION_VALID_TILL_COL,row);
        String mobileNo=null;
        if (ImportHandlerUtils.readAsLong(ClientEntityConstants.MOBILE_NO_COL, row)!=null)
         mobileNo = ImportHandlerUtils.readAsLong(ClientEntityConstants.MOBILE_NO_COL, row).toString();

        String clientType=ImportHandlerUtils.readAsString(ClientEntityConstants.CLIENT_TYPE_COL, row);
        Long clientTypeId = null;
        if (clientType!=null) {
            String clientTypeAr[] =clientType .split("-");
            if (clientTypeAr[1] != null) {
                clientTypeId = Long.parseLong(clientTypeAr[1]);
            }
        }
        String clientClassification= ImportHandlerUtils.readAsString(ClientEntityConstants.CLIENT_CLASSIFICATION_COL, row);
        Long clientClassicationId = null;
        if (clientClassification!=null) {
            String clientClassificationAr[] =clientClassification.split("-");
            if (clientClassificationAr[1] != null)
                clientClassicationId = Long.parseLong(clientClassificationAr[1]);
        }
        String incorporationNo=ImportHandlerUtils.readAsString(ClientEntityConstants.INCOPORATION_NUMBER_COL,row);

        String mainBusinessLine=ImportHandlerUtils.readAsString(ClientEntityConstants.MAIN_BUSINESS_LINE,row);
        Long mainBusinessId = null;
        if (mainBusinessLine!=null) {
            String mainBusinessLineAr[] = ImportHandlerUtils.readAsString(ClientEntityConstants.MAIN_BUSINESS_LINE, row).split("-");
            if (mainBusinessLineAr[1] != null)
                mainBusinessId = Long.parseLong(mainBusinessLineAr[1]);
        }
        String constitution= ImportHandlerUtils.readAsString(ClientEntityConstants.CONSTITUTION_COL,row);
        Long constitutionId = null;
        if (constitution!=null) {
            String constitutionAr[] = constitution.split("-");
            if (constitutionAr[1] != null)
                constitutionId = Long.parseLong(constitutionAr[1]);
        }
        String remarks = ImportHandlerUtils.readAsString(ClientEntityConstants.REMARKS_COL, row);

        ClientNonPersonData clientNonPersonData= ClientNonPersonData.importInstance(incorporationNo,incorporationTill,remarks,
                mainBusinessId,constitutionId,locale,dateFormat);

        String externalId= ImportHandlerUtils.readAsString(ClientEntityConstants.EXTERNAL_ID_COL, row);

        Boolean active = ImportHandlerUtils.readAsBoolean(ClientEntityConstants.ACTIVE_COL, row);

        LocalDate submittedOn=ImportHandlerUtils.readAsDate(ClientEntityConstants.SUBMITTED_ON_COL,row);

        LocalDate activationDate = ImportHandlerUtils.readAsDate(ClientEntityConstants.ACTIVATION_DATE_COL, row);
        if (!active){
            activationDate=submittedOn;
        }
        AddressData addressDataObj=null;
        Collection<AddressData> addressList = null;
        if (ImportHandlerUtils.readAsBoolean(ClientEntityConstants.ADDRESS_ENABLED,row)) {
            String addressType = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_TYPE_COL, row);
            Long addressTypeId = null;
            if (addressType!=null) {
                String addressTypeAr[] = addressType.split("-");
                if (addressTypeAr[1] != null)
                    addressTypeId = Long.parseLong(addressTypeAr[1]);
            }
            String street = ImportHandlerUtils.readAsString(ClientEntityConstants.STREET_COL, row);
            String addressLine1 = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_LINE_1_COL, row);
            String addressLine2 = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_LINE_2_COL, row);
            String addressLine3 = ImportHandlerUtils.readAsString(ClientEntityConstants.ADDRESS_LINE_3_COL, row);
            String city = ImportHandlerUtils.readAsString(ClientEntityConstants.CITY_COL, row);

            String postalCode = ImportHandlerUtils.readAsString(ClientEntityConstants.POSTAL_CODE_COL, row);
            Boolean isActiveAddress = ImportHandlerUtils.readAsBoolean(ClientEntityConstants.IS_ACTIVE_ADDRESS_COL, row);

            String stateProvince=ImportHandlerUtils.readAsString(ClientEntityConstants.STATE_PROVINCE_COL, row);
            Long stateProvinceId = null;
            if (stateProvince!=null) {
                String stateProvinceAr[] = stateProvince.split("-");
                if (stateProvinceAr[1] != null)
                    stateProvinceId = Long.parseLong(stateProvinceAr[1]);
            }
            String country= ImportHandlerUtils.readAsString(ClientEntityConstants.COUNTRY_COL, row);
            Long countryId = null;
            if (country!=null) {
                String countryAr[] = country.split("-");
                if (countryAr[1] != null)
                    countryId = Long.parseLong(countryAr[1]);
            }
            addressDataObj = new AddressData(addressTypeId, street, addressLine1, addressLine2, addressLine3,
                    city, postalCode, isActiveAddress, stateProvinceId, countryId);
            addressList = new ArrayList<AddressData>(Arrays.asList(addressDataObj));
        }
        return ClientData.importClientEntityInstance(legalFormId,row.getRowNum(),name,officeId,clientTypeId,clientClassicationId,
				staffId, active, activationDate, submittedOn, externalId, incorportionDate, mobileNo,
				clientNonPersonData, addressList, locale, dateFormat);
	}

    public Count importEntity(String dateFormat) {
        Sheet clientSheet=workbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);

        int successCount = 0;
        int errorCount = 0;
        String errorMessage = "";

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));

        for (ClientData client: clients) {
            try {
                String payload=gsonBuilder.create().toJson(client);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createClient() //
                        .withJson(payload) //
                        .build(); //
                final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = clientSheet.getRow(client.getRowIndex()).createCell(ClientEntityConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            }catch (RuntimeException ex){
                errorCount++;
                ex.printStackTrace();
                errorMessage=ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(clientSheet,client.getRowIndex(),errorMessage,ClientEntityConstants.STATUS_COL);
            }
        }
        clientSheet.setColumnWidth(ClientEntityConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(ClientEntityConstants.STATUS_COL, clientSheet.getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX),
                TemplatePopulateImportConstants.STATUS_COLUMN_HEADER);

        return Count.instance(successCount,errorCount);
    }


}
