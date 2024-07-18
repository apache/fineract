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
package org.apache.fineract.portfolio.loanaccount.jobs.accrualactivityposting;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccrualActivityRepository;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualActivityProcessingService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccrualActivityPostingTasklet implements Tasklet {

    private final LoanAccrualActivityProcessingService loanAccrualActivityProcessingService;
    private final LoanAccrualActivityRepository loanAccrualActivityRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final LocalDate yesterday = DateUtils.getBusinessLocalDate().minusDays(1);
        List<Throwable> errors = new ArrayList<>();
        Set<Long> loanAccounts = loanAccrualActivityRepository.fetchLoanIdsForAccrualActivityPosting(yesterday);
        for (Long accountId : loanAccounts) {
            try {
                loanAccrualActivityProcessingService.makeAccrualActivityTransaction(accountId, yesterday);
            } catch (Exception e) {
                log.error("Failed to add accrual activity transaction for loan {}", accountId, e);
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }

        return RepeatStatus.FINISHED;
    }
}
