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
package org.apache.fineract.portfolio.search.starter;

import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.search.service.SearchReadPlatformService;
import org.apache.fineract.portfolio.search.service.SearchReadPlatformServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class SearchConfiguration {

    @Bean
    @ConditionalOnMissingBean(SearchReadPlatformService.class)
    public SearchReadPlatformService searchReadPlatformService(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            PlatformSecurityContext context, LoanProductReadPlatformService loanProductReadPlatformService,
            OfficeReadPlatformService officeReadPlatformService, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new SearchReadPlatformServiceImpl(namedParameterJdbcTemplate, context, loanProductReadPlatformService,
                officeReadPlatformService, sqlGenerator);
    }
}
