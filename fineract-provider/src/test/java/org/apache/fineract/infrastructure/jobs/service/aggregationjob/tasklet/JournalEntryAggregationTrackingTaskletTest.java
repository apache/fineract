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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.JournalEntryAggregationJobConstant;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationTrackingData;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.services.JournalEntryAggregationWriterService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JournalEntryAggregationTrackingTaskletTest {

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepContext stepContext;

    @Mock
    private StepExecution stepExecution;

    @Mock
    private JobExecution jobExecutionContext;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private JournalEntryAggregationWriterService journalEntryAggregationWriterService;

    @InjectMocks
    private JournalEntryAggregationTrackingTasklet underTest;

    /**
     * DataProvider for the testHappyPath method.
     *
     * @return the argument list
     */
    private static Stream<Arguments> recordWriteCount() {
        // spotless:off
        return Stream.of(Arguments.of(1L), Arguments.of(0L));
        // spotless:on
    }

    @ParameterizedTest
    @MethodSource("recordWriteCount")
    public void testHappyPath(final Long recordWriteCount) throws Exception {
        given(stepContribution.getStepExecution()).willReturn(stepExecution);
        given(stepExecution.getWriteCount()).willReturn(recordWriteCount);
        doNothing().when(journalEntryAggregationWriterService).insertJournalEntryTracking(any(JournalEntryAggregationTrackingData.class));
        given(chunkContext.getStepContext()).willReturn(stepContext);
        given(stepContext.getStepExecution()).willReturn(stepExecution);
        given(stepExecution.getJobExecution()).willReturn(jobExecutionContext);
        given(jobExecutionContext.getExecutionContext()).willReturn(executionContext);
        given(jobExecutionContext.getStepExecutions()).willReturn(Collections.singletonList(stepExecution));
        given(stepExecution.getStepName()).willReturn(JournalEntryAggregationJobConstant.JOB_SUMMARY_STEP_NAME);
        given(executionContext.get(any())).willReturn(LocalDate.now(Clock.systemUTC()));

        final HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        final LocalDate cobDate = LocalDate.now(Clock.systemUTC());
        businessDates.put(BusinessDateType.COB_DATE, cobDate);
        businessDates.put(BusinessDateType.BUSINESS_DATE, cobDate.plusDays(1));
        ThreadLocalContextUtil.setActionContext(ActionContext.COB);
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        RepeatStatus repeatStatus = underTest.execute(stepContribution, chunkContext);

        if (recordWriteCount > 0) {
            verify(chunkContext, times(1)).getStepContext();
            verify(stepContext, times(1)).getStepExecution();
            verify(stepExecution, times(3)).getJobExecution();
            verify(jobExecutionContext, times(2)).getExecutionContext();
            verify(executionContext, times(2)).get(any());
            verify(journalEntryAggregationWriterService, times(1))
                    .insertJournalEntryTracking(any(JournalEntryAggregationTrackingData.class));
        } else {
            verify(chunkContext, times(0)).getStepContext();
            verify(stepContext, times(0)).getStepExecution();
            verify(stepExecution, times(1)).getJobExecution();
            verify(jobExecutionContext, times(0)).getExecutionContext();
            verify(executionContext, times(0)).get(any());
        }
        assertEquals(RepeatStatus.FINISHED, repeatStatus);
    }

}
