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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob.listener;

import static org.apache.fineract.infrastructure.jobs.service.aggregationjob.JournalEntryAggregationJobConstant.JOB_SUMMARY_STEP_NAME;
import static org.apache.fineract.infrastructure.jobs.service.aggregationjob.JournalEntryAggregationJobConstant.JOURNAL_ENTRY_AGGREGATION_JOB_NAME;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.JournalEntryAggregationJobConstant;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain.JournalEntryAggregationTrackingRepository;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.services.JournalEntryAggregationWriterService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JournalEntryAggregationJobListener implements JobExecutionListener {

    private static final List<String> successJobStatus = List.of(ExitStatus.COMPLETED.getExitCode(), ExitStatus.NOOP.getExitCode());

    private final FineractProperties fineractProperties;
    private final JournalEntryAggregationTrackingRepository journalEntryAggregationTrackingRepository;
    private final JournalEntryAggregationWriterService journalEntryAggregationWriterService;

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        log.info("Journal Entry Aggregation Job Started  jobName={}, jobExecutionId={}", JOURNAL_ENTRY_AGGREGATION_JOB_NAME,
                jobExecution.getId());
        LocalDate providedAggregatedOnDate = (LocalDate) jobExecution.getExecutionContext()
                .get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE);
        // if aggregatedOnDate not provided in the parameter it will be defaulted to businessDate.
        final LocalDate aggregatedOnDate = providedAggregatedOnDate != null //
                ? providedAggregatedOnDate.minusDays(fineractProperties.getJob().getJournalEntryAggregation().getExcludeRecentNDays()) //
                : ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.BUSINESS_DATE)
                        .minusDays(fineractProperties.getJob().getJournalEntryAggregation().getExcludeRecentNDays()); //
        // get last or most recent aggregatedOnDate from tacking table.
        final LocalDate lastAggregatedOnDate = journalEntryAggregationTrackingRepository.findLatestAggregatedOnDate();

        initializeDates(jobExecution, aggregatedOnDate, lastAggregatedOnDate);
    }

    private void initializeDates(final JobExecution jobExecution, final LocalDate aggregatedOnDate, final LocalDate lastAggregatedOnDate) {
        jobExecution.getExecutionContext().put(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE, aggregatedOnDate);
        jobExecution.getExecutionContext().put(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_TO, aggregatedOnDate);
        if (lastAggregatedOnDate == null) {
            // Job is running for the first time
            jobExecution.getExecutionContext().put(JournalEntryAggregationJobConstant.LAST_AGGREGATED_ON_DATE, null);
            jobExecution.getExecutionContext().put(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_FROM, LocalDate.of(1970, 1, 1));
        } else {
            // Job is running for the subsequent time.
            jobExecution.getExecutionContext().put(JournalEntryAggregationJobConstant.LAST_AGGREGATED_ON_DATE, lastAggregatedOnDate);
            jobExecution.getExecutionContext().put(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_FROM, lastAggregatedOnDate);
        }
    }

    @Override
    public void afterJob(final JobExecution jobExecution) {
        // if job not completed successfully, rollback any insertion made.
        if (!successJobStatus.contains(jobExecution.getExitStatus().getExitCode())) {
            journalEntryAggregationWriterService.rollbackJournalEntrySummary(jobExecution.getId());
            journalEntryAggregationWriterService.rollbackJournalEntryTracking(jobExecution.getId());
        }
        // if job completed successfully, log the summary.
        logJobExecutionSummary(jobExecution);
    }

    private void logJobExecutionSummary(final JobExecution jobExecution) {
        final LocalDate aggregatedOnDateFrom = (LocalDate) jobExecution.getExecutionContext()
                .get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_FROM);
        final LocalDate aggregatedOnDateTo = (LocalDate) jobExecution.getExecutionContext()
                .get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_TO);
        final Long jobExecutionId = jobExecution.getId();
        final Long recordProcessCount = jobExecution.getStepExecutions().stream()
                .filter(stepExecution -> stepExecution.getStepName().equals(JOB_SUMMARY_STEP_NAME)).mapToLong(StepExecution::getWriteCount)
                .sum();
        final Instant startDateTime = jobExecution.getStartTime() != null ? jobExecution.getStartTime().toInstant(ZoneOffset.UTC) : null;
        final Instant endDateTime = jobExecution.getEndTime() != null ? jobExecution.getEndTime().toInstant(ZoneOffset.UTC) : null;
        long jobDuration = 0L;
        Long startDateTimeMilliSecond = null;
        Long endDateTimeMilliSecond = null;
        if (startDateTime != null && endDateTime != null) {
            startDateTimeMilliSecond = startDateTime.toEpochMilli();
            endDateTimeMilliSecond = endDateTime.toEpochMilli();
            jobDuration = startDateTime.until(endDateTime, ChronoUnit.MINUTES);
        }
        log.info(
                "Execution Summary for jobName={}, aggregatedDateFrom={}, aggregatedDateTo={}, totalRecordProcessCount={}, startTime={}, endTime={}, startTime_ms={}, endTime_ms={}, "
                        + "jobExecutionId={}, jobExecutionDurationInMinutes={}, tenantId={}",
                JOURNAL_ENTRY_AGGREGATION_JOB_NAME, aggregatedOnDateFrom, aggregatedOnDateTo, recordProcessCount, startDateTime,
                endDateTime, startDateTimeMilliSecond, endDateTimeMilliSecond, jobExecutionId, jobDuration,
                ThreadLocalContextUtil.getTenant().getTenantIdentifier());
    }
}
