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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob.tasklet;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.JournalEntryAggregationJobConstant;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationTrackingData;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.services.JournalEntryAggregationWriterService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class JournalEntryAggregationTrackingTasklet implements Tasklet {

    private final JournalEntryAggregationWriterService journalEntryAggregationWriterService;

    @Override
    public RepeatStatus execute(StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        // If no record is persisted in summary table then skip persisting data in tracking as well
        Optional<StepExecution> jobSummaryStepExecution = contribution.getStepExecution().getJobExecution().getStepExecutions().stream()
                .filter(stepExecution -> stepExecution.getStepName().equals(JournalEntryAggregationJobConstant.JOB_SUMMARY_STEP_NAME))
                .findFirst();
        long writeCount = jobSummaryStepExecution.map(StepExecution::getWriteCount).orElse(0L);

        log.info("Starting journal entry aggregation tasklet to insert into tracking table writeCount={}", writeCount);
        if (writeCount > 0) {
            final JobExecution jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution();
            final LocalDate aggregatedOnDateFrom = (LocalDate) jobExecutionContext.getExecutionContext()
                    .get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_FROM);
            final LocalDate aggregatedOnDateTo = (LocalDate) jobExecutionContext.getExecutionContext()
                    .get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_TO);
            JournalEntryAggregationTrackingData journalEntrySummaryTrackingDTO = JournalEntryAggregationTrackingData.builder()
                    .submittedOnDate(ThreadLocalContextUtil.getBusinessDate()).aggregatedOnDateFrom(aggregatedOnDateFrom)
                    .aggregatedOnDateTo(aggregatedOnDateTo).jobExecutionId(contribution.getStepExecution().getJobExecution().getId())
                    .build();
            journalEntryAggregationWriterService.insertJournalEntryTracking(journalEntrySummaryTrackingDTO);
        }
        return RepeatStatus.FINISHED;
    }
}
