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
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.util.List;
import java.util.Objects;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.cache.service.CacheWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.filters.CorrelationHeaderFilter;
import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreFilter;
import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreHelper;
import org.apache.fineract.infrastructure.core.filters.RequestResponseFilter;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.MDCWrapper;
import org.apache.fineract.infrastructure.instancemode.filter.FineractInstanceModeApiFilter;
import org.apache.fineract.infrastructure.jobs.filter.LoanCOBApiFilter;
import org.apache.fineract.infrastructure.jobs.filter.LoanCOBFilterHelper;
import org.apache.fineract.infrastructure.security.data.PlatformRequestLog;
import org.apache.fineract.infrastructure.security.filter.InsecureTwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.filter.TenantAwareBasicAuthenticationFilter;
import org.apache.fineract.infrastructure.security.filter.TwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import org.apache.fineract.infrastructure.security.service.TwoFactorService;
import org.apache.fineract.notification.service.UserNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@ConditionalOnProperty("fineract.security.basicauth.enabled")
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TenantAwareJpaPlatformUserDetailsService userDetailsService;

    @Autowired
    private FineractProperties fineractProperties;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer;
    @Autowired
    private ConfigurationDomainService configurationDomainService;
    @Autowired
    private CacheWritePlatformService cacheWritePlatformService;
    @Autowired
    private UserNotificationService userNotificationService;
    @Autowired
    private BasicAuthTenantDetailsService basicAuthTenantDetailsService;
    @Autowired
    private BusinessDateReadPlatformService businessDateReadPlatformService;
    @Autowired
    private MDCWrapper mdcWrapper;
    @Autowired
    private CommandSourceRepository commandSourceRepository;
    @Autowired
    private CommandSourceService commandSourceService;
    @Autowired
    private FineractRequestContextHolder fineractRequestContextHolder;

    @Autowired(required = false)
    private LoanCOBFilterHelper loanCOBFilterHelper;
    @Autowired
    private PlatformSecurityContext context;
    @Autowired
    private IdempotencyStoreHelper idempotencyStoreHelper;

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
                            .requestMatchers(antMatcher(HttpMethod.PUT, "/api/*/instance-mode")).permitAll() //
                            .requestMatchers(antMatcher(HttpMethod.POST, "/api/*/twofactor/validate")).fullyAuthenticated() //
                            .requestMatchers(antMatcher("/api/*/twofactor")).fullyAuthenticated() //
                            .requestMatchers(antMatcher("/api/**"))
                            .access(allOf(fullyAuthenticated(), hasAuthority("TWOFACTOR_AUTHENTICATED"))); //
                }).httpBasic((httpBasic) -> httpBasic.authenticationEntryPoint(basicAuthenticationEntryPoint())) //
                .cors(Customizer.withDefaults()).csrf((csrf) -> csrf.disable()) // NOSONAR only creating a service that
                                                                                // is used by non-browser clients
                .sessionManagement((smc) -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //
                .addFilterBefore(tenantAwareBasicAuthenticationFilter(), SecurityContextHolderFilter.class) //
                .addFilterAfter(requestResponseFilter(), ExceptionTranslationFilter.class) //
                .addFilterAfter(correlationHeaderFilter(), RequestResponseFilter.class) //
                .addFilterAfter(fineractInstanceModeApiFilter(), CorrelationHeaderFilter.class); //
        if (!Objects.isNull(loanCOBFilterHelper)) {
            http.addFilterAfter(loanCOBApiFilter(), FineractInstanceModeApiFilter.class) //
                    .addFilterAfter(idempotencyStoreFilter(), LoanCOBApiFilter.class); //
        } else {
            http.addFilterAfter(idempotencyStoreFilter(), FineractInstanceModeApiFilter.class); //
        }

        if (fineractProperties.getSecurity().getTwoFactor().isEnabled()) {
            http.addFilterAfter(twoFactorAuthenticationFilter(), CorrelationHeaderFilter.class);
        } else {
            http.addFilterAfter(insecureTwoFactorAuthenticationFilter(), CorrelationHeaderFilter.class);
        }

        if (serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.requestMatchers(antMatcher("/api/**")).requiresSecure());
        }
        return http.build();
    }

    public RequestResponseFilter requestResponseFilter() {
        return new RequestResponseFilter();
    }

    public LoanCOBApiFilter loanCOBApiFilter() {
        return new LoanCOBApiFilter(loanCOBFilterHelper);
    }

    public TwoFactorAuthenticationFilter twoFactorAuthenticationFilter() {
        TwoFactorService twoFactorService = applicationContext.getBean(TwoFactorService.class);
        return new TwoFactorAuthenticationFilter(twoFactorService);
    }

    public InsecureTwoFactorAuthenticationFilter insecureTwoFactorAuthenticationFilter() {
        return new InsecureTwoFactorAuthenticationFilter();
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

    public TenantAwareBasicAuthenticationFilter tenantAwareBasicAuthenticationFilter() throws Exception {
        TenantAwareBasicAuthenticationFilter filter = new TenantAwareBasicAuthenticationFilter(authenticationManagerBean(),
                basicAuthenticationEntryPoint(), toApiJsonSerializer, configurationDomainService, cacheWritePlatformService,
                userNotificationService, basicAuthTenantDetailsService, businessDateReadPlatformService);
        filter.setRequestMatcher(antMatcher("/api/**"));
        return filter;
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
