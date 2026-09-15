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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import static org.apache.fineract.portfolio.account.jobs.executestandinginstructions.StandingInstructionTestData.dueFixedSavingsToSavings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.domain.StandingInstructionPriority;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

@ExtendWith(MockitoExtension.class)
class StandingInstructionItemReaderTest {

    private static final int ACTIVE = StandingInstructionStatus.ACTIVE.getValue();
    private static final int PAGE_SIZE = 2;

    @Mock
    private StandingInstructionReadPlatformService readService;

    private StandingInstructionItemReader reader;

    @BeforeEach
    void setUp() {
        reader = new StandingInstructionItemReader(PAGE_SIZE, readService);
        final StepExecution stepExecution = new StepExecution(ExecuteStandingInstructionsConstant.WORKER_STEP, new JobExecution(1L));
        stepExecution.getExecutionContext().putLong(ExecuteStandingInstructionsConstant.MIN_ACCOUNT_KEY, 100L);
        stepExecution.getExecutionContext().putLong(ExecuteStandingInstructionsConstant.MAX_ACCOUNT_KEY, 199L);
        reader.beforeStep(stepExecution);
    }

    /**
     * The second page must resume from the last item of the first, not from an offset: executing an instruction takes
     * it out of the due set, so an offset would skip as many instructions as the previous page committed.
     */
    @Test
    void eachPageResumesFromTheLastInstructionOfThePreviousOne() {
        final StandingInstructionData first = dueFixedSavingsToSavings(1L, StandingInstructionPriority.URGENT);
        final StandingInstructionData second = dueFixedSavingsToSavings(2L, StandingInstructionPriority.URGENT);
        final StandingInstructionData third = dueFixedSavingsToSavings(3L, StandingInstructionPriority.LOW);
        when(readService.retrieveDuePage(eq(ACTIVE), eq(100L), eq(199L), isNull(), isNull(), eq(PAGE_SIZE)))
                .thenReturn(List.of(first, second));
        when(readService.retrieveDuePage(eq(ACTIVE), eq(100L), eq(199L), eq(StandingInstructionPriority.URGENT.getValue()), eq(2L),
                eq(PAGE_SIZE))).thenReturn(List.of(third));

        assertThat(reader.read()).isSameAs(first);
        assertThat(reader.read()).isSameAs(second);
        assertThat(reader.read()).isSameAs(third);
        assertThat(reader.read()).isNull();
    }

    /** A short page means the partition is done; the reader must not ask for another one. */
    @Test
    void aShortPageEndsThePartitionWithoutAFurtherQuery() {
        final StandingInstructionData only = dueFixedSavingsToSavings(1L);
        when(readService.retrieveDuePage(eq(ACTIVE), eq(100L), eq(199L), isNull(), isNull(), eq(PAGE_SIZE))).thenReturn(List.of(only));

        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNull();
        assertThat(reader.read()).isNull();

        verify(readService).retrieveDuePage(eq(ACTIVE), eq(100L), eq(199L), isNull(), isNull(), eq(PAGE_SIZE));
    }

    @Test
    void anEmptyPartitionReadsNothing() {
        when(readService.retrieveDuePage(eq(ACTIVE), eq(100L), eq(199L), isNull(), isNull(), eq(PAGE_SIZE))).thenReturn(List.of());

        assertThat(reader.read()).isNull();
    }
}
