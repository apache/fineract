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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstructionCustomRepositoryImpl;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstructionsHistoryCustomRepositoryImpl;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ExecuteStandingInstructionsConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private StandingInstructionReadPlatformService standingInstructionReadPlatformService;
    @Autowired
    private AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    @Autowired
    private AccountTransferStandingInstructionCustomRepositoryImpl accountTransferStandingInstructionCustomRepositoryImpl;
    @Autowired
    private AccountTransferStandingInstructionsHistoryCustomRepositoryImpl accountTransferStandingInstructionsHistoryCustomRepositoryImpl;

    @Bean
    protected Step executeStandingInstructionsStep() {
        return new StepBuilder(JobName.EXECUTE_STANDING_INSTRUCTIONS.name(), jobRepository)
                .tasklet(executeStandingInstructionsTasklet(), transactionManager).build();
    }

    @Bean
    public Job executeStandingInstructionsJob() {
        return new JobBuilder(JobName.EXECUTE_STANDING_INSTRUCTIONS.name(), jobRepository).start(executeStandingInstructionsStep())
                .incrementer(new RunIdIncrementer()).build();
    }

    @Bean
    public ExecuteStandingInstructionsTasklet executeStandingInstructionsTasklet() {
        return new ExecuteStandingInstructionsTasklet(standingInstructionReadPlatformService, accountTransfersWritePlatformService,
                accountTransferStandingInstructionCustomRepositoryImpl, accountTransferStandingInstructionsHistoryCustomRepositoryImpl);
    }
}
