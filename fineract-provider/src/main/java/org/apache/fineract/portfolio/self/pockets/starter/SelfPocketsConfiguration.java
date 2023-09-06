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
package org.apache.fineract.portfolio.self.pockets.starter;

import java.util.Set;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.self.loanaccount.service.AppuserLoansMapperReadService;
import org.apache.fineract.portfolio.self.pockets.data.PocketDataValidator;
import org.apache.fineract.portfolio.self.pockets.domain.PocketAccountMappingRepositoryWrapper;
import org.apache.fineract.portfolio.self.pockets.domain.PocketRepositoryWrapper;
import org.apache.fineract.portfolio.self.pockets.service.AccountEntityService;
import org.apache.fineract.portfolio.self.pockets.service.AccountEntityServiceFactory;
import org.apache.fineract.portfolio.self.pockets.service.AccountEntityServiceForLoanImpl;
import org.apache.fineract.portfolio.self.pockets.service.AccountEntityServiceForSavingsImpl;
import org.apache.fineract.portfolio.self.pockets.service.AccountEntityServiceForShareAccountsImpl;
import org.apache.fineract.portfolio.self.pockets.service.PocketAccountMappingReadPlatformService;
import org.apache.fineract.portfolio.self.pockets.service.PocketAccountMappingReadPlatformServiceImpl;
import org.apache.fineract.portfolio.self.pockets.service.PocketWritePlatformService;
import org.apache.fineract.portfolio.self.pockets.service.PocketWritePlatformServiceImpl;
import org.apache.fineract.portfolio.self.savings.service.AppuserSavingsMapperReadService;
import org.apache.fineract.portfolio.self.shareaccounts.service.AppUserShareAccountsMapperReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SelfPocketsConfiguration {

    @Bean
    @Scope("singleton")
    @ConditionalOnMissingBean(AccountEntityServiceFactory.class)
    public AccountEntityServiceFactory accountEntityServiceFactory(Set<AccountEntityService> accountEntityServices) {
        return new AccountEntityServiceFactory(accountEntityServices);
    }

    @Bean
    @ConditionalOnMissingBean(AccountEntityServiceForLoanImpl.class)
    public AccountEntityService accountEntityServiceForLoanImpl(PlatformSecurityContext context,
            AppuserLoansMapperReadService appuserLoansMapperReadService, LoanReadPlatformService loanReadPlatformService) {

        return new AccountEntityServiceForLoanImpl(context, appuserLoansMapperReadService, loanReadPlatformService);

    }

    @Bean
    @ConditionalOnMissingBean(AccountEntityServiceForSavingsImpl.class)
    public AccountEntityService accountEntityServiceForSavingsImpl(PlatformSecurityContext context,
            AppuserSavingsMapperReadService appuserSavingsMapperReadService,
            SavingsAccountReadPlatformService savingsAccountReadPlatformService) {

        return new AccountEntityServiceForSavingsImpl(context, appuserSavingsMapperReadService, savingsAccountReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(AccountEntityServiceForShareAccountsImpl.class)
    public AccountEntityService accountEntityServiceForShareAccountsImpl(PlatformSecurityContext context,
            AppUserShareAccountsMapperReadPlatformService appUserShareAccountsMapperReadPlatformService,
            ShareAccountReadPlatformService shareAccountReadPlatformService) {
        return new AccountEntityServiceForShareAccountsImpl(context, appUserShareAccountsMapperReadPlatformService,
                shareAccountReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(PocketAccountMappingReadPlatformService.class)
    public PocketAccountMappingReadPlatformService pocketAccountMappingReadPlatformService(JdbcTemplate jdbcTemplate,
            PlatformSecurityContext context, PocketRepositoryWrapper pocketRepositoryWrapper,
            PocketAccountMappingRepositoryWrapper pocketAccountMappingRepositoryWrapper) {
        return new PocketAccountMappingReadPlatformServiceImpl(jdbcTemplate, context, pocketRepositoryWrapper,
                pocketAccountMappingRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(PocketWritePlatformService.class)
    public PocketWritePlatformService pocketWritePlatformService(PlatformSecurityContext context, PocketDataValidator pocketDataValidator,
            AccountEntityServiceFactory accountEntityServiceFactory, PocketRepositoryWrapper pocketRepositoryWrapper,
            PocketAccountMappingRepositoryWrapper pocketAccountMappingRepositoryWrapper,
            PocketAccountMappingReadPlatformService pocketAccountMappingReadPlatformService) {
        return new PocketWritePlatformServiceImpl(context, pocketDataValidator, accountEntityServiceFactory, pocketRepositoryWrapper,
                pocketAccountMappingRepositoryWrapper, pocketAccountMappingReadPlatformService);
    }
}
