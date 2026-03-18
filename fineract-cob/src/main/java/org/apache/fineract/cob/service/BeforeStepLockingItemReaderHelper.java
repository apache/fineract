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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.converter.COBParameterConverter;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.domain.AccountLock;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.resolver.CatchUpFlagResolver;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public class BeforeStepLockingItemReaderHelper<T extends AccountLock> {

    private final RetrieveIdService retrieveIdService;
    private final LockingService<T> loanLockingService;

    @SuppressWarnings({ "unchecked" })
    public LinkedBlockingQueue<Long> filterRemainingData(@NonNull StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        COBParameter loanCOBParameter = COBParameterConverter.convert(executionContext.get(COBConstant.COB_PARAMETER));
        List<Long> loanIds;
        boolean isCatchUp = CatchUpFlagResolver.resolve(stepExecution);
        if (Objects.isNull(loanCOBParameter)
                || (Objects.isNull(loanCOBParameter.getMinAccountId()) && Objects.isNull(loanCOBParameter.getMaxAccountId()))
                || (loanCOBParameter.getMinAccountId().equals(0L) && loanCOBParameter.getMaxAccountId().equals(0L))) {
            loanIds = Collections.emptyList();
        } else {
            loanIds = retrieveIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, isCatchUp);
            if (!loanIds.isEmpty()) {
                List<Long> lockedByCOBChunkProcessingAccountIds = getLoanIdsLockedWithChunkProcessingLock(loanIds);
                loanIds.retainAll(lockedByCOBChunkProcessingAccountIds);
            }
        }
        return new LinkedBlockingQueue<>(loanIds);
    }

    private List<Long> getLoanIdsLockedWithChunkProcessingLock(List<Long> loanIds) {
        List<T> accountLocks = new ArrayList<>(
                loanLockingService.findAllByLoanIdInAndLockOwner(loanIds, LockOwner.LOAN_COB_CHUNK_PROCESSING));
        return accountLocks.stream().map(T::getId).toList();
    }
}
