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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * Two transfers in opposite directions (A to B and B to A) deadlock when each locks its own account first. The accounts
 * are therefore always locked in ascending id order, whatever the direction of the transfer.
 */
class SavingsAccountRepositoryWrapperLockOrderTest {

    private final SavingsAccountRepository repository = mock(SavingsAccountRepository.class);
    private final SavingsAccountRepositoryWrapper underTest = new SavingsAccountRepositoryWrapper(repository,
            mock(SavingsAccountTransactionRepository.class));

    @Test
    void accountsAreLockedInAscendingIdOrderWhateverTheDirection() {
        underTest.lockInIdOrder(9L, 3L);
        verify(repository).findAllLockedOrderedById(List.of(3L, 9L));
    }

    @Test
    void theOppositeDirectionLocksTheSameOrder() {
        underTest.lockInIdOrder(3L, 9L);
        verify(repository).findAllLockedOrderedById(List.of(3L, 9L));
    }

    @Test
    void nullAndDuplicateIdsAreIgnored() {
        underTest.lockInIdOrder(5L, null, 5L);
        verify(repository).findAllLockedOrderedById(List.of(5L));
    }

    @Test
    void nothingIsLockedWithoutIds() {
        underTest.lockInIdOrder((Long) null);
        verify(repository, never()).findAllLockedOrderedById(ArgumentMatchers.any());
    }
}
