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

import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.common.InitialisationTasklet;
import org.apache.fineract.cob.common.ResetContextTasklet;
import org.apache.fineract.cob.conditions.BatchWorkerCondition;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.listener.ChunkProcessingLoanItemListener;
import org.apache.fineract.cob.listener.CobWorkerStepListener;
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.cob.service.RetrieveLoanIdService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanModelProcessingService;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Conditional(BatchWorkerCondition.class)
public class LoanCOBWorkerConfiguration {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    @Qualifier("jdbcTransactionManager")
    private PlatformTransactionManager jdbcTransactionManager;
    @Autowired
    @Qualifier("requiresNewTransactionJdbcTemplate")
    private TransactionTemplate requiresNewTransactionJdbcTemplate;
    @Autowired
    private RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;

    @Autowired
    private PropertyService propertyService;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private QueueChannel inboundRequests;
    @Autowired
    private COBBusinessStepService cobBusinessStepService;
    @Autowired
    private AppUserRepositoryWrapper userRepository;
    @Autowired
    private RetrieveLoanIdService retrieveIdService;

    @Autowired
    private FineractProperties fineractProperties;
    @Autowired
    @Qualifier("retrieveLoanLockingService")
    private LockingService loanLockingService;

    @Autowired
    private ProgressiveLoanModelProcessingService progressiveLoanModelProcessingService;

    @Bean(name = LoanCOBConstant.LOAN_COB_WORKER_STEP)
    public Step loanCOBWorkerStep() {
        final SimpleStepBuilder<Loan, Loan> stepBuilder = stepBuilderFactory.get("Loan COB worker - Step").inputChannel(inboundRequests)
                .<Loan, Loan>chunk(propertyService.getChunkSize(JobName.LOAN_COB.name()), transactionManager) //
                .reader(cobWorkerItemReader()) //
                .processor(cobWorkerItemProcessor()) //
                .writer(cobWorkerItemWriter()) //
                .faultTolerant() //
                .retry(Exception.class) //
                .retryLimit(propertyService.getRetryLimit(LoanCOBConstant.JOB_NAME)) //
                .skip(Exception.class) //
                .skipLimit(propertyService.getChunkSize(LoanCOBConstant.JOB_NAME) + 1) //
                .listener(loanItemListener()) //
                .listener(cobWorkerStepListener()) //
                .transactionManager(transactionManager);

        if (propertyService.getThreadPoolMaxPoolSize(LoanCOBConstant.JOB_NAME) > 1) {
            stepBuilder.taskExecutor(cobTaskExecutor());
        }

        return stepBuilder.build();
    }

    @Bean
    public TaskExecutor cobTaskExecutor() {
        if (propertyService.getThreadPoolMaxPoolSize(LoanCOBConstant.JOB_NAME) == 1) {
            return new SyncTaskExecutor();
        }
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("COB-Thread-");
        taskExecutor.setThreadGroupName("COB-Thread");
        taskExecutor.setCorePoolSize(propertyService.getThreadPoolCorePoolSize(JobName.LOAN_COB.name()));
        taskExecutor.setMaxPoolSize(propertyService.getThreadPoolMaxPoolSize(JobName.LOAN_COB.name()));
        taskExecutor.setQueueCapacity(propertyService.getThreadPoolQueueCapacity(JobName.LOAN_COB.name()));
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.setTaskDecorator(new ContextAwareTaskDecorator());
        return taskExecutor;
    }

    @Bean
    public CobWorkerStepListener cobWorkerStepListener() {
        return new CobWorkerStepListener(initialiseContext(), applyLock(), resetContext());
    }

    @Bean
    public InitialisationTasklet initialiseContext() {
        return new InitialisationTasklet(userRepository);
    }

    @Bean
    public ChunkProcessingLoanItemListener loanItemListener() {
        return new ChunkProcessingLoanItemListener(loanLockingService, requiresNewTransactionJdbcTemplate);
    }

    @Bean
    public ApplyLoanLockTasklet applyLock() {
        return new ApplyLoanLockTasklet(fineractProperties, loanLockingService, retrieveIdService, requiresNewTransactionJdbcTemplate);
    }

    @Bean
    public ResetContextTasklet resetContext() {
        return new ResetContextTasklet();
    }

    @Bean
    @StepScope
    public LoanItemReader cobWorkerItemReader() {
        return new LoanItemReader(loanRepository, new BeforeStepLockingItemReaderHelper(retrieveIdService, loanLockingService));
    }

    @Bean
    @StepScope
    public LoanItemProcessor cobWorkerItemProcessor() {
        return new LoanItemProcessor(cobBusinessStepService, progressiveLoanModelProcessingService);
    }

    @Bean
    @StepScope
    public LoanItemWriter cobWorkerItemWriter() {
        LoanItemWriter repositoryItemWriter = new LoanItemWriter(loanLockingService);
        repositoryItemWriter.setRepository(loanRepository);
        return repositoryItemWriter;
    }
}
