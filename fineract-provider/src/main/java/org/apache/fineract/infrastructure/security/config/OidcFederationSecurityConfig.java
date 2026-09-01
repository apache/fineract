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
package org.apache.fineract.infrastructure.security.config;

import jakarta.servlet.Filter;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.filters.CallerIpTrackingFilter;
import org.apache.fineract.infrastructure.core.filters.CorrelationHeaderFilter;
import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreFilter;
import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreHelper;
import org.apache.fineract.infrastructure.core.filters.RequestResponseFilter;
import org.apache.fineract.infrastructure.core.service.MDCWrapper;
import org.apache.fineract.infrastructure.instancemode.filter.FineractInstanceModeApiFilter;
import org.apache.fineract.infrastructure.jobs.filter.LoanCOBApiFilter;
import org.apache.fineract.infrastructure.jobs.filter.LoanCOBFilterHelper;
import org.apache.fineract.infrastructure.jobs.filter.ProgressiveLoanModelCheckerFilter;
import org.apache.fineract.infrastructure.jobs.filter.WorkingCapitalLoanCOBApiFilter;
import org.apache.fineract.infrastructure.jobs.filter.WorkingCapitalLoanCOBFilterHelper;
import org.apache.fineract.infrastructure.security.converter.FineractOidcJwtAuthenticationConverter;
import org.apache.fineract.infrastructure.security.filter.BusinessDateFilter;
import org.apache.fineract.infrastructure.security.filter.OidcTenantAwareFilter;
import org.apache.fineract.infrastructure.security.handler.OidcAuthenticationSuccessHandler;
import org.apache.fineract.infrastructure.security.handler.OidcLogoutSuccessHandler;
import org.apache.fineract.infrastructure.security.service.AuthTenantDetailsService;
import org.apache.fineract.infrastructure.security.service.FineractOidcUserService;
import org.apache.fineract.infrastructure.security.service.TenantOidcConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

