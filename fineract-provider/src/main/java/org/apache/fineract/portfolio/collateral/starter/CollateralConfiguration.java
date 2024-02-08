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
package org.apache.fineract.portfolio.collateral.starter;

import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateral.domain.LoanCollateralRepository;
import org.apache.fineract.portfolio.collateral.serialization.CollateralCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.collateral.service.CollateralAssembler;
import org.apache.fineract.portfolio.collateral.service.CollateralReadPlatformService;
import org.apache.fineract.portfolio.collateral.service.CollateralReadPlatformServiceImpl;
import org.apache.fineract.portfolio.collateral.service.CollateralWritePlatformService;
import org.apache.fineract.portfolio.collateral.service.CollateralWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CollateralConfiguration {

    @Bean
    @ConditionalOnMissingBean(CollateralAssembler.class)
    public CollateralAssembler collateralAssembler(FromJsonHelper fromApiJsonHelper, CodeValueRepositoryWrapper codeValueRepository,
            LoanCollateralRepository loanCollateralRepository) {
        return new CollateralAssembler(fromApiJsonHelper, codeValueRepository, loanCollateralRepository);
    }

    @Bean
    @ConditionalOnMissingBean(CollateralReadPlatformService.class)
    public CollateralReadPlatformService collateralReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            LoanRepositoryWrapper loanRepositoryWrapper) {
        return new CollateralReadPlatformServiceImpl(context, jdbcTemplate, loanRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(CollateralWritePlatformService.class)
    public CollateralWritePlatformService collateralWritePlatformService(PlatformSecurityContext context,
            LoanRepositoryWrapper loanRepositoryWrapper, LoanCollateralRepository collateralRepository,
            CodeValueRepositoryWrapper codeValueRepository,
            CollateralCommandFromApiJsonDeserializer collateralCommandFromApiJsonDeserializer) {
        return new CollateralWritePlatformServiceJpaRepositoryImpl(context, loanRepositoryWrapper, collateralRepository,
                codeValueRepository, collateralCommandFromApiJsonDeserializer);
    }

}
