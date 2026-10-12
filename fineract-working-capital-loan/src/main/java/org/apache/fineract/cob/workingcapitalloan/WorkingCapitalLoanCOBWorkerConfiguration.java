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

import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_JOB_NAME;
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_WORKER_STEP;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.common.InitialisationTasklet;
import org.apache.fineract.cob.common.ResetContextTasklet;
import org.apache.fineract.cob.conditions.BatchWorkerCondition;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.listener.CobWorkerStepListener;
import org.apache.fineract.cob.loan.ContextAwareTaskDecorator;
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Conditional(BatchWorkerCondition.class)
@RequiredArgsConstructor
public class WorkingCapitalLoanCOBWorkerConfiguration {

    private final JobRepository jobRepository;
    private final RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;
    private final MessageChannel inboundRequests;
    private final PropertyService propertyService;
    private final PlatformTransactionManager transactionManager;
    @Qualifier("jdbcTransactionManager")
    private final PlatformTransactionManager jdbcTransactionManager;
    @Qualifier("requiresNewTransactionJdbcTemplate")
    private final TransactionTemplate requiresNewTransactionJdbcTemplate;
    @Qualifier("workingCapitalLoanLockingService")
    private final LockingService wpcLoanLockingService;
    private final FineractProperties fineractProperties;
    private final WorkingCapitalLoanRetrieveIdService retrieveIdService;
    private final WorkingCapitalLoanRepository workingCapitalLoanRepository;
    @Qualifier("initialiseContext")
    private final InitialisationTasklet initialisationTasklet;
    @Qualifier("resetContext")
    private final ResetContextTasklet resetContextTasklet;

    @Bean(WORKING_CAPITAL_LOAN_COB_WORKER_STEP)
    public Step workingCapitalLoanCOBWorkerStep(final COBBusinessStepService cobBusinessStepService) {
        final SimpleStepBuilder<WorkingCapitalLoan, WorkingCapitalLoan> stepBuilder = stepBuilderFactory
                .get(WORKING_CAPITAL_LOAN_COB_WORKER_STEP).inputChannel(inboundRequests)
                .<WorkingCapitalLoan, WorkingCapitalLoan>chunk(propertyService.getChunkSize(JobName.LOAN_COB.name()), transactionManager) //
                .reader(new WorkingCapitalLoanCOBWorkerItemReader(workingCapitalLoanRepository,
                        new BeforeStepLockingItemReaderHelper(retrieveIdService, wpcLoanLockingService))) //
                .processor(new WorkingCapitalLoanCOBWorkerItemProcessor(cobBusinessStepService)) //
                .writer(new WorkingCapitalLoanCOBWorkerItemWriter(wpcLoanLockingService, workingCapitalLoanRepository)) //
                .faultTolerant() //
                .retry(Exception.class) //
                .retryLimit(propertyService.getRetryLimit(WORKING_CAPITAL_JOB_NAME)) //
                .skip(Exception.class) //
                .skipLimit(propertyService.getChunkSize(WORKING_CAPITAL_JOB_NAME) + 1) //
                .listener(workingCapitalLoanItemListener()) //
                .listener(workingCapitalCobWorkerStepListener()) //
                .transactionManager(transactionManager);

        if (propertyService.getThreadPoolMaxPoolSize(WORKING_CAPITAL_JOB_NAME) > 1) {
            stepBuilder.taskExecutor(workingCapitalCobTaskExecutor());
        }

        return stepBuilder.build();
    }

    @Bean
    public CobWorkerStepListener workingCapitalCobWorkerStepListener() {
        return new CobWorkerStepListener(initialisationTasklet, applyWorkingCapitalLoanLock(), resetContextTasklet);
    }

    @Bean
    public TaskExecutor workingCapitalCobTaskExecutor() {
        if (propertyService.getThreadPoolMaxPoolSize(WORKING_CAPITAL_JOB_NAME) == 1) {
            return new SyncTaskExecutor();
        }
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("COB-Thread-");
        taskExecutor.setThreadGroupName("COB-Thread");
        taskExecutor.setCorePoolSize(propertyService.getThreadPoolCorePoolSize(WORKING_CAPITAL_JOB_NAME));
        taskExecutor.setMaxPoolSize(propertyService.getThreadPoolMaxPoolSize(WORKING_CAPITAL_JOB_NAME));
        taskExecutor.setQueueCapacity(propertyService.getThreadPoolQueueCapacity(WORKING_CAPITAL_JOB_NAME));
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.setTaskDecorator(new ContextAwareTaskDecorator());
        return taskExecutor;
    }

    @Bean
    public WorkingCapitalLoanCOBWorkerItemListener workingCapitalLoanItemListener() {
        return new WorkingCapitalLoanCOBWorkerItemListener(wpcLoanLockingService, requiresNewTransactionJdbcTemplate);
    }

    @Bean
    public ApplyWorkingCapitalLoanLockTasklet applyWorkingCapitalLoanLock() {
        return new ApplyWorkingCapitalLoanLockTasklet(fineractProperties, wpcLoanLockingService, retrieveIdService,
                requiresNewTransactionJdbcTemplate);
    }
}
