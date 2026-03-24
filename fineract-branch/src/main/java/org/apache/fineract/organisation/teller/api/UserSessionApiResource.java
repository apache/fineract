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
package org.apache.fineract.organisation.teller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.teller.data.CashierSessionData;
import org.apache.fineract.organisation.teller.service.CashierSessionReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Component;

@Path("/v1/users")
@Component
@Tag(name = "User Session Management", description = "Endpoint for resolving the active cashier session for a user.")
@RequiredArgsConstructor
public class UserSessionApiResource {

    private final CashierSessionReadPlatformService readService;
    private final PlatformSecurityContext context;

    /**
     * GET /users/{id}/session/active — Resolve active session for logged-in user.
     */
    @GET
    @Path("{userId}/session/active")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get active session for user", description = "Returns the currently open cashier session for the given user.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public Optional<CashierSessionData> getActiveSessionForUser(
            @PathParam("userId") @Parameter(description = "userId") final Long userId) {
        final AppUser currentUser = context.authenticatedUser();
        final Long officeId = currentUser.getOffice().getId();
        return readService.findActiveSessionForUser(userId, officeId);
    }
}
