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
package org.apache.fineract.portfolio.client.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientChargeRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientTransaction;
import org.apache.fineract.portfolio.client.domain.ClientTransactionRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientChargeSavingsUndoTest {

    @Mock
    private ClientTransactionRepositoryWrapper clientTransactionRepository;
    @Mock
    private ClientRepositoryWrapper clientRepository;
    @Mock
    private OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;
    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;
    @Mock
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Mock
    private SavingsAccountAssembler savingsAccountAssembler;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ClientChargeRepositoryWrapper clientChargeRepository;
    @InjectMocks
    private ClientTransactionWritePlatformServiceJpaRepositoryImpl service;

    private ClientTransaction transaction;

    @BeforeEach
    void setup() {
        transaction = mock(ClientTransaction.class);
        when(clientTransactionRepository.findOneWithNotFoundDetection(1L, 2L)).thenReturn(transaction);
        when(clientRepository.getActiveClientInUserScope(1L)).thenReturn(mock(Client.class));
        SavingsAccountTransaction funding = mock(SavingsAccountTransaction.class);
        SavingsAccount savings = mock(SavingsAccount.class);
        when(transaction.getFundingSavingsTransaction()).thenReturn(funding);
        when(funding.getSavingsAccount()).thenReturn(savings);
        when(funding.getId()).thenReturn(3L);
        when(savings.getId()).thenReturn(4L);
        when(transaction.getClientChargePaidByCollection()).thenReturn(Set.of());
        when(context.authenticatedUser()).thenReturn(mock(AppUser.class));
    }

    @Test
    void reversesSavingsBeforeClientAndRetainsFundingLink() {
        service.undo(1L, 2L);
        var ordered = inOrder(savingsAccountWritePlatformService, transaction);
        ordered.verify(savingsAccountWritePlatformService).undoTransaction(4L, 3L, true);
        ordered.verify(transaction).reverse();
        verify(savingsAccountAssembler).assembleFromInUserScope(4L, false);
        verify(transaction, never()).setFundingSavingsTransaction(null);
    }

    @Test
    void failedSavingsUndoDoesNotReverseClient() {
        doThrow(new IllegalStateException("Cannot reverse withdrawal")).when(savingsAccountWritePlatformService).undoTransaction(4L, 3L,
                true);
        assertThrows(IllegalStateException.class, () -> service.undo(1L, 2L));
        verify(transaction, never()).reverse();
        verify(clientTransactionRepository, never()).saveAndFlush(transaction);
    }
}
