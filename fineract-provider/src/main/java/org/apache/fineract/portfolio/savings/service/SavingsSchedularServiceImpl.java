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

import java.util.List;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SavingsSchedularServiceImpl implements SavingsSchedularService {

    private final static Logger LOG = LoggerFactory.getLogger(SavingsSchedularServiceImpl.class);

    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final SavingsAccountReadPlatformService savingAccountReadPlatformService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;

    @Autowired
    public SavingsSchedularServiceImpl(final SavingsAccountAssembler savingAccountAssembler,
            final SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            final SavingsAccountReadPlatformService savingAccountReadPlatformService, final SavingsAccountRepositoryWrapper savingsAccountRepository) {
        this.savingAccountAssembler = savingAccountAssembler;
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
        this.savingAccountReadPlatformService = savingAccountReadPlatformService;
        this.savingsAccountRepository = savingsAccountRepository;
    }

    @Override
    @CronTarget(jobName = JobName.POST_INTEREST_FOR_SAVINGS)
    public void postInterestForAccounts() throws JobExecutionException {
        int page = 0;
        Integer initialSize = 500;
        Integer totalPageSize = 0;
        int errors = 0;
        do {
            PageRequest pageRequest = PageRequest.of(page, initialSize);
            Page<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findByStatus(ACTIVE.getValue(), pageRequest);
            for (SavingsAccount savingsAccount : savingsAccounts.getContent()) {
                try {
                    this.savingAccountAssembler.assignSavingAccountHelpers(savingsAccount);
                    boolean postInterestAsOn = false;
                    LocalDate transactionDate = null;
                    this.savingsAccountWritePlatformService.postInterest(savingsAccount, postInterestAsOn, transactionDate);
                } catch (Exception e) {
                    LOG.error("Failed to post interest for Savings with id {}", savingsAccount.getId(), e);
                    ++errors;
                }
            }
            page++;
            totalPageSize = savingsAccounts.getTotalPages();
        } while (page < totalPageSize);

        if (errors > 0) { throw new JobExecutionException(errors); }
    }

    @Override
    @CronTarget(jobName = JobName.UPDATE_SAVINGS_DORMANT_ACCOUNTS)
    public void updateSavingsDormancyStatus() throws JobExecutionException {
        LocalDate tenantLocalDate = DateUtils.getLocalDateOfTenant();

        List<Long> savingsPendingInactive = savingAccountReadPlatformService.retrieveSavingsIdsPendingInactive(tenantLocalDate);
        if(null != savingsPendingInactive && savingsPendingInactive.size() > 0){
            for(Long savingsId : savingsPendingInactive){
                this.savingsAccountWritePlatformService.setSubStatusInactive(savingsId);
            }
        }

        List<Long> savingsPendingDormant = savingAccountReadPlatformService.retrieveSavingsIdsPendingDormant(tenantLocalDate);
        if(null != savingsPendingDormant && savingsPendingDormant.size() > 0){
            for(Long savingsId : savingsPendingDormant){
                this.savingsAccountWritePlatformService.setSubStatusDormant(savingsId);
            }
        }

        List<Long> savingsPendingEscheat = savingAccountReadPlatformService.retrieveSavingsIdsPendingEscheat(tenantLocalDate);
        if(null != savingsPendingEscheat && savingsPendingEscheat.size() > 0){
            for(Long savingsId : savingsPendingEscheat){
                this.savingsAccountWritePlatformService.escheat(savingsId);
            }
        }
    }
}
