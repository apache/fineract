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
package org.apache.fineract.organisation.staff.starter;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.mapper.StaffCreateRequestMapper;
import org.apache.fineract.organisation.staff.service.StaffReadService;
import org.apache.fineract.organisation.staff.service.StaffReadServiceImpl;
import org.apache.fineract.organisation.staff.service.StaffWriteService;
import org.apache.fineract.organisation.staff.service.StaffWriteServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
class StaffConfiguration {

    @Bean
    @ConditionalOnMissingBean(StaffReadService.class)
    StaffReadService staffReadService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate) {
        return new StaffReadServiceImpl(context, jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(StaffWriteService.class)
    StaffWriteService staffWriteService(StaffRepository staffRepository, OfficeRepository officeRepository,
            StaffCreateRequestMapper staffCreateRequestMapper) {
        return new StaffWriteServiceImpl(staffRepository, officeRepository, staffCreateRequestMapper);
    }
}
