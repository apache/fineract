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
package org.apache.fineract.cob.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import jakarta.persistence.OptimisticLockException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.apache.fineract.cob.data.COBIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.domain.AccountLockRepository;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.exceptions.AccountLockCannotBeOverruledException;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.commands.configuration.RetryConfigurationAssembler;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformInternalServerException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

class InlineLoanCOBExecutorServiceImplTest {

    @InjectMocks
    private InlineLoanCOBExecutorServiceImpl testObj;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private InlineLoanCOBExecutionDataParser dataParser;
    @Mock
    private RetrieveLoanIdService retrieveIdService;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private FineractProperties.FineractQueryProperties fineractQueryProperties;
    @Mock
    private FineractProperties.FineractApiProperties fineractApiProperties;
    @Mock
    private FineractProperties.FineractBodyItemSizeLimitProperties fineractBodyItemSizeLimitProperties;
    @Mock
    private LoanAccountLockRepository loanAccountLockRepository;
    @Mock
    private JobOperator jobOperator;
    @Mock
    private JobRegistry jobRegistry;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private CustomJobParameterRepository customJobParameterRepository;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private AppUser appUser;
    @Mock
    private RetryConfigurationAssembler retryConfigurationAssembler;

    @BeforeEach
    public void setUp() {
        // a single attempt by default, so tests that are not about retrying keep asserting one run
        stubInlineCobRetry(1);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    /**
     * Stubs the retry policy the executor asks for: any exception, no wait, {@code maxAttempts} attempts - matching
     * {@link org.apache.fineract.commands.configuration.RetryConfigurationAssembler#getRetryConfigurationForInlineCob()}.
     */
    private void stubInlineCobRetry(int maxAttempts) {
        RetryConfig retryConfig = RetryConfig.<Throwable>custom().maxAttempts(maxAttempts).waitDuration(Duration.ZERO)
                .retryOnException(ex -> true).build();
        when(retryConfigurationAssembler.getRetryConfigurationForInlineCob()).thenReturn(Retry.of("inlineCobExecutorTest", retryConfig));
    }

    @Test
    void shouldExceptionThrownIfLoanIsAlreadyLocked() {
        JsonCommand command = mock(JsonCommand.class);
        COBIdAndLastClosedBusinessDate loan = mock(COBIdAndLastClosedBusinessDate.class);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        doThrow(new AccountLockCannotBeOverruledException("")).when(transactionTemplate).executeWithoutResult(any());
        when(fineractProperties.getQuery()).thenReturn(fineractQueryProperties);
        when(fineractProperties.getApi()).thenReturn(fineractApiProperties);
        when(dataParser.parseExecution(any())).thenReturn(List.of(1L));
        when(fineractQueryProperties.getInClauseParameterSizeLimit()).thenReturn(65000);
        when(fineractApiProperties.getBodyItemSizeLimit()).thenReturn(fineractBodyItemSizeLimitProperties);
        when(fineractBodyItemSizeLimitProperties.getInlineLoanCob()).thenReturn(1000);
        when(retrieveIdService.retrieveLoanIdsBehindDateOrNull(any(), anyList())).thenReturn(List.of(loan));
        assertThrows(AccountLockCannotBeOverruledException.class, () -> testObj.executeInlineJob(command, "INLINE_LOAN_COB"));
    }

    @Test
    void shouldListBePartitioned() {
        JsonCommand command = mock(JsonCommand.class);
        COBIdAndLastClosedBusinessDate loan1 = mock(COBIdAndLastClosedBusinessDate.class);
        COBIdAndLastClosedBusinessDate loan2 = mock(COBIdAndLastClosedBusinessDate.class);
        COBIdAndLastClosedBusinessDate loan3 = mock(COBIdAndLastClosedBusinessDate.class);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        doThrow(new AccountLockCannotBeOverruledException("")).when(transactionTemplate).executeWithoutResult(any());
        when(fineractProperties.getQuery()).thenReturn(fineractQueryProperties);
        when(fineractProperties.getApi()).thenReturn(fineractApiProperties);
        when(dataParser.parseExecution(any())).thenReturn(List.of(1L, 2L, 3L));
        when(fineractQueryProperties.getInClauseParameterSizeLimit()).thenReturn(2);
        when(fineractApiProperties.getBodyItemSizeLimit()).thenReturn(fineractBodyItemSizeLimitProperties);
        when(fineractBodyItemSizeLimitProperties.getInlineLoanCob()).thenReturn(1000);
        when(retrieveIdService.retrieveLoanIdsBehindDateOrNull(any(), anyList())).thenReturn(List.of(loan1, loan2, loan3));
        assertThrows(AccountLockCannotBeOverruledException.class, () -> testObj.executeInlineJob(command, "INLINE_LOAN_COB"));
        verify(retrieveIdService, times(2)).retrieveLoanIdsBehindDateOrNull(any(), anyList());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void shouldRetryFailedBusinessDateFromTheOriginalBusinessDateContext() throws Exception {
        long loanId = 1L;
        LocalDate targetCobDate = LocalDate.of(2026, 8, 25);
        LocalDate firstExecutionDate = targetCobDate.minusDays(2);
        HashMap<BusinessDateType, LocalDate> requestBusinessDates = new HashMap<>();
        requestBusinessDates.put(BusinessDateType.COB_DATE, targetCobDate);
        requestBusinessDates.put(BusinessDateType.BUSINESS_DATE, targetCobDate.plusDays(1));
        HashMap<BusinessDateType, LocalDate> expectedBusinessDates = new HashMap<>(requestBusinessDates);
        ThreadLocalContextUtil.setBusinessDates(requestBusinessDates);
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);

        COBIdAndLastClosedBusinessDate initialLoanState = mock(COBIdAndLastClosedBusinessDate.class);
        when(initialLoanState.getId()).thenReturn(loanId);
        when(initialLoanState.getLastClosedBusinessDate()).thenReturn(firstExecutionDate.minusDays(1));
        COBIdAndLastClosedBusinessDate retryLoanState = mock(COBIdAndLastClosedBusinessDate.class);
        when(retryLoanState.getId()).thenReturn(loanId);
        when(retryLoanState.getLastClosedBusinessDate()).thenReturn(firstExecutionDate);
        when(retrieveIdService.retrieveLoanIdsBehindDateOrNull(eq(targetCobDate), anyList())).thenReturn(List.of(initialLoanState),
                List.of(retryLoanState));

        when(fineractProperties.getQuery()).thenReturn(fineractQueryProperties);
        when(fineractQueryProperties.getInClauseParameterSizeLimit()).thenReturn(65000);
        stubInlineCobRetry(2);

        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(mock(TransactionStatus.class));
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        AtomicReference<LoanAccountLock> storedLock = new AtomicReference<>();
        AccountLockRepository<LoanAccountLock> accountLockRepository = loanAccountLockRepository;
        when(accountLockRepository.findById(loanId)).thenAnswer(invocation -> Optional.ofNullable(storedLock.get()));
        when(accountLockRepository.saveAndFlush(any(LoanAccountLock.class))).thenAnswer(invocation -> {
            LoanAccountLock lock = invocation.getArgument(0);
            lock.setVersion(lock.getVersion() == null ? 0L : lock.getVersion() + 1);
            storedLock.set(lock);
            return lock;
        });
        when(loanAccountLockRepository.findAllByLoanIdInAndLockOwner(anyList(), eq(LockOwner.LOAN_INLINE_COB_PROCESSING)))
                .thenAnswer(invocation -> storedLock.get() == null ? List.of() : List.of(storedLock.get()));
        when(context.getAuthenticatedUserIfPresent()).thenReturn(appUser);
        when(appUser.isBypassUser()).thenReturn(false);

        Job inlineJob = mock(Job.class);
        when(inlineJob.getName()).thenReturn(LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME);
        when(jobRegistry.getJob(LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME)).thenReturn(inlineJob);
        AtomicLong customParameterId = new AtomicLong();
        List<LocalDate> executionDates = new ArrayList<>();
        when(customJobParameterRepository.save(any())).thenAnswer(invocation -> {
            Set<JobParameterDTO> parameters = invocation.getArgument(0);
            JobParameterDTO parameter = parameters.iterator().next();
            if (parameter.getParameterName().equals(org.apache.fineract.cob.COBConstant.BUSINESS_DATE_PARAMETER_NAME)) {
                executionDates.add(LocalDate.parse(parameter.getParameterValue()));
            }
            return customParameterId.incrementAndGet();
        });

        JobExecution completedExecution = mock(JobExecution.class);
        when(completedExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        JobExecution failedExecution = failedJobExecution(new OptimisticLockException("commit failed"));
        // day 1 passes, day 2 loses an optimistic-lock race, day 2's retry passes, day 3 passes
        List<JobExecution> executions = List.of(completedExecution, failedExecution, completedExecution, completedExecution);
        AtomicInteger executionIndex = new AtomicInteger();
        List<String> lockErrorsAtLaunch = new ArrayList<>();
        when(jobOperator.start(eq(inlineJob), any())).thenAnswer(invocation -> {
            assertEquals(expectedBusinessDates, ThreadLocalContextUtil.getBusinessDates());
            assertEquals(ActionContext.DEFAULT, ThreadLocalContextUtil.getActionContext());
            lockErrorsAtLaunch.add(storedLock.get() == null ? null : storedLock.get().getError());
            HashMap<BusinessDateType, LocalDate> mutatedBusinessDates = ThreadLocalContextUtil.getBusinessDates();
            mutatedBusinessDates.put(BusinessDateType.COB_DATE, executionDates.getLast());
            mutatedBusinessDates.put(BusinessDateType.BUSINESS_DATE, executionDates.getLast().plusDays(1));
            ThreadLocalContextUtil.setActionContext(ActionContext.COB);
            JobExecution execution = executions.get(executionIndex.getAndIncrement());
            if (BatchStatus.COMPLETED.equals(execution.getStatus())) {
                storedLock.set(null); // the real writer deletes this lock in the successful chunk transaction
            }
            return execution;
        });

        testObj.execute(List.of(loanId), LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME);

        // the failed business date is retried in place, so a single call walks the whole catch-up
        assertEquals(List.of(firstExecutionDate, firstExecutionDate.plusDays(1), firstExecutionDate.plusDays(1), targetCobDate),
                executionDates);
        assertEquals(4, executionIndex.get());
        // the retry took over the lock the failed attempt had stamped, having cleared that attempt's error off it
        assertTrue(lockErrorsAtLaunch.stream().noneMatch(error -> error != null),
                "stale error carried into an attempt: " + lockErrorsAtLaunch);
        assertEquals(expectedBusinessDates, ThreadLocalContextUtil.getBusinessDates());
        assertEquals(ActionContext.DEFAULT, ThreadLocalContextUtil.getActionContext());
        assertNull(storedLock.get());
    }

    private JobExecution failedJobExecution(Throwable stepFailure) {
        JobExecution jobExecution = mock(JobExecution.class);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);
        when(jobExecution.getFailureExceptions()).thenReturn(List.of());
        StepExecution stepExecution = mock(StepExecution.class);
        when(stepExecution.getFailureExceptions()).thenReturn(List.of(stepFailure));
        when(jobExecution.getStepExecutions()).thenReturn(List.of(stepExecution));
        return jobExecution;
    }

    /**
     * A single loan one business date behind, so execute(..) launches exactly one inline job. Returns the holder the
     * stubbed repository keeps the account lock in.
     */
    private AtomicReference<LoanAccountLock> givenLoanBehindByOneDay(long loanId, LocalDate cobDate, Job inlineJob) {
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.COB_DATE, cobDate);
        businessDates.put(BusinessDateType.BUSINESS_DATE, cobDate.plusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);

        COBIdAndLastClosedBusinessDate loanState = mock(COBIdAndLastClosedBusinessDate.class);
        when(loanState.getId()).thenReturn(loanId);
        when(loanState.getLastClosedBusinessDate()).thenReturn(cobDate.minusDays(1));
        when(retrieveIdService.retrieveLoanIdsBehindDateOrNull(eq(cobDate), anyList())).thenReturn(List.of(loanState));

        when(fineractProperties.getQuery()).thenReturn(fineractQueryProperties);
        when(fineractQueryProperties.getInClauseParameterSizeLimit()).thenReturn(65000);

        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(mock(TransactionStatus.class));
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        AtomicReference<LoanAccountLock> storedLock = new AtomicReference<>();
        // the executor holds the repository as AccountLockRepository; stub through that view to stay unambiguous
        AccountLockRepository<LoanAccountLock> accountLockRepository = loanAccountLockRepository;
        when(accountLockRepository.findById(loanId)).thenAnswer(invocation -> Optional.ofNullable(storedLock.get()));
        when(accountLockRepository.saveAndFlush(any(LoanAccountLock.class))).thenAnswer(invocation -> {
            LoanAccountLock lock = invocation.getArgument(0);
            lock.setVersion(lock.getVersion() == null ? 1L : lock.getVersion() + 1);
            storedLock.set(lock);
            return lock;
        });
        when(loanAccountLockRepository.findAllByLoanIdInAndLockOwner(anyList(), eq(LockOwner.LOAN_INLINE_COB_PROCESSING)))
                .thenAnswer(invocation -> storedLock.get() == null ? List.of() : List.of(storedLock.get()));
        when(context.getAuthenticatedUserIfPresent()).thenReturn(appUser);
        when(appUser.isBypassUser()).thenReturn(false);

        when(inlineJob.getName()).thenReturn(LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME);
        when(jobRegistry.getJob(LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME)).thenReturn(inlineJob);
        when(customJobParameterRepository.save(any())).thenReturn(1L);
        return storedLock;
    }

