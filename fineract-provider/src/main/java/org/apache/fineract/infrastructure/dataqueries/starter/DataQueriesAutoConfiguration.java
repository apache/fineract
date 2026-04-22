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
package org.apache.fineract.infrastructure.dataqueries.starter;

import org.apache.fineract.infrastructure.codes.service.CodeReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableKeywordGenerator;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableReadService;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableReadServiceImpl;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableUtil;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableWriteService;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableWriteServiceImpl;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class DataQueriesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DatatableReadService datatableReadService(final JdbcTemplate jdbcTemplate, final DatabaseSpecificSQLGenerator sqlGenerator,
            final PlatformSecurityContext context, final GenericDataService genericDataService, final DataTableValidator dataTableValidator,
            final SqlValidator sqlValidator, final SearchUtil searchUtil, final DatatableUtil datatableUtil) {
        return new DatatableReadServiceImpl(jdbcTemplate, sqlGenerator, context, genericDataService, dataTableValidator, sqlValidator,
                searchUtil, datatableUtil);
    }

    @Bean
    @ConditionalOnMissingBean
    public DatatableWriteService datatableWriteService(final JdbcTemplate jdbcTemplate, final DatabaseTypeResolver databaseTypeResolver,
            final DatabaseSpecificSQLGenerator sqlGenerator, final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,
            final GenericDataService genericDataService, final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ConfigurationDomainService configurationDomainService, final CodeReadPlatformService codeReadPlatformService,
            final DataTableValidator dataTableValidator, final NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            final DatatableKeywordGenerator datatableKeywordGenerator, final SearchUtil searchUtil,
            final BusinessEventNotifierService businessEventNotifierService, final DatatableReadService datatableReadService,
            final DatatableUtil datatableUtil) {
        return new DatatableWriteServiceImpl(jdbcTemplate, databaseTypeResolver, sqlGenerator, context, fromJsonHelper, genericDataService,
                fromApiJsonDeserializer, configurationDomainService, codeReadPlatformService, dataTableValidator,
                namedParameterJdbcTemplate, datatableKeywordGenerator, searchUtil, businessEventNotifierService, datatableReadService,
                datatableUtil);
    }

}
