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
package org.apache.fineract.portfolio.loanaccount.jobs.setloandelinquencytags;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class SetLoanDelinquencyTagsTasklet implements Tasklet {

    private final DelinquencyWritePlatformService delinquencyWritePlatformService;
    private final LoanReadPlatformService loanReadPlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("Run job for date {}", DateUtils.getBusinessLocalDate());
        Collection<LoanScheduleDelinquencyData> loanScheduleDelinquencyData = this.loanReadPlatformService
                .retrieveScheduleDelinquencyData(DateUtils.getBusinessLocalDate());
        log.debug("Were found {} items", loanScheduleDelinquencyData.size());
        for (LoanScheduleDelinquencyData loanDelinquencyData : loanScheduleDelinquencyData) {
            log.debug("Processing Loan {} with due date {} and {} overdue days", loanDelinquencyData.getLoanId(),
                    loanDelinquencyData.getDueDate(), loanDelinquencyData.getAgeDays());
            this.delinquencyWritePlatformService.applyDelinquencyTagToLoan(loanDelinquencyData.getLoanId(),
                    loanDelinquencyData.getAgeDays());
        }

        log.debug("{}: Records affected by setLoanDelinquencyTags: {}", ThreadLocalContextUtil.getTenant().getName(),
                loanScheduleDelinquencyData.size());
        return RepeatStatus.FINISHED;
    }

}
