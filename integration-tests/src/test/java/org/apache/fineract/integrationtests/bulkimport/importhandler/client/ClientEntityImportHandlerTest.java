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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.infrastructure.bulkimport.constants.ClientEntityConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.integrationtests.bulkimport.importhandler.BulkImportOutputTemplateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBulkImportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientEntityImportHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientEntityImportHandlerTest.class);

    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    private final FeignBulkImportHelper bulkImportHelper = new FeignBulkImportHelper(fineractClient);
    private final FeignCodeHelper codeHelper = new FeignCodeHelper(fineractClient);

    @Test
    public void testClientImport() throws InterruptedException, IOException, ParseException {

        // in order to populate helper sheets
        Long outcome_staff_creation = new FeignStaffHelper(fineractClient).createStaff().getResourceId();
        Assertions.assertNotNull(outcome_staff_creation, "Could not create staff");

        // in order to populate helper sheets
        OfficeHelper officeHelper = new OfficeHelper();
        Integer outcome_office_creation = officeHelper.createOffice(java.time.LocalDate.of(2000, 5, 2)).getResourceId().intValue();
        Assertions.assertNotNull(outcome_office_creation, "Could not create office");

        // in order to populate helper columns in client entity sheet
        // create constitution
        codeHelper.retrieveOrCreateCodeValueId(24L);
        // create client classification
        codeHelper.retrieveOrCreateCodeValueId(17L);
        // create client types
        codeHelper.retrieveOrCreateCodeValueId(16L);
        // create Address types
        codeHelper.retrieveOrCreateCodeValueId(29L);
        // create State
        codeHelper.retrieveOrCreateCodeValueId(27L);
        // create Country
        codeHelper.retrieveOrCreateCodeValueId(28L);
        // create Main business line
        codeHelper.retrieveOrCreateCodeValueId(25L);

        Workbook workbook = bulkImportHelper.downloadTemplate("clients",
                Map.of("legalFormType", GlobalEntityType.CLIENTS_ENTITY.toString(), "dateFormat", DATE_FORMAT));

        // insert dummy data into client entity sheet
        Sheet clientEntitySheet = workbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
        Row firstClientRow = clientEntitySheet.getRow(1);
        firstClientRow.createCell(ClientEntityConstants.NAME_COL).setCellValue(Utils.randomStringGenerator("C_E_", 6));
        Sheet staffSheet = workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
        firstClientRow.createCell(ClientEntityConstants.OFFICE_NAME_COL).setCellValue(staffSheet.getRow(1).getCell(0).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.STAFF_NAME_COL).setCellValue(staffSheet.getRow(1).getCell(1).getStringCellValue());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Date incoporationDate = simpleDateFormat.parse("14 May 2001");
        firstClientRow.createCell(ClientEntityConstants.INCOPORATION_DATE_COL).setCellValue(incoporationDate);
        Date validTill = simpleDateFormat.parse("14 May 2019");
        firstClientRow.createCell(ClientEntityConstants.INCOPORATION_VALID_TILL_COL).setCellValue(validTill);
        firstClientRow.createCell(ClientEntityConstants.MOBILE_NO_COL).setCellValue(Utils.uniqueRandomNumberGenerator(9));
        firstClientRow.createCell(ClientEntityConstants.CLIENT_TYPE_COL)
                .setCellValue(clientEntitySheet.getRow(1).getCell(ClientEntityConstants.LOOKUP_CLIENT_TYPES).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.CLIENT_CLASSIFICATION_COL)
                .setCellValue(clientEntitySheet.getRow(1).getCell(ClientEntityConstants.LOOKUP_CLIENT_CLASSIFICATION).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.INCOPORATION_NUMBER_COL).setCellValue(Utils.randomNumberGenerator(6));
        firstClientRow.createCell(ClientEntityConstants.MAIN_BUSINESS_LINE)
                .setCellValue(clientEntitySheet.getRow(1).getCell(ClientEntityConstants.LOOKUP_MAIN_BUSINESS_LINE).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.CONSTITUTION_COL)
                .setCellValue(clientEntitySheet.getRow(1).getCell(ClientEntityConstants.LOOKUP_CONSTITUTION_COL).getStringCellValue());
        firstClientRow.createCell(ClientEntityConstants.ACTIVE_COL).setCellValue("False");
        Date submittedDate = simpleDateFormat.parse("28 September 2017");
        firstClientRow.createCell(ClientEntityConstants.SUBMITTED_ON_COL).setCellValue(submittedDate);
        firstClientRow.createCell(ClientEntityConstants.ADDRESS_ENABLED).setCellValue("False");

        Long importDocumentId = bulkImportHelper.uploadTemplate("clients",
                Map.of("legalFormType", GlobalEntityType.CLIENTS_ENTITY.toString()), workbook, "ClientEntity.xls", "en", DATE_FORMAT);
        Assertions.assertNotNull(importDocumentId);

        // check status column of output excel
        try (Workbook outputWorkbook = BulkImportOutputTemplateHelper.waitForWorkbook(
                () -> bulkImportHelper.downloadOutputTemplate(importDocumentId), TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME,
                1, ClientEntityConstants.STATUS_COL)) {
            Sheet outputClientEntitySheet = outputWorkbook.getSheet(TemplatePopulateImportConstants.CLIENT_ENTITY_SHEET_NAME);
            Row row = outputClientEntitySheet.getRow(1);
            String status = row.getCell(ClientEntityConstants.STATUS_COL).getStringCellValue();

            LOG.info("Client import status: {}", status);

            Assertions.assertEquals("Imported", status, () -> "Client import failed: " + status);
        }
    }
}
