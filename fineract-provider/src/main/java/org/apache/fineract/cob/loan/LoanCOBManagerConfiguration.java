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
import org.apache.fineract.cob.COBPropertyService;
import org.apache.fineract.cob.listener.COBExecutionListenerRunner;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;

@Configuration
@EnableBatchIntegration
@ConditionalOnProperty(value = "fineract.mode.batch-manager-enabled", havingValue = "true")
public class LoanCOBManagerConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private RemotePartitioningManagerStepBuilderFactory stepBuilderFactory;
    @Autowired
    private COBPropertyService cobPropertyService;
    @Autowired
    private DirectChannel outboundRequests;
    @Autowired
    private COBBusinessStepService cobBusinessStepService;
    @Autowired
    private JobOperator jobOperator;
    @Autowired
    private JobExplorer jobExplorer;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired(required = false)
    private RetrieveLoanIdService retrieveLoanIdService;

    @Bean
    public LoanCOBPartitioner partitioner() {
        return new LoanCOBPartitioner(cobPropertyService, cobBusinessStepService, jobOperator, jobExplorer, retrieveLoanIdService);
    }

    @Bean
    public Step loanCOBStep() {
        return stepBuilderFactory.get(JobName.LOAN_COB.name()).partitioner("Loan COB worker", partitioner()).outputChannel(outboundRequests)
                .build();
    }

    @Bean(name = "loanCOBJob")
    public Job loanCOBJob() {
        return jobBuilderFactory.get(JobName.LOAN_COB.name()) //
                .listener(new COBExecutionListenerRunner(applicationContext, JobName.LOAN_COB.name())) //
                .start(loanCOBStep()) //
                .incrementer(new RunIdIncrementer()) //
                .build();
    }

}
