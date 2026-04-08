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
package org.apache.fineract.test.config;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@Slf4j
public class TestDatabaseConfiguration {

    @Value("${fineract-test.db.protocol}")
    private String protocol;

    @Value("${fineract-test.db.hostname}")
    private String hostname;

    @Value("${fineract-test.db.port}")
    private String port;

    @Value("${fineract-test.db.name}")
    private String dbName;

    @Value("${fineract-test.db.username}")
    private String username;

    @Value("${fineract-test.db.password}")
    private String password;

    @Bean
    public DataSource testDataSource() {
        // DriverManagerDataSource creates a new connection per call (no pooling).
        // This is intentional for lightweight e2e test usage — no pool management overhead needed.
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String url = protocol + "://" + hostname + ":" + port + "/" + dbName;
        log.debug("Test database URL: {}", url);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public JdbcTemplate testJdbcTemplate(DataSource testDataSource) {
        return new JdbcTemplate(testDataSource);
    }
}
