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

import java.util.Deque;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkingCapitalLoanBreachResetServiceImpl implements WorkingCapitalLoanBreachResetService {

    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;
    private final WorkingCapitalLoanActiveBreachResetResolver activeBreachResetResolver;

    @Override
    public void resetBreach(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachAction resetAction) {
        if (resetAction.getStartDate() == null) {
            return;
        }
        if (Boolean.TRUE.equals(resetAction.getRestartPeriodFromResetDate())) {
            breachScheduleService.splitPeriodAtReset(loan, resetAction.getStartDate());
            breachScheduleService.reprocessBreachSchedule(loan);
        } else {
            breachScheduleService.applyActiveResetFlags(loan);
            breachScheduleService.recalculatePastDueAmount(loan);
        }
    }

    @Override
    public void undoResetBreach(final WorkingCapitalLoan loan, final WorkingCapitalLoanBreachAction undoResetAction,
            final List<WorkingCapitalLoanBreachAction> priorActions) {
        if (undoResetAction.getStartDate() == null) {
            return;
        }
        final Deque<WorkingCapitalLoanBreachAction> activeResets = activeBreachResetResolver.activeResets(priorActions);
        final WorkingCapitalLoanBreachAction undoneReset = activeResets.poll();
        if (undoneReset == null) {
            log.warn("No active breach reset found to undo on working capital loan {}", loan.getId());
        }
        if (undoneReset != null && Boolean.TRUE.equals(undoneReset.getRestartPeriodFromResetDate())) {
            breachScheduleService.restoreSplitPeriod(loan, undoneReset);
            breachScheduleService.reprocessBreachSchedule(loan);
        } else {
            breachScheduleService.applyActiveResetFlags(loan);
            breachScheduleService.recalculatePastDueAmount(loan);
        }
    }
}
