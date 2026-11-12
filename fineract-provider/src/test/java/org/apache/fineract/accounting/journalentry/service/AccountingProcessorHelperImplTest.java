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
package org.apache.fineract.accounting.journalentry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountingProcessorHelperImplTest {

    @Mock
    private JournalEntryRepository glJournalEntryRepository;
    @Mock
    private ProductToGLAccountMappingRepository accountMappingRepository;
    @Mock
    private FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository;
    @Mock
    private GLClosureRepository closureRepository;
    @Mock
    private GLAccountRepository glAccountRepository;
    @Mock
    private OfficeRepository officeRepository;
    @Mock
    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    @Mock
    private ChargeRepositoryWrapper chargeRepositoryWrapper;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @InjectMocks
    private AccountingProcessorHelperImpl helper;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2021, 9, 1))));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void reversedSavingsTransactionMirrorsTheEntriesStoredForIt() {
        final Office office = mock(Office.class);
        final GLAccount interestPayable = mock(GLAccount.class);
        final GLAccount overdraftPortfolio = mock(GLAccount.class);
        final LocalDate postingDate = LocalDate.of(2021, 8, 31);
        // as the Post Interest job stores them: its credit entries carry the loan entity type
        final JournalEntry debit = JournalEntry.createNew(office, null, interestPayable, "USD", "S200", false, postingDate,
                JournalEntryType.DEBIT, BigDecimal.TEN, null, PortfolioProductType.SAVING.getValue(), 100L, null, null, 200L, null, null);
        final JournalEntry credit = JournalEntry.createNew(office, null, overdraftPortfolio, "USD", "S200", false, postingDate,
                JournalEntryType.CREDIT, BigDecimal.TEN, null, PortfolioProductType.LOAN.getValue(), 100L, null, null, 200L, null, null);
        when(glJournalEntryRepository.findUnReversedSystemJournalEntriesByTransactionId("S200")).thenReturn(List.of(debit, credit));

        helper.createJournalEntriesForReversedSavingsTransaction("200", postingDate);

        final ArgumentCaptor<JournalEntry> mirrors = ArgumentCaptor.forClass(JournalEntry.class);
        verify(glJournalEntryRepository, times(2)).saveAndFlush(mirrors.capture());
        assertThat(mirrors.getAllValues())
                .extracting(JournalEntry::getGlAccount, JournalEntry::isDebitEntry, JournalEntry::getAmount, JournalEntry::getTransactionId,
                        JournalEntry::getSavingsTransactionId, JournalEntry::getEntityType, JournalEntry::getTransactionDate)
                .containsExactly(
                        tuple(interestPayable, false, BigDecimal.TEN, "S200", 200L, PortfolioProductType.SAVING.getValue(), postingDate),
                        tuple(overdraftPortfolio, true, BigDecimal.TEN, "S200", 200L, PortfolioProductType.LOAN.getValue(), postingDate));
    }

    @Test
    void reversedSavingsTransactionWithNothingBookedGetsNoEntries() {
        when(glJournalEntryRepository.findUnReversedSystemJournalEntriesByTransactionId("S200")).thenReturn(List.of());

        helper.createJournalEntriesForReversedSavingsTransaction("200", LocalDate.of(2021, 8, 31));

        verify(glJournalEntryRepository, never()).saveAndFlush(any());
    }

    @Test
    void classifyReplacedTransferTransactionsWithoutLeakingToRegularTransactions() {
        final var accountingBridgeData = accountingBridgeData(List.of(transaction(10L), transaction(11L), transaction(12L)),
                Set.of(10L, 12L));
        when(accountTransfersReadPlatformService.isAccountTransfer(11L, PortfolioAccountType.SAVINGS)).thenReturn(false);

        final var transactions = helper.populateSavingsDtoFromMap(accountingBridgeData, true, false).getNewSavingsTransactions();

        assertTrue(transactions.get(0).isAccountTransfer());
        assertFalse(transactions.get(1).isAccountTransfer());
        assertTrue(transactions.get(2).isAccountTransfer());
        verify(accountTransfersReadPlatformService).isAccountTransfer(11L, PortfolioAccountType.SAVINGS);
    }

    @Test
    void determineTransferStatusIndependentlyForEachTransaction() {
        final var accountingBridgeData = accountingBridgeData(List.of(transaction(20L), transaction(21L)), Set.of());
        when(accountTransfersReadPlatformService.isAccountTransfer(20L, PortfolioAccountType.SAVINGS)).thenReturn(true);
        when(accountTransfersReadPlatformService.isAccountTransfer(21L, PortfolioAccountType.SAVINGS)).thenReturn(false);

        final var transactions = helper.populateSavingsDtoFromMap(accountingBridgeData, true, false).getNewSavingsTransactions();

        assertTrue(transactions.get(0).isAccountTransfer());
        assertFalse(transactions.get(1).isAccountTransfer());
    }

    private Map<String, Object> accountingBridgeData(final List<Map<String, Object>> transactions,
            final Set<Long> accountTransferTransactionIds) {
        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("savingsId", 1L);
        accountingBridgeData.put("savingsProductId", 2L);
        accountingBridgeData.put("officeId", 1L);
        accountingBridgeData.put("currencyCode", "USD");
        accountingBridgeData.put("isAccountTransfer", false);
        accountingBridgeData.put("accountTransferTransactionIds", accountTransferTransactionIds);
        accountingBridgeData.put("newSavingsTransactions", transactions);
        return accountingBridgeData;
    }

    private Map<String, Object> transaction(final Long id) {
        final Map<String, Object> transaction = new LinkedHashMap<>();
        transaction.put("officeId", 1L);
        transaction.put("id", id);
        transaction.put("date", LocalDate.of(2013, 2, 28));
        transaction.put("type", new SavingsAccountTransactionEnumData(2L, "withdrawal", "Withdrawal"));
        transaction.put("amount", BigDecimal.TEN);
        transaction.put("reversed", false);
        transaction.put("paymentTypeId", null);
        transaction.put("overdraftAmount", BigDecimal.ZERO);
        return transaction;
    }
}
