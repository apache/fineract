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
package org.apache.fineract.integrationtests.bulkimport.importhandler.loan;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.apache.fineract.infrastructure.bulkimport.constants.LoanConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.common.*;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
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

public class LoanImportHandlerTest {
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
    public void testLoanImport() throws InterruptedException, IOException, ParseException {
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

       // LoanTransactionHelper ltHelper=new LoanTransactionHelper(requestSpec,responseSpec);
       // LoanProductTestBuilder loanProductTestBuilder=new LoanProductTestBuilder();
       // String jsonLoanProduct=loanProductTestBuilder.build(null);
       // Integer outcome_lp_creaion=ltHelper.getLoanProductId(jsonLoanProduct);
       // Assert.assertNotNull("Could not create Loan Product" ,outcome_lp_creaion);

       // FundsResourceHandler fundsResourceHandler=new FundsResourceHandler();
       // String jsonFund="{\n" +
       //         "\t\"name\": \""+Utils.randomNameGenerator("Fund_Name",9)+"\"\n" +
       //         "}";
       // Integer outcome_fund_creation=fundsResourceHandler.createFund(jsonFund,requestSpec,responseSpec);
       // Assert.assertNotNull("Could not create Fund" ,outcome_fund_creation);

       // PaymentTypeHelper paymentTypeHelper=new PaymentTypeHelper();
       // String name = PaymentTypeHelper.randomNameGenerator("P_T", 5);
       // String description = PaymentTypeHelper.randomNameGenerator("PT_Desc", 15);
       // Boolean isCashPayment = true;
       // Integer position = 1;
       // Integer outcome_payment_creation= paymentTypeHelper.createPaymentType(requestSpec, responseSpec,name,description,isCashPayment,position);
       // Assert.assertNotNull("Could not create payment type" ,outcome_payment_creation);

        LoanTransactionHelper loanTransactionHelper=new LoanTransactionHelper(requestSpec,responseSpec);
        Workbook workbook=loanTransactionHelper.getLoanWorkbook("dd MMMM yyyy");

        //insert dummy data into loan Sheet
        Sheet loanSheet = workbook.getSheet(TemplatePopulateImportConstants.LOANS_SHEET_NAME);
        Row firstLoanRow=loanSheet.getRow(1);
        Sheet officeSheet=workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        firstLoanRow.createCell(LoanConstants.OFFICE_NAME_COL).setCellValue(officeSheet.getRow(1).getCell(1).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.LOAN_TYPE_COL).setCellValue("Individual");
        firstLoanRow.createCell(LoanConstants.CLIENT_NAME_COL).setCellValue(loanSheet.getRow(1).getCell(LoanConstants.LOOKUP_CLIENT_NAME_COL).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.CLIENT_EXTERNAL_ID).setCellValue(loanSheet.getRow(1).getCell(LoanConstants.LOOKUP_CLIENT_EXTERNAL_ID).getStringCellValue());
        Sheet loanProductSheet=workbook.getSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
        firstLoanRow.createCell(LoanConstants.PRODUCT_COL).setCellValue(loanProductSheet.getRow(1).getCell(1).getStringCellValue());
        Sheet staffSheet=workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
        firstLoanRow.createCell(LoanConstants.LOAN_OFFICER_NAME_COL).setCellValue(staffSheet.getRow(1).getCell(1).getStringCellValue());
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd MMMM yyyy");
        Date date=simpleDateFormat.parse("13 May 2017");
        firstLoanRow.createCell(LoanConstants.SUBMITTED_ON_DATE_COL).setCellValue(date);
        firstLoanRow.createCell(LoanConstants.APPROVED_DATE_COL).setCellValue(date);
        firstLoanRow.createCell(LoanConstants.DISBURSED_DATE_COL).setCellValue(date);
        Sheet extrasSheet=workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
        firstLoanRow.createCell(LoanConstants.DISBURSED_PAYMENT_TYPE_COL).setCellValue(extrasSheet.getRow(1).getCell(3).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.FUND_NAME_COL).setCellValue(extrasSheet.getRow(1).getCell(1).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.PRINCIPAL_COL).setCellValue(loanProductSheet.getRow(1).getCell(3).getNumericCellValue());
        firstLoanRow.createCell(LoanConstants.NO_OF_REPAYMENTS_COL).setCellValue(loanProductSheet.getRow(1).getCell(6).getNumericCellValue());
        firstLoanRow.createCell(LoanConstants.REPAID_EVERY_COL).setCellValue(loanProductSheet.getRow(1).getCell(9).getNumericCellValue());
        firstLoanRow.createCell(LoanConstants.REPAID_EVERY_FREQUENCY_COL).setCellValue(loanProductSheet.getRow(1).getCell(10).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.LOAN_TERM_COL).setCellValue(60);
        firstLoanRow.createCell(LoanConstants.LOAN_TERM_FREQUENCY_COL).setCellValue(loanProductSheet.getRow(1).getCell(10).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.NOMINAL_INTEREST_RATE_COL).setCellValue(loanProductSheet.getRow(1).getCell(11).getNumericCellValue());
        firstLoanRow.createCell(LoanConstants.NOMINAL_INTEREST_RATE_FREQUENCY_COL).setCellValue(loanProductSheet.getRow(1).getCell(14).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.AMORTIZATION_COL).setCellValue(loanProductSheet.getRow(1).getCell(15).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.INTEREST_METHOD_COL).setCellValue(loanProductSheet.getRow(1).getCell(16).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.INTEREST_CALCULATION_PERIOD_COL).setCellValue(loanProductSheet.getRow(1).getCell(17).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.ARREARS_TOLERANCE_COL).setCellValue(0);
        firstLoanRow.createCell(LoanConstants.REPAYMENT_STRATEGY_COL).setCellValue(loanProductSheet.getRow(1).getCell(19).getStringCellValue());
        firstLoanRow.createCell(LoanConstants.GRACE_ON_PRINCIPAL_PAYMENT_COL).setCellValue(0);
        firstLoanRow.createCell(LoanConstants.GRACE_ON_INTEREST_PAYMENT_COL).setCellValue(0);
        firstLoanRow.createCell(LoanConstants.GRACE_ON_INTEREST_CHARGED_COL).setCellValue(0);
        firstLoanRow.createCell(LoanConstants.FIRST_REPAYMENT_COL).setCellValue(date);
        firstLoanRow.createCell(LoanConstants.TOTAL_AMOUNT_REPAID_COL).setCellValue(6000);
        firstLoanRow.createCell(LoanConstants.LAST_REPAYMENT_DATE_COL).setCellValue(date);
        firstLoanRow.createCell(LoanConstants.REPAYMENT_TYPE_COL).setCellValue(extrasSheet.getRow(1).getCell(3).getStringCellValue());

        String currentdirectory = new File("").getAbsolutePath();
        File directory=new File(currentdirectory+File.separator+"src"+File.separator+"integrationTest"+File.separator+
                "resources"+File.separator+"bulkimport"+File.separator+"importhandler"+File.separator+"loan") ;
        if (!directory.exists())
            directory.mkdirs();
        File file= new File(directory+File.separator+"Loan.xls");
        OutputStream outputStream=new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();

        String importDocumentId=loanTransactionHelper.importLoanTemplate(file);
        file.delete();
        Assert.assertNotNull(importDocumentId);

        //Wait for the creation of output excel
        Thread.sleep(10000);

        //check status column of output excel
        String location=loanTransactionHelper.getOutputTemplateLocation(importDocumentId);
        FileInputStream fileInputStream = new FileInputStream(location);
        Workbook Outputworkbook=new HSSFWorkbook(fileInputStream);
        Sheet outputLoanSheet = Outputworkbook.getSheet(TemplatePopulateImportConstants.LOANS_SHEET_NAME);
        Row row= outputLoanSheet.getRow(1);
        Assert.assertEquals("Imported",row.getCell(LoanConstants.STATUS_COL).getStringCellValue());

    }
}
