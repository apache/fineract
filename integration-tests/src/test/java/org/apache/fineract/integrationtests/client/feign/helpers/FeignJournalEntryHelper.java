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

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;

public class FeignJournalEntryHelper {

    private final FineractFeignClient fineractClient;

    public FeignJournalEntryHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public GetJournalEntriesTransactionIdResponse getJournalEntriesForLoan(Long loanId) {
        return ok(() -> fineractClient.journalEntries().retrieveAll1(Map.of("loanId", loanId)));
    }

    public void verifyJournalEntries(Long loanId, LoanTestData.Journal... expectedEntries) {
        GetJournalEntriesTransactionIdResponse journalEntries = getJournalEntriesForLoan(loanId);
        assertNotNull(journalEntries);
        assertNotNull(journalEntries.getPageItems());

        List<JournalEntryTransactionItem> actualEntries = journalEntries.getPageItems();
        assertEquals(expectedEntries.length, actualEntries.size(),
                "Expected " + expectedEntries.length + " journal entries but found " + actualEntries.size());

        for (int i = 0; i < expectedEntries.length; i++) {
            LoanTestData.Journal expected = expectedEntries[i];
            JournalEntryTransactionItem actual = actualEntries.get(i);

            Double expectedAmount = expected.amount;
            Double actualAmount = actual.getAmount();
            assertEquals(0, Double.compare(expectedAmount, actualAmount),
                    "Journal entry " + i + " amount mismatch: expected " + expectedAmount + " but got " + actualAmount);
            assertEquals(expected.account.getAccountID().longValue(), actual.getGlAccountId(), "Journal entry " + i + " account mismatch");
            assertEquals(expected.type, actual.getEntryType().getValue(), "Journal entry " + i + " type mismatch");
        }
    }

    public void verifyJournalEntriesSequentially(Long loanId, LoanTestData.Journal... expectedEntries) {
        verifyJournalEntries(loanId, expectedEntries);
    }
}
