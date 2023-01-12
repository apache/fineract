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
package org.apache.fineract.portfolio.loanaccount.jobs.recalculateinterestforloan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.fineract.infrastructure.event.business.annotation.BulkEventSupport;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.exception.OfficeNotFoundException;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.RecalculateInterestPoster;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
@BulkEventSupport
public class RecalculateInterestForLoanTasklet implements Tasklet {

    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanWritePlatformService loanWritePlatformService;
    private final RecalculateInterestPoster recalculateInterestPoster;
    private final OfficeReadPlatformService officeReadPlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Map<String, JobParameter> jobParameters = chunkContext.getStepContext().getStepExecution().getJobParameters().getParameters();
        if (!jobParameters.isEmpty()) {
            final String officeId = (String) jobParameters.get("officeId").getValue();
            log.debug("recalculateInterest: officeId={}", officeId);
            Long officeIdLong = Long.valueOf(officeId);

            final OfficeData office = officeReadPlatformService.retrieveOffice(officeIdLong);
            if (office == null) {
                throw new OfficeNotFoundException(officeIdLong);
            }
            final int threadPoolSize = Integer.parseInt((String) jobParameters.get("thread-pool-size").getValue());
            final int batchSize = Integer.parseInt((String) jobParameters.get("batch-size").getValue());

            recalculateInterest(office, threadPoolSize, batchSize);
        } else {
            Collection<Long> loanIds = loanReadPlatformService.fetchLoansForInterestRecalculation();
            if (!loanIds.isEmpty()) {
                List<Throwable> errors = new ArrayList<>();
                for (Long loanId : loanIds) {
                    log.debug("recalculateInterest: Loan ID = {}", loanId);
                    try {
                        loanWritePlatformService.recalculateInterest(loanId);
                    } catch (Exception e) {
                        errors.add(e);
                    }
                }
                if (!errors.isEmpty()) {
                    throw new JobExecutionException(errors);
                }
            }
        }
        return RepeatStatus.FINISHED;
    }

    private void recalculateInterest(OfficeData office, int threadPoolSize, int batchSize) {
        final int pageSize = batchSize * threadPoolSize;

        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        Long maxLoanIdInList = 0L;
        final String officeHierarchy = office.getHierarchy() + "%";

        List<Long> loanIds = Collections.synchronizedList(
                this.loanReadPlatformService.fetchLoansForInterestRecalculation(pageSize, maxLoanIdInList, officeHierarchy));

        do {
            int totalFilteredRecords = loanIds.size();
            log.debug("Starting accrual - total filtered records - {}", totalFilteredRecords);
            recalculateInterest(loanIds, threadPoolSize, batchSize, executorService);
            maxLoanIdInList += pageSize + 1;
            loanIds = Collections.synchronizedList(
                    this.loanReadPlatformService.fetchLoansForInterestRecalculation(pageSize, maxLoanIdInList, officeHierarchy));
        } while (!CollectionUtils.isEmpty(loanIds));

        executorService.shutdownNow();
    }

    private void recalculateInterest(List<Long> loanIds, int threadPoolSize, int batchSize, final ExecutorService executorService) {

        List<Callable<Void>> posters = new ArrayList<>();
        int fromIndex = 0;
        int size = loanIds.size();
        double toGetCeilValue = size / ((double) threadPoolSize);
        batchSize = (int) Math.ceil(toGetCeilValue);

        if (batchSize == 0) {
            return;
        }

        int toIndex = (batchSize > size - 1) ? size : batchSize;
        while (toIndex < size && loanIds.get(toIndex - 1).equals(loanIds.get(toIndex))) {
            toIndex++;
        }
        boolean lastBatch = false;
        int loopCount = size / batchSize + 1;

        for (long i = 0; i < loopCount; i++) {
            List<Long> subList = safeSubList(loanIds, fromIndex, toIndex);
            recalculateInterestPoster.setLoanIds(subList);
            recalculateInterestPoster.setLoanWritePlatformService(loanWritePlatformService);
            posters.add(recalculateInterestPoster);
            if (lastBatch) {
                break;
            }
            if (toIndex + batchSize > size - 1) {
                lastBatch = true;
            }
            fromIndex = fromIndex + (toIndex - fromIndex);
            toIndex = (toIndex + batchSize > size - 1) ? size : toIndex + batchSize;
            while (toIndex < size && loanIds.get(toIndex - 1).equals(loanIds.get(toIndex))) {
                toIndex++;
            }
        }

        try {
            List<Future<Void>> responses = executorService.invokeAll(posters);
            checkCompletion(responses);
        } catch (InterruptedException e1) {
            log.error("Interrupted while recalculateInterest", e1);
        }
    }

    private <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
        int size = list.size();
        if (fromIndex >= size || toIndex <= 0 || fromIndex >= toIndex) {
            return Collections.emptyList();
        }

        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.min(size, toIndex);

        return list.subList(fromIndex, toIndex);
    }

    private void checkCompletion(List<Future<Void>> responses) {
        try {
            for (Future<Void> f : responses) {
                f.get();
            }
            boolean allThreadsExecuted = false;
            int noOfThreadsExecuted = 0;
            for (Future<Void> future : responses) {
                if (future.isDone()) {
                    noOfThreadsExecuted++;
                }
            }
            allThreadsExecuted = noOfThreadsExecuted == responses.size();
            if (!allThreadsExecuted) {
                log.error("All threads could not execute.");
            }
        } catch (InterruptedException e1) {
            log.error("Interrupted while posting IR entries", e1);
        } catch (ExecutionException e2) {
            log.error("Execution exception while posting IR entries", e2);
        }
    }
}
