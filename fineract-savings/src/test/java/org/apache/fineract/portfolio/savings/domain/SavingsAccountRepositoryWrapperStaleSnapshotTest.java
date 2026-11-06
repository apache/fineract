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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * On MySQL a locked account comes back with its latest version even when the transaction's snapshot is older, and the
 * rest of the transfer would read the account from that snapshot. Locking compares the two and fails when the account
 * changed since the snapshot, as PostgreSQL does.
 */
class SavingsAccountRepositoryWrapperStaleSnapshotTest {

    private final SavingsAccountRepository repository = mock(SavingsAccountRepository.class);
    private final SavingsAccountRepositoryWrapper underTest = new SavingsAccountRepositoryWrapper(repository,
            mock(SavingsAccountTransactionRepository.class), mock(PlatformSecurityContext.class));

    @Test
    void anAccountChangedSinceTheSnapshotFailsTheLock() {
        // another transaction credited account 9 after this one took its snapshot
        final List<SavingsAccount> lockedAccounts = List.of(account(3L, 1), account(9L, 5));
        when(repository.findAllLockedOrderedById(List.of(3L, 9L))).thenReturn(lockedAccounts);
        when(repository.findVersionsById(List.of(3L, 9L))).thenReturn(List.of(new Object[] { 3L, 1 }, new Object[] { 9L, 4 }));

        assertThatThrownBy(() -> underTest.lockInIdOrder(3L, 9L)).isInstanceOf(ObjectOptimisticLockingFailureException.class)
                .hasMessageContaining("[9]");
    }

    @Test
    void accountsUnchangedSinceTheSnapshotAreLocked() {
        final List<SavingsAccount> lockedAccounts = List.of(account(3L, 1), account(9L, 4));
        when(repository.findAllLockedOrderedById(List.of(3L, 9L))).thenReturn(lockedAccounts);
        when(repository.findVersionsById(List.of(3L, 9L))).thenReturn(List.of(new Object[] { 3L, 1 }, new Object[] { 9L, 4 }));

        assertThatCode(() -> underTest.lockInIdOrder(9L, 3L)).doesNotThrowAnyException();
    }

    @Test
    void anAccountMissingFromTheSnapshotFailsTheLock() {
        // account 9 was committed after this transaction took its snapshot, so the plain read does not see it
        final List<SavingsAccount> lockedAccounts = List.of(account(3L, 1), account(9L, 1));
        when(repository.findAllLockedOrderedById(List.of(3L, 9L))).thenReturn(lockedAccounts);
        when(repository.findVersionsById(List.of(3L, 9L))).thenReturn(List.<Object[]>of(new Object[] { 3L, 1 }));

        assertThatThrownBy(() -> underTest.lockInIdOrder(3L, 9L)).isInstanceOf(ObjectOptimisticLockingFailureException.class)
                .hasMessageContaining("[9]");
    }

    @Test
    void theSameAccountTwiceIsLockedOnce() {
        final List<SavingsAccount> lockedAccounts = List.of(account(3L, 1));
        when(repository.findAllLockedOrderedById(List.of(3L))).thenReturn(lockedAccounts);
        when(repository.findVersionsById(List.of(3L))).thenReturn(List.<Object[]>of(new Object[] { 3L, 1 }));

        assertThatCode(() -> underTest.lockInIdOrder(3L, null, 3L)).doesNotThrowAnyException();
        verify(repository).findAllLockedOrderedById(List.of(3L));
    }

    @Test
    void noAccountIdsLockNothing() {
        underTest.lockInIdOrder();
        underTest.lockInIdOrder((Long) null);

        verifyNoInteractions(repository);
    }

    private static SavingsAccount account(final Long id, final int version) {
        final SavingsAccount account = mock(SavingsAccount.class);
        when(account.getId()).thenReturn(id);
        when(account.getVersion()).thenReturn(version);
        return account;
    }
}
