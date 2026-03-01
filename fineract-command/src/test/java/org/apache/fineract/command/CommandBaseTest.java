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
package org.apache.fineract.command;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.persistence.domain.CommandRepository;
import org.apache.fineract.command.persistence.mapping.CommandMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
abstract class CommandBaseTest {

    protected static Network network = Network.newNetwork();

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3"))
            .withNetwork(network).withUsername("root").withPassword("mifos").withDatabaseName("fineract-test");

    @Container
    private static final MariaDBContainer<?> MARIADB_CONTAINER = new MariaDBContainer<>(DockerImageName.parse("mariadb:11.4"))
            .withNetwork(network).withUsername("root").withPassword("mifos").withDatabaseName("fineract-test");

    @Container
    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8")).withNetwork(network)
            .withUsername("root").withPassword("mifos").withDatabaseName("fineract-test");

    @Autowired
    protected CommandRepository commandRepository;

    @Autowired
    protected CommandMapper commandMapper;

    @DynamicPropertySource
    protected static void configure(DynamicPropertyRegistry registry) {
        postgres(registry);
        // mariadb(registry);
        // mysql(registry);
    }

    @BeforeAll
    static void requireDocker() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not available, skipping Testcontainers tests");
    }

    private static void postgres(DynamicPropertyRegistry registry) {
        POSTGRES_CONTAINER.start();

        registry.add("spring.datasource.driver-class-name", POSTGRES_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.platform", () -> "postgresql");
        registry.add("spring.liquibase.contexts", () -> "postgresql");
    }

    @SuppressWarnings("UnusedMethod")
    private static void mariadb(DynamicPropertyRegistry registry) {
        MARIADB_CONTAINER.start();

        registry.add("spring.datasource.driver-class-name", MARIADB_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.username", MARIADB_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MARIADB_CONTAINER::getPassword);
        registry.add("spring.datasource.url", MARIADB_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.platform", () -> "mysql");
        registry.add("spring.liquibase.contexts", () -> "mysql");
    }

    @SuppressWarnings("UnusedMethod")
    private static void mysql(DynamicPropertyRegistry registry) {
        MYSQL_CONTAINER.start();

        registry.add("spring.datasource.driver-class-name", MYSQL_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.platform", () -> "mysql");
        registry.add("spring.liquibase.contexts", () -> "mysql");
    }
}
