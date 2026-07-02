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
package org.apache.fineract.infrastructure.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.security.data.FineractOidcUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
public class OidcLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final String FALLBACK_URL = "/login?logout";

    private final FineractProperties fineractProperties;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        if (authentication != null && authentication.getPrincipal() instanceof FineractOidcUser oidcUser) {
            log.debug("OIDC logout initiated for user '{}' (tenant: '{}')", oidcUser.getName(), oidcUser.getTenantId());

            String provider = fineractProperties.getSecurity().getOidcFederation().getProvider().getCode();
            String logoutUrl = buildLogoutUrl(provider, oidcUser, request);

            if (logoutUrl != null) {
                log.debug("Redirecting to IdP logout URL: {}", logoutUrl);
                response.sendRedirect(logoutUrl);
                return;
            }
        }

        // Fallback: no IdP logout — redirect locally
        fallbackHandler().onLogoutSuccess(request, response, authentication);
    }

    private String buildLogoutUrl(String provider, FineractOidcUser oidcUser, HttpServletRequest request) {
        String idToken = oidcUser.getIdToken().getTokenValue();
        String issuer = oidcUser.getIdToken().getIssuer().toString();
        String postLogoutUri = resolvePostLogoutUri(request);

        return switch (provider) {
            case "keycloak" ->
                UriComponentsBuilder.fromUriString(issuer).path("/protocol/openid-connect/logout").queryParam("id_token_hint", idToken)
                        .queryParam("post_logout_redirect_uri", postLogoutUri).encode(StandardCharsets.UTF_8).toUriString();

            case "azure_ad" -> UriComponentsBuilder.fromUriString(issuer).path("/oauth2/v2.0/logout")
                    .queryParam("post_logout_redirect_uri", postLogoutUri).encode(StandardCharsets.UTF_8).toUriString();

            case "okta" -> UriComponentsBuilder.fromUriString(issuer).path("/v1/logout").queryParam("id_token_hint", idToken)
                    .queryParam("post_logout_redirect_uri", postLogoutUri).encode(StandardCharsets.UTF_8).toUriString();

            case "auth0" -> UriComponentsBuilder.fromUriString(issuer).path("/v2/logout").queryParam("returnTo", postLogoutUri)
                    .encode(StandardCharsets.UTF_8).toUriString();

            // "generic" and any unknown value: let Spring discover end_session_endpoint
            default -> null;
        };
    }

    private LogoutSuccessHandler fallbackHandler() {
        SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
        handler.setDefaultTargetUrl(FALLBACK_URL);
        return handler;
    }

    private String resolvePostLogoutUri(HttpServletRequest request) {
        String configured = fineractProperties.getSecurity().getOidcFederation().getPostLogoutRedirectUri();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        // Build from the incoming request base URL
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        StringBuilder base = new StringBuilder(scheme).append("://").append(host);
        if (!((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443))) {
            base.append(":").append(port);
        }
        return base.append(FALLBACK_URL).toString();
    }
}