    @Test
    void shouldReportPlatformInternalServerExceptionWhenEveryAttemptFails() throws Exception {
        long loanId = 1L;
        LocalDate cobDate = LocalDate.of(2026, 8, 25);
        Job inlineJob = mock(Job.class);
        AtomicReference<LoanAccountLock> storedLock = givenLoanBehindByOneDay(loanId, cobDate, inlineJob);
        stubInlineCobRetry(2);

        // build the executions before stubbing: nesting mock creation inside when(..) leaves the stubbing unfinished
        JobExecution firstFailed = failedJobExecution(new OptimisticLockException("first attempt lost the race"));
        IllegalStateException lastFailure = new IllegalStateException("second attempt blew up");
        JobExecution secondFailed = failedJobExecution(lastFailure);
        when(jobOperator.start(eq(inlineJob), any())).thenReturn(firstFailed, secondFailed);

        PlatformInternalServerException thrown = assertThrows(PlatformInternalServerException.class,
                () -> testObj.execute(List.of(loanId), LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME));

        verify(jobOperator, times(2)).start(eq(inlineJob), any());
        assertEquals("error.msg.sheduler.job.execution.failed", thrown.getGlobalisationMessageCode());
        assertSame(lastFailure, thrown.getCause());
        // any failure is recorded, and it is the last attempt's failure that stays on the lock
        assertTrue(storedLock.get().getError().contains("Inline COB execution failed"));
        assertTrue(storedLock.get().getStacktrace().contains("second attempt blew up"));
    }

