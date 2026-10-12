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

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.services.RetainedEarningDataService;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

/**
 * Spring Batch ItemReader for Retained Earning Job. Fetches trial balance data and delegates processing to the data
 * service. Compatible with RetainedEarningJobWriter.
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class RetainedEarningJobReader implements ItemReader<AccountGLJournalEntryAnnualSummaryData>, StepExecutionListener {

    private final RetainedEarningDataService retainedEarningDataService;
    private final RetainedEarningConfigurationService retainedEarningConfigurationService;

    private ListItemReader<AccountGLJournalEntryAnnualSummaryData> delegate;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Starting RetainedEarningJobReader step at {}", ThreadLocalContextUtil.getBusinessDate());
    }

    @Override
    public AccountGLJournalEntryAnnualSummaryData read() throws Exception {
        if (delegate == null) {
            initialize();
        }
        return delegate.read();
    }

    private void initialize() {
        try {
            final LocalDate currentDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.BUSINESS_DATE);
            final LocalDate lastDayOfPreviousFiscalYear = retainedEarningConfigurationService.getLastDayOfPreviousFiscalYear(currentDate);

            log.info("Retained earning job started: businessDate={}, fiscalYearEnd={}, dayOfWeek={}", currentDate,
                    lastDayOfPreviousFiscalYear, currentDate.getDayOfWeek());

            List<AccountGLJournalEntryAnnualSummaryData> rawData = retainedEarningDataService
                    .fetchTrialBalanceData(retainedEarningConfigurationService.getReportName(), lastDayOfPreviousFiscalYear);

            log.info("Fetched {} raw records from trial balance for fiscalYearEnd={}", rawData.size(), lastDayOfPreviousFiscalYear);

            final List<AccountGLJournalEntryAnnualSummaryData> processedData = retainedEarningDataService.processTrialBalanceData(rawData,
                    lastDayOfPreviousFiscalYear);

            delegate = new ListItemReader<>(processedData);
            log.info("Initialized with {} total records for fiscalYearEnd={}", processedData.size(), lastDayOfPreviousFiscalYear);

        } catch (Exception e) {
            log.error("Failed to initialize RetainedEarningJobReader", e);
            throw new RuntimeException("Error initializing reader: " + e.getMessage(), e);
        }
    }
}
