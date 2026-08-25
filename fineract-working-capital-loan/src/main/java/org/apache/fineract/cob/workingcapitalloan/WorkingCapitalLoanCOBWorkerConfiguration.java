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
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
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
        WorkingCapitalLoanCOBWorkerItemReader reader = new WorkingCapitalLoanCOBWorkerItemReader(workingCapitalLoanRepository,
                new BeforeStepLockingItemReaderHelper(retrieveIdService, wpcLoanLockingService));
        WorkingCapitalLoanCOBWorkerItemProcessor processor = new WorkingCapitalLoanCOBWorkerItemProcessor(cobBusinessStepService);
        final ChunkOrientedStepBuilder<WorkingCapitalLoan, WorkingCapitalLoan> stepBuilder = stepBuilderFactory
                .get(WORKING_CAPITAL_LOAN_COB_WORKER_STEP).inputChannel(inboundRequests)
                .<WorkingCapitalLoan, WorkingCapitalLoan>chunk(propertyService.getChunkSize(JobName.LOAN_COB.name())) //
                .reader(reader) //
                .processor(processor) //
                .writer(new WorkingCapitalLoanCOBWorkerItemWriter(wpcLoanLockingService, workingCapitalLoanRepository)) //
                .faultTolerant() //
                .retry(Exception.class) //
                .retryLimit(propertyService.getRetryLimit(WORKING_CAPITAL_JOB_NAME)) //
                .skip(Exception.class) //
                .skipLimit(propertyService.getSkipLimit(WORKING_CAPITAL_JOB_NAME)) //
                .listener(workingCapitalLoanItemListener()) //
                .listener(workingCapitalCobWorkerStepListener()) //
                .transactionManager(transactionManager);

        // No task executor is registered, deliberately and unconditionally - see the comment in
        // LoanCOBWorkerConfiguration#loanCOBWorkerStep. Concurrent item processing would take COB business step
        // writes out of the chunk transaction, so it stays sequential and is not configurable. See FINERACT-2684.

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
}
