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
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.common.ResetContextTasklet;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.listener.ChunkProcessingSavingsItemListener;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class SavingsInlineCOBConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private SavingsAccountRepository savingsAccountRepository;
    @Autowired
    private SavingsAccountAssembler savingsAccountAssembler;
    @Autowired
    private COBBusinessStepService cobBusinessStepService;
    @Autowired
    @Qualifier("requiresNewTransactionJdbcTemplate")
    private TransactionTemplate requiresNewTransactionJdbcTemplate;
    @Autowired
    private CustomJobParameterRepository customJobParameterRepository;
    @Autowired
    private CustomJobParameterResolver customJobParameterResolver;
    @Autowired
    @Qualifier("savingsLockingService")
    private LockingService savingsLockingService;

    @Bean
    public InlineSavingsCOBBuildExecutionContextTasklet inlineSavingsCOBBuildExecutionContextTasklet() {
        return new InlineSavingsCOBBuildExecutionContextTasklet(cobBusinessStepService, customJobParameterRepository,
                customJobParameterResolver);
    }

    @Bean
    protected Step inlineSavingsCOBBuildExecutionContextStep() {
        return new StepBuilder("Inline Savings COB build execution context step", jobRepository)
                .tasklet(inlineSavingsCOBBuildExecutionContextTasklet(), transactionManager).listener(inlineSavingsCobPromotionListener())
                .build();
    }

    @Bean
    public Step inlineSavingsCOBStep() {
        return new StepBuilder("Inline Savings COB Step", jobRepository)
                .<SavingsAccount, SavingsAccount>chunk(propertyService.getChunkSize(JobName.SAVINGS_COB.name()), transactionManager)
                .reader(inlineCobSavingsItemReader()).processor(inlineCobSavingsItemProcessor()).writer(inlineCobSavingsItemWriter())
                .listener(inlineSavingsItemListener()).build();
    }

    @Bean(name = "savingsInlineCOBJob")
    public Job savingsInlineCOBJob() {
        return new JobBuilder(SavingsCOBConstant.INLINE_SAVINGS_COB_JOB_NAME, jobRepository) //
                .start(inlineSavingsCOBBuildExecutionContextStep()).next(inlineSavingsCOBStep()).next(inlineSavingsCOBResetContextStep()) //
                .incrementer(new RunIdIncrementer()) //
                .build();
    }

    @JobScope
    @Bean
    public InlineCOBSavingsItemReader inlineCobSavingsItemReader() {
        return new InlineCOBSavingsItemReader(savingsAccountRepository, savingsAccountAssembler);
    }

    @JobScope
    @Bean
    public InlineCOBSavingsItemProcessor inlineCobSavingsItemProcessor() {
        return new InlineCOBSavingsItemProcessor(cobBusinessStepService);
    }

    @Bean
    public InlineCOBSavingsItemWriter inlineCobSavingsItemWriter() {
        InlineCOBSavingsItemWriter repositoryItemWriter = new InlineCOBSavingsItemWriter(savingsLockingService);
        repositoryItemWriter.setRepository(savingsAccountRepository);
        return repositoryItemWriter;
    }

    @Bean
    public Step inlineSavingsCOBResetContextStep() {
        return new StepBuilder("Inline Savings COB reset context step", jobRepository)
                .tasklet(inlineSavingsCOBResetContext(), transactionManager).build();
    }

    @Bean
    public ChunkProcessingSavingsItemListener inlineSavingsItemListener() {
        return new ChunkProcessingSavingsItemListener(savingsLockingService, requiresNewTransactionJdbcTemplate);
    }

    @Bean
    public ResetContextTasklet inlineSavingsCOBResetContext() {
        return new ResetContextTasklet();
    }

    @Bean
    public ExecutionContextPromotionListener inlineSavingsCobPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] { COBConstant.COB_PARAMETER, COBConstant.BUSINESS_STEPS, COBConstant.BUSINESS_DATE_PARAMETER_NAME });
        return listener;
    }
}
