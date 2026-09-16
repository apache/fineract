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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanActiveBreachResetResolverTest {

    private static final Long LOAN_ID = 1L;

    @Mock
    private WorkingCapitalLoanBreachActionRepository breachActionRepository;

    private WorkingCapitalLoanActiveBreachResetResolver underTest;

    @BeforeEach
    void setUp() {
        underTest = new WorkingCapitalLoanActiveBreachResetResolver(breachActionRepository);
    }

    private WorkingCapitalLoanBreachAction action(final long id, final WorkingCapitalLoanBreachActionType type, final LocalDate startDate) {
        final WorkingCapitalLoanBreachAction action = new WorkingCapitalLoanBreachAction();
        action.setId(id);
        action.setAction(type);
        action.setStartDate(startDate);
        return action;
    }

    private static List<Long> ids(final Deque<WorkingCapitalLoanBreachAction> stack) {
        return stack.stream().map(WorkingCapitalLoanBreachAction::getId).toList();
    }

    @Test
    void activeResets_theOnlyResetIsActive() {
        final Deque<WorkingCapitalLoanBreachAction> actual = underTest
                .activeResets(List.of(action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 15))));

        assertEquals(List.of(1L), ids(actual));
    }

    @Test
    void activeResets_anUndoCancelsTheOnlyReset() {
        final Deque<WorkingCapitalLoanBreachAction> actual = underTest
                .activeResets(List.of(action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 15)),
                        action(2L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 4, 20))));

        assertTrue(actual.isEmpty());
    }

    @Test
    void activeResets_stackedResetsAreAllActiveLatestOnTop() {
        final Deque<WorkingCapitalLoanBreachAction> actual = underTest
                .activeResets(List.of(action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 15)),
                        action(2L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 5, 20))));

        assertEquals(List.of(2L, 1L), ids(actual));
        assertEquals(2L, actual.peek().getId().longValue());
    }

    @Test
    void activeResets_anUndoCancelsTheLatestOfStackedResets() {
        final Deque<WorkingCapitalLoanBreachAction> actual = underTest
                .activeResets(List.of(action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 15)),
                        action(2L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 5, 20)),
                        action(3L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 5, 25))));

        assertEquals(List.of(1L), ids(actual));
    }

    @Test
    void activeResets_aSecondUndoCancelsTheEarlierReset() {
        final Deque<WorkingCapitalLoanBreachAction> actual = underTest
                .activeResets(List.of(action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 15)),
                        action(2L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 5, 20)),
                        action(3L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 5, 25)),
                        action(4L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 5, 30))));

        assertTrue(actual.isEmpty());
    }

    @Test
    void activeResets_pairsByRecordingOrderNotByDate() {
        final Deque<WorkingCapitalLoanBreachAction> actual = underTest
                .activeResets(List.of(action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 5, 20)),
                        action(2L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 5)),
                        action(3L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 4, 10))));

        assertEquals(List.of(1L), ids(actual));
    }

    @Test
    void activeResets_ignoresTheOtherActionTypesAndAnUndoWithoutAReset() {
        final Deque<WorkingCapitalLoanBreachAction> actual = underTest
                .activeResets(List.of(action(1L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 4, 1)),
                        action(2L, WorkingCapitalLoanBreachActionType.PAUSE, LocalDate.of(2026, 4, 5)),
                        action(3L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 15)),
                        action(4L, WorkingCapitalLoanBreachActionType.RESCHEDULE, LocalDate.of(2026, 4, 20))));

        assertEquals(List.of(3L), ids(actual));
    }

    @Test
    void findLatestActiveReset_replaysTheLoanActionsInRecordingOrder() {
        when(breachActionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID))
                .thenReturn(List.of(action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 15)),
                        action(2L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 5, 20)),
                        action(3L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 5, 25))));

        final Optional<WorkingCapitalLoanBreachAction> actual = underTest.findLatestActiveReset(LOAN_ID);

        assertTrue(actual.isPresent());
        assertEquals(1L, actual.get().getId().longValue());
        assertTrue(underTest.hasActiveReset(LOAN_ID));
    }

    @Test
    void findLatestActiveReset_returnsEmptyWhenEveryResetWasUndone() {
        when(breachActionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID))
                .thenReturn(List.of(action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 15)),
                        action(2L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 4, 20))));

        assertEquals(Optional.empty(), underTest.findLatestActiveReset(LOAN_ID));
        assertFalse(underTest.hasActiveReset(LOAN_ID));
    }

    @Test
    void existsActiveResetInPeriod_matchesOnlyTheRangeHoldingTheActiveResetDate() {
        when(breachActionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID))
                .thenReturn(List.of(action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 4, 15))));

        assertTrue(underTest.existsActiveResetInPeriod(LOAN_ID, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)));
        assertTrue(underTest.existsActiveResetInPeriod(LOAN_ID, LocalDate.of(2026, 4, 15), LocalDate.of(2026, 4, 15)));
        assertFalse(underTest.existsActiveResetInPeriod(LOAN_ID, LocalDate.of(2026, 4, 16), LocalDate.of(2026, 4, 30)));
    }

    @Test
    void existsActiveResetInPeriod_seesEveryActiveResetNotOnlyTheLatest() {
        when(breachActionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID)).thenReturn(List.of(
                action(1L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 5, 20)),
                action(2L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 3, 20)),
                action(3L, WorkingCapitalLoanBreachActionType.RESET, LocalDate.of(2026, 2, 10)),
                action(4L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 2, 11))));

        assertTrue(underTest.existsActiveResetInPeriod(LOAN_ID, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)));
        assertTrue(underTest.existsActiveResetInPeriod(LOAN_ID, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)));
        assertFalse(underTest.existsActiveResetInPeriod(LOAN_ID, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)));
    }
}
