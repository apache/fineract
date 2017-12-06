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
package org.apache.fineract.integrationtests.bulkimport.importhandler.office;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.populator.AbstractWorkbookPopulator;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OfficeImportHandlerTest {
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup(){
        Utils.initializeRESTAssured();
        this.requestSpec=new RequestSpecBuilder().build();
        this.requestSpec
                .header("Authorization",
                        "Basic "
                                + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200)
                .build();
    }

    @Test
    public void testOfficeImport() throws IOException, InterruptedException, NoSuchFieldException, ParseException {
        OfficeHelper officeHelper=new OfficeHelper(requestSpec,responseSpec);
        Workbook workbook=officeHelper.getOfficeWorkBook("dd MMMM yyyy");

        //insert dummy data into excel
        Sheet sheet=workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row firstOfficeRow= sheet.getRow(1);
        firstOfficeRow.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue(Utils.randomNameGenerator("Test_Off_",6));
        firstOfficeRow.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL).setCellValue(firstOfficeRow.getCell(OfficeConstants.LOOKUP_OFFICE_COL).getStringCellValue());
        firstOfficeRow.createCell(OfficeConstants.PARENT_OFFICE_ID_COL).setCellValue(firstOfficeRow.getCell(OfficeConstants.LOOKUP_OFFICE_ID_COL).getNumericCellValue());
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd MMMM yyyy");
        Date date=simpleDateFormat.parse("14 May 2001");
        firstOfficeRow.createCell(OfficeConstants.OPENED_ON_COL).setCellValue(date);

        String currentdirectory = new File("").getAbsolutePath();
        File directory=new File(currentdirectory+"\\src\\integrationTest\\" +
                "resources\\bulkimport\\importhandler\\office");
        if (!directory.exists())
            directory.mkdirs();
        File file= new File(directory+"\\Office.xls");
        OutputStream outputStream=new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();

        String importDocumentId=officeHelper.importOfficeTemplate(file);
        file.delete();
        Assert.assertNotNull(importDocumentId);

        // Wait for the creation of output excel
        Thread.sleep(3000);

        //check  status column of output excel
        String location=officeHelper.getOutputTemplateLocation(importDocumentId);
        FileInputStream fileInputStream = new FileInputStream(location);
        Workbook outputWorkbook=new HSSFWorkbook(fileInputStream);
        Sheet officeSheet = outputWorkbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row row= officeSheet.getRow(1);
        Assert.assertEquals("Imported",row.getCell(OfficeConstants.STATUS_COL).getStringCellValue());

    }
}
