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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

@Slf4j
@RequiredArgsConstructor
public class LoanAccrualPlatformServiceImpl implements LoanAccrualPlatformService {

    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanAccrualWritePlatformService loanAccrualWritePlatformService;

    @Override
    public void addPeriodicAccruals(final LocalDate tillDate) throws JobExecutionException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDataList = this.loanReadPlatformService
                .retrievePeriodicAccrualData(tillDate);
        addPeriodicAccruals(tillDate, loanScheduleAccrualDataList);
    }

    @Override
    public void addPeriodicAccruals(final LocalDate tillDate, Loan loan) throws JobExecutionException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDataList = this.loanReadPlatformService.retrievePeriodicAccrualData(tillDate,
                loan);
        addPeriodicAccruals(tillDate, loanScheduleAccrualDataList);
    }

    @Override
    public void addPeriodicAccruals(final LocalDate tillDate, Collection<LoanScheduleAccrualData> loanScheduleAccrualDataList)
            throws JobExecutionException {
        Map<Long, Collection<LoanScheduleAccrualData>> loanDataMap = new HashMap<>();
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDataList) {
            if (loanDataMap.containsKey(accrualData.getLoanId())) {
                loanDataMap.get(accrualData.getLoanId()).add(accrualData);
            } else {
                Collection<LoanScheduleAccrualData> accrualDataList = new ArrayList<>();
                accrualDataList.add(accrualData);
                loanDataMap.put(accrualData.getLoanId(), accrualDataList);
            }
        }

        List<Throwable> errors = new ArrayList<>();
        for (Map.Entry<Long, Collection<LoanScheduleAccrualData>> mapEntry : loanDataMap.entrySet()) {
            try {
                this.loanAccrualWritePlatformService.addPeriodicAccruals(tillDate, mapEntry.getKey(), mapEntry.getValue());
            } catch (Exception e) {
                log.error("Failed to add accrual transaction for loan {}", mapEntry.getKey(), e);
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }
}
