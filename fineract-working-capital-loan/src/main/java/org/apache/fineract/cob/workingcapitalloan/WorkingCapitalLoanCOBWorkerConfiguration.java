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
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_BUSINESS_STEP;
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_FLOW;
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_WORKER_STEP;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.conditions.BatchWorkerCondition;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.domain.WorkingCapitalLoanAccountLock;
import org.apache.fineract.cob.loan.ContextAwareTaskDecorator;
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private final RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;
    private final MessageChannel inboundRequests;
    private final JobRepository jobRepository;
    private final PropertyService propertyService;
    private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate transactionTemplate;
    private final LockingService<WorkingCapitalLoanAccountLock> wpcLoanLockingService;
    private final FineractProperties fineractProperties;
    private final WorkingCapitalLoanRetrieveIdService retrieveIdService;
    private final WorkingCapitalLoanRepository workingCapitalLoanRepository;

    @Bean(WORKING_CAPITAL_LOAN_COB_FLOW)
    public Flow workingCapitalLoanCOBFlow(Step initialisationStep, Step applyWorkingCapitalLockStep, Step workingCapitalLoanCOBBusinessStep,
            Step resetContextStep) {
        return new FlowBuilder<Flow>(WORKING_CAPITAL_LOAN_COB_FLOW).start(initialisationStep).next(applyWorkingCapitalLockStep)
                .next(workingCapitalLoanCOBBusinessStep).next(resetContextStep).build();
    }

    @Bean(WORKING_CAPITAL_LOAN_COB_WORKER_STEP)
    public Step workingCapitalLoanCOBWorkerStep(Flow workingCapitalLoanCOBFlow) {
        return stepBuilderFactory.get(WORKING_CAPITAL_LOAN_COB_WORKER_STEP).inputChannel(inboundRequests).flow(workingCapitalLoanCOBFlow)
                .build();
    }

    @Bean(WORKING_CAPITAL_LOAN_COB_BUSINESS_STEP)
    @StepScope
    public Step workingCapitalLoanCOBBusinessStep(@Value("#{stepExecutionContext['partition']}") String partitionName,
            TaskExecutor workingCapitalCobTaskExecutor, COBBusinessStepService cobBusinessStepService) {
        SimpleStepBuilder<WorkingCapitalLoan, WorkingCapitalLoan> stepBuilder = new StepBuilder("Loan Business - Step:" + partitionName,
                jobRepository)
                .<WorkingCapitalLoan, WorkingCapitalLoan>chunk(propertyService.getChunkSize(JobName.LOAN_COB.name()), transactionManager) //
                .reader(new WorkingCapitalLoanCOBWorkerItemReader(workingCapitalLoanRepository,
                        new BeforeStepLockingItemReaderHelper<>(retrieveIdService, wpcLoanLockingService))) //
                .processor(new WorkingCapitalLoanCOBWorkerItemProcessor(cobBusinessStepService)) //
                .writer(new WorkingCapitalLoanCOBWorkerItemWriter(wpcLoanLockingService, workingCapitalLoanRepository)) //
                .faultTolerant() //
                .retry(Exception.class) //
                .retryLimit(propertyService.getRetryLimit(WORKING_CAPITAL_JOB_NAME)) //
                .skip(Exception.class) //
                .skipLimit(propertyService.getChunkSize(WORKING_CAPITAL_JOB_NAME) + 1) //
                .listener(workingCapitalLoanItemListener()) //
                .transactionManager(transactionManager);

        if (propertyService.getThreadPoolMaxPoolSize(WORKING_CAPITAL_JOB_NAME) > 1) {
            stepBuilder.taskExecutor(workingCapitalCobTaskExecutor);
        }

        return stepBuilder.build();
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

    // Lock
    @Bean
    public WorkingCapitalLoanCOBWorkerItemListener workingCapitalLoanItemListener() {
        return new WorkingCapitalLoanCOBWorkerItemListener(wpcLoanLockingService, transactionTemplate);
    }

    @Bean("applyWorkingCapitalLockStep")
    @StepScope
    public Step applyWorkingCapitalLockStep(@Value("#{stepExecutionContext['partition']}") String partitionName) {
        return new StepBuilder("Apply lock - Step:" + partitionName, jobRepository)
                .tasklet(applyWorkingCapitalLoanLock(), transactionManager).build();
    }

    @Bean
    public ApplyWorkingCapitalLoanLockTasklet applyWorkingCapitalLoanLock() {
        return new ApplyWorkingCapitalLoanLockTasklet(fineractProperties, wpcLoanLockingService, retrieveIdService, transactionTemplate);
    }

}
