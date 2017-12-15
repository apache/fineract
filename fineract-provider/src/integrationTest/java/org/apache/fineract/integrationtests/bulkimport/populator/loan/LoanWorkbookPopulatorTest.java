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
package org.apache.fineract.integrationtests.bulkimport.populator.loan;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.integrationtests.common.*;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
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

public class LoanWorkbookPopulatorTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup(){
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }
    @Test
    public void testLoanWorkbookPopulate() throws IOException {
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        //in order to populate helper sheets
        OfficeHelper officeHelper=new OfficeHelper(requestSpec,responseSpec);
        Integer outcome_office_creation=officeHelper.createOffice("02 May 2000");
        Assert.assertNotNull("Could not create office" ,outcome_office_creation);

        //in order to populate helper sheets
        ClientHelper clientHelper=new ClientHelper(requestSpec,responseSpec);
        Integer outcome_client_creation=clientHelper.createClient(requestSpec,responseSpec);
        Assert.assertNotNull("Could not create client" ,outcome_client_creation);

        //in order to populate helper sheets
        GroupHelper groupHelper=new GroupHelper(requestSpec,responseSpec);
        Integer outcome_group_creation=groupHelper.createGroup(requestSpec,responseSpec,true);
        Assert.assertNotNull("Could not create group" ,outcome_group_creation);

        //in order to populate helper sheets
        StaffHelper staffHelper=new StaffHelper();
        Integer outcome_staff_creation =staffHelper.createStaff(requestSpec,responseSpec);
        Assert.assertNotNull("Could not create staff",outcome_staff_creation);

        LoanTransactionHelper loanTransactionHelper=new LoanTransactionHelper(requestSpec,responseSpec);
        LoanProductTestBuilder loanProductTestBuilder=new LoanProductTestBuilder();
        String jsonLoanProduct=loanProductTestBuilder.build(null);
        Integer outcome_lp_creaion=loanTransactionHelper.getLoanProductId(jsonLoanProduct);
        Assert.assertNotNull("Could not create Loan Product" ,outcome_lp_creaion);

        FundsResourceHandler fundsResourceHandler=new FundsResourceHandler();
        String jsonFund="{\n" +
                "\t\"name\": \""+Utils.randomNameGenerator("Fund_Name",9)+"\"\n" +
                "}";
        Integer outcome_fund_creation=fundsResourceHandler.createFund(jsonFund,requestSpec,responseSpec);
        Assert.assertNotNull("Could not create Fund" ,outcome_fund_creation);

        PaymentTypeHelper paymentTypeHelper=new PaymentTypeHelper();
        String name = PaymentTypeHelper.randomNameGenerator("P_T", 5);
        String description = PaymentTypeHelper.randomNameGenerator("PT_Desc", 15);
        Boolean isCashPayment = true;
        Integer position = 1;
        Integer outcome_payment_creation= paymentTypeHelper.createPaymentType(requestSpec, responseSpec,name,description,isCashPayment,position);
        Assert.assertNotNull("Could not create payment type" ,outcome_payment_creation);

        Workbook workbook=loanTransactionHelper.getLoanWorkbook("dd MMMM yyyy");

        Sheet officeSheet=workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row firstOfficeRow=officeSheet.getRow(1);
        Assert.assertNotNull("No offices found ",firstOfficeRow.getCell(1));

        Sheet clientSheet=workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME);
        Row firstClientRow=clientSheet.getRow(1);
        Assert.assertNotNull("No clients found ",firstClientRow.getCell(1));

        Sheet groupSheet=workbook.getSheet(TemplatePopulateImportConstants.GROUP_SHEET_NAME);
        Row firstGroupRow=groupSheet.getRow(1);
        Assert.assertNotNull("No groups found ",firstGroupRow.getCell(1));

        Sheet staffSheet=workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
        Row firstStaffRow=staffSheet.getRow(1);
        Assert.assertNotNull("No staff found ",firstStaffRow.getCell(1));

        Sheet productSheet=workbook.getSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
        Row firstProductRow=productSheet.getRow(1);
        Assert.assertNotNull("No products found ",firstProductRow.getCell(1));

        Sheet extrasSheet=workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
        Row firstExtrasRow=extrasSheet.getRow(1);
        Assert.assertNotNull("No Extras found ",firstExtrasRow.getCell(1));
    }
}
