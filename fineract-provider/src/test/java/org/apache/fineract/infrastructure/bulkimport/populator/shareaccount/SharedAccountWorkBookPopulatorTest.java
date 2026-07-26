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
package org.apache.fineract.infrastructure.bulkimport.populator.shareaccount;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.bulkimport.populator.ClientSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.SavingsAccountSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.SharedProductsSheetPopulator;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class SharedAccountWorkBookPopulatorTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";

    // The Clients and Products named-range upper bounds must be (size + 1) as a NUMBER — row 3 for two entries. The
    // former code wrote `... "!$B$2:$B$" + list.size() + 1`, where the whole expression is left-to-right STRING
    // concatenation, so the "1" was appended rather than added: two entries produced "$B$21" instead of "$B$3" and
    // the dropdown spanned dozens of blank rows. The sibling populators already parenthesise both bounds.
    //
    // The spies let setNames see controlled client/product counts while the real (empty) populate() still builds the
    // sheets safely — the sheet writers iterate their own fields, not these accessors.
    @Test
    void namedRangeBoundsAreSizePlusOneNotStringConcat() throws Exception {
        ShareProductData firstProduct = mock(ShareProductData.class);
        ShareProductData secondProduct = mock(ShareProductData.class);
        when(firstProduct.getName()).thenReturn("Alpha");
        when(secondProduct.getName()).thenReturn("Beta");
        SharedProductsSheetPopulator products = spy(new SharedProductsSheetPopulator(List.of(), List.of()));
        doReturn(List.of(firstProduct, secondProduct)).when(products).getSharedProductDataList();

        ClientSheetPopulator clients = spy(new ClientSheetPopulator(List.of(), List.of()));
        doReturn(List.of(mock(ClientData.class), mock(ClientData.class))).when(clients).getClients();

        try (Workbook workbook = new HSSFWorkbook()) {
            new SharedAccountWorkBookPopulator(products, clients, new SavingsAccountSheetPopulator(List.of())).populate(workbook,
                    DATE_FORMAT);

            String productsFormula = workbook.getName("Products").getRefersToFormula();
            String clientsFormula = workbook.getName("Clients").getRefersToFormula();
            assertTrue(productsFormula.endsWith("$B$2:$B$3"), "expected a (size + 1) row bound, got: " + productsFormula);
            assertTrue(clientsFormula.endsWith("$B$2:$B$3"), "expected a (size + 1) row bound, got: " + clientsFormula);
        }
    }
}
