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
package org.apache.fineract.infrastructure.bulkimport.populator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.portfolio.savings.data.RecurringDepositProductData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class RecurringDepositProductSheetPopulatorTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final int NAME_COL = 1;

    // Deep stubs cover the enum-valued lookups (interest compounding/posting/calculation, …) that the populator
    // always dereferences; the test only pins the field under test.
    private RecurringDepositProductData product(String name) {
        RecurringDepositProductData product = mock(RecurringDepositProductData.class, RETURNS_DEEP_STUBS);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn(name);
        when(product.getShortName()).thenReturn("RD01");
        return product;
    }

    // Regression: a recurring-deposit product with no minimum-deposit-term type made the download template 500 —
    // populate() wrote MIN_DEPOSIT_TERM_TYPE_COL through an unguarded getMinDepositTermType().getValue() while every
    // sibling optional field was null-guarded (the fixed-deposit populator already guards the equivalent field).
    // The column is optional on the product, so the sheet must simply leave that cell empty.
    @Test
    void productWithoutMinDepositTermTypeStillPopulates() throws Exception {
        RecurringDepositProductData product = product("Regular Recurring Deposit");
        when(product.getMinDepositTermType()).thenReturn(null);

        try (Workbook workbook = new HSSFWorkbook()) {
            RecurringDepositProductSheetPopulator populator = new RecurringDepositProductSheetPopulator(List.of(product));

            assertDoesNotThrow(() -> populator.populate(workbook, DATE_FORMAT));

            Sheet productSheet = workbook.getSheet(TemplatePopulateImportConstants.PRODUCT_SHEET_NAME);
            assertEquals("Regular_Recurring_Deposit", productSheet.getRow(1).getCell(NAME_COL).getStringCellValue());
        }
    }
}
