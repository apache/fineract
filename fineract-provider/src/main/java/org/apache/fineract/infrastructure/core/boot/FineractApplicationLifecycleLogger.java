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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FineractApplicationLifecycleLogger {

    private volatile ConfigurableApplicationContext readyApplicationContext;

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        readyApplicationContext = applicationContext;
        Environment environment = applicationContext.getEnvironment();
        log.info("Fineract lifecycle event: {}", createReadyMessage(environment, getServerPort(applicationContext)));
    }

    @EventListener
    public void onApplicationClosing(ContextClosedEvent event) {
        if (event.getApplicationContext() == readyApplicationContext) {
            log.info("Fineract lifecycle event: {}", createShuttingDownMessage());
        }
    }

    static String createReadyMessage(Environment environment, int serverPort) {
        String scheme = environment.getProperty("server.ssl.enabled", Boolean.class, false) ? "https" : "http";
        String host = formatHost(environment.getProperty("server.address", "localhost"));
        String contextPath = normalizeContextPath(environment.getProperty("server.servlet.context-path", ""));
        String baseUrl = scheme + "://" + host + ":" + serverPort + contextPath;

        return String.join(System.lineSeparator(), "", "========================================================================",
                "    APACHE FINERACT IS READY", "    Base URL: " + baseUrl, "    Health:   " + baseUrl + "/actuator/health",
                "========================================================================");
    }

    static String createShuttingDownMessage() {
        return String.join(System.lineSeparator(), "", "========================================================================",
                "    APACHE FINERACT IS SHUTTING DOWN GRACEFULLY",
                "========================================================================");
    }

    private static int getServerPort(ConfigurableApplicationContext applicationContext) {
        if (applicationContext instanceof WebServerApplicationContext webApplicationContext
                && webApplicationContext.getWebServer() != null) {
            return webApplicationContext.getWebServer().getPort();
        }
        return applicationContext.getEnvironment().getProperty("server.port", Integer.class, 8080);
    }

    private static String formatHost(String host) {
        if (host.isBlank()) {
            return "localhost";
        }
        if (host.equals("0.0.0.0") || host.equals("::")) {
            return "localhost";
        }
        return host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
    }

    private static String normalizeContextPath(String contextPath) {
        if (contextPath.isBlank() || contextPath.equals("/")) {
            return "";
        }
        String normalizedContextPath = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
        return normalizedContextPath.endsWith("/") ? normalizedContextPath.substring(0, normalizedContextPath.length() - 1)
                : normalizedContextPath;
    }
}
