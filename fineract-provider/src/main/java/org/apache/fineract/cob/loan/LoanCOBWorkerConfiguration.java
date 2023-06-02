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
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.common.InitialisationTasklet;
import org.apache.fineract.cob.common.ResetContextTasklet;
import org.apache.fineract.cob.listener.ChunkProcessingLoanItemListener;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ConditionalOnProperty(value = "fineract.mode.batch-worker-enabled", havingValue = "true")
public class LoanCOBWorkerConfiguration {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
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
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RetrieveLoanIdService retrieveLoanIdService;

    @Autowired
    private FineractProperties fineractProperties;
    @Autowired
    private LoanLockingService loanLockingService;

    @Autowired
    private CustomJobParameterResolver customJobParameterResolver;

    @Bean(name = LoanCOBConstant.LOAN_COB_WORKER_STEP)
    public Step loanCOBWorkerStep() {
        return stepBuilderFactory.get("Loan COB worker - Step").inputChannel(inboundRequests).flow(flow()).build();
    }

    @Bean
    public Flow flow() {
        return new FlowBuilder<Flow>("cobFlow").start(initialisationStep(null)).next(applyLockStep(null)).next(loanBusinessStep(null))
                .next(resetContextStep(null)).build();
    }

    @Bean
    @StepScope
    public Step initialisationStep(@Value("#{stepExecutionContext['partition']}") String partitionName) {
        return new StepBuilder("Initialisation - Step:" + partitionName, jobRepository).tasklet(initialiseContext(), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Step loanBusinessStep(@Value("#{stepExecutionContext['partition']}") String partitionName) {
        return new StepBuilder("Loan Business - Step:" + partitionName, jobRepository)
                .<Loan, Loan>chunk(propertyService.getChunkSize(JobName.LOAN_COB.name()), transactionManager).reader(cobWorkerItemReader())
                .processor(cobWorkerItemProcessor()).writer(cobWorkerItemWriter()).faultTolerant().retry(Exception.class)
                .retryLimit(propertyService.getRetryLimit(LoanCOBConstant.JOB_NAME)).skip(Exception.class)
                .skipLimit(propertyService.getChunkSize(JobName.LOAN_COB.name()) + 1).listener(loanItemListener()).build();
    }

    @Bean
    @StepScope
    public Step applyLockStep(@Value("#{stepExecutionContext['partition']}") String partitionName) {
        return new StepBuilder("Apply lock - Step:" + partitionName, jobRepository).tasklet(applyLock(), transactionManager).build();
    }

    @Bean
    @StepScope
    public Step resetContextStep(@Value("#{stepExecutionContext['partition']}") String partitionName) {
        return new StepBuilder("Reset context - Step:" + partitionName, jobRepository).tasklet(resetContext(), transactionManager).build();
    }

    @Bean
    public InitialisationTasklet initialiseContext() {
        return new InitialisationTasklet(userRepository);
    }

    @Bean
    public ChunkProcessingLoanItemListener loanItemListener() {
        return new ChunkProcessingLoanItemListener(loanLockingService, transactionTemplate);
    }

    @Bean
    public ApplyLoanLockTasklet applyLock() {
        return new ApplyLoanLockTasklet(fineractProperties, loanLockingService, retrieveLoanIdService, customJobParameterResolver);
    }

    @Bean
    public ResetContextTasklet resetContext() {
        return new ResetContextTasklet();
    }

    @Bean
    @StepScope
    public LoanItemReader cobWorkerItemReader() {
        return new LoanItemReader(loanRepository, retrieveLoanIdService, customJobParameterResolver);
    }

    @Bean
    @StepScope
    public LoanItemProcessor cobWorkerItemProcessor() {
        return new LoanItemProcessor(cobBusinessStepService);
    }

    @Bean
    @StepScope
    public LoanItemWriter cobWorkerItemWriter() {
        LoanItemWriter repositoryItemWriter = new LoanItemWriter(loanLockingService);
        repositoryItemWriter.setRepository(loanRepository);
        return repositoryItemWriter;
    }
}
