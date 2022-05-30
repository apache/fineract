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
package org.apache.fineract.portfolio.loanaccount.jobs.applychargetooverdueloaninstallment;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplyChargeToOverdueLoanInstallmentConfig {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    @Autowired
    private ConfigurationDomainService configurationDomainService;
    @Autowired
    private LoanReadPlatformService loanReadPlatformService;
    @Autowired
    private LoanWritePlatformService loanWritePlatformService;

    @Bean
    protected Step applyChargeToOverdueLoanInstallmentStep() {
        return steps.get(JobName.APPLY_CHARGE_TO_OVERDUE_LOAN_INSTALLMENT.name()).tasklet(applyChargeToOverdueLoanInstallmentTasklet())
                .build();
    }

    @Bean
    public Job applyChargeToOverdueLoanInstallmentsJob() {
        return jobs.get(JobName.APPLY_CHARGE_TO_OVERDUE_LOAN_INSTALLMENT.name()).start(applyChargeToOverdueLoanInstallmentStep())
                .incrementer(new RunIdIncrementer()).build();
    }

    @Bean
    public ApplyChargeToOverdueLoanInstallmentTasklet applyChargeToOverdueLoanInstallmentTasklet() {
        return new ApplyChargeToOverdueLoanInstallmentTasklet(configurationDomainService, loanReadPlatformService,
                loanWritePlatformService);
    }
}
