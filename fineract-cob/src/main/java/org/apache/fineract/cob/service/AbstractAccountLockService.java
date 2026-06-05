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
package org.apache.fineract.cob.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.domain.AccountLock;
import org.apache.fineract.cob.domain.AccountLockRepository;
import org.apache.fineract.cob.domain.CustomLoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public abstract class AbstractAccountLockService<T extends AccountLock> implements AccountLockService<T> {

    protected static final List<LockOwner> COB_LOCK_OWNERS = List.of(LockOwner.LOAN_COB_CHUNK_PROCESSING,
            LockOwner.LOAN_INLINE_COB_PROCESSING);

    private final AccountLockRepository<T> loanAccountLockRepository;
    private final CustomLoanAccountLockRepository<T> customLoanAccountLockRepository;

    @Override
    @Transactional(readOnly = true)
    public List<T> getLockedLoanAccountByPage(int page, int limit) {
        Pageable loanAccountLockPage = PageRequest.of(page, limit);
        Page<T> loanAccountLocks = loanAccountLockRepository.findAll(loanAccountLockPage);
        return loanAccountLocks.getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAnyLoanHardLocked(List<Long> loanIds) {
        return !loanIds.isEmpty() && loanAccountLockRepository.existsByLoanIdInAndLockOwnerIn(loanIds, getCobLockOwners());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAnyLockOverrulable(List<Long> loanIds) {
        return !loanIds.isEmpty() && loanAccountLockRepository.existsByLoanIdInAndLockOwnerInAndErrorIsNotNull(loanIds, getCobLockOwners());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCobAndRemoveLocks() {
        customLoanAccountLockRepository.updateLoanFromAccountLocks();
        loanAccountLockRepository.removeByLockOwnerInAndErrorIsNotNullAndLockPlacedOnCobBusinessDateIsNotNull(getCobLockOwners());
    }

    /**
     * Lock owners that mark an account as locked by COB (batch or inline). Defaults to the loan owners, which the loan
     * and working capital loan lock tables share; account types with their own owners override it.
     */
    protected List<LockOwner> getCobLockOwners() {
        return COB_LOCK_OWNERS;
    }

    @Override
    public int removeOrphanedLocksForProcessedAccounts() {
        return loanAccountLockRepository.deleteOrphanedLocksForProcessedAccounts(getCobLockOwners());
    }

}
