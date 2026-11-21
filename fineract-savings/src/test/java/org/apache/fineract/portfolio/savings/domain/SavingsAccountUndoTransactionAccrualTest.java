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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.junit.jupiter.api.Test;

class SavingsAccountUndoTransactionAccrualTest {

    private static final LocalDate DEPOSIT_DATE = LocalDate.of(2026, 7, 1);

    @Test
    void undoingADepositReversesTheAccrualsFromItsDate() {
        final var account = new SavingsAccount() {};
        final SavingsAccountTransaction deposit = transaction(1L, SavingsAccountTransactionType.DEPOSIT, DEPOSIT_DATE);
        final SavingsAccountTransaction accrualBefore = transaction(2L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.minusDays(1));
        final SavingsAccountTransaction accrualSameDay = transaction(3L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE);
        final SavingsAccountTransaction accrualAfter = transaction(4L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.plusDays(30));
        account.transactions = new ArrayList<>(List.of(accrualBefore, deposit, accrualSameDay, accrualAfter));

        account.undoTransaction(1L);

        verify(deposit).reverse();
        verify(accrualSameDay).reverse();
        verify(accrualAfter).reverse();
        verify(accrualBefore, never()).reverse();
    }

    @Test
    void undoingADepositWithPivotDateReversesTheAccrualsFromItsDate() {
        final var account = new SavingsAccount() {};
        final SavingsAccountTransaction deposit = transaction(1L, SavingsAccountTransactionType.DEPOSIT, DEPOSIT_DATE);
        final SavingsAccountTransaction accrualBefore = transaction(2L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.minusDays(1));
        final SavingsAccountTransaction accrualAfter = transaction(3L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.plusDays(30));
        account.savingsAccountTransactions = new ArrayList<>(List.of(accrualBefore, deposit, accrualAfter));

        account.undoSavingsTransaction(1L);

        verify(deposit).reverse();
        verify(accrualAfter).reverse();
        verify(accrualBefore, never()).reverse();
    }

    @Test
    void reversingATransactionReversesTheAccrualsFromItsDate() {
        final var account = new SavingsAccount() {};
        final SavingsAccountTransaction deposit = transaction(1L, SavingsAccountTransactionType.DEPOSIT, DEPOSIT_DATE);
        final SavingsAccountTransaction accrualBefore = transaction(2L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.minusDays(1));
        final SavingsAccountTransaction accrualAfter = transaction(3L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.plusDays(30));
        account.transactions = new ArrayList<>(List.of(accrualBefore, deposit, accrualAfter));

        account.undoTransaction(deposit, false);

        verify(deposit).reverse();
        verify(accrualAfter).reverse();
        verify(accrualBefore, never()).reverse();
    }

    @Test
    void reversingATransactionWithPivotDateUsesOnlyTheTransactionsAfterThePivot() {
        final var account = new SavingsAccount() {};
        final SavingsAccountTransaction deposit = transaction(1L, SavingsAccountTransactionType.DEPOSIT, DEPOSIT_DATE);
        final SavingsAccountTransaction accrualBefore = transaction(2L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.minusDays(1));
        final SavingsAccountTransaction accrualAfter = transaction(3L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.plusDays(30));
        // with the pivot date config on, the account only has the transactions after the pivot loaded
        account.savingsAccountTransactions = new ArrayList<>(List.of(accrualBefore, deposit, accrualAfter));
        final SavingsAccountTransaction notLoaded = transaction(4L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.plusDays(1));
        account.transactions = new ArrayList<>(List.of(notLoaded));

        account.undoTransaction(deposit, true);

        verify(deposit).reverse();
        verify(accrualAfter).reverse();
        verify(accrualBefore, never()).reverse();
        verify(notLoaded, never()).reverse();
    }

    @Test
    void reversingAnAccrualDoesNotReverseOtherAccruals() {
        final var account = new SavingsAccount() {};
        final SavingsAccountTransaction accrual = transaction(1L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE);
        final SavingsAccountTransaction laterAccrual = transaction(2L, SavingsAccountTransactionType.ACCRUAL, DEPOSIT_DATE.plusDays(1));
        account.transactions = new ArrayList<>(List.of(accrual, laterAccrual));

        account.undoTransaction(accrual, false);

        verify(accrual).reverse();
        verify(laterAccrual, never()).reverse();
    }

    private static SavingsAccountTransaction transaction(final Long id, final SavingsAccountTransactionType type, final LocalDate date) {
        final SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getId()).thenReturn(id);
        when(transaction.isIdentifiedBy(id)).thenReturn(true);
        when(transaction.getTransactionType()).thenReturn(type);
        when(transaction.isAccrual()).thenReturn(type == SavingsAccountTransactionType.ACCRUAL);
        when(transaction.getTransactionDate()).thenReturn(date);
        when(transaction.getDateOf()).thenReturn(date);
        return transaction;
    }
}
