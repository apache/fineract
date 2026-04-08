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
package org.apache.fineract.cob.workingcapitalloan;

import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_JOB_HUMAN_READABLE_NAME;
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_JOB_NAME;
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_PARTITIONER;
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_PARTITIONER_STEP;
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_STEP;
import static org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanCOBConstant.WORKING_CAPITAL_LOAN_COB_WORKER_STEP;
import static org.apache.fineract.infrastructure.jobs.service.JobName.WORKING_CAPITAL_LOAN_COB_JOB;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.conditions.BatchManagerCondition;
import org.apache.fineract.infrastructure.springbatch.PropertyService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchIntegration
@Conditional(BatchManagerCondition.class)
@RequiredArgsConstructor
public class WorkingCapitalLoanCOBManagerConfiguration {

    private final JobRepository jobRepository;

    private final CustomJobParameterResolver customJobParameterResolver;

    private final PlatformTransactionManager transactionManager;
    private final RemotePartitioningManagerStepBuilderFactory stepBuilderFactory;
    private final COBBusinessStepService cobBusinessStepService;
    private final JobOperator jobOperator;
    private final DirectChannel inboundRequests;

    private final DirectChannel outboundRequests;
    private final PropertyService propertyService;
    private final WorkingCapitalLoanRetrieveIdService retrieveIdService;

    @Bean(WORKING_CAPITAL_LOAN_COB_PARTITIONER)
    @StepScope
    public WorkingCapitalLoanCOBPartitioner workingCapitalLoanCOBPartitioner(@Value("#{stepExecution}") StepExecution stepExecution) {
        return new WorkingCapitalLoanCOBPartitioner(jobOperator, stepExecution, WorkingCapitalLoanCOBConstant.NUMBER_OF_DAYS_BEHIND,
                retrieveIdService, cobBusinessStepService, propertyService);
    }

    @Bean(WORKING_CAPITAL_JOB_HUMAN_READABLE_NAME)
    public Job workingCapitalLoanCOBJob(WorkingCapitalLoanCOBPartitioner workingCapitalLoanCOBPartitioner,
            ExecutionContextPromotionListener customJobParametersPromotionListener) {
        return new JobBuilder(WORKING_CAPITAL_LOAN_COB_JOB.name(), jobRepository)
                .start(resolveCustomJobParametersForWorkingCapitalStep(customJobParametersPromotionListener))
                .next(workingCapitalLoanCOBStep(workingCapitalLoanCOBPartitioner)).incrementer(new RunIdIncrementer()) //
                .build();
    }

    @Bean
    public WorkingCapitalLoanCOBCustomJobParametersResolverTasklet resolveCustomJobParametersForWorkingCapitalTasklet() {
        return new WorkingCapitalLoanCOBCustomJobParametersResolverTasklet(customJobParameterResolver);
    }

    @Bean
    public Step resolveCustomJobParametersForWorkingCapitalStep(ExecutionContextPromotionListener customJobParametersPromotionListener) {
        return new StepBuilder("Resolve custom job parameters - Step", jobRepository)
                .tasklet(resolveCustomJobParametersForWorkingCapitalTasklet(), transactionManager)
                .listener(customJobParametersPromotionListener).build();
    }

    @Bean(WORKING_CAPITAL_LOAN_COB_STEP)
    public Step workingCapitalLoanCOBStep(WorkingCapitalLoanCOBPartitioner partitioner) {
        return stepBuilderFactory.get(WORKING_CAPITAL_LOAN_COB_PARTITIONER_STEP)//
                .partitioner(WORKING_CAPITAL_LOAN_COB_WORKER_STEP, partitioner)//
                .pollInterval(propertyService.getPollInterval(WORKING_CAPITAL_JOB_NAME))//
                .outputChannel(outboundRequests).build();//
    }
}
