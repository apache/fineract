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
package org.apache.fineract.useradministration.starter;

import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.useradministration.data.PasswordPreferencesDataValidator;
import org.apache.fineract.useradministration.domain.AppUserPreviousPasswordRepository;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.PasswordValidationPolicyRepository;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.domain.UserDomainService;
import org.apache.fineract.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
import org.apache.fineract.useradministration.service.AppUserReadPlatformService;
import org.apache.fineract.useradministration.service.AppUserReadPlatformServiceImpl;
import org.apache.fineract.useradministration.service.AppUserWritePlatformService;
import org.apache.fineract.useradministration.service.AppUserWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.useradministration.service.PasswordPreferencesWritePlatformService;
import org.apache.fineract.useradministration.service.PasswordPreferencesWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.useradministration.service.PasswordValidationPolicyReadPlatformService;
import org.apache.fineract.useradministration.service.PasswordValidationPolicyReadPlatformServiceImpl;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.apache.fineract.useradministration.service.PermissionReadPlatformServiceImpl;
import org.apache.fineract.useradministration.service.PermissionWritePlatformService;
import org.apache.fineract.useradministration.service.PermissionWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.useradministration.service.RoleDataValidator;
import org.apache.fineract.useradministration.service.RoleReadPlatformService;
import org.apache.fineract.useradministration.service.RoleReadPlatformServiceImpl;
import org.apache.fineract.useradministration.service.RoleWritePlatformService;
import org.apache.fineract.useradministration.service.RoleWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.useradministration.service.UserDataValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class UserAdministrationConfiguration {

    @Bean
    @ConditionalOnMissingBean(AppUserReadPlatformService.class)
    public AppUserReadPlatformService appUserReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            OfficeReadPlatformService officeReadPlatformService, RoleReadPlatformService roleReadPlatformService,
            AppUserRepository appUserRepository, StaffReadPlatformService staffReadPlatformService) {
        return new AppUserReadPlatformServiceImpl(context, jdbcTemplate, officeReadPlatformService, roleReadPlatformService,
                appUserRepository, staffReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(AppUserWritePlatformService.class)
    public AppUserWritePlatformService appUserWritePlatformService(PlatformSecurityContext context, UserDomainService userDomainService,
            PlatformPasswordEncoder platformPasswordEncoder, AppUserRepository appUserRepository,
            OfficeRepositoryWrapper officeRepositoryWrapper, RoleRepository roleRepository, UserDataValidator fromApiJsonDeserializer,
            AppUserPreviousPasswordRepository appUserPreviewPasswordRepository, StaffRepositoryWrapper staffRepositoryWrapper,
            ClientRepositoryWrapper clientRepositoryWrapper) {
        return new AppUserWritePlatformServiceJpaRepositoryImpl(context, userDomainService, platformPasswordEncoder, appUserRepository,
                officeRepositoryWrapper, roleRepository, fromApiJsonDeserializer, appUserPreviewPasswordRepository, staffRepositoryWrapper,
                clientRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(PasswordPreferencesWritePlatformService.class)
    public PasswordPreferencesWritePlatformService passwordPreferencesWritePlatformService(
            PasswordValidationPolicyRepository validationPolicyRepository, PasswordPreferencesDataValidator dataValidator) {
        return new PasswordPreferencesWritePlatformServiceJpaRepositoryImpl(validationPolicyRepository, dataValidator);
    }

    @Bean
    @ConditionalOnMissingBean(PasswordValidationPolicyReadPlatformService.class)
    public PasswordValidationPolicyReadPlatformService passwordValidationPolicyReadPlatformService(JdbcTemplate jdbcTemplate,
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new PasswordValidationPolicyReadPlatformServiceImpl(jdbcTemplate, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(PermissionReadPlatformService.class)
    public PermissionReadPlatformService permissionReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new PermissionReadPlatformServiceImpl(context, jdbcTemplate, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(PermissionWritePlatformService.class)
    public PermissionWritePlatformService permissionWritePlatformService(PlatformSecurityContext context,
            PermissionRepository permissionRepository, PermissionsCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        return new PermissionWritePlatformServiceJpaRepositoryImpl(context, permissionRepository, fromApiJsonDeserializer);
    }

    @Bean
    @ConditionalOnMissingBean(RoleReadPlatformService.class)
    public RoleReadPlatformService roleReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new RoleReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(RoleWritePlatformService.class)
    public RoleWritePlatformService roleWritePlatformService(PlatformSecurityContext context, RoleRepository roleRepository,
            PermissionRepository permissionRepository, RoleDataValidator roleCommandFromApiJsonDeserializer,
            PermissionsCommandFromApiJsonDeserializer permissionsFromApiJsonDeserializer) {
        return new RoleWritePlatformServiceJpaRepositoryImpl(context, roleRepository, permissionRepository,
                roleCommandFromApiJsonDeserializer, permissionsFromApiJsonDeserializer);
    }
}
