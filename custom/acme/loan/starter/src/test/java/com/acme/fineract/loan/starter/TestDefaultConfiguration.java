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
package com.acme.fineract.loan.starter;

import static org.mockito.Mockito.mock;

import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBBusinessStepServiceImpl;
import org.apache.fineract.cob.domain.BatchBusinessStepRepository;
import org.apache.fineract.cob.service.ReloaderService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingConfiguration;
import org.apache.fineract.infrastructure.core.diagnostics.performance.sampling.core.SamplingServiceFactory;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties({ FineractProperties.class })
public class TestDefaultConfiguration {

    @Bean
    public BatchBusinessStepRepository batchBusinessStepRepository() {
        return mock(BatchBusinessStepRepository.class);
    }

    @Bean
    public BusinessEventNotifierService businessEventNotifierService() {
        return mock(BusinessEventNotifierService.class);
    }

    @Bean
    public COBBusinessStepService cobBusinessStepService(BatchBusinessStepRepository batchBusinessStepRepository,
            ApplicationContext context, ListableBeanFactory beanFactory, BusinessEventNotifierService businessEventNotifierService,
            ConfigurationDomainService configurationDomainService, ReloaderService reloaderService) {
        return new COBBusinessStepServiceImpl(batchBusinessStepRepository, context, beanFactory, businessEventNotifierService,
                configurationDomainService, reloaderService);
    }

    @Bean
    public SamplingServiceFactory samplingServiceFactory(SamplingConfiguration samplingConfiguration) {
        return new SamplingServiceFactory(samplingConfiguration);
    }

    @Bean
    public LoanAccountDomainService loanAccountDomainService() {
        return mock(LoanAccountDomainService.class);
    }
}
