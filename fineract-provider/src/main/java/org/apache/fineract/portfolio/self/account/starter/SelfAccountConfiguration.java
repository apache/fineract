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
package org.apache.fineract.portfolio.self.account.starter;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.self.account.data.SelfBeneficiariesTPTDataValidator;
import org.apache.fineract.portfolio.self.account.domain.SelfBeneficiariesTPTRepository;
import org.apache.fineract.portfolio.self.account.service.SelfAccountTransferReadService;
import org.apache.fineract.portfolio.self.account.service.SelfAccountTransferReadServiceImpl;
import org.apache.fineract.portfolio.self.account.service.SelfBeneficiariesTPTReadPlatformService;
import org.apache.fineract.portfolio.self.account.service.SelfBeneficiariesTPTReadPlatformServiceImpl;
import org.apache.fineract.portfolio.self.account.service.SelfBeneficiariesTPTWritePlatformService;
import org.apache.fineract.portfolio.self.account.service.SelfBeneficiariesTPTWritePlatformServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SelfAccountConfiguration {

    @Bean
    @ConditionalOnMissingBean(SelfAccountTransferReadService.class)
    public SelfAccountTransferReadService selfAccountTransferReadService(JdbcTemplate jdbcTemplate) {
        return new SelfAccountTransferReadServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(SelfBeneficiariesTPTReadPlatformService.class)
    public SelfBeneficiariesTPTReadPlatformService selfBeneficiariesTPTReadPlatformService(PlatformSecurityContext context,
            JdbcTemplate jdbcTemplate) {
        return new SelfBeneficiariesTPTReadPlatformServiceImpl(context, jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(SelfBeneficiariesTPTWritePlatformService.class)
    public SelfBeneficiariesTPTWritePlatformService selfBeneficiariesTPTWritePlatformService(PlatformSecurityContext context,
            SelfBeneficiariesTPTRepository repository, SelfBeneficiariesTPTDataValidator validator,
            LoanRepositoryWrapper loanRepositoryWrapper, SavingsAccountRepositoryWrapper savingRepositoryWrapper) {
        return new SelfBeneficiariesTPTWritePlatformServiceImpl(context, repository, validator, loanRepositoryWrapper,
                savingRepositoryWrapper);
    }
}
