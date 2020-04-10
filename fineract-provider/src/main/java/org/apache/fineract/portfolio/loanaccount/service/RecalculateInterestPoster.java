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

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class RecalculateInterestPoster implements Callable<Void> {

    private final static Logger logger = LoggerFactory.getLogger(RecalculateInterestPoster.class);

    private Collection<Long> loanIds;
    private LoanWritePlatformService loanWritePlatformService;

    public void setLoanIds(final Collection<Long> loanIds) {
        this.loanIds = loanIds;
    }

    public void setLoanWritePlatformService(final LoanWritePlatformService loanWritePlatformService) {
        this.loanWritePlatformService = loanWritePlatformService;
    }

    @Override
    public Void call() throws JobExecutionException {
        Integer maxNumberOfRetries = ThreadLocalContextUtil.getTenant()
                .getConnection().getMaxRetriesOnDeadlock();
        Integer maxIntervalBetweenRetries = ThreadLocalContextUtil.getTenant()
                .getConnection().getMaxIntervalBetweenRetries();

        int i = 0;
        if (!loanIds.isEmpty()) {
            int errors = 0;
            for (Long loanId : loanIds) {
                logger.info("Loan ID {}", loanId);
                Integer numberOfRetries = 0;
                while (numberOfRetries <= maxNumberOfRetries) {
                    try {
                        this.loanWritePlatformService.recalculateInterest(loanId);
                        numberOfRetries = maxNumberOfRetries + 1;
                    } catch (CannotAcquireLockException
                            | ObjectOptimisticLockingFailureException exception) {
                        logger.info("Recalulate interest job has been retried {} time(s)", numberOfRetries);
                        // Fail if the transaction has been retired for maxNumberOfRetries
                        if (numberOfRetries >= maxNumberOfRetries) {
                            logger.error("Recalulate interest job has been retried for the max allowed attempts of {} and will be rolled back", numberOfRetries);
                            ++errors;
                            break;
                        }
                        // Else sleep for a random time (between 1 to 10 seconds) and continue
                        try {
                            Random random = new Random();
                            int randomNum = random.nextInt(maxIntervalBetweenRetries + 1);
                            Thread.sleep(1000 + (randomNum * 1000));
                            numberOfRetries = numberOfRetries + 1;
                        } catch (InterruptedException e) {
                            logger.error("Interest recalculation for loans retry failed due to InterruptedException", e) ;
                            ++errors;
                            break;
                        }
                    } catch (Exception e) {
                        logger.error("Interest recalculation for loans failed for account {}", loanId, e);
                        numberOfRetries = maxNumberOfRetries + 1;
                        ++errors;
                    }
                    i++;
                }
                logger.info("Loans count {}", i);
            }
            if (errors > 0) { throw new JobExecutionException(errors); }
        }
        return null;
    }
}