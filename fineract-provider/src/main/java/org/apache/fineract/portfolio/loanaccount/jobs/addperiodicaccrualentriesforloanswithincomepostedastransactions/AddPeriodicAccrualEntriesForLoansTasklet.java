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
package org.apache.fineract.portfolio.loanaccount.jobs.addperiodicaccrualentriesforloanswithincomepostedastransactions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class AddPeriodicAccrualEntriesForLoansTasklet implements Tasklet {

    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Collection<Long> loanIds = loanReadPlatformService.retrieveLoanIdsWithPendingIncomePostingTransactions();
        if (!CollectionUtils.isEmpty(loanIds)) {
            List<Throwable> errors = new ArrayList<>();
            for (Long loanId : loanIds) {
                try {
                    loanAccrualsProcessingService.addIncomeAndAccrualTransactions(loanId);
                } catch (Exception e) {
                    log.error("Failed to add income and accrual transaction for loan {}", loanId, e);
                    errors.add(e);
                }
            }
            if (!errors.isEmpty()) {
                throw new JobExecutionException(errors);
            }
        }
        return RepeatStatus.FINISHED;
    }
}
