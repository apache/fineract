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
package org.apache.fineract.integrationtests.bulkimport.importhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.FeignException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.StaffConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBulkImportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that bulk import of offices and staff is only possible for users holding the respective
 * {@code CREATE_OFFICE} / {@code CREATE_STAFF} permission.
 *
 * <p>
 * Office imports are processed asynchronously row by row, so the no-permission user's upload is accepted but every row
 * is rejected in the output workbook (per-row permission check in {@code OfficeImportHandler}) and no office is
 * created. Staff imports are additionally blocked at the URL level by the CREATE_STAFF rule on the staff upload
 * endpoint, so the upload itself fails with 403.
 */
public class BulkImportPermissionTest {

    private static final String TEST_USER_PASSWORD = "AKleRbDhK421$";
    private static final Long HEAD_OFFICE_ID = 1L;

    private FineractFeignClient adminClient;
    private FineractFeignClient limitedClient;

    @BeforeEach
    public void createUserWithoutPermissions() {
        adminClient = FineractFeignClientHelper.getFineractFeignClient();

        final String roleName = Utils.uniqueRandomStringGenerator("TEST_ROLE_", 10);
        Long roleId = adminClient.roles().createRole(new PostRolesRequest().name(roleName).description("Role without permissions"))
                .getResourceId();

        final String firstname = Utils.randomFirstNameGenerator();
        final String lastname = Utils.randomLastNameGenerator();
        final String userName = Utils.uniqueRandomStringGenerator("testUserName", 4);
        adminClient.users()
                .createUser(new PostUsersRequest().addRolesItem(roleId).email(firstname + "." + lastname + "@whatever.mifos.org")
                        .firstname(firstname).lastname(lastname).repeatPassword(TEST_USER_PASSWORD).sendPasswordToEmail(false)
                        .officeId(HEAD_OFFICE_ID).username(userName).password(TEST_USER_PASSWORD));

        limitedClient = FineractFeignClientHelper.createNewFineractFeignClient(userName, TEST_USER_PASSWORD);
    }

    @Test
    public void testOfficeImportIsRejectedForUserWithoutCreateOfficePermission() throws Exception {
        FeignBulkImportHelper adminBulkImportHelper = new FeignBulkImportHelper(adminClient);
        FeignBulkImportHelper limitedBulkImportHelper = new FeignBulkImportHelper(limitedClient);

        String officeName = Utils.uniqueRandomStringGenerator("Test_Off_", 6);
        Workbook workbook = adminBulkImportHelper.downloadTemplate("offices", Map.of("dateFormat", "dd MMMM yyyy"));
        fillOfficeRow(workbook, officeName);

        // the no-permission user can upload, but the per-row permission check rejects every row.
        // The output workbook is fetched as admin: the limited user lacks even READ_IMPORT, and the
        // processing result is server-side anyway.
        Long importDocumentId = limitedBulkImportHelper.uploadTemplate("offices", Map.of(), workbook, "Office.xls", "en", "dd MMMM yyyy");
        assertNotNull(importDocumentId);

        try (Workbook outputWorkbook = waitForOutputStatus(adminBulkImportHelper, importDocumentId,
                TemplatePopulateImportConstants.OFFICE_SHEET_NAME, 1, OfficeConstants.STATUS_COL)) {
            Sheet officeSheet = outputWorkbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            String status = officeSheet.getRow(1).getCell(OfficeConstants.STATUS_COL).getStringCellValue();
            assertTrue(status.contains("no authority"), "expected permission error in status column but was: " + status);
        }

        // no office was created
        List<GetOfficesResponse> offices = new FeignOfficeHelper(adminClient).retrieveAllOffices();
        assertFalse(offices.stream().anyMatch(office -> officeName.equals(office.getName())),
                "office was created although the user lacks CREATE_OFFICE");

        // positive control: the same workbook imports successfully as admin
        Long adminImportDocumentId = adminBulkImportHelper.uploadTemplate("offices", Map.of(), workbook, "Office.xls", "en",
                "dd MMMM yyyy");
        try (Workbook outputWorkbook = waitForOutputStatus(adminBulkImportHelper, adminImportDocumentId,
                TemplatePopulateImportConstants.OFFICE_SHEET_NAME, 1, OfficeConstants.STATUS_COL)) {
            Sheet officeSheet = outputWorkbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            assertEquals(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED,
                    officeSheet.getRow(1).getCell(OfficeConstants.STATUS_COL).getStringCellValue());
        }
        assertTrue(new FeignOfficeHelper(adminClient).retrieveAllOffices().stream().anyMatch(office -> officeName.equals(office.getName())),
                "admin import did not create the office");
    }

