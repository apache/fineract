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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob;

import static org.apache.fineract.infrastructure.jobs.service.aggregationjob.JournalEntryAggregationJobConstant.JOB_SUMMARY_STEP_NAME;
import static org.apache.fineract.infrastructure.jobs.service.aggregationjob.JournalEntryAggregationJobConstant.JOB_TRACKING_STEP_NAME;

import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.migration.TenantDataSourceFactory;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationSummaryData;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.listener.JournalEntryAggregationJobListener;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.tasklet.JournalEntryAggregationTrackingTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnProperty(value = "fineract.job.journal-entry-aggregation.enabled", havingValue = "true")
public class JournalEntryAggregationJobConfiguration {

    @Autowired
    private JournalEntryAggregationJobListener journalEntryAggregationJobListener;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JournalEntryAggregationJobExecutionDecider journalEntryAggregationJobExecutionDecider;
    @Autowired
    private JournalEntryAggregationJobWriter aggregationItemWriter;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private FineractProperties fineractProperties;
    @Autowired
    private JournalEntryAggregationTrackingTasklet journalEntryAggregationTrackingTasklet;
    @Autowired
    private TenantDataSourceFactory tenantDataSourceFactory;

    @Bean
    public Step journalEntryAggregationSummaryStep() {
        return new StepBuilder(JOB_SUMMARY_STEP_NAME, jobRepository)
                .<JournalEntryAggregationSummaryData, JournalEntryAggregationSummaryData>chunk(
                        fineractProperties.getJob().getJournalEntryAggregation().getChunkSize(), transactionManager)
                .reader(journalEntryAggregationJobReader()).writer(aggregationItemWriter).allowStartIfComplete(true).build();
    }

    @Bean
    public JournalEntryAggregationJobReader journalEntryAggregationJobReader() {
        return new JournalEntryAggregationJobReader(tenantDataSourceFactory);
    }

    @Bean
    protected Step journalEntryAggregationTrackingStep() {
        return new StepBuilder(JOB_TRACKING_STEP_NAME, jobRepository).tasklet(journalEntryAggregationTrackingTasklet, transactionManager)
                .build();
    }

    @Bean(name = "journalEntryAggregation")
    public Job journalEntryAggregation() {
        return new JobBuilder(JobName.JOURNAL_ENTRY_AGGREGATION.name(), jobRepository).listener(journalEntryAggregationJobListener)
                .start(journalEntryAggregationJobExecutionDecider).on(JournalEntryAggregationJobConstant.NO_OP_EXECUTION).end()
                .from(journalEntryAggregationJobExecutionDecider).on(JournalEntryAggregationJobConstant.CONTINUE_JOB_EXECUTION)
                .to(journalEntryAggregationSummaryStep()).next(journalEntryAggregationTrackingStep()).end()
                .incrementer(new RunIdIncrementer()).build();
    }

}
