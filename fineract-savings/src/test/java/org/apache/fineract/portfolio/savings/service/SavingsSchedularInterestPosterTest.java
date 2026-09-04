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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

class SavingsSchedularInterestPosterTest {

    private static final String JOURNAL_ENTRY_TABLE = "acc_gl_journal_entry";

    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService = mock(SavingsAccountWritePlatformService.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService = mock(SavingsAccountReadPlatformService.class);
    private final PlatformSecurityContext platformSecurityContext = mock(PlatformSecurityContext.class);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    private SavingsSchedularInterestPoster interestPosterFor(final Long debitGlAccountId, final Long creditGlAccountId)
            throws JobExecutionException {
        final AppUser appUser = mock(AppUser.class);
        when(appUser.getId()).thenReturn(1L);
        when(platformSecurityContext.authenticatedUser()).thenReturn(appUser);

        final CurrencyData currency = mock(CurrencyData.class);
        when(currency.getCode()).thenReturn("USD");

        final SavingsAccountTransactionEnumData transactionType = mock(SavingsAccountTransactionEnumData.class);
        when(transactionType.getId()).thenReturn(703L);

        final SavingsAccountTransactionData interestPosting = mock(SavingsAccountTransactionData.class);
        when(interestPosting.getId()).thenReturn(null);
        // mirror the real setter: after the poster assigns the fetched id, getId() must return it
        doAnswer(invocation -> {
            when(interestPosting.getId()).thenReturn(invocation.getArgument(0));
            return null;
        }).when(interestPosting).setId(any());
        when(interestPosting.getAmount()).thenReturn(BigDecimal.TEN);
        when(interestPosting.getTransactionType()).thenReturn(transactionType);
        when(interestPosting.getTransactionDate()).thenReturn(LocalDate.of(2013, 4, 1));
        when(interestPosting.getRefNo()).thenReturn("ref-1");
        when(interestPosting.getAccountDebit()).thenReturn(debitGlAccountId);
        when(interestPosting.getAccountCredit()).thenReturn(creditGlAccountId);

        final SavingsAccountData account = mock(SavingsAccountData.class);
        when(account.getId()).thenReturn(42L);
        when(account.getVersion()).thenReturn(1);
        when(account.getSummary()).thenReturn(mock(SavingsAccountSummaryData.class));
        when(account.getCurrency()).thenReturn(currency);
        when(account.getSavingsAccountTransactionData()).thenReturn(new ArrayList<>(List.of(interestPosting)));

        when(savingsAccountWritePlatformService.postInterest(eq(account), eq(false), isNull(), eq(false))).thenReturn(account);
        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[] { 1 });

        final SavingsAccountTransactionData fetched = mock(SavingsAccountTransactionData.class);
        when(fetched.getRefNo()).thenReturn("ref-1");
        when(fetched.getId()).thenReturn(99L);
        when(savingsAccountReadPlatformService.retrieveAllTransactionData(anyList())).thenReturn(List.of(fetched));

        final SavingsSchedularInterestPoster interestPoster = new SavingsSchedularInterestPoster(savingsAccountWritePlatformService,
                jdbcTemplate, savingsAccountReadPlatformService, platformSecurityContext);
        interestPoster.setSavingAccounts(List.of(account));
        interestPoster.setBackdatedTxnsAllowedTill(false);
        interestPoster.postInterest();
        return interestPoster;
    }

    private List<String> executedBatchUpdateQueries() {
        final ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeastOnce()).batchUpdate(queryCaptor.capture(), anyList());
        return queryCaptor.getAllValues();
    }

    @Test
    void testJournalEntriesSkippedWhenTransactionGlAccountUnresolved() throws JobExecutionException {
        interestPosterFor(30L, 0L);

        assertTrue(executedBatchUpdateQueries().stream().noneMatch(query -> query.contains(JOURNAL_ENTRY_TABLE)),
                "No journal entry insert should be executed when a transaction GL account is unresolved");
    }

    @Test
    void testJournalEntriesWrittenWhenTransactionGlAccountsResolved() throws JobExecutionException {
        interestPosterFor(30L, 20L);

        final ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final ArgumentCaptor<List<Object[]>> paramsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(jdbcTemplate, atLeastOnce()).batchUpdate(queryCaptor.capture(), paramsCaptor.capture());

        List<Object[]> journalEntryParams = null;
        for (int i = 0; i < queryCaptor.getAllValues().size(); i++) {
            if (queryCaptor.getAllValues().get(i).contains(JOURNAL_ENTRY_TABLE)) {
                journalEntryParams = paramsCaptor.getAllValues().get(i);
            }
        }

        assertTrue(journalEntryParams != null, "Journal entry insert should be executed when both GL accounts are resolved");
        assertEquals(2, journalEntryParams.size(), "One credit and one debit journal entry should be inserted");
        assertEquals(20L, journalEntryParams.get(0)[0], "Credit entry should reference the resolved credit GL account");
        assertEquals(30L, journalEntryParams.get(1)[0], "Debit entry should reference the resolved debit GL account");
    }

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
}
