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

package org.apache.fineract.infrastructure.core.config;

import static org.apache.fineract.infrastructure.security.vote.SelfServiceUserAuthorizationManager.selfServiceUserAuthManager;
import static org.springframework.security.authorization.AuthenticatedAuthorizationManager.fullyAuthenticated;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;
import static org.springframework.security.authorization.AuthorizationManagers.allOf;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.util.Collection;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.cache.service.CacheWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exceptionmapper.OAuth2ExceptionEntryPoint;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.data.FineractJwtAuthenticationToken;
import org.apache.fineract.infrastructure.security.data.PlatformRequestLog;
import org.apache.fineract.infrastructure.security.filter.InsecureTwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.filter.TenantAwareTenantIdentifierFilter;
import org.apache.fineract.infrastructure.security.filter.TwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import org.apache.fineract.infrastructure.security.service.TwoFactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@ConditionalOnProperty("fineract.security.oauth.enabled")
@EnableMethodSecurity
public class OAuth2SecurityConfig {

    @Autowired
    private TenantAwareJpaPlatformUserDetailsService userDetailsService;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private FineractProperties fineractProperties;

    @Autowired
    private BasicAuthTenantDetailsService basicAuthTenantDetailsService;

    @Autowired
    private ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer;

    @Autowired
    private ConfigurationDomainService configurationDomainService;

    @Autowired
    private CacheWritePlatformService cacheWritePlatformService;

    @Autowired
    private BusinessDateReadPlatformService businessDateReadPlatformService;
    @Autowired
    private ApplicationContext applicationContext;

    private static final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http //
                .securityMatcher(antMatcher("/api/**")).authorizeHttpRequests((auth) -> {
                    auth.requestMatchers(antMatcher(HttpMethod.OPTIONS, "/api/**")).permitAll() //
                            .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/echo")).permitAll() //
                            .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/authentication")).permitAll() //
                            .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/self/authentication")).permitAll() //
                            .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/self/registration")).permitAll() //
                            .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/self/registration/user")).permitAll() //
                            .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/twofactor/validate")).fullyAuthenticated() //
                            .requestMatchers(antMatcher("/api/*/twofactor")).fullyAuthenticated() //
                            .requestMatchers(antMatcher("/api/**"))
                            .access(allOf(fullyAuthenticated(), hasAuthority("TWOFACTOR_AUTHENTICATED"), selfServiceUserAuthManager())); //
                }).csrf((csrf) -> csrf.disable()) // NOSONAR only creating a service that is used by non-browser clients
                .exceptionHandling((ehc) -> ehc.authenticationEntryPoint(new OAuth2ExceptionEntryPoint()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter()))
                        .authenticationEntryPoint(new OAuth2ExceptionEntryPoint())) //
                .sessionManagement((smc) -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //
                .addFilterAfter(tenantAwareTenantIdentifierFilter(), SecurityContextHolderFilter.class);

        if (fineractProperties.getSecurity().getTwoFactor().isEnabled()) {
            http.addFilterAfter(twoFactorAuthenticationFilter(), BasicAuthenticationFilter.class);
        } else {
            http.addFilterAfter(insecureTwoFactorAuthenticationFilter(), BasicAuthenticationFilter.class);
        }

        if (serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.requestMatchers(antMatcher("/api/**")).requiresSecure());
        }

        return http.build();
    }

    public TenantAwareTenantIdentifierFilter tenantAwareTenantIdentifierFilter() {
        return new TenantAwareTenantIdentifierFilter(basicAuthTenantDetailsService, toApiJsonSerializer, configurationDomainService,
                cacheWritePlatformService, businessDateReadPlatformService);
    }

    public TwoFactorAuthenticationFilter twoFactorAuthenticationFilter() {
        TwoFactorService twoFactorService = applicationContext.getBean(TwoFactorService.class);
        return new TwoFactorAuthenticationFilter(twoFactorService);
    }

    public InsecureTwoFactorAuthenticationFilter insecureTwoFactorAuthenticationFilter() {
        return new InsecureTwoFactorAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private Converter<Jwt, FineractJwtAuthenticationToken> authenticationConverter() {
        return jwt -> {
            try {
                UserDetails user = userDetailsService.loadUserByUsername(jwt.getSubject());
                jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
                Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
                return new FineractJwtAuthenticationToken(jwt, authorities, user);
            } catch (UsernameNotFoundException ex) {
                throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN), ex);
            }
        };
    }
}
