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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanMapper;
import org.apache.fineract.portfolio.loanaccount.service.schedule.LoanScheduleComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Which way {@link LoanScheduleService#regenerateScheduleForReprocessing} regenerates the schedule that the
 * transactions are replayed over.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanScheduleServiceTest {

    @Mock
    private LoanChargeService loanChargeService;
    @Mock
    private ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    @Mock
    private LoanMapper loanMapper;
    @Mock
    private LoanTransactionProcessingService loanTransactionProcessingService;
    @Mock
    private LoanScheduleComponent loanSchedule;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private ILoanUtilService loanUtilService;
    @Mock
    private ScheduleGeneratorDTO generatorDTO;
    @Mock
    private LoanScheduleModel scheduleModel;
    @Mock
    private Loan loan;

    @InjectMocks
    private LoanScheduleService service;

    @BeforeEach
    void setUp() {
        when(loan.isInterestBearingAndInterestRecalculationEnabled()).thenReturn(true);
        when(loan.isProgressiveSchedule()).thenReturn(true);
        when(loan.getActiveCharges()).thenReturn(Set.of());
        when(loanMapper.regenerateScheduleModel(generatorDTO, loan)).thenReturn(scheduleModel);
    }

    @Test
    void progressiveLoanWithInterestRecalculationIsRegeneratedWithInterestRecalculation() {
        service.regenerateScheduleForReprocessing(loan, generatorDTO);

        verify(loanTransactionProcessingService).getRecalculatedSchedule(generatorDTO, loan);
        verify(loanMapper, never()).regenerateScheduleModel(any(), any());
    }

    /**
     * The interest recalculation variant regenerates nothing for a non performing loan, which would leave the replay
     * with the installments as they are stored, re-aged ones included.
     */
    @Test
    void progressiveNonPerformingLoanIsRegeneratedFromItsTerms() {
        when(loan.isNpa()).thenReturn(true);

        service.regenerateScheduleForReprocessing(loan, generatorDTO);

        verify(loanMapper).regenerateScheduleModel(generatorDTO, loan);
        verify(loanSchedule).updateLoanSchedule(loan, scheduleModel);
        verify(loanTransactionProcessingService, never()).getRecalculatedSchedule(any(), any());
    }

    @Test
    void progressiveChargedOffLoanIsRegeneratedFromItsTerms() {
        when(loan.isChargedOff()).thenReturn(true);

        service.regenerateScheduleForReprocessing(loan, generatorDTO);

        verify(loanMapper).regenerateScheduleModel(generatorDTO, loan);
        verify(loanTransactionProcessingService, never()).getRecalculatedSchedule(any(), any());
    }

    @Test
    void cumulativeNonPerformingLoanKeepsTheInterestRecalculationVariant() {
        when(loan.isProgressiveSchedule()).thenReturn(false);
        when(loan.isNpa()).thenReturn(true);

        service.regenerateScheduleForReprocessing(loan, generatorDTO);

        verify(loanTransactionProcessingService).getRecalculatedSchedule(generatorDTO, loan);
        verify(loanMapper, never()).regenerateScheduleModel(any(), any());
    }
}
