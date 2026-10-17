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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
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
    private LoanAdjustmentService loanAdjustmentService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private JsonCommand command;

    @Test
    void givenLoanToSavingsRefund_whenUndo_thenSavingsUndoneLoanAdjustedAndTransferReversed() {
        AccountTransferTransaction transaction = mock(AccountTransferTransaction.class);
        AccountTransferDetails details = mock(AccountTransferDetails.class);
        Loan loan = mock(Loan.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        LoanTransaction fromLoanTransaction = mock(LoanTransaction.class);
        SavingsAccountTransaction toSavingsTransaction = mock(SavingsAccountTransaction.class);
        ExternalId reversalExternalId = mock(ExternalId.class);

        when(command.entityId()).thenReturn(1L);
        when(accountTransferRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transaction.isReversed()).thenReturn(false);
        when(transaction.getAccountTransferDetails()).thenReturn(details);
        when(details.fromLoanAccount()).thenReturn(loan);
        when(details.toSavingsAccount()).thenReturn(toSavingsAccount);
        when(transaction.getFromLoanTransaction()).thenReturn(fromLoanTransaction);
        when(fromLoanTransaction.isDisbursement()).thenReturn(false);
        when(fromLoanTransaction.getLoan()).thenReturn(loan);
        when(fromLoanTransaction.getTransactionDate()).thenReturn(LocalDate.of(2026, 5, 2));
        when(transaction.getToSavingsTransaction()).thenReturn(toSavingsTransaction);
        when(toSavingsTransaction.getSavingsAccount()).thenReturn(toSavingsAccount);
        when(toSavingsAccount.getId()).thenReturn(2L);
        when(toSavingsTransaction.getId()).thenReturn(3L);
        when(externalIdFactory.create()).thenReturn(reversalExternalId);

        underTest.undo(command);

        verify(savingsAccountWritePlatformService).undoTransaction(2L, 3L, true);
        verify(loanAdjustmentService).adjustLoanTransaction(eq(loan), eq(fromLoanTransaction), any(), isNull(), any());
        verify(transaction).reverse();
    }

    @Test
    void givenLoanToSavingsDisbursement_whenUndo_thenRejectedBeforeAnythingIsReversed() {
        AccountTransferTransaction transaction = mock(AccountTransferTransaction.class);
        AccountTransferDetails details = mock(AccountTransferDetails.class);
        Loan loan = mock(Loan.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        LoanTransaction fromLoanTransaction = mock(LoanTransaction.class);

        when(command.entityId()).thenReturn(1L);
        when(accountTransferRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transaction.isReversed()).thenReturn(false);
        when(transaction.getAccountTransferDetails()).thenReturn(details);
        when(details.fromLoanAccount()).thenReturn(loan);
        when(details.toSavingsAccount()).thenReturn(toSavingsAccount);
        when(transaction.getFromLoanTransaction()).thenReturn(fromLoanTransaction);
        when(fromLoanTransaction.isDisbursement()).thenReturn(true);

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.undo(command));

        verify(savingsAccountWritePlatformService, never()).undoTransaction(anyLong(), anyLong(), anyBoolean());
        verify(loanAdjustmentService, never()).adjustLoanTransaction(any(), any(), any(), any(), any());
        verify(transaction, never()).reverse();
    }

    @Test
    void givenAlreadyReversedTransfer_whenUndo_thenThrowsGeneralPlatformDomainRuleException() {
        AccountTransferTransaction transaction = mock(AccountTransferTransaction.class);

        when(command.entityId()).thenReturn(1L);
        when(accountTransferRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transaction.isReversed()).thenReturn(true);

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.undo(command));

        verify(savingsAccountWritePlatformService, never()).undoTransaction(anyLong(), anyLong(), anyBoolean());
        verify(loanAdjustmentService, never()).adjustLoanTransaction(any(), any(), any(), any(), any());
        verify(transaction, never()).reverse();
    }
}
