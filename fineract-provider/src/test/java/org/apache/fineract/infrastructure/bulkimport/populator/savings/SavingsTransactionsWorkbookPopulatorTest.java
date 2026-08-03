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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.infrastructure.bulkimport.populator.ClientSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.ExtrasSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountApplicationTimelineData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

class SavingsTransactionsWorkbookPopulatorTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final LocalDate ACTIVATED_ON = LocalDate.of(2024, 1, 1);

    private SavingsAccountData account(Long clientId, String clientName, String accountNo) {
        SavingsAccountApplicationTimelineData timeline = mock(SavingsAccountApplicationTimelineData.class);
        when(timeline.getActivatedOnDate()).thenReturn(ACTIVATED_ON);
        SavingsAccountData account = mock(SavingsAccountData.class);
        when(account.getClientId()).thenReturn(clientId);
        when(account.getClientName()).thenReturn(clientName);
        when(account.getAccountNo()).thenReturn(accountNo);
        when(account.getSavingsProductName()).thenReturn("Regular Savings");
        when(account.getTimeline()).thenReturn(timeline);
        return account;
    }

    // A client must contribute exactly one Account_<client>_<id>_ defined name however many accounts it has. The
    // lookup table is sorted with SavingsAccountData.ClientNameComparator, which compares client names
    // CASE-INSENSITIVELY, while setNames detects a new client with a CASE-SENSITIVE equals. A second client whose
    // name differs only in case therefore sorts in between one client's two accounts and splits them into two runs;
    // the second run re-emitted the same defined name and POI rejected it with
    // "IllegalArgumentException: The workbook already contains this name: Account_<client>_<id>_", failing the whole
    // template download with HTTP 500.
    @Test
    void clientWithSeveralAccountsEmitsOneDefinedName() throws Exception {
        List<SavingsAccountData> accounts = new ArrayList<>(List.of(account(18L, "maryam yusuf", "000000018"),
                account(19L, "MARYAM YUSUF", "000000019"), account(18L, "maryam yusuf", "000000020")));

        try (Workbook workbook = new HSSFWorkbook()) {
            SavingsTransactionsWorkbookPopulator populator = new SavingsTransactionsWorkbookPopulator(new OfficeSheetPopulator(List.of()),
                    new ClientSheetPopulator(List.of(), List.of()), new ExtrasSheetPopulator(List.of(), List.of(), List.of()), accounts);

            assertDoesNotThrow(() -> populator.populate(workbook, DATE_FORMAT));

            List<String> names = workbook.getAllNames().stream().map(Name::getNameName).filter(name -> name.startsWith("Account_"))
                    .toList();
            assertEquals(2, names.size(), "expected one defined name per client, got: " + names);
            assertEquals(2, names.stream().map(name -> name.toUpperCase(Locale.ROOT)).distinct().count(),
                    "defined names collide case-insensitively: " + names);
        }
    }
}
