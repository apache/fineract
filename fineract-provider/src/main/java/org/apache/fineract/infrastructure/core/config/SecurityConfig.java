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

import jakarta.servlet.Filter;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.cache.service.CacheWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.filters.CallerIpTrackingFilter;
import org.apache.fineract.infrastructure.core.filters.CorrelationHeaderFilter;
import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreFilter;
import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreHelper;
import org.apache.fineract.infrastructure.core.filters.RequestResponseFilter;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.MDCWrapper;
import org.apache.fineract.infrastructure.instancemode.filter.FineractInstanceModeApiFilter;
import org.apache.fineract.infrastructure.jobs.filter.LoanCOBApiFilter;
import org.apache.fineract.infrastructure.jobs.filter.LoanCOBFilterHelper;
import org.apache.fineract.infrastructure.jobs.filter.ProgressiveLoanModelCheckerFilter;
import org.apache.fineract.infrastructure.jobs.filter.WorkingCapitalLoanCOBApiFilter;
import org.apache.fineract.infrastructure.jobs.filter.WorkingCapitalLoanCOBFilterHelper;
import org.apache.fineract.infrastructure.jobs.filter.WorkingCapitalLoanModelCheckerFilter;
import org.apache.fineract.infrastructure.security.data.PlatformRequestLog;
import org.apache.fineract.infrastructure.security.filter.DatabaseUnavailableAwareAuthenticationEntryPoint;
import org.apache.fineract.infrastructure.security.filter.TenantAwareBasicAuthenticationFilter;
import org.apache.fineract.infrastructure.security.filter.TwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.service.AuthTenantDetailsService;
import org.apache.fineract.infrastructure.security.service.PlatformUserDetailsChecker;
import org.apache.fineract.infrastructure.security.service.TemporaryPasswordAwareAuthenticationProvider;
import org.apache.fineract.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import org.apache.fineract.infrastructure.security.service.TwoFactorService;
import org.apache.fineract.notification.service.UserNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@ConditionalOnProperty("fineract.security.basicauth.enabled")
@EnableMethodSecurity
public class SecurityConfig {

