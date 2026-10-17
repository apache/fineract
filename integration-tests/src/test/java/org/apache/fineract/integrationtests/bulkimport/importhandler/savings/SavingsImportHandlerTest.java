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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.infrastructure.bulkimport.constants.SavingsConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.bulkimport.importhandler.BulkImportOutputTemplateHelper;
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
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsTestLifecycleExtension;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith({ SavingsTestLifecycleExtension.class })
public class SavingsImportHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsImportHandlerTest.class);

    public static final String DATE_FORMAT = "dd MMMM yyyy";

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    private final FeignBulkImportHelper bulkImportHelper = new FeignBulkImportHelper(fineractClient);

    @Test
    public void testSavingsImport() throws InterruptedException, IOException, ParseException {

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
                .officeId(outcome_office_creation.longValue()).legalFormId(1L).firstname(firstName).lastname(lastName)
                .externalId(externalId).dateFormat(DATE_FORMAT).locale("en").active(true).activationDate("04 March 2011")).getClientId();
        Assertions.assertNotNull(outcome_client_creation, "Could not create client");

        // in order to populate helper sheets
        Long outcome_group_creation = new FeignGroupHelper(fineractClient).createActiveGroup().getGroupId();
        Assertions.assertNotNull(outcome_group_creation, "Could not create group");

        // in order to populate helper sheets
        FeignStaffHelper staffHelper = new FeignStaffHelper(fineractClient);
        Long outcome_staff_creation = staffHelper.createStaff().getResourceId();
        Assertions.assertNotNull(outcome_staff_creation, "Could not create staff");

        String staffDisplayName = staffHelper.retrieveStaff(outcome_staff_creation).getDisplayName();
        Assertions.assertNotNull(staffDisplayName, "Could not retrieve created staff");

        Long outcome_sp_creaction = new FeignSavingsProductHelper(fineractClient)
                .createSavingsProduct(SavingsRequestBuilders.savingsProduct(InterestCompoundingPeriodType.MONTHLY,
                        InterestPostingPeriodType.MONTHLY, InterestCalculationType.DAILY_BALANCE))
                .getResourceId();
        Assertions.assertNotNull(outcome_sp_creaction, "Could not create Savings product");

        Workbook workbook = bulkImportHelper.downloadTemplate("savingsaccounts", Map.of("dateFormat", DATE_FORMAT));

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
        firstSavingsRow.createCell(SavingsConstants.FIELD_OFFICER_NAME_COL).setCellValue(staffDisplayName);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Date date = simpleDateFormat.parse("13 May 2017");
        firstSavingsRow.createCell(SavingsConstants.SUBMITTED_ON_DATE_COL).setCellValue(date);
        firstSavingsRow.createCell(SavingsConstants.APPROVED_DATE_COL).setCellValue(date);
        firstSavingsRow.createCell(SavingsConstants.ACTIVATION_DATE_COL).setCellValue(date);
        firstSavingsRow.createCell(SavingsConstants.CURRENCY_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(10).getStringCellValue());
        firstSavingsRow.createCell(SavingsConstants.DECIMAL_PLACES_COL)
                .setCellValue(savingsProductSheet.getRow(1).getCell(11).getNumericCellValue());
        safeNumericValueSetter(firstSavingsRow, SavingsConstants.IN_MULTIPLES_OF_COL, savingsProductSheet, 1, 12);
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

        Long importDocumentId = bulkImportHelper.uploadTemplate("savingsaccounts", Map.of(), workbook, "Savings.xls", "en", DATE_FORMAT);
        Assertions.assertNotNull(importDocumentId);

        // Wait for the creation of output excel
        Thread.sleep(1000);

        // check status column of output excel
        try (Workbook wb = BulkImportOutputTemplateHelper.waitForWorkbook(() -> bulkImportHelper.downloadOutputTemplate(importDocumentId),
                TemplatePopulateImportConstants.SAVINGS_ACCOUNTS_SHEET_NAME, 1, SavingsConstants.STATUS_COL)) {
            Sheet sheet = wb.getSheet(TemplatePopulateImportConstants.SAVINGS_ACCOUNTS_SHEET_NAME);
            Row row = sheet.getRow(1);

            LOG.info("Failure reason column: {}", row.getCell(SavingsConstants.STATUS_COL).getStringCellValue());

            Assertions.assertEquals("Imported", row.getCell(SavingsConstants.STATUS_COL).getStringCellValue());
        }
    }

    private void safeNumericValueSetter(Row targetRow, int targetColId, Sheet sourceSheet, int rowId, int colId) {
        Row row = sourceSheet.getRow(rowId);
        if (row == null) {
            targetRow.createCell(targetColId).setBlank();
        } else {
            Cell cell = row.getCell(colId);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                targetRow.createCell(targetColId).setBlank();
            } else {
                targetRow.createCell(targetColId).setCellValue(cell.getNumericCellValue());
            }
        }
    }
}
