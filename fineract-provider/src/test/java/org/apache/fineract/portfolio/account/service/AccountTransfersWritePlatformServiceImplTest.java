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
package org.apache.fineract.portfolio.account.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountTransfersWritePlatformServiceImplTest {

    @InjectMocks
    private AccountTransfersWritePlatformServiceImpl underTest;

    @Mock
    private AccountTransferRepository accountTransferRepository;
    @Mock
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Mock
    private LoanAccountDomainService loanAccountDomainService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private JsonCommand command;

    @Test
    void givenLoanToSavingsTransfer_whenUndo_thenSavingsUndoneLoanReversedAndTransferMarkedReversed() {
        // Arrange
        AccountTransferDetails accountTransferDetails = mock(AccountTransferDetails.class);
        AccountTransferTransaction transaction = mock(AccountTransferTransaction.class);
        Loan fromLoanAccount = mock(Loan.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        LoanTransaction fromLoanTransaction = mock(LoanTransaction.class);
        SavingsAccountTransaction toSavingsTransaction = mock(SavingsAccountTransaction.class);

        when(command.entityId()).thenReturn(1L);
        when(accountTransferRepository.findById(1L)).thenReturn(java.util.Optional.of(transaction));
        when(transaction.getId()).thenReturn(1L);
        when(transaction.isReversed()).thenReturn(false);
        when(transaction.getAccountTransferDetails()).thenReturn(accountTransferDetails);

        when(accountTransferDetails.fromLoanAccount()).thenReturn(fromLoanAccount);
        when(accountTransferDetails.toLoanAccount()).thenReturn(null);
        when(accountTransferDetails.toSavingsAccount()).thenReturn(toSavingsAccount);

        when(transaction.getToSavingsTransaction()).thenReturn(toSavingsTransaction);
        when(toSavingsTransaction.getSavingsAccount()).thenReturn(toSavingsAccount);
        when(toSavingsAccount.getId()).thenReturn(2L);
        when(toSavingsTransaction.getId()).thenReturn(3L);
        when(transaction.getFromLoanTransaction()).thenReturn(fromLoanTransaction);

        // Act
        underTest.undo(command);

        // Assert
        verify(savingsAccountWritePlatformService).undoTransaction(2L, 3L, true);
        verify(loanAccountDomainService).reverseTransfer(fromLoanTransaction);
        verify(transaction).reverse();
    }

    @Test
    void givenAlreadyReversedTransfer_whenUndo_thenThrowsGeneralPlatformDomainRuleException() {
        // Arrange
        AccountTransferTransaction transaction = mock(AccountTransferTransaction.class);

        when(command.entityId()).thenReturn(1L);
        when(accountTransferRepository.findById(1L)).thenReturn(java.util.Optional.of(transaction));
        when(transaction.isReversed()).thenReturn(true);

        // Act & Assert
        try {
            underTest.undo(command);
            org.junit.jupiter.api.Assertions.fail("Expected GeneralPlatformDomainRuleException");
        } catch (GeneralPlatformDomainRuleException e) {
            // expected
        }

        verify(savingsAccountWritePlatformService, never()).undoTransaction(anyLong(), anyLong(),
                org.mockito.ArgumentMatchers.anyBoolean());
        verify(loanAccountDomainService, never()).reverseTransfer(org.mockito.ArgumentMatchers.any());
        verify(transaction, never()).reverse();
    }
}
