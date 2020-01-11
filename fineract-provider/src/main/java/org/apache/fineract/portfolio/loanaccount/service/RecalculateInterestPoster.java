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

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepository;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;


@Component

@Scope("prototype")

public class RecalculateInterestPoster implements Runnable {



    private final static Logger logger = LoggerFactory.getLogger(" recalculate interest poster");

    private Collection<Long> loanIds;

    private LoanWritePlatformService loanWritePlatformService;


    public void setLoanIds(final Collection<Long> loanIds) {

        this.loanIds = loanIds;

    }



    public void setLoanWritePlatformService(final LoanWritePlatformService loanWritePlatformService) {

        this.loanWritePlatformService = loanWritePlatformService;

    }



    @Override
    public void run() {

        Integer maxNumberOfRetries = ThreadLocalContextUtil.getTenant()
                .getConnection().getMaxRetriesOnDeadlock();
        Integer maxIntervalBetweenRetries = ThreadLocalContextUtil.getTenant()
                .getConnection().getMaxIntervalBetweenRetries();

        int i = 0;
        if (!loanIds.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (Long loanId : loanIds) {
                logger.info("Loan ID " + loanId);
                Integer numberOfRetries = 0;
                while (numberOfRetries <= maxNumberOfRetries) {
                    try {
                        this.loanWritePlatformService
                                .recalculateInterest(loanId);
                        numberOfRetries = maxNumberOfRetries + 1;
                    } catch (CannotAcquireLockException
                            | ObjectOptimisticLockingFailureException exception) {
                        logger.info("Recalulate interest job has been retried  "
                                + numberOfRetries + " time(s)");
                        /***
                         * Fail if the transaction has been retired for
                         * maxNumberOfRetries
                         **/
                        if (numberOfRetries >= maxNumberOfRetries) {
                            logger.warn("Recalulate interest job has been retried for the max allowed attempts of "
                                    + numberOfRetries
                                    + " and will be rolled back");
                            sb.append("Recalulate interest job has been retried for the max allowed attempts of "
                                    + numberOfRetries
                                    + " and will be rolled back");
                            break;
                        }
                        /***
                         * Else sleep for a random time (between 1 to 10
                         * seconds) and continue
                         **/
                        try {
                            Random random = new Random();
                            int randomNum = random
                                    .nextInt(maxIntervalBetweenRetries + 1);
                            Thread.sleep(1000 + (randomNum * 1000));
                            numberOfRetries = numberOfRetries + 1;
                        } catch (InterruptedException e) {
                            sb.append("Interest recalculation for loans failed " + exception.getMessage()) ;
                            break;
                        }
                    } catch (Exception e) {
                        Throwable realCause = e;
                        if (e.getCause() != null) {
                            realCause = e.getCause();
                        }
                        logger.error("Interest recalculation for loans failed for account:"	+ loanId + " with message " + realCause.getMessage(), e);
                        sb.append("Interest recalculation for loans failed for account:").append(loanId).append(" with message ")
                                .append(realCause.getMessage());
                        numberOfRetries = maxNumberOfRetries + 1;
                    }
                    i++;
                }
                logger.info("Loans count " + i);
            }
            if (sb.length() > 0) {
                try {
                    throw new JobExecutionException(sb.toString());
                } catch (JobExecutionException e) {
                    logger.info("JobExecutionException occured :", e);
                }
            }
        }


    }



}