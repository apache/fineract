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
package org.apache.fineract.interoperation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.interoperation.data.InteropTransactionsData;
import org.apache.fineract.interoperation.domain.InteropIdentifierRepository;
import org.apache.fineract.interoperation.serialization.InteropDataValidator;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class InteropServiceImplTest {

    @Mock
    private PlatformSecurityContext securityContext;
    @Mock
    private InteropDataValidator dataValidator;
    @Mock
    private SavingsAccountRepository savingsAccountRepository;
    @Mock
    private SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    @Mock
    private ApplicationCurrencyRepository currencyRepository;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private PaymentTypeRepository paymentTypeRepository;
    @Mock
    private InteropIdentifierRepository identifierRepository;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private SavingsHelper savingsHelper;
    @Mock
    private SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    @Mock
    private SavingsAccountDomainService savingsAccountService;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Mock
    private DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;

    @InjectMocks
    private InteropServiceImpl underTest;

    @Test
    void getAccountTransactions_shouldLookUpNotesBySavingsTransactionId() {
        String accountExternalId = "savings-ext-1";
        SavingsAccount account = mock(SavingsAccount.class);
        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        MonetaryCurrency currency = mock(MonetaryCurrency.class);
        Money runningBalance = mock(Money.class);
        Note note = mock(Note.class);
        when(savingsAccountRepository.findByExternalId(argThat(id -> accountExternalId.equals(id.getValue())))).thenReturn(account);
        when(account.getId()).thenReturn(1L);
        when(account.getTransactions()).thenReturn(List.of(transaction));
        when(account.getCurrency()).thenReturn(currency);
        when(account.getExternalId()).thenReturn(new ExternalId(accountExternalId));
        when(currency.getCode()).thenReturn("USD");
        when(transaction.getSavingsAccount()).thenReturn(account);
        when(transaction.getId()).thenReturn(42L);
        when(transaction.getTransactionType()).thenReturn(SavingsAccountTransactionType.DEPOSIT);
        when(transaction.getAmount()).thenReturn(new BigDecimal("100"));
        when(transaction.getSavingsAccountChargesPaid()).thenReturn(Set.of());
        when(transaction.getRunningBalance(currency)).thenReturn(runningBalance);
        when(runningBalance.getAmount()).thenReturn(new BigDecimal("100"));
        when(transaction.getTransactionDate()).thenReturn(LocalDate.of(2026, 1, 15));
        when(note.getNote()).thenReturn("transaction note");
        when(noteRepository.findBySavingsTransactionId(42L)).thenReturn(List.of(note));

        InteropTransactionsData result = underTest.getAccountTransactions(accountExternalId, true, true, null, null);

        assertThat(result.getTransactions()).hasSize(1);
        assertThat(result.getTransactions().get(0).getNote()).contains("transaction note");
        verify(noteRepository).findBySavingsTransactionId(42L);
    }
}
