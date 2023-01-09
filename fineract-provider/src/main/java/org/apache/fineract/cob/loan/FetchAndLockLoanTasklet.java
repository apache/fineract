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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class FetchAndLockLoanTasklet implements Tasklet {

    private static final Long NUMBER_OF_DAYS_BEHIND = 1L;

    private final LoanAccountLockRepository loanAccountLockRepository;

    private final RetrieveLoanIdService retrieveLoanIdService;

    @Override
    public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) throws Exception {
        String businessDateParameter = (String) contribution.getStepExecution().getJobExecution().getExecutionContext()
                .get(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME);
        LocalDate businessDate = LocalDate.parse(Objects.requireNonNull(businessDateParameter));
        List<Long> allNonClosedLoanIds = retrieveLoanIdService.retrieveLoanIdsNDaysBehind(NUMBER_OF_DAYS_BEHIND, businessDate);
        if (allNonClosedLoanIds.isEmpty()) {
            return RepeatStatus.FINISHED;
        }
        List<Long> remainingIds = new ArrayList<>(allNonClosedLoanIds);

        List<LoanAccountLock> loanAccountLocks = loanAccountLockRepository.findAllByLoanIdIn(remainingIds);

        List<Long> alreadySoftLockedAccounts = loanAccountLocks.stream()
                .filter(e -> LockOwner.LOAN_COB_PARTITIONING.equals(e.getLockOwner())).map(LoanAccountLock::getLoanId).toList();
        List<Long> alreadyMarkedForInlineCOBLockedAccounts = loanAccountLocks.stream()
                .filter(e -> LockOwner.LOAN_INLINE_COB_PROCESSING.equals(e.getLockOwner())).map(LoanAccountLock::getLoanId).toList();
        List<Long> alreadyMarkedForChunkProcessingLockedAccounts = loanAccountLocks.stream()
                .filter(e -> LockOwner.LOAN_COB_CHUNK_PROCESSING.equals(e.getLockOwner())).map(LoanAccountLock::getLoanId).toList();

        // Remove already hard locked accounts
        remainingIds.removeAll(alreadyMarkedForChunkProcessingLockedAccounts);
        remainingIds.removeAll(alreadyMarkedForInlineCOBLockedAccounts);

        List<Long> lockableLoanAccounts = new ArrayList<>(remainingIds);
        lockableLoanAccounts.removeAll(alreadySoftLockedAccounts);

        applySoftLock(lockableLoanAccounts);

        contribution.getStepExecution().getJobExecution().getExecutionContext().put(LoanCOBConstant.LOAN_IDS, remainingIds);

        return RepeatStatus.FINISHED;
    }

    private void applySoftLock(List<Long> alreadySoftLockedAccounts) {
        for (Long loanId : alreadySoftLockedAccounts) {
            LoanAccountLock loanAccountLock = new LoanAccountLock(loanId, LockOwner.LOAN_COB_PARTITIONING);
            loanAccountLockRepository.save(loanAccountLock);
        }
    }
}
