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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.data.LoanCOBParameter;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class ApplyLoanLockTasklet implements Tasklet {

    private final FineractProperties fineractProperties;
    private final LoanLockingService loanLockingService;
    private final RetrieveLoanIdService retrieveLoanIdService;
    private final CustomJobParameterResolver customJobParameterResolver;

    @Override
    public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) throws Exception {
        ExecutionContext executionContext = contribution.getStepExecution().getExecutionContext();
        LoanCOBParameter loanCOBParameter = (LoanCOBParameter) executionContext.get(LoanCOBConstant.LOAN_COB_PARAMETER);
        List<Long> loanIds;
        if (Objects.isNull(loanCOBParameter)
                || (Objects.isNull(loanCOBParameter.getMinLoanId()) && Objects.isNull(loanCOBParameter.getMaxLoanId()))
                || (loanCOBParameter.getMinLoanId().equals(0L) && loanCOBParameter.getMaxLoanId().equals(0L))) {
            loanIds = Collections.emptyList();
        } else {
            loanIds = new ArrayList<>(
                    retrieveLoanIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter,
                            customJobParameterResolver
                                    .getCustomJobParameterById(contribution.getStepExecution(), LoanCOBConstant.IS_CATCH_UP_PARAMETER_NAME)
                                    .map(Boolean::parseBoolean).orElse(false)));
        }
        List<List<Long>> loanIdPartitions = Lists.partition(loanIds, getInClauseParameterSizeLimit());
        List<LoanAccountLock> accountLocks = new ArrayList<>();
        loanIdPartitions.forEach(loanIdPartition -> accountLocks.addAll(loanLockingService.findAllByLoanIdIn(loanIdPartition)));

        List<Long> alreadyLockedByChunkProcessingAccountIds = accountLocks.stream()
                .filter(e -> LockOwner.LOAN_COB_CHUNK_PROCESSING.equals(e.getLockOwner())).map(LoanAccountLock::getLoanId).toList();

        List<Long> toBeProcessedLoanIds = new ArrayList<>(loanIds);
        toBeProcessedLoanIds.removeAll(alreadyLockedByChunkProcessingAccountIds);

        loanLockingService.applyLock(toBeProcessedLoanIds, LockOwner.LOAN_COB_CHUNK_PROCESSING);
        return RepeatStatus.FINISHED;
    }

    private int getInClauseParameterSizeLimit() {
        return fineractProperties.getQuery().getInClauseParameterSizeLimit();
    }
}
