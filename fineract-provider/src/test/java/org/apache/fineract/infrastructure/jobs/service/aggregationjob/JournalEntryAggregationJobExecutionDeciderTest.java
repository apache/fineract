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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
public class JournalEntryAggregationJobExecutionDeciderTest {

    final LocalDate aggregatedOnDate = LocalDate.now(ZoneId.of("UTC"));
    @InjectMocks
    private JournalEntryAggregationJobExecutionDecider decider;
    @Mock
    private JobExecution jobExecution;
    @Mock
    private StepExecution stepExecution;
    @Mock
    private ExecutionContext executionContext;

    @Mock
    private FineractProperties.FineractJobProperties fineractJobProperties;

    @BeforeEach
    public void setUp() {
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        when(jobExecution.getExecutionContext().get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE)).thenReturn(aggregatedOnDate);
    }

    @Test
    public void testDecideContinueExecution() {
        // Arrange
        LocalDate lastAggregatedOnDate = aggregatedOnDate.minusDays(1);
        when(jobExecution.getExecutionContext().get(JournalEntryAggregationJobConstant.LAST_AGGREGATED_ON_DATE))
                .thenReturn(lastAggregatedOnDate);

        // Act
        FlowExecutionStatus status = decider.decide(jobExecution, stepExecution);

        // Assert
        assertEquals(JournalEntryAggregationJobConstant.CONTINUE_JOB_EXECUTION, status.getName(),
                "Should continue execution when lastAggregatedOnDate is before aggregatedOnDate");
    }

    @ParameterizedTest
    @MethodSource("provideNoOpExecutionScenarios")
    public void testDecideNoOpExecution(LocalDate lastAggregatedOnDate, String scenarioDescription) {
        // Arrange
        when(jobExecution.getExecutionContext().get(JournalEntryAggregationJobConstant.LAST_AGGREGATED_ON_DATE))
                .thenReturn(lastAggregatedOnDate);
        when(jobExecution.getExitStatus()).thenReturn(ExitStatus.NOOP);

        // Act
        FlowExecutionStatus status = decider.decide(jobExecution, stepExecution);

        // Assert
        assertEquals(JournalEntryAggregationJobConstant.NO_OP_EXECUTION, status.getName(),
                "Should perform no-op execution when " + scenarioDescription);
        assertEquals(ExitStatus.NOOP, jobExecution.getExitStatus(),
                "Exit status should remain NOOP for scenario: " + scenarioDescription);
    }

    private static Stream<Arguments> provideNoOpExecutionScenarios() {
        LocalDate aggregatedOnDate = LocalDate.now(Clock.systemUTC());
        return Stream.of(Arguments.of(aggregatedOnDate, "lastAggregatedOnDate equals aggregatedOnDate"),
                Arguments.of(aggregatedOnDate.plusDays(1), "lastAggregatedOnDate is after aggregatedOnDate"));
    }

    @Test
    public void testDecideFirstTimeExecution() {
        // Arrange
        when(jobExecution.getExecutionContext().get(JournalEntryAggregationJobConstant.LAST_AGGREGATED_ON_DATE))
                .thenReturn(null);

        // Act
        FlowExecutionStatus status = decider.decide(jobExecution, stepExecution);

        // Assert
        assertEquals(JournalEntryAggregationJobConstant.CONTINUE_JOB_EXECUTION, status.getName(),
                "Should continue execution when no previous execution date exists (first time execution)");
    }

    @Test
    public void testDecideContinueExecutionWithMultipleDaysDifference() {
        // Arrange
        LocalDate lastAggregatedOnDate = aggregatedOnDate.minusDays(5);
        when(jobExecution.getExecutionContext().get(JournalEntryAggregationJobConstant.LAST_AGGREGATED_ON_DATE))
                .thenReturn(lastAggregatedOnDate);

        // Act
        FlowExecutionStatus status = decider.decide(jobExecution, stepExecution);

        // Assert
        assertEquals(JournalEntryAggregationJobConstant.CONTINUE_JOB_EXECUTION, status.getName(),
                "Should continue execution when lastAggregatedOnDate is multiple days before aggregatedOnDate");
    }
}
