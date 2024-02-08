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
package org.apache.fineract.portfolio.meeting.starter;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.meeting.data.MeetingDataValidator;
import org.apache.fineract.portfolio.meeting.domain.MeetingRepository;
import org.apache.fineract.portfolio.meeting.domain.MeetingRepositoryWrapper;
import org.apache.fineract.portfolio.meeting.service.MeetingReadPlatformService;
import org.apache.fineract.portfolio.meeting.service.MeetingReadPlatformServiceImpl;
import org.apache.fineract.portfolio.meeting.service.MeetingWritePlatformService;
import org.apache.fineract.portfolio.meeting.service.MeetingWritePlatformServiceJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MeetingConfiguration {

    @Bean
    @ConditionalOnMissingBean(MeetingReadPlatformService.class)
    public MeetingReadPlatformService meetingReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new MeetingReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(MeetingWritePlatformService.class)
    public MeetingWritePlatformService meetingWritePlatformService(MeetingRepositoryWrapper meetingRepositoryWrapper,
            MeetingRepository meetingRepository, MeetingDataValidator meetingDataValidator,
            CalendarInstanceRepository calendarInstanceRepository, CalendarRepository calendarRepository,
            ClientRepositoryWrapper clientRepositoryWrapper, GroupRepository groupRepository, FromJsonHelper fromApiJsonHelper,
            ConfigurationDomainService configurationDomainService) {
        return new MeetingWritePlatformServiceJpaRepositoryImpl(meetingRepositoryWrapper, meetingRepository, meetingDataValidator,
                calendarInstanceRepository, calendarRepository, clientRepositoryWrapper, groupRepository, fromApiJsonHelper,
                configurationDomainService);
    }
}
