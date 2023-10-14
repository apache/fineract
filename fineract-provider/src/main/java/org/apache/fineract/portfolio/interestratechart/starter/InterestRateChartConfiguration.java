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
package org.apache.fineract.portfolio.interestratechart.starter;

import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartDataValidator;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabDataValidator;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartRepositoryWrapper;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlabRepository;
import org.apache.fineract.portfolio.interestratechart.service.InterestIncentiveAssembler;
import org.apache.fineract.portfolio.interestratechart.service.InterestIncentiveDropdownReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.service.InterestIncentivesDropdownReadPlatformServiceImpl;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartAssembler;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartDropdownReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartDropdownReadPlatformServiceImpl;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadPlatformServiceImpl;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartSlabAssembler;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartSlabReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartSlabReadPlatformServiceImpl;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartSlabWritePlatformService;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartSlabWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartWritePlatformService;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class InterestRateChartConfiguration {

    @Bean
    @ConditionalOnMissingBean(InterestIncentiveAssembler.class)
    public InterestIncentiveAssembler interestIncentiveAssembler(FromJsonHelper fromApiJsonHelper) {
        return new InterestIncentiveAssembler(fromApiJsonHelper);
    }

    @Bean
    @ConditionalOnMissingBean(InterestIncentiveDropdownReadPlatformService.class)
    public InterestIncentiveDropdownReadPlatformService interestIncentiveDropdownReadPlatformService(

    ) {
        return new InterestIncentivesDropdownReadPlatformServiceImpl(

        );
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartAssembler.class)
    public InterestRateChartAssembler interestRateChartAssembler(FromJsonHelper fromApiJsonHelper,
            InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper, InterestRateChartSlabAssembler chartSlabAssembler) {
        return new InterestRateChartAssembler(fromApiJsonHelper, interestRateChartRepositoryWrapper, chartSlabAssembler);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartDropdownReadPlatformService.class)
    public InterestRateChartDropdownReadPlatformService interestRateChartDropdownReadPlatformService(

    ) {
        return new InterestRateChartDropdownReadPlatformServiceImpl(

        );
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartReadPlatformService.class)
    public InterestRateChartReadPlatformService interestRateChartReadPlatformService(PlatformSecurityContext context,
            JdbcTemplate jdbcTemplate, InterestRateChartDropdownReadPlatformService chartDropdownReadPlatformService,
            InterestIncentiveDropdownReadPlatformService interestIncentiveDropdownReadPlatformService,
            CodeValueReadPlatformService codeValueReadPlatformService, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new InterestRateChartReadPlatformServiceImpl(context, jdbcTemplate, chartDropdownReadPlatformService,
                interestIncentiveDropdownReadPlatformService, codeValueReadPlatformService, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartSlabAssembler.class)
    public InterestRateChartSlabAssembler interestRateChartSlabAssembler(FromJsonHelper fromApiJsonHelper,
            InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper, InterestIncentiveAssembler incentiveAssembler) {
        return new InterestRateChartSlabAssembler(fromApiJsonHelper, interestRateChartRepositoryWrapper, incentiveAssembler);
    }

    @Bean
    public InterestRateChartSlabReadPlatformServiceImpl.InterestRateChartSlabExtractor interestRateChartSlabExtractor(
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new InterestRateChartSlabReadPlatformServiceImpl.InterestRateChartSlabExtractor(sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartSlabReadPlatformService.class)
    public InterestRateChartSlabReadPlatformService interestRateChartSlabReadPlatformService(

            PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            InterestRateChartSlabReadPlatformServiceImpl.InterestRateChartSlabExtractor chartSlabExtractor,
            InterestRateChartDropdownReadPlatformService chartDropdownReadPlatformService,
            InterestIncentiveDropdownReadPlatformService interestIncentiveDropdownReadPlatformService,
            CodeValueReadPlatformService codeValueReadPlatformService) {
        return new InterestRateChartSlabReadPlatformServiceImpl(context, jdbcTemplate, chartSlabExtractor, chartDropdownReadPlatformService,
                interestIncentiveDropdownReadPlatformService, codeValueReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartSlabWritePlatformService.class)
    public InterestRateChartSlabWritePlatformService interestRateChartSlabWritePlatformService(

            PlatformSecurityContext context, InterestRateChartSlabDataValidator interestRateChartSlabDataValidator,
            InterestRateChartAssembler interestRateChartAssembler, InterestRateChartSlabAssembler interestRateChartSlabAssembler,
            InterestRateChartRepositoryWrapper interestRateChartRepository, InterestRateChartSlabRepository chartSlabRepository,
            SavingsProductRepository savingsProductRepository) {
        return new InterestRateChartSlabWritePlatformServiceJpaRepositoryImpl(context, interestRateChartSlabDataValidator,
                interestRateChartAssembler, interestRateChartSlabAssembler, interestRateChartRepository, chartSlabRepository,
                savingsProductRepository);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartWritePlatformService.class)
    public InterestRateChartWritePlatformService interestRateChartWritePlatformService(PlatformSecurityContext context,
            InterestRateChartDataValidator interestRateChartDataValidator, InterestRateChartAssembler interestRateChartAssembler,
            InterestRateChartRepositoryWrapper interestRateChartRepository) {
        return new InterestRateChartWritePlatformServiceJpaRepositoryImpl(context, interestRateChartDataValidator,
                interestRateChartAssembler, interestRateChartRepository);
    }

}
