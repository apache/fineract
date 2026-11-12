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
package org.apache.fineract.infrastructure.core.config;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcMySqlDialect;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Profile("!liquibase-only")
@Configuration
@EnableJdbcRepositories(basePackages = { "org.apache.fineract.command.jdbc.store.domain",
        "org.apache.fineract.infrastructure.documentmanagement.domain" }, jdbcOperationsRef = "namedParameterJdbcTemplate", transactionManagerRef = "jdbcTransactionManager")
public class JdbcConfig {

    @Bean
    public JdbcDialect jdbcDialect(DatabaseTypeResolver databaseTypeResolver) {
        // Spring Data JDBC 4 resolves the dialect eagerly by probing the DataSource, which does not
        // work with the per-tenant RoutingDataSource; MariaDB is handled as MySQL throughout Fineract
        return databaseTypeResolver.isPostgreSQL() ? JdbcPostgresDialect.INSTANCE : JdbcMySqlDialect.INSTANCE;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(RoutingDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(RoutingDataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public com.querydsl.sql.Configuration querydslConfiguration(DatabaseTypeResolver databaseTypeResolver) {
        var templates = databaseTypeResolver.isPostgreSQL() ? PostgreSQLTemplates.builder().build() : MySQLTemplates.builder().build();
        com.querydsl.sql.Configuration config = new com.querydsl.sql.Configuration(templates);
        config.setExceptionTranslator(new SpringExceptionTranslator());
        return config;
    }

    @Bean
    public SQLQueryFactory sqlQueryFactory(com.querydsl.sql.Configuration config, DataSource dataSource) {
        return new SQLQueryFactory(config, new SpringConnectionProvider(dataSource));
    }
}
