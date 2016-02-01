/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.List;

import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountAssembler;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountRepository;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavingsSchedularServiceImpl implements SavingsSchedularService {

    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final SavingsAccountRepository savingAccountRepository;

    @Autowired
    public SavingsSchedularServiceImpl(final SavingsAccountAssembler savingAccountAssembler,
            final SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            final SavingsAccountRepository savingAccountRepository) {
        this.savingAccountAssembler = savingAccountAssembler;
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
        this.savingAccountRepository = savingAccountRepository;
    }

    @CronTarget(jobName = JobName.POST_INTEREST_FOR_SAVINGS)
    @Override
    public void postInterestForAccounts() throws JobExecutionException {
        final List<SavingsAccount> savingsAccounts = this.savingAccountRepository.findSavingAccountByStatus(SavingsAccountStatusType.ACTIVE
                .getValue());
        StringBuffer sb = new StringBuffer();
        for (final SavingsAccount savingsAccount : savingsAccounts) {
            try {
                this.savingAccountAssembler.assignSavingAccountHelpers(savingsAccount);
                this.savingsAccountWritePlatformService.postInterest(savingsAccount);
            } catch (Exception e) {
                Throwable realCause = e;
                if (e.getCause() != null) {
                    realCause = e.getCause();
                }
                sb.append("failed to post interest for Savings with id " + savingsAccount.getId() + " with message "
                        + realCause.getMessage());
            }
        }
        
        if (sb.length() > 0) { throw new JobExecutionException(sb.toString()); }
    }
}
