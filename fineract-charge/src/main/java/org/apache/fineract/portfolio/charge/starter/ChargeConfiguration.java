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
package org.apache.fineract.portfolio.charge.starter;

import jakarta.validation.Validator;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityVisibilityService;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.service.ChargeDropdownReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeDropdownReadPlatformServiceImpl;
import org.apache.fineract.portfolio.charge.service.ChargeReadService;
import org.apache.fineract.portfolio.charge.service.ChargeReadServiceImpl;
import org.apache.fineract.portfolio.charge.service.ChargeWriteService;
import org.apache.fineract.portfolio.charge.service.ChargeWriteServiceImpl;
import org.apache.fineract.portfolio.charge.service.LoanOfficeIdProvider;
import org.apache.fineract.portfolio.charge.service.LoanProductChargeUsageChecker;
import org.apache.fineract.portfolio.charge.service.SavingsAccountOfficeIdProvider;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.apache.fineract.portfolio.tax.service.TaxReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class ChargeConfiguration {

    @Bean
    @ConditionalOnMissingBean(ChargeDropdownReadPlatformService.class)
    public ChargeDropdownReadPlatformService chargeDropdownReadPlatformService() {
        return new ChargeDropdownReadPlatformServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ChargeReadService.class)
    public ChargeReadService chargeReadService(CurrencyReadPlatformService currencyReadPlatformService,
            ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, JdbcTemplate jdbcTemplate,
            DropdownReadPlatformService dropdownReadPlatformService, FineractEntityVisibilityService fineractEntityVisibilityService,
            AccountingDropdownReadPlatformService accountingDropdownReadPlatformService, TaxReadPlatformService taxReadPlatformService,
            ConfigurationDomainService configurationDomainService, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            LoanOfficeIdProvider loanOfficeIdProvider, SavingsAccountOfficeIdProvider savingsAccountOfficeIdProvider) {
        return new ChargeReadServiceImpl(currencyReadPlatformService, chargeDropdownReadPlatformService, jdbcTemplate,
                dropdownReadPlatformService, fineractEntityVisibilityService, accountingDropdownReadPlatformService, taxReadPlatformService,
                configurationDomainService, namedParameterJdbcTemplate, loanOfficeIdProvider, savingsAccountOfficeIdProvider);
    }

    @Bean
    @ConditionalOnMissingBean(ChargeWriteService.class)
    public ChargeWriteService chargeWriteService(Validator validator, ChargeRepository chargeRepository,
            LoanProductChargeUsageChecker loanProductChargeUsageChecker, FineractEntityVisibilityService fineractEntityVisibilityService,
            GLAccountRepositoryWrapper glAccountRepository, TaxGroupRepositoryWrapper taxGroupRepository,
            PaymentTypeRepository paymentTypeRepository) {
        return new ChargeWriteServiceImpl(validator, chargeRepository, loanProductChargeUsageChecker, fineractEntityVisibilityService,
                glAccountRepository, taxGroupRepository, paymentTypeRepository);
    }
}
