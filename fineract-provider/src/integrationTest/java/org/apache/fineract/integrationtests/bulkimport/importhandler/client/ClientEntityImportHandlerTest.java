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
package org.apache.fineract.integrationtests.bulkimport.importhandler.client;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.apache.fineract.infrastructure.bulkimport.constants.ClientEntityConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientEntityImportHandlerTest {
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testClientImport() throws InterruptedException, IOException, ParseException {

        //in order to populate helper sheets
        StaffHelper staffHelper=new StaffHelper();
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        Integer outcome_staff_creation =staffHelper.createStaff(requestSpec,responseSpec);
        Assert.assertNotNull("Could not create staff",outcome_staff_creation);

        //in order to populate helper sheets
        OfficeHelper officeHelper=new OfficeHelper(requestSpec,responseSpec);
        Integer outcome_office_creation=officeHelper.createOffice("02 May 2000");
        Assert.assertNotNull("Could not create office" ,outcome_office_creation);

        //in order to populate helper columns in client entity sheet
        CodeHelper codeHelper=new CodeHelper();
        //create constitution
        codeHelper.retrieveOrCreateCodeValue(24,requestSpec,responseSpec);
        //create client classification
        codeHelper.retrieveOrCreateCodeValue(17,requestSpec,responseSpec);
        //create client types
        codeHelper.retrieveOrCreateCodeValue(16,requestSpec,responseSpec);
        //create Address types
        codeHelper.retrieveOrCreateCodeValue(29,requestSpec,responseSpec);
        //create State
        codeHelper.retrieveOrCreateCodeValue(27,requestSpec,responseSpec);
        //create Country
        codeHelper.retrieveOrCreateCodeValue(28,requestSpec,responseSpec);
        //create Main business line
        codeHelper.retrieveOrCreateCodeValue(25,requestSpec,responseSpec);

        ClientHelper clientHelper=new ClientHelper(requestSpec,responseSpec);
        Workbook workbook=clientHelper.getClientEntityWorkbook(GlobalEntityType.CLIENTS_ENTTTY,"dd MMMM yyyy");

        //insert dummy data into client entity sheet
        Sheet clientEntitySheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
        Row firstClientRow=clientEntitySheet.getRow(1);
        firstClientRow.createCell(ClientEntityConstants.NAME_COL).setCellValue(Utils.randomNameGenerator("C_E_",6));
        Sheet staffSheet=workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
        firstClientRow.createCell(ClientEntityConstants.OFFICE_NAME_COL).setCellValue(staffSheet.getRow(1).getCell(0).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.STAFF_NAME_COL).setCellValue(staffSheet.getRow(1).getCell(1).getStringCellValue());
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd MMMM yyyy");
        Date incoporationDate=simpleDateFormat.parse("14 May 2001");
        firstClientRow.createCell(ClientEntityConstants.INCOPORATION_DATE_COL).setCellValue(incoporationDate);
        Date validTill=simpleDateFormat.parse("14 May 2019");
        firstClientRow.createCell(ClientEntityConstants.INCOPORATION_VALID_TILL_COL).setCellValue(validTill);
        firstClientRow.createCell(ClientEntityConstants.MOBILE_NO_COL).setCellValue(Utils.randomNumberGenerator(7));
        firstClientRow.createCell(ClientEntityConstants.CLIENT_TYPE_COL).setCellValue(clientEntitySheet.getRow(1).getCell(ClientEntityConstants.LOOKUP_CLIENT_TYPES).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.CLIENT_CLASSIFICATION_COL).setCellValue(clientEntitySheet.getRow(1).getCell(ClientEntityConstants.LOOKUP_CLIENT_CLASSIFICATION).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.INCOPORATION_NUMBER_COL).setCellValue(Utils.randomNumberGenerator(6));
        firstClientRow.createCell(ClientEntityConstants.MAIN_BUSINESS_LINE).setCellValue(clientEntitySheet.getRow(1).getCell(ClientEntityConstants.LOOKUP_MAIN_BUSINESS_LINE).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.CONSTITUTION_COL).setCellValue(clientEntitySheet.getRow(1).getCell(ClientEntityConstants.LOOKUP_CONSTITUTION_COL).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.ACTIVE_COL).setCellValue("False");
        Date submittedDate=simpleDateFormat.parse("28 September 2017");
        firstClientRow.createCell(ClientEntityConstants.SUBMITTED_ON_COL).setCellValue(submittedDate);
        firstClientRow.createCell(ClientEntityConstants.ADDRESS_ENABLED).setCellValue("False");

        File directory=new File(System.getProperty("user.home")+File.separator+"Fineract"+File.separator+"bulkimport"+File.separator+"integration_tests"+File.separator+"importhandler"+File.separator+"client") ;
        if (!directory.exists())
            directory.mkdirs();
        File file= new File(directory+File.separator+"ClientEntity.xls");
        OutputStream outputStream=new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();

        String importDocumentId=clientHelper.importClientEntityTemplate(file);
        file.delete();
        Assert.assertNotNull(importDocumentId);

        //Wait for the creation of output excel
        Thread.sleep(3000);

        //check status column of output excel
        String location=clientHelper.getOutputTemplateLocation(importDocumentId);
        FileInputStream fileInputStream = new FileInputStream(location);
        Workbook outputWorkbook=new HSSFWorkbook(fileInputStream);
        Sheet outputClientEntitySheet = outputWorkbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
        Row row= outputClientEntitySheet.getRow(1);
        Assert.assertEquals("Imported",row.getCell(ClientEntityConstants.STATUS_COL).getStringCellValue());

    }
}
