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
package org.apache.fineract.integrationtests.bulkimport.populator.savings;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBulkImportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData.InterestCalculationType;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData.InterestCompoundingPeriodType;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData.InterestPostingPeriodType;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

public class SavingsWorkbookPopulateTest {

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();

    @Test
    public void testSavingsWorkbookPopulate() {
        // in order to populate helper sheets
        OfficeHelper officeHelper = new OfficeHelper();
        Integer outcome_office_creation = officeHelper.createOffice(java.time.LocalDate.of(2000, 5, 2)).getResourceId().intValue();
        assertNotNull(outcome_office_creation, "Could not create office");

        // in order to populate helper sheets
        Long outcome_client_creation = new FeignClientHelper(fineractClient).createClient();
        assertNotNull(outcome_client_creation, "Could not create client");

        // in order to populate helper sheets
        Long outcome_group_creation = new FeignGroupHelper(fineractClient).createActiveGroup().getGroupId();
        assertNotNull(outcome_group_creation, "Could not create group");

        // in order to populate helper sheets
        Long outcome_staff_creation = new FeignStaffHelper(fineractClient).createStaff().getResourceId();
        assertNotNull(outcome_staff_creation, "Could not create staff");

        Long outcome_sp_creaction = new FeignSavingsProductHelper(fineractClient)
                .createSavingsProduct(SavingsRequestBuilders.savingsProduct(InterestCompoundingPeriodType.MONTHLY,
                        InterestPostingPeriodType.MONTHLY, InterestCalculationType.DAILY_BALANCE))
                .getResourceId();
        assertNotNull(outcome_sp_creaction, "Could not create Savings product");

        Workbook workbook = new FeignBulkImportHelper(fineractClient).downloadTemplate("savingsaccounts",
                Map.of("dateFormat", "dd MMMM yyyy"));

        Sheet officeSheet = workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row firstOfficeRow = officeSheet.getRow(1);
        assertNotNull(firstOfficeRow.getCell(1), "No offices found ");

        Sheet clientSheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME);
        Row firstClientRow = clientSheet.getRow(1);
        assertNotNull(firstClientRow.getCell(1), "No clients found ");

        Sheet groupSheet = workbook.getSheet(TemplatePopulateImportConstants.GROUP_SHEET_NAME);
        Row firstGroupRow = groupSheet.getRow(1);
        assertNotNull(firstGroupRow.getCell(1), "No groups found ");

        Sheet staffSheet = workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
        Row firstStaffRow = staffSheet.getRow(1);
        assertNotNull(firstStaffRow.getCell(1), "No staff found ");

        Sheet productSheet = workbook.getSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
        Row firstProductRow = productSheet.getRow(1);
        assertNotNull(firstProductRow.getCell(1), "No products found ");
    }
}
