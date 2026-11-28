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
package org.apache.fineract.test.querydsl;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import javax.sql.DataSource;
import org.apache.fineract.test.database.DatabaseContainerTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootTest(classes = QuerydslDatabaseTest.DatabaseConfiguration.class, webEnvironment = WebEnvironment.NONE, properties = "spring.liquibase.change-log=classpath:/querydsl-test-fixtures/db/changelog/querydsl-changelog-master.xml")
public abstract class QuerydslDatabaseTest extends DatabaseContainerTest {

    @ImportAutoConfiguration({ DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class, LiquibaseAutoConfiguration.class })
    @EnableTransactionManagement
    static class DatabaseConfiguration {

        @Bean
        SQLQueryFactory queryFactory(final DataSource dataSource, @Value("${spring.datasource.platform}") final String platform) {
            final SQLTemplates templates = "postgresql".equals(platform) ? PostgreSQLTemplates.DEFAULT : MySQLTemplates.DEFAULT;
            final com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(templates);
            configuration.setExceptionTranslator(new SpringExceptionTranslator());
            return new SQLQueryFactory(configuration, new SpringConnectionProvider(dataSource));
        }
    }
}
