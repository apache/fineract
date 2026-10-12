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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanBreachResetServiceImplTest {

    private static final Long LOAN_ID = 1L;

    @Mock
    private WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;

    @Mock
    private WorkingCapitalLoanBreachScheduleService breachScheduleService;

    private WorkingCapitalLoanBreachResetServiceImpl underTest;

    private WorkingCapitalLoan loan;

    @BeforeEach
    void setUp() {
        underTest = new WorkingCapitalLoanBreachResetServiceImpl(breachScheduleRepository, breachScheduleService);
        loan = new WorkingCapitalLoan();
        loan.setId(LOAN_ID);
    }

    @Test
    void resetBreach_setsResetFlagOnActionDatePeriodAndKeepsValues() {
        final WorkingCapitalLoanBreachAction resetAction = new WorkingCapitalLoanBreachAction();
        resetAction.setAction(WorkingCapitalLoanBreachActionType.RESET);
        resetAction.setStartDate(LocalDate.of(2026, 4, 15));

        final WorkingCapitalLoanBreachSchedule pastPeriod = new WorkingCapitalLoanBreachSchedule();
        pastPeriod.setLoan(loan);
        pastPeriod.setPeriodNumber(1);
        pastPeriod.setFromDate(LocalDate.of(2026, 1, 1));
        pastPeriod.setToDate(LocalDate.of(2026, 3, 1));

        final WorkingCapitalLoanBreachSchedule actionDatePeriod = new WorkingCapitalLoanBreachSchedule();
        actionDatePeriod.setLoan(loan);
        actionDatePeriod.setPeriodNumber(2);
        actionDatePeriod.setFromDate(LocalDate.of(2026, 4, 1));
        actionDatePeriod.setToDate(LocalDate.of(2026, 4, 30));
        actionDatePeriod.setMinPaymentAmount(BigDecimal.valueOf(100));
        actionDatePeriod.setPaidAmount(BigDecimal.valueOf(40));
        actionDatePeriod.setOutstandingAmount(BigDecimal.valueOf(60));
        actionDatePeriod.setBreach(Boolean.TRUE);
        actionDatePeriod.setNearBreach(Boolean.FALSE);

        final WorkingCapitalLoanBreachSchedule futurePeriod = new WorkingCapitalLoanBreachSchedule();
        futurePeriod.setLoan(loan);
        futurePeriod.setPeriodNumber(3);
        futurePeriod.setFromDate(LocalDate.of(2026, 5, 1));
        futurePeriod.setToDate(LocalDate.of(2026, 5, 31));

        when(breachScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, resetAction.getStartDate(),
                resetAction.getStartDate())).thenReturn(Optional.of(actionDatePeriod));

        underTest.resetBreach(loan, resetAction);

        assertFalse(pastPeriod.isReset());
        assertTrue(actionDatePeriod.isReset());
        assertFalse(futurePeriod.isReset());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(actionDatePeriod.getMinPaymentAmount()));
        assertEquals(0, BigDecimal.valueOf(40).compareTo(actionDatePeriod.getPaidAmount()));
        assertEquals(0, BigDecimal.valueOf(60).compareTo(actionDatePeriod.getOutstandingAmount()));
        assertEquals(Boolean.TRUE, actionDatePeriod.getBreach());
        assertEquals(Boolean.FALSE, actionDatePeriod.getNearBreach());
        verify(breachScheduleService).recalculatePastDueAmount(loan);
    }

    @Test
    void undoResetBreach_liftsResetFlagOnActionDatePeriodOnly() {
        final WorkingCapitalLoanBreachAction undoResetAction = new WorkingCapitalLoanBreachAction();
        undoResetAction.setAction(WorkingCapitalLoanBreachActionType.UNDO_RESET);
        undoResetAction.setStartDate(LocalDate.of(2026, 4, 15));

        final WorkingCapitalLoanBreachSchedule pastPeriod = new WorkingCapitalLoanBreachSchedule();
        pastPeriod.setLoan(loan);
        pastPeriod.setPeriodNumber(1);
        pastPeriod.setFromDate(LocalDate.of(2026, 1, 1));
        pastPeriod.setToDate(LocalDate.of(2026, 3, 1));
        pastPeriod.setReset(true);

        final WorkingCapitalLoanBreachSchedule actionDatePeriod = new WorkingCapitalLoanBreachSchedule();
        actionDatePeriod.setLoan(loan);
        actionDatePeriod.setPeriodNumber(2);
        actionDatePeriod.setFromDate(LocalDate.of(2026, 4, 1));
        actionDatePeriod.setToDate(LocalDate.of(2026, 4, 30));
        actionDatePeriod.setMinPaymentAmount(BigDecimal.valueOf(100));
        actionDatePeriod.setPaidAmount(BigDecimal.valueOf(40));
        actionDatePeriod.setOutstandingAmount(BigDecimal.valueOf(60));
        actionDatePeriod.setBreach(Boolean.TRUE);
        actionDatePeriod.setNearBreach(Boolean.FALSE);
        actionDatePeriod.setReset(true);

        final WorkingCapitalLoanBreachSchedule futurePeriod = new WorkingCapitalLoanBreachSchedule();
        futurePeriod.setLoan(loan);
        futurePeriod.setPeriodNumber(3);
        futurePeriod.setFromDate(LocalDate.of(2026, 5, 1));
        futurePeriod.setToDate(LocalDate.of(2026, 5, 31));
        futurePeriod.setReset(true);

        when(breachScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, undoResetAction.getStartDate(),
                undoResetAction.getStartDate())).thenReturn(Optional.of(actionDatePeriod));

        underTest.undoResetBreach(loan, undoResetAction);

        assertTrue(pastPeriod.isReset());
        assertFalse(actionDatePeriod.isReset());
        assertTrue(futurePeriod.isReset());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(actionDatePeriod.getMinPaymentAmount()));
        assertEquals(0, BigDecimal.valueOf(40).compareTo(actionDatePeriod.getPaidAmount()));
        assertEquals(0, BigDecimal.valueOf(60).compareTo(actionDatePeriod.getOutstandingAmount()));
        assertEquals(Boolean.TRUE, actionDatePeriod.getBreach());
        assertEquals(Boolean.FALSE, actionDatePeriod.getNearBreach());
        verify(breachScheduleService).recalculatePastDueAmount(loan);
        verify(breachScheduleService, never()).reprocessBreachSchedule(loan);
    }
}