/**
 * Spring Security filter chain for OIDC Federation authentication.
 *
 * <p>
 * Active only when {@code fineract.security.oidc-federation.enabled=true}. Sits at {@code @Order(100)} — after the
 * Authorization Server chains ({@code @Order} 1–3) and before the Basic Auth chain (no {@code @Order}, defaults to
 * lowest priority).
 *
 * <p>
 * Provides two authentication modes for {@code /api/**}:
 * <ul>
 * <li><b>Bearer JWT</b> — external IdP access tokens validated via the configured JWK set URI.</li>
 * <li><b>OAuth2 login redirect</b> — browser-based flow (requires {@code spring.security.oauth2.client.registration.*}
 * properties).</li>
 * </ul>
 *
 * <p>
 * {@code @EnableMethodSecurity} is intentionally omitted. When Basic Auth is also enabled ({@code SecurityConfig} is
 * loaded), it already registers method security. When only OIDC is enabled, add {@code @EnableMethodSecurity} to a
 * separate configuration class.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty("fineract.security.oidc-federation.enabled")
@Order(100)
@EnableConfigurationProperties(FineractProperties.class)
public class OidcFederationSecurityConfig {

    @Autowired
    private FineractProperties fineractProperties;
    @Autowired
    private FineractOidcUserService oidcUserService;

    @Autowired
    private AuthTenantDetailsService tenantDetailsService;

    @Autowired
    private TenantOidcConfigService tenantOidcConfigService;

    @Autowired
    private DynamicJwtIssuerAuthenticationManagerResolver dynamicIssuerResolver;

    @Autowired
    private BusinessDateReadPlatformService businessDateReadPlatformService;

    @Autowired
    private MDCWrapper mdcWrapper;

    @Autowired
    private FineractRequestContextHolder fineractRequestContextHolder;

    @Autowired
    private IdempotencyStoreHelper idempotencyStoreHelper;

    @Autowired
    private ProgressiveLoanModelCheckerFilter progressiveLoanModelCheckerFilter;

    @Autowired(required = false)
    private LoanCOBFilterHelper loanCOBFilterHelper;

    @Autowired(required = false)
    private WorkingCapitalLoanCOBFilterHelper workingCapitalLoanCOBFilterHelper;

    // Optional: only needed for browser-based OAuth2 login redirect flow.
    // Not required for Bearer token API authentication.
    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    @Order(100)
    public SecurityFilterChain oidcFederationFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/*/echo").permitAll().anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Bearer JWT validation — multi-issuer dynamic resolver (DB + YAML fallback)
                .oauth2ResourceServer(resourceServer -> resourceServer.authenticationManagerResolver(dynamicIssuerResolver))
                // Tenant resolution before Spring validates the JWT
                .addFilterBefore(oidcTenantAwareFilter(), SecurityContextHolderFilter.class)
                .addFilterAfter(businessDateFilter(), OidcTenantAwareFilter.class)
                .addFilterAfter(requestResponseFilter(), ExceptionTranslationFilter.class)
                .addFilterAfter(correlationHeaderFilter(), RequestResponseFilter.class)
                .addFilterAfter(fineractInstanceModeApiFilter(), CorrelationHeaderFilter.class);

        Class<? extends Filter> lastCobFilter;
        if (loanCOBFilterHelper != null) {
            http.addFilterAfter(loanCOBApiFilter(), FineractInstanceModeApiFilter.class).addFilterBefore(progressiveLoanModelCheckerFilter,
                    LoanCOBApiFilter.class);
            lastCobFilter = LoanCOBApiFilter.class;
        } else {
            http.addFilterAfter(progressiveLoanModelCheckerFilter, FineractInstanceModeApiFilter.class);
            lastCobFilter = ProgressiveLoanModelCheckerFilter.class;
        }
        if (workingCapitalLoanCOBFilterHelper != null) {
            http.addFilterAfter(workingCapitalLoanCOBApiFilter(), lastCobFilter);
            lastCobFilter = WorkingCapitalLoanCOBApiFilter.class;
        }
        http.addFilterAfter(idempotencyStoreFilter(), lastCobFilter);

        if (fineractProperties.getIpTracking().isEnabled()) {
            http.addFilterAfter(callerIpTrackingFilter(), RequestResponseFilter.class);
        }

        // Optional: browser-based OAuth2 login redirect flow
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2 -> oauth2.clientRegistrationRepository(clientRegistrationRepository)
                    .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcSpringUserService()))
                    .successHandler(new OidcAuthenticationSuccessHandler()).failureUrl("/login?error=true"))
                    .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(oidcLogoutSuccessHandler())
                            .invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID"));
        }

        if (fineractProperties.getSecurity().getCors().isEnabled()) {
            http.cors(Customizer.withDefaults());
        }

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Managed beans — kept as @Bean for testability
    // -------------------------------------------------------------------------

    @Bean
    public FineractOidcJwtAuthenticationConverter oidcJwtConverter() {
        return new FineractOidcJwtAuthenticationConverter(oidcUserService);
    }

    @Bean
    public OidcTenantAwareFilter oidcTenantAwareFilter() {
        return new OidcTenantAwareFilter(new DefaultBearerTokenResolver(), tenantDetailsService, fineractProperties,
                tenantOidcConfigService);
    }

    // -------------------------------------------------------------------------
    // Plain factory methods — NOT @Bean — to avoid duplicate bean registration
    // when SecurityConfig or AuthorizationServerConfig is also active
    // -------------------------------------------------------------------------

    public BusinessDateFilter businessDateFilter() {
        return new BusinessDateFilter(businessDateReadPlatformService);
    }

    public RequestResponseFilter requestResponseFilter() {
        return new RequestResponseFilter();
    }

    public CorrelationHeaderFilter correlationHeaderFilter() {
        return new CorrelationHeaderFilter(fineractProperties, mdcWrapper);
    }

    public FineractInstanceModeApiFilter fineractInstanceModeApiFilter() {
        return new FineractInstanceModeApiFilter(fineractProperties);
    }

    public LoanCOBApiFilter loanCOBApiFilter() {
        return new LoanCOBApiFilter(loanCOBFilterHelper);
    }

    public WorkingCapitalLoanCOBApiFilter workingCapitalLoanCOBApiFilter() {
        return new WorkingCapitalLoanCOBApiFilter(workingCapitalLoanCOBFilterHelper);
    }

    public IdempotencyStoreFilter idempotencyStoreFilter() {
        return new IdempotencyStoreFilter(fineractRequestContextHolder, idempotencyStoreHelper, fineractProperties);
    }

    public CallerIpTrackingFilter callerIpTrackingFilter() {
        return new CallerIpTrackingFilter(fineractProperties);
    }

    public OidcLogoutSuccessHandler oidcLogoutSuccessHandler() {
        return new OidcLogoutSuccessHandler(fineractProperties);
    }

    /**
     * Wraps Spring's OidcUserService to process the OIDC user through Fineract's resolution logic during the browser
     * login redirect flow.
     */
    private org.springframework.security.oauth2.client.userinfo.OAuth2UserService<org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest, org.springframework.security.oauth2.core.oidc.user.OidcUser> oidcSpringUserService() {

        var delegate = new org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService();
        return userRequest -> {
            var oidcUser = delegate.loadUser(userRequest);
            // Tenant ID is not available here; OidcTenantAwareFilter sets it from the token
            String tenantId = fineractProperties.getSecurity().getOidcFederation().getTenantClaimName();
            String resolvedTenant = oidcUser.getClaimAsString(tenantId) != null ? oidcUser.getClaimAsString(tenantId) : "default";
            return oidcUserService.processOidcUser(oidcUser, resolvedTenant);
        };
    }
}
