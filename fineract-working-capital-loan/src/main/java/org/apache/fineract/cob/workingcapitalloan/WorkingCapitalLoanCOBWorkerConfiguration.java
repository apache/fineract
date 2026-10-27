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
import org.apache.fineract.cob.processor.ItemTransactionProcessor;
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.infrastructure.springbatch.StepRetryPolicyFactory;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanModelProcessingService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Conditional(BatchWorkerCondition.class)
@RequiredArgsConstructor
public class WorkingCapitalLoanCOBWorkerConfiguration {

    private final RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;
    private final MessageChannel inboundRequests;
    private final PropertyService propertyService;
    private final PlatformTransactionManager transactionManager;
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
    public Step workingCapitalLoanCOBWorkerStep(final WorkingCapitalLoanCOBWorkerItemReader reader,
            final ItemTransactionProcessor<WorkingCapitalLoan> processor,
            @Qualifier("workingCapitalLoanCOBWorkerItemWriter") final ItemWriter<WorkingCapitalLoan> writer) {
        final ChunkOrientedStepBuilder<WorkingCapitalLoan, WorkingCapitalLoan> stepBuilder = stepBuilderFactory
                .get(WORKING_CAPITAL_LOAN_COB_WORKER_STEP).inputChannel(inboundRequests)
                .<WorkingCapitalLoan, WorkingCapitalLoan>chunk(propertyService.getChunkSize(WORKING_CAPITAL_JOB_NAME)) //
                .reader(reader) //
                .processor(processor) //
                .writer(writer) //
                .faultTolerant() //
                .retryPolicy(StepRetryPolicyFactory.immediateRetryPolicy(propertyService.getRetryLimit(WORKING_CAPITAL_JOB_NAME),
                        Exception.class)) //
                .skip(Exception.class) //
                .skipLimit(propertyService.getSkipLimit(WORKING_CAPITAL_JOB_NAME)) //
                .listener(workingCapitalLoanItemListener()) //
                .listener(workingCapitalCobWorkerStepListener()) //
                .transactionManager(transactionManager);

        // No task executor is registered, deliberately and unconditionally - see the comment in
        // LoanCOBWorkerConfiguration#loanCOBWorkerStep. Each loan commits in a transaction of its own
        // (ItemTransactionProcessor), but the COB context is set on the step thread only, so item processing stays
        // sequential and is not configurable.

        return stepBuilder.build();
    }

    @Bean
    public CobWorkerStepListener workingCapitalCobWorkerStepListener() {
        return new CobWorkerStepListener(initialisationTasklet, applyWorkingCapitalLoanLock(), resetContextTasklet);
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

    // Reader, processor and writer are step-scoped, as in LoanCOBWorkerConfiguration. The reader keeps the partition's
    // loan ids and the processor keeps the step's execution context and business date in instance fields, so every
    // partition (and every tenant) needs its own instance. A single shared instance let concurrent partitions - more
    // eligible loans than the partition size in one tenant, or several tenants running WC COB at once - overwrite each
    // other's queue: one partition then read nothing, finished COMPLETED and left its loan a day behind with a stale
    // LOAN_COB_CHUNK_PROCESSING lock. The bean methods return the concrete classes on purpose: the scoped proxy then
    // subclasses them, so it is still a StepExecutionListener and Spring Batch registers it for beforeStep.

    @Bean
    @StepScope
    public WorkingCapitalLoanCOBWorkerItemReader workingCapitalLoanCOBWorkerItemReader() {
        return new WorkingCapitalLoanCOBWorkerItemReader(workingCapitalLoanRepository,
                new BeforeStepLockingItemReaderHelper(retrieveIdService, wpcLoanLockingService));
    }

    @Bean
    @StepScope
    public ItemTransactionProcessor<WorkingCapitalLoan> workingCapitalLoanCOBWorkerItemProcessor(
            final COBBusinessStepService cobBusinessStepService, final WorkingCapitalLoanModelProcessingService modelProcessingService) {
        final TransactionTemplate requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new ItemTransactionProcessor<>(new WorkingCapitalLoanCOBWorkerItemProcessor(cobBusinessStepService, modelProcessingService),
                new WorkingCapitalLoanCOBWorkerItemWriter(wpcLoanLockingService, workingCapitalLoanRepository),
                loanId -> workingCapitalLoanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId)),
                requiresNewTransactionTemplate);
    }

    @Bean
    @StepScope
    public ItemWriter<WorkingCapitalLoan> workingCapitalLoanCOBWorkerItemWriter() {
        return ItemTransactionProcessor.noOpWriter();
    }
}
