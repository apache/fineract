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
package org.apache.fineract.infrastructure.campaigns.jobs.executereportmailingjobs;

import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.report.provider.ReportingProcessServiceProvider;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRepository;
import org.apache.fineract.infrastructure.reportmailingjob.domain.ReportMailingJobRunHistoryRepository;
import org.apache.fineract.infrastructure.reportmailingjob.service.ReportMailingJobEmailService;
import org.apache.fineract.infrastructure.reportmailingjob.validation.ReportMailingJobValidator;
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
public class ExecuteReportMailingJobsConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ReportMailingJobRepository reportMailingJobRepository;
    @Autowired
    private ReportMailingJobValidator reportMailingJobValidator;
    @Autowired
    private ReadReportingService readReportingService;
    @Autowired
    private ReportingProcessServiceProvider reportingProcessServiceProvider;
    @Autowired
    private ReportMailingJobEmailService reportMailingJobEmailService;
    @Autowired
    private ReportMailingJobRunHistoryRepository reportMailingJobRunHistoryRepository;
    @Autowired
    private FineractProperties fineractProperties;

    @Bean
    protected Step executeReportMailingJobsStep() {
        return new StepBuilder(JobName.EXECUTE_REPORT_MAILING_JOBS.name(), jobRepository)
                .tasklet(executeReportMailingJobsTasklet(), transactionManager).build();
    }

    @Bean
    public Job executeReportMailingJobsJob() {
        return new JobBuilder(JobName.EXECUTE_REPORT_MAILING_JOBS.name(), jobRepository).start(executeReportMailingJobsStep())
                .incrementer(new RunIdIncrementer()).build();
    }

    @Bean
    public ExecuteReportMailingJobsTasklet executeReportMailingJobsTasklet() {
        return new ExecuteReportMailingJobsTasklet(reportMailingJobRepository, reportMailingJobValidator, readReportingService,
                reportingProcessServiceProvider, reportMailingJobEmailService, reportMailingJobRunHistoryRepository, fineractProperties);
    }
}
