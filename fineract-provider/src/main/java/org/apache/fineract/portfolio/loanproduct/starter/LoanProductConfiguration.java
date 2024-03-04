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
package org.apache.fineract.portfolio.loanproduct.starter;

import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductWritePlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.rate.domain.RateRepositoryWrapper;
import org.apache.fineract.portfolio.rate.service.RateReadService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class LoanProductConfiguration {

    @Bean
    @ConditionalOnMissingBean(LoanDropdownReadPlatformService.class)
    public LoanDropdownReadPlatformService loanDropdownReadPlatformService(
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory) {
        return new LoanDropdownReadPlatformServiceImpl(loanRepaymentScheduleTransactionProcessorFactory);
    }

    @Bean
    @ConditionalOnMissingBean(LoanProductReadPlatformService.class)
    public LoanProductReadPlatformService loanProductReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            ChargeReadPlatformService chargeReadPlatformService, RateReadService rateReadService, DatabaseSpecificSQLGenerator sqlGenerator,
            FineractEntityAccessUtil fineractEntityAccessUtil, DelinquencyReadPlatformService delinquencyReadPlatformService,
            LoanProductRepository loanProductRepository) {
        return new LoanProductReadPlatformServiceImpl(context, jdbcTemplate, chargeReadPlatformService, rateReadService, sqlGenerator,
                fineractEntityAccessUtil, delinquencyReadPlatformService, loanProductRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanProductWritePlatformService.class)
    public LoanProductWritePlatformService loanProductWritePlatformService(PlatformSecurityContext context,
            LoanProductDataValidator fromApiJsonDeserializer, LoanProductRepository loanProductRepository, AprCalculator aprCalculator,
            FundRepository fundRepository, ChargeRepositoryWrapper chargeRepository, RateRepositoryWrapper rateRepository,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService,
            FineractEntityAccessUtil fineractEntityAccessUtil, FloatingRateRepositoryWrapper floatingRateRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, BusinessEventNotifierService businessEventNotifierService,
            DelinquencyBucketRepository delinquencyBucketRepository,
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            AdvancedPaymentAllocationsJsonParser advancedPaymentJsonParser, CreditAllocationsJsonParser creditAllocationsJsonParser) {
        return new LoanProductWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer, loanProductRepository, aprCalculator,
                fundRepository, chargeRepository, rateRepository, accountMappingWritePlatformService, fineractEntityAccessUtil,
                floatingRateRepository, loanRepositoryWrapper, businessEventNotifierService, delinquencyBucketRepository,
                loanRepaymentScheduleTransactionProcessorFactory, advancedPaymentJsonParser, creditAllocationsJsonParser);
    }
}
