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

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.cob.service.RetrieveIdService;
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
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
class LoanItemReaderTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RetrieveIdService retrieveIdService;

    @Mock
    private LockingService loanLockingService;

    @Mock
    private StepExecution stepExecution;
    @Mock
    private JobExecution jobExecution;

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
        LoanItemReader loanItemReader = new LoanItemReader(new BeforeStepLockingItemReaderHelper(retrieveIdService, loanLockingService));
        when(stepExecution.getExecutionContext()).thenReturn(executionContext);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        COBParameter loanCOBParameter = new COBParameter(1L, 5L);
        when(executionContext.get(LoanCOBConstant.COB_PARAMETER)).thenReturn(loanCOBParameter);
        when(retrieveIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, false))
                .thenReturn(new ArrayList<>(List.of(1L, 2L, 3L, 4L, 5L)));
        List<Long> accountLocks = Stream.of(1L, 2L, 3L, 4L, 5L).toList();
        when(loanLockingService.findLockIdsByLoanIdInAndLockOwner(List.of(1L, 2L, 3L, 4L, 5L), LockOwner.LOAN_COB_CHUNK_PROCESSING))
                .thenReturn(accountLocks);

        // when + then
        loanItemReader.beforeStep(stepExecution);
        for (long i = 1; i <= 5; i++) {
            Assertions.assertEquals(i, loanItemReader.read());
        }

        // Reading hands out ids only; the writer loads each loan inside the write.
        Mockito.verifyNoInteractions(loanRepository);
    }

    @Test
    public void testLoanItemReadNoOpenLoansFound() throws Exception {
        // given
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "test", "UTC", null));
        LoanItemReader loanItemReader = new LoanItemReader(new BeforeStepLockingItemReaderHelper(retrieveIdService, loanLockingService));
        when(stepExecution.getExecutionContext()).thenReturn(executionContext);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        COBParameter loanCOBParameter = new COBParameter(1L, 5L);
        when(executionContext.get(LoanCOBConstant.COB_PARAMETER)).thenReturn(loanCOBParameter);
        when(retrieveIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, false))
                .thenReturn(new ArrayList<>(List.of()));

        // when + then
        loanItemReader.beforeStep(stepExecution);
        Assertions.assertNull(loanItemReader.read());

        Mockito.verifyNoInteractions(loanRepository);
        Mockito.verifyNoInteractions(loanLockingService);
    }

    @Test
    public void testLoanItemReaderMultiThreadRead() throws Exception {
        // given
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "test", "UTC", null));
        LoanItemReader loanItemReader = new LoanItemReader(new BeforeStepLockingItemReaderHelper(retrieveIdService, loanLockingService));
        when(stepExecution.getExecutionContext()).thenReturn(executionContext);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);
        COBParameter loanCOBParameter = new COBParameter(1L, 100L);
        when(executionContext.get(LoanCOBConstant.COB_PARAMETER)).thenReturn(loanCOBParameter);
        when(retrieveIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter, false))
                .thenReturn(new ArrayList<>(IntStream.rangeClosed(1, 100).boxed().map(Long::valueOf).toList()));
        List<Long> accountLocks = IntStream.rangeClosed(1, 100).boxed().map(Long::valueOf).toList();
        when(loanLockingService.findLockIdsByLoanIdInAndLockOwner(IntStream.rangeClosed(1, 100).boxed().map(Long::valueOf).toList(),
                LockOwner.LOAN_COB_CHUNK_PROCESSING)).thenReturn(accountLocks);

        // when + then
        loanItemReader.beforeStep(stepExecution);
        Set<Long> readIds = ConcurrentHashMap.newKeySet();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 1; i <= 100; i++) {
            Future<?> notUsed = executorService.submit(() -> {
                Long id = loanItemReader.read();
                Assertions.assertNotNull(id);
                Assertions.assertTrue(readIds.add(id), "id " + id + " was read twice");
            });
        }
        executorService.shutdown();
        boolean b = executorService.awaitTermination(5L, TimeUnit.SECONDS);
        Assertions.assertTrue(b, "Executor did not terminate successfully");

        // every id was handed out exactly once, and none of them was loaded by the reader
        Assertions.assertEquals(LongStream.rangeClosed(1, 100).boxed().collect(Collectors.toSet()), readIds);
        Mockito.verifyNoInteractions(loanRepository);
    }
}
