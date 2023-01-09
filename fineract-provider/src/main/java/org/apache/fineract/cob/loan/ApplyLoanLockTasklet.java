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
package org.apache.fineract.cob.loan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class ApplyLoanLockTasklet implements Tasklet {

    private final LoanAccountLockRepository accountLockRepository;

    @Override
    public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) throws Exception {
        ExecutionContext executionContext = contribution.getStepExecution().getExecutionContext();
        List<Long> loanIds = (List<Long>) executionContext.get(LoanCOBConstant.LOAN_IDS);

        List<LoanAccountLock> accountLocks = accountLockRepository.findAllByLoanIdIn(loanIds);

        Map<Long, LoanAccountLock> alreadySoftLockedAccountsMap = accountLocks.stream()
                .filter(e -> LockOwner.LOAN_COB_PARTITIONING.equals(e.getLockOwner()))
                .collect(Collectors.toMap(LoanAccountLock::getLoanId, Function.identity()));

        List<Long> alreadyLockedByChunkProcessingAccountIds = accountLocks.stream()
                .filter(e -> LockOwner.LOAN_COB_CHUNK_PROCESSING.equals(e.getLockOwner())).map(LoanAccountLock::getLoanId).toList();

        List<Long> toBeProcessedLoanIds = new ArrayList<>(alreadySoftLockedAccountsMap.keySet());

        for (Long loanId : toBeProcessedLoanIds) {
            upgradeToHardLock(loanId, alreadySoftLockedAccountsMap);
        }

        toBeProcessedLoanIds.addAll(alreadyLockedByChunkProcessingAccountIds);
        List<Long> alreadyLockedByInlineCOBOrProcessedLoanIds = new ArrayList<>(loanIds);
        alreadyLockedByInlineCOBOrProcessedLoanIds.removeAll(toBeProcessedLoanIds);

        executionContext.put(LoanCOBConstant.ALREADY_LOCKED_BY_INLINE_COB_OR_PROCESSED_LOAN_IDS,
                new ArrayList<>(alreadyLockedByInlineCOBOrProcessedLoanIds));
        return RepeatStatus.FINISHED;
    }

    private void upgradeToHardLock(Long loanId, Map<Long, LoanAccountLock> alreadySoftLockedAccountsMap) {
        LoanAccountLock loanAccountLock = alreadySoftLockedAccountsMap.get(loanId);
        // Upgrade lock
        loanAccountLock.setNewLockOwner(LockOwner.LOAN_COB_CHUNK_PROCESSING);
        accountLockRepository.save(loanAccountLock);
    }
}
