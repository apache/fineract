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
package org.apache.fineract.cob.savings;

import java.util.List;
import org.apache.fineract.cob.domain.AccountLockRepository;
import org.apache.fineract.cob.domain.CustomLoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.service.AbstractAccountLockService;
import org.springframework.stereotype.Service;

@Service
public class SavingsAccountLockServiceImpl extends AbstractAccountLockService<SavingsAccountLock> {

    private static final List<LockOwner> SAVINGS_COB_LOCK_OWNERS = List.of(LockOwner.SAVINGS_COB_CHUNK_PROCESSING,
            LockOwner.SAVINGS_INLINE_COB_PROCESSING);

    public SavingsAccountLockServiceImpl(final AccountLockRepository<SavingsAccountLock> savingsAccountLockRepository,
            final CustomLoanAccountLockRepository<SavingsAccountLock> customSavingsAccountLockRepository) {
        super(savingsAccountLockRepository, customSavingsAccountLockRepository);
    }

    @Override
    protected List<LockOwner> getCobLockOwners() {
        return SAVINGS_COB_LOCK_OWNERS;
    }
}
