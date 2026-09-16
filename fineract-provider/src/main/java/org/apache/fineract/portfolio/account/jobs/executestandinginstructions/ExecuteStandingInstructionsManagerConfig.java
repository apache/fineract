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

import org.apache.fineract.cob.conditions.BatchManagerCondition;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;

/**
 * Manager half of the standing-instruction job: it cuts the due set into partitions and hands them to workers.
 */
@Configuration
@EnableBatchIntegration
@Conditional(BatchManagerCondition.class)
public class ExecuteStandingInstructionsManagerConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private RemotePartitioningManagerStepBuilderFactory stepBuilderFactory;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private DirectChannel outboundRequests;
    @Autowired
    private StandingInstructionReadPlatformService standingInstructionReadPlatformService;

    @Bean
    public StandingInstructionPartitioner standingInstructionPartitioner() {
        return new StandingInstructionPartitioner(propertyService, standingInstructionReadPlatformService);
    }

    @Bean
    public Step executeStandingInstructionsPartitionerStep() {
        return stepBuilderFactory.get(ExecuteStandingInstructionsConstant.PARTITIONER_STEP)
                .partitioner(ExecuteStandingInstructionsConstant.WORKER_STEP, standingInstructionPartitioner())
                .pollInterval(propertyService.getPollInterval(ExecuteStandingInstructionsConstant.JOB_NAME)).outputChannel(outboundRequests)
                .build();
    }

    @Bean
    public Job executeStandingInstructionsJob() {
        return new JobBuilder(JobName.EXECUTE_STANDING_INSTRUCTIONS.name(), jobRepository)
                .start(executeStandingInstructionsPartitionerStep()).incrementer(new RunIdIncrementer()).build();
    }
}
