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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.bulkimport.constants.OfficeConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.bulkimport.importhandler.BulkImportOutputTemplateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBulkImportHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class OfficeImportHandlerTest {

    private final FeignBulkImportHelper bulkImportHelper = new FeignBulkImportHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testOfficeImport() throws IOException, InterruptedException, ParseException {
        Workbook workbook = bulkImportHelper.downloadTemplate("offices", Map.of("dateFormat", "dd MMMM yyyy"));

        // insert dummy data into excel
        Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row firstOfficeRow = sheet.getRow(1);
        firstOfficeRow.createCell(OfficeConstants.OFFICE_NAME_COL).setCellValue(Utils.uniqueRandomStringGenerator("Test_Off_", 6));
        firstOfficeRow.createCell(OfficeConstants.PARENT_OFFICE_NAME_COL)
                .setCellValue(firstOfficeRow.getCell(OfficeConstants.LOOKUP_OFFICE_COL).getStringCellValue());
        firstOfficeRow.createCell(OfficeConstants.PARENT_OFFICE_ID_COL)
                .setCellValue(firstOfficeRow.getCell(OfficeConstants.LOOKUP_OFFICE_ID_COL).getNumericCellValue());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        Date date = simpleDateFormat.parse("14 May 2001");
        firstOfficeRow.createCell(OfficeConstants.OPENED_ON_COL).setCellValue(date);

        Long importDocumentId = bulkImportHelper.uploadTemplate("offices", Map.of(), workbook, "Office.xls", "en", "dd MMMM yyyy");
        Assertions.assertNotNull(importDocumentId);

        // Wait for the creation of output excel
        Thread.sleep(1000);

        // check status column of output excel
        try (Workbook outputWorkbook = BulkImportOutputTemplateHelper.waitForWorkbook(
                () -> bulkImportHelper.downloadOutputTemplate(importDocumentId), TemplatePopulateImportConstants.OFFICE_SHEET_NAME, 1,
                OfficeConstants.STATUS_COL)) {
            Sheet officeSheet = outputWorkbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
            Row row = officeSheet.getRow(1);

            log.info("Failure reason column: {}", row.getCell(OfficeConstants.STATUS_COL).getStringCellValue());

            Assertions.assertEquals("Imported", row.getCell(OfficeConstants.STATUS_COL).getStringCellValue());
        }
    }
}
