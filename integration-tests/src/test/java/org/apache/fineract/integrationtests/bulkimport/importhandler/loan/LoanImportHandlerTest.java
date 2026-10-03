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

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.FundRequest;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.infrastructure.bulkimport.constants.LoanConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.bulkimport.importhandler.BulkImportOutputTemplateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBulkImportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFundHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignPaymentTypeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(3)
public class LoanImportHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanImportHandlerTest.class);
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final double DISBURSEMENT_CHARGE_AMOUNT = 100;

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    private final FeignBulkImportHelper bulkImportHelper = new FeignBulkImportHelper(fineractClient);

    @Test
    public void testLoanImport() throws InterruptedException, IOException, ParseException {
        // in order to populate helper sheets
        OfficeHelper officeHelper = new OfficeHelper();
        Integer outcome_office_creation = officeHelper.createOffice(java.time.LocalDate.of(2000, 5, 2)).getResourceId().intValue();
        Assertions.assertNotNull(outcome_office_creation, "Could not create office");

        GetOfficesResponse office = officeHelper.retrieveOffice(outcome_office_creation.longValue());
        Assertions.assertNotNull(office, "Could not retrieve created office");

        String firstName = Utils.randomStringGenerator("Client_FirstName_", 5);
        String lastName = Utils.randomStringGenerator("Client_LastName_", 4);
        String externalId = UUID.randomUUID().toString();

        Long outcome_client_creation = new FeignClientHelper(fineractClient).createClient(new PostClientsRequest()
                .officeId(outcome_office_creation.longValue()).firstname(firstName).lastname(lastName).externalId(externalId)
                .dateFormat(DATE_FORMAT).legalFormId(1L).locale("en").active(true).activationDate("04 March 2011")).getClientId();
        Assertions.assertNotNull(outcome_client_creation, "Could not create client");

        List<HashMap> collaterals = new ArrayList<>();
        HashMap<String, String> collateralHashMap = new HashMap<>();
        FeignCollateralHelper collateralHelper = new FeignCollateralHelper(fineractClient);
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(outcome_client_creation, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        collateralHashMap.put("clientCollateralId", clientCollateralId.toString());
        collateralHashMap.put("quantity", "1");
        collaterals.add(collateralHashMap);

        final ChargeRequest disbursementCharge = ChargeRequestBuilders.loanDisbursementFee(DISBURSEMENT_CHARGE_AMOUNT);

        final Long disbursementChargeId = new FeignChargesHelper(fineractClient).createCharge(disbursementCharge).getResourceId();

        Assertions.assertNotNull(disbursementChargeId, "Could not create charge");

        // in order to populate helper sheets
        Long outcome_group_creation = new FeignGroupHelper(fineractClient).createActiveGroup().getGroupId();
        Assertions.assertNotNull(outcome_group_creation, "Could not create group");

        // in order to populate helper sheets
        FeignStaffHelper staffHelper = new FeignStaffHelper(fineractClient);
        Long outcome_staff_creation = staffHelper.createStaff().getResourceId();
        Assertions.assertNotNull(outcome_staff_creation, "Could not create staff");

        String staffDisplayName = staffHelper.retrieveStaff(outcome_staff_creation).getDisplayName();
        Assertions.assertNotNull(staffDisplayName, "Could not retrieve created staff");

        FeignLoanHelper loanHelper = new FeignLoanHelper(fineractClient);
        LoanProductTestBuilder loanProductTestBuilder = new LoanProductTestBuilder();
        Long outcome_lp_creation = loanHelper.createLoanProduct(loanProductTestBuilder.buildRequest(null)).getResourceId();
        Assertions.assertNotNull(outcome_lp_creation, "Could not create Loan Product");

        GetLoanProductsProductIdResponse loanProduct = loanHelper.retrieveLoanProduct(outcome_lp_creation);
        Assertions.assertNotNull(loanProduct, "Could not get created Loan Product");

        String fundName = Utils.uniqueRandomStringGenerator("", 9);
        Long outcome_fund_creation = new FeignFundHelper(fineractClient)
                .createFund(new FundRequest().name(fundName).externalId(UUID.randomUUID().toString())).getResourceId();
        Assertions.assertNotNull(outcome_fund_creation, "Could not create Fund");

        String paymentTypeName = Utils.randomStringGenerator("P_T", 5);
        String paymentTypeDescription = Utils.randomStringGenerator("PT_Desc", 15);

        var paymentTypesResponse = new FeignPaymentTypeHelper(fineractClient).createPaymentType(
                new PaymentTypeCreateRequest().name(paymentTypeName).description(paymentTypeDescription).isCashPayment(true).position(1L));
        Long outcome_payment_creation = paymentTypesResponse.getResourceId();

        Assertions.assertNotNull(outcome_payment_creation, "Could not create payment type");

        Workbook workbook = bulkImportHelper.downloadTemplate("loans", Map.of("dateFormat", DATE_FORMAT));

        // insert dummy data into loan Sheet
        Sheet loanSheet = workbook.getSheet(TemplatePopulateImportConstants.LOANS_SHEET_NAME);
        Row firstLoanRow = loanSheet.getRow(1);
        firstLoanRow.createCell(LoanConstants.OFFICE_NAME_COL).setCellValue(office.getName());
        firstLoanRow.createCell(LoanConstants.LOAN_TYPE_COL).setCellValue("Individual");
        firstLoanRow.createCell(LoanConstants.CLIENT_NAME_COL)
                .setCellValue(firstName + " " + lastName + "(" + outcome_client_creation + ")");
        firstLoanRow.createCell(LoanConstants.CLIENT_EXTERNAL_ID).setCellValue(externalId);
        firstLoanRow.createCell(LoanConstants.PRODUCT_COL).setCellValue(loanProduct.getName());
        firstLoanRow.createCell(LoanConstants.LOAN_OFFICER_NAME_COL).setCellValue(staffDisplayName);

        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.US);
        final LocalDate localDate = LocalDate.parse("17 May 2017", dateFormat);

        firstLoanRow.createCell(LoanConstants.SUBMITTED_ON_DATE_COL).setCellValue(localDate);
        firstLoanRow.createCell(LoanConstants.APPROVED_DATE_COL).setCellValue(localDate);
        firstLoanRow.createCell(LoanConstants.DISBURSED_DATE_COL).setCellValue(localDate);
        firstLoanRow.createCell(LoanConstants.DISBURSED_PAYMENT_TYPE_COL).setCellValue(paymentTypeName);
        firstLoanRow.createCell(LoanConstants.FUND_NAME_COL).setCellValue(fundName);
        firstLoanRow.createCell(LoanConstants.PRINCIPAL_COL).setCellValue(loanProduct.getPrincipal());
        firstLoanRow.createCell(LoanConstants.NO_OF_REPAYMENTS_COL).setCellValue(loanProduct.getNumberOfRepayments());
        firstLoanRow.createCell(LoanConstants.REPAID_EVERY_COL).setCellValue(loanProduct.getRepaymentEvery());
        firstLoanRow.createCell(LoanConstants.REPAID_EVERY_FREQUENCY_COL).setCellValue(loanProduct.getRepaymentFrequencyType().getValue());
        firstLoanRow.createCell(LoanConstants.LOAN_TERM_COL)
                .setCellValue(loanProduct.getRepaymentEvery() * loanProduct.getNumberOfRepayments());
        firstLoanRow.createCell(LoanConstants.LOAN_TERM_FREQUENCY_COL).setCellValue(loanProduct.getRepaymentFrequencyType().getValue());
        firstLoanRow.createCell(LoanConstants.NOMINAL_INTEREST_RATE_COL).setCellValue(loanProduct.getInterestRatePerPeriod());
        firstLoanRow.createCell(LoanConstants.NOMINAL_INTEREST_RATE_FREQUENCY_COL)
                .setCellValue(loanProduct.getInterestRateFrequencyType().getValue());
        firstLoanRow.createCell(LoanConstants.AMORTIZATION_COL).setCellValue(loanProduct.getAmortizationType().getValue());
        firstLoanRow.createCell(LoanConstants.INTEREST_METHOD_COL).setCellValue(loanProduct.getInterestType().getValue());
        firstLoanRow.createCell(LoanConstants.INTEREST_CALCULATION_PERIOD_COL)
                .setCellValue(loanProduct.getInterestCalculationPeriodType().getValue());
        firstLoanRow.createCell(LoanConstants.ARREARS_TOLERANCE_COL).setCellValue(0);
        firstLoanRow.createCell(LoanConstants.REPAYMENT_STRATEGY_COL).setCellValue(loanProduct.getTransactionProcessingStrategyName());
        firstLoanRow.createCell(LoanConstants.GRACE_ON_PRINCIPAL_PAYMENT_COL).setCellValue(0);
        firstLoanRow.createCell(LoanConstants.GRACE_ON_INTEREST_PAYMENT_COL).setCellValue(0);
        firstLoanRow.createCell(LoanConstants.GRACE_ON_INTEREST_CHARGED_COL).setCellValue(0);
        firstLoanRow.createCell(LoanConstants.FIRST_REPAYMENT_COL).setCellValue(localDate);
        firstLoanRow.createCell(LoanConstants.TOTAL_AMOUNT_REPAID_COL).setCellValue(6000);
        firstLoanRow.createCell(LoanConstants.LAST_REPAYMENT_DATE_COL).setCellValue(localDate);
        firstLoanRow.createCell(LoanConstants.REPAYMENT_TYPE_COL).setCellValue(paymentTypeName);
        firstLoanRow.createCell(LoanConstants.LOAN_COLLATERAL_ID).setCellValue(collaterals.get(0).get("clientCollateralId").toString());
        firstLoanRow.createCell(LoanConstants.LOAN_COLLATERAL_QUANTITY).setCellValue(collaterals.get(0).get("quantity").toString());
        firstLoanRow.createCell(LoanConstants.CHARGE_NAME_1).setCellValue(disbursementCharge.getName());
        firstLoanRow.createCell(LoanConstants.CHARGE_AMOUNT_1).setCellValue(disbursementCharge.getAmount());
        firstLoanRow.createCell(LoanConstants.CHARGE_AMOUNT_TYPE_1)
                .setCellValue(String.valueOf(disbursementCharge.getChargeCalculationType()));

        Long importDocumentId = bulkImportHelper.uploadTemplate("loans", Map.of(), workbook, "Loan.xls", "en", DATE_FORMAT);
        Assertions.assertNotNull(importDocumentId);

        // Wait for the creation of output excel
        Thread.sleep(1000);

        // check status column of output excel
        try (Workbook outputworkbook = BulkImportOutputTemplateHelper.waitForWorkbook(
                () -> bulkImportHelper.downloadOutputTemplate(importDocumentId), TemplatePopulateImportConstants.LOANS_SHEET_NAME, 1,
                LoanConstants.STATUS_COL)) {
            Sheet outputLoanSheet = outputworkbook.getSheet(TemplatePopulateImportConstants.LOANS_SHEET_NAME);
            Row row = outputLoanSheet.getRow(1);

            LOG.info("Failure reason column: {}", row.getCell(LoanConstants.FAILURE_REPORT_COL).getStringCellValue());

            Assertions.assertEquals("Imported", row.getCell(LoanConstants.STATUS_COL).getStringCellValue());
        }
    }
}
