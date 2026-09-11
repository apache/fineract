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

import java.net.Inet6Address;
import java.net.InetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.Ssl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class FineractApplicationLifecycleLogger {

    private final ApplicationContext applicationContext;
    private final ServerProperties serverProperties;
    private final WebEndpointProperties webEndpointProperties;
    private final ManagementServerProperties managementServerProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Integer serverPort = getServerPort();
        String baseUrl = createUrl(serverProperties.getSsl(), serverProperties.getAddress(), serverPort,
                serverProperties.getServlet().getContextPath());

        log.info("""
                Fineract lifecycle event:
                ========================================================================
                    APACHE FINERACT IS READY
                    Base URL: {}
                    Health:   {}
                ========================================================================""", baseUrl, createHealthUrl(baseUrl, serverPort));
    }

    @EventListener
    public void onApplicationClosing(ContextClosedEvent event) {
        // Child contexts (for example the one behind a dedicated management port) propagate their close event to the
        // parent; only the application's own context is worth announcing.
        if (event.getApplicationContext() == applicationContext) {
            log.info("""
                    Fineract lifecycle event:
                    ========================================================================
                        APACHE FINERACT IS SHUTTING DOWN GRACEFULLY
                    ========================================================================""");
        }
    }

    private String createHealthUrl(String baseUrl, Integer serverPort) {
        Integer managementPort = managementServerProperties.getPort();
        if (managementPort == null || managementPort.equals(serverPort)) {
            return baseUrl + webEndpointProperties.getBasePath() + "/health";
        }

        // A dedicated management port is served by its own context: the server's servlet context path does not apply
        // and both the address and the SSL settings fall back to the server's own when not overridden.
        Ssl ssl = managementServerProperties.getSsl() != null ? managementServerProperties.getSsl() : serverProperties.getSsl();
        InetAddress address = managementServerProperties.getAddress() != null ? managementServerProperties.getAddress()
                : serverProperties.getAddress();
        return createUrl(ssl, address, managementPort, managementServerProperties.getBasePath()) + webEndpointProperties.getBasePath()
                + "/health";
    }

    private String createUrl(Ssl ssl, InetAddress address, Integer port, String path) {
        return (Ssl.isEnabled(ssl) ? "https" : "http") + "://" + formatHost(address) + (port == null ? "" : ":" + port)
                + (path == null ? "" : path);
    }

    private String formatHost(InetAddress address) {
        if (address == null) {
            return "localhost";
        }
        String host = address.getHostAddress();
        return address instanceof Inet6Address ? "[" + host + "]" : host;
    }

    private Integer getServerPort() {
        if (applicationContext instanceof WebServerApplicationContext webApplicationContext
                && webApplicationContext.getWebServer() != null) {
            return webApplicationContext.getWebServer().getPort();
        }
        // Without an embedded web server (a WAR deployed into an external container) the listen port belongs to the
        // container, so the configured server.port is only a best effort and can be missing altogether.
        return serverProperties.getPort();
    }
}