    private static final PathPatternRequestMatcher.Builder API_MATCHER = PathPatternRequestMatcher.withDefaults();

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
    private AuthTenantDetailsService basicAuthTenantDetailsService;
    @Autowired
    private BusinessDateReadPlatformService businessDateReadPlatformService;
    @Autowired
    private MDCWrapper mdcWrapper;
    @Autowired
    private FineractRequestContextHolder fineractRequestContextHolder;
    @Autowired(required = false)
    private LoanCOBFilterHelper loanCOBFilterHelper;
    @Autowired(required = false)
    private WorkingCapitalLoanCOBFilterHelper workingCapitalLoanCOBFilterHelper;
    @Autowired
    private IdempotencyStoreHelper idempotencyStoreHelper;
    @Autowired
    private ProgressiveLoanModelCheckerFilter progressiveLoanModelCheckerFilter;
    @Autowired
    private WorkingCapitalLoanModelCheckerFilter workingCapitalLoanModelCheckerFilter;
    @Autowired
    private PlatformUserDetailsChecker platformUserDetailsChecker;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.securityMatcher(API_MATCHER.matcher("/api/**")).authorizeHttpRequests(auth -> {

            List<AuthorizationManager<RequestAuthorizationContext>> authorizationManagers = new ArrayList<>();
            authorizationManagers.add(fullyAuthenticated());

            if (fineractProperties.getSecurity().getTwoFactor().isEnabled()) {
                authorizationManagers.add(hasAuthority("TWOFACTOR_AUTHENTICATED"));
            }

            auth.requestMatchers(API_MATCHER.matcher(HttpMethod.OPTIONS, "/api/**")).permitAll()
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/authentication")).permitAll()
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/password/forgot")).permitAll()
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/instance-mode")).permitAll();

            ApiAuthorizationRules.configure(auth);

            auth.requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/twofactor/validate")).fullyAuthenticated()
                    .requestMatchers(API_MATCHER.matcher("/api/*/twofactor")).fullyAuthenticated()
                    .requestMatchers(API_MATCHER.matcher("/api/**")).access(allOfRequestManagers(authorizationManagers));
        }).httpBasic(hb -> hb.authenticationEntryPoint(databaseUnavailableAwareEntryPoint())).csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tenantAwareBasicAuthenticationFilter(), SecurityContextHolderFilter.class)
                .addFilterAfter(requestResponseFilter(), ExceptionTranslationFilter.class)
                .addFilterAfter(correlationHeaderFilter(), RequestResponseFilter.class)
                .addFilterAfter(fineractInstanceModeApiFilter(), CorrelationHeaderFilter.class);

        Class<? extends Filter> lastCobFilter;
        if (loanCOBFilterHelper != null) {
            http.addFilterAfter(loanCOBApiFilter(), FineractInstanceModeApiFilter.class);
            http.addFilterBefore(progressiveLoanModelCheckerFilter, LoanCOBApiFilter.class);
            lastCobFilter = LoanCOBApiFilter.class;
        } else {
            http.addFilterAfter(progressiveLoanModelCheckerFilter, FineractInstanceModeApiFilter.class);
            lastCobFilter = ProgressiveLoanModelCheckerFilter.class;
        }
        if (workingCapitalLoanCOBFilterHelper != null) {
            http.addFilterAfter(workingCapitalLoanCOBApiFilter(), lastCobFilter);
            http.addFilterBefore(workingCapitalLoanModelCheckerFilter, WorkingCapitalLoanCOBApiFilter.class);
            lastCobFilter = WorkingCapitalLoanCOBApiFilter.class;
        } else {
            http.addFilterAfter(workingCapitalLoanModelCheckerFilter, lastCobFilter);
            lastCobFilter = WorkingCapitalLoanModelCheckerFilter.class;
        }
        http.addFilterAfter(idempotencyStoreFilter(), lastCobFilter);
        if (fineractProperties.getIpTracking().isEnabled()) {
            http.addFilterAfter(callerIpTrackingFilter(), RequestResponseFilter.class);
        }
        if (fineractProperties.getSecurity().getTwoFactor().isEnabled()) {
            http.addFilterAfter(twoFactorAuthenticationFilter(), CorrelationHeaderFilter.class);
        }

        if (serverProperties.getSsl().isEnabled()) {
            http.redirectToHttps(redirect -> redirect.requestMatchers(API_MATCHER.matcher("/api/**")));
        }

        if (fineractProperties.getSecurity().getHsts().isEnabled()) {
            http.redirectToHttps(Customizer.withDefaults()).headers(
                    headers -> headers.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)));
        }

        if (fineractProperties.getSecurity().getCors().isEnabled()) {
            http.cors(Customizer.withDefaults());
        }

        return http.build();
    }

    public RequestResponseFilter requestResponseFilter() {
        return new RequestResponseFilter();
    }

    public LoanCOBApiFilter loanCOBApiFilter() {
        return new LoanCOBApiFilter(loanCOBFilterHelper);
    }

    public WorkingCapitalLoanCOBApiFilter workingCapitalLoanCOBApiFilter() {
        return new WorkingCapitalLoanCOBApiFilter(workingCapitalLoanCOBFilterHelper);
    }

    public TwoFactorAuthenticationFilter twoFactorAuthenticationFilter() {
        TwoFactorService twoFactorService = applicationContext.getBean(TwoFactorService.class);
        return new TwoFactorAuthenticationFilter(twoFactorService);
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

    public CallerIpTrackingFilter callerIpTrackingFilter() {
        return new CallerIpTrackingFilter(fineractProperties);
    }

    public TenantAwareBasicAuthenticationFilter tenantAwareBasicAuthenticationFilter() throws Exception {
        TenantAwareBasicAuthenticationFilter filter = new TenantAwareBasicAuthenticationFilter(authenticationManagerBean(),
                databaseUnavailableAwareEntryPoint(), toApiJsonSerializer, configurationDomainService, cacheWritePlatformService,
                userNotificationService, basicAuthTenantDetailsService, businessDateReadPlatformService);

        filter.setRequestMatcher(API_MATCHER.matcher("/api/**"));
        return filter;
    }

    @Bean
    public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint() {
        BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
        basicAuthenticationEntryPoint.setRealmName("Fineract Platform API");
        return basicAuthenticationEntryPoint;
    }

    private DatabaseUnavailableAwareAuthenticationEntryPoint databaseUnavailableAwareEntryPoint() {
        return new DatabaseUnavailableAwareAuthenticationEntryPoint(basicAuthenticationEntryPoint());
    }

    @Bean(name = "customAuthenticationProvider")
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new TemporaryPasswordAwareAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setPostAuthenticationChecks(platformUserDetailsChecker);
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private AuthorizationManager<RequestAuthorizationContext> allOfRequestManagers(
            List<AuthorizationManager<RequestAuthorizationContext>> authorizationManagers) {
        return (authentication, context) -> {
            for (AuthorizationManager<RequestAuthorizationContext> authorizationManager : authorizationManagers) {
                AuthorizationResult result = authorizationManager.authorize(authentication, context);
                if (result != null && !result.isGranted()) {
                    return new AuthorizationDecision(false);
                }
            }
            return new AuthorizationDecision(true);
        };
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        ProviderManager providerManager = new ProviderManager(authProvider());
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;
    }

}
