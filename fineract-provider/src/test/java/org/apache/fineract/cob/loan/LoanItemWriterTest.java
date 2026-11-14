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
package org.apache.fineract.cob.loan;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanModelProcessingService;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
class LoanItemWriterTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 10, 6);
    private static final TreeMap<Long, String> BUSINESS_STEP_MAP = new TreeMap<>(Map.of(1L, "firstStep"));

    @Mock
    private COBBusinessStepService cobBusinessStepService;
    @Mock
    private ProgressiveLoanModelProcessingService modelProcessingService;
    @Mock
    private LockingService lockingService;
    @Mock
    private LoanRepository loanRepository;

    @Test
    void loadsEachLoanRunsItsBusinessStepsAndReleasesTheLocksOnlyAfterFlushing() throws Exception {
        LoanItemWriter writer = stepScopedWriter();
        Loan fresh1 = mock(Loan.class);
        Loan fresh2 = mock(Loan.class);
        Loan processed1 = loanWithId(1L);
        Loan processed2 = loanWithId(2L);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(fresh1));
        when(loanRepository.findById(2L)).thenReturn(Optional.of(fresh2));
        when(cobBusinessStepService.run(BUSINESS_STEP_MAP, fresh1)).thenReturn(processed1);
        when(cobBusinessStepService.run(BUSINESS_STEP_MAP, fresh2)).thenReturn(processed2);

        writer.write(new Chunk<>(1L, 2L));

        verify(processed1).setLastClosedBusinessDate(BUSINESS_DATE);
        verify(processed2).setLastClosedBusinessDate(BUSINESS_DATE);
        InOrder inOrder = inOrder(loanRepository, lockingService);
        inOrder.verify(loanRepository).flush();
        inOrder.verify(lockingService).deleteByLoanIdInAndLockOwner(List.of(1L, 2L), LockOwner.LOAN_COB_CHUNK_PROCESSING);
        // The loans are managed by the chunk transaction: flushing writes them, a save would only merge them again.
        verify(loanRepository, never()).saveAll(anyList());
    }

    @Test
    void aFailingLoanStopsTheWriteBeforeAnythingIsSavedOrUnlocked() throws Exception {
        LoanItemWriter writer = stepScopedWriter();
        Loan loan1 = loanWithId(1L);
        Loan loan2 = loanWithId(2L);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan1));
        when(loanRepository.findById(2L)).thenReturn(Optional.of(loan2));
        when(cobBusinessStepService.run(BUSINESS_STEP_MAP, loan1)).thenReturn(loan1);
        when(cobBusinessStepService.run(BUSINESS_STEP_MAP, loan2)).thenThrow(new BusinessStepException("step failed"));

        assertThrows(BusinessStepException.class, () -> writer.write(new Chunk<>(1L, 2L)));

        verify(loanRepository, never()).saveAll(anyList());
        verify(loanRepository, never()).flush();
        verifyNoInteractions(lockingService);
    }

    @Test
    void aLoanThatNoLongerExistsFailsTheWrite() {
        LoanItemWriter writer = stepScopedWriter();
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessStepException.class, () -> writer.write(new Chunk<>(1L)));

        verifyNoInteractions(cobBusinessStepService, lockingService);
    }

    @Test
    void rebuildsAnOutdatedProgressiveModelBeforeTheBusinessSteps() throws Exception {
        LoanItemWriter writer = stepScopedWriter();
        Loan loan = loanWithId(1L);
        when(loan.isProgressiveSchedule()).thenReturn(true);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(modelProcessingService.hasValidModel(1L, ProgressiveLoanInterestScheduleModel.getModelVersion())).thenReturn(false);
        when(cobBusinessStepService.run(BUSINESS_STEP_MAP, loan)).thenReturn(loan);

        writer.write(new Chunk<>(1L));

        InOrder inOrder = inOrder(modelProcessingService, cobBusinessStepService);
        inOrder.verify(modelProcessingService).recalculateModelAndSave(loan);
        inOrder.verify(cobBusinessStepService).run(BUSINESS_STEP_MAP, loan);
    }

    @Test
    void anEmptyChunkTouchesNothing() throws Exception {
        stepScopedWriter().write(new Chunk<Long>());

        verifyNoInteractions(cobBusinessStepService, loanRepository, lockingService);
    }

    @Test
    void failsWhenTheExecutionContextHasNoBusinessSteps() {
        LoanItemWriter writer = new LoanItemWriter(cobBusinessStepService, modelProcessingService, lockingService, loanRepository);
        writer.beforeStep(stepExecution(new ExecutionContext()));

        assertThrows(IllegalStateException.class, () -> writer.write(new Chunk<>(1L)));
    }

    @Test
    void inlineWriterReadsTheJobContextAndReleasesInlineLocks() throws Exception {
        InlineCOBLoanItemWriter writer = new InlineCOBLoanItemWriter(cobBusinessStepService, modelProcessingService, lockingService,
                loanRepository);
        StepExecution stepExecution = stepExecution(new ExecutionContext());
        stepExecution.getJobExecution().getExecutionContext().put(COBConstant.BUSINESS_STEPS, businessSteps());
        writer.beforeStep(stepExecution);
        Loan loan = loanWithId(1L);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(cobBusinessStepService.run(BUSINESS_STEP_MAP, loan)).thenReturn(loan);

        writer.write(new Chunk<>(1L));

        verify(loan).setLastClosedBusinessDate(BUSINESS_DATE);
        verify(lockingService).deleteByLoanIdInAndLockOwner(List.of(1L), LockOwner.LOAN_INLINE_COB_PROCESSING);
    }

    private LoanItemWriter stepScopedWriter() {
        LoanItemWriter writer = new LoanItemWriter(cobBusinessStepService, modelProcessingService, lockingService, loanRepository);
        ExecutionContext stepContext = new ExecutionContext();
        stepContext.put(COBConstant.BUSINESS_STEPS, businessSteps());
        writer.beforeStep(stepExecution(stepContext));
        return writer;
    }

    private static Set<BusinessStepNameAndOrder> businessSteps() {
        return Set.of(new BusinessStepNameAndOrder("firstStep", 1L));
    }

    private static StepExecution stepExecution(ExecutionContext stepContext) {
        JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters());
        jobExecution.getExecutionContext().put(COBConstant.BUSINESS_DATE_PARAMETER_NAME, BUSINESS_DATE);
        StepExecution stepExecution = new StepExecution("test", jobExecution);
        stepExecution.setExecutionContext(stepContext);
        return stepExecution;
    }

    private static Loan loanWithId(Long id) {
        Loan loan = mock(Loan.class);
        lenient().when(loan.getId()).thenReturn(id);
        return loan;
    }
}
