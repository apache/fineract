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
package org.apache.fineract.integrationtests.bulkimport.importhandler.center;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.models.GetCentersPageItems;
import org.apache.fineract.infrastructure.bulkimport.constants.CenterConstants;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.integrationtests.bulkimport.importhandler.BulkImportOutputTemplateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBulkImportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCenterHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

/** Centers imported from the bulk import template are created through the typed CenterCreateCommand. */
public class CenterImportHandlerTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private final FeignBulkImportHelper bulkImportHelper = new FeignBulkImportHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignCenterHelper centerHelper = new FeignCenterHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void importsAnActiveCenter() throws Exception {
        // a new office, so its name occurs once in the template's office sheet
        final OfficeHelper officeHelper = new OfficeHelper();
        final Long officeId = officeHelper.createOffice(LocalDate.of(2000, 5, 2)).getResourceId();
        final String officeName = officeHelper.retrieveOffice(officeId).getName();
        final Workbook workbook = bulkImportHelper.downloadTemplate("centers", Map.of("dateFormat", DATE_FORMAT));
        final String centerName = Utils.uniqueRandomStringGenerator("Imported_Center_", 6);
        final Date activationDate = new SimpleDateFormat(DATE_FORMAT, Locale.US).parse("04 March 2011");

        final Sheet sheet = workbook.getSheet(TemplatePopulateImportConstants.CENTER_SHEET_NAME);
        final Row row = sheet.getRow(1) == null ? sheet.createRow(1) : sheet.getRow(1);
        row.createCell(CenterConstants.CENTER_NAME_COL).setCellValue(centerName);
        row.createCell(CenterConstants.OFFICE_NAME_COL).setCellValue(officeName);
        row.createCell(CenterConstants.ACTIVE_COL).setCellValue(true);
        row.createCell(CenterConstants.ACTIVATION_DATE_COL).setCellValue(activationDate);
        row.createCell(CenterConstants.SUBMITTED_ON_DATE_COL).setCellValue(activationDate);

        final Long importDocumentId = bulkImportHelper.uploadTemplate("centers", Map.of(), workbook, "Center.xls", "en", DATE_FORMAT);
        assertNotNull(importDocumentId);

        try (Workbook output = BulkImportOutputTemplateHelper.waitForWorkbook(
                () -> bulkImportHelper.downloadOutputTemplate(importDocumentId), TemplatePopulateImportConstants.CENTER_SHEET_NAME, 1,
                CenterConstants.STATUS_COL)) {
            final Row outputRow = output.getSheet(TemplatePopulateImportConstants.CENTER_SHEET_NAME).getRow(1);
            final String failure = outputRow.getCell(CenterConstants.FAILURE_COL) == null ? ""
                    : outputRow.getCell(CenterConstants.FAILURE_COL).getStringCellValue();
            assertEquals(TemplatePopulateImportConstants.STATUS_CELL_IMPORTED,
                    outputRow.getCell(CenterConstants.STATUS_COL).getStringCellValue(), failure);
        }

        final List<GetCentersPageItems> imported = centerHelper.listCenters().stream().filter(c -> centerName.equals(c.getName())).toList();
        assertEquals(1, imported.size());
        assertEquals(Boolean.TRUE, imported.get(0).getActive());
        assertEquals(officeId, imported.get(0).getOfficeId());
    }
}
