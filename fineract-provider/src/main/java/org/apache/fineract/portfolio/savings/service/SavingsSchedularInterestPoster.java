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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * @author manoj
 */

@Component
@Scope("prototype")
public class SavingsSchedularInterestPoster implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsSchedularInterestPoster.class);
    private static final SecureRandom random = new SecureRandom();

    private Collection<Long> savingsIds;
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private SavingsAccountRepositoryWrapper savingsAccountRepository;
    private SavingsAccountAssembler savingAccountAssembler;
    private FineractPlatformTenant tenant;
    private ConfigurationDomainService configurationDomainService;

    public void setSavingsIds(Collection<Long> savingsIds) {
        this.savingsIds = savingsIds;
    }

    public void setSavingsAccountWritePlatformService(SavingsAccountWritePlatformService savingsAccountWritePlatformService) {
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
    }

    public void setSavingsAccountRepository(SavingsAccountRepositoryWrapper savingsAccountRepository) {
        this.savingsAccountRepository = savingsAccountRepository;
    }

    public void setSavingAccountAssembler(SavingsAccountAssembler savingAccountAssembler) {
        this.savingAccountAssembler = savingAccountAssembler;
    }

    public void setTenant(FineractPlatformTenant tenant) {
        this.tenant = tenant;
    }

    @Override
    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public Void call() throws org.apache.fineract.infrastructure.jobs.exception.JobExecutionException {
        ThreadLocalContextUtil.setTenant(tenant);
        Integer maxNumberOfRetries = tenant.getConnection().getMaxRetriesOnDeadlock();
        Integer maxIntervalBetweenRetries = tenant.getConnection().getMaxIntervalBetweenRetries();
        final boolean backdatedTxnsAllowedTill = this.configurationDomainService.retrievePivotDateConfig();
        int i = 0;
        if (!savingsIds.isEmpty()) {
            List<Throwable> errors = new ArrayList<>();
            for (Long savingsId : savingsIds) {
                LOG.info("Savings ID {}", savingsId);
                Integer numberOfRetries = 0;
                while (numberOfRetries <= maxNumberOfRetries) {
                    try {
                        SavingsAccount savingsAccount = this.savingsAccountRepository.findOneWithNotFoundDetection(savingsId);
                        this.savingAccountAssembler.assignSavingAccountHelpers(savingsAccount);
                        boolean postInterestAsOn = false;
                        LocalDate transactionDate = null;
                        this.savingsAccountWritePlatformService.postInterest(savingsAccount, postInterestAsOn, transactionDate,
                                backdatedTxnsAllowedTill);

                        numberOfRetries = maxNumberOfRetries + 1;
                    } catch (CannotAcquireLockException | ObjectOptimisticLockingFailureException exception) {
                        LOG.info("Interest posting job for savings ID {} has been retried {} time(s)", savingsId, numberOfRetries);
                        // Fail if the transaction has been retired for
                        // maxNumberOfRetries
                        if (numberOfRetries >= maxNumberOfRetries) {
                            LOG.error(
                                    "Interest posting job for savings ID {} has been retried for the max allowed attempts of {} and will be rolled back",
                                    savingsId, numberOfRetries);
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
                            LOG.error("Interest posting job for savings retry failed due to InterruptedException", e);
                            errors.add(e);
                            break;
                        }
                    } catch (Exception e) {
                        LOG.error("Interest posting job for savings failed for account {}", savingsId, e);
                        numberOfRetries = maxNumberOfRetries + 1;
                        errors.add(e);
                    }
                    i++;
                }
                LOG.info("Savings count {}", i);
            }
            if (!errors.isEmpty()) {
                throw new JobExecutionException(errors);
            }
        }
        return null;
    }
}
