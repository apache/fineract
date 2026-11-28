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
package org.apache.fineract.test.database;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public abstract class DatabaseContainerTest {

    private static final JdbcDatabaseContainer<?> DATABASE = createDatabase(System.getProperty("fineract.test.database", "postgresql"));

    private static JdbcDatabaseContainer<?> createDatabase(final String engine) {
        final JdbcDatabaseContainer<?> database = switch (engine) {
            case "postgresql" -> new PostgreSQLContainer<>("postgres:18.3-bookworm");
            case "mysql" ->
                new MySQLContainer<>("mysql:8").withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");
            case "mariadb" -> new MariaDBContainer<>("mariadb:12.2").withCommand("--innodb-snapshot-isolation=OFF",
                    "--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");
            default ->
                throw new IllegalArgumentException("Expected postgresql, mysql or mariadb for fineract.test.database, got " + engine);
        };
        return database.withDatabaseName("fineract-test").withUsername("root").withPassword("mifos");
    }

    @DynamicPropertySource
    static synchronized void configureDatabase(final DynamicPropertyRegistry registry) {
        DATABASE.start();
        final String platform = DATABASE instanceof PostgreSQLContainer<?> ? "postgresql" : "mysql";
        registry.add("spring.datasource.driver-class-name", DATABASE::getDriverClassName);
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
        registry.add("spring.datasource.hikari.driver-class-name", DATABASE::getDriverClassName);
        registry.add("spring.datasource.hikari.jdbc-url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.hikari.username", DATABASE::getUsername);
        registry.add("spring.datasource.hikari.password", DATABASE::getPassword);
        registry.add("spring.datasource.platform", () -> platform);
        registry.add("spring.liquibase.contexts", () -> platform);
    }
}
