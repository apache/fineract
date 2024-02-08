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
package org.apache.fineract.organisation.teller.starter;

import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.organisation.teller.data.CashierTransactionDataValidator;
import org.apache.fineract.organisation.teller.domain.CashierRepository;
import org.apache.fineract.organisation.teller.domain.CashierTransactionRepository;
import org.apache.fineract.organisation.teller.domain.TellerRepositoryWrapper;
import org.apache.fineract.organisation.teller.serialization.TellerCommandFromApiJsonDeserializer;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformServiceImpl;
import org.apache.fineract.organisation.teller.service.TellerWritePlatformService;
import org.apache.fineract.organisation.teller.service.TellerWritePlatformServiceJpaImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OrganisationTellerConfiguration {

    @Bean
    @ConditionalOnMissingBean(TellerManagementReadPlatformService.class)
    public TellerManagementReadPlatformService tellerManagementReadPlatformService(JdbcTemplate jdbcTemplate,
            PlatformSecurityContext context, OfficeReadPlatformService officeReadPlatformService,
            StaffReadPlatformService staffReadPlatformService, CurrencyReadPlatformService currencyReadPlatformService,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper, ColumnValidator columnValidator) {
        return new TellerManagementReadPlatformServiceImpl(jdbcTemplate, context, officeReadPlatformService, staffReadPlatformService,
                currencyReadPlatformService, sqlGenerator, paginationHelper, columnValidator);
    }

    @Bean
    @ConditionalOnMissingBean(TellerWritePlatformService.class)
    public TellerWritePlatformService tellerWritePlatformService(PlatformSecurityContext context,
            TellerCommandFromApiJsonDeserializer fromApiJsonDeserializer, TellerRepositoryWrapper tellerRepositoryWrapper,
            OfficeRepositoryWrapper officeRepositoryWrapper, StaffRepository staffRepository, CashierRepository cashierRepository,
            CashierTransactionRepository cashierTxnRepository, JournalEntryRepository glJournalEntryRepository,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper,
            CashierTransactionDataValidator cashierTransactionDataValidator) {
        return new TellerWritePlatformServiceJpaImpl(context, fromApiJsonDeserializer, tellerRepositoryWrapper, officeRepositoryWrapper,
                staffRepository, cashierRepository, cashierTxnRepository, glJournalEntryRepository,
                financialActivityAccountRepositoryWrapper, cashierTransactionDataValidator);
    }
}
