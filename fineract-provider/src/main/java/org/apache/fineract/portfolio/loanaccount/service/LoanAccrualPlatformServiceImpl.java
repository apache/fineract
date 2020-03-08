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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.exception.MultiException;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanAccrualPlatformServiceImpl implements LoanAccrualPlatformService {

    private final static Logger LOG = LoggerFactory.getLogger(LoanAccrualPlatformServiceImpl.class);

    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanAccrualWritePlatformService loanAccrualWritePlatformService;

    @Autowired
    public LoanAccrualPlatformServiceImpl(final LoanReadPlatformService loanReadPlatformService,
            final LoanAccrualWritePlatformService loanAccrualWritePlatformService) {
        this.loanReadPlatformService = loanReadPlatformService;
        this.loanAccrualWritePlatformService = loanAccrualWritePlatformService;
    }

    @Override
    @CronTarget(jobName = JobName.ADD_ACCRUAL_ENTRIES)
    public void addAccrualAccounting() throws JobExecutionException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas = this.loanReadPlatformService.retriveScheduleAccrualData();
        Map<Long, Collection<LoanScheduleAccrualData>> loanDataMap = new HashMap<>();
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            if (loanDataMap.containsKey(accrualData.getLoanId())) {
                loanDataMap.get(accrualData.getLoanId()).add(accrualData);
            } else {
                Collection<LoanScheduleAccrualData> accrualDatas = new ArrayList<>();
                accrualDatas.add(accrualData);
                loanDataMap.put(accrualData.getLoanId(), accrualDatas);
            }
        }

        int errors = 0;
        for (Map.Entry<Long, Collection<LoanScheduleAccrualData>> mapEntry : loanDataMap.entrySet()) {
            try {
                this.loanAccrualWritePlatformService.addAccrualAccounting(mapEntry.getKey(), mapEntry.getValue());
            } catch (Exception e) {
                LOG.error("Failed to add accural transaction for loan {}", mapEntry.getKey(), e);
                ++errors;
            }
        }
        if (errors > 0) { throw new JobExecutionException(errors); }
    }

    @Override
    @CronTarget(jobName = JobName.ADD_PERIODIC_ACCRUAL_ENTRIES)
    public void addPeriodicAccruals() throws JobExecutionException {
        try {
            addPeriodicAccruals(LocalDate.now());
        } catch (MultiException e) {
            throw new JobExecutionException(e.getCausesSize());
        }
    }

    @Override
    public void addPeriodicAccruals(final LocalDate tilldate) throws MultiException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas = this.loanReadPlatformService.retrivePeriodicAccrualData(tilldate);
        addPeriodicAccruals(tilldate, loanScheduleAccrualDatas);
    }

    @Override
    public void addPeriodicAccruals(final LocalDate tilldate, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas) throws MultiException {
        Map<Long, Collection<LoanScheduleAccrualData>> loanDataMap = new HashMap<>();
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            if (loanDataMap.containsKey(accrualData.getLoanId())) {
                loanDataMap.get(accrualData.getLoanId()).add(accrualData);
            } else {
                Collection<LoanScheduleAccrualData> accrualDatas = new ArrayList<>();
                accrualDatas.add(accrualData);
                loanDataMap.put(accrualData.getLoanId(), accrualDatas);
            }
        }

        int errors = 0;
        for (Map.Entry<Long, Collection<LoanScheduleAccrualData>> mapEntry : loanDataMap.entrySet()) {
            try {
                this.loanAccrualWritePlatformService.addPeriodicAccruals(tilldate, mapEntry.getKey(), mapEntry.getValue());
            } catch (Exception e) {
                LOG.error("Failed to add accural transaction for loan {}", mapEntry.getKey(), e);
                ++errors;
            }
        }
        if (errors > 0) { throw new MultiException(errors); }
    }

    @Override
    @CronTarget(jobName = JobName.ADD_PERIODIC_ACCRUAL_ENTRIES_FOR_LOANS_WITH_INCOME_POSTED_AS_TRANSACTIONS)
    public void addPeriodicAccrualsForLoansWithIncomePostedAsTransactions() throws JobExecutionException {
        Collection<Long> loanIds = this.loanReadPlatformService.retrieveLoanIdsWithPendingIncomePostingTransactions();
        if(loanIds != null && loanIds.size() > 0){
            int errors = 0;
            for (Long loanId : loanIds) {
                try {
                    this.loanAccrualWritePlatformService.addIncomeAndAccrualTransactions(loanId);
                } catch (Exception e) {
                    LOG.error("Failed to add income and accrual transaction for loan {}", loanId, e);
                    ++errors;
                }
            }
            if (errors > 0) { throw new JobExecutionException(errors); }
        }
    }
}
