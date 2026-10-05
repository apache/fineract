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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanBreachResetServiceImplTest {

    private static final Long LOAN_ID = 1L;

    @Mock
    private WorkingCapitalLoanBreachScheduleService breachScheduleService;

    @Mock
    private WorkingCapitalLoanBreachActionRepository breachActionRepository;

    private WorkingCapitalLoanBreachResetServiceImpl underTest;

    private WorkingCapitalLoan loan;

    @BeforeEach
    void setUp() {
        underTest = new WorkingCapitalLoanBreachResetServiceImpl(breachScheduleService,
                new WorkingCapitalLoanActiveBreachResetResolver(breachActionRepository));
        loan = new WorkingCapitalLoan();
        loan.setId(LOAN_ID);
    }

    private WorkingCapitalLoanBreachAction action(final long id, final WorkingCapitalLoanBreachActionType type, final LocalDate startDate) {
        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setId(id);
        action.setAction(type);
        action.setStartDate(startDate);
        return action;
    }

    private WorkingCapitalLoanBreachAction reset(final long id, final LocalDate date) {
        return action(id, WorkingCapitalLoanBreachActionType.RESET, date);
    }

    private WorkingCapitalLoanBreachAction restartReset(final long id, final LocalDate date) {
        final WorkingCapitalLoanBreachAction reset = reset(id, date);
        reset.setRestartPeriodFromResetDate(true);
        return reset;
    }

    private WorkingCapitalLoanBreachAction undo(final long id, final LocalDate date) {
        return action(id, WorkingCapitalLoanBreachActionType.UNDO_RESET, date);
    }

    private void assertFlagOnlyPath() {
        final InOrder inOrder = inOrder(breachScheduleService);
        inOrder.verify(breachScheduleService).applyActiveResetFlags(loan);
        inOrder.verify(breachScheduleService).recalculatePastDueAmount(loan);
        verify(breachScheduleService, never()).splitPeriodAtReset(any(), any());
        verify(breachScheduleService, never()).restoreSplitPeriod(any(), any());
        verify(breachScheduleService, never()).reprocessBreachSchedule(any());
    }

    @Test
    void resetBreach_ofAPlainReset_derivesTheFlagsAndRecalculatesThePastDueAmount() {
        underTest.resetBreach(loan, reset(1L, LocalDate.of(2026, 4, 15)));

        assertFlagOnlyPath();
    }

    @Test
    void resetBreach_withRestartPeriodOption_splitsThenReprocesses() {
        final LocalDate resetDate = LocalDate.of(2026, 4, 15);

        underTest.resetBreach(loan, restartReset(1L, resetDate));

        final InOrder inOrder = inOrder(breachScheduleService);
        inOrder.verify(breachScheduleService).splitPeriodAtReset(loan, resetDate);
        inOrder.verify(breachScheduleService).reprocessBreachSchedule(loan);
        verify(breachScheduleService, never()).applyActiveResetFlags(any());
        verify(breachScheduleService, never()).recalculatePastDueAmount(any());
    }

    @Test
    void undoResetBreach_ofAPlainReset_derivesTheFlagsAndRecalculatesThePastDueAmount() {
        underTest.undoResetBreach(loan, undo(2L, LocalDate.of(2026, 4, 20)), List.of(reset(1L, LocalDate.of(2026, 4, 15))));

        assertFlagOnlyPath();
    }

    @Test
    void undoResetBreach_ofARestartReset_restoresTheSplitThenReprocesses() {
        final WorkingCapitalLoanBreachAction undoneReset = restartReset(1L, LocalDate.of(2026, 4, 15));

        underTest.undoResetBreach(loan, undo(2L, LocalDate.of(2026, 4, 15)), List.of(undoneReset));

        final InOrder inOrder = inOrder(breachScheduleService);
        inOrder.verify(breachScheduleService).restoreSplitPeriod(loan, undoneReset);
        inOrder.verify(breachScheduleService).reprocessBreachSchedule(loan);
        verify(breachScheduleService, never()).applyActiveResetFlags(any());
        verify(breachScheduleService, never()).recalculatePastDueAmount(any());
    }

    @Test
    void undoResetBreach_undoesTheLatestActiveReset() {
        final WorkingCapitalLoanBreachAction earlierRestartReset = restartReset(1L, LocalDate.of(2026, 2, 20));
        final WorkingCapitalLoanBreachAction latestReset = reset(2L, LocalDate.of(2026, 4, 15));

        underTest.undoResetBreach(loan, undo(3L, LocalDate.of(2026, 4, 15)), List.of(earlierRestartReset, latestReset));

        assertFlagOnlyPath();
    }

    @Test
    void undoResetBreach_skipsAnAlreadyUndoneReset() {
        final WorkingCapitalLoanBreachAction restartReset = restartReset(1L, LocalDate.of(2026, 2, 20));

        underTest.undoResetBreach(loan, undo(4L, LocalDate.of(2026, 4, 20)),
                List.of(restartReset, reset(2L, LocalDate.of(2026, 4, 15)), undo(3L, LocalDate.of(2026, 4, 16))));

        verify(breachScheduleService).restoreSplitPeriod(loan, restartReset);
        verify(breachScheduleService).reprocessBreachSchedule(loan);
    }

    @Test
    void undoResetBreach_withoutAnActiveReset_onlyDerivesTheFlagsAndRecalculatesThePastDueAmount() {
        underTest.undoResetBreach(loan, undo(1L, LocalDate.of(2026, 4, 20)), List.of());

        assertFlagOnlyPath();
    }

    @Test
    void actionsWithoutADate_leaveTheScheduleAlone() {
        underTest.resetBreach(loan, reset(1L, null));
        underTest.undoResetBreach(loan, undo(2L, null), List.of(reset(1L, LocalDate.of(2026, 4, 15))));

        verifyNoInteractions(breachScheduleService);
    }
}
