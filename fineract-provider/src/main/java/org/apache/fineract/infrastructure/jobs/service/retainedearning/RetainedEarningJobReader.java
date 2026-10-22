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

import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.FISCAL_YEAR_END_DATE_CONTEXT_KEY;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.services.RetainedEarningDataService;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
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

    private StepExecution stepExecution;
    private ListItemReader<AccountGLJournalEntryAnnualSummaryData> delegate;

    @Override
    public void beforeStep(final StepExecution stepExecution) {
        log.info("Starting RetainedEarningJobReader step for jobExecutionId={}", stepExecution.getJobExecution().getId());
        this.stepExecution = stepExecution;
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
            final LocalDate fiscalYearEnd = stepExecution.getJobExecution().getExecutionContext().get(FISCAL_YEAR_END_DATE_CONTEXT_KEY,
                    LocalDate.class);

            log.info("Retained earning job reader initializing for fiscalYearEnd={}", fiscalYearEnd);

            final List<AccountGLJournalEntryAnnualSummaryData> processedData = new ArrayList<>();
            processedData
                    .addAll(fetchAndProcess(retainedEarningConfigurationService.getReportName(), PortfolioProductType.LOAN, fiscalYearEnd));
            processedData.addAll(fetchAndProcess(retainedEarningConfigurationService.getWorkingCapitalReportName(),
                    PortfolioProductType.WORKING_CAPITAL_LOAN, fiscalYearEnd));

            delegate = new ListItemReader<>(processedData);
            log.info("Initialized with {} total records for fiscalYearEnd={}", processedData.size(), fiscalYearEnd);

        } catch (Exception e) {
            log.error("Failed to initialize RetainedEarningJobReader for jobExecutionId={}", stepExecution.getJobExecution().getId(), e);
            throw new RuntimeException("Error initializing reader: " + e.getMessage(), e);
        }
    }

    private List<AccountGLJournalEntryAnnualSummaryData> fetchAndProcess(final String reportName, final PortfolioProductType productType,
            final LocalDate lastDayOfPreviousFiscalYear) {
        final List<AccountGLJournalEntryAnnualSummaryData> rawData = retainedEarningDataService.fetchTrialBalanceData(reportName,
                lastDayOfPreviousFiscalYear);

        log.info("Fetched {} raw records from trial balance report '{}' for fiscalYearEnd={}", rawData.size(), reportName,
                lastDayOfPreviousFiscalYear);

        return retainedEarningDataService.processTrialBalanceData(rawData, lastDayOfPreviousFiscalYear, productType);
    }
}
