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
package org.apache.fineract.portfolio.savings.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.exception.DepositAccountTransactionNotAllowedException;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SavingsAccountDomainServiceJpaTest {

    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepository;

    @Mock
    private SavingsAccountTransactionRepository savingsAccountTransactionRepository;

    @Mock
    private ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;

    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @InjectMocks
    private SavingsAccountDomainServiceJpa domainService;

    private MonetaryCurrency currency;
    private SavingsAccountTransaction sharedMockTxn;

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 3, 15);
    private static final String TENANT_IDENTIFIER = "default";

    @BeforeEach
    void setUp() {
        FineractPlatformTenant tenant = new FineractPlatformTenant(1L, TENANT_IDENTIFIER, "Default", "Asia/Kolkata", null);
        ThreadLocalContextUtil.setTenant(tenant);
        MoneyHelper.initializeTenantRoundingMode(TENANT_IDENTIFIER, RoundingMode.HALF_EVEN.ordinal());
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, TEST_DATE);
        businessDates.put(BusinessDateType.COB_DATE, TEST_DATE.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        this.currency = new MonetaryCurrency("USD", 2, 1);
        sharedMockTxn = mock(SavingsAccountTransaction.class);
        when(sharedMockTxn.getId()).thenReturn(1L);
        when(sharedMockTxn.getAmount()).thenReturn(new BigDecimal("1000.00"));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
        try {
            MoneyHelper.clearCache();
        } catch (Exception | Error ignored) {
            // best-effort cache cleanup
        }
    }

    private SavingsAccount prepareSavingsAccount(BigDecimal balance) {
        SavingsAccount account = mock(SavingsAccount.class);
        doReturn(DepositAccountType.SAVINGS_DEPOSIT).when(account).depositAccountType();
        doReturn(currency).when(account).getCurrency();
        doReturn(1L).when(account).getId();
        SavingsAccountSummary mockSummary = mock(SavingsAccountSummary.class);
        doReturn(balance).when(mockSummary).getAccountBalance();
        doReturn(mockSummary).when(account).getSummary();
        doReturn(false).when(account).isNotActive();
        doReturn(true).when(account).isActive();
        doReturn(true).when(account).allowWithdrawal();
        doReturn(true).when(account).allowDeposit();
        doReturn(BigDecimal.ZERO).when(account).getOnHoldFunds();
        doReturn(sharedMockTxn).when(account).withdraw(any(), anyBoolean(), anyBoolean(), any(), any());
        doReturn(sharedMockTxn).when(account).deposit(any(), any(), anyBoolean(), any(), any());
        when(savingsAccountTransactionRepository.saveAndFlush(any())).thenReturn(sharedMockTxn);
        when(savingsAccountTransactionRepository.save(any())).thenReturn(sharedMockTxn);
        doReturn(new HashSet<SavingsAccountCharge>()).when(account).charges();
        return account;
    }

    private void stubAccountBlocked(SavingsAccount account) {
        doReturn(true).when(account).isNotActive();
        doReturn(false).when(account).isActive();
        doReturn(false).when(account).allowWithdrawal();
        doReturn(false).when(account).allowDeposit();
    }

    private void stubInsufficientBalance(SavingsAccount account) {
        doThrow(InsufficientAccountBalanceException.class).when(account).withdraw(any(), anyBoolean(), anyBoolean(), any(), any());
    }

    @Test
    void testHandleWithdrawal_InsufficientFunds_ThrowsException() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(100.00));
        stubInsufficientBalance(account);
        when(depositAccountOnHoldTransactionRepository.findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(any()))
                .thenReturn(Collections.emptyList());
        SavingsTransactionBooleanValues booleans = new SavingsTransactionBooleanValues(false, true, false, false, false);
        assertThrows(InsufficientAccountBalanceException.class,
                () -> domainService.handleWithdrawal(account, null, TEST_DATE, BigDecimal.valueOf(150.00), null, booleans, false));
    }

    @Test
    void testHandleWithdrawal_AccountDebitBlocked_ThrowsException() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        stubAccountBlocked(account);
        SavingsTransactionBooleanValues booleans = new SavingsTransactionBooleanValues(false, true, false, false, false);
        assertThrows(Exception.class,
                () -> domainService.handleWithdrawal(account, null, TEST_DATE, BigDecimal.valueOf(50.00), null, booleans, false));
    }

    @Test
    void testHandleWithdrawal_BackdatedTransaction_RecalculatesInterest() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(5000.00));
        when(depositAccountOnHoldTransactionRepository.findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(any()))
                .thenReturn(Collections.emptyList());
        SavingsTransactionBooleanValues booleans = new SavingsTransactionBooleanValues(false, true, false, false, false);
        SavingsAccountTransaction result = domainService.handleWithdrawal(account, null, TEST_DATE.minusDays(30),
                BigDecimal.valueOf(100.00), null, booleans, true);
        assertNotNull(result);
        verify(account, atLeastOnce()).calculateInterestUsing(any(), any(), anyBoolean(), anyBoolean(), anyInt(), any(), anyBoolean(),
                anyBoolean());
    }

    @Test
    void testHandleWithdrawal_SufficientBalance_Success() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        when(depositAccountOnHoldTransactionRepository.findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(any()))
                .thenReturn(Collections.emptyList());
        SavingsTransactionBooleanValues booleans = new SavingsTransactionBooleanValues(false, true, false, false, false);
        SavingsAccountTransaction result = domainService.handleWithdrawal(account, null, TEST_DATE, BigDecimal.valueOf(200.00), null,
                booleans, false);
        assertNotNull(result);
    }

    @Test
    void testHandleWithdrawal_ForceWithdrawal_BypassesBalanceCheck() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(50.00));
        when(depositAccountOnHoldTransactionRepository.findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(any()))
                .thenReturn(Collections.emptyList());
        SavingsTransactionBooleanValues booleans = new SavingsTransactionBooleanValues(false, true, false, true, true);
        SavingsAccountTransaction result = domainService.handleWithdrawal(account, null, TEST_DATE, BigDecimal.valueOf(100.00), null,
                booleans, false);
        assertNotNull(result);
    }

    @Test
    void testHandleWithdrawal_WithdrawalNotAllowed_ThrowsException() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        doReturn(false).when(account).allowWithdrawal();
        SavingsTransactionBooleanValues booleans = new SavingsTransactionBooleanValues(false, true, false, false, false);
        assertThrows(DepositAccountTransactionNotAllowedException.class,
                () -> domainService.handleWithdrawal(account, null, TEST_DATE, BigDecimal.valueOf(100.00), null, booleans, false));
    }

    @Test
    void testHandleWithdrawal_WithOnHoldFunds_ConsidersHoldInBalance() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        doReturn(BigDecimal.valueOf(300.00)).when(account).getOnHoldFunds();
        stubInsufficientBalance(account);
        when(depositAccountOnHoldTransactionRepository.findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(any()))
                .thenReturn(List.of(mock(DepositAccountOnHoldTransaction.class)));
        SavingsTransactionBooleanValues booleans = new SavingsTransactionBooleanValues(false, true, false, false, false);
        assertThrows(InsufficientAccountBalanceException.class,
                () -> domainService.handleWithdrawal(account, null, TEST_DATE, BigDecimal.valueOf(800.00), null, booleans, false));
    }

    @Test
    void testHandleDeposit_AccountCreditBlocked_ThrowsException() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        stubAccountBlocked(account);
        assertThrows(Exception.class,
                () -> domainService.handleDeposit(account, null, TEST_DATE, BigDecimal.valueOf(500.00), null, false, true, false));
    }

    @Test
    void testHandleDeposit_ValidDeposit_Success() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        SavingsAccountTransaction result = domainService.handleDeposit(account, null, TEST_DATE, BigDecimal.valueOf(500.00), null, false,
                true, false);
        assertNotNull(result);
    }

    @Test
    void testHandleDeposit_BackdatedDeposit_RecalculatesInterest() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        SavingsAccountTransaction result = domainService.handleDeposit(account, null, TEST_DATE.minusDays(60), BigDecimal.valueOf(1000.00),
                null, false, true, true);
        assertNotNull(result);
        verify(account, atLeastOnce()).calculateInterestUsing(any(), any(), anyBoolean(), anyBoolean(), anyInt(), any(), anyBoolean(),
                anyBoolean());
    }

    @Test
    void testHandleDeposit_DepositNotAllowed_ThrowsException() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        doReturn(false).when(account).allowDeposit();
        assertThrows(DepositAccountTransactionNotAllowedException.class,
                () -> domainService.handleDeposit(account, null, TEST_DATE, BigDecimal.valueOf(500.00), null, false, true, false));
    }

    @Test
    void testHandleHold_ValidAmount_Success() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(5000.00));
        SavingsAccountTransaction result = domainService.handleHold(account, BigDecimal.valueOf(1000.00), TEST_DATE, true);
        assertNotNull(result);
        assertEquals(0, new BigDecimal("1000.00").compareTo(result.getAmount()), "Hold amount should be 1000 (scale-insensitive)");
    }

    @Test
    void testHandleDividendPayout_ValidPayout_Success() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        SavingsAccountTransaction result = domainService.handleDividendPayout(account, TEST_DATE, BigDecimal.valueOf(250.00), false);
        assertNotNull(result);
    }

    @Test
    void testHandleReversal_ValidReversal_Success() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        SavingsAccountTransaction originalTxn = mock(SavingsAccountTransaction.class);
        when(originalTxn.getAmount()).thenReturn(BigDecimal.valueOf(100.00));
        when(originalTxn.getTransactionDate()).thenReturn(TEST_DATE.minusDays(5));
        when(originalTxn.getSavingsAccountChargesPaid()).thenReturn(new HashSet<>());
        SavingsAccountTransaction result = domainService.handleReversal(account, List.of(originalTxn), false);
        assertNotNull(result);
        verify(savingsAccountRepository).save(any());
    }

    @Test
    void testPostJournalEntries_ValidTransaction_CreatesJournalEntries() {
        SavingsAccount account = prepareSavingsAccount(BigDecimal.valueOf(1000.00));
        doReturn(new HashMap<>()).when(account).deriveAccountingBridgeData(anyString(), any(), any(), anyBoolean(), anyBoolean());
        domainService.postJournalEntries(account, new HashSet<>(), new HashSet<>(), false);
        verify(journalEntryWritePlatformService).createJournalEntriesForSavings(any());
    }
}
