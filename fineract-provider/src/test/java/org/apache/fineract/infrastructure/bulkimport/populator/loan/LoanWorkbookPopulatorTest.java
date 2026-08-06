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
package org.apache.fineract.infrastructure.bulkimport.populator.loan;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.bulkimport.populator.ChargeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ClientSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ExtrasSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.GroupSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.LoanProductSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class LoanWorkbookPopulatorTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private LoanWorkbookPopulator populator(LoanProductSheetPopulator products, ChargeSheetPopulator charges) {
        return new LoanWorkbookPopulator(new OfficeSheetPopulator(List.of()), new ClientSheetPopulator(List.of(), List.of()),
                new GroupSheetPopulator(List.of(), List.of()), new PersonnelSheetPopulator(List.of(), List.of()), products, charges,
                new ExtrasSheetPopulator(List.of(), List.of(), List.of()));
    }

    private long definedNamesStartingWith(Workbook workbook, String prefix) {
        return workbook.getAllNames().stream().map(Name::getNameName).filter(name -> name.startsWith(prefix)).count();
    }

    // Two charges whose names differ only by case (or by characters the name transform folds away) resolve to the
    // SAME Excel defined name — defined names are case-insensitive — so the second createName call was rejected with
    // "The workbook already contains this name", failing the whole template download. Each name must be emitted once.
    // The spy feeds setNames the colliding pair while the real (empty) populator still writes its own sheet.
    @Test
    void chargeNamesCollidingOnlyByCaseEmitOneDefinedNameSet() throws Exception {
        ChargeData first = mock(ChargeData.class);
        ChargeData second = mock(ChargeData.class);
        when(first.getName()).thenReturn("Late Fee");
        when(second.getName()).thenReturn("LATE FEE ");
        ChargeSheetPopulator charges = spy(new ChargeSheetPopulator(List.of()));
        doReturn(List.of(first, second)).when(charges).getCharges();

        try (Workbook workbook = new HSSFWorkbook()) {
            LoanWorkbookPopulator populator = populator(new LoanProductSheetPopulator(List.of()), charges);

            assertDoesNotThrow(() -> populator.populate(workbook, DATE_FORMAT));

            assertEquals(1, definedNamesStartingWith(workbook, "CHARGE_NAME_"),
                    "colliding charge names must yield a single defined-name set");
        }
    }

    // Same defect on the loan-product loop. Note the product loop substitutes spaces BEFORE the name is sanitised, so
    // a trailing space survives as a trailing underscore and produces a genuinely different defined name — unlike the
    // charge loop above, only a case-only clash collides here.
    @Test
    void productNamesCollidingOnlyByCaseEmitOneDefinedNameSet() throws Exception {
        LoanProductData first = mock(LoanProductData.class);
        LoanProductData second = mock(LoanProductData.class);
        when(first.getName()).thenReturn("Micro Loan");
        when(second.getName()).thenReturn("MICRO LOAN");
        LoanProductSheetPopulator products = spy(new LoanProductSheetPopulator(List.of()));
        doReturn(List.of(first, second)).when(products).getProducts();

        try (Workbook workbook = new HSSFWorkbook()) {
            LoanWorkbookPopulator populator = populator(products, new ChargeSheetPopulator(List.of()));

            assertDoesNotThrow(() -> populator.populate(workbook, DATE_FORMAT));

            assertEquals(1, definedNamesStartingWith(workbook, "PRINCIPAL_"),
                    "colliding product names must yield a single defined-name set");
        }
    }
}
