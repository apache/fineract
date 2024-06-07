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

package org.apache.fineract.portfolio.group.starter;

import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.PaginationParametersDataValidator;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.group.domain.GroupLevelRepository;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRoleRepositoryWrapper;
import org.apache.fineract.portfolio.group.serialization.GroupRolesDataValidator;
import org.apache.fineract.portfolio.group.serialization.GroupingTypesDataValidator;
import org.apache.fineract.portfolio.group.service.CenterReadPlatformService;
import org.apache.fineract.portfolio.group.service.CenterReadPlatformServiceImpl;
import org.apache.fineract.portfolio.group.service.GroupLevelReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupLevelReadPlatformServiceImpl;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformServiceImpl;
import org.apache.fineract.portfolio.group.service.GroupRolesReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupRolesReadPlatformServiceImpl;
import org.apache.fineract.portfolio.group.service.GroupRolesWritePlatformService;
import org.apache.fineract.portfolio.group.service.GroupRolesWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.group.service.GroupingTypesWritePlatformService;
import org.apache.fineract.portfolio.group.service.GroupingTypesWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class GroupConfiguration {

    @Bean
    @ConditionalOnMissingBean(CenterReadPlatformService.class)
    public CenterReadPlatformService centerReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            ClientReadPlatformService clientReadPlatformService, OfficeReadPlatformService officeReadPlatformService,
            StaffReadPlatformService staffReadPlatformService, CodeValueReadPlatformService codeValueReadPlatformService,
            ConfigurationDomainService configurationDomainService, ColumnValidator columnValidator, PaginationHelper paginationHelper,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationParametersDataValidator paginationParametersDataValidator) {
        return new CenterReadPlatformServiceImpl(jdbcTemplate, context, clientReadPlatformService, officeReadPlatformService,
                staffReadPlatformService, codeValueReadPlatformService, configurationDomainService, columnValidator, paginationHelper,
                sqlGenerator, paginationParametersDataValidator);
    }

    @Bean
    @ConditionalOnMissingBean(GroupingTypesWritePlatformService.class)
    public GroupingTypesWritePlatformService groupingTypesWritePlatformService(PlatformSecurityContext context,
            GroupRepositoryWrapper groupRepository, ClientRepositoryWrapper clientRepositoryWrapper,
            OfficeRepositoryWrapper officeRepositoryWrapper, StaffRepositoryWrapper staffRepository, NoteRepository noteRepository,
            GroupLevelRepository groupLevelRepository, GroupingTypesDataValidator fromApiJsonDeserializer,
            LoanRepositoryWrapper loanRepositoryWrapper, CodeValueRepositoryWrapper codeValueRepository,
            CommandProcessingService commandProcessingService, CalendarInstanceRepository calendarInstanceRepository,
            ConfigurationDomainService configurationDomainService, SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper,
            AccountNumberFormatRepositoryWrapper accountNumberFormatRepository, AccountNumberGenerator accountNumberGenerator,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService,
            BusinessEventNotifierService businessEventNotifierService

    ) {
        return new GroupingTypesWritePlatformServiceJpaRepositoryImpl(context, groupRepository, clientRepositoryWrapper,
                officeRepositoryWrapper, staffRepository, noteRepository, groupLevelRepository, fromApiJsonDeserializer,
                loanRepositoryWrapper, codeValueRepository, commandProcessingService, calendarInstanceRepository,
                configurationDomainService, savingsAccountRepositoryWrapper, accountNumberFormatRepository, accountNumberGenerator,
                entityDatatableChecksWritePlatformService, businessEventNotifierService

        );
    }

    @Bean
    @ConditionalOnMissingBean(GroupLevelReadPlatformService.class)
    public GroupLevelReadPlatformService groupLevelReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate) {
        return new GroupLevelReadPlatformServiceImpl(context, jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(GroupReadPlatformService.class)
    public GroupReadPlatformService groupReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            OfficeReadPlatformService officeReadPlatformService, StaffReadPlatformService staffReadPlatformService,
            CenterReadPlatformService centerReadPlatformService, CodeValueReadPlatformService codeValueReadPlatformService,
            PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator,
            PaginationParametersDataValidator paginationParametersDataValidator, ColumnValidator columnValidator) {
        return new GroupReadPlatformServiceImpl(jdbcTemplate, context, officeReadPlatformService, staffReadPlatformService,
                centerReadPlatformService, codeValueReadPlatformService, paginationHelper, sqlGenerator, paginationParametersDataValidator,
                columnValidator);
    }

    @Bean
    @ConditionalOnMissingBean(GroupRolesReadPlatformService.class)
    public GroupRolesReadPlatformService groupRolesReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context) {
        return new GroupRolesReadPlatformServiceImpl(jdbcTemplate, context);
    }

    @Bean
    @ConditionalOnMissingBean(GroupRolesWritePlatformService.class)
    public GroupRolesWritePlatformService groupRolesWritePlatformService(PlatformSecurityContext context,
            GroupRepositoryWrapper groupRepository, GroupRolesDataValidator fromApiJsonDeserializer,
            CodeValueRepositoryWrapper codeValueRepository, ClientRepositoryWrapper clientRepository,
            GroupRoleRepositoryWrapper groupRoleRepository) {
        return new GroupRolesWritePlatformServiceJpaRepositoryImpl(context, groupRepository, fromApiJsonDeserializer, codeValueRepository,
                clientRepository, groupRoleRepository);
    }
}
