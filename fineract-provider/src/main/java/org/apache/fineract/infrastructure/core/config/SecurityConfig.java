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

import static org.springframework.security.authorization.AuthenticatedAuthorizationManager.fullyAuthenticated;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;
import static org.springframework.security.authorization.AuthorizationManagers.allOf;

import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreFilter;
import org.apache.fineract.infrastructure.instancemode.filter.FineractInstanceModeApiFilter;
import org.apache.fineract.infrastructure.jobs.filter.LoanCOBApiFilter;
import org.apache.fineract.infrastructure.security.filter.InsecureTwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.filter.TenantAwareBasicAuthenticationFilter;
import org.apache.fineract.infrastructure.security.filter.TwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@ConditionalOnProperty("fineract.security.basicauth.enabled")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private TenantAwareJpaPlatformUserDetailsService userDetailsService;

    @Autowired
    private TwoFactorAuthenticationFilter twoFactorAuthenticationFilter;

    @Autowired
    private FineractInstanceModeApiFilter fineractInstanceModeApiFilter;
    @Autowired
    private LoanCOBApiFilter loanCOBApiFilter;

    @Autowired
    private FineractProperties fineractProperties;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private IdempotencyStoreFilter idempotencyStoreFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http //
                .csrf((csrf) -> csrf.disable()) // NOSONAR only creating a service that is used by non-browser clients
                .securityMatcher("/api/**").authorizeHttpRequests((auth) -> {
                    auth
                            .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/echo").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/authentication").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/self/authentication").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/self/registration").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/self/registration/user").permitAll() //
                            .requestMatchers(HttpMethod.PUT, "/api/*/instance-mode").permitAll() //
                            .requestMatchers(HttpMethod.POST, "/api/*/twofactor/validate").fullyAuthenticated() //
                            .requestMatchers("/api/*/twofactor").fullyAuthenticated() //
                            .requestMatchers("/api/**").access(allOf(fullyAuthenticated(), hasAuthority("TWOFACTOR_AUTHENTICATED"))); //
                }) //
                .httpBasic((httpBasic) -> httpBasic.authenticationEntryPoint(basicAuthenticationEntryPoint())) //
                .sessionManagement((smc) -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //
                .addFilterAfter(fineractInstanceModeApiFilter, SecurityContextHolderFilter.class) //
                .addFilterAfter(tenantAwareBasicAuthenticationFilter(), FineractInstanceModeApiFilter.class) //
                .addFilterAfter(twoFactorAuthenticationFilter, BasicAuthenticationFilter.class) //
                .addFilterAfter(loanCOBApiFilter, InsecureTwoFactorAuthenticationFilter.class)
                .addFilterBefore(idempotencyStoreFilter, ExceptionTranslationFilter.class);

        if (serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.requestMatchers("/api/**").requiresSecure());
        }
        return http.build();
    }

    @Bean
    public TenantAwareBasicAuthenticationFilter tenantAwareBasicAuthenticationFilter() throws Exception {
        return new TenantAwareBasicAuthenticationFilter(authenticationManagerBean(), basicAuthenticationEntryPoint());
    }

    @Bean
    public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint() {
        BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
        basicAuthenticationEntryPoint.setRealmName("Fineract Platform API");
        return basicAuthenticationEntryPoint;
    }

    @Bean(name = "customAuthenticationProvider")
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        ProviderManager providerManager = new ProviderManager(authProvider());
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;
    }

    @Bean
    public FilterRegistrationBean<TenantAwareBasicAuthenticationFilter> tenantAwareBasicAuthenticationFilterRegistration()
            throws Exception {
        FilterRegistrationBean<TenantAwareBasicAuthenticationFilter> registration = new FilterRegistrationBean<TenantAwareBasicAuthenticationFilter>(
                tenantAwareBasicAuthenticationFilter());
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TwoFactorAuthenticationFilter> twoFactorAuthenticationFilterRegistration() {
        FilterRegistrationBean<TwoFactorAuthenticationFilter> registration = new FilterRegistrationBean<TwoFactorAuthenticationFilter>(
                twoFactorAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }
}
