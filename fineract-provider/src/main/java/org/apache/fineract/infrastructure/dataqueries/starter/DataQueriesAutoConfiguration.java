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
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataServiceImpl;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlInjectionPreventerService;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
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
    public ReadWriteNonCoreDataService readWriteNonCoreDataService(final JdbcTemplate jdbcTemplate,
            final DatabaseTypeResolver databaseTypeResolver, final DatabaseSpecificSQLGenerator sqlGenerator,
            final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper, final GenericDataService genericDataService,
            final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ConfigurationDomainService configurationDomainService, final CodeReadPlatformService codeReadPlatformService,
            final DataTableValidator dataTableValidator, final ColumnValidator columnValidator,
            final NamedParameterJdbcTemplate namedParameterJdbcTemplate, final SqlInjectionPreventerService preventSqlInjectionService,
            DatatableKeywordGenerator datatableKeywordGenerator, SqlValidator sqlValidator, SearchUtil searchUtil) {
        return new ReadWriteNonCoreDataServiceImpl(jdbcTemplate, databaseTypeResolver, sqlGenerator, context, fromJsonHelper,
                genericDataService, fromApiJsonDeserializer, configurationDomainService, codeReadPlatformService, dataTableValidator,
                columnValidator, namedParameterJdbcTemplate, preventSqlInjectionService, datatableKeywordGenerator, sqlValidator,
                searchUtil);
    }
}
