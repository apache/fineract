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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob;

import java.time.LocalDate;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JournalEntryAggregationJobExecutionDecider implements JobExecutionDecider {

    @Override
    @NonNull
    public FlowExecutionStatus decide(final @NonNull JobExecution jobExecution, final StepExecution stepExecution) {
        final LocalDate aggregatedOnDate = (LocalDate) jobExecution.getExecutionContext()
                .get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE);
        final LocalDate lastAggregatedOnDate = (LocalDate) jobExecution.getExecutionContext()
                .get(JournalEntryAggregationJobConstant.LAST_AGGREGATED_ON_DATE);

        // check if aggregation for given date range already exist.
        if (aggregationAlreadyExist(lastAggregatedOnDate, aggregatedOnDate)) {
            log.info(
                    "Journal entry aggregation for given aggregatedOnDate already exist hence skipping jobName={} execution for aggregatedOnDate={}",
                    JournalEntryAggregationJobConstant.JOURNAL_ENTRY_AGGREGATION_JOB_NAME, aggregatedOnDate);
            jobExecution.setExitStatus(ExitStatus.NOOP);
            return new FlowExecutionStatus(JournalEntryAggregationJobConstant.NO_OP_EXECUTION);
        }
        log.info("Continue executing journal entry aggregation job jobName={} execution for aggregatedOnDate={}",
                JournalEntryAggregationJobConstant.JOURNAL_ENTRY_AGGREGATION_JOB_NAME, aggregatedOnDate);
        return new FlowExecutionStatus(JournalEntryAggregationJobConstant.CONTINUE_JOB_EXECUTION);
    }

    private boolean aggregationAlreadyExist(final LocalDate lastAggregatedOnDate, final LocalDate aggregatedOnDate) {
        if (Objects.isNull(lastAggregatedOnDate)) {
            log.info("Journal Entry aggregation job being executed for the first time");
            return false;
        }
        return aggregatedOnDate.isBefore(lastAggregatedOnDate) || aggregatedOnDate.isEqual(lastAggregatedOnDate);
    }
}
