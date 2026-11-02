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

import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.FISCAL_YEAR_END_DATE_CONTEXT_KEY;
import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.JOB_SUMMARY_STEP_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningConfigurationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;

@ExtendWith(MockitoExtension.class)
class RetainedEarningJobListenerTest {

    @Mock
    private RetainedEarningConfigurationService retainedEarningConfigurationService;

    @InjectMocks
    private RetainedEarningJobListener retainedEarningJobListener;

    private final LocalDate businessDate = LocalDate.of(2026, 1, 1);
    private final LocalDate lastDayOfPreviousFiscalYear = LocalDate.of(2025, 12, 31);

    @BeforeEach
    public void setup() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default tenant", "UTC", null));
        final HashMap<BusinessDateType, LocalDate> businessDateMap = new HashMap<>();
        businessDateMap.put(BusinessDateType.BUSINESS_DATE, businessDate);
        ThreadLocalContextUtil.setBusinessDates(businessDateMap);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testBeforeJobStoresFiscalYearEndDateInContext() {
        final JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters());
        when(retainedEarningConfigurationService.getLastDayOfPreviousFiscalYear(businessDate)).thenReturn(lastDayOfPreviousFiscalYear);

        retainedEarningJobListener.beforeJob(jobExecution);

        assertEquals(lastDayOfPreviousFiscalYear,
                jobExecution.getExecutionContext().get(FISCAL_YEAR_END_DATE_CONTEXT_KEY, LocalDate.class));
    }

    @Test
    public void testAfterJob() {
        final JobExecution jobExecution = mock(JobExecution.class);
        // Mock start and end times
        LocalDateTime startTime = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(10);
        LocalDateTime endTime = LocalDateTime.now(ZoneId.systemDefault());

        when(jobExecution.getStartTime()).thenReturn(startTime);
        when(jobExecution.getEndTime()).thenReturn(endTime);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);

        StepExecution stepExecution = mock(StepExecution.class);
        when(stepExecution.getStepName()).thenReturn(JOB_SUMMARY_STEP_NAME);
        when(stepExecution.getWriteCount()).thenReturn(100L);

        Set<StepExecution> stepExecutions = new HashSet<>();
        stepExecutions.add(stepExecution);
        when(jobExecution.getStepExecutions()).thenReturn(stepExecutions);

        // This also primarily logs a message, we can test that it completes without exceptions
        retainedEarningJobListener.afterJob(jobExecution);
        verify(jobExecution).getId();
        verify(jobExecution).getStepExecutions();
        verify(jobExecution).getStartTime();
        verify(jobExecution).getEndTime();
        verify(jobExecution).getStatus();
        verify(jobExecution).getExitStatus();
    }
}
