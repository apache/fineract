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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionReplacement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingsAccountTransfersServiceImplTest {

    @Mock
    private AccountTransferRepository accountTransferRepository;

    @InjectMocks
    private SavingsAccountTransfersServiceImpl service;

    @Test
    void updateSourceTransactionReference() {
        final var replacedTransaction = transaction(1L);
        final var replacementTransaction = mock(SavingsAccountTransaction.class);
        final var transferTransaction = mock(AccountTransferTransaction.class);
        when(transferTransaction.getFromTransaction()).thenReturn(replacedTransaction);
        when(accountTransferRepository.findBySavingsTransactionIds(Set.of(1L))).thenReturn(List.of(transferTransaction));

        service.updateSavingsTransactionReferences(
                List.of(new SavingsAccountTransactionReplacement(replacedTransaction, replacementTransaction)));

        verify(transferTransaction).updateFromSavingsTransaction(replacementTransaction);
        verify(transferTransaction, never()).updateToSavingsTransaction(replacementTransaction);
        verify(accountTransferRepository).saveAll(List.of(transferTransaction));
    }

    @Test
    void updateDestinationTransactionReference() {
        final var replacedTransaction = transaction(3L);
        final var replacementTransaction = mock(SavingsAccountTransaction.class);
        final var transferTransaction = mock(AccountTransferTransaction.class);
        when(transferTransaction.getToSavingsTransaction()).thenReturn(replacedTransaction);
        when(accountTransferRepository.findBySavingsTransactionIds(Set.of(3L))).thenReturn(List.of(transferTransaction));

        service.updateSavingsTransactionReferences(
                List.of(new SavingsAccountTransactionReplacement(replacedTransaction, replacementTransaction)));

        verify(transferTransaction).updateToSavingsTransaction(replacementTransaction);
        verify(transferTransaction, never()).updateFromSavingsTransaction(replacementTransaction);
        verify(accountTransferRepository).saveAll(List.of(transferTransaction));
    }

    @Test
    void updateTransactionReferenceToFinalReplacement() {
        final var replacedTransaction = transaction(9L);
        final var firstReplacement = transaction(10L);
        final var finalReplacement = transaction(11L);
        final var transferTransaction = mock(AccountTransferTransaction.class);
        when(transferTransaction.getFromTransaction()).thenReturn(replacedTransaction);
        when(accountTransferRepository.findBySavingsTransactionIds(Set.of(9L, 10L))).thenReturn(List.of(transferTransaction));

        service.updateSavingsTransactionReferences(List.of(new SavingsAccountTransactionReplacement(replacedTransaction, firstReplacement),
                new SavingsAccountTransactionReplacement(firstReplacement, finalReplacement)));

        verify(transferTransaction).updateFromSavingsTransaction(finalReplacement);
        verify(accountTransferRepository).saveAll(List.of(transferTransaction));
    }

    @Test
    void identifyBothSidesOfTransferReplacementForAccounting() {
        final var replacedSource = transaction(5L);
        final var replacementSource = transaction(6L);
        final var replacedDestination = transaction(7L);
        final var replacementDestination = transaction(8L);
        final var transferTransaction = mock(AccountTransferTransaction.class);
        when(transferTransaction.getFromTransaction()).thenReturn(replacedSource);
        when(transferTransaction.getToSavingsTransaction()).thenReturn(replacedDestination);
        when(accountTransferRepository.findBySavingsTransactionIds(Set.of(5L, 7L))).thenReturn(List.of(transferTransaction));

        final var transactionIds = service
                .findTransferTransactionIds(List.of(new SavingsAccountTransactionReplacement(replacedSource, replacementSource),
                        new SavingsAccountTransactionReplacement(replacedDestination, replacementDestination)));

        assertEquals(Set.of(5L, 6L, 7L, 8L), transactionIds);
    }

    @Test
    void identifyEveryTransactionInReplacementChainForAccounting() {
        final var replacedTransaction = transaction(12L);
        final var firstReplacement = transaction(13L);
        final var finalReplacement = transaction(14L);
        final var transferTransaction = mock(AccountTransferTransaction.class);
        when(transferTransaction.getFromTransaction()).thenReturn(replacedTransaction);
        when(accountTransferRepository.findBySavingsTransactionIds(Set.of(12L, 13L))).thenReturn(List.of(transferTransaction));

        final var transactionIds = service
                .findTransferTransactionIds(List.of(new SavingsAccountTransactionReplacement(replacedTransaction, firstReplacement),
                        new SavingsAccountTransactionReplacement(firstReplacement, finalReplacement)));

        assertEquals(Set.of(12L, 13L, 14L), transactionIds);
    }

    @Test
    void ignoreEmptyReplacementCollection() {
        assertEquals(Set.of(), service.findTransferTransactionIds(List.of()));
        service.updateSavingsTransactionReferences(List.of());

        verifyNoInteractions(accountTransferRepository);
    }

    private SavingsAccountTransaction transaction(final Long id) {
        final var transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getId()).thenReturn(id);
        return transaction;
    }
}
