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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachResetHistory;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachScheduleEvaluationUtils;
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
        final WorkingCapitalLoanBreachSchedule evaluationPeriod = WorkingCapitalLoanBreachScheduleEvaluationUtils
                .resolveEvaluationPeriod(periods, actionDate).orElseThrow();
        final Integer evaluationPeriodNumber = evaluationPeriod.getPeriodNumber();
        if (evaluationPeriodNumber == null) {
            return;
        }

        final List<WorkingCapitalLoanBreachResetHistory> resetHistory = new ArrayList<>();

        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            final Integer periodNumber = period.getPeriodNumber();
            if (periodNumber == null || periodNumber > evaluationPeriodNumber) {
                continue;
            }
            if (periodNumber < evaluationPeriodNumber) {
                if (!hasPriorPeriodResettableState(period)) {
                    continue;
                }
                resetHistory.add(createResetHistory(resetAction, period));
                period.setBreach(null);
                period.setNearBreach(null);
                period.setOutstandingAmount(BigDecimal.ZERO);
            } else {
                if (Boolean.TRUE.equals(period.getBreach()) || Boolean.TRUE.equals(period.getNearBreach())) {
                    resetHistory.add(createResetHistory(resetAction, period));
                }
                period.setBreach(null);
                period.setNearBreach(null);
                final BigDecimal paidAmount = period.getPaidAmount() != null ? period.getPaidAmount() : BigDecimal.ZERO;
                final BigDecimal minPayment = period.getMinPaymentAmount() != null ? period.getMinPaymentAmount() : BigDecimal.ZERO;
                period.setOutstandingAmount(minPayment.subtract(paidAmount).max(BigDecimal.ZERO));
            }
        }

        breachScheduleRepository.saveAllAndFlush(periods);
        breachResetHistoryRepository.saveAllAndFlush(resetHistory);
    }

    @Override
    public void undoResetBreach(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachAction undoResetAction) {
        final WorkingCapitalLoanBreachAction latestReset = activeBreachResetResolver
                .findLatestActiveReset(loan.getId(), undoResetAction.getId()).orElseThrow();
        final Long latestResetId = latestReset.getId();

        final List<WorkingCapitalLoanBreachResetHistory> resetHistory = breachResetHistoryRepository
                .findByBreachActionIdOrderByBreachSchedulePeriodNumberAsc(latestResetId);
        final List<WorkingCapitalLoanBreachSchedule> periods = breachScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(loan.getId());
        final LocalDate actionDate = undoResetAction.getStartDate();

        for (final WorkingCapitalLoanBreachResetHistory historyEntry : resetHistory) {
            final WorkingCapitalLoanBreachSchedule historySchedule = historyEntry.getBreachSchedule();
            if (historySchedule == null || historySchedule.getId() == null) {
                continue;
            }
            final Long historyScheduleId = historySchedule.getId();
            periods.stream().filter(period -> Objects.equals(period.getId(), historyScheduleId)).findFirst().ifPresent(period -> {
                period.setOutstandingAmount(historyEntry.getOutstandingAmount());
                period.setBreach(historyEntry.getBreach());
                period.setNearBreach(historyEntry.getNearBreach());
            });
        }

        breachScheduleRepository.saveAllAndFlush(periods);
        recalculateBreachesAfterUndo(loan, periods, actionDate);
    }

    private boolean hasPriorPeriodResettableState(final WorkingCapitalLoanBreachSchedule period) {
        final BigDecimal outstandingAmount = period.getOutstandingAmount();
        final boolean hasOutstanding = outstandingAmount != null && outstandingAmount.compareTo(BigDecimal.ZERO) > 0;
        return Boolean.TRUE.equals(period.getBreach()) || Boolean.TRUE.equals(period.getNearBreach()) || hasOutstanding
                || period.getBreach() != null;
    }

    private WorkingCapitalLoanBreachResetHistory createResetHistory(final WorkingCapitalLoanBreachAction resetAction,
            final WorkingCapitalLoanBreachSchedule period) {
        final WorkingCapitalLoanBreachResetHistory historyEntry = new WorkingCapitalLoanBreachResetHistory();
        historyEntry.setBreachAction(resetAction);
        historyEntry.setBreachSchedule(period);
        historyEntry.setOutstandingAmount(period.getOutstandingAmount());
        historyEntry.setBreach(period.getBreach());
        historyEntry.setNearBreach(period.getNearBreach());
        return historyEntry;
    }

    private void recalculateBreachesAfterUndo(final WorkingCapitalLoan loan, final List<WorkingCapitalLoanBreachSchedule> periods,
            final LocalDate actionDate) {
        if (actionDate == null) {
            return;
        }
        for (final WorkingCapitalLoanBreachSchedule period : periods) {
            if (period.getBreach() != null) {
                continue;
            }
            final LocalDate toDate = period.getToDate();
            if (toDate != null && !toDate.isAfter(actionDate) && breachScheduleService.evaluateBreachOnDate(period, actionDate)) {
                breachScheduleRepository.saveAndFlush(period);
            }
        }
        breachScheduleService.evaluateBreach(loan, actionDate);
    }

}
