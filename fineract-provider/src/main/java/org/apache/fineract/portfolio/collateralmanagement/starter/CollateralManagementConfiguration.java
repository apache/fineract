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
package org.apache.fineract.portfolio.collateralmanagement.starter;

import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.service.ClientCollateralManagementReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.ClientCollateralManagementReadPlatformServiceImpl;
import org.apache.fineract.portfolio.collateralmanagement.service.ClientCollateralManagementWritePlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.ClientCollateralManagementWritePlatformServiceImpl;
import org.apache.fineract.portfolio.collateralmanagement.service.CollateralManagementReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.CollateralManagementReadPlatformServiceImpl;
import org.apache.fineract.portfolio.collateralmanagement.service.CollateralManagementWritePlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.CollateralManagementWritePlatformServiceImpl;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformServiceImpl;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementWritePlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementWritePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagementRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollateralManagementConfiguration {

    @Bean
    @ConditionalOnMissingBean(ClientCollateralManagementReadPlatformService.class)
    public ClientCollateralManagementReadPlatformService clientCollateralManagementReadPlatformService(PlatformSecurityContext context,
            ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper,
            LoanTransactionRepository loanTransactionRepository) {
        return new ClientCollateralManagementReadPlatformServiceImpl(context, clientCollateralManagementRepositoryWrapper,
                loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(ClientCollateralManagementWritePlatformService.class)
    public ClientCollateralManagementWritePlatformService clientCollateralManagementWritePlatformService(
            ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper,
            CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper, ClientRepositoryWrapper clientRepositoryWrapper) {
        return new ClientCollateralManagementWritePlatformServiceImpl(clientCollateralManagementRepositoryWrapper,
                collateralManagementRepositoryWrapper, clientRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(CollateralManagementReadPlatformService.class)
    public CollateralManagementReadPlatformService collateralManagementReadPlatformService(PlatformSecurityContext context,
            CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper) {
        return new CollateralManagementReadPlatformServiceImpl(context, collateralManagementRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(CollateralManagementWritePlatformService.class)
    public CollateralManagementWritePlatformService collateralManagementWritePlatformService(
            CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper,
            ApplicationCurrencyRepository applicationCurrencyRepository, FromJsonHelper fromApiJsonHelper) {
        return new CollateralManagementWritePlatformServiceImpl(collateralManagementRepositoryWrapper, applicationCurrencyRepository,
                fromApiJsonHelper);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCollateralAssembler.class)
    public LoanCollateralAssembler loanCollateralAssembler(FromJsonHelper fromApiJsonHelper, CodeValueRepositoryWrapper codeValueRepository,
            LoanCollateralManagementRepository loanCollateralRepository,
            ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper) {
        return new LoanCollateralAssembler(fromApiJsonHelper, codeValueRepository, loanCollateralRepository,
                clientCollateralManagementRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCollateralManagementReadPlatformService.class)
    public LoanCollateralManagementReadPlatformService loanCollateralManagementReadPlatformService(PlatformSecurityContext context,
            LoanCollateralManagementRepository loanCollateralManagementRepository, LoanRepository loanRepository) {
        return new LoanCollateralManagementReadPlatformServiceImpl(context, loanCollateralManagementRepository, loanRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCollateralManagementWritePlatformService.class)
    public LoanCollateralManagementWritePlatformService loanCollateralManagementWritePlatformService(
            LoanCollateralManagementRepository loanCollateralManagementRepository,
            ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper) {
        return new LoanCollateralManagementWritePlatformServiceImpl(loanCollateralManagementRepository,
                clientCollateralManagementRepositoryWrapper);
    }
}
