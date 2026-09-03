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
package org.apache.fineract.infrastructure.core.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

public class FineractApplicationLifecycleLoggerTest {

    @Test
    public void readyMessageContainsConfiguredHttpsUrlAndHealthEndpoint() {
        MockEnvironment environment = new MockEnvironment().withProperty("server.ssl.enabled", "true")
                .withProperty("server.servlet.context-path", "/fineract-provider/");

        String message = FineractApplicationLifecycleLogger.createReadyMessage(environment, 8443);

        assertThat(message).contains("APACHE FINERACT IS READY").contains("Base URL: https://localhost:8443/fineract-provider")
                .contains("Health:   https://localhost:8443/fineract-provider/actuator/health");
    }

    @Test
    public void readyMessageSupportsHttpRootContextAndIpv6Address() {
        MockEnvironment environment = new MockEnvironment().withProperty("server.address", "2001:db8::1")
                .withProperty("server.servlet.context-path", "/");

        String message = FineractApplicationLifecycleLogger.createReadyMessage(environment, 8080);

        assertThat(message).contains("Base URL: http://[2001:db8::1]:8080").contains("Health:   http://[2001:db8::1]:8080/actuator/health");
    }

    @Test
    public void shuttingDownMessageDescribesAnInProgressGracefulShutdown() {
        assertThat(FineractApplicationLifecycleLogger.createShuttingDownMessage()).contains("APACHE FINERACT IS SHUTTING DOWN GRACEFULLY");
    }
}
