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
package org.apache.fineract.infrastructure.bulkimport.populator.savings;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.bulkimport.populator.ClientSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.GroupSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.SavingsProductSheetPopulator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class SavingsWorkbookPopulatorTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";

    // Two savings products whose names differ only by case, or by whitespace the sanitiser trims, resolve to the SAME
    // Excel defined name — defined names are case-insensitive — so the second createName call was rejected with
    // "The workbook already contains this name", failing the whole template download. Each name must be emitted once.
    // The spy feeds setNames the colliding pair while the real (empty) populator still writes its own sheet.
    @Test
    void productNamesCollidingOnlyByCaseEmitOneDefinedNameSet() throws Exception {
        CurrencyData currency = mock(CurrencyData.class);
        when(currency.getInMultiplesOf()).thenReturn(1);
        when(currency.getDecimalPlaces()).thenReturn(2);
        SavingsProductData first = mock(SavingsProductData.class);
        SavingsProductData second = mock(SavingsProductData.class);
        when(first.getName()).thenReturn("Target Savings");
        when(second.getName()).thenReturn("TARGET SAVINGS "); // the sanitiser trims, so this collides with the first
        when(first.getCurrency()).thenReturn(currency);
        when(second.getCurrency()).thenReturn(currency);
        SavingsProductSheetPopulator products = spy(new SavingsProductSheetPopulator(List.of()));
        doReturn(List.of(first, second)).when(products).getProducts();

        try (Workbook workbook = new HSSFWorkbook()) {
            SavingsWorkbookPopulator populator = new SavingsWorkbookPopulator(new OfficeSheetPopulator(List.of()),
                    new ClientSheetPopulator(List.of(), List.of()), new GroupSheetPopulator(List.of(), List.of()),
                    new PersonnelSheetPopulator(List.of(), List.of()), products);

            assertDoesNotThrow(() -> populator.populate(workbook, DATE_FORMAT));

            long compoundingNames = workbook.getAllNames().stream().map(Name::getNameName)
                    .filter(name -> name.startsWith("Interest_Compouding_")).count();
            assertEquals(1, compoundingNames, "colliding product names must yield a single defined-name set");
        }
    }
}
