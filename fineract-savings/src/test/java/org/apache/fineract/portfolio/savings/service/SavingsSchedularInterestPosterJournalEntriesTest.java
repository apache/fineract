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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The journal entries the Post Interest For Savings job writes for the transactions it posts. Interest posted to an
 * overdrawn account pays down the overdraft first, so that part is credited to Overdraft Portfolio Control, as the
 * accounting processors do for a manual posting.
 */
class SavingsSchedularInterestPosterJournalEntriesTest {

    private static final Long SAVINGS_ACCOUNT_ID = 1L;
    private static final Long TRANSACTION_ID = 7L;
    private static final Long INTEREST_ON_SAVINGS_GL_ACCOUNT_ID = 101L;
    private static final Long SAVINGS_CONTROL_GL_ACCOUNT_ID = 102L;
    private static final Long INTEREST_PAYABLE_GL_ACCOUNT_ID = 103L;
    private static final Long INCOME_FROM_INTEREST_GL_ACCOUNT_ID = 105L;
    private static final Long OVERDRAFT_PORTFOLIO_GL_ACCOUNT_ID = 106L;
    private static final CurrencyData CURRENCY = new MonetaryCurrency("USD", 2, null).toData();
    private static final LocalDate POSTING_DATE = LocalDate.of(2026, 9, 30);

    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService = mock(SavingsAccountWritePlatformService.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService = mock(SavingsAccountReadPlatformService.class);
    private final PlatformSecurityContext platformSecurityContext = mock(PlatformSecurityContext.class);
    private final SavingsSchedularInterestPoster poster = new SavingsSchedularInterestPoster(savingsAccountWritePlatformService,
            jdbcTemplate, savingsAccountReadPlatformService, platformSecurityContext);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, POSTING_DATE.plusDays(1))));

        final AppUser user = mock(AppUser.class);
        when(user.getId()).thenReturn(1L);
        when(platformSecurityContext.authenticatedUser()).thenReturn(user);
        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[] { 1 });
        // the job reads the inserted transactions back by reference number to learn their ids
        when(savingsAccountReadPlatformService.retrieveAllTransactionData(anyList())).thenAnswer(invocation -> {
            final List<String> refNos = invocation.getArgument(0);
            return refNos.stream().map(refNo -> {
                final SavingsAccountTransactionData fetched = SavingsAccountTransactionData.create(TRANSACTION_ID);
                fetched.setRefNo(refNo);
                return fetched;
            }).toList();
        });
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void interestThatPaysDownPartOfAnOverdraftIsSplitBetweenOverdraftPortfolioAndSavingsControl() throws Exception {
        final SavingsAccountTransactionData posting = interestPosting("50", "10", INTEREST_ON_SAVINGS_GL_ACCOUNT_ID);

        assertThat(journalEntriesWrittenFor(posting)).containsExactly("CREDIT 106 10", "DEBIT 101 10", "CREDIT 102 40", "DEBIT 101 40");
    }

    @Test
    void interestThatLeavesTheAccountOverdrawnIsCreditedToOverdraftPortfolioOnly() throws Exception {
        final SavingsAccountTransactionData posting = interestPosting("50", "50", INTEREST_ON_SAVINGS_GL_ACCOUNT_ID);

        assertThat(journalEntriesWrittenFor(posting)).containsExactly("CREDIT 106 50", "DEBIT 101 50");
    }

    @Test
    void accrualInterestKeepsDebitingInterestPayableForBothParts() throws Exception {
        final SavingsAccountTransactionData posting = interestPosting("50", "10", INTEREST_PAYABLE_GL_ACCOUNT_ID);

        assertThat(journalEntriesWrittenFor(posting)).containsExactly("CREDIT 106 10", "DEBIT 103 10", "CREDIT 102 40", "DEBIT 103 40");
    }

    @Test
    void interestPostedToAnAccountInCreditIsCreditedToSavingsControl() throws Exception {
        final SavingsAccountTransactionData posting = interestPosting("50", null, INTEREST_ON_SAVINGS_GL_ACCOUNT_ID);

        assertThat(journalEntriesWrittenFor(posting)).containsExactly("CREDIT 102 50", "DEBIT 101 50");
    }

    @Test
    void overdraftInterestIsNotSplit() throws Exception {
        final SavingsAccountTransactionData overdraftInterest = transaction(SavingsAccountTransactionType.OVERDRAFT_INTEREST, "5", "5",
                OVERDRAFT_PORTFOLIO_GL_ACCOUNT_ID, INCOME_FROM_INTEREST_GL_ACCOUNT_ID);

        assertThat(journalEntriesWrittenFor(overdraftInterest)).containsExactly("CREDIT 105 5", "DEBIT 106 5");
    }

    private List<String> journalEntriesWrittenFor(final SavingsAccountTransactionData transaction) throws Exception {
        final SavingsAccountData account = accountWith(transaction);
        when(savingsAccountWritePlatformService.postInterest(account, false, null, false)).thenReturn(account);
        poster.setSavingAccounts(List.of(account));

        poster.postInterest();

        final ArgumentCaptor<List<Object[]>> rows = ArgumentCaptor.captor();
        verify(jdbcTemplate).batchUpdate(startsWith("INSERT INTO acc_gl_journal_entry"), rows.capture());
        // account_id, type_enum and amount are the 1st, 12th and 13th columns of the insert
        return rows.getValue().stream().map(row -> (JournalEntryType.CREDIT.getValue().longValue() == (Long) row[11] ? "CREDIT" : "DEBIT")
                + " " + row[0] + " " + ((BigDecimal) row[12]).stripTrailingZeros().toPlainString()).toList();
    }

    private static SavingsAccountTransactionData interestPosting(final String amount, final String overdraftAmount,
            final Long debitAccountId) {
        return transaction(SavingsAccountTransactionType.INTEREST_POSTING, amount, overdraftAmount, debitAccountId,
                SAVINGS_CONTROL_GL_ACCOUNT_ID);
    }

    /** A new transaction from the posting service, with the GL accounts selectAccountId gave it. */
    private static SavingsAccountTransactionData transaction(final SavingsAccountTransactionType type, final String amount,
            final String overdraftAmount, final Long debitAccountId, final Long creditAccountId) {
        final SavingsAccountTransactionData transaction = SavingsAccountTransactionData.create(null,
                SavingsEnumerations.transactionType(type), null, SAVINGS_ACCOUNT_ID, null, POSTING_DATE, CURRENCY, new BigDecimal(amount),
                null, null, false, POSTING_DATE, false, null, POSTING_DATE);
        if (overdraftAmount != null) {
            transaction.updateOverdraftAmount(new BigDecimal(overdraftAmount));
        }
        transaction.setAccountDebit(debitAccountId);
        transaction.setAccountCredit(creditAccountId);
        return transaction;
    }

    private static SavingsAccountData accountWith(final SavingsAccountTransactionData transaction) {
        final SavingsAccountData account = mock(SavingsAccountData.class);
        when(account.getId()).thenReturn(SAVINGS_ACCOUNT_ID);
        when(account.getCurrency()).thenReturn(CURRENCY);
        when(account.getSummary()).thenReturn(mock(SavingsAccountSummaryData.class));
        when(account.getSavingsAccountTransactionData()).thenReturn(List.of(transaction));
        when(account.getGlAccountIdForInterestOnSavings()).thenReturn(INTEREST_ON_SAVINGS_GL_ACCOUNT_ID);
        when(account.getGlAccountIdForSavingsControl()).thenReturn(SAVINGS_CONTROL_GL_ACCOUNT_ID);
        when(account.getGlAccountIdForOverdraftPorfolio()).thenReturn(OVERDRAFT_PORTFOLIO_GL_ACCOUNT_ID);
        return account;
    }
}
