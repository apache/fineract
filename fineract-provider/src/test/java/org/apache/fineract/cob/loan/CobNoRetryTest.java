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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.cob.service.RetrieveLoanIdService;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBWorkerConfiguration;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBWorkerItemReader;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBWorkerItemWriter;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanModelProcessingService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Pins the COB worker steps, as their configurations build them, to one attempt per account in the chunk and one in
 * scan mode, and no more.
 * <p>
 * Spring Batch 6 retries an item inside the chunk transaction it failed in, and a COB failure has by then marked that
 * transaction rollback-only. A retry that then succeeds hides the failure: scan mode never starts, and the chunk is
 * silently rolled back while being recorded as committed, leaving its accounts locked with no error. See
 * {@code StepRetryPolicyFactory}. Each worker step is run here with an account whose business steps always fail: it
 * must be attempted exactly twice - once with its chunk, once alone in scan mode - and then skipped.
 */
class CobNoRetryTest {

    private static final String BUSINESS_DATE = "2026-10-06";

    private final JobRepository jobRepository = new ResourcelessJobRepository();
    private final PlatformTransactionManager transactionManager = new ResourcelessTransactionManager();
    private final COBBusinessStepService businessSteps = mock(COBBusinessStepService.class);
    private final RetrieveLoanIdService retrieveIdService = mock(RetrieveLoanIdService.class);
    private final LockingService lockingService = mock(LockingService.class);
    private final AtomicInteger attempts = new AtomicInteger();
    private RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;
    private PropertyService propertyService;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        LocalDate cobDate = LocalDate.parse(BUSINESS_DATE);
        ThreadLocalContextUtil.setBusinessDates(
                new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, cobDate.plusDays(1), BusinessDateType.COB_DATE, cobDate)));
        stepBuilderFactory = mock(RemotePartitioningWorkerStepBuilderFactory.class);
        RemotePartitioningWorkerStepBuilder workerStepBuilder = mock(RemotePartitioningWorkerStepBuilder.class);
        when(stepBuilderFactory.get(anyString())).thenReturn(workerStepBuilder);
        when(workerStepBuilder.inputChannel(any())).thenReturn(workerStepBuilder);
        doReturn(new StepBuilder("worker", jobRepository).chunk(10)).when(workerStepBuilder).chunk(anyInt());
        propertyService = mock(PropertyService.class);
        when(propertyService.getChunkSize(anyString())).thenReturn(10);
        when(propertyService.getSkipLimit(anyString())).thenReturn(11);
        when(retrieveIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(any(), anyBoolean()))
                .thenReturn(List.of(1L));
        when(lockingService.findLockIdsByLoanIdInAndLockOwner(anyList(), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING))).thenReturn(List.of(1L));
        when(businessSteps.run(any(), any())).thenAnswer(invocation -> {
            attempts.incrementAndGet();
            throw new BusinessStepException("business step always fails");
        });
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void loanCobWorkerStepAttemptsAFailingLoanOnceWithItsChunkAndOnceAlone() throws Exception {
        LoanRepository loanRepository = mock(LoanRepository.class);
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(mock(Loan.class)));
        AppUserRepositoryWrapper userRepository = mock(AppUserRepositoryWrapper.class);
        when(userRepository.fetchSystemUser()).thenReturn(mock(AppUser.class));
        LoanCOBWorkerConfiguration configuration = new LoanCOBWorkerConfiguration();
        for (Field field : LoanCOBWorkerConfiguration.class.getDeclaredFields()) {
            ReflectionTestUtils.setField(configuration, field.getName(), dependency(field.getType()));
        }
        ReflectionTestUtils.setField(configuration, "loanRepository", loanRepository);
        ReflectionTestUtils.setField(configuration, "userRepository", userRepository);

        StepExecution result = run(configuration.loanCOBWorkerStep());

        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(attempts).as("one attempt with the chunk and one alone in scan mode; any more is a retry").hasValue(2);
        assertThat(result.getWriteSkipCount()).isEqualTo(1);
    }

    @Test
    void workingCapitalLoanCobWorkerStepAttemptsAFailingLoanOnceWithItsChunkAndOnceAlone() throws Exception {
        Constructor<?> constructor = WorkingCapitalLoanCOBWorkerConfiguration.class.getDeclaredConstructors()[0];
        Object[] arguments = new Object[constructor.getParameterCount()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = dependency(constructor.getParameterTypes()[i]);
        }
        WorkingCapitalLoanCOBWorkerConfiguration configuration = (WorkingCapitalLoanCOBWorkerConfiguration) constructor
                .newInstance(arguments);
        WorkingCapitalLoanRepository loanRepository = mock(WorkingCapitalLoanRepository.class);
        when(loanRepository.findById(anyLong())).thenReturn(Optional.of(mock(WorkingCapitalLoan.class)));
        WorkingCapitalLoanCOBWorkerItemReader reader = new WorkingCapitalLoanCOBWorkerItemReader(
                new BeforeStepLockingItemReaderHelper(retrieveIdService, lockingService));
        WorkingCapitalLoanCOBWorkerItemWriter writer = new WorkingCapitalLoanCOBWorkerItemWriter(businessSteps,
                mock(WorkingCapitalLoanModelProcessingService.class), lockingService, loanRepository);

        StepExecution result = run(configuration.workingCapitalLoanCOBWorkerStep(reader, writer));

        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(attempts).as("one attempt with the chunk and one alone in scan mode; any more is a retry").hasValue(2);
        assertThat(result.getWriteSkipCount()).isEqualTo(1);
    }

    private StepExecution run(Step step) throws Exception {
        ExecutionContext jobContext = new ExecutionContext();
        jobContext.put(COBConstant.BUSINESS_DATE_PARAMETER_NAME, BUSINESS_DATE);
        JobExecution job = jobRepository.createJobExecution(jobRepository.createJobInstance("cob", new JobParameters()),
                new JobParameters(), jobContext);
        StepExecution execution = jobRepository.createStepExecution(step.getName(), job);
        execution.getExecutionContext().put(COBConstant.COB_PARAMETER, new COBParameter(1L, 1L));
        execution.getExecutionContext().put(COBConstant.BUSINESS_STEPS, Set.of(new BusinessStepNameAndOrder("failingStep", 1L)));
        step.execute(execution);
        return execution;
    }

    private Object dependency(Class<?> type) {
        if (type == RemotePartitioningWorkerStepBuilderFactory.class) {
            return stepBuilderFactory;
        }
        if (type == PropertyService.class) {
            return propertyService;
        }
        if (type == PlatformTransactionManager.class) {
            return transactionManager;
        }
        if (type == TransactionTemplate.class) {
            return new TransactionTemplate(transactionManager);
        }
        if (type == COBBusinessStepService.class) {
            return businessSteps;
        }
        if (type.isInstance(retrieveIdService)) {
            return retrieveIdService;
        }
        if (type == LockingService.class) {
            return lockingService;
        }
        if (type == FineractProperties.class) {
            FineractProperties properties = new FineractProperties();
            FineractProperties.FineractQueryProperties query = new FineractProperties.FineractQueryProperties();
            query.setInClauseParameterSizeLimit(1000);
            properties.setQuery(query);
            return properties;
        }
        if (type.isPrimitive() || type.isEnum() || java.lang.reflect.Modifier.isFinal(type.getModifiers())) {
            return null;
        }
        return mock(type);
    }
}
