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

import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.FundRequest;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBulkImportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFundHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignPaymentTypeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanWorkbookPopulatorTest {

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();

    @Test
    public void testLoanWorkbookPopulate() {
        // in order to populate helper sheets
        OfficeHelper officeHelper = new OfficeHelper();
        Integer outcome_office_creation = officeHelper.createOffice(java.time.LocalDate.of(2000, 5, 2)).getResourceId().intValue();
        Assertions.assertNotNull(outcome_office_creation, "Could not create office");

        // in order to populate helper sheets
        Long outcome_client_creation = new FeignClientHelper(fineractClient).createClient();
        Assertions.assertNotNull(outcome_client_creation, "Could not create client");

        // in order to populate helper sheets
        Long outcome_group_creation = new FeignGroupHelper(fineractClient).createActiveGroup().getGroupId();
        Assertions.assertNotNull(outcome_group_creation, "Could not create group");

        // in order to populate helper sheets
        Long outcome_staff_creation = new FeignStaffHelper(fineractClient).createStaff().getResourceId();
        Assertions.assertNotNull(outcome_staff_creation, "Could not create staff");

        LoanProductTestBuilder loanProductTestBuilder = new LoanProductTestBuilder();
        Long outcome_lp_creaion = new FeignLoanHelper(fineractClient).createLoanProduct(loanProductTestBuilder.buildRequest(null))
                .getResourceId();
        Assertions.assertNotNull(outcome_lp_creaion, "Could not create Loan Product");

        Long outcome_fund_creation = new FeignFundHelper(fineractClient)
                .createFund(new FundRequest().name(Utils.uniqueRandomStringGenerator("Fund_Name", 9))).getResourceId();
        Assertions.assertNotNull(outcome_fund_creation, "Could not create Fund");

        String name = Utils.randomStringGenerator("P_T", 5);
        String description = Utils.randomStringGenerator("PT_Desc", 15);
        Boolean isCashPayment = true;
        Long position = 1L;
        var paymentTypesResponse = new FeignPaymentTypeHelper(fineractClient).createPaymentType(
                new PaymentTypeCreateRequest().name(name).description(description).isCashPayment(isCashPayment).position(position));
        Long outcome_payment_creation = paymentTypesResponse.getResourceId();
        Assertions.assertNotNull(outcome_payment_creation, "Could not create payment type");

        Workbook workbook = new FeignBulkImportHelper(fineractClient).downloadTemplate("loans", Map.of("dateFormat", "dd MMMM yyyy"));

        Sheet officeSheet = workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row firstOfficeRow = officeSheet.getRow(1);
        Assertions.assertNotNull(firstOfficeRow.getCell(1), "No offices found ");

        Sheet clientSheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME);
        Row firstClientRow = clientSheet.getRow(1);
        Assertions.assertNotNull(firstClientRow.getCell(1), "No clients found ");

        Sheet groupSheet = workbook.getSheet(TemplatePopulateImportConstants.GROUP_SHEET_NAME);
        Row firstGroupRow = groupSheet.getRow(1);
        Assertions.assertNotNull(firstGroupRow.getCell(1), "No groups found ");

        Sheet staffSheet = workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
        Row firstStaffRow = staffSheet.getRow(1);
        Assertions.assertNotNull(firstStaffRow.getCell(1), "No staff found ");

        Sheet productSheet = workbook.getSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
        Row firstProductRow = productSheet.getRow(1);
        Assertions.assertNotNull(firstProductRow.getCell(1), "No products found ");

        Sheet extrasSheet = workbook.getSheet(TemplatePopulateImportConstants.EXTRAS_SHEET_NAME);
        Row firstExtrasRow = extrasSheet.getRow(1);
        Assertions.assertNotNull(firstExtrasRow.getCell(1), "No Extras found ");
    }
}
