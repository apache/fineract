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
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanBreachResetServiceImpl implements WorkingCapitalLoanBreachResetService {

    private final WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;

    @Override
    public void resetBreach(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachAction resetAction) {
        final LocalDate actionDate = resetAction.getStartDate();
        if (actionDate == null) {
            return;
        }

        final List<WorkingCapitalLoanBreachSchedule> periods = breachScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(loan.getId());
        if (Boolean.TRUE.equals(resetAction.getRestartPeriodFromResetDate()) && !periods.isEmpty()
                && !periods.getLast().getFromDate().isEqual(actionDate)) {
            final WorkingCapitalLoanBreachSchedule lastPeriod = periods.getLast();
            lastPeriod.setToDate(actionDate.minusDays(1));
            lastPeriod.setNumberOfDays((int) ChronoUnit.DAYS.between(lastPeriod.getFromDate(), lastPeriod.getToDate()) + 1);
            breachScheduleService.generateNextPeriodIfNeeded(loan, actionDate);
            breachScheduleService.reprocessBreachSchedule(loan);
        }

        breachScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loan.getId(), actionDate, actionDate)
                .filter(period -> !period.isReset()).ifPresent(period -> {
                    period.setReset(true);
                });
        breachScheduleService.recalculatePastDueAmount(loan);
    }

    @Override
    public void undoResetBreach(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachAction undoResetAction) {
        final LocalDate actionDate = undoResetAction.getStartDate();
        if (actionDate == null) {
            return;
        }
        breachScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(loan.getId(), actionDate, actionDate)
                .filter(WorkingCapitalLoanBreachSchedule::isReset).ifPresent(period -> {
                    period.setReset(false);
                });
        breachScheduleService.recalculatePastDueAmount(loan);
    }
}
