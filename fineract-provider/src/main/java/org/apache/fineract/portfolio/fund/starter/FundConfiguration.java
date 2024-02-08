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
package org.apache.fineract.portfolio.fund.starter;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.serialization.FundCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformServiceImpl;
import org.apache.fineract.portfolio.fund.service.FundWritePlatformService;
import org.apache.fineract.portfolio.fund.service.FundWritePlatformServiceJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class FundConfiguration {

    @Bean
    @ConditionalOnMissingBean(FundReadPlatformService.class)
    public FundReadPlatformService fundReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context) {
        return new FundReadPlatformServiceImpl(jdbcTemplate, context);
    }

    @Bean
    @ConditionalOnMissingBean(FundWritePlatformService.class)
    public FundWritePlatformService fundWritePlatformService(PlatformSecurityContext context,
            FundCommandFromApiJsonDeserializer fromApiJsonDeserializer, FundRepository fundRepository) {
        return new FundWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer, fundRepository);
    }
}
