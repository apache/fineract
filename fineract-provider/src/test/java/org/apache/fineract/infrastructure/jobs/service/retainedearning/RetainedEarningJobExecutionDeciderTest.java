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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.parameters.JobParameters;

@ExtendWith(MockitoExtension.class)
class RetainedEarningJobExecutionDeciderTest {

    private final LocalDate fiscalYearEndDate = LocalDate.of(2025, 12, 31);
    private final JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters());

    @InjectMocks
    private RetainedEarningJobExecutionDecider decider;

    @Mock
    private AccountGLJournalEntryAnnualSummaryRepository annualSummaryRepository;

    @BeforeEach
    public void setUp() {
        jobExecution.getExecutionContext().put(RetainedEarningJobConstant.FISCAL_YEAR_END_DATE_CONTEXT_KEY, fiscalYearEndDate);
    }

    @Test
    public void testDecideContinueExecutionWhenNoEntriesExist() {
        when(annualSummaryRepository.existsByYearEndDate(fiscalYearEndDate)).thenReturn(false);

        final FlowExecutionStatus status = decider.decide(jobExecution, null);

        assertEquals(RetainedEarningJobConstant.CONTINUE_JOB_EXECUTION, status.getName(),
                "Should continue execution when no entries exist for the fiscal year end date");
    }

    @Test
    public void testDecideNoOpExecutionWhenEntriesAlreadyExist() {
        when(annualSummaryRepository.existsByYearEndDate(fiscalYearEndDate)).thenReturn(true);

        final FlowExecutionStatus status = decider.decide(jobExecution, null);

        assertEquals(RetainedEarningJobConstant.NO_OP_EXECUTION, status.getName(),
                "Should perform no-op execution when entries already exist for the fiscal year end date");
    }
}
