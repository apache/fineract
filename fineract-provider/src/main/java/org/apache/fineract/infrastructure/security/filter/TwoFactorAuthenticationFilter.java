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
package org.apache.fineract.infrastructure.security.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.fineract.infrastructure.security.constants.TwoFactorConstants;
import org.apache.fineract.infrastructure.security.domain.TFAccessToken;
import org.apache.fineract.infrastructure.security.service.TwoFactorService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;


/**
 * This filter is responsible for handling two-factor authentication.
 * The filter is enabled when 'twofactor' environment profile is active, otherwise
 * {@link InsecureTwoFactorAuthenticationFilter} is used.
 *
 * This filter validates an access-token provided as a header 'Fineract-Platform-TFA-Token'.
 * If a valid token is provided, a 'TWOFACTOR_AUTHENTICATED' authority is added to the current
 * authentication.
 * If an invalid(non-existent or invalid) token is provided, 403 response is returned.
 *
 * An authenticated platform user with permission 'BYPASS_TWOFACTOR' will always be granted
 * 'TWOFACTOR_AUTHENTICATED' authority regardless of the value of the 'Fineract-Platform-TFA-Token'
 * header.
 */
@Service(value = "twoFactorAuthFilter")
@Profile("twofactor")
public class TwoFactorAuthenticationFilter extends GenericFilterBean {

    private final TwoFactorService twoFactorService;

    @Autowired
    public TwoFactorAuthenticationFilter(TwoFactorService twoFactorService) {
        this.twoFactorService = twoFactorService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = null;
        if(context != null) {
            authentication = context.getAuthentication();
        }

        // Process two-factor only when user is authenticated
        if(authentication != null && authentication.isAuthenticated()) {
            AppUser user = (AppUser) authentication.getPrincipal();

            if(user == null) {
                return;
            }

            if(!user.hasSpecificPermissionTo(TwoFactorConstants.BYPASS_TWO_FACTOR_PERMISSION)) {
                // User can't bypass two-factor auth, check two-factor access token
                String token = request.getHeader("Fineract-Platform-TFA-Token");
                if(token != null) {
                    TFAccessToken accessToken = twoFactorService.fetchAccessTokenForUser(user, token);
                    // Token is non-existent or invalid
                    if(accessToken == null || !accessToken.isValid()) {
                        response.addHeader("WWW-Authenticate",
                                "Basic realm=\"Fineract Platform API Two Factor\"");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                "Invalid two-factor access token provided");
                        return;
                    }
                } else {
                    // No token provided
                    chain.doFilter(req, res);
                    return;
                }
            }

            List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authentication.getAuthorities());
            updatedAuthorities.add(new SimpleGrantedAuthority("TWOFACTOR_AUTHENTICATED"));
            final Authentication updatedAuthentication = createUpdatedAuthentication(authentication,
                    updatedAuthorities);
            context.setAuthentication(updatedAuthentication);
        }

        chain.doFilter(req, res);
    }

    private Authentication createUpdatedAuthentication(final Authentication currentAuthentication,
                              final List<GrantedAuthority> updatedAuthorities) {

            final UsernamePasswordAuthenticationToken authentication = new
                    UsernamePasswordAuthenticationToken(currentAuthentication.getPrincipal(),
                    currentAuthentication.getCredentials(), updatedAuthorities);

            if(currentAuthentication instanceof OAuth2Authentication) {
                final OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) currentAuthentication;
                return new OAuth2Authentication(oAuth2Authentication.getOAuth2Request(), authentication);
            }

            return authentication;
    }
}