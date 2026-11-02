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

import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.JOB_SUMMARY_STEP_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.services.RetainedEarningDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;

@ExtendWith(MockitoExtension.class)
class RetainedEarningJobWriterTest {

    @Mock
    private RetainedEarningDataService retainedEarningDataService;

    @InjectMocks
    private RetainedEarningJobWriter retainedEarningJobWriter;

    @BeforeEach
    public void setup() {
        final JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters());
        retainedEarningJobWriter.beforeStep(new StepExecution(JOB_SUMMARY_STEP_NAME, jobExecution));
    }

    @Test
    public void testWriteWithValidItems() {
        List<AccountGLJournalEntryAnnualSummaryData> testItems = createTestData();
        Chunk<AccountGLJournalEntryAnnualSummaryData> chunk = new Chunk<>(testItems);

        retainedEarningJobWriter.write(chunk);

        verify(retainedEarningDataService, times(1)).insertRetainedEarningSummaryBatch(testItems);
    }

    @Test
    public void testWriteWithNullItems() {
        List<AccountGLJournalEntryAnnualSummaryData> testItems = new ArrayList<>(createTestData());
        testItems.add(null);
        Chunk<AccountGLJournalEntryAnnualSummaryData> chunk = new Chunk<>(testItems);

        retainedEarningJobWriter.write(chunk);

        verify(retainedEarningDataService, times(1))
                .insertRetainedEarningSummaryBatch(testItems.stream().filter(java.util.Objects::nonNull).toList());
    }

    @Test
    public void testWriteWithEmptyList() throws Exception {
        Chunk<AccountGLJournalEntryAnnualSummaryData> chunk = new Chunk<>(Collections.emptyList());

        retainedEarningJobWriter.write(chunk);

        verify(retainedEarningDataService, never()).insertRetainedEarningSummaryBatch(anyList());
    }

    @Test
    public void testWriteWithServiceException() {
        List<AccountGLJournalEntryAnnualSummaryData> testItems = createTestData();
        Chunk<AccountGLJournalEntryAnnualSummaryData> chunk = new Chunk<>(testItems);

        doThrow(new RuntimeException("Test exception")).when(retainedEarningDataService).insertRetainedEarningSummaryBatch(anyList());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            retainedEarningJobWriter.write(chunk);
        });
        assertEquals("Test exception", exception.getMessage());
    }

    private List<AccountGLJournalEntryAnnualSummaryData> createTestData() {
        LocalDate yearEndDate = LocalDate.of(2024, 12, 31);

        return Arrays.asList(
                AccountGLJournalEntryAnnualSummaryData.builder().glAccountCode("101").productId(123L).officeId(1L)
                        .ownerExternalId(ExternalIdFactory.produce("OWNER1")).openingBalanceAmount(BigDecimal.valueOf(1000))
                        .endingBalanceAmount(BigDecimal.valueOf(1200)).yearEndDate(yearEndDate).build(),

                AccountGLJournalEntryAnnualSummaryData.builder().glAccountCode("102").productId(123L).officeId(1L)
                        .ownerExternalId(ExternalIdFactory.produce("OWNER1")).openingBalanceAmount(BigDecimal.valueOf(500))
                        .endingBalanceAmount(BigDecimal.valueOf(600)).yearEndDate(yearEndDate).build());
    }
}
