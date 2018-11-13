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
import org.apache.fineract.infrastructure.bulkimport.constants.ClientPersonConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.Count;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.client.data.ClientData;
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
public class ClientPersonImportHandler implements ImportHandler {

    private Workbook workbook;
    private List<ClientData> clients;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

   @Autowired
    public ClientPersonImportHandler(final PortfolioCommandSourceWritePlatformService
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
        Sheet clientSheet=workbook.getSheet(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME);
        Integer noOfEntries= ImportHandlerUtils.getNumberOfRows(clientSheet,0);
        for (int rowIndex=1;rowIndex<=noOfEntries;rowIndex++){
            Row row;
                row=clientSheet.getRow(rowIndex);
                if (ImportHandlerUtils.isNotImported(row, ClientPersonConstants.STATUS_COL)){
                    clients.add(readClient(row,locale,dateFormat));
                }
        }
    }

    private ClientData readClient(Row row,final String locale, final String dateFormat) {
        Long legalFormId=1L;
        String firstName = ImportHandlerUtils.readAsString(ClientPersonConstants.FIRST_NAME_COL, row);
        String lastName = ImportHandlerUtils.readAsString(ClientPersonConstants.LAST_NAME_COL, row);
        String middleName = ImportHandlerUtils.readAsString(ClientPersonConstants.MIDDLE_NAME_COL, row);
        String officeName = ImportHandlerUtils.readAsString(ClientPersonConstants.OFFICE_NAME_COL, row);
        Long officeId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME), officeName);
        String staffName = ImportHandlerUtils.readAsString(ClientPersonConstants.STAFF_NAME_COL, row);
        Long staffId = ImportHandlerUtils.getIdByName(workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME), staffName);
        String externalId = ImportHandlerUtils.readAsString(ClientPersonConstants.EXTERNAL_ID_COL, row);
        LocalDate submittedOn=ImportHandlerUtils.readAsDate(ClientPersonConstants.SUBMITTED_ON_COL,row);
        LocalDate activationDate = ImportHandlerUtils.readAsDate(ClientPersonConstants.ACTIVATION_DATE_COL, row);
        Boolean active = ImportHandlerUtils.readAsBoolean(ClientPersonConstants.ACTIVE_COL, row);
        if (!active){
            activationDate=submittedOn;
        }
        String mobileNo=null;
        if (ImportHandlerUtils.readAsLong(ClientPersonConstants.MOBILE_NO_COL, row)!=null)
            mobileNo = ImportHandlerUtils.readAsLong(ClientPersonConstants.MOBILE_NO_COL, row).toString();
        LocalDate dob = ImportHandlerUtils.readAsDate(ClientPersonConstants.DOB_COL, row);

        String clientType=ImportHandlerUtils.readAsString(ClientPersonConstants.CLIENT_TYPE_COL, row);
        Long clientTypeId = null;
        if (clientType!=null) {
            String clientTypeAr[] = clientType.split("-");
            if (clientTypeAr[1] != null) {
                clientTypeId = Long.parseLong(clientTypeAr[1]);
            }
        }
        String gender=ImportHandlerUtils.readAsString(ClientPersonConstants.GENDER_COL, row);
        Long genderId = null;
        if (gender!=null) {
            String genderAr[] = gender.split("-");
            if (genderAr[1] != null)
                genderId = Long.parseLong(genderAr[1]);
        }
        String clientClassification= ImportHandlerUtils.readAsString(ClientPersonConstants.CLIENT_CLASSIFICATION_COL, row);
        Long clientClassicationId = null;
        if (clientClassification!=null) {
            String clientClassificationAr[] = clientClassification.split("-");
            if (clientClassificationAr[1] != null)
                clientClassicationId = Long.parseLong(clientClassificationAr[1]);
        }
        Boolean isStaff = ImportHandlerUtils.readAsBoolean(ClientPersonConstants.IS_STAFF_COL, row);

        AddressData addressDataObj=null;
        Collection<AddressData> addressList = null;
        if (ImportHandlerUtils.readAsBoolean(ClientPersonConstants.ADDRESS_ENABLED_COL,row)) {
            String addressType=ImportHandlerUtils.readAsString(ClientPersonConstants.ADDRESS_TYPE_COL, row);
            Long addressTypeId = null;
            if (addressType!=null) {
                String addressTypeAr[] = addressType.split("-");

                if (addressTypeAr[1] != null)
                    addressTypeId = Long.parseLong(addressTypeAr[1]);
            }
            String street = ImportHandlerUtils.readAsString(ClientPersonConstants.STREET_COL, row);
            String addressLine1 = ImportHandlerUtils.readAsString(ClientPersonConstants.ADDRESS_LINE_1_COL, row);
            String addressLine2 = ImportHandlerUtils.readAsString(ClientPersonConstants.ADDRESS_LINE_2_COL, row);
            String addressLine3 = ImportHandlerUtils.readAsString(ClientPersonConstants.ADDRESS_LINE_3_COL, row);
            String city = ImportHandlerUtils.readAsString(ClientPersonConstants.CITY_COL, row);

            String postalCode = ImportHandlerUtils.readAsString(ClientPersonConstants.POSTAL_CODE_COL, row);
            Boolean isActiveAddress = ImportHandlerUtils.readAsBoolean(ClientPersonConstants.IS_ACTIVE_ADDRESS_COL, row);

            String stateProvince=ImportHandlerUtils.readAsString(ClientPersonConstants.STATE_PROVINCE_COL, row);
            Long stateProvinceId = null;
            if (stateProvince!=null) {
                String stateProvinceAr[] = stateProvince.split("-");
                if (stateProvinceAr[1] != null)
                    stateProvinceId = Long.parseLong(stateProvinceAr[1]);
            }
            String country=ImportHandlerUtils.readAsString(ClientPersonConstants.COUNTRY_COL, row);
            Long countryId=null;
            if (country!=null) {
                String countryAr[] = country.split("-");
                if (countryAr[1] != null)
                    countryId = Long.parseLong(countryAr[1]);
            }
             addressDataObj = new AddressData(addressTypeId, street, addressLine1, addressLine2, addressLine3,
                    city, postalCode, isActiveAddress, stateProvinceId, countryId);
             addressList = new ArrayList<AddressData>(Arrays.asList(addressDataObj));
        }
        return ClientData.importClientPersonInstance(legalFormId,row.getRowNum(),firstName,lastName,middleName,submittedOn,activationDate,active,externalId,
				officeId, staffId, mobileNo, dob, clientTypeId, genderId, clientClassicationId, isStaff,
				addressList, locale, dateFormat);

	}

    public Count importEntity(String dateFormat) {
        Sheet clientSheet=workbook.getSheet(TemplatePopulateImportConstants.CLIENT_PERSON_SHEET_NAME);
        int successCount=0;
        int errorCount=0;
        String errorMessage="";
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer(dateFormat));
        for (ClientData client: clients) {
            try {
                String payload=gsonBuilder.create().toJson(client);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createClient() //
                        .withJson(payload) //
                        .build(); //
                commandsSourceWritePlatformService.logCommandSource(commandRequest);
                successCount++;
                Cell statusCell = clientSheet.getRow(client.getRowIndex()).createCell(ClientPersonConstants.STATUS_COL);
                statusCell.setCellValue(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED);
                statusCell.setCellStyle(ImportHandlerUtils.getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            }catch (RuntimeException ex){
                errorCount++;
                ex.printStackTrace();
                errorMessage=ImportHandlerUtils.getErrorMessage(ex);
                ImportHandlerUtils.writeErrorMessage(clientSheet,client.getRowIndex(),errorMessage,ClientPersonConstants.STATUS_COL);
            }
        }
        clientSheet.setColumnWidth(ClientPersonConstants.STATUS_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
        ImportHandlerUtils.writeString(ClientPersonConstants.STATUS_COL, clientSheet.
                getRow(TemplatePopulateImportConstants.ROWHEADER_INDEX), TemplatePopulateImportConstants.STATUS_COLUMN_HEADER);

        return Count.instance(successCount,errorCount);
    }


}
