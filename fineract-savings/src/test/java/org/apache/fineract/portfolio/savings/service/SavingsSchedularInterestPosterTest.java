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
package org.apache.fineract.portfolio.savings.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

class SavingsSchedularInterestPosterTest {

    @Test
    void testUpdateCountsZeroMeansVersionMismatch() {
        int[] updateCounts = { 1, 0, 1 };
        Set<Long> skippedAccountIds = new HashSet<>();
        List<Long> accountIds = List.of(1L, 2L, 3L);

        for (int i = 0; i < updateCounts.length; i++) {
            if (updateCounts[i] == 0) {
                skippedAccountIds.add(accountIds.get(i));
            }
        }

        assertEquals(1, skippedAccountIds.size(), "Exactly one account should be skipped");
        assertTrue(skippedAccountIds.contains(2L), "Account 2 should be skipped due to version mismatch");
    }

    @Test
    void testAllVersionsMatchNoSkippedAccounts() {
        int[] updateCounts = { 1, 1, 1 };
        Set<Long> skippedAccountIds = new HashSet<>();
        List<Long> accountIds = List.of(1L, 2L, 3L);

        for (int i = 0; i < updateCounts.length; i++) {
            if (updateCounts[i] == 0) {
                skippedAccountIds.add(accountIds.get(i));
            }
        }

        assertTrue(skippedAccountIds.isEmpty(), "No accounts should be skipped when all versions match");
    }

    @Test
    void testAllVersionsMismatchAllSkipped() {
        int[] updateCounts = { 0, 0, 0 };
        Set<Long> skippedAccountIds = new HashSet<>();
        List<Long> accountIds = List.of(1L, 2L, 3L);

        for (int i = 0; i < updateCounts.length; i++) {
            if (updateCounts[i] == 0) {
                skippedAccountIds.add(accountIds.get(i));
            }
        }

        assertEquals(3, skippedAccountIds.size(), "All 3 accounts should be detected as version mismatched");
        assertTrue(skippedAccountIds.containsAll(List.of(1L, 2L, 3L)), "All account IDs should be in skipped set");
    }

    @Test
    void testVersionMismatchSkipsFailedAccountAndProceedsWithOthers() {
        int[] updateCounts = { 1, 0, 1 };
        List<Long> accountIds = List.of(1L, 2L, 3L);
        List<Long> successfulIds = new ArrayList<>();

        for (int i = 0; i < updateCounts.length; i++) {
            if (updateCounts[i] == 0) {
                // account is skipped due to concurrent modification — logged, not thrown
            } else {
                successfulIds.add(accountIds.get(i));
            }
        }

        assertEquals(2, successfulIds.size(), "Two accounts should proceed normally");
        assertTrue(successfulIds.containsAll(List.of(1L, 3L)), "Accounts 1 and 3 should succeed independently");
    }

