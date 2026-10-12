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
package org.apache.fineract.cob.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.AccountLock;
import org.apache.fineract.cob.service.AccountLockService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;

/**
 * Tasklet that unlocks accounts which were successfully processed during COB but whose locks were not removed.
 *
 * An account is considered successfully processed when its {@code last_closed_business_date} matches the
 * {@code lock_placed_on_cob_business_date} on the lock record, proving all COB business steps completed. If the lock
 * still exists with no error, it is orphaned and should be removed.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class UnlockProcessedAccountsTasklet<T extends AccountLock> implements Tasklet {

    private final AccountLockService<T> accountLockService;

    @Override
    public RepeatStatus execute(@NonNull final StepContribution contribution, @NonNull final ChunkContext chunkContext) throws Exception {
        final int removedCount = accountLockService.removeOrphanedLocksForProcessedAccounts();
        if (removedCount > 0) {
            log.debug("Unlocked {} account(s) that completed COB processing but remained locked", removedCount);
        } else {
            log.debug("No orphaned account locks found after COB processing");
        }
        return RepeatStatus.FINISHED;
    }
}
