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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.ProjectedAmortizationLoanModelRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The version stamped on a stored model is only worth writing if something reads it. These cover that it is read, that
 * the answer is the version now in force, and that a rebuild which cannot be made does not take its caller down.
 */
@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanModelProcessingServiceTest {

    private static final Long LOAN_ID = 4711L;

    @Mock
    private ProjectedAmortizationLoanModelRepository modelRepository;
    @Mock
    private WorkingCapitalLoanRepository loanRepository;
    @Mock
    private WorkingCapitalLoanAmortizationScheduleWriteService scheduleWriteService;
    @Mock
    private WorkingCapitalLoan loan;

    @InjectMocks
    private WorkingCapitalLoanModelProcessingService service;

    @Test
    void aModelStampedWithAnOlderVersionIsReportedAsNeedingARebuild() {
        when(modelRepository.existsByLoanIdAndJsonModelVersionNot(LOAN_ID, ProjectedAmortizationScheduleModel.getModelVersion()))
                .thenReturn(true);

        assertTrue(service.requiresModelRecalculation(LOAN_ID), "a model written by an older version must be rebuilt before use");
    }

    @Test
    void aModelStampedWithTheCurrentVersionIsLeftAlone() {
        when(modelRepository.existsByLoanIdAndJsonModelVersionNot(anyLong(), anyString())).thenReturn(false);

        assertFalse(service.requiresModelRecalculation(LOAN_ID), "nothing to do for a model already at the current version");
    }

    @Test
    void theBatchQuestionIsAskedAgainstTheVersionNowInForceAndOnlyForRebuildableStatuses() {
        when(modelRepository.findLoanIdsRequiringModelRecalculation(any(), any(), anyString())).thenReturn(List.of(LOAN_ID));

        assertEquals(List.of(LOAN_ID), service.findLoanIdsRequiringModelRecalculation(List.of(LOAN_ID, 99L)));

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<LoanStatus>> statuses = ArgumentCaptor.forClass(List.class);
        verify(modelRepository).findLoanIdsRequiringModelRecalculation(any(), statuses.capture(),
                eq(ProjectedAmortizationScheduleModel.getModelVersion()));
        assertTrue(statuses.getValue().contains(LoanStatus.ACTIVE), "an active loan can be replayed");
        assertTrue(statuses.getValue().contains(LoanStatus.CLOSED_OBLIGATIONS_MET), "so can one that has been repaid");
        assertTrue(statuses.getValue().contains(LoanStatus.OVERPAID), "and one that was overpaid");
        assertFalse(statuses.getValue().contains(LoanStatus.APPROVED), "but a loan with no schedule yet has nothing to replay");
    }

    @Test
    void anEmptyBatchAsksTheDatabaseNothing() {
        assertEquals(List.of(), service.findLoanIdsRequiringModelRecalculation(List.of()));
        verify(modelRepository, never()).findLoanIdsRequiringModelRecalculation(any(), any(), anyString());
    }

    @Test
    void aRebuildReplaysTheLoansRecordedHistory() {
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));

        service.recalculateModelAndSave(LOAN_ID);

        verify(scheduleWriteService).rebuildScheduleModelFromRecordedHistory(loan);
    }

    /**
     * The rebuild is a repair. A loan whose history no longer replays - a rate that stopped solving, say - keeps its
     * stale model, and the batch that swept it up carries on to the others.
     */
    @Test
    void aRebuildThatCannotBeMadeDoesNotFailTheCaller() {
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(loan));
        doThrow(new IllegalStateException("EIR no longer solvable")).when(scheduleWriteService)
                .rebuildScheduleModelFromRecordedHistory(loan);

        service.recalculateModelAndSave(LOAN_ID);
    }

    @Test
    void aLoanThatHasGoneAwayIsNotRebuilt() {
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

        service.recalculateModelAndSave(LOAN_ID);

        verify(scheduleWriteService, never()).rebuildScheduleModelFromRecordedHistory(any());
    }
}
