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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.lang.NonNull;

@Slf4j
public class LoanItemReader extends AbstractLoanItemReader {

    private final RetrieveLoanIdService retrieveLoanIdService;
    private final CustomJobParameterResolver customJobParameterResolver;
    private final LoanLockingService loanLockingService;

    public LoanItemReader(LoanRepository loanRepository, RetrieveLoanIdService retrieveLoanIdService,
            CustomJobParameterResolver customJobParameterResolver, LoanLockingService loanLockingService) {
        super(loanRepository);
        this.retrieveLoanIdService = retrieveLoanIdService;
        this.customJobParameterResolver = customJobParameterResolver;
        this.loanLockingService = loanLockingService;
    }

    @BeforeStep
    @SuppressWarnings({ "unchecked" })
    public void beforeStep(@NonNull StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        COBParameter loanCOBParameter = (COBParameter) executionContext.get(LoanCOBConstant.LOAN_COB_PARAMETER);
        List<Long> loanIds;
        if (Objects.isNull(loanCOBParameter)
                || (Objects.isNull(loanCOBParameter.getMinAccountId()) && Objects.isNull(loanCOBParameter.getMaxAccountId()))
                || (loanCOBParameter.getMinAccountId().equals(0L) && loanCOBParameter.getMaxAccountId().equals(0L))) {
            loanIds = Collections.emptyList();
        } else {
            loanIds = retrieveLoanIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter,
                    customJobParameterResolver.getCustomJobParameterById(stepExecution, LoanCOBConstant.IS_CATCH_UP_PARAMETER_NAME)
                            .map(Boolean::parseBoolean).orElse(false));
            if (loanIds.size() > 0) {
                List<Long> lockedByCOBChunkProcessingAccountIds = getLoanIdsLockedWithChunkProcessingLock(loanIds);
                loanIds.retainAll(lockedByCOBChunkProcessingAccountIds);
            }
        }
        setRemainingData(new LinkedBlockingQueue<>(loanIds));
    }

    private List<Long> getLoanIdsLockedWithChunkProcessingLock(List<Long> loanIds) {
        List<LoanAccountLock> accountLocks = new ArrayList<>();
        accountLocks.addAll(loanLockingService.findAllByLoanIdInAndLockOwner(loanIds, LockOwner.LOAN_COB_CHUNK_PROCESSING));
        return accountLocks.stream().map(LoanAccountLock::getLoanId).toList();
    }
}