    @Test
    public void testStaffImportIsRejectedForUserWithoutCreateStaffPermission() throws Exception {
        FeignBulkImportHelper adminBulkImportHelper = new FeignBulkImportHelper(adminClient);
        FeignBulkImportHelper limitedBulkImportHelper = new FeignBulkImportHelper(limitedClient);

        Workbook workbook = adminBulkImportHelper.downloadTemplate("staff", Map.of("dateFormat", "dd MMMM yyyy"));
        String officeName = workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME).getRow(1).getCell(1).getStringCellValue();
        fillStaffRow(workbook, officeName);

        // staff upload is protected at the URL level by the CREATE_STAFF rule on the staff upload endpoint
        FeignException.Forbidden exception = assertThrows(FeignException.Forbidden.class,
                () -> limitedBulkImportHelper.uploadTemplate("staff", Map.of(), workbook, "Staff.xls", "en", "dd MMMM yyyy"));
        assertEquals(403, exception.status());
    }

    private static void fillOfficeRow(Workbook workbook, String officeName) throws ParseException {
        Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row firstOfficeRow = sheet.getRow(1);
        firstOfficeRow.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue(officeName);
        firstOfficeRow.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL)
                .setCellValue(firstOfficeRow.getCell(OfficeConstants.LOOKUP_OFFICE_COL).getStringCellValue());
        firstOfficeRow.createCell(OfficeConstants.PARENT_OFFICE_ID_COL)
                .setCellValue(firstOfficeRow.getCell(OfficeConstants.LOOKUP_OFFICE_ID_COL).getNumericCellValue());
        Date date = new SimpleDateFormat("dd MMMM yyyy", Locale.US).parse("14 May 2001");
        firstOfficeRow.createCell(OfficeConstants.OPENED_ON_COL).setCellValue(date);
    }

    private static void fillStaffRow(Workbook workbook, String officeName) {
        Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.EMPLOYEE_SHEET_NAME);
        Row firstStaffRow = sheet.getRow(1);
        if (firstStaffRow == null) {
            firstStaffRow = sheet.createRow(1);
        }
        firstStaffRow.createCell(StaffConstants.OFFICE_NAME_COL).setCellValue(officeName);
        firstStaffRow.createCell(StaffConstants.FIRST_NAME_COL).setCellValue(Utils.randomFirstNameGenerator());
        firstStaffRow.createCell(StaffConstants.LAST_NAME_COL).setCellValue(Utils.randomLastNameGenerator());
        firstStaffRow.createCell(StaffConstants.IS_LOAN_OFFICER).setCellValue(false);
        firstStaffRow.createCell(StaffConstants.JOINED_ON_COL).setCellValue(new Date());
        firstStaffRow.createCell(StaffConstants.EXTERNAL_ID_COL).setCellValue(Utils.uniqueRandomStringGenerator("STF_", 6));
        firstStaffRow.createCell(StaffConstants.IS_ACTIVE_COL).setCellValue(true);
    }

    /**
     * Polls the import output workbook until the given row's status cell is populated (the server processes imports
     * asynchronously) or 60 s have elapsed.
     */
    private static Workbook waitForOutputStatus(FeignBulkImportHelper bulkImportHelper, Long importDocumentId, String sheetName,
            int rowIndex, int statusColumn) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 60_000;
        DataFormatter formatter = new DataFormatter();
        RuntimeException lastException = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                Workbook workbook = new HSSFWorkbook(new ByteArrayInputStream(bulkImportHelper.downloadOutputTemplate(importDocumentId)));
                Sheet sheet = workbook.getSheet(sheetName);
                Row row = sheet == null ? null : sheet.getRow(rowIndex);
                if (row != null && StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(statusColumn)))) {
                    return workbook;
                }
                workbook.close();
            } catch (IOException | RuntimeException e) {
                lastException = new RuntimeException(e);
            }
            Thread.sleep(500);
        }
        throw new AssertionError("no processed bulk import output workbook within 60s for sheet: " + sheetName, lastException);
    }
}
