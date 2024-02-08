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
package org.apache.fineract.portfolio.loanaccount.jobs.recalculateinterestforloan;

import org.apache.fineract.infrastructure.core.config.TaskExecutorConstant;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.RecalculateInterestPoster;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class RecalculateInterestForLoanConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private LoanReadPlatformService loanReadPlatformService;

    @Autowired
    private LoanWritePlatformService loanWritePlatformService;

    @Autowired
    private RecalculateInterestPoster recalculateInterestPoster;

    @Autowired
    private OfficeReadPlatformService officeReadPlatformService;

    @Autowired
    @Qualifier(TaskExecutorConstant.DEFAULT_TASK_EXECUTOR_BEAN_NAME)
    private ThreadPoolTaskExecutor taskExecutor;

    @Bean
    protected Step recalculateInterestForLoanStep() {
        return new StepBuilder(JobName.RECALCULATE_INTEREST_FOR_LOAN.name(), jobRepository)
                .tasklet(recalculateInterestForLoanTasklet(), transactionManager).build();
    }

    @Bean
    public Job recalculateInterestForLoanJob() {
        return new JobBuilder(JobName.RECALCULATE_INTEREST_FOR_LOAN.name(), jobRepository).start(recalculateInterestForLoanStep())
                .incrementer(new RunIdIncrementer()).build();
    }

    @Bean
    public RecalculateInterestForLoanTasklet recalculateInterestForLoanTasklet() {
        return new RecalculateInterestForLoanTasklet(loanReadPlatformService, loanWritePlatformService, recalculateInterestPoster,
                officeReadPlatformService, taskExecutor);
    }
}
