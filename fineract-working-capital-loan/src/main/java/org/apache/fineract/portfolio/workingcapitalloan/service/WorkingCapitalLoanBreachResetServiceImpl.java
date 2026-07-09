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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachResetHistory;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachResetHistoryRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanBreachResetServiceImpl implements WorkingCapitalLoanBreachResetService {

    private final WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    private final WorkingCapitalLoanBreachResetHistoryRepository breachResetHistoryRepository;
    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;
    private final WorkingCapitalLoanActiveBreachResetResolver activeBreachResetResolver;

    @Override
    public void resetBreach(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachAction resetAction) {
        final LocalDate actionDate = resetAction.getStartDate();
        if (actionDate == null) {
            return;
        }
        final List<WorkingCapitalLoanBreachSchedule> periods = breachScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(loan.getId());
        final List<WorkingCapitalLoanBreachSchedule> pastPeriods = periods.stream()
                .filter(period -> DateUtils.isBefore(period.getToDate(), resetAction.getStartDate())).filter(period -> !period.isReset())
                .toList();
        final List<WorkingCapitalLoanBreachResetHistory> resetHistory = new ArrayList<>();

        for (final WorkingCapitalLoanBreachSchedule period : pastPeriods) {
            resetHistory.add(createResetHistory(resetAction, period));
            period.reset();
        }

        breachScheduleRepository.saveAllAndFlush(periods);
        breachResetHistoryRepository.saveAllAndFlush(resetHistory);
    }

    @Override
    public void undoResetBreach(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachAction undoResetAction) {
        final Optional<WorkingCapitalLoanBreachAction> latestActiveReset = activeBreachResetResolver.findLatestActiveReset(loan.getId());
        final List<WorkingCapitalLoanBreachSchedule> periods = breachScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(loan.getId());
        if (latestActiveReset.isPresent()) {
            periods.stream().filter(period -> DateUtils.isAfterInclusive(period.getToDate(), latestActiveReset.get().getStartDate()))
                    .forEach(period -> {
                        period.setReset(false);
                    });
        } else {
            periods.forEach(period -> {
                period.setReset(false);
            });
        }

        breachScheduleService.reprocessBreachSchedule(loan);
    }

    private WorkingCapitalLoanBreachResetHistory createResetHistory(final WorkingCapitalLoanBreachAction resetAction,
            final WorkingCapitalLoanBreachSchedule period) {
        return new WorkingCapitalLoanBreachResetHistory(resetAction, period);
    }
}
