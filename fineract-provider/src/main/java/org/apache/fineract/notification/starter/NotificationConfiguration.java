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
package org.apache.fineract.notification.starter;

import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.notification.eventandlistener.NotificationEventPublisher;
import org.apache.fineract.notification.service.NotificationDomainService;
import org.apache.fineract.notification.service.NotificationDomainServiceImpl;
import org.apache.fineract.notification.service.NotificationGeneratorReadRepositoryWrapper;
import org.apache.fineract.notification.service.NotificationGeneratorWritePlatformService;
import org.apache.fineract.notification.service.NotificationMapperWritePlatformService;
import org.apache.fineract.notification.service.NotificationReadPlatformService;
import org.apache.fineract.notification.service.NotificationReadPlatformServiceImpl;
import org.apache.fineract.notification.service.NotificationWritePlatformService;
import org.apache.fineract.notification.service.NotificationWritePlatformServiceImpl;
import org.apache.fineract.notification.service.UserNotificationService;
import org.apache.fineract.notification.service.UserNotificationServiceImpl;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class NotificationConfiguration {

    @Bean
    @ConditionalOnMissingBean(NotificationDomainService.class)
    public NotificationDomainService notificationDomainService(BusinessEventNotifierService businessEventNotifierService,
            PlatformSecurityContext context, UserNotificationService userNotificationService) {
        return new NotificationDomainServiceImpl(businessEventNotifierService, context, userNotificationService);
    }

    @Bean
    @ConditionalOnMissingBean(NotificationReadPlatformService.class)
    public NotificationReadPlatformService notificationReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            ColumnValidator columnValidator, PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new NotificationReadPlatformServiceImpl(jdbcTemplate, context, columnValidator, paginationHelper, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(NotificationWritePlatformService.class)
    public NotificationWritePlatformService notificationWritePlatformService(
            NotificationGeneratorWritePlatformService notificationGeneratorWritePlatformService,
            NotificationGeneratorReadRepositoryWrapper notificationGeneratorReadRepositoryWrapper, AppUserRepository appUserRepository,
            NotificationMapperWritePlatformService notificationMapperWritePlatformService) {
        return new NotificationWritePlatformServiceImpl(notificationGeneratorWritePlatformService,
                notificationGeneratorReadRepositoryWrapper, appUserRepository, notificationMapperWritePlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(UserNotificationService.class)
    public UserNotificationService userNotificationService(NotificationEventPublisher notificationEventPublisher,
            AppUserRepository appUserRepository, FineractProperties fineractProperties,
            NotificationReadPlatformService notificationReadPlatformService,
            NotificationWritePlatformService notificationWritePlatformService) {
        return new UserNotificationServiceImpl(notificationEventPublisher, appUserRepository, fineractProperties,
                notificationReadPlatformService, notificationWritePlatformService);
    }
}
