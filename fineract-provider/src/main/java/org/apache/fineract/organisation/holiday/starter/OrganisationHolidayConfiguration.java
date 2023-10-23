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
package org.apache.fineract.organisation.holiday.starter;

import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.data.HolidayDataValidator;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.holiday.service.HolidayReadPlatformService;
import org.apache.fineract.organisation.holiday.service.HolidayReadPlatformServiceImpl;
import org.apache.fineract.organisation.holiday.service.HolidayWritePlatformService;
import org.apache.fineract.organisation.holiday.service.HolidayWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OrganisationHolidayConfiguration {

    @Bean
    @ConditionalOnMissingBean(HolidayReadPlatformService.class)
    public HolidayReadPlatformService holidayReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate) {
        return new HolidayReadPlatformServiceImpl(context, jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(HolidayWritePlatformService.class)
    public HolidayWritePlatformService holidayWritePlatformService(HolidayDataValidator fromApiJsonDeserializer,
            HolidayRepositoryWrapper holidayRepository, PlatformSecurityContext context, OfficeRepositoryWrapper officeRepositoryWrapper,
            FromJsonHelper fromApiJsonHelper, WorkingDaysRepositoryWrapper daysRepositoryWrapper) {
        return new HolidayWritePlatformServiceJpaRepositoryImpl(fromApiJsonDeserializer, holidayRepository, daysRepositoryWrapper, context,
                officeRepositoryWrapper, fromApiJsonHelper);
    }
}
