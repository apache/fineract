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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.lang.NonNull;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSavingsItemWriter extends RepositoryItemWriter<SavingsAccount> {

    private final LockingService savingsLockingService;

    @Override
    public void write(@NonNull Chunk<? extends SavingsAccount> items) throws Exception {
        if (!items.isEmpty()) {
            super.write(items);
            List<Long> savingsIds = items.getItems().stream().map(AbstractPersistableCustom::getId).toList();
            savingsLockingService.deleteByLoanIdInAndLockOwner(savingsIds, getLockOwner());
        }
    }

    protected abstract LockOwner getLockOwner();
}
