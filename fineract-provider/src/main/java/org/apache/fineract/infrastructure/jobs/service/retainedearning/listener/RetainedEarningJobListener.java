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
package org.apache.fineract.infrastructure.jobs.service.retainedearning.listener;

import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.JOB_SUMMARY_STEP_NAME;
import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.RETAINED_EARNING_JOB_NAME;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * The job listener
 */
@Component
@Slf4j
public class RetainedEarningJobListener implements JobExecutionListener {

    /**
     * {@inheritDoc}
     *
     * @param jobExecution
     *            the job execution
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Starting Retained Earning Job: {}", jobExecution.getJobId());
    }

    /**
     * {@inheritDoc}
     *
     * @param jobExecution
     *            the job execution
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        logJobExecutionSummary(jobExecution);
    }

    /**
     * Method to log the job execution summary
     *
     * @param jobExecution
     *            the job execution
     */
    private void logJobExecutionSummary(final JobExecution jobExecution) {
        final Long jobExecutionId = jobExecution.getId();
        final Long recordProcessCount = jobExecution.getStepExecutions().stream()
                .filter(stepExecution -> stepExecution.getStepName().equals(JOB_SUMMARY_STEP_NAME))
                .mapToLong(stepExecution -> stepExecution.getWriteCount()).sum();
        final Instant startDateTime = jobExecution.getStartTime().toInstant(ZoneOffset.UTC);
        final Instant endDateTime = jobExecution.getEndTime().toInstant(ZoneOffset.UTC);
        Long jobDuration = 0L;
        Long startDateTimeMilliSecond = null;
        Long endDateTimeMilliSecond = null;
        if (startDateTime != null && endDateTime != null) {
            startDateTimeMilliSecond = startDateTime.toEpochMilli();
            endDateTimeMilliSecond = endDateTime.toEpochMilli();
            jobDuration = startDateTime.until(endDateTime, ChronoUnit.MINUTES);
        }
        log.info(
                "Execution Summary for jobName={}, totalRecordProcessCount={}, startTime={}, endTime={}, startTime_ms={}, endTime_ms={}, "
                        + "jobExecutionId={}, jobExecutionDurationInMinutes={}, tenantId={}, jobStatus={}",
                RETAINED_EARNING_JOB_NAME, recordProcessCount, startDateTime, endDateTime, startDateTimeMilliSecond, endDateTimeMilliSecond,
                jobExecutionId, jobDuration, ThreadLocalContextUtil.getTenant().getTenantIdentifier(), jobExecution.getStatus());
    }
}
