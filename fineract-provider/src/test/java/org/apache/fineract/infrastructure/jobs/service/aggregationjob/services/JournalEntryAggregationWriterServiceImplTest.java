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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationSummaryData;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationTrackingData;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain.JournalEntryAggregationTrackingRepository;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain.JournalEntrySummary;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain.JournalEntrySummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class JournalEntryAggregationWriterServiceImplTest {

    @InjectMocks
    private JournalEntryAggregationWriterServiceImpl writerService;

    @Mock
    private JournalEntrySummaryRepository journalSummaryRepository;

    @Mock
    private JournalEntryAggregationTrackingRepository journalEntryAggregationTrackingRepository;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Test
    public void testInsertJournalEntryTracking() throws Exception {
        // Arrange
        JournalEntryAggregationTrackingData trackingData = mock(JournalEntryAggregationTrackingData.class);

        // Act
        writerService.insertJournalEntryTracking(trackingData);

        // Assert
        verify(journalEntryAggregationTrackingRepository, times(1)).save(any());
    }

    @Test
    public void testRollbackJournalEntrySummary() {
        // Arrange
        Long jobExecutionId = 123L;
        // Act
        writerService.rollbackJournalEntrySummary(jobExecutionId);
        // Assert
        verify(journalSummaryRepository, times(1)).deleteByJobExecutionId(jobExecutionId);
    }

    @Test
    public void testRollbackJournalEntryTracking() {
        // Arrange
        final Long jobExecutionId = 123L;
        // Act
        writerService.rollbackJournalEntryTracking(jobExecutionId);
        // Assert
        verify(journalEntryAggregationTrackingRepository, times(1)).deleteByJobExecutionId(jobExecutionId);
    }

    @Test
    public void testInsertJournalEntrySummaryBatch() {
        // Arrange
        JournalEntryAggregationSummaryData summaryData1 = mock(JournalEntryAggregationSummaryData.class);
        JournalEntryAggregationSummaryData summaryData2 = mock(JournalEntryAggregationSummaryData.class);

        when(summaryData1.getAggregatedOnDate()).thenReturn(LocalDate.now(Clock.systemUTC()));
        when(summaryData2.getAggregatedOnDate()).thenReturn(LocalDate.now(Clock.systemUTC()));
        when(summaryData1.getJobExecutionId()).thenReturn(123L);
        when(summaryData2.getJobExecutionId()).thenReturn(123L);

        List<JournalEntryAggregationSummaryData> summariesList = Arrays.asList(summaryData1, summaryData2);

        // Act
        writerService.insertJournalEntrySummaryBatch(summariesList);

        // Assert
        verify(journalSummaryRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testInsertJournalEntrySummaryBatchEmpty() {
        // Arrange
        List<JournalEntryAggregationSummaryData> emptySummariesList = List.of();

        // Act
        writerService.insertJournalEntrySummaryBatch(emptySummariesList);

        // Assert
        verify(journalSummaryRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testInsertJournalEntrySummaryBatchWithAmounts() {
        // Arrange
        JournalEntryAggregationSummaryData summaryData = mock(JournalEntryAggregationSummaryData.class);
        BigDecimal debitAmount = BigDecimal.valueOf(100.50);
        BigDecimal creditAmount = BigDecimal.valueOf(200.75);

        when(summaryData.getAggregatedOnDate()).thenReturn(LocalDate.now(Clock.systemUTC()));
        when(summaryData.getDebitAmount()).thenReturn(debitAmount);
        when(summaryData.getCreditAmount()).thenReturn(creditAmount);
        when(summaryData.getJobExecutionId()).thenReturn(123L);

        List<JournalEntryAggregationSummaryData> summariesList = List.of(summaryData);

        // Act
        writerService.insertJournalEntrySummaryBatch(summariesList);

        // Assert
        verify(journalSummaryRepository, times(1)).saveAll(argThat(entities -> {
            if (!(entities instanceof List) || ((List<?>) entities).isEmpty()) {
                return false;
            }
            JournalEntrySummary entity = (JournalEntrySummary) ((List<?>) entities).getFirst();
            return entity.getDebitAmount().compareTo(debitAmount) == 0 && entity.getCreditAmount().compareTo(creditAmount) == 0;
        }));
        verify(summaryData, times(1)).getDebitAmount();
        verify(summaryData, times(1)).getCreditAmount();
    }
}
