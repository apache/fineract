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
package org.apache.fineract.portfolio.savings.jobs.generaterdschedule;

import org.apache.fineract.infrastructure.core.service.database.RoutingDataSourceServiceFactory;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
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
public class GenerateRdScheduleConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private RoutingDataSourceServiceFactory dataSourceServiceFactory;
    @Autowired
    private DepositAccountReadPlatformService depositAccountReadPlatformService;
    @Autowired
    private PlatformSecurityContext securityContext;

    @Bean
    protected Step generateRdScheduleStep() {
        return new StepBuilder(JobName.GENERATE_RD_SCEHDULE.name(), jobRepository).tasklet(generateRdScheduleTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Job generateRdScheduleJob() {
        return new JobBuilder(JobName.GENERATE_RD_SCEHDULE.name(), jobRepository).start(generateRdScheduleStep())
                .incrementer(new RunIdIncrementer()).build();
    }

    @Bean
    public GenerateRdScheduleTasklet generateRdScheduleTasklet() {
        return new GenerateRdScheduleTasklet(dataSourceServiceFactory, depositAccountReadPlatformService, securityContext);
    }
}
