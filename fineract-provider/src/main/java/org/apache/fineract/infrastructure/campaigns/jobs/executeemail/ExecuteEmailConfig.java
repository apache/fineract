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
package org.apache.fineract.infrastructure.campaigns.jobs.executeemail;

import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageRepository;
import org.apache.fineract.infrastructure.campaigns.email.service.EmailMessageJobEmailService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.reportmailingjob.validation.ReportMailingJobValidator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecuteEmailConfig {

    @Autowired
    private JobBuilderFactory jobs;
    @Autowired
    private StepBuilderFactory steps;
    @Autowired
    private EmailMessageRepository emailMessageRepository;
    @Autowired
    private EmailCampaignRepository emailCampaignRepository;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private SavingsAccountRepository savingsAccountRepository;
    @Autowired
    private EmailMessageJobEmailService emailMessageJobEmailService;
    @Autowired
    private ReadReportingService readReportingService;
    @Autowired
    private ReportMailingJobValidator reportMailingJobValidator;

    @Bean
    protected Step executeEmailStep() {
        return steps.get(JobName.EXECUTE_EMAIL.name()).tasklet(executeEmailTasklet()).build();
    }

    @Bean
    public Job executeEmailJob() {
        return jobs.get(JobName.EXECUTE_EMAIL.name()).start(executeEmailStep()).incrementer(new RunIdIncrementer()).build();
    }

    @Bean
    public ExecuteEmailTasklet executeEmailTasklet() {
        return new ExecuteEmailTasklet(emailMessageRepository, emailCampaignRepository, loanRepository, savingsAccountRepository,
                emailMessageJobEmailService, readReportingService, reportMailingJobValidator);
    }
}
