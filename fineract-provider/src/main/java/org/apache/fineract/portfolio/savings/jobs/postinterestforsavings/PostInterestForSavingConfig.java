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
package org.apache.fineract.portfolio.savings.jobs.postinterestforsavings;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsSchedularInterestPosterTask;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PostInterestForSavingConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private SavingsAccountReadPlatformService savingAccountReadPlatformService;
    @Autowired
    private ConfigurationDomainService configurationDomainService;
    @Autowired
    private SavingsSchedularInterestPosterTask savingsSchedularInterestPosterTask;
    @Autowired
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Autowired
    private SavingsAccountRepositoryWrapper savingsAccountRepository;
    @Autowired
    private SavingsAccountAssembler savingAccountAssembler;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Bean
    protected Step postInterestForSavingStep(PostInterestForSavingTasklet postInterestForSavingTasklet) {
        return new StepBuilder(JobName.POST_INTEREST_FOR_SAVINGS.name(), jobRepository)
                .tasklet(postInterestForSavingTasklet, transactionManager).build();
    }

    @Bean
    public Job postInterestForSavingJob(PostInterestForSavingTasklet postInterestForSavingTasklet) {
        return new JobBuilder(JobName.POST_INTEREST_FOR_SAVINGS.name(), jobRepository)
                .start(postInterestForSavingStep(postInterestForSavingTasklet)).incrementer(new RunIdIncrementer()).build();
    }
}
