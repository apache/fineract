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
package org.apache.fineract.commands.starter;

import org.apache.fineract.commands.service.AuditReadPlatformService;
import org.apache.fineract.commands.service.AuditReadPlatformServiceImpl;
import org.apache.fineract.infrastructure.core.data.PaginationParametersDataValidator;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.useradministration.service.AppUserReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CommandsConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditReadPlatformService.class)
    public AuditReadPlatformService auditReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            FromJsonHelper fromApiJsonHelper, AppUserReadPlatformService appUserReadPlatformService,
            OfficeReadPlatformService officeReadPlatformService, ClientReadPlatformService clientReadPlatformService,
            LoanProductReadPlatformService loanProductReadPlatformService, StaffReadPlatformService staffReadPlatformService,
            PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator,
            PaginationParametersDataValidator paginationParametersDataValidator,
            SavingsProductReadPlatformService savingsProductReadPlatformService,
            DepositProductReadPlatformService depositProductReadPlatformService, ColumnValidator columnValidator) {
        return new AuditReadPlatformServiceImpl(jdbcTemplate, context, fromApiJsonHelper, appUserReadPlatformService,
                officeReadPlatformService, clientReadPlatformService, loanProductReadPlatformService, staffReadPlatformService,
                paginationHelper, sqlGenerator, paginationParametersDataValidator, savingsProductReadPlatformService,
                depositProductReadPlatformService, columnValidator);
    }

}
