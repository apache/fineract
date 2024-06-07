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
package org.apache.fineract.portfolio.shareaccounts.start;

import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.accounts.constants.AccountsApiConstants;
import org.apache.fineract.portfolio.accounts.service.AccountsCommandsService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountDividendRepository;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountRepositoryWrapper;
import org.apache.fineract.portfolio.shareaccounts.serialization.ShareAccountDataSerializer;
import org.apache.fineract.portfolio.shareaccounts.service.PurchasedSharesReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.PurchasedSharesReadPlatformServiceImpl;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountChargeReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountChargeReadPlatformServiceImpl;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountCommandsServiceImpl;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountDividendReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountDividendReadPlatformServiceImpl;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountReadPlatformServiceImpl;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountSchedularService;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountSchedularServiceImpl;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountWritePlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductRepositoryWrapper;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductDropdownReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ShareAccountsConfiguration {

    @Bean
    @ConditionalOnMissingBean(PurchasedSharesReadPlatformService.class)
    public PurchasedSharesReadPlatformService purchasedSharesReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new PurchasedSharesReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(ShareAccountChargeReadPlatformService.class)
    public ShareAccountChargeReadPlatformService shareAccountChargeReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new ShareAccountChargeReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean(value = "SHAREACCOUNT_COMMANDSERVICE")
    @ConditionalOnMissingBean(AccountsCommandsService.class)
    public AccountsCommandsService accountsCommandsService(FromJsonHelper fromApiJsonHelper,
            ShareAccountDataSerializer shareAccountDataSerializer) {
        return new ShareAccountCommandsServiceImpl(fromApiJsonHelper, shareAccountDataSerializer);
    }

    @Bean
    @ConditionalOnMissingBean(ShareAccountDividendReadPlatformService.class)
    public ShareAccountDividendReadPlatformService shareAccountDividendReadPlatformService(JdbcTemplate jdbcTemplate,
            ColumnValidator columnValidator, PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new ShareAccountDividendReadPlatformServiceImpl(jdbcTemplate, columnValidator, paginationHelper, sqlGenerator);

    }

    @Bean(value = "share" + AccountsApiConstants.READPLATFORM_NAME)
    @ConditionalOnMissingBean(ShareAccountReadPlatformService.class)
    public ShareAccountReadPlatformService shareAccountReadPlatformService(ApplicationContext applicationContext,
            ChargeReadPlatformService chargeReadPlatformService,
            ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService,
            SavingsAccountReadPlatformService savingsAccountReadPlatformService, ClientReadPlatformService clientReadPlatformService,
            ShareAccountChargeReadPlatformService shareAccountChargeReadPlatformService,
            PurchasedSharesReadPlatformService purchasedSharesReadPlatformService, JdbcTemplate jdbcTemplate,
            PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new ShareAccountReadPlatformServiceImpl(applicationContext, chargeReadPlatformService,
                shareProductDropdownReadPlatformService, savingsAccountReadPlatformService, clientReadPlatformService,
                shareAccountChargeReadPlatformService, purchasedSharesReadPlatformService, jdbcTemplate, paginationHelper, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(ShareAccountSchedularService.class)
    public ShareAccountSchedularService shareAccountSchedularService(ShareAccountDividendRepository shareAccountDividendRepository,
            SavingsAccountDomainService savingsAccountDomainService, SavingsAccountAssembler savingsAccountAssembler) {
        return new ShareAccountSchedularServiceImpl(shareAccountDividendRepository, savingsAccountDomainService, savingsAccountAssembler);
    }

    @Bean
    @ConditionalOnMissingBean(ShareAccountWritePlatformService.class)
    public ShareAccountWritePlatformService shareAccountWritePlatformService(ShareAccountDataSerializer accountDataSerializer,
            ShareAccountRepositoryWrapper shareAccountRepository, ShareProductRepositoryWrapper shareProductRepository,
            AccountNumberGenerator accountNumberGenerator, AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            JournalEntryWritePlatformService journalEntryWritePlatformService, NoteRepository noteRepository,
            BusinessEventNotifierService businessEventNotifierService) {
        return new ShareAccountWritePlatformServiceJpaRepositoryImpl(accountDataSerializer, shareAccountRepository, shareProductRepository,
                accountNumberGenerator, accountNumberFormatRepository, journalEntryWritePlatformService, noteRepository,
                businessEventNotifierService);
    }
}
