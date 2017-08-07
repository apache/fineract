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

import com.google.gson.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.importhandler.AbstractImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.helper.DateSerializer;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class ClientPersonImportHandler extends AbstractImportHandler {
    private static final int FIRST_NAME_COL = 0;//A
    private static final int LAST_NAME_COL = 1;//B
    private static final int MIDDLE_NAME_COL = 2;//C
    private static final int OFFICE_NAME_COL = 3;//D
    private static final int STAFF_NAME_COL = 4;//E
    private static final int EXTERNAL_ID_COL = 5;//F
    private static final int ACTIVE_COL = 6;//G
    private static final int ACTIVATION_DATE_COL = 7;//H
    private static final int SUBMITTED_ON_COL=8;//I
    private static final int MOBILE_NO_COL=9;//J
    private static final int DOB_COL=10;//K
    private static final int CLIENT_TYPE_COL=11;//L
    private static final int GENDER_COL=12;//M
    private static final int CLIENT_CLASSIFICATION_COL=13;//N
    private static final int IS_STAFF_COL=14;//O
    private static final int ADDRESS_ENABLED=15;//P
    private static final int ADDRESS_TYPE_COL=16;//Q
    private static final int STREET_COL=17;//R
    private static final int ADDRESS_LINE_1_COL=18;//S
    private static final int ADDRESS_LINE_2_COL=19;//T
    private static final int ADDRESS_LINE_3_COL=20;//U
    private static final int CITY_COL=21;//V
    private static final int STATE_PROVINCE_COL=22;//W
    private static final int COUNTRY_COL=23;//X
    private static final int POSTAL_CODE_COL=24;//Y
    private static final int IS_ACTIVE_ADDRESS_COL=25;//Z
    private static final int STATUS_COL = 26;//AA

    private Workbook workbook;
    private List<ClientData> clients;


    public ClientPersonImportHandler(Workbook workbook) {
        this.workbook = workbook;
        this.clients=new ArrayList<ClientData>();
    }

    @Override
    public void readExcelFile() {
        Sheet clientSheet=workbook.getSheet("ClientPerson");
        Integer noOfEntries=getNumberOfRows(clientSheet,0);
        for (int rowIndex=1;rowIndex<noOfEntries;rowIndex++){
            Row row;
            try {
                row=clientSheet.getRow(rowIndex);
                if (isNotImported(row,STATUS_COL)){
                    clients.add(readClient(row));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ClientData readClient(Row row) {
        Long legalFormId=1L;
        String firstName = readAsString(FIRST_NAME_COL, row);
        String lastName = readAsString(LAST_NAME_COL, row);
        String middleName = readAsString(MIDDLE_NAME_COL, row);
        String officeName = readAsString(OFFICE_NAME_COL, row);
        Long officeId = getIdByName(workbook.getSheet("Offices"), officeName);
        String staffName = readAsString(STAFF_NAME_COL, row);
        Long staffId = getIdByName(workbook.getSheet("Staff"), staffName);
        String externalId = readAsLong(EXTERNAL_ID_COL, row).toString();
        LocalDate submittedOn=readAsDate(SUBMITTED_ON_COL,row);
        Boolean active = readAsBoolean(ACTIVE_COL, row);
        LocalDate activationDate=null;
        if (active) {
            activationDate = readAsDate(ACTIVATION_DATE_COL, row);
        }else {
            activationDate=submittedOn;
        }
        String mobileNo = readAsInt(MOBILE_NO_COL, row).toString();
        LocalDate dob = readAsDate(DOB_COL, row);

        String clientTypeAr[] = readAsString(CLIENT_TYPE_COL, row).split("-");
        Long clientTypeId=Long.parseLong(clientTypeAr[1]);

        String genderAr[] = readAsString(GENDER_COL, row).split("-");
        Long genderId=Long.parseLong(genderAr[1]);

        String clientClassification[] = readAsString(CLIENT_CLASSIFICATION_COL, row).split("-");
        Long clientClassicationId=Long.parseLong(clientClassification[1]);

        Boolean isStaff = readAsBoolean(IS_STAFF_COL, row);

        AddressData addressDataObj=null;
        if (readAsBoolean(ADDRESS_ENABLED,row)) {
            String addressType[] = readAsString(ADDRESS_TYPE_COL, row).split("-");
            Long addressTypeId = Long.parseLong(addressType[1]);

            String street = readAsString(STREET_COL, row);
            String addressLine1 = readAsString(ADDRESS_LINE_1_COL, row);
            String addressLine2 = readAsString(ADDRESS_LINE_2_COL, row);
            String addressLine3 = readAsString(ADDRESS_LINE_3_COL, row);
            String city = readAsString(CITY_COL, row);

            String postalCode = readAsString(POSTAL_CODE_COL, row);
            Boolean isActiveAddress = readAsBoolean(IS_ACTIVE_ADDRESS_COL, row);
            String stateProvinceAr[] = readAsString(STATE_PROVINCE_COL, row).split("-");
            Long stateProvinceId = Long.parseLong(stateProvinceAr[1]);

            String countryAr[] = readAsString(COUNTRY_COL, row).split("-");
            Long countryId = Long.parseLong(countryAr[1]);

            addressDataObj = new AddressData(addressTypeId, street, addressLine1, addressLine2, addressLine3,
                    city, postalCode, isActiveAddress, stateProvinceId, countryId);
        }
        return  new ClientData(legalFormId,row.getRowNum(),firstName,lastName,middleName,submittedOn,activationDate,active,externalId,
                officeId,staffId,mobileNo,dob,clientTypeId,genderId,clientClassicationId,isStaff,addressDataObj);

    }



    @Override
    public void Upload(PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        Sheet clientSheet=workbook.getSheet("ClientPerson");
        for (ClientData client: clients) {
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                String payload=gsonBuilder.registerTypeAdapter(LocalDate.class, new DateSerializer()).create().toJson(client);
                final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                        .createClient() //
                        .withJson(payload) //
                        .build(); //
                final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
                Cell statusCell = clientSheet.getRow(client.getRowIndex()).createCell(STATUS_COL);
                statusCell.setCellValue("Imported");
                statusCell.setCellStyle(getCellStyle(workbook, IndexedColors.LIGHT_GREEN));
            } catch (RuntimeException e) {
                String message = parseStatus(e.getMessage());
                Cell statusCell = clientSheet.getRow(client.getRowIndex()).createCell(STATUS_COL);
                statusCell.setCellValue(message);
                statusCell.setCellStyle(getCellStyle(workbook, IndexedColors.RED));

            }
        }
        clientSheet.setColumnWidth(STATUS_COL, 15000);
        writeString(STATUS_COL, clientSheet.getRow(0), "Status");
    }
}
