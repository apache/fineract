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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationSummaryData;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.services.JournalEntryAggregationWriterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
public class JournalEntryAggregationJobWriterTest {

    @InjectMocks
    private JournalEntryAggregationJobWriter writer;

    @Mock
    private JournalEntryAggregationWriterService writerService;

    @Test
    public void testWrite() {
        // Arrange
        JournalEntryAggregationSummaryData summaryData1 = mock(JournalEntryAggregationSummaryData.class);
        JournalEntryAggregationSummaryData summaryData2 = mock(JournalEntryAggregationSummaryData.class);
        Chunk<JournalEntryAggregationSummaryData> chunk = new Chunk<>(List.of(summaryData1, summaryData2));

        StepExecution stepExecution = mock(StepExecution.class);
        JobExecution jobExecution = mock(JobExecution.class);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(123L);

        writer.beforeStep(stepExecution);

        // Act
        writer.write(chunk);

        // Assert
        verify(writerService, times(1)).insertJournalEntrySummaryBatch(anyList());
        verify(summaryData1, times(1)).setJobExecutionId(123L);
        verify(summaryData2, times(1)).setJobExecutionId(123L);
    }

    @Test
    public void testWriteEmptyChunk() {
        // Arrange
        Chunk<JournalEntryAggregationSummaryData> emptyChunk = new Chunk<>();

        StepExecution stepExecution = mock(StepExecution.class);
        JobExecution jobExecution = mock(JobExecution.class);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(123L);

        writer.beforeStep(stepExecution);

        // Act
        writer.write(emptyChunk);

        // Assert
        verify(writerService, times(1)).insertJournalEntrySummaryBatch(anyList());
    }

    @Test
    public void testWriteSingleItem() {
        // Arrange
        JournalEntryAggregationSummaryData summaryData = mock(JournalEntryAggregationSummaryData.class);
        Chunk<JournalEntryAggregationSummaryData> chunk = new Chunk<>(List.of(summaryData));

        StepExecution stepExecution = mock(StepExecution.class);
        JobExecution jobExecution = mock(JobExecution.class);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(456L);

        writer.beforeStep(stepExecution);

        // Act
        writer.write(chunk);

        // Assert
        verify(writerService, times(1)).insertJournalEntrySummaryBatch(anyList());
        verify(summaryData, times(1)).setJobExecutionId(456L);
    }
}
