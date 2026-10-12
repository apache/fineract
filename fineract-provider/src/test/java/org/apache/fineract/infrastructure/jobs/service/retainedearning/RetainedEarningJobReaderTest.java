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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.services.RetainedEarningDataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepExecution;

@ExtendWith(MockitoExtension.class)
class RetainedEarningJobReaderTest {

    @Mock
    private RetainedEarningDataService retainedEarningDataService;

    @Mock
    private RetainedEarningConfigurationService retainedEarningConfigurationService;

    @InjectMocks
    private RetainedEarningJobReader retainedEarningJobReader;

    @Mock
    private StepExecution stepExecution;

    private final LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
    private final LocalDate lastDayOfPreviousFiscalYear = LocalDate.of(businessDate.getYear() - 1, 12, 31);

    @BeforeEach
    public void setup() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default tenant", "UTC", null));
        HashMap<BusinessDateType, LocalDate> businessDateMap = new HashMap<>();
        businessDateMap.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        businessDateMap.put(BusinessDateType.BUSINESS_DATE, businessDate);
        ThreadLocalContextUtil.setBusinessDates(businessDateMap);

        when(retainedEarningConfigurationService.getLastDayOfPreviousFiscalYear(businessDate)).thenReturn(lastDayOfPreviousFiscalYear);
        when(retainedEarningConfigurationService.getReportName()).thenReturn("test-report");
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testReadWithEmptyData() throws Exception {
        when(retainedEarningDataService.fetchTrialBalanceData(any(), any())).thenReturn(Collections.emptyList());
        when(retainedEarningDataService.processTrialBalanceData(anyList(), eq(lastDayOfPreviousFiscalYear)))
                .thenReturn(Collections.emptyList());

        retainedEarningJobReader.beforeStep(stepExecution);
        AccountGLJournalEntryAnnualSummaryData result = retainedEarningJobReader.read();

        assertNull(result, "Result should be null with empty data");
        verify(retainedEarningDataService).fetchTrialBalanceData(any(), any());
        verify(retainedEarningDataService).processTrialBalanceData(anyList(), eq(lastDayOfPreviousFiscalYear));
    }

    @Test
    public void testReadDelegatesToProcessTrialBalanceData() throws Exception {
        List<AccountGLJournalEntryAnnualSummaryData> rawData = List.of(AccountGLJournalEntryAnnualSummaryData.builder()
                .glAccountCode("401001").productName("Test Product").officeId(1L).ownerExternalId(ExternalIdFactory.produce("OWNER1"))
                .endingBalanceAmount(BigDecimal.valueOf(1200)).yearEndDate(lastDayOfPreviousFiscalYear).build());

        List<AccountGLJournalEntryAnnualSummaryData> processedData = List.of(
                AccountGLJournalEntryAnnualSummaryData.builder().glAccountCode("401001").productId(123L).officeId(1L)
                        .ownerExternalId(ExternalIdFactory.produce("OWNER1")).endingBalanceAmount(BigDecimal.valueOf(1200))
                        .currencyCode("USD").yearEndDate(lastDayOfPreviousFiscalYear).build(),
                AccountGLJournalEntryAnnualSummaryData.builder().glAccountCode("320000").productId(123L).officeId(1L)
                        .ownerExternalId(ExternalIdFactory.produce("OWNER1")).openingBalanceAmount(BigDecimal.valueOf(1200))
                        .currencyCode("USD").yearEndDate(lastDayOfPreviousFiscalYear).build());

        when(retainedEarningDataService.fetchTrialBalanceData(any(), any())).thenReturn(rawData);
        when(retainedEarningDataService.processTrialBalanceData(eq(rawData), eq(lastDayOfPreviousFiscalYear))).thenReturn(processedData);

        retainedEarningJobReader.beforeStep(stepExecution);

        int readCount = 0;
        AccountGLJournalEntryAnnualSummaryData result;
        while ((result = retainedEarningJobReader.read()) != null) {
            readCount++;
            assertNotNull(result);
        }

        assertEquals(2, readCount, "Should read all processed records");
        verify(retainedEarningDataService).processTrialBalanceData(eq(rawData), eq(lastDayOfPreviousFiscalYear));
    }

    @Test
    public void testInitializeWithDataProcessingErrors() throws Exception {
        when(retainedEarningDataService.fetchTrialBalanceData(any(), any())).thenThrow(new RuntimeException("Test exception"));

        retainedEarningJobReader.beforeStep(stepExecution);

        try {
            retainedEarningJobReader.read();
            assertTrue(false, "Should have thrown an exception");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Test exception"));
        }

        verify(retainedEarningDataService).fetchTrialBalanceData(any(), any());
    }
}
