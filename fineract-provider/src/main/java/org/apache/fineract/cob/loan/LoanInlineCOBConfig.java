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
import org.apache.fineract.cob.common.ResetContextTasklet;
import org.apache.fineract.cob.conditions.LoanCOBEnabledCondition;
import org.apache.fineract.cob.listener.InlineCOBLoanItemListener;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableBatchIntegration
@Conditional(LoanCOBEnabledCondition.class)
public class LoanInlineCOBConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private COBBusinessStepService cobBusinessStepService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private CustomJobParameterRepository customJobParameterRepository;
    @Autowired
    private CustomJobParameterResolver customJobParameterResolver;

    @Autowired
    private LoanLockingService loanLockingService;

    @Bean
    public InlineLoanCOBBuildExecutionContextTasklet inlineLoanCOBBuildExecutionContextTasklet() {
        return new InlineLoanCOBBuildExecutionContextTasklet(cobBusinessStepService, customJobParameterRepository,
                customJobParameterResolver);
    }

    @Bean
    protected Step inlineCOBBuildExecutionContextStep() {
        return new StepBuilder("Inline COB build execution context step", jobRepository)
                .tasklet(inlineLoanCOBBuildExecutionContextTasklet(), transactionManager).listener(inlineCobPromotionListener()).build();
    }

    @Bean
    public Step inlineLoanCOBStep() {
        return new StepBuilder("Inline Loan COB Step", jobRepository)
                .<Loan, Loan>chunk(propertyService.getChunkSize(JobName.LOAN_COB.name()), transactionManager)
                .reader(inlineCobWorkerItemReader()).processor(inlineCobWorkerItemProcessor()).writer(inlineCobWorkerItemWriter())
                .listener(inlineCobLoanItemListener()).build();
    }

    @Bean(name = "loanInlineCOBJob")
    public Job loanInlineCOBJob() {
        return new JobBuilder(LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME, jobRepository) //
                .start(inlineCOBBuildExecutionContextStep()).next(inlineLoanCOBStep()).next(inlineCOBResetContextStep()) //
                .incrementer(new RunIdIncrementer()) //
                .build();
    }

    @JobScope
    @Bean
    public InlineCOBLoanItemReader inlineCobWorkerItemReader() {
        return new InlineCOBLoanItemReader(loanRepository);
    }

    @JobScope
    @Bean
    public InlineCOBLoanItemProcessor inlineCobWorkerItemProcessor() {
        return new InlineCOBLoanItemProcessor(cobBusinessStepService);
    }

    @Bean
    public Step inlineCOBResetContextStep() {
        return new StepBuilder("Reset context - Step", jobRepository).tasklet(inlineCOBResetContext(), transactionManager).build();
    }

    @Bean
    public InlineCOBLoanItemWriter inlineCobWorkerItemWriter() {
        InlineCOBLoanItemWriter repositoryItemWriter = new InlineCOBLoanItemWriter(loanLockingService);
        repositoryItemWriter.setRepository(loanRepository);
        return repositoryItemWriter;
    }

    @Bean
    public InlineCOBLoanItemListener inlineCobLoanItemListener() {
        return new InlineCOBLoanItemListener(loanLockingService, transactionTemplate);
    }

    @Bean
    public ResetContextTasklet inlineCOBResetContext() {
        return new ResetContextTasklet();
    }

    @Bean
    public ExecutionContextPromotionListener inlineCobPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] { LoanCOBConstant.LOAN_COB_PARAMETER, LoanCOBConstant.BUSINESS_STEPS,
                LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME });
        return listener;
    }
}
