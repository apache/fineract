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

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.persistence.domain.CommandRepository;
import org.apache.fineract.command.persistence.mapping.CommandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
abstract class CommandBaseTest {

    protected static Network network = Network.newNetwork();

    @Container
    protected static final GenericContainer POSTGRES_CONTAINER = new GenericContainer("postgres:16").withNetwork(network)
            .withNetworkAliases("postgres").withExposedPorts(5432)
            .withEnv(Map.of("POSTGRES_DB", "fineract-test", "POSTGRES_USER", "root", "POSTGRES_PASSWORD", "mifos"));

    @Autowired
    protected CommandRepository commandRepository;

    @Autowired
    protected CommandMapper commandMapper;

    @DynamicPropertySource
    protected static void configure(DynamicPropertyRegistry registry) {
        POSTGRES_CONTAINER.start();

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "mifos");
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://" + POSTGRES_CONTAINER.getHost() + ":"
                + POSTGRES_CONTAINER.getMappedPort(5432) + "/fineract-test");
        registry.add("spring.datasource.platform", () -> "postgresql");
    }
}
