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
package org.apache.fineract.cob.savings;

import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.common.ContextAwareTaskDecorator;
import org.apache.fineract.cob.common.InitialisationTasklet;
import org.apache.fineract.cob.common.ResetContextTasklet;
import org.apache.fineract.cob.conditions.BatchWorkerCondition;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.domain.SavingsAccountLockRepository;
import org.apache.fineract.cob.listener.ChunkProcessingSavingsItemListener;
import org.apache.fineract.cob.listener.CobWorkerStepListener;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
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
public class SavingsCOBWorkerConfiguration {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    @Qualifier("requiresNewTransactionJdbcTemplate")
    private TransactionTemplate batchJdbcTransactionTemplate;
    @Autowired
    private RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private SavingsAccountRepository savingsAccountRepository;
    @Autowired
    private SavingsAccountAssembler savingsAccountAssembler;
    @Autowired
    private QueueChannel inboundRequests;
    @Autowired
    private COBBusinessStepService cobBusinessStepService;
    @Autowired
    private AppUserRepositoryWrapper userRepository;
    @Autowired
    private RetrieveSavingsIdService retrieveSavingsIdService;
    @Autowired
    private FineractProperties fineractProperties;
    @Autowired
    @Qualifier("savingsLockingService")
    private LockingService savingsLockingService;
    @Autowired
    private SavingsAccountLockRepository savingsAccountLockRepository;

    @Bean(name = SavingsCOBConstant.SAVINGS_COB_WORKER_STEP)
    public Step savingsCOBWorkerStep() {
        final SimpleStepBuilder<SavingsAccount, SavingsAccount> stepBuilder = stepBuilderFactory.get("Savings COB worker - Step")
                .inputChannel(inboundRequests)
                .<SavingsAccount, SavingsAccount>chunk(propertyService.getChunkSize(JobName.SAVINGS_COB.name()), transactionManager) //
                .reader(savingsCobWorkerItemReader()) //
                .processor(savingsCobWorkerItemProcessor()) //
                .writer(savingsCobWorkerItemWriter()) //
                .faultTolerant() //
                .retry(Exception.class) //
                .retryLimit(propertyService.getRetryLimit(SavingsCOBConstant.JOB_NAME)) //
                .skip(Exception.class) //
                .skipLimit(propertyService.getChunkSize(SavingsCOBConstant.JOB_NAME) + 1) //
                .listener(savingsItemListener()) //
                .listener(savingsCobWorkerStepListener()) //
                .transactionManager(transactionManager);

        if (propertyService.getThreadPoolMaxPoolSize(SavingsCOBConstant.JOB_NAME) > 1) {
            stepBuilder.taskExecutor(savingsCobTaskExecutor());
        }
        return stepBuilder.build();
    }

    @Bean("savingsCobTaskExecutor")
    public TaskExecutor savingsCobTaskExecutor() {
        if (propertyService.getThreadPoolMaxPoolSize(SavingsCOBConstant.JOB_NAME) == 1) {
            return new SyncTaskExecutor();
        }
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("SavingsCOB-Thread-");
        taskExecutor.setThreadGroupName("SavingsCOB-Thread");
        taskExecutor.setCorePoolSize(propertyService.getThreadPoolCorePoolSize(JobName.SAVINGS_COB.name()));
        taskExecutor.setMaxPoolSize(propertyService.getThreadPoolMaxPoolSize(JobName.SAVINGS_COB.name()));
        taskExecutor.setQueueCapacity(propertyService.getThreadPoolQueueCapacity(JobName.SAVINGS_COB.name()));
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.setTaskDecorator(new ContextAwareTaskDecorator());
        return taskExecutor;
    }

    @Bean("savingsCobWorkerStepListener")
    public CobWorkerStepListener savingsCobWorkerStepListener() {
        return new CobWorkerStepListener(savingsInitialiseContext(), savingsApplyLock(), savingsResetContext());
    }

    @Bean("savingsInitialiseContext")
    public InitialisationTasklet savingsInitialiseContext() {
        return new InitialisationTasklet(userRepository);
    }

    @Bean("savingsItemListener")
    public ChunkProcessingSavingsItemListener savingsItemListener() {
        return new ChunkProcessingSavingsItemListener(savingsLockingService, batchJdbcTransactionTemplate);
    }

    @Bean("savingsApplyLock")
    public ApplySavingsLockTasklet savingsApplyLock() {
        return new ApplySavingsLockTasklet(fineractProperties, savingsLockingService, retrieveSavingsIdService,
                batchJdbcTransactionTemplate);
    }

    @Bean("savingsResetContext")
    public ResetContextTasklet savingsResetContext() {
        return new ResetContextTasklet();
    }

    @Bean
    @StepScope
    public SavingsItemReader savingsCobWorkerItemReader() {
        return new SavingsItemReader(savingsAccountRepository, savingsAccountAssembler, retrieveSavingsIdService, savingsLockingService);
    }

    @Bean
    @StepScope
    public SavingsItemProcessor savingsCobWorkerItemProcessor() {
        return new SavingsItemProcessor(cobBusinessStepService);
    }

    @Bean
    @StepScope
    public SavingsItemWriter savingsCobWorkerItemWriter() {
        SavingsItemWriter writer = new SavingsItemWriter(savingsLockingService);
        writer.setRepository(savingsAccountRepository);
        return writer;
    }
}
