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
package org.apache.fineract.cob.domain;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.cob.savings.SavingsAccountLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavingsAccountLockRepository
        extends JpaRepository<SavingsAccountLock, Long>, JpaSpecificationExecutor<SavingsAccountLock> {

    Optional<SavingsAccountLock> findBySavingsIdAndLockOwner(Long savingsId, LockOwner lockOwner);

    void deleteBySavingsIdInAndLockOwner(List<Long> savingsIds, LockOwner lockOwner);

    List<SavingsAccountLock> findAllBySavingsIdIn(List<Long> savingsIds);

    boolean existsBySavingsIdAndLockOwner(Long savingsId, LockOwner lockOwner);

    boolean existsBySavingsIdAndLockOwnerAndErrorIsNotNull(Long savingsId, LockOwner lockOwner);

    @Query("""
            delete from SavingsAccountLock lck where lck.lockPlacedOnCobBusinessDate is not null and lck.error is not null and
            lck.lockOwner in (org.apache.fineract.cob.domain.LockOwner.SAVINGS_COB_CHUNK_PROCESSING,
                              org.apache.fineract.cob.domain.LockOwner.SAVINGS_INLINE_COB_PROCESSING)
            """)
    @Modifying(clearAutomatically = true)
    void removeLockByOwner();

    List<SavingsAccountLock> findAllBySavingsIdInAndLockOwner(List<Long> savingsIds, LockOwner lockOwner);

    @Query("""
            delete from SavingsAccountLock lck
            where lck.error is null
              and lck.lockPlacedOnCobBusinessDate is not null
              and lck.lockOwner in :lockOwners
              and exists (
                select sa from SavingsAccount sa
                where sa.id = lck.savingsId
                  and sa.lastClosedBusinessDate = lck.lockPlacedOnCobBusinessDate
              )
            """)
    @Modifying(clearAutomatically = true)
    int removeOrphanedLocksForProcessedAccounts(@Param("lockOwners") List<LockOwner> lockOwners);
}
