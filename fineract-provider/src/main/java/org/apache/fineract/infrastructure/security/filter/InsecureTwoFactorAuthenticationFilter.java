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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.security.data.FineractJwtAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A dummy {@link TwoFactorAuthenticationFilter} filter used when 'twofactor' environment profile is not active.
 *
 * This filter adds 'TWOFACTOR_AUTHENTICATED' authority to every authenticated platform user.
 */
public class InsecureTwoFactorAuthenticationFilter extends TwoFactorAuthenticationFilter {

    public InsecureTwoFactorAuthenticationFilter() {
        super(null);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = null;
        if (context != null) {
            authentication = context.getAuthentication();
        }

        // Add two-factor authenticated authority if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authentication.getAuthorities());
            updatedAuthorities.add(new SimpleGrantedAuthority("TWOFACTOR_AUTHENTICATED"));

            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken updatedAuthentication = new UsernamePasswordAuthenticationToken(
                        authentication.getPrincipal(), authentication.getCredentials(), updatedAuthorities);
                context.setAuthentication(updatedAuthentication);
            } else if (authentication instanceof FineractJwtAuthenticationToken) {
                FineractJwtAuthenticationToken fineractJwtAuthenticationToken = (FineractJwtAuthenticationToken) authentication;
                FineractJwtAuthenticationToken updatedAuthentication = new FineractJwtAuthenticationToken(
                        fineractJwtAuthenticationToken.getToken(), (Collection<GrantedAuthority>) updatedAuthorities,
                        (UserDetails) authentication.getPrincipal());
                context.setAuthentication(updatedAuthentication);
            }
        }

        chain.doFilter(req, res);
    }
}
