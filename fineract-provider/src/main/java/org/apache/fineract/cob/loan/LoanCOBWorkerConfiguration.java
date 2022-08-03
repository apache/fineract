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

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBPropertyService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;

@Configuration
@ConditionalOnProperty(value = "fineract.mode.batch-worker-enabled", havingValue = "true")
public class LoanCOBWorkerConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;
    @Autowired
    private COBPropertyService cobPropertyService;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private QueueChannel inboundRequests;
    @Autowired
    private COBBusinessStepService cobBusinessStepService;

    @Bean(name = "Loan COB worker")
    public Step loanCOBWorkerStep() {
        return stepBuilderFactory.get("Loan COB worker").inputChannel(inboundRequests)
                .<Loan, Loan>chunk(cobPropertyService.getChunkSize(JobName.LOAN_COB.name())).reader(itemReader(null))
                .processor(itemProcessor()).writer(itemWriter()).build();
    }

    @Bean
    public Job loanCOBWorkerJob() {
        return jobBuilderFactory.get("Loan COB worker").start(loanCOBWorkerStep()).incrementer(new RunIdIncrementer()).build();
    }

    @Bean
    @StepScope
    public ItemReader<Loan> itemReader(@Value("#{stepExecutionContext['loanIds']}") List<Integer> data) {
        List<Integer> remainingData = new ArrayList<>(data);
        return () -> {
            if (remainingData.size() > 0) {
                return loanRepository.findById(remainingData.remove(0).longValue()).orElse(null);
            }
            return null;
        };
    }

    @Bean
    public ItemProcessor<Loan, Loan> itemProcessor() {
        return new LoanItemProcessor(cobBusinessStepService);
    }

    @Bean
    public RepositoryItemWriter<Loan> itemWriter() {
        RepositoryItemWriter<Loan> writer = new RepositoryItemWriter<>();
        writer.setRepository(loanRepository);
        writer.setMethodName("save");
        return writer;
    }
}
