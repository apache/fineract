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
package org.apache.fineract.infrastructure.jobs.service.retainedearning;

import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.CONTINUE_JOB_EXECUTION;
import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.FISCAL_YEAR_END_DATE_CONTEXT_KEY;
import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.NO_OP_EXECUTION;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummaryRepository;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

/**
 * Decides, before the retained earning summary step runs, whether the job should continue (fetch and persist trial
 * balance data) or end as a NoOp because entries already exist for the fiscal year end date.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetainedEarningJobExecutionDecider implements JobExecutionDecider {

    private final AccountGLJournalEntryAnnualSummaryRepository annualSummaryRepository;

    @Override
    public FlowExecutionStatus decide(final JobExecution jobExecution, final StepExecution stepExecution) {
        final LocalDate fiscalYearEndDate = Optional
                .ofNullable(jobExecution.getExecutionContext().get(FISCAL_YEAR_END_DATE_CONTEXT_KEY, LocalDate.class))
                .orElseThrow(() -> new IllegalStateException("Job execution context is missing " + FISCAL_YEAR_END_DATE_CONTEXT_KEY
                        + "; RetainedEarningJobListener#beforeJob must run and populate it before this decider executes."));

        if (annualSummaryRepository.existsByYearEndDate(fiscalYearEndDate)) {
            log.info("Retained earning entries already exist for jobExecutionId={}, fiscalYearEndDate={}. "
                    + "Skipping retained earning job execution.", jobExecution.getId(), fiscalYearEndDate);
            return new FlowExecutionStatus(NO_OP_EXECUTION);
        }
        log.info(
                "No retained earning entries exist for jobExecutionId={}, fiscalYearEndDate={}. Continuing retained earning job execution.",
                jobExecution.getId(), fiscalYearEndDate);
        return new FlowExecutionStatus(CONTINUE_JOB_EXECUTION);
    }
}
