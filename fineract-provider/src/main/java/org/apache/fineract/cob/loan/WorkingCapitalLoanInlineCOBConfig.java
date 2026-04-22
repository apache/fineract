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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.common.ResetContextTasklet;
import org.apache.fineract.cob.conditions.LoanCOBEnabledCondition;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.domain.WorkingCapitalLoanAccountLock;
import org.apache.fineract.cob.workingcapitalloan.InlineWorkingCapitalLoanCOBWorkerItemListener;
import org.apache.fineract.cob.workingcapitalloan.InlineWorkingCapitalLoanCOBWorkerItemWriter;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanInlineCOBWorkerItemProcessor;
import org.apache.fineract.cob.workingcapitalloan.businessstep.WorkingCapitalLoanCOBBusinessStep;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableBatchIntegration
@Conditional(LoanCOBEnabledCondition.class)
@RequiredArgsConstructor
public class WorkingCapitalLoanInlineCOBConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PropertyService propertyService;
    private final COBBusinessStepService cobBusinessStepService;
    private final TransactionTemplate transactionTemplate;
    private final CustomJobParameterRepository customJobParameterRepository;
    private final CustomJobParameterResolver customJobParameterResolver;
    private final LockingService<WorkingCapitalLoanAccountLock> loanLockingService;
    private final WorkingCapitalLoanRepository loanRepository;

    @Bean
    public InlineLoanCOBBuildExecutionContextTasklet<WorkingCapitalLoan, WorkingCapitalLoanCOBBusinessStep> inlineWorkingCapitalLoanCOBBuildExecutionContextTasklet() {
        return new InlineLoanCOBBuildExecutionContextTasklet<>(cobBusinessStepService, customJobParameterRepository,
                customJobParameterResolver, WorkingCapitalLoanCOBBusinessStep.class,
                WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_JOB_NAME);
    }

    @Bean
    protected Step inlineWorkingCapitalLoanCOBBuildExecutionContextStep(
            InlineLoanCOBBuildExecutionContextTasklet<WorkingCapitalLoan, WorkingCapitalLoanCOBBusinessStep> inlineWorkingCapitalLoanCOBBuildExecutionContextTasklet,
            ExecutionContextPromotionListener inlineWorkingCapitalLoanCobPromotionListener) {
        return new StepBuilder("Inline COB build execution context step", jobRepository)
                .tasklet(inlineWorkingCapitalLoanCOBBuildExecutionContextTasklet, transactionManager)
                .listener(inlineWorkingCapitalLoanCobPromotionListener).build();
    }

    @Bean
    public Step inlineWorkingCapitalLoanCOBStep(WorkingCapitalInlineCOBLoanItemReader inlineWorkingCapitalLoanCobWorkerItemReader,
            WorkingCapitalLoanInlineCOBWorkerItemProcessor inlineWorkingCapitalLoanCobWorkerItemProcessor,
            InlineWorkingCapitalLoanCOBWorkerItemWriter inlineWorkingCapitalLoanCobWorkerItemWriter,
            InlineWorkingCapitalLoanCOBWorkerItemListener inlineWorkingCapitalLoanCobLoanItemListener) {
        return new StepBuilder("Inline Working Capital Loan COB Step", jobRepository)
                .<WorkingCapitalLoan, WorkingCapitalLoan>chunk(propertyService.getChunkSize(JobName.WORKING_CAPITAL_LOAN_COB_JOB.name()),
                        transactionManager)
                .reader(inlineWorkingCapitalLoanCobWorkerItemReader).processor(inlineWorkingCapitalLoanCobWorkerItemProcessor)
                .writer(inlineWorkingCapitalLoanCobWorkerItemWriter).listener(inlineWorkingCapitalLoanCobLoanItemListener).build();
    }

    @Bean(name = "inlineWorkingCapitalLoanCOBJob")
    public Job inlineWorkingCapitalLoanCOBJob(Step inlineWorkingCapitalLoanCOBBuildExecutionContextStep,
            Step inlineWorkingCapitalLoanCOBStep, Step inlineWorkingCapitalLoanCOBResetContextStep) {
        return new JobBuilder(WorkingCapitalLoanCOBConstant.INLINE_WORKING_CAPITAL_LOAN_COB_JOB_NAME, jobRepository) //
                .start(inlineWorkingCapitalLoanCOBBuildExecutionContextStep).next(inlineWorkingCapitalLoanCOBStep)
                .next(inlineWorkingCapitalLoanCOBResetContextStep) //
                .incrementer(new RunIdIncrementer()) //
                .build();
    }

    @JobScope
    @Bean
    public WorkingCapitalInlineCOBLoanItemReader inlineWorkingCapitalLoanCobWorkerItemReader() {
        return new WorkingCapitalInlineCOBLoanItemReader(loanRepository);
    }

    @JobScope
    @Bean
    public WorkingCapitalLoanInlineCOBWorkerItemProcessor inlineWorkingCapitalLoanCobWorkerItemProcessor() {
        return new WorkingCapitalLoanInlineCOBWorkerItemProcessor(cobBusinessStepService);
    }

    @Bean
    public Step inlineWorkingCapitalLoanCOBResetContextStep(ResetContextTasklet inlineWorkingCapitalLoanCOBResetContext) {
        return new StepBuilder("Reset context - Step", jobRepository).tasklet(inlineWorkingCapitalLoanCOBResetContext, transactionManager)
                .build();
    }

    @Bean
    public InlineWorkingCapitalLoanCOBWorkerItemWriter inlineWorkingCapitalLoanCobWorkerItemWriter() {
        return new InlineWorkingCapitalLoanCOBWorkerItemWriter(loanLockingService, loanRepository);
    }

    @Bean
    public InlineWorkingCapitalLoanCOBWorkerItemListener inlineWorkingCapitalLoanCobLoanItemListener() {
        return new InlineWorkingCapitalLoanCOBWorkerItemListener(loanLockingService, transactionTemplate);
    }

    @Bean
    public ResetContextTasklet inlineWorkingCapitalLoanCOBResetContext() {
        return new ResetContextTasklet();
    }

    @Bean
    public ExecutionContextPromotionListener inlineWorkingCapitalLoanCobPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] { COBConstant.COB_PARAMETER, COBConstant.BUSINESS_STEPS, COBConstant.BUSINESS_DATE_PARAMETER_NAME });
        return listener;
    }
}
