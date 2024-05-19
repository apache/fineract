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
package org.apache.fineract.portfolio.tax.starter;

import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepository;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.apache.fineract.portfolio.tax.serialization.TaxValidator;
import org.apache.fineract.portfolio.tax.service.TaxAssembler;
import org.apache.fineract.portfolio.tax.service.TaxReadPlatformService;
import org.apache.fineract.portfolio.tax.service.TaxReadPlatformServiceImpl;
import org.apache.fineract.portfolio.tax.service.TaxWritePlatformService;
import org.apache.fineract.portfolio.tax.service.TaxWritePlatformServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class TaxConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaxAssembler.class)
    public TaxAssembler taxAssembler(FromJsonHelper fromApiJsonHelper, GLAccountRepositoryWrapper glAccountRepositoryWrapper,
            TaxComponentRepositoryWrapper taxComponentRepositoryWrapper) {
        return new TaxAssembler(fromApiJsonHelper, glAccountRepositoryWrapper, taxComponentRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(TaxReadPlatformService.class)
    public TaxReadPlatformService taxReadPlatformService(JdbcTemplate jdbcTemplate,
            AccountingDropdownReadPlatformService accountingDropdownReadPlatformService) {
        return new TaxReadPlatformServiceImpl(jdbcTemplate, accountingDropdownReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(TaxWritePlatformService.class)
    public TaxWritePlatformService taxWritePlatformService(TaxValidator validator, TaxAssembler taxAssembler,
            TaxComponentRepository taxComponentRepository, TaxGroupRepository taxGroupRepository,
            TaxComponentRepositoryWrapper taxComponentRepositoryWrapper, TaxGroupRepositoryWrapper taxGroupRepositoryWrapper) {
        return new TaxWritePlatformServiceImpl(validator, taxAssembler, taxComponentRepository, taxComponentRepositoryWrapper,
                taxGroupRepository, taxGroupRepositoryWrapper);
    }
}
