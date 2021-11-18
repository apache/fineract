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
package org.apache.fineract.portfolio.savings.service;

import static org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType.ACTIVE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SavingsSchedularServiceImpl implements SavingsSchedularService {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsSchedularServiceImpl.class);

    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final SavingsAccountReadPlatformService savingAccountReadPlatformService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final ApplicationContext applicationContext;

    @Autowired
    public SavingsSchedularServiceImpl(final SavingsAccountAssembler savingAccountAssembler,
            final SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            final SavingsAccountReadPlatformService savingAccountReadPlatformService,
            final SavingsAccountRepositoryWrapper savingsAccountRepository, final ApplicationContext applicationContext) {
        this.savingAccountAssembler = savingAccountAssembler;
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
        this.savingAccountReadPlatformService = savingAccountReadPlatformService;
        this.savingsAccountRepository = savingsAccountRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    @CronTarget(jobName = JobName.POST_INTEREST_FOR_SAVINGS)
    public void postInterestForAccounts(Map<String, String> jobParameters) throws JobExecutionException {

        final int threadPoolSize = Integer.parseInt(jobParameters.get("thread-pool-size"));
        final int batchSize = Integer.parseInt(jobParameters.get("batch-size"));
        final int pageSize = batchSize * threadPoolSize;
        Long maxSavingsIdInList = 0L;
        // initialise the executor service with fetched configurations
        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        List<Long> savingsIds = Collections.synchronizedList(
                this.savingAccountReadPlatformService.getAccountsIdsByStatusPaged(ACTIVE.getValue(), pageSize, maxSavingsIdInList));
        if (!CollectionUtils.isEmpty(savingsIds)) {
            do {
                int totalFilteredRecords = savingsIds.size();
                LOG.info("Starting Interest posting - total filtered records - {}", totalFilteredRecords);
                postInterest(savingsIds, threadPoolSize, batchSize, executorService);
                maxSavingsIdInList = savingsIds.get(savingsIds.size() - 1);
                savingsIds = Collections.synchronizedList(
                        this.savingAccountReadPlatformService.getAccountsIdsByStatusPaged(ACTIVE.getValue(), pageSize, maxSavingsIdInList));
            } while (!CollectionUtils.isEmpty(savingsIds));
        }
        // shutdown the executor when done
        executorService.shutdownNow();
    }

    private void postInterest(List<Long> savingsIds, int threadPoolSize, int batchSize, ExecutorService executorService) {
        List<Callable<Void>> posters = new ArrayList<>();
        int fromIndex = 0;
        // get the size of current paginated dataset
        int size = savingsIds.size();
        // calculate the batch size
        batchSize = (int) Math.ceil((double) size / threadPoolSize);

        if (batchSize == 0) {
            return;
        }

        int toIndex = (batchSize > size - 1) ? size : batchSize;
        while (toIndex < size && savingsIds.get(toIndex - 1).equals(savingsIds.get(toIndex))) {
            toIndex++;
        }
        boolean lastBatch = false;
        int loopCount = size / batchSize + 1;

        for (long i = 0; i < loopCount; i++) {
            List<Long> subList = safeSubList(savingsIds, fromIndex, toIndex);
            SavingsSchedularInterestPoster poster = (SavingsSchedularInterestPoster) this.applicationContext
                    .getBean("savingsSchedularInterestPoster");
            poster.setSavingsIds(subList);
            poster.setTenant(ThreadLocalContextUtil.getTenant());
            poster.setSavingsAccountWritePlatformService(savingsAccountWritePlatformService);
            poster.setSavingsAccountRepository(savingsAccountRepository);
            poster.setSavingAccountAssembler(savingAccountAssembler);
            posters.add(poster);

            if (lastBatch) {
                break;
            }
            if (toIndex + batchSize > size - 1) {
                lastBatch = true;
            }
            fromIndex = fromIndex + (toIndex - fromIndex);
            toIndex = (toIndex + batchSize > size - 1) ? size : toIndex + batchSize;
            while (toIndex < size && savingsIds.get(toIndex - 1).equals(savingsIds.get(toIndex))) {
                toIndex++;
            }
        }
        try {
            List<Future<Void>> responses = executorService.invokeAll(posters);
            checkCompletion(responses);
        } catch (InterruptedException e1) {
            LOG.error("Interrupted while postInterest", e1);
        }
    }

    // break the lists into sub lists
    public <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
        int size = list.size();
        if (fromIndex >= size || toIndex <= 0 || fromIndex >= toIndex) {
            return Collections.emptyList();
        }

        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.min(size, toIndex);

        return list.subList(fromIndex, toIndex);
    }

    // checks the execution of task by each thread in the executor service
    private void checkCompletion(List<Future<Void>> responses) {
        try {
            for (Future f : responses) {
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
                LOG.error("All threads could not execute.");
            }
        } catch (InterruptedException e1) {
            LOG.error("Interrupted while interest posting entries", e1);
        } catch (ExecutionException e2) {
            LOG.error("Execution exception while interest posting entries", e2);
        }
    }

    @Override
    @CronTarget(jobName = JobName.UPDATE_SAVINGS_DORMANT_ACCOUNTS)
    public void updateSavingsDormancyStatus() throws JobExecutionException {
        LocalDate tenantLocalDate = DateUtils.getLocalDateOfTenant();

        List<Long> savingsPendingInactive = savingAccountReadPlatformService.retrieveSavingsIdsPendingInactive(tenantLocalDate);
        if (null != savingsPendingInactive && savingsPendingInactive.size() > 0) {
            for (Long savingsId : savingsPendingInactive) {
                this.savingsAccountWritePlatformService.setSubStatusInactive(savingsId);
            }
        }

        List<Long> savingsPendingDormant = savingAccountReadPlatformService.retrieveSavingsIdsPendingDormant(tenantLocalDate);
        if (null != savingsPendingDormant && savingsPendingDormant.size() > 0) {
            for (Long savingsId : savingsPendingDormant) {
                this.savingsAccountWritePlatformService.setSubStatusDormant(savingsId);
            }
        }

        List<Long> savingsPendingEscheat = savingAccountReadPlatformService.retrieveSavingsIdsPendingEscheat(tenantLocalDate);
        if (null != savingsPendingEscheat && savingsPendingEscheat.size() > 0) {
            for (Long savingsId : savingsPendingEscheat) {
                this.savingsAccountWritePlatformService.escheat(savingsId);
            }
        }
    }
}
