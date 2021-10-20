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
package org.apache.fineract.portfolio.loanaccount.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.exception.OfficeNotFoundException;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class LoanSchedularServiceImpl implements LoanSchedularService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanSchedularServiceImpl.class);
    private static final SecureRandom random = new SecureRandom();

    private final ConfigurationDomainService configurationDomainService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanWritePlatformService loanWritePlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final ApplicationContext applicationContext;

    @Autowired
    public LoanSchedularServiceImpl(final ConfigurationDomainService configurationDomainService,
            final LoanReadPlatformService loanReadPlatformService, final LoanWritePlatformService loanWritePlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final ApplicationContext applicationContext) {
        this.configurationDomainService = configurationDomainService;
        this.loanReadPlatformService = loanReadPlatformService;
        this.loanWritePlatformService = loanWritePlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.applicationContext = applicationContext;
    }

    @Override
    @CronTarget(jobName = JobName.APPLY_CHARGE_TO_OVERDUE_LOAN_INSTALLMENT)
    public void applyChargeForOverdueLoans() throws JobExecutionException {

        final Long penaltyWaitPeriodValue = this.configurationDomainService.retrievePenaltyWaitPeriod();
        final Boolean backdatePenalties = this.configurationDomainService.isBackdatePenaltiesEnabled();
        final Collection<OverdueLoanScheduleData> overdueLoanScheduledInstallments = this.loanReadPlatformService
                .retrieveAllLoansWithOverdueInstallments(penaltyWaitPeriodValue, backdatePenalties);

        if (!overdueLoanScheduledInstallments.isEmpty()) {
            final Map<Long, Collection<OverdueLoanScheduleData>> overdueScheduleData = new HashMap<>();
            for (final OverdueLoanScheduleData overdueInstallment : overdueLoanScheduledInstallments) {
                if (overdueScheduleData.containsKey(overdueInstallment.getLoanId())) {
                    overdueScheduleData.get(overdueInstallment.getLoanId()).add(overdueInstallment);
                } else {
                    Collection<OverdueLoanScheduleData> loanData = new ArrayList<>();
                    loanData.add(overdueInstallment);
                    overdueScheduleData.put(overdueInstallment.getLoanId(), loanData);
                }
            }

            List<Throwable> exceptions = new ArrayList<>();
            for (final Long loanId : overdueScheduleData.keySet()) {
                try {
                    this.loanWritePlatformService.applyOverdueChargesForLoan(loanId, overdueScheduleData.get(loanId));

                } catch (final PlatformApiDataValidationException e) {
                    final List<ApiParameterError> errors = e.getErrors();
                    for (final ApiParameterError error : errors) {
                        LOG.error("Apply Charges due for overdue loans failed for account {} with message: {}", loanId,
                                error.getDeveloperMessage(), e);
                    }
                    exceptions.add(e);
                } catch (final AbstractPlatformDomainRuleException e) {
                    LOG.error("Apply Charges due for overdue loans failed for account {} with message: {}", loanId,
                            e.getDefaultUserMessage(), e);
                    exceptions.add(e);
                } catch (Exception e) {
                    LOG.error("Apply Charges due for overdue loans failed for account {}", loanId, e);
                    exceptions.add(e);
                }
            }
            if (!exceptions.isEmpty()) {
                throw new JobExecutionException(exceptions);
            }
        }
    }

    @Override
    @CronTarget(jobName = JobName.RECALCULATE_INTEREST_FOR_LOAN)
    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public void recalculateInterest() throws JobExecutionException {
        Integer maxNumberOfRetries = ThreadLocalContextUtil.getTenant().getConnection().getMaxRetriesOnDeadlock();
        Integer maxIntervalBetweenRetries = ThreadLocalContextUtil.getTenant().getConnection().getMaxIntervalBetweenRetries();
        Collection<Long> loanIds = this.loanReadPlatformService.fetchLoansForInterestRecalculation();
        int i = 0;
        if (!loanIds.isEmpty()) {
            List<Throwable> errors = new ArrayList<>();
            for (Long loanId : loanIds) {
                LOG.info("recalculateInterest: Loan ID = {}", loanId);
                Integer numberOfRetries = 0;
                while (numberOfRetries <= maxNumberOfRetries) {
                    try {
                        this.loanWritePlatformService.recalculateInterest(loanId);
                        numberOfRetries = maxNumberOfRetries + 1;
                    } catch (CannotAcquireLockException | ObjectOptimisticLockingFailureException exception) {
                        LOG.info("Recalulate interest job has been retried {} time(s)", numberOfRetries);
                        // Fail if the transaction has been retried for
                        // maxNumberOfRetries
                        if (numberOfRetries >= maxNumberOfRetries) {
                            LOG.error("Recalulate interest job has been retried for the max allowed attempts of {} and will be rolled back",
                                    numberOfRetries);
                            errors.add(exception);
                            break;
                        }
                        // Else sleep for a random time (between 1 to 10
                        // seconds) and continue
                        try {
                            int randomNum = random.nextInt(maxIntervalBetweenRetries + 1);
                            Thread.sleep(1000 + (randomNum * 1000));
                            numberOfRetries = numberOfRetries + 1;
                        } catch (InterruptedException e) {
                            LOG.error("Interest recalculation for loans retry failed due to InterruptedException", e);
                            errors.add(e);
                            break;
                        }
                    } catch (Exception e) {
                        LOG.error("Interest recalculation for loans failed for account {}", loanId, e);
                        numberOfRetries = maxNumberOfRetries + 1;
                        errors.add(e);
                    }
                    i++;
                }
                LOG.info("recalculateInterest: Loans count {}", i);
            }
            if (!errors.isEmpty()) {
                throw new JobExecutionException(errors);
            }
        }

    }

    @Override
    @CronTarget(jobName = JobName.RECALCULATE_INTEREST_FOR_LOAN)
    public void recalculateInterest(Map<String, String> jobParameters) {
        // gets the officeId
        final String officeId = jobParameters.get("officeId");
        LOG.info("recalculateInterest: officeId={}", officeId);
        Long officeIdLong = Long.valueOf(officeId);

        // gets the Office object
        final OfficeData office = this.officeReadPlatformService.retrieveOffice(officeIdLong);
        if (office == null) {
            throw new OfficeNotFoundException(officeIdLong);
        }
        final int threadPoolSize = Integer.parseInt(jobParameters.get("thread-pool-size"));
        final int batchSize = Integer.parseInt(jobParameters.get("batch-size"));

        recalculateInterest(office, threadPoolSize, batchSize);
    }

    private void recalculateInterest(OfficeData office, int threadPoolSize, int batchSize) {
        final int pageSize = batchSize * threadPoolSize;

        // initialise the executor service with fetched configurations
        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        Long maxLoanIdInList = 0L;
        final String officeHierarchy = office.getHierarchy() + "%";

        // get the loanIds from service
        List<Long> loanIds = Collections.synchronizedList(
                this.loanReadPlatformService.fetchLoansForInterestRecalculation(pageSize, maxLoanIdInList, officeHierarchy));

        // gets the loanIds data set iteratively and call addAccuruals for that
        // paginated dataset
        do {
            int totalFilteredRecords = loanIds.size();
            LOG.info("Starting accrual - total filtered records - {}", totalFilteredRecords);
            recalculateInterest(loanIds, threadPoolSize, batchSize, executorService);
            maxLoanIdInList += pageSize + 1;
            loanIds = Collections.synchronizedList(
                    this.loanReadPlatformService.fetchLoansForInterestRecalculation(pageSize, maxLoanIdInList, officeHierarchy));
        } while (!CollectionUtils.isEmpty(loanIds));

        // shutdown the executor when done
        executorService.shutdownNow();
    }

    private void recalculateInterest(List<Long> loanIds, int threadPoolSize, int batchSize, final ExecutorService executorService) {

        List<Callable<Void>> posters = new ArrayList<>();
        int fromIndex = 0;
        // get the size of current paginated dataset
        int size = loanIds.size();
        // calculate the batch size
        double toGetCeilValue = size / threadPoolSize;
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
            RecalculateInterestPoster poster = (RecalculateInterestPoster) this.applicationContext.getBean("recalculateInterestPoster");
            poster.setLoanIds(subList);
            poster.setLoanWritePlatformService(loanWritePlatformService);
            posters.add(poster);
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
            LOG.error("Interrupted while recalculateInterest", e1);
        }
    }

    // break the lists into sub lists
    private <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
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
                LOG.error("All threads could not execute.");
            }
        } catch (InterruptedException e1) {
            LOG.error("Interrupted while posting IR entries", e1);
        } catch (ExecutionException e2) {
            LOG.error("Execution exception while posting IR entries", e2);
        }
    }
}
