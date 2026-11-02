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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.services.RetainedEarningDataService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retained earning summary item writer. Persists the given items; whether the step runs at all is decided upfront by
 * {@link RetainedEarningJobExecutionDecider}.
 */
@Component
@StepScope
@Slf4j
@RequiredArgsConstructor
public class RetainedEarningJobWriter implements ItemWriter<AccountGLJournalEntryAnnualSummaryData>, StepExecutionListener {

    private final RetainedEarningDataService retainedEarningDataService;

    private StepExecution stepExecution;

    @Override
    public void beforeStep(final StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    @Transactional
    public void write(@NonNull Chunk<? extends AccountGLJournalEntryAnnualSummaryData> retainedEarningSummaries) {
        final Long jobExecutionId = stepExecution.getJobExecution().getId();
        List<AccountGLJournalEntryAnnualSummaryData> validSummaries = retainedEarningSummaries.getItems().stream().filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (validSummaries.isEmpty()) {
            log.info("No valid retained earning entries to write for jobExecutionId={}", jobExecutionId);
            return;
        }

        retainedEarningDataService.insertRetainedEarningSummaryBatch(validSummaries);
        log.info("Year-end processing: persisted {} retained earning records for jobExecutionId={}.", validSummaries.size(),
                jobExecutionId);
    }
}