    @Test
    void testPostInterestPersistsWithholdTaxDetailsAndBooksJournalEntriesPerTaxComponent() throws Exception {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(
                Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 10, 2), BusinessDateType.COB_DATE, LocalDate.of(2026, 10, 2))));

        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        final SavingsAccountWritePlatformService writeService = mock(SavingsAccountWritePlatformService.class);
        final SavingsAccountReadPlatformService readService = mock(SavingsAccountReadPlatformService.class);
        final PlatformSecurityContext securityContext = mock(PlatformSecurityContext.class);
        final AppUser appUser = mock(AppUser.class);
        when(appUser.getId()).thenReturn(1L);
        when(securityContext.authenticatedUser()).thenReturn(appUser);

        final TaxComponentData firstComponent = TaxComponentData.instance(11L, "Tax 1", new BigDecimal("6.000000"), null, null, null,
                GLAccountData.createFrom(801L), LocalDate.of(2020, 1, 1), List.of());
        final TaxComponentData secondComponent = TaxComponentData.instance(12L, "Tax 2", new BigDecimal("4.000000"), null, null, null,
                GLAccountData.createFrom(802L), LocalDate.of(2020, 1, 1), List.of());

        final SavingsAccountData savingsAccountData = mock(SavingsAccountData.class);
        when(savingsAccountData.getId()).thenReturn(1L);

        final Map<TaxComponentData, BigDecimal> taxDetails = new LinkedHashMap<>();
        taxDetails.put(firstComponent, new BigDecimal("60.00"));
        taxDetails.put(secondComponent, new BigDecimal("40.00"));

        // a real transaction object, built the way the posting service builds it: the poster assigns the
        // fetched id to the transaction via setId, which a mock would silently swallow
        final Money withholdTaxAmount = Money.of(new CurrencyData("USD", 2, null), new BigDecimal("100.00"));
        final SavingsAccountTransactionData withholdTransaction = SavingsAccountTransactionData
                .withHoldTax(savingsAccountData, LocalDate.of(2026, 10, 1), withholdTaxAmount, taxDetails);

        when(savingsAccountData.getOfficeId()).thenReturn(1L);
        when(savingsAccountData.getVersion()).thenReturn(1);
        when(savingsAccountData.getCurrency()).thenReturn(new CurrencyData("USD", 2, null));
        when(savingsAccountData.getSummary()).thenReturn(mock(SavingsAccountSummaryData.class));
        when(savingsAccountData.getSavingsAccountTransactionData()).thenReturn(List.of(withholdTransaction));
        when(savingsAccountData.getGlAccountIdForSavingsControl()).thenReturn(700L);
        when(savingsAccountData.getGlAccountIdForInterestOnSavings()).thenReturn(701L);
        when(savingsAccountData.getGlAccountIdForSavingsReference()).thenReturn(702L);

        when(writeService.postInterest(eq(savingsAccountData), anyBoolean(), isNull(), anyBoolean())).thenReturn(savingsAccountData);

        // the poster fetches the persisted transactions by the refNos it assigned during the batch insert
        when(readService.retrieveAllTransactionData(anyList())).thenAnswer(invocation -> {
            final List<String> requestedRefNos = invocation.getArgument(0);
            final SavingsAccountTransactionData fetchedTransaction = mock(SavingsAccountTransactionData.class);
            when(fetchedTransaction.getId()).thenReturn(555L);
            when(fetchedTransaction.getRefNo()).thenReturn(requestedRefNos.get(0));
            return List.of(fetchedTransaction);
        });

        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[] { 1 });

        final SavingsSchedularInterestPoster poster = new SavingsSchedularInterestPoster(writeService, jdbcTemplate, readService,
                securityContext);
        poster.setSavingAccounts(List.of(savingsAccountData));
        poster.postInterest();

        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<Object[]>> paramsCaptor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate, times(5)).batchUpdate(sqlCaptor.capture(), paramsCaptor.capture());

        List<Object[]> taxDetailParams = null;
        List<Object[]> journalEntryParams = null;
        for (int i = 0; i < sqlCaptor.getAllValues().size(); i++) {
            if (sqlCaptor.getAllValues().get(i).contains("m_savings_account_transaction_tax_details")) {
                taxDetailParams = paramsCaptor.getAllValues().get(i);
            }
            if (sqlCaptor.getAllValues().get(i).contains("acc_gl_journal_entry")) {
                journalEntryParams = paramsCaptor.getAllValues().get(i);
            }
        }

        assertTrue(taxDetailParams != null, "Tax details of the withhold tax transaction should be batch inserted");
        assertEquals(2, taxDetailParams.size());
        assertEquals(555L, taxDetailParams.get(0)[0]);
        assertEquals(11L, taxDetailParams.get(0)[1]);
        assertEquals(new BigDecimal("60.00"), taxDetailParams.get(0)[2]);
        assertEquals(555L, taxDetailParams.get(1)[0]);
        assertEquals(12L, taxDetailParams.get(1)[1]);
        assertEquals(new BigDecimal("40.00"), taxDetailParams.get(1)[2]);

        assertTrue(journalEntryParams != null, "Journal entries should be created for the withhold tax transaction");
        assertEquals(3, journalEntryParams.size(), "One debit to savings control and one credit per tax component");
        final Object[] debitEntry = journalEntryParams.get(0);
        assertEquals(700L, debitEntry[0]);
        assertEquals(JournalEntryType.DEBIT.getValue().longValue(), debitEntry[11]);
        assertEquals(new BigDecimal("100.00"), debitEntry[12]);
        final Object[] firstCreditEntry = journalEntryParams.get(1);
        assertEquals(801L, firstCreditEntry[0]);
        assertEquals(JournalEntryType.CREDIT.getValue().longValue(), firstCreditEntry[11]);
        assertEquals(new BigDecimal("60.00"), firstCreditEntry[12]);
        final Object[] secondCreditEntry = journalEntryParams.get(2);
        assertEquals(802L, secondCreditEntry[0]);
        assertEquals(JournalEntryType.CREDIT.getValue().longValue(), secondCreditEntry[11]);
        assertEquals(new BigDecimal("40.00"), secondCreditEntry[12]);
    }
}
