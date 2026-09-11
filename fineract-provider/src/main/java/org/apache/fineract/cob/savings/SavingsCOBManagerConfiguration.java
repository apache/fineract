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
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.conditions.BatchManagerCondition;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.domain.SavingsAccountLockRepository;
import org.apache.fineract.cob.listener.COBExecutionListenerRunner;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchIntegration
@Conditional(BatchManagerCondition.class)
public class SavingsCOBManagerConfiguration {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private RemotePartitioningManagerStepBuilderFactory stepBuilderFactory;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private DirectChannel outboundRequests;
    @Autowired
    private COBBusinessStepService cobBusinessStepService;
    @Autowired
    private JobOperator jobOperator;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private RetrieveSavingsIdService retrieveSavingsIdService;
    @Autowired
    private BusinessEventNotifierService businessEventNotifierService;
    @Autowired
    private CustomJobParameterResolver customJobParameterResolver;
    @Autowired
    @Qualifier("savingsLockingService")
    private LockingService savingsLockingService;
    @Autowired
    private SavingsAccountLockRepository savingsAccountLockRepository;

    @Bean
    @StepScope
    public SavingsCOBPartitioner savingsPartitioner(@Value("#{stepExecution}") StepExecution stepExecution) {
        return new SavingsCOBPartitioner(propertyService, cobBusinessStepService, retrieveSavingsIdService, jobOperator, stepExecution);
    }

    @Bean("savingsCOBStep")
    public Step savingsCOBStep(SavingsCOBPartitioner savingsPartitioner) {
        return stepBuilderFactory.get(SavingsCOBConstant.SAVINGS_COB_PARTITIONER_STEP)
                .partitioner(SavingsCOBConstant.SAVINGS_COB_WORKER_STEP, savingsPartitioner)
                .pollInterval(propertyService.getPollInterval(SavingsCOBConstant.JOB_NAME)).outputChannel(outboundRequests).build();
    }

    @Bean("savingsResolveCustomJobParametersStep")
    public Step savingsResolveCustomJobParametersStep() {
        return new StepBuilder("Resolve custom job parameters - Savings Step", jobRepository)
                .tasklet(savingsResolveCustomJobParametersTasklet(), transactionManager).build();
    }

    @Bean("savingsStayedLockedStep")
    public Step savingsStayedLockedStep() {
        return new StepBuilder("Stayed locked savings accounts - Step", jobRepository)
                .tasklet(savingsStayedLockedTasklet(), transactionManager).build();
    }

    @Bean("savingsUnlockProcessedStep")
    public Step savingsUnlockProcessedStep() {
        return new StepBuilder("Unlock processed savings accounts - Step", jobRepository)
                .tasklet(savingsUnlockProcessedTasklet(), transactionManager).build();
    }

    @Bean("savingsResolveCustomJobParametersTasklet")
    public ResolveSavingsCOBCustomJobParametersTasklet savingsResolveCustomJobParametersTasklet() {
        return new ResolveSavingsCOBCustomJobParametersTasklet(customJobParameterResolver);
    }

    @Bean("savingsStayedLockedTasklet")
    public StayedLockedSavingsTasklet savingsStayedLockedTasklet() {
        return new StayedLockedSavingsTasklet(businessEventNotifierService, retrieveSavingsIdService);
    }

    @Bean("savingsUnlockProcessedTasklet")
    public UnlockProcessedSavingsTasklet savingsUnlockProcessedTasklet() {
        return new UnlockProcessedSavingsTasklet(savingsAccountLockRepository);
    }

    @Bean(name = "savingsCOBJob")
    public Job savingsCOBJob(SavingsCOBPartitioner savingsPartitioner) {
        return new JobBuilder(JobName.SAVINGS_COB.name(), jobRepository) //
                .listener(new COBExecutionListenerRunner(applicationContext, JobName.SAVINGS_COB.name())) //
                .start(savingsResolveCustomJobParametersStep()) //
                .next(savingsCOBStep(savingsPartitioner)) //
                .next(savingsStayedLockedStep()) //
                .next(savingsUnlockProcessedStep()) //
                .incrementer(new RunIdIncrementer()) //
                .build();
    }
}
