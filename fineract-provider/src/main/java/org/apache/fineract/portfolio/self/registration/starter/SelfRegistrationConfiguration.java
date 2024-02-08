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
package org.apache.fineract.portfolio.self.registration.starter;

import org.apache.fineract.infrastructure.campaigns.sms.service.SmsCampaignDropdownReadPlatformService;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.GmailBackedPlatformEmailService;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.scheduler.SmsMessageScheduledJobService;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.self.registration.domain.SelfServiceRegistrationRepository;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationReadPlatformService;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationReadPlatformServiceImpl;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationWritePlatformService;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationWritePlatformServiceImpl;
import org.apache.fineract.useradministration.domain.PasswordValidationPolicyRepository;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.domain.UserDomainService;
import org.apache.fineract.useradministration.service.AppUserReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SelfRegistrationConfiguration {

    @Bean
    @ConditionalOnMissingBean(SelfServiceRegistrationReadPlatformService.class)
    public SelfServiceRegistrationReadPlatformService selfServiceRegistrationReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new SelfServiceRegistrationReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(SelfServiceRegistrationWritePlatformService.class)
    public SelfServiceRegistrationWritePlatformService selfServiceRegistrationWritePlatformService(

            SelfServiceRegistrationRepository selfServiceRegistrationRepository, FromJsonHelper fromApiJsonHelper,
            SelfServiceRegistrationReadPlatformService selfServiceRegistrationReadPlatformService, ClientRepositoryWrapper clientRepository,
            PasswordValidationPolicyRepository passwordValidationPolicy, UserDomainService userDomainService,
            GmailBackedPlatformEmailService gmailBackedPlatformEmailService, SmsMessageRepository smsMessageRepository,
            SmsMessageScheduledJobService smsMessageScheduledJobService,
            SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService,
            AppUserReadPlatformService appUserReadPlatformService, RoleRepository roleRepository) {
        return new SelfServiceRegistrationWritePlatformServiceImpl(selfServiceRegistrationRepository, fromApiJsonHelper,
                selfServiceRegistrationReadPlatformService, clientRepository, passwordValidationPolicy, userDomainService,
                gmailBackedPlatformEmailService, smsMessageRepository, smsMessageScheduledJobService,
                smsCampaignDropdownReadPlatformService, appUserReadPlatformService, roleRepository);
    }
}
