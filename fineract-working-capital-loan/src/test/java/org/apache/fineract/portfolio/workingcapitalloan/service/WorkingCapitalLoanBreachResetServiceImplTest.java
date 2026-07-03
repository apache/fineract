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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachResetHistoryRepository;
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
    private WorkingCapitalLoanBreachResetHistoryRepository breachResetHistoryRepository;

    @Mock
    private WorkingCapitalLoanBreachScheduleService breachScheduleService;

    @Mock
    private WorkingCapitalLoanActiveBreachResetResolver activeBreachResetResolver;

    private WorkingCapitalLoanBreachResetServiceImpl underTest;

    private WorkingCapitalLoan loan;

    @BeforeEach
    void setUp() {
        underTest = new WorkingCapitalLoanBreachResetServiceImpl(breachScheduleRepository, breachResetHistoryRepository,
                breachScheduleService, activeBreachResetResolver);
        loan = new WorkingCapitalLoan();
        loan.setId(LOAN_ID);
    }

    @Test
    void resetBreach_recalculatesPastDueAmountAfterResettingPeriods() {
        final WorkingCapitalLoanBreachAction resetAction = new WorkingCapitalLoanBreachAction();
        resetAction.setAction(WorkingCapitalLoanBreachActionType.RESET);
        resetAction.setStartDate(LocalDate.of(2026, 4, 15));

        final WorkingCapitalLoanBreachSchedule pastPeriod = new WorkingCapitalLoanBreachSchedule();
        pastPeriod.setLoan(loan);
        pastPeriod.setPeriodNumber(1);
        pastPeriod.setFromDate(LocalDate.of(2026, 1, 1));
        pastPeriod.setToDate(LocalDate.of(2026, 3, 1));

        when(breachScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(pastPeriod));

        underTest.resetBreach(loan, resetAction);

        verify(breachScheduleService).recalculatePastDueAmount(loan);
        verify(breachScheduleRepository).saveAllAndFlush(any());
    }
}
