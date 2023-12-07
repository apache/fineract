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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.data.LoanCOBParameter;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
class LoanItemReaderTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RetrieveLoanIdService retrieveLoanIdService;

    @Mock
    private CustomJobParameterResolver customJobParameterResolver;

    @Mock
    private LoanLockingService loanLockingService;

    @Mock
    private StepExecution stepExecution;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private Loan loan;

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testLoanItemReaderSimple() throws Exception {
        // given
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "test", "UTC", null));
        LoanItemReader loanItemReader = new LoanItemReader(loanRepository, retrieveLoanIdService, customJobParameterResolver,
                loanLockingService);
        when(stepExecution.getExecutionContext()).thenReturn(executionContext);
        LoanCOBParameter loanCOBParameter = new LoanCOBParameter(1L, 5L);
        when(executionContext.get(LoanCOBConstant.LOAN_COB_PARAMETER)).thenReturn(loanCOBParameter);
        when(retrieveLoanIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, false))
                .thenReturn(new ArrayList<>(List.of(1L, 2L, 3L, 4L, 5L)));
        List<LoanAccountLock> accountLocks = List.of(1L, 2L, 3L, 4L, 5L).stream()
                .map(l -> new LoanAccountLock(l, LockOwner.LOAN_COB_CHUNK_PROCESSING, LocalDate.of(2023, 7, 25))).toList();
        when(loanLockingService.findAllByLoanIdInAndLockOwner(List.of(1L, 2L, 3L, 4L, 5L), LockOwner.LOAN_COB_CHUNK_PROCESSING))
                .thenReturn(accountLocks);
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));

        // when + then
        loanItemReader.beforeStep(stepExecution);
        for (long i = 1; i <= 5; i++) {
            Loan myLoan = loanItemReader.read();
            Assertions.assertEquals(loan, myLoan);
            verify(loanRepository, times(1)).findById(i);
        }

        Mockito.verifyNoMoreInteractions(loanRepository);
    }

    @Test
    public void testLoanItemReadNoOpenLoansFound() throws Exception {
        // given
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "test", "UTC", null));
        LoanItemReader loanItemReader = new LoanItemReader(loanRepository, retrieveLoanIdService, customJobParameterResolver,
                loanLockingService);
        when(stepExecution.getExecutionContext()).thenReturn(executionContext);
        LoanCOBParameter loanCOBParameter = new LoanCOBParameter(1L, 5L);
        when(executionContext.get(LoanCOBConstant.LOAN_COB_PARAMETER)).thenReturn(loanCOBParameter);
        when(retrieveLoanIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, false))
                .thenReturn(new ArrayList<>(List.of()));

        // when + then
        loanItemReader.beforeStep(stepExecution);
        Loan myLoan = loanItemReader.read();
        Assertions.assertNull(myLoan);

        Mockito.verifyNoMoreInteractions(loanRepository);
        Mockito.verifyNoInteractions(loanLockingService);
    }

    @Test
    public void testLoanItemReaderMultiThreadRead() throws Exception {
        // given
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "test", "UTC", null));
        LoanItemReader loanItemReader = new LoanItemReader(loanRepository, retrieveLoanIdService, customJobParameterResolver,
                loanLockingService);
        when(stepExecution.getExecutionContext()).thenReturn(executionContext);
        LoanCOBParameter loanCOBParameter = new LoanCOBParameter(1L, 100L);
        when(executionContext.get(LoanCOBConstant.LOAN_COB_PARAMETER)).thenReturn(loanCOBParameter);
        when(retrieveLoanIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, false))
                .thenReturn(new ArrayList<>(IntStream.rangeClosed(1, 100).boxed().map(Long::valueOf).toList()));
        List<LoanAccountLock> accountLocks = IntStream.rangeClosed(1, 100).boxed().map(Long::valueOf)
                .map(l -> new LoanAccountLock(l, LockOwner.LOAN_COB_CHUNK_PROCESSING, LocalDate.of(2023, 7, 25))).toList();
        when(loanLockingService.findAllByLoanIdInAndLockOwner(IntStream.rangeClosed(1, 100).boxed().map(Long::valueOf).toList(),
                LockOwner.LOAN_COB_CHUNK_PROCESSING)).thenReturn(accountLocks);
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(loan));

        // when + then
        loanItemReader.beforeStep(stepExecution);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 1; i <= 100; i++) {
            Future<?> notUsed = executorService.submit(() -> {
                try {
                    Loan myLoan = loanItemReader.read();
                    Assertions.assertEquals(loan, myLoan);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executorService.shutdown();
        boolean b = executorService.awaitTermination(5L, TimeUnit.SECONDS);
        Assertions.assertTrue(b, "Executor did not terminate successfully");

        // verify that this was called 100times, and for each loan it was called exactly once
        for (long i = 1; i <= 100; i++) {
            verify(loanRepository, times(1)).findById(i);
        }

        Mockito.verifyNoMoreInteractions(loanRepository);
    }
}
