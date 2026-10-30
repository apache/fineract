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
package org.apache.fineract.integrationtests.bulkimport.populator.client;

import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBulkImportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientEntityWorkbookPopulatorTest {

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();

    @Test
    public void testClientEntityWorkbookPopulate() {
        // in order to populate helper sheets
        Long outcome_staff_creation = new FeignStaffHelper(fineractClient).createStaff().getResourceId();
        Assertions.assertNotNull(outcome_staff_creation, "Could not create staff");

        // in order to populate helper sheets
        OfficeHelper officeHelper = new OfficeHelper();
        Integer outcome_office_creation = officeHelper.createOffice(java.time.LocalDate.of(2000, 5, 2)).getResourceId().intValue();
        Assertions.assertNotNull(outcome_office_creation, "Could not create office");

        Workbook workbook = new FeignBulkImportHelper(fineractClient).downloadTemplate("clients",
                Map.of("legalFormType", GlobalEntityType.CLIENTS_ENTITY.toString(), "dateFormat", "dd MMMM yyyy"));
        Sheet officeSheet = workbook.getSheet(TemplatePopulateImportConstants.OFFICE_SHEET_NAME);
        Row firstOfficeRow = officeSheet.getRow(1);
        Assertions.assertNotNull(firstOfficeRow.getCell(1), "No offices found for given OfficeId ");
        Sheet staffSheet = workbook.getSheet(TemplatePopulateImportConstants.STAFF_SHEET_NAME);
        Row firstStaffRow = staffSheet.getRow(1);
        Assertions.assertNotNull(firstStaffRow.getCell(1), "No staff found for given staffId");
    }
}
