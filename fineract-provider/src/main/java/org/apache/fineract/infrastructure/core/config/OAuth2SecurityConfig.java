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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.core.exceptionmapper.OAuth2ExceptionEntryPoint;
import org.apache.fineract.infrastructure.security.data.FineractJwtAuthenticationToken;
import org.apache.fineract.infrastructure.security.filter.TenantAwareTenantIdentifierFilter;
import org.apache.fineract.infrastructure.security.filter.TwoFactorAuthenticationFilter;
import org.apache.fineract.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import org.apache.fineract.infrastructure.security.vote.SelfServiceUserAccessVote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
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
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@ConditionalOnProperty("fineract.security.oauth.enabled")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private TwoFactorAuthenticationFilter twoFactorAuthenticationFilter;

    @Autowired
    private TenantAwareTenantIdentifierFilter tenantAwareTenantIdentifierFilter;

    @Autowired
    private TenantAwareJpaPlatformUserDetailsService userDetailsService;

    @Autowired
    private ServerProperties serverProperties;

    private static final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http //
                .csrf().disable() // NOSONAR only creating a service that is used by non-browser clients
                .antMatcher("/api/**").authorizeRequests() //
                .antMatchers(HttpMethod.OPTIONS, "/api/**").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/echo").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/authentication").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/self/authentication").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/self/registration").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/self/registration/user").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/twofactor/validate").fullyAuthenticated() //
                .antMatchers("/api/*/twofactor").fullyAuthenticated() //
                .antMatchers("/api/**").access("isFullyAuthenticated() and hasAuthority('TWOFACTOR_AUTHENTICATED')") //
                .accessDecisionManager(accessDecisionManager()).and() //
                .exceptionHandling().authenticationEntryPoint(new OAuth2ExceptionEntryPoint()).and()
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter()))
                        .authenticationEntryPoint(new OAuth2ExceptionEntryPoint())) //
                .sessionManagement() //
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //
                .and() //
                .addFilterAfter(tenantAwareTenantIdentifierFilter, SecurityContextPersistenceFilter.class) //
                .addFilterAfter(twoFactorAuthenticationFilter, BasicAuthenticationFilter.class); //

        if (serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.antMatchers("/api/**").requiresSecure());
        }
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

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<? extends Object>> decisionVoters = Arrays.asList(new RoleVoter(), new AuthenticatedVoter(),
                new WebExpressionVoter(), new SelfServiceUserAccessVote());

        return new UnanimousBased(decisionVoters);
    }

    @Bean
    public FilterRegistrationBean<TenantAwareTenantIdentifierFilter> tenantAwareTenantIdentifierFilterRegistration() throws Exception {
        FilterRegistrationBean<TenantAwareTenantIdentifierFilter> registration = new FilterRegistrationBean<TenantAwareTenantIdentifierFilter>(
                tenantAwareTenantIdentifierFilter);
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
