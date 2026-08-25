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
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
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
        final ChunkOrientedStepBuilder<Loan, Loan> stepBuilder = stepBuilderFactory.get("Loan COB worker - Step")
                .inputChannel(inboundRequests) //
                .<Loan, Loan>chunk(propertyService.getChunkSize(JobName.LOAN_COB.name())) //
                .reader(cobWorkerItemReader()) //
                .processor(cobWorkerItemProcessor()) //
                .writer(cobWorkerItemWriter()) //
                .faultTolerant() //
                .retry(Exception.class) //
                .retryLimit(propertyService.getRetryLimit(LoanCOBConstant.JOB_NAME)) //
                .skip(Exception.class) //
                .skipLimit(propertyService.getSkipLimit(LoanCOBConstant.JOB_NAME)) //
                .listener(loanItemListener()) //
                .listener(cobWorkerStepListener()) //
                .transactionManager(transactionManager);

        // No task executor is registered, deliberately and unconditionally.
        //
        // Batch 6's ChunkOrientedStep keeps the chunk transaction on the step thread and, when a task executor is
        // present, submits the ITEMS of a chunk to it (ChunkOrientedStep.processChunkConcurrently). Item processing
        // then runs with no transaction of its own, so COBBusinessStepService.run has to open one - which commits
        // independently of the chunk write. A chunk-write rollback, a skip in scan mode or a step restart therefore
        // leaves the business-step writes committed while the loan is not marked COB'd, and the next pass re-runs
        // them over it.
        //
        // Leaving the executor unset keeps isConcurrent() false, so processing stays on the step thread and joins the
        // chunk transaction exactly as it did under Batch 5. This is NOT configurable on purpose: the thread pool size
        // reads like a throughput dial but decides transactional semantics, so it must not be reachable from
        // configuration. Concurrency comes from partitioning instead - lower partition-size and add worker instances.
        //
        // Do not reintroduce a task executor until FINERACT-2621 establishes that every COB business step is
        // idempotent under a second pass.

        return stepBuilder.build();
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
        return new LoanItemWriter(loanLockingService, loanRepository);
    }
}
