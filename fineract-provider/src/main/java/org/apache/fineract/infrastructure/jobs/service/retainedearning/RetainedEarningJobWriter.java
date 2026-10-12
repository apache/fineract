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
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummaryRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.services.RetainedEarningDataService;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retained earning summary item writer. The Reader always runs the full pipeline and logs validation stats daily. This
 * Writer persists to the database only if entries do not already exist for the fiscal year end date (idempotency
 * guard). This allows the job to be safely rerun on any day if the initial run fails.
 */
@Component
@StepScope
@Slf4j
@RequiredArgsConstructor
public class RetainedEarningJobWriter implements ItemWriter<AccountGLJournalEntryAnnualSummaryData>, StepExecutionListener {

    private final RetainedEarningDataService retainedEarningDataService;
    private final AccountGLJournalEntryAnnualSummaryRepository annualSummaryRepository;
    private final RetainedEarningConfigurationService retainedEarningConfigurationService;

    private boolean shouldWrite;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        final LocalDate currentDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.BUSINESS_DATE);
        final LocalDate lastDayOfPreviousFiscalYear = retainedEarningConfigurationService.getLastDayOfPreviousFiscalYear(currentDate);
        final boolean entriesExist = !annualSummaryRepository.findByYearEndDate(lastDayOfPreviousFiscalYear).isEmpty();

        if (entriesExist) {
            shouldWrite = false;
            log.info("Retained earning Writer: entries already exist for yearEndDate={}. Will run as dry run.",
                    lastDayOfPreviousFiscalYear);
        } else {
            shouldWrite = true;
            log.info("Retained earning Writer: no existing entries for yearEndDate={}. Will persist records.", lastDayOfPreviousFiscalYear);
        }
    }

    @Override
    @Transactional
    public void write(@NonNull Chunk<? extends AccountGLJournalEntryAnnualSummaryData> retainedEarningSummaries) {
        List<AccountGLJournalEntryAnnualSummaryData> validSummaries = retainedEarningSummaries.getItems().stream().filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (validSummaries.isEmpty()) {
            log.info("No valid retained earning entries to write");
            return;
        }

        if (!shouldWrite) {
            log.info("Dry run complete: data pipeline validated successfully, recordsProcessed={}, no records written.",
                    validSummaries.size());
            return;
        }

        retainedEarningDataService.insertRetainedEarningSummaryBatch(validSummaries);
        log.info("Year-end processing: persisted {} retained earning records.", validSummaries.size());
    }
}
