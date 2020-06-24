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
package org.apache.fineract.integrationtests.bulkimport.importhandler.savings;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.bulkimport.constants.SavingsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.common.GroupHelper;
import org.apache.fineract.integrationtests.common.OfficeDomain;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavingsImportHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsImportHandlerTest.class);

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    private static final String CREATE_CLIENT_URL = "/fineract-provider/api/v1/clients?" + Utils.TENANT_IDENTIFIER;
    public static final String DATE_FORMAT = "dd MMMM yyyy";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testSavingsImport() throws InterruptedException, IOException, ParseException {

        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        // in order to populate helper sheets
        OfficeHelper officeHelper = new OfficeHelper(requestSpec, responseSpec);
        Integer outcome_office_creation = officeHelper.createOffice("02 May 2000");
        Assertions.assertNotNull(outcome_office_creation, "Could not create office");

        OfficeDomain office = officeHelper.retrieveOfficeByID(outcome_office_creation);
        Assertions.assertNotNull(office, "Could not retrieve created office");

        String firstName = Utils.randomNameGenerator("Client_FirstName_", 5);
        String lastName = Utils.randomNameGenerator("Client_LastName_", 4);
        String externalId = Utils.randomStringGenerator("ID_", 7, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        final HashMap<String, String> clientMap = new HashMap<>();
        clientMap.put("officeId", outcome_office_creation.toString());
        clientMap.put("firstname", firstName);
        clientMap.put("lastname", lastName);
        clientMap.put("externalId", externalId);
        clientMap.put("dateFormat", DATE_FORMAT);
        clientMap.put("locale", "en");
        clientMap.put("active", "true");
        clientMap.put("activationDate", "04 March 2011");

        Integer outcome_client_creation = Utils.performServerPost(requestSpec, responseSpec, CREATE_CLIENT_URL,
                new Gson().toJson(clientMap), "clientId");
        Assertions.assertNotNull(outcome_client_creation, "Could not create client");

        // in order to populate helper sheets
        Integer outcome_group_creation = GroupHelper.createGroup(requestSpec, responseSpec, true);
        Assertions.assertNotNull(outcome_group_creation, "Could not create group");

        // in order to populate helper sheets
        Integer outcome_staff_creation = StaffHelper.createStaff(requestSpec, responseSpec);
        Assertions.assertNotNull(outcome_staff_creation, "Could not create staff");

        Map<String, Object> staffMap = StaffHelper.getStaff(requestSpec, responseSpec, outcome_staff_creation);
        Assertions.assertNotNull(staffMap, "Could not retrieve created staff");

        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        String jsonSavingsProduct = savingsProductHelper.build();
        Integer outcome_sp_creaction = SavingsProductHelper.createSavingsProduct(jsonSavingsProduct, requestSpec, responseSpec);
        Assertions.assertNotNull(outcome_sp_creaction, "Could not create Savings product");

        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
        Workbook workbook = savingsAccountHelper.getSavingsWorkbook("dd MMMM yyyy");

        // insert dummy data into Savings sheet
        Sheet savingsSheet = workbook.getSheet(TemplatePopulateImportConstants.SAVINGS_ACCOUNTS_SHEET_NAME);
        Row firstSavingsRow = savingsSheet.getRow(1);
        firstSavingsRow.createCell(SavingsConstants.OFFICE_NAME_COL).setCellValue(office.getName());
        firstSavingsRow.createCell(SavingsConstants.SAVINGS_TYPE_COL).setCellValue("Individual");
        firstSavingsRow.createCell(SavingsConstants.CLIENT_NAME_COL)
                .setCellValue(firstName + " " + lastName + "(" + outcome_client_creation + ")");
        Sheet savingsProductSheet = workbook.getSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
        firstSavingsRow.createCell(SavingsConstants.PRODUCT_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(1).getStringCellValue());
        firstSavingsRow.createCell(SavingsConstants.FIELD_OFFICER_NAME_COL).setCellValue((String) staffMap.get("displayName"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");
        Date date = simpleDateFormat.parse("13 May 2017");
        firstSavingsRow.createCell(SavingsConstants.SUBMITTED_ON_DATE_COL).setCellValue(date);
        firstSavingsRow.createCell(SavingsConstants.APPROVED_DATE_COL).setCellValue(date);
        firstSavingsRow.createCell(SavingsConstants.ACTIVATION_DATE_COL).setCellValue(date);
        firstSavingsRow.createCell(SavingsConstants.CURRENCY_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(10).getStringCellValue());
        firstSavingsRow.createCell(SavingsConstants.DECIMAL_PLACES_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(11).getNumericCellValue());
        firstSavingsRow.createCell(SavingsConstants.IN_MULTIPLES_OF_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(12).getNumericCellValue());
        firstSavingsRow.createCell(SavingsConstants.NOMINAL_ANNUAL_INTEREST_RATE_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(2).getNumericCellValue());
        firstSavingsRow.createCell(SavingsConstants.INTEREST_COMPOUNDING_PERIOD_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(3).getStringCellValue());
        firstSavingsRow.createCell(SavingsConstants.INTEREST_POSTING_PERIOD_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(4).getStringCellValue());
        firstSavingsRow.createCell(SavingsConstants.INTEREST_CALCULATION_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(5).getStringCellValue());
        firstSavingsRow.createCell(SavingsConstants.INTEREST_CALCULATION_DAYS_IN_YEAR_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(6).getStringCellValue());
        firstSavingsRow.createCell(SavingsConstants.MIN_OPENING_BALANCE_COL).setCellValue(1000.0);
        firstSavingsRow.createCell(SavingsConstants.LOCKIN_PERIOD_COL).setCellValue(1);
        firstSavingsRow.createCell(SavingsConstants.LOCKIN_PERIOD_FREQUENCY_COL).setCellValue("Weeks");
        firstSavingsRow.createCell(SavingsConstants.APPLY_WITHDRAWAL_FEE_FOR_TRANSFERS).setCellValue("False");
        firstSavingsRow.createCell(SavingsConstants.ALLOW_OVER_DRAFT_COL).setCellValue("False");
        firstSavingsRow.createCell(SavingsConstants.OVER_DRAFT_LIMIT_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(15).getNumericCellValue());

        String currentdirectory = new File("").getAbsolutePath();
        File directory = new File(currentdirectory + File.separator + "src" + File.separator + "integrationTest" + File.separator
                + "resources" + File.separator + "bulkimport" + File.separator + "importhandler" + File.separator + "savings");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory + File.separator + "Savings.xls");
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();

        String importDocumentId = savingsAccountHelper.importSavingsTemplate(file);
        file.delete();
        Assertions.assertNotNull(importDocumentId);

        // Wait for the creation of output excel
        Thread.sleep(10000);

        // check status column of output excel
        String location = savingsAccountHelper.getOutputTemplateLocation(importDocumentId);
        FileInputStream fileInputStream = new FileInputStream(location);
        Workbook Outputworkbook = new HSSFWorkbook(fileInputStream);
        Sheet OutputSavingsSheet = Outputworkbook.getSheet(TemplatePopulateImportConstants.SAVINGS_ACCOUNTS_SHEET_NAME);
        Row row = OutputSavingsSheet.getRow(1);

        LOG.info("Output location: {}", location);
        LOG.info("Failure reason column: {}", row.getCell(SavingsConstants.STATUS_COL).getStringCellValue());

        Assertions.assertEquals("Imported", row.getCell(SavingsConstants.STATUS_COL).getStringCellValue());
        Outputworkbook.close();
    }
}
