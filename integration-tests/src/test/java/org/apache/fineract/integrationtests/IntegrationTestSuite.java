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
package org.apache.fineract.integrationtests;

import static java.util.Collections.singletonMap;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;

@Suite
@SelectPackages({"org.apache.fineract.integrationtests"})
@SuiteDisplayName("Fineract Integration Test Suite")
public class IntegrationTestSuite {

    private static final Logger log = LoggerFactory.getLogger(IntegrationTestSuite.class);

    public static final Network network = Network.newNetwork();

    public static final GenericContainer<?> mysql = new GenericContainer<>("mysql:5.7").withNetworkAliases("mysql").withExposedPorts(3306)
            .withLogConsumer(new Slf4jLogConsumer(log))
            .withFileSystemBind("../fineract-db/docker", "/docker-entrypoint-initdb.d", BindMode.READ_ONLY)
            .withEnv("MYSQL_ROOT_PASSWORD", "skdcnwauicn2ucnaecasdsajdnizucawencascdca").withNetwork(network)
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 1).withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS)));

    public static final GenericContainer<?> fineract = new GenericContainer<>("fineract:latest").withNetworkAliases("fineract")
            .withImagePullPolicy(PullPolicy.defaultPolicy()).dependsOn(mysql).withNetwork(network)
            .withLogConsumer(new Slf4jLogConsumer(log)).withExposedPorts(8443).withEnv("fineract.driver", "org.drizzle.jdbc.DrizzleDriver")
            .withTmpFs(singletonMap("/tmp/.fineract", "rw")).withEnv("user.home", "/tmp")
            .withEnv("fineract.url", "jdbc:mysql:thin://mysql:3306/fineract_tenants").withEnv("fineract.username", "root")
            .withEnv("fineract.password", "skdcnwauicn2ucnaecasdsajdnizucawencascdca")
            .withEnv("DRIVERCLASS_NAME", "org.drizzle.jdbc.DrizzleDriver").withEnv("PROTOCOL", "jdbc").withEnv("node_id", "1")
            .withEnv("SUB_PROTOCOL", "mysql:thin").withEnv("fineract_tenants_driver", "org.drizzle.jdbc.DrizzleDriver")
            .withEnv("fineract_tenants_url", "jdbc:mysql:thin://mysql:3306/fineract_tenants").withEnv("fineract_tenants_uid", "root")
            .withEnv("fineract_tenants_pwd", "skdcnwauicn2ucnaecasdsajdnizucawencascdca")
            .withEnv("FINERACT_DEFAULT_TENANTDB_HOSTNAME", "mysql").withEnv("FINERACT_DEFAULT_TENANTDB_PORT", "3306")
            .withEnv("FINERACT_DEFAULT_TENANTDB_UID", "root")
            .withEnv("FINERACT_DEFAULT_TENANTDB_PWD", "skdcnwauicn2ucnaecasdsajdnizucawencascdca")
            .withEnv("FINERACT_DEFAULT_TENANTDB_CONN_PARAMS", "")
            .withCommand("java", "-cp", "/app/resources:/app/classes:/app/libs/*", "-Xmx1G", "-Xms1G", "-XshowSettings:vm",
                    "-XX:+UseContainerSupport", "-XX:+UseStringDeduplication", "-XX:MinRAMPercentage=25", "-XX:MaxRAMPercentage=80",
                    "org.apache.fineract.ServerApplication", "-Duser.home=/tmp/.fineract", "-Dspring.profiles.active=basicauth",
                    "-Dfile.encoding=UTF-8", "-Duser.timezone=Asia/Kolkata", "-Djava.security.egd=file:/dev/./urando")
            .waitingFor(Wait.forLogMessage(".*JVM running for.*", 1).withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS)));

    static {
        mysql.start();
        fineract.start();
    }
}
