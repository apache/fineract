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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

@ExtendWith(MockitoExtension.class)
class RetainedEarningJobListenerTest {

    @InjectMocks
    private RetainedEarningJobListener retainedEarningJobListener;

    @Mock
    private JobExecution jobExecution;

    @BeforeEach
    public void setup() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default tenant", "UTC", null));
    }

    @Test
    public void testBeforeJob() {
        // beforeJob should complete without exceptions and log the job ID
        retainedEarningJobListener.beforeJob(jobExecution);
        verify(jobExecution).getJobId();
    }

    @Test
    public void testAfterJob() {
        // Mock start and end times
        LocalDateTime startTime = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(10);
        LocalDateTime endTime = LocalDateTime.now(ZoneId.systemDefault());

        when(jobExecution.getStartTime()).thenReturn(startTime);
        when(jobExecution.getEndTime()).thenReturn(endTime);

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
    }
}
