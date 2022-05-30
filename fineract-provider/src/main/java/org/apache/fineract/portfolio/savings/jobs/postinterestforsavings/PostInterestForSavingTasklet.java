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
package org.apache.fineract.portfolio.savings.jobs.postinterestforsavings;

import static org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType.ACTIVE;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsSchedularInterestPoster;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
@Slf4j
@Component
public class PostInterestForSavingTasklet implements Tasklet {

    private final SavingsAccountReadPlatformService savingAccountReadPlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final SavingsAccountAssembler savingAccountAssembler;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final Queue<List<SavingsAccountData>> queue = new ArrayDeque<>();
    private final ApplicationContext applicationContext;
    private final int queueSize = 1;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final int threadPoolSize = Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("thread-pool-size"));
        final int batchSize = Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("batch-size"));
        final int pageSize = batchSize * threadPoolSize;
        Long maxSavingsIdInList = 0L;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        final boolean backdatedTxnsAllowedTill = false;

        long start = System.currentTimeMillis();

        log.info("Reading Savings Account Data!");
        List<SavingsAccountData> savingsAccounts = savingAccountReadPlatformService
                .retrieveAllSavingsDataForInterestPosting(backdatedTxnsAllowedTill, pageSize, ACTIVE.getValue(), maxSavingsIdInList);

        if (savingsAccounts != null && savingsAccounts.size() > 0) {
            savingsAccounts = Collections.synchronizedList(savingsAccounts);
            long finish = System.currentTimeMillis();
            log.info("Done fetching Data within {} milliseconds", finish - start);
            queue.add(savingsAccounts);

            if (!CollectionUtils.isEmpty(queue)) {
                do {
                    int totalFilteredRecords = savingsAccounts.size();
                    log.info("Starting Interest posting - total records - {}", totalFilteredRecords);
                    List<SavingsAccountData> queueElement = queue.element();
                    maxSavingsIdInList = queueElement.get(queueElement.size() - 1).getId();
                    postInterest(queue.remove(), threadPoolSize, executorService, backdatedTxnsAllowedTill, pageSize, maxSavingsIdInList);
                } while (!CollectionUtils.isEmpty(queue));
            }
            executorService.shutdownNow();
        }
        return RepeatStatus.FINISHED;
    }

    private void postInterest(List<SavingsAccountData> savingsAccounts, int threadPoolSize, ExecutorService executorService,
            final boolean backdatedTxnsAllowedTill, final int pageSize, Long maxSavingsIdInList) {
        List<Callable<Void>> posters = new ArrayList<>();
        int fromIndex = 0;
        int size = savingsAccounts.size();
        int batchSize = (int) Math.ceil((double) size / threadPoolSize);

        if (batchSize == 0) {
            return;
        }

        int toIndex = (batchSize > size - 1) ? size : batchSize;
        while (toIndex < size && savingsAccounts.get(toIndex - 1).getId().equals(savingsAccounts.get(toIndex).getId())) {
            toIndex++;
        }
        boolean lastBatch = false;
        int loopCount = size / batchSize + 1;

        FineractContext context = ThreadLocalContextUtil.getContext();

        Callable<Void> fetchData = () -> {
            ThreadLocalContextUtil.init(context);
            Long maxId = maxSavingsIdInList;
            if (!queue.isEmpty()) {
                maxId = Math.max(maxSavingsIdInList, queue.element().get(queue.element().size() - 1).getId());
            }

            while (queue.size() <= queueSize) {
                log.info("Fetching while threads are running!");
                List<SavingsAccountData> savingsAccountDataList = Collections.synchronizedList(this.savingAccountReadPlatformService
                        .retrieveAllSavingsDataForInterestPosting(backdatedTxnsAllowedTill, pageSize, ACTIVE.getValue(), maxId));
                if (savingsAccountDataList.isEmpty()) {
                    break;
                }
                maxId = savingsAccountDataList.get(savingsAccountDataList.size() - 1).getId();
                queue.add(savingsAccountDataList);
            }
            return null;
        };
        posters.add(fetchData);

        for (long i = 0; i < loopCount; i++) {
            List<SavingsAccountData> subList = safeSubList(savingsAccounts, fromIndex, toIndex);
            SavingsSchedularInterestPoster savingsSchedularInterestPoster = (SavingsSchedularInterestPoster) applicationContext
                    .getBean("savingsSchedularInterestPoster");
            savingsSchedularInterestPoster.setSavingAccounts(subList);
            savingsSchedularInterestPoster.setContext(ThreadLocalContextUtil.getContext());
            savingsSchedularInterestPoster.setSavingsAccountWritePlatformService(savingsAccountWritePlatformService);
            savingsSchedularInterestPoster.setSavingsAccountReadPlatformService(savingAccountReadPlatformService);
            savingsSchedularInterestPoster.setSavingsAccountRepository(savingsAccountRepository);
            savingsSchedularInterestPoster.setSavingAccountAssembler(savingAccountAssembler);
            savingsSchedularInterestPoster.setJdbcTemplate(jdbcTemplate);
            savingsSchedularInterestPoster.setBackdatedTxnsAllowedTill(backdatedTxnsAllowedTill);
            savingsSchedularInterestPoster.setTransactionTemplate(transactionTemplate);
            savingsSchedularInterestPoster.setConfigurationDomainService(configurationDomainService);

            posters.add(savingsSchedularInterestPoster);

            if (lastBatch) {
                break;
            }
            if (toIndex + batchSize > size - 1) {
                lastBatch = true;
            }
            fromIndex = fromIndex + (toIndex - fromIndex);
            toIndex = (toIndex + batchSize > size - 1) ? size : toIndex + batchSize;
            while (toIndex < size && savingsAccounts.get(toIndex - 1).getId().equals(savingsAccounts.get(toIndex).getId())) {
                toIndex++;
            }
        }

        try {
            List<Future<Void>> responses = executorService.invokeAll(posters);
            Long maxId = maxSavingsIdInList;
            if (!queue.isEmpty()) {
                maxId = Math.max(maxSavingsIdInList, queue.element().get(queue.element().size() - 1).getId());
            }

            while (queue.size() <= queueSize) {
                log.info("Fetching while threads are running!..:: this is not supposed to run........");
                savingsAccounts = Collections.synchronizedList(this.savingAccountReadPlatformService
                        .retrieveAllSavingsDataForInterestPosting(backdatedTxnsAllowedTill, pageSize, ACTIVE.getValue(), maxId));
                if (savingsAccounts.isEmpty()) {
                    break;
                }
                maxId = savingsAccounts.get(savingsAccounts.size() - 1).getId();
                log.info("Add to the Queue");
                queue.add(savingsAccounts);
            }

            checkCompletion(responses);
            log.info("Queue size {}", queue.size());
        } catch (InterruptedException e1) {
            log.error("Interrupted while postInterest", e1);
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
            boolean allThreadsExecuted;
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
            log.error("Interrupted while interest posting entries", e1);
        } catch (ExecutionException e2) {
            log.error("Execution exception while interest posting entries", e2);
        }
    }
}
