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
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.journalentry.exception.JournalEntryInvalidException;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.ExceptionHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobExecuter;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.jobs.service.JobRunner;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class SavingsSchedularServiceImpl implements SavingsSchedularService {

    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final SavingsAccountReadPlatformService savingAccountReadPlatformService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final ApplicationContext applicationContext;
    private final ConfigurationDomainService configurationDomainService;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private Queue<List<SavingsAccountData>> queue = new ArrayDeque<>();
    private final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper;

    private final SavingsProductRepository savingsProductRepository;
    private final JobExecuter jobExecuter;

    private static final Logger logger = LoggerFactory.getLogger(SavingsSchedularServiceImpl.class);

    @Override
    @CronTarget(jobName = JobName.UPDATE_SAVINGS_DORMANT_ACCOUNTS)
    public void updateSavingsDormancyStatus() throws JobExecutionException {
        LocalDate tenantLocalDate = DateUtils.getBusinessLocalDate();

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

    @Override
    @CronTarget(jobName = JobName.UPDATE_SAVINGS_INTEREST_POSTING_QUALIFY_CONFIG)
    public void updateSavingsInterestPostingQualifyConfig() {

        List<SavingsProduct> products = this.savingsProductRepository.findAll();
        log.info("Reading Savings Account Data!");
        for (SavingsProduct product : products) {
            List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findByProductIdAndStatus(product.getId(),
                    ACTIVE.getValue(), product.getNumOfCreditTransaction(), product.getNumOfDebitTransaction(),
                    product.minBalanceForInterestCalculation());
            if (savingsAccounts.size() > 0) {
                if (product.isInterestPostingUpdate()) {
                    for (SavingsAccount sav : savingsAccounts) {
                        sav.setNumOfCreditTransaction(product.getNumOfCreditTransaction());
                        sav.setNumOfDebitTransaction(product.getNumOfDebitTransaction());
                        sav.setMinBalanceForInterestCalculation(product.minBalanceForInterestCalculation());
                        this.savingsAccountRepository.saveAndFlush(sav);
                        log.info("Successfully Updates Savings Account Data! number is" + sav.getId());
                    }
                }
            }
        }
    }

    @Override
    @CronTarget(jobName = JobName.POST_INTEREST_FOR_SAVINGS)
    public void postInterestForAccountsThreaded(Map<String, String> jobParameters) throws JobExecutionException {

        try {
            Thread nonInterestRecalculationThread = new Thread(new SavingsInterestRunnable());
            nonInterestRecalculationThread.start();
            nonInterestRecalculationThread.join();
        } catch (InterruptedException e) {
            logger.error("Thread Interrupted for Post  : " + e.getMessage());
        }
    }

    private class SavingsInterestRunnable implements Runnable {

        final FineractPlatformTenant tenant;
        final Authentication auth;
        final Map<String, Object> jobParams;
        final LocalDate jobRunDate;

        public SavingsInterestRunnable() {
            this.tenant = ThreadLocalContextUtil.getTenant();
            if (SecurityContextHolder.getContext() == null) {
                this.auth = null;
            } else {
                this.auth = SecurityContextHolder.getContext().getAuthentication();
            }

            this.jobParams = ThreadLocalContextUtil.getJobParams();
            this.jobRunDate = DateUtils.getLocalDateOfTenant();
        }

        @Override
        public void run() {
            ThreadLocalContextUtil.setTenant(tenant);
            ThreadLocalContextUtil.setJobParams(jobParams);
            if (this.auth != null) {
                SecurityContextHolder.getContext().setAuthentication(this.auth);
            }
            final List<Long> activeSavingsAccounts = savingAccountReadPlatformService.retrieveActiveSavingAccountsWithZeroInterest();
            activeSavingsAccounts.addAll(savingAccountReadPlatformService.retrieveActiveOverdraftSavingAccounts());
            JobRunner<List<Long>> runner = new SavingsInterestJobRunner(jobRunDate);
            jobExecuter.executeJob(activeSavingsAccounts, runner);
        }
    }

    private class SavingsInterestJobRunner implements JobRunner<List<Long>> {

        final Integer maxNumberOfRetries;
        final Integer maxIntervalBetweenRetries;
        final LocalDate jobRunDate;

        public SavingsInterestJobRunner(final LocalDate jobRunDate) {
            this.jobRunDate = jobRunDate;
            maxNumberOfRetries = ThreadLocalContextUtil.getTenant().getConnection().getMaxRetriesOnDeadlock();
            maxIntervalBetweenRetries = ThreadLocalContextUtil.getTenant().getConnection().getMaxIntervalBetweenRetries();
        }

        @Override
        public void runJob(final List<Long> savingIds, StringBuilder sb) {
            postInterest(sb, this.maxNumberOfRetries, this.maxIntervalBetweenRetries, savingIds, this.jobRunDate);
        }

    }

    private void postInterest(final StringBuilder sb, Integer maxNumberOfRetries, Integer maxIntervalBetweenRetries, List<Long> savingIds,
            LocalDate jobRunDate) {
        final String errorMessage = "Post Interest failed for account:";

        for (Long savingAccountId : savingIds) {
            if (savingAccountId == 0) {
                continue;
            }
            logger.info("Interest Saving ID " + savingAccountId + " which is " + savingIds.indexOf(savingAccountId) + " of "
                    + savingIds.size());
            Integer numberOfRetries = 0;
            String savingsAccountNumber = "";
            while (numberOfRetries <= maxNumberOfRetries) {
                try {
                    final SavingsAccount savingAccount = this.savingAccountAssembler.assembleFrom(savingAccountId);
                    savingsAccountNumber = savingAccount.getAccountNumber();
                    checkClientOrGroupActive(savingAccount);
                    this.savingsAccountWritePlatformService.postInterest(savingAccount, false, jobRunDate);
                    numberOfRetries = maxNumberOfRetries + 1;
                } catch (CannotAcquireLockException | ObjectOptimisticLockingFailureException exception) {
                    logger.info("Recalulate interest job has been retried  " + numberOfRetries + " time(s)");
                    /***
                     * Fail if the transaction has been retired for maxNumberOfRetries
                     **/
                    if (numberOfRetries >= maxNumberOfRetries) {
                        logger.warn("Post interest job has been retried for the max allowed attempts of " + numberOfRetries
                                + " and will be rolled back. ");
                        sb.append("Post interest job has been retried for the max allowed attempts of " + numberOfRetries
                                + " and will be rolled back. ");
                        break;
                    }
                    /***
                     * Else sleep for a random time (between 1 to 10 seconds) and continue
                     **/
                    try {
                        Random random = new Random();
                        int randomNum = random.nextInt(maxIntervalBetweenRetries + 1);
                        Thread.sleep(1000 + (randomNum * 1000));
                        numberOfRetries = numberOfRetries + 1;
                    } catch (InterruptedException e) {
                        sb.append("Post interest for savings failed " + exception.getMessage());
                        break;
                    }
                } catch (Exception e) {
                    if (e instanceof JournalEntryInvalidException) {
                        Throwable realCause = e;
                        if (e.getCause() != null) {
                            realCause = e.getCause();
                        }
                        String message = realCause.getMessage();
                        if (message == null && realCause instanceof JournalEntryInvalidException) {
                            message = ((JournalEntryInvalidException) realCause).getDefaultUserMessage();
                        }
                        sb.append(" Failed to post interest for Savings with id " + savingsAccountNumber + " with message " + message);
                    } else {
                        ExceptionHelper.handleExceptions(e, sb, errorMessage, savingAccountId, logger);
                    }
                    numberOfRetries = maxNumberOfRetries + 1;
                }
            }
        }
    }

    private void checkClientOrGroupActive(final SavingsAccount account) {
        final Client client = account.getClient();
        if (client != null) {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        final Group group = account.group();
        if (group != null) {
            if (group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }
        }
    }
}
