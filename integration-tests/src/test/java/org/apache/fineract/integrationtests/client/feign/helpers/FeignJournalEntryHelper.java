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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostJournalEntriesResponse;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.accounting.Account;

public class FeignJournalEntryHelper {

    private final FineractFeignClient fineractClient;

    public FeignJournalEntryHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostJournalEntriesResponse createJournalEntry(JournalEntryCommand command) {
        return ok(() -> fineractClient.journalEntries().createGLJournalEntry("", command));
    }

    public GetJournalEntriesTransactionIdResponse getJournalEntriesForLoan(Long loanId) {
        return ok(() -> fineractClient.journalEntries().retrieveAllJournalEntries(Map.of("loanId", loanId)));
    }

    public GetJournalEntriesTransactionIdResponse getJournalEntriesByTransactionId(String transactionId) {
        return ok(() -> fineractClient.journalEntries()
                .retrieveAllJournalEntries(Map.of("transactionId", transactionId, "orderBy", "id", "sortOrder", "desc")));
    }

    public GetJournalEntriesTransactionIdResponse getJournalEntries(String transactionId) {
        return getJournalEntriesByTransactionId(transactionId);
    }

    public void verifyTRJournalEntries(Long transactionId, LoanTestData.Journal... entries) {
        assertNotNull(transactionId, "transactionId is null");
        GetJournalEntriesTransactionIdResponse journalEntries = getJournalEntries("L" + transactionId);
        assertEquals(entries.length, journalEntries.getPageItems().size());
        verifyJournalEntriesMatch(new ArrayList<>(journalEntries.getPageItems()), entries);
    }

    public void verifyJournalEntries(Long loanId, LoanTestData.Journal... expectedEntries) {
        GetJournalEntriesTransactionIdResponse journalEntries = getJournalEntriesForLoan(loanId);
        assertNotNull(journalEntries);
        assertNotNull(journalEntries.getPageItems());

        List<JournalEntryTransactionItem> actualEntries = journalEntries.getPageItems();
        assertEquals(expectedEntries.length, actualEntries.size(),
                "Expected " + expectedEntries.length + " journal entries but found " + actualEntries.size() + ": " + actualEntries);

        verifyJournalEntriesMatch(new ArrayList<>(actualEntries), expectedEntries);
    }

    private static void verifyJournalEntriesMatch(List<JournalEntryTransactionItem> actualEntries, LoanTestData.Journal[] expectedEntries) {
        List<JournalEntryTransactionItem> remaining = new ArrayList<>(actualEntries);
        for (LoanTestData.Journal expected : expectedEntries) {
            int matchIndex = -1;
            for (int i = 0; i < remaining.size(); i++) {
                if (matchesJournalEntry(remaining.get(i), expected)) {
                    matchIndex = i;
                    break;
                }
            }
            assertTrue(matchIndex >= 0, "Required journal entry not found: " + expected);
            remaining.remove(matchIndex);
        }
        assertTrue(remaining.isEmpty(), "Unexpected extra journal entries: " + remaining);
    }

    private static boolean matchesJournalEntry(JournalEntryTransactionItem item, LoanTestData.Journal expected) {
        return amountsMatch(item.getAmount(), expected.amount)
                && Objects.equals(item.getGlAccountId(), expected.account.getAccountID().longValue())
                && Objects.requireNonNull(item.getEntryType()).getValue().equals(expected.type);
    }

    private static boolean amountsMatch(Double actual, Double expected) {
        if (actual == null || expected == null) {
            return Objects.equals(actual, expected);
        }
        return Math.abs(actual - expected) < 0.01;
    }

    public void verifyJournalEntriesSequentially(Long loanId, LoanTestData.Journal... expectedEntries) {
        GetJournalEntriesTransactionIdResponse journalEntries = getJournalEntriesForLoan(loanId);
        assertNotNull(journalEntries);
        assertNotNull(journalEntries.getPageItems());

        List<JournalEntryTransactionItem> sortedEntries = journalEntries.getPageItems().stream()
                .sorted(Comparator.comparing(JournalEntryTransactionItem::getId)).toList();
        for (int i = 0; i < expectedEntries.length && i < sortedEntries.size(); i++) {
            LoanTestData.Journal expected = expectedEntries[i];
            JournalEntryTransactionItem item = sortedEntries.get(i);
            boolean found = amountsMatch(item.getAmount(), expected.amount)
                    && Objects.equals(item.getGlAccountId(), expected.account.getAccountID().longValue())
                    && Objects.requireNonNull(item.getEntryType()).getValue().equals(expected.type);
            assertTrue(found, "Journal entry mismatch at position " + i + ". Wanted: " + expected + " Actual: " + item);
        }
        assertEquals(expectedEntries.length, journalEntries.getPageItems().size(),
                "There were more journal entries expected than actually present.");
    }

    public void checkJournalEntryForAssetAccount(Account assetAccount, String date, LoanTestData.Journal... accountEntries) {
        checkJournalEntryForAccount(assetAccount, date, accountEntries);
    }

    public void checkJournalEntryForLiabilityAccount(Account liabilityAccount, String date, LoanTestData.Journal... accountEntries) {
        checkJournalEntryForAccount(liabilityAccount, date, accountEntries);
    }

    public void checkJournalEntryForIncomeAccount(Account incomeAccount, String date, LoanTestData.Journal... accountEntries) {
        checkJournalEntryForAccount(incomeAccount, date, accountEntries);
    }

    public void checkJournalEntryForExpenseAccount(Account expenseAccount, String date, LoanTestData.Journal... accountEntries) {
        checkJournalEntryForAccount(expenseAccount, date, accountEntries);
    }

    private void checkJournalEntryForAccount(Account account, String date, LoanTestData.Journal... accountEntries) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("glAccountId", account.getAccountID());
        queryParams.put("type", account.getAccountType());
        queryParams.put("fromDate", date);
        queryParams.put("toDate", date);
        queryParams.put("orderBy", "id");
        queryParams.put("sortOrder", "desc");
        queryParams.put("locale", "en");
        queryParams.put("dateFormat", "dd MMMM yyyy");

        GetJournalEntriesTransactionIdResponse journalEntries = ok(
                () -> fineractClient.journalEntries().retrieveAllJournalEntries(queryParams));
        List<JournalEntryTransactionItem> actualEntries = journalEntries.getPageItems();
        assertNotNull(actualEntries);

        List<JournalEntryTransactionItem> remaining = new ArrayList<>(actualEntries);
        for (LoanTestData.Journal expected : accountEntries) {
            if (expected.amount > 0) {
                int matchIndex = -1;
                for (int i = 0; i < remaining.size(); i++) {
                    if (matchesJournalEntry(remaining.get(i), expected)) {
                        matchIndex = i;
                        break;
                    }
                }
                assertTrue(matchIndex >= 0,
                        "Journal entry not found for account " + account.getAccountID() + " on " + date + ": " + expected);
                remaining.remove(matchIndex);
            }
        }
    }
}
