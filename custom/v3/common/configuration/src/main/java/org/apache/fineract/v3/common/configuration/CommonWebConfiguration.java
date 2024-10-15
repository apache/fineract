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
package org.apache.fineract.v3.common.configuration;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.cache.service.CacheWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.filters.CorrelationHeaderFilter;
import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreFilter;
import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreHelper;
import org.apache.fineract.infrastructure.core.filters.RequestResponseFilter;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.MDCWrapper;
import org.apache.fineract.infrastructure.instancemode.filter.FineractInstanceModeApiFilter;
import org.apache.fineract.infrastructure.security.data.PlatformRequestLog;
import org.apache.fineract.infrastructure.security.filter.TenantAwareBasicAuthenticationFilter;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.service.UserNotificationService;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Slf4j
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class CommonWebConfiguration {

    private final FineractProperties fineractProperties;

    private final ServerProperties serverProperties;

    private final BasicAuthenticationEntryPoint basicAuthenticationEntryPoint;

    private final AuthenticationManager authenticationManagerBean;

    private final ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer;

    private final ConfigurationDomainService configurationDomainService;

    private final CacheWritePlatformService cacheWritePlatformService;

    private final UserNotificationService userNotificationService;

    private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;

    private final BusinessDateReadPlatformService businessDateReadPlatformService;

    private final MDCWrapper mdcWrapper;

    private final FineractRequestContextHolder fineractRequestContextHolder;

    private final IdempotencyStoreHelper idempotencyStoreHelper;

    @Bean
    @Order(1)
    public SecurityFilterChain v3FilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher(antMatcher("/v3/**")).csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((auth) -> auth.requestMatchers(antMatcher(HttpMethod.OPTIONS, "/v3/**")).permitAll()
                        .requestMatchers(antMatcher("/v3/**")).fullyAuthenticated())
                .httpBasic((httpBasic) -> httpBasic.authenticationEntryPoint(basicAuthenticationEntryPoint))
                .sessionManagement((smc) -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tenantAwareBasicAuthenticationFilter(), SecurityContextHolderFilter.class)
                .addFilterAfter(requestResponseFilter(), ExceptionTranslationFilter.class)
                .addFilterAfter(correlationHeaderFilter(), RequestResponseFilter.class)
                .addFilterAfter(fineractInstanceModeApiFilter(), CorrelationHeaderFilter.class)
                .addFilterAfter(idempotencyStoreFilter(), FineractInstanceModeApiFilter.class);

        if (serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.requestMatchers(antMatcher("/v3/**")).requiresSecure());
        }

        return http.build();
    }

    public RequestResponseFilter requestResponseFilter() {
        return new RequestResponseFilter();
    }

    public FineractInstanceModeApiFilter fineractInstanceModeApiFilter() {
        return new FineractInstanceModeApiFilter(fineractProperties);
    }

    public IdempotencyStoreFilter idempotencyStoreFilter() {
        return new IdempotencyStoreFilter(fineractRequestContextHolder, idempotencyStoreHelper, fineractProperties);
    }

    public CorrelationHeaderFilter correlationHeaderFilter() {
        return new CorrelationHeaderFilter(fineractProperties, mdcWrapper);
    }

    public TenantAwareBasicAuthenticationFilter tenantAwareBasicAuthenticationFilter() {
        TenantAwareBasicAuthenticationFilter filter = new TenantAwareBasicAuthenticationFilter(authenticationManagerBean,
                basicAuthenticationEntryPoint, toApiJsonSerializer, configurationDomainService, cacheWritePlatformService,
                userNotificationService, basicAuthTenantDetailsService, businessDateReadPlatformService);
        filter.setRequestMatcher(antMatcher("/v3/**"));
        return filter;
    }
}
