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
package org.apache.fineract.infrastructure.bulkimport.populator.chartofaccounts;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class ChartOfAccountsWorkbookTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private GLAccountData glAccount(Long id, String name, String type, String tag) {
        return GLAccountData.importInstance(name, null, "GL" + id, Boolean.TRUE, new EnumOptionData(1L, type, type),
                new EnumOptionData(1L, "DETAIL", "DETAIL"), null, CodeValueData.instance(id, tag), null).setId(id);
    }

    private ChartOfAccountsWorkbook workbook(List<GLAccountData> glAccounts) {
        return new ChartOfAccountsWorkbook(glAccounts, List.of(OfficeData.testInstance(1L, "Head Office")),
                List.of(new CurrencyData("USD")));
    }

    // The Tags named range is built per account type from the lookup-index entry, and the guard in front of it must
    // test the per-iteration array it dereferences (tagValueBeginEndIndexes) rather than the map it was read from.
    // Guarding the map instead makes the check always true — dead code that protects nothing and would let a null
    // index entry through to tagValueBeginEndIndexes[0]. This pins the working path: one Tags_<type> defined name
    // per account type, alongside the AccountName_<type> range its correctly-guarded sibling block emits.
    @Test
    void tagsNamedRangeIsBuiltForEachAccountType() throws Exception {
        List<GLAccountData> glAccounts = List.of(glAccount(1L, "Cash", "ASSET", "Current Assets"),
                glAccount(2L, "Share Capital", "EQUITY", "Capital"));

        try (Workbook workbook = new HSSFWorkbook()) {
            assertDoesNotThrow(() -> workbook(glAccounts).populate(workbook, DATE_FORMAT));

            assertNotNull(workbook.getName("Tags_ASSET"), "expected a Tags named range for the ASSET account type");
            assertNotNull(workbook.getName("Tags_EQUITY"), "expected a Tags named range for the EQUITY account type");
            // the sibling AccountName block guards correctly and must keep working unchanged
            assertNotNull(workbook.getName("AccountName_ASSET"));
            assertNotNull(workbook.getName("AccountName_EQUITY"));

            long tagRanges = workbook.getAllNames().stream().filter(name -> name.getNameName().startsWith("Tags_")).count();
            assertEquals(2, tagRanges, "one Tags named range per account type");
        }
    }
}
