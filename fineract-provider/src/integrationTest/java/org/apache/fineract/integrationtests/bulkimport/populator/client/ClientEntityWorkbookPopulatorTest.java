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
package org.apache.fineract.integrationtests.bulkimport.populator.client;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

public class ClientEntityWorkbookPopulatorTest {

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
    public void testClientEntityWorkbookPopulate() throws IOException {
        //in order to populate helper sheets
        StaffHelper staffHelper=new StaffHelper();
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        Integer outcome_staff_creation =staffHelper.createStaff(requestSpec,responseSpec);
        Assert.assertNotNull("Could not create staff",outcome_staff_creation);

        //in order to populate helper sheets
        OfficeHelper officeHelper=new OfficeHelper(requestSpec,responseSpec);
        Integer outcome_office_creation=officeHelper.createOffice("02 May 2000");
        Assert.assertNotNull("Could not create office" ,outcome_office_creation);

        ClientHelper clientHelper=new ClientHelper(requestSpec,responseSpec);
        Workbook workbook=clientHelper.getClientEntityWorkbook(GlobalEntityType.CLIENTS_ENTTTY,"dd MMMM yyyy");
        Sheet officeSheet=workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row firstOfficeRow=officeSheet.getRow(1);
        Assert.assertNotNull("No offices found for given OfficeId ",firstOfficeRow.getCell(1));
        Sheet staffSheet=workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
        Row firstStaffRow=staffSheet.getRow(1);
        Assert.assertNotNull("No staff found for given staffId",firstStaffRow.getCell(1));
    }
}
