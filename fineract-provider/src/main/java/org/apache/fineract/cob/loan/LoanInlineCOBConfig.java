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
import org.apache.fineract.cob.common.ResetContextTasklet;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.listener.InlineCOBLoanItemListener;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableBatchIntegration
public class LoanInlineCOBConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private COBBusinessStepService cobBusinessStepService;
    @Autowired
    private LoanAccountLockRepository accountLockRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private CustomJobParameterRepository loanIdListRepository;

    @Bean
    public InlineLoanCOBBuildExecutionContextTasklet inlineLoanCOBBuildExecutionContextTasklet() {
        return new InlineLoanCOBBuildExecutionContextTasklet(cobBusinessStepService, loanIdListRepository);
    }

    @Bean
    protected Step inlineCOBBuildExecutionContextStep() {
        return stepBuilderFactory.get("Inline COB build execution context step").tasklet(inlineLoanCOBBuildExecutionContextTasklet())
                .listener(inlineCobPromotionListener()).build();
    }

    @Bean
    public Step inlineLoanCOBStep() {
        return stepBuilderFactory.get("Inline Loan COB Step").<Loan, Loan>chunk(propertyService.getChunkSize(JobName.LOAN_COB.name()))
                .reader(inlineCobWorkerItemReader()).processor(inlineCobWorkerItemProcessor()).writer(inlineCobWorkerItemWriter())
                .listener(inlineCobLoanItemListener()).build();
    }

    @Bean(name = "loanInlineCOBJob")
    public Job loanInlineCOBJob() {
        return jobBuilderFactory.get(LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME) //
                .start(inlineCOBBuildExecutionContextStep()).next(inlineLoanCOBStep()).next(inlineCOBResetContextStep()) //
                .incrementer(new RunIdIncrementer()) //
                .build();
    }

    @Bean
    public InlineCOBLoanItemReader inlineCobWorkerItemReader() {
        return new InlineCOBLoanItemReader(loanRepository);
    }

    @Bean
    public InlineCOBLoanItemProcessor inlineCobWorkerItemProcessor() {
        return new InlineCOBLoanItemProcessor(cobBusinessStepService);
    }

    @Bean
    public Step inlineCOBResetContextStep() {
        return stepBuilderFactory.get("Reset context - Step").tasklet(inlineCOBResetContext()).build();
    }

    @Bean
    public InlineCOBLoanItemWriter inlineCobWorkerItemWriter() {
        InlineCOBLoanItemWriter repositoryItemWriter = new InlineCOBLoanItemWriter(accountLockRepository);
        repositoryItemWriter.setRepository(loanRepository);
        return repositoryItemWriter;
    }

    @Bean
    public InlineCOBLoanItemListener inlineCobLoanItemListener() {
        return new InlineCOBLoanItemListener(accountLockRepository, transactionTemplate);
    }

    @Bean
    public ResetContextTasklet inlineCOBResetContext() {
        return new ResetContextTasklet();
    }

    @Bean
    public ExecutionContextPromotionListener inlineCobPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(
                new String[] { LoanCOBConstant.LOAN_IDS, LoanCOBConstant.BUSINESS_STEP_MAP, LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME });
        return listener;
    }
}