    @Test
    void shouldNotRecordFailureOnLocksWhileTheJobIsStillRunning() throws Exception {
        long loanId = 1L;
        LocalDate cobDate = LocalDate.of(2026, 8, 25);
        Job inlineJob = mock(Job.class);
        AtomicReference<LoanAccountLock> storedLock = givenLoanBehindByOneDay(loanId, cobDate, inlineJob);
        stubInlineCobRetry(1);

        JobExecution runningExecution = mock(JobExecution.class);
        when(runningExecution.getStatus()).thenReturn(BatchStatus.STARTED);
        when(runningExecution.getFailureExceptions()).thenReturn(List.of());
        when(runningExecution.getStepExecutions()).thenReturn(List.of());
        when(jobOperator.start(eq(inlineJob), any())).thenReturn(runningExecution);

        assertThrows(PlatformInternalServerException.class,
                () -> testObj.execute(List.of(loanId), LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME));

        // the execution is still writing to these locks, so they are left to it
        assertNull(storedLock.get().getError());
    }

    @Test
    void shouldOldestCloseBusinessDateReturnWithCorrectDate()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        COBIdAndLastClosedBusinessDate loan1 = mock(COBIdAndLastClosedBusinessDate.class);
        COBIdAndLastClosedBusinessDate loan2 = mock(COBIdAndLastClosedBusinessDate.class);
        COBIdAndLastClosedBusinessDate loan3 = mock(COBIdAndLastClosedBusinessDate.class);
        when(loan1.getLastClosedBusinessDate()).thenReturn(null);
        when(loan2.getLastClosedBusinessDate()).thenReturn(LocalDate.of(2023, 1, 10));
        when(loan3.getLastClosedBusinessDate()).thenReturn(LocalDate.of(2023, 1, 11));
        assertEquals(LocalDate.of(2023, 1, 10), getOldestCOBBusinessDate().invoke(testObj, List.of(loan1, loan2, loan3)));
    }

    private Method getOldestCOBBusinessDate() throws NoSuchMethodException {
        Method method = InlineCommonLockableCOBExecutorService.class.getDeclaredMethod("getOldestCOBBusinessDate", List.class);
        method.setAccessible(true);
        return method;
    }
}
