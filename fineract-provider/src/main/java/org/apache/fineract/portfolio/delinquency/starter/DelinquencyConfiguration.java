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
package org.apache.fineract.portfolio.delinquency.starter;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketMappingsRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformServiceImpl;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformServiceHelper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformServiceImpl;
import org.apache.fineract.portfolio.delinquency.service.LoanDelinquencyDomainService;
import org.apache.fineract.portfolio.delinquency.service.LoanDelinquencyDomainServiceImpl;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyBucketParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyRangeParseAndValidator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DelinquencyConfiguration {

    @Bean
    @ConditionalOnMissingBean(DelinquencyReadPlatformService.class)
    public DelinquencyReadPlatformService delinquencyReadPlatformService(DelinquencyRangeRepository repositoryRange,
            DelinquencyBucketRepository repositoryBucket, LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory,
            DelinquencyRangeMapper mapperRange, DelinquencyBucketMapper mapperBucket,
            LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory, LoanRepository loanRepository,
            LoanDelinquencyDomainService loanDelinquencyDomainService,
            LoanInstallmentDelinquencyTagRepository repositoryLoanInstallmentDelinquencyTag,
            LoanDelinquencyActionRepository loanDelinquencyActionRepository,
            DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper, ConfigurationDomainService configurationDomainService) {
        return new DelinquencyReadPlatformServiceImpl(repositoryRange, repositoryBucket, repositoryLoanDelinquencyTagHistory, mapperRange,
                mapperBucket, mapperLoanDelinquencyTagHistory, loanRepository, loanDelinquencyDomainService,
                repositoryLoanInstallmentDelinquencyTag, loanDelinquencyActionRepository, delinquencyEffectivePauseHelper,
                configurationDomainService);
    }

    @Bean
    @ConditionalOnMissingBean(DelinquencyWritePlatformService.class)
    public DelinquencyWritePlatformService delinquencyWritePlatformService(DelinquencyBucketParseAndValidator dataValidatorBucket,
            DelinquencyRangeParseAndValidator dataValidatorRange, DelinquencyRangeRepository repositoryRange,
            DelinquencyBucketRepository repositoryBucket, DelinquencyBucketMappingsRepository repositoryBucketMappings,
            LoanDelinquencyTagHistoryRepository loanDelinquencyTagRepository, LoanRepositoryWrapper loanRepository,
            LoanProductRepository loanProductRepository, BusinessEventNotifierService businessEventNotifierService,
            LoanDelinquencyDomainService loanDelinquencyDomainService,
            LoanInstallmentDelinquencyTagRepository loanInstallmentDelinquencyTagRepository,
            DelinquencyReadPlatformService delinquencyReadPlatformService, LoanDelinquencyActionRepository loanDelinquencyActionRepository,
            DelinquencyActionParseAndValidator delinquencyActionParseAndValidator,
            DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper,
            DelinquencyWritePlatformServiceHelper delinquencyWritePlatformServiceHelper) {
        return new DelinquencyWritePlatformServiceImpl(dataValidatorBucket, dataValidatorRange, repositoryRange, repositoryBucket,
                repositoryBucketMappings, loanDelinquencyTagRepository, loanRepository, loanProductRepository, loanDelinquencyDomainService,
                loanInstallmentDelinquencyTagRepository, delinquencyReadPlatformService, loanDelinquencyActionRepository,
                delinquencyActionParseAndValidator, delinquencyEffectivePauseHelper, businessEventNotifierService,
                delinquencyWritePlatformServiceHelper);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDelinquencyDomainService.class)
    public LoanDelinquencyDomainService loanDelinquencyDomainService(DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper) {
        return new LoanDelinquencyDomainServiceImpl(delinquencyEffectivePauseHelper);
    }
}
