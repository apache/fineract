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
package org.apache.fineract.cob.workingcapitalloan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.common.InitialisationTasklet;
import org.apache.fineract.cob.common.ResetContextTasklet;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanModelProcessingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.StepScope;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.boot.LazyInitializationBeanFactoryPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Partitions of one Working Capital Loan COB job can run at the same time on one instance, so every partition needs its
 * own reader, processor and writer. A shared reader lets the second partition's {@code beforeStep} replace the first
 * partition's queue: the first partition's loans stay locked and are never processed.
 */
class WorkingCapitalLoanCOBWorkerScopeTest {

    private final WorkingCapitalLoanRetrieveIdService retrieveIdService = mock(WorkingCapitalLoanRetrieveIdService.class);
    private final LockingService lockingService = mock(LockingService.class);
    private final WorkingCapitalLoanRepository repository = mock(WorkingCapitalLoanRepository.class);
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        when(retrieveIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(any(COBParameter.class), anyBoolean()))
                .thenAnswer(invocation -> {
                    COBParameter parameter = invocation.getArgument(0);
                    return LongStream.rangeClosed(parameter.getMinAccountId(), parameter.getMaxAccountId()).boxed().toList();
                });
        when(lockingService.findLockIdsByLoanIdInAndLockOwner(anyList(), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findById(anyLong())).thenAnswer(invocation -> {
            WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
            when(loan.getId()).thenReturn(invocation.getArgument(0));
            return Optional.of(loan);
        });

        context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("test", Map.of("fineract.mode.batch-worker-enabled", "true")));
        // only the beans a test asks for are created, so the worker step itself needs no builder mocks
        context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
        context.registerBean(StepScope.class);
        context.registerBean("workingCapitalLoanRetrieveIdService", WorkingCapitalLoanRetrieveIdService.class, () -> retrieveIdService);
        context.registerBean("workingCapitalLoanLockingService", LockingService.class, () -> lockingService);
        context.registerBean("workingCapitalLoanRepository", WorkingCapitalLoanRepository.class, () -> repository);
        registerMock("jobRepository", JobRepository.class);
        registerMock("stepBuilderFactory", RemotePartitioningWorkerStepBuilderFactory.class);
        registerMock("inboundRequests", MessageChannel.class);
        registerMock("propertyService", PropertyService.class);
        registerMock("transactionManager", PlatformTransactionManager.class);
        registerMock("jdbcTransactionManager", PlatformTransactionManager.class);
        registerMock("requiresNewTransactionJdbcTemplate", TransactionTemplate.class);
        registerMock("fineractProperties", FineractProperties.class);
        registerMock("initialiseContext", InitialisationTasklet.class);
        registerMock("resetContext", ResetContextTasklet.class);
        registerMock("cobBusinessStepService", COBBusinessStepService.class);
        registerMock("workingCapitalLoanModelProcessingService", WorkingCapitalLoanModelProcessingService.class);
        context.register(WorkingCapitalLoanCOBWorkerConfiguration.class);
        context.refresh();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void eachPartitionReadsItsOwnLoansWhenTwoRunAtOnce() throws Exception {
        StepExecution first = partition(1L, 1L, 3L);
        StepExecution second = partition(2L, 4L, 6L);
        WorkingCapitalLoanCOBWorkerItemReader reader = context.getBean(WorkingCapitalLoanCOBWorkerItemReader.class);

        StepSynchronizationManager.register(first);
        try {
            reader.beforeStep(first);
            StepSynchronizationManager.register(second);
            try {
                reader.beforeStep(second);
                assertThat(readAll(reader)).containsExactly(4L, 5L, 6L);
            } finally {
                StepSynchronizationManager.close();
            }
            assertThat(readAll(reader)).containsExactly(1L, 2L, 3L);
        } finally {
            StepSynchronizationManager.close();
        }
    }

    @Test
    void eachPartitionGetsItsOwnItemComponents() {
        for (Class<?> component : List.of(WorkingCapitalLoanCOBWorkerItemReader.class, WorkingCapitalLoanCOBWorkerItemProcessor.class,
                WorkingCapitalLoanCOBWorkerItemWriter.class)) {
            Object bean = context.getBean(component);
            Object firstTarget = inStep(partition(1L, 1L, 3L), () -> target(bean));
            Object secondTarget = inStep(partition(2L, 4L, 6L), () -> target(bean));
            assertThat(secondTarget).as("%s must be step-scoped", component.getSimpleName()).isNotSameAs(firstTarget);
        }
    }

    private <T> void registerMock(String name, Class<T> type) {
        T instance = mock(type);
        context.registerBean(name, type, () -> instance);
    }

    private static StepExecution partition(long id, long minLoanId, long maxLoanId) {
        JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_JOB_NAME),
                new JobParameters());
        StepExecution stepExecution = new StepExecution(id,
                WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_WORKER_STEP + ":partition_" + id, jobExecution);
        stepExecution.getExecutionContext().put(COBConstant.COB_PARAMETER, new COBParameter(minLoanId, maxLoanId));
        return stepExecution;
    }

    private static List<Long> readAll(WorkingCapitalLoanCOBWorkerItemReader reader) throws Exception {
        List<Long> loanIds = new ArrayList<>();
        for (WorkingCapitalLoan loan = reader.read(); loan != null; loan = reader.read()) {
            loanIds.add(loan.getId());
        }
        return loanIds;
    }

    private static Object inStep(StepExecution stepExecution, Supplier<Object> action) {
        StepSynchronizationManager.register(stepExecution);
        try {
            return action.get();
        } finally {
            StepSynchronizationManager.close();
        }
    }

    private static Object target(Object bean) {
        if (!(bean instanceof Advised advised)) {
            return bean;
        }
        try {
            return advised.getTargetSource().getTarget();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
