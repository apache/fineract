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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.Test;

public class FeignJournalEntrySourceFilterTest extends FeignSavingsTestBase {

    private static final String DATE = "01 January 2026";

    @Test
    public void testEntrySourceFiltersManualAndSystemEntries() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();

        // System entries: a deposit credits the savings control (liability) account
        final Long productId = createSavingsProduct(SavingsRequestBuilders.withCashBasedAccounting(
                SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0), assetAccount, liabilityAccount,
                incomeAccount, expenseAccount)).getResourceId();
        final Long savingsId = createApproveActivateSavings(createClient(DATE), productId, DATE);
        deposit(savingsId, "100", DATE);

        // Manual entry on the same liability account
        journalEntryHelper.createJournalEntry(new JournalEntryCommand().officeId(1L).currencyCode("USD").locale("en")
                .dateFormat("uuuu-MM-dd").transactionDate(LocalDate.of(2026, 1, 1))
                .addDebitsItem(
                        new SingleDebitOrCreditEntryCommand().glAccountId(assetAccount.getAccountID().longValue()).amount(BigDecimal.TEN))
                .addCreditsItem(new SingleDebitOrCreditEntryCommand().glAccountId(liabilityAccount.getAccountID().longValue())
                        .amount(BigDecimal.TEN)));

        final Long glAccountId = liabilityAccount.getAccountID().longValue();
        final List<Boolean> all = manualFlags(glAccountId, Map.of());
        assertEquals(2, all.size());
        assertTrue(all.contains(true) && all.contains(false), "Both a manual and a system entry are expected: " + all);
        assertEquals(List.of(true), manualFlags(glAccountId, Map.of("entrySource", "MANUAL")));
        assertEquals(List.of(false), manualFlags(glAccountId, Map.of("entrySource", "SYSTEM")));
        assertEquals(List.of(false), manualFlags(glAccountId, Map.of("entrySource", "system")));

        // Both filters apply when manualEntriesOnly is sent as well
        assertEquals(List.of(true), manualFlags(glAccountId, Map.of("manualEntriesOnly", "true", "entrySource", "MANUAL")));
        assertEquals(List.of(), manualFlags(glAccountId, Map.of("manualEntriesOnly", "true", "entrySource", "SYSTEM")));
        assertEquals(List.of(false), manualFlags(glAccountId, Map.of("manualEntriesOnly", "false", "entrySource", "SYSTEM")));
    }

    @Test
    public void testEntrySourceRejectsUnsupportedValue() {
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient().journalEntries().retrieveAllJournalEntries(Map.of("entrySource", "AUTOMATIC")));

        assertEquals(400, exception.getStatus());
        assertTrue(exception.getResponseBody().contains("error.msg.query.parameter.value.unsupported"), exception.getResponseBody());
    }

    private List<Boolean> manualFlags(final Long glAccountId, final Map<String, String> filter) {
        final Map<String, Object> query = new HashMap<>(filter);
        query.put("glAccountId", glAccountId);
        return ok(() -> fineractClient().journalEntries().retrieveAllJournalEntries(query)).getPageItems().stream()
                .map(JournalEntryTransactionItem::getManualEntry).toList();
    }
}
