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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SavingsAccountAssemblerTest {

    @Mock
    private SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    @Mock
    private SavingsAccountTransactionDataSummaryWrapper savingsAccountTransactionDataSummaryWrapper;
    @Mock
    private ClientRepositoryWrapper clientRepository;
    @Mock
    private GroupRepositoryWrapper groupRepository;
    @Mock
    private StaffRepositoryWrapper staffRepository;
    @Mock
    private SavingsProductRepository savingProductRepository;
    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepository;
    @Mock
    private SavingsAccountChargeAssembler savingsAccountChargeAssembler;
    @Mock
    private FromJsonHelper fromApiJsonHelper;
    @Mock
    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private SavingsAccount account;

    // Regression test for: SavingsAccountAssembler.loadTransactionsToSavingsAccount() called
    // savingsAccountTransactions.get(0) unconditionally right after findTransactionsAfterPivotDate(), with no
    // empty-list guard, when the relaxing-days pivot-date config is enabled. When an account has zero transactions
    // after the relaxed pivot date (e.g. a dormant account, or one where all transactions fall before the pivot
    // window), this threw IndexOutOfBoundsException on every write action that assembles the account (deposit,
    // withdrawal, etc.). Fixed by guarding the access with the same null/empty check already used a few lines
    // below for the transient-variable update.
    @Test
    void loadTransactionsToSavingsAccountWithNoTransactionsAfterPivotDateDoesNotThrow() {
        SavingsAccountAssembler assembler = new SavingsAccountAssembler(savingsAccountTransactionSummaryWrapper,
                savingsAccountTransactionDataSummaryWrapper, clientRepository, groupRepository, staffRepository, savingProductRepository,
                savingsAccountRepository, savingsAccountChargeAssembler, fromApiJsonHelper, accountTransfersReadPlatformService,
                jdbcTemplate, configurationDomainService, externalIdFactory);

        SavingsAccountSummary summary = new SavingsAccountSummary();
        summary.setInterestPostedTillDate(LocalDate.of(2026, 1, 1).minusDays(1));

        when(account.getSummary()).thenReturn(summary);
        when(configurationDomainService.isRelaxingDaysConfigForPivotDateEnabled()).thenReturn(true);
        when(configurationDomainService.retrieveRelaxingDaysConfigForPivotDate()).thenReturn(5L);
        // The scenario that triggers the bug: no transactions exist after the relaxed pivot date.
        when(savingsAccountRepository.findTransactionsAfterPivotDate(any(SavingsAccount.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(savingsAccountRepository.findTransactionRunningBalanceBeforePivotDate(any(SavingsAccount.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> assembler.loadTransactionsToSavingsAccount(account, true));
    }
}
