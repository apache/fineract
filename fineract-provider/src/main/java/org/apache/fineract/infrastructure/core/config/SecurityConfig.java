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
import org.apache.fineract.infrastructure.security.data.PlatformRequestLog;
import org.apache.fineract.infrastructure.security.filter.TenantAwareBasicAuthenticationFilter;
import org.apache.fineract.infrastructure.security.filter.TwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.service.AuthTenantDetailsService;
import org.apache.fineract.infrastructure.security.service.PlatformUserDetailsChecker;
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
import org.springframework.security.authorization.AuthorizationManager;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@ConditionalOnProperty("fineract.security.basicauth.enabled")
@EnableMethodSecurity
public class SecurityConfig {

    private static final PathPatternRequestMatcher.Builder API_MATCHER = PathPatternRequestMatcher.withDefaults();
    private static final String ALL_FUNCTIONS = "ALL_FUNCTIONS";
    private static final String ALL_FUNCTIONS_READ = "ALL_FUNCTIONS_READ";
    private static final String ALL_FUNCTIONS_WRITE = "ALL_FUNCTIONS_WRITE";

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
    @Autowired
    private IdempotencyStoreHelper idempotencyStoreHelper;
    @Autowired
    private ProgressiveLoanModelCheckerFilter progressiveLoanModelCheckerFilter;
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
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/echo")).permitAll()
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/authentication")).permitAll()
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/instance-mode")).permitAll()
                    // businessdate
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/businessdate/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_BUSINESS_DATE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/businessdate"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_BUSINESS_DATE")
                    // external
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/externalevents/configuration"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_EXTERNAL_EVENT_CONFIGURATION")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/externalevents/configuration"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_EXTERNAL_EVENT_CONFIGURATION")
                    // cache
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/caches"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CACHE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/caches"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CACHE")
                    // currency
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/currencies"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CURRENCY")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/currencies"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CURRENCY")
                    // notes: read
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/clients/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CLIENTNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/loans/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOANNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/loanTransactions/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_LOANTRANSACTIONNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/savings/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_SAVINGNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/groups/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_GROUPNOTE")
                    // notes: create
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/clients/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CLIENTNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/loans/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_LOANNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/loanTransactions/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_LOANTRANSACTIONNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/savings/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_SAVINGNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/groups/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_GROUPNOTE")
                    // notes: update
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/clients/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CLIENTNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/loans/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_LOANNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/loanTransactions/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_LOANTRANSACTIONNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/savings/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_SAVINGNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/groups/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_GROUPNOTE")
                    // notes: delete
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/clients/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CLIENTNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/loans/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_LOANNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/loanTransactions/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_LOANTRANSACTIONNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/savings/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_SAVINGNOTE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/groups/*/notes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_GROUPNOTE")
                    // document: clients
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/clients/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/clients/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/clients/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/clients/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                    // document: client_identifiers
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/client_identifiers/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/client_identifiers/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/client_identifiers/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/client_identifiers/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                    // document: staff
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/staff/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/staff/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/staff/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/staff/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                    // document: loans
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/loans/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/loans/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/loans/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/loans/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                    // document: savings
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/savings/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/savings/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/savings/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/savings/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                    // document: groups
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/groups/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/groups/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/groups/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/groups/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                    // document: import
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/import/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/import/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/import/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/import/*/documents"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_DOCUMENT")
                    // image: clients
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/clients/*/images"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/clients/*/images"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CLIENTIMAGE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/clients/*/images"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CLIENTIMAGE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/clients/*/images"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CLIENTIMAGE")
                    // image: staff
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/staff/*/images"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_DOCUMENT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/staff/*/images"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_STAFFIMAGE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/staff/*/images"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_STAFFIMAGE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/staff/*/images"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_STAFFIMAGE")
                    // bulk import
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/import"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_IMPORT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/import/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_IMPORT")
                    // payment type
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/paymenttypes/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_PAYMENTTYPE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/paymenttypes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_PAYMENTTYPE")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/paymenttypes"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_PAYMENTTYPE")
                    // mix: taxonomy
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/mixtaxonomy/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_MIX_TAXONOMY")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/mixtaxonomy/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_MIX_TAXONOMY")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/mixtaxonomy/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_MIX_TAXONOMY")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/mixtaxonomy/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_MIX_TAXONOMY")
                    // mix: mapping
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/mixmapping/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_MIX_MAPPING")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/mixmapping/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_MIX_MAPPING")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/mixmapping/*"))
                    // TODO: "UPDATE_XBRLMAPPING" is the legacy permission name; we should rename for consistency
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_MIX_MAPPING", "UPDATE_XBRLMAPPING")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/mixmapping/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_MIX_MAPPING")
                    // mix: report
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/mixreport/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_MIX_REPORT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/mixreport/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_MIX_REPORT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/mixreport/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_MIX_REPORT")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/mixreport/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_MIX_REPORT")
                    // working days
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/workingdays"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_WORKING_DAYS")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/workingdays/template"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_WORKING_DAYS")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/workingdays"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_WORKING_DAYS")
                    // interest rate chart slabs (before charts for specificity)
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/interestratecharts/*/chartslabs"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CHARTSLAB")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/interestratecharts/*/chartslabs/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_CHARTSLAB")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/interestratecharts/*/chartslabs"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_CHARTSLAB")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/interestratecharts/*/chartslabs/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_CHARTSLAB")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/interestratecharts/*/chartslabs/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_CHARTSLAB")
                    // interest rate chart
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/interestratecharts/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_INTERESTRATECHART")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/interestratecharts"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_INTERESTRATECHART")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/interestratecharts/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_INTERESTRATECHART")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/interestratecharts/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_INTERESTRATECHART")
                    // staff
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/staff/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_STAFF")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/staff/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_STAFF")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/staff/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_STAFF")
                    // meeting
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.GET, "/api/*/*/*/meeting/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_READ, "READ_MEETING")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/*/*/meeting/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "CREATE_MEETING")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.PUT, "/api/*/*/*/meeting/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "UPDATE_MEETING")
                    .requestMatchers(API_MATCHER.matcher(HttpMethod.DELETE, "/api/*/*/*/meeting/*"))
                    .hasAnyAuthority(ALL_FUNCTIONS, ALL_FUNCTIONS_WRITE, "DELETE_MEETING")

                    .requestMatchers(API_MATCHER.matcher(HttpMethod.POST, "/api/*/twofactor/validate")).fullyAuthenticated()
                    .requestMatchers(API_MATCHER.matcher("/api/*/twofactor")).fullyAuthenticated()
                    .requestMatchers(API_MATCHER.matcher("/api/**"))
                    .access(allOf(authorizationManagers.toArray(new AuthorizationManager[0])));
        }).httpBasic(hb -> hb.authenticationEntryPoint(basicAuthenticationEntryPoint())).csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tenantAwareBasicAuthenticationFilter(), SecurityContextHolderFilter.class)
                .addFilterAfter(requestResponseFilter(), ExceptionTranslationFilter.class)
                .addFilterAfter(correlationHeaderFilter(), RequestResponseFilter.class)
                .addFilterAfter(fineractInstanceModeApiFilter(), CorrelationHeaderFilter.class);

        if (loanCOBFilterHelper != null) {
            http.addFilterAfter(loanCOBApiFilter(), FineractInstanceModeApiFilter.class).addFilterAfter(idempotencyStoreFilter(),
                    LoanCOBApiFilter.class);
            http.addFilterBefore(progressiveLoanModelCheckerFilter, LoanCOBApiFilter.class);
        } else {
            http.addFilterAfter(idempotencyStoreFilter(), FineractInstanceModeApiFilter.class);
            http.addFilterAfter(progressiveLoanModelCheckerFilter, FineractInstanceModeApiFilter.class);
        }
        if (fineractProperties.getIpTracking().isEnabled()) {
            http.addFilterAfter(callerIpTrackingFilter(), RequestResponseFilter.class);
        }
        if (fineractProperties.getSecurity().getTwoFactor().isEnabled()) {
            http.addFilterAfter(twoFactorAuthenticationFilter(), CorrelationHeaderFilter.class);
        }

        if (serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.requestMatchers(API_MATCHER.matcher("/api/**")).requiresSecure());
        }

        if (fineractProperties.getSecurity().getHsts().isEnabled()) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure()).headers(
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
                basicAuthenticationEntryPoint(), toApiJsonSerializer, configurationDomainService, cacheWritePlatformService,
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

    @Bean(name = "customAuthenticationProvider")
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setPostAuthenticationChecks(platformUserDetailsChecker);
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
        CorsConfiguration config = new CorsConfiguration();
        FineractProperties.CorsProperties corsConfiguration = fineractProperties.getSecurity().getCors();
        config.setAllowedOriginPatterns(corsConfiguration.getAllowedOriginPatterns());
        config.setAllowedMethods(corsConfiguration.getAllowedMethods());
        config.setAllowedHeaders(corsConfiguration.getAllowedHeaders());
        config.setExposedHeaders(corsConfiguration.getExposedHeaders());
        config.setAllowCredentials(corsConfiguration.isAllowCredentials()); // if you use cookies / Authorization header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
