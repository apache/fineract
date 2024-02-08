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
package org.apache.fineract.spm.starter;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.spm.domain.LookupTableRepository;
import org.apache.fineract.spm.domain.ScorecardRepository;
import org.apache.fineract.spm.domain.SurveyRepository;
import org.apache.fineract.spm.domain.SurveyValidator;
import org.apache.fineract.spm.service.LookupTableService;
import org.apache.fineract.spm.service.ScorecardReadPlatformService;
import org.apache.fineract.spm.service.ScorecardReadPlatformServiceImpl;
import org.apache.fineract.spm.service.ScorecardService;
import org.apache.fineract.spm.service.SpmService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SpmConfiguration {

    @Bean
    @ConditionalOnMissingBean(LookupTableService.class)
    public LookupTableService lookupTableService(PlatformSecurityContext securityContext, LookupTableRepository lookupTableRepository) {
        return new LookupTableService(securityContext, lookupTableRepository);
    }

    @Bean
    @ConditionalOnMissingBean(ScorecardReadPlatformService.class)
    public ScorecardReadPlatformService scorecardReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context) {
        return new ScorecardReadPlatformServiceImpl(jdbcTemplate, context);
    }

    @Bean
    @ConditionalOnMissingBean(ScorecardService.class)
    public ScorecardService scorecardService(PlatformSecurityContext securityContext, ScorecardRepository scorecardRepository) {

        return new ScorecardService(securityContext, scorecardRepository);
    }

    @Bean
    @ConditionalOnMissingBean(SpmService.class)
    public SpmService spmService(PlatformSecurityContext securityContext, SurveyRepository surveyRepository,
            SurveyValidator surveyValidator) {
        return new SpmService(securityContext, surveyRepository, surveyValidator);
    }
}
