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
package org.apache.fineract.oauth2tests;

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
@SelectPackages({ "org.apache.fineract.oauth2test" })
@SuiteDisplayName("Fineract OAuth2 Test Suite")
@SuppressWarnings({ "HideUtilityClassConstructor" })
public class OAuth2TestSuite {

    private static final Logger log = LoggerFactory.getLogger(OAuth2TestSuite.class);

    public static final Network network = Network.newNetwork();

    private static final String TEST_OAUTH_SERVER_DOCKER_IMAGE = "ghcr.io/navikt/mock-oauth2-server:0.4.0";
    private static final Integer TEST_OAUTH_SERVER_PORT = 9000;
    private static final String TEST_DB_DOCKER_IMAGE = "mariadb:10.6";
    private static final String TEST_DB_USER = "root";
    private static final String TEST_DB_PW = "skdcnwauicn2ucnaecasdsajdnizucawencascdca";
    private static final String TEST_DB_DRIVER = "org.mariadb.jdbc.Driver";
    private static final String TEST_DB_HOST = "mariadb";
    private static final Integer TEST_DB_PORT = 3306;

    public static final GenericContainer<?> mariadb = new GenericContainer<>(TEST_DB_DOCKER_IMAGE).withNetworkAliases(TEST_DB_HOST)
            .withExposedPorts(TEST_DB_PORT).withLogConsumer(new Slf4jLogConsumer(log))
            .withFileSystemBind("../fineract-db/docker", "/docker-entrypoint-initdb.d", BindMode.READ_ONLY)
            .withEnv("MARIADB_ROOT_PASSWORD", TEST_DB_PW).withNetwork(network)
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 1).withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS)));

    public static final GenericContainer<?> oauthServer = new GenericContainer<>(TEST_OAUTH_SERVER_DOCKER_IMAGE)
            .withNetworkAliases(TEST_DB_HOST).withExposedPorts(TEST_OAUTH_SERVER_PORT).withLogConsumer(new Slf4jLogConsumer(log))
            .withEnv("SERVER_PORT", TEST_OAUTH_SERVER_PORT.toString())
            .withEnv("JSON_CONFIG",
                    "{ \"interactiveLogin\": true, \"httpServer\": \"NettyWrapper\", \"tokenCallbacks\": [ { \"issuerId\": \"auth/realms/fineract\", \"tokenExpiry\": 120, \"requestMappings\": [{ \"requestParam\": \"scope\", \"match\": \"fineract\", \"claims\": { \"sub\": \"mifos\", \"scope\": [ \"test\" ] } } ] } ] }")
            .withNetwork(network).waitingFor(Wait.forLogMessage(".*OAuth2HttpServer - started server on address.*", 1)
                    .withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS)));

    public static final GenericContainer<?> fineract = new GenericContainer<>("fineract:latest").withNetworkAliases("fineract")
            .withImagePullPolicy(PullPolicy.defaultPolicy()).dependsOn(mariadb).withNetwork(network)
            .withLogConsumer(new Slf4jLogConsumer(log)).withExposedPorts(8443).withEnv("fineract.driver", TEST_DB_DRIVER)
            .withTmpFs(singletonMap("/tmp/.fineract", "rw")).withEnv("user.home", "/tmp")
            .withEnv("fineract.url", "jdbc:mariadb://" + TEST_DB_HOST + ":" + TEST_DB_PORT + "/fineract_tenants")
            .withEnv("fineract.username", TEST_DB_USER).withEnv("fineract.password", TEST_DB_PW).withEnv("DRIVERCLASS_NAME", TEST_DB_DRIVER)
            .withEnv("PROTOCOL", "jdbc").withEnv("node_id", "1").withEnv("SUB_PROTOCOL", "mariadb")
            .withEnv("fineract_tenants_driver", TEST_DB_DRIVER)
            .withEnv("fineract_tenants_url", "jdbc:mariadb://" + TEST_DB_HOST + ":" + TEST_DB_PORT + "/fineract_tenants")
            .withEnv("fineract_tenants_uid", TEST_DB_USER).withEnv("fineract_tenants_pwd", TEST_DB_PW)
            .withEnv("FINERACT_DEFAULT_TENANTDB_HOSTNAME", TEST_DB_HOST).withEnv("FINERACT_DEFAULT_TENANTDB_PORT", TEST_DB_PORT.toString())
            .withEnv("FINERACT_DEFAULT_TENANTDB_UID", TEST_DB_USER).withEnv("FINERACT_DEFAULT_TENANTDB_PWD", TEST_DB_PW)
            .withEnv("FINERACT_DEFAULT_TENANTDB_CONN_PARAMS", "").withEnv("FINERACT_SECURITY_BASICAUTH_ENABLED", "false")
            .withEnv("FINERACT_SECURITY_OAUTH_ENABLED", "true")
            .withCommand("java", "-cp", "/app/resources:/app/classes:/app/libs/*", "-Xmx1G", "-Xms1G", "-XshowSettings:vm",
                    "-XX:+UseContainerSupport", "-XX:+UseStringDeduplication", "-XX:MinRAMPercentage=25", "-XX:MaxRAMPercentage=80",
                    "--add-exports=java.naming/com.sun.jndi.ldap=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED",
                    "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED",
                    "--add-opens=java.base/java.security=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED",
                    "--add-opens=java.management/javax.management=ALL-UNNAMED", "--add-opens=java.naming/javax.naming=ALL-UNNAMED",
                    "org.apache.fineract.ServerApplication", "-Duser.home=/tmp/.fineract", "-Dfile.encoding=UTF-8",
                    "-Duser.timezone=Asia/Kolkata", "-Djava.security.egd=file:/dev/./urandom")
            .waitingFor(Wait.forLogMessage(".*JVM running for.*", 1).withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS)));

    static {
        mariadb.start();
        oauthServer.start();
        fineract.start();
    }
}
