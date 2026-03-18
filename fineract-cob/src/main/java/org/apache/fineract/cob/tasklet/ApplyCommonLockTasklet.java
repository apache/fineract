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

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.converter.COBParameterConverter;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.domain.AccountLock;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.LockCannotBeAppliedException;
import org.apache.fineract.cob.resolver.CatchUpFlagResolver;
import org.apache.fineract.cob.service.RetrieveIdService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@RequiredArgsConstructor
public abstract class ApplyCommonLockTasklet<T extends AccountLock> implements Tasklet {

    private static final long NUMBER_OF_RETRIES = 3;
    private final FineractProperties fineractProperties;
    private final LockingService<T> loanLockingService;
    private final RetrieveIdService retrieveIdService;
    private final TransactionTemplate transactionTemplate;

    public abstract String getCOBParameter();

    public abstract LockOwner getLockOwner();

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext)
            throws LockCannotBeAppliedException {
        ExecutionContext executionContext = contribution.getStepExecution().getExecutionContext();
        long numberOfExecutions = contribution.getStepExecution().getCommitCount();
        COBParameter loanCOBParameter = COBParameterConverter.convert(executionContext.get(getCOBParameter()));
        boolean isCatchUp = CatchUpFlagResolver.resolve(contribution.getStepExecution());
        List<Long> loanIds;
        if (Objects.isNull(loanCOBParameter)
                || (Objects.isNull(loanCOBParameter.getMinAccountId()) && Objects.isNull(loanCOBParameter.getMaxAccountId()))
                || (loanCOBParameter.getMinAccountId().equals(0L) && loanCOBParameter.getMaxAccountId().equals(0L))) {
            loanIds = Collections.emptyList();
        } else {
            loanIds = new ArrayList<>(
                    retrieveIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, isCatchUp));
        }
        List<List<Long>> loanIdPartitions = Lists.partition(loanIds, getInClauseParameterSizeLimit());
        List<T> accountLocks = new ArrayList<>();
        loanIdPartitions.forEach(loanIdPartition -> accountLocks.addAll(loanLockingService.findAllByLoanIdIn(loanIdPartition)));

        List<Long> toBeProcessedLoanIds = new ArrayList<>(loanIds);
        List<Long> alreadyLockedAccountIds = accountLocks.stream().map(AccountLock::getId).toList();

        toBeProcessedLoanIds.removeAll(alreadyLockedAccountIds);
        try {
            applyLocks(toBeProcessedLoanIds);
        } catch (Exception e) {
            if (numberOfExecutions > NUMBER_OF_RETRIES) {
                String message = "There was an error applying lock to loan accounts.";
                log.error("{}", message, e);
                throw new LockCannotBeAppliedException(message, e);
            } else {
                return RepeatStatus.CONTINUABLE;
            }
        }

        return RepeatStatus.FINISHED;
    }

    private void applyLocks(List<Long> toBeProcessedLoanIds) {
        transactionTemplate.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                log.info("Apply locks for {} by owner {}", toBeProcessedLoanIds, getLockOwner());
                loanLockingService.applyLock(toBeProcessedLoanIds, getLockOwner());
            }
        });
    }

    private int getInClauseParameterSizeLimit() {
        return fineractProperties.getQuery().getInClauseParameterSizeLimit();
    }
}
