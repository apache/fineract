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
package org.apache.fineract.interoperation.starter;

import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.interoperation.domain.InteropIdentifierRepository;
import org.apache.fineract.interoperation.serialization.InteropDataValidator;
import org.apache.fineract.interoperation.service.InteropService;
import org.apache.fineract.interoperation.service.InteropServiceImpl;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class InteroperationConfiguration {

    @Bean
    @ConditionalOnMissingBean(InteropService.class)
    public InteropService interopService(PlatformSecurityContext securityContext, InteropDataValidator interopDataValidator,
            SavingsAccountRepository savingsAccountRepository, SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            ApplicationCurrencyRepository applicationCurrencyRepository, NoteRepository noteRepository,
            PaymentTypeRepository paymentTypeRepository, InteropIdentifierRepository identifierRepository, LoanRepository loanRepository,
            SavingsHelper savingsHelper, SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            SavingsAccountDomainService savingsAccountService, JdbcTemplate jdbcTemplate,
            PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new InteropServiceImpl(securityContext, interopDataValidator, savingsAccountRepository, savingsAccountTransactionRepository,
                applicationCurrencyRepository, noteRepository, paymentTypeRepository, identifierRepository, loanRepository, savingsHelper,
                savingsAccountTransactionSummaryWrapper, savingsAccountService, jdbcTemplate, commandsSourceWritePlatformService,
                toApiJsonSerializer, sqlGenerator);
    }
}
