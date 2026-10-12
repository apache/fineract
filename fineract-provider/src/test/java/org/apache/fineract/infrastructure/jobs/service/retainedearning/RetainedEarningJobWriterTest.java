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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummary;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummaryRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.services.RetainedEarningDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class RetainedEarningJobWriterTest {

    @Mock
    private RetainedEarningDataService retainedEarningDataService;

    @Mock
    private AccountGLJournalEntryAnnualSummaryRepository annualSummaryRepository;

    @Mock
    private RetainedEarningConfigurationService retainedEarningConfigurationService;

    @InjectMocks
    private RetainedEarningJobWriter retainedEarningJobWriter;

    @Mock
    private StepExecution stepExecution;

    @BeforeEach
    public void setup() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default tenant", "UTC", null));
    }

    private void setBusinessDate(LocalDate date) {
        HashMap<BusinessDateType, LocalDate> businessDateMap = new HashMap<>();
        businessDateMap.put(BusinessDateType.COB_DATE, date.minusDays(1));
        businessDateMap.put(BusinessDateType.BUSINESS_DATE, date);
        ThreadLocalContextUtil.setBusinessDates(businessDateMap);
    }

    private void setupWriteMode(LocalDate businessDate) {
        setBusinessDate(businessDate);
        LocalDate fiscalEnd = LocalDate.of(businessDate.getYear() - 1, 12, 31);
        when(retainedEarningConfigurationService.getLastDayOfPreviousFiscalYear(businessDate)).thenReturn(fiscalEnd);
        when(annualSummaryRepository.findByYearEndDate(fiscalEnd)).thenReturn(Collections.emptyList());
        retainedEarningJobWriter.beforeStep(stepExecution);
    }

    @Test
    public void testWriteWithValidItems() {
        setupWriteMode(LocalDate.of(LocalDate.now(ZoneId.systemDefault()).getYear(), 1, 1));
        List<AccountGLJournalEntryAnnualSummaryData> testItems = createTestData();
        Chunk<AccountGLJournalEntryAnnualSummaryData> chunk = new Chunk<>(testItems);

        retainedEarningJobWriter.write(chunk);

        verify(retainedEarningDataService, times(1)).insertRetainedEarningSummaryBatch(testItems);
    }

    @Test
    public void testWriteWithNullItems() {
        setupWriteMode(LocalDate.of(LocalDate.now(ZoneId.systemDefault()).getYear(), 1, 1));
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
        setupWriteMode(LocalDate.of(LocalDate.now(ZoneId.systemDefault()).getYear(), 1, 1));
        List<AccountGLJournalEntryAnnualSummaryData> testItems = createTestData();
        Chunk<AccountGLJournalEntryAnnualSummaryData> chunk = new Chunk<>(testItems);

        doThrow(new RuntimeException("Test exception")).when(retainedEarningDataService).insertRetainedEarningSummaryBatch(anyList());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            retainedEarningJobWriter.write(chunk);
        });
        assertEquals("Test exception", exception.getMessage());
    }

    @Test
    public void testWritePersistsOnNonJanFirstWhenNoEntriesExist() {
        setupWriteMode(LocalDate.of(2026, 1, 3));

        List<AccountGLJournalEntryAnnualSummaryData> testItems = createTestData();
        Chunk<AccountGLJournalEntryAnnualSummaryData> chunk = new Chunk<>(testItems);
        retainedEarningJobWriter.write(chunk);

        verify(retainedEarningDataService, times(1)).insertRetainedEarningSummaryBatch(testItems);
    }

    @Test
    public void testWriteSkipsWhenEntriesAlreadyExist() {
        LocalDate businessDate = LocalDate.of(LocalDate.now(ZoneId.systemDefault()).getYear(), 1, 1);
        setBusinessDate(businessDate);
        LocalDate fiscalEnd = LocalDate.of(businessDate.getYear() - 1, 12, 31);
        when(retainedEarningConfigurationService.getLastDayOfPreviousFiscalYear(businessDate)).thenReturn(fiscalEnd);
        when(annualSummaryRepository.findByYearEndDate(fiscalEnd)).thenReturn(List.of(new AccountGLJournalEntryAnnualSummary()));
        retainedEarningJobWriter.beforeStep(stepExecution);

        List<AccountGLJournalEntryAnnualSummaryData> testItems = createTestData();
        Chunk<AccountGLJournalEntryAnnualSummaryData> chunk = new Chunk<>(testItems);
        retainedEarningJobWriter.write(chunk);

        verify(retainedEarningDataService, never()).insertRetainedEarningSummaryBatch(anyList());
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
