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

import org.apache.fineract.cob.conditions.BatchWorkerCondition;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.apache.fineract.portfolio.account.data.DueStandingInstruction;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Worker half of the standing-instruction job: it executes the instructions of one partition.
 *
 * <p>
 * The step is chunk oriented, so a chunk of instructions is attempted in a single transaction and, if any of them
 * fails, replayed one instruction per transaction — the fallback is Spring Batch's own, not hand written. Only
 * transient failures are retried; an instruction that cannot be paid is skipped and recorded against its mandate,
 * because a mandate that fails is an outcome of that mandate and not a failure of the run.
 * </p>
 */
@Configuration
@Conditional(BatchWorkerCondition.class)
public class ExecuteStandingInstructionsWorkerConfig {

    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;
    @Autowired
    private QueueChannel inboundRequests;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private StandingInstructionReadPlatformService standingInstructionReadPlatformService;
    @Autowired
    private StandingInstructionExecutionService standingInstructionExecutionService;

    @Bean(name = ExecuteStandingInstructionsConstant.WORKER_STEP)
    public Step executeStandingInstructionsWorkerStep() {
        final int chunkSize = propertyService.getChunkSize(ExecuteStandingInstructionsConstant.JOB_NAME);
        return stepBuilderFactory.get("Execute Standing Instructions worker - Step") //
                .inputChannel(inboundRequests) //
                .<StandingInstructionData, DueStandingInstruction>chunk(chunkSize, transactionManager) //
                .reader(standingInstructionItemReader()) //
                .processor(standingInstructionItemProcessor()) //
                .writer(standingInstructionItemWriter()) //
                .faultTolerant() //
                // Retry only what a second attempt can plausibly fix. An instruction short of funds is not retried:
                // the balance will not have changed by the next attempt, and the retries would only delay the run and
                // pad the mandate's history.
                .retry(TransientDataAccessException.class) //
                .retry(ConcurrencyFailureException.class) //
                .retry(AbstractPlatformServiceUnavailableException.class) //
                .retryLimit(propertyService.getRetryLimit(ExecuteStandingInstructionsConstant.JOB_NAME)) //
                .skipPolicy(new StandingInstructionSkipPolicy()) //
                .listener(standingInstructionItemListener()) //
                .build();
    }

    @Bean
    @StepScope
    public StandingInstructionItemReader standingInstructionItemReader() {
        return new StandingInstructionItemReader(propertyService.getChunkSize(ExecuteStandingInstructionsConstant.JOB_NAME),
                standingInstructionReadPlatformService);
    }

    @Bean
    @StepScope
    public StandingInstructionItemProcessor standingInstructionItemProcessor() {
        return new StandingInstructionItemProcessor(standingInstructionReadPlatformService);
    }

    @Bean
    @StepScope
    public StandingInstructionItemWriter standingInstructionItemWriter() {
        return new StandingInstructionItemWriter(standingInstructionExecutionService);
    }

    @Bean
    public StandingInstructionItemListener standingInstructionItemListener() {
        return new StandingInstructionItemListener(standingInstructionExecutionService);
    }
}
