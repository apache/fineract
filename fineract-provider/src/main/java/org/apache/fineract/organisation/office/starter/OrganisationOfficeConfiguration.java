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
package org.apache.fineract.organisation.office.starter;

import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.InputValidator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.OfficeTransactionRepository;
import org.apache.fineract.organisation.office.mapper.OfficeDataMapper;
import org.apache.fineract.organisation.office.serialization.OfficeTransactionCommandFromApiJsonDeserializer;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformServiceImpl;
import org.apache.fineract.organisation.office.service.OfficeWritePlatformService;
import org.apache.fineract.organisation.office.service.OfficeWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.organisation.office.service.OfficeWriteService;
import org.apache.fineract.organisation.office.service.OfficeWriteServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OrganisationOfficeConfiguration {

    @Bean
    @ConditionalOnMissingBean(OfficeReadPlatformService.class)
    public OfficeReadPlatformService officeReadPlatformService(JdbcTemplate jdbcTemplate, DatabaseSpecificSQLGenerator sqlGenerator,
            PlatformSecurityContext context, CurrencyReadPlatformService currencyReadPlatformService, InputValidator inputValidator,
            OfficeRepository officeRepository, OfficeDataMapper officeDataMapper) {
        return new OfficeReadPlatformServiceImpl(jdbcTemplate, sqlGenerator, context, currencyReadPlatformService, inputValidator,
                officeRepository, officeDataMapper);
    }

    @Bean
    @ConditionalOnMissingBean(OfficeWritePlatformService.class)
    public OfficeWritePlatformService officeWritePlatformService(PlatformSecurityContext context,
            OfficeTransactionCommandFromApiJsonDeserializer moneyTransferCommandFromApiJsonDeserializer,
            OfficeRepositoryWrapper officeRepositoryWrapper, OfficeTransactionRepository officeTransactionRepository,
            ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository) {
        return new OfficeWritePlatformServiceJpaRepositoryImpl(context, moneyTransferCommandFromApiJsonDeserializer,
                officeRepositoryWrapper, officeTransactionRepository, applicationCurrencyRepository);
    }

    @Bean
    @ConditionalOnMissingBean(OfficeWriteService.class)
    public OfficeWriteService officeWriteService(PlatformSecurityContext context, OfficeRepositoryWrapper officeRepositoryWrapper) {
        return new OfficeWriteServiceImpl(context, officeRepositoryWrapper);
    }
}
