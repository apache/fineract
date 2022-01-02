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
package org.apache.fineract.infrastructure.security.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.constants.TwoFactorConstants;
import org.apache.fineract.infrastructure.security.data.AuthenticatedOauthUserData;
import org.apache.fineract.infrastructure.security.data.FineractJwtAuthenticationToken;
import org.apache.fineract.infrastructure.security.service.SpringSecurityPlatformSecurityContext;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*
 * Implementation of Oauth2 authentication APIs, loaded only when "oauth" profile is enabled.
 */
@Path("/userdetails")
@Component
@ConditionalOnProperty("fineract.security.oauth.enabled")
@Scope("singleton")
@Tag(name = "Fetch authenticated user details", description = "")
public class UserDetailsApiResource {

    private final ToApiJsonSerializer<AuthenticatedOauthUserData> apiJsonSerializerService;
    private final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext;

    @Value("${fineract.security.2fa.enabled}")
    private boolean twoFactorEnabled;

    @Autowired
    public UserDetailsApiResource(final ToApiJsonSerializer<AuthenticatedOauthUserData> apiJsonSerializerService,
            final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext) {
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.springSecurityPlatformSecurityContext = springSecurityPlatformSecurityContext;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Fetch authenticated user details\n", description = "checks the Authentication and returns the set roles and permissions allowed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UserDetailsApiResourceSwagger.GetUserDetailsResponse.class))) })
    public String fetchAuthenticatedUserData() {

        final SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }

        final FineractJwtAuthenticationToken authentication = (FineractJwtAuthenticationToken) context.getAuthentication();
        if (authentication == null) {
            return null;
        }

        final AppUser principal = (AppUser) authentication.getPrincipal();
        if (principal == null) {
            return null;
        }

        final Collection<String> permissions = new ArrayList<>();
        AuthenticatedOauthUserData authenticatedUserData = new AuthenticatedOauthUserData(principal.getUsername(), permissions);

        final Collection<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
        for (final GrantedAuthority grantedAuthority : authorities) {
            permissions.add(grantedAuthority.getAuthority());
        }

        final Collection<RoleData> roles = new ArrayList<>();
        final Set<Role> userRoles = principal.getRoles();
        for (final Role role : userRoles) {
            roles.add(role.toData());
        }

        final Long officeId = principal.getOffice().getId();
        final String officeName = principal.getOffice().getName();

        final Long staffId = principal.getStaffId();
        final String staffDisplayName = principal.getStaffDisplayName();

        final EnumOptionData organisationalRole = principal.organisationalRoleData();

        boolean isTwoFactorRequired = this.twoFactorEnabled
                && !principal.hasSpecificPermissionTo(TwoFactorConstants.BYPASS_TWO_FACTOR_PERMISSION);
        if (this.springSecurityPlatformSecurityContext.doesPasswordHasToBeRenewed(principal)) {
            authenticatedUserData = new AuthenticatedOauthUserData(principal.getUsername(), principal.getId(),
                    authentication.getToken().getTokenValue(), isTwoFactorRequired);
        } else {
            authenticatedUserData = new AuthenticatedOauthUserData(principal.getUsername(), officeId, officeName, staffId, staffDisplayName,
                    organisationalRole, roles, permissions, principal.getId(), authentication.getToken().getTokenValue(),
                    isTwoFactorRequired);
        }

        return this.apiJsonSerializerService.serialize(authenticatedUserData);
    }
}
