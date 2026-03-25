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

import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.teller.data.CashierSessionData;
import org.apache.fineract.organisation.teller.data.CashierSessionSummaryData;
import org.apache.fineract.organisation.teller.service.CashierSessionReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/tellers")
@Component
@Tag(name = "Cashier Session Management", description = "Endpoints for managing cashier session lifecycle.")
@RequiredArgsConstructor
public class CashierSessionApiResource {

    private static final String RESOURCE_NAME = "CASHIERSESSION";

    private final CashierSessionReadPlatformService readService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final PlatformSecurityContext context;

    /**
     * POST /tellers/{id}/cashiers/{cId}/sessions — Open a new cashier session.
     */
    @POST
    @Path("{tellerId}/cashiers/{cashierId}/sessions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Open a cashier session", description = "Opens a new cashier session for the given cashier on the given teller.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public CommandProcessingResult openSession(
            @PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId,
            @QueryParam("currencyCode") @Parameter(description = "currencyCode") final String currencyCode) {
        context.authenticatedUser().validateHasCreatePermission(RESOURCE_NAME);
        final JsonObject json = new JsonObject();
        json.addProperty("currencyCode", currencyCode != null ? currencyCode : "");
        final CommandWrapper request = new CommandWrapperBuilder()
                .openCashierSession(tellerId, cashierId)
                .withJson(json.toString())
                .build();
        return commandWritePlatformService.logCommandSource(request);
    }

    /**
     * GET /tellers/{id}/cashiers/{cId}/sessions/active — Get the currently open session.
     */
    @GET
    @Path("{tellerId}/cashiers/{cashierId}/sessions/active")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get active cashier session", description = "Returns the currently open session for the given cashier on the given teller.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public Optional<CashierSessionData> getActiveSession(
            @PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        return readService.findActiveSession(cashierId, tellerId);
    }

    /**
     * GET /tellers/{id}/cashiers/{cId}/sessions — List all sessions (paginated).
     */
    @GET
    @Path("{tellerId}/cashiers/{cashierId}/sessions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List cashier sessions", description = "Returns all sessions for the given cashier on the given teller.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public List<CashierSessionData> listSessions(
            @PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        return readService.findAllSessions(cashierId, tellerId);
    }

    /**
     * POST /tellers/{id}/cashiers/{cId}/sessions/{sId}/close — Close session.
     */
    @POST
    @Path("{tellerId}/cashiers/{cashierId}/sessions/{sessionId}/close")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Close a cashier session", description = "Closes the specified cashier session.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public CommandProcessingResult closeSession(
            @PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId,
            @PathParam("sessionId") @Parameter(description = "sessionId") final Long sessionId) {
        context.authenticatedUser().validateHasPermissionTo("CLOSE_CASHIERSESSION");
        final CommandWrapper request = new CommandWrapperBuilder()
                .closeCashierSession(tellerId, cashierId, sessionId)
                .withJson("{}")
                .build();
        return commandWritePlatformService.logCommandSource(request);
    }

    /**
     * GET /tellers/{id}/cashiers/{cId}/sessions/{sId}/summary — Full session summary with GL reconciliation.
     */
    @GET
    @Path("{tellerId}/cashiers/{cashierId}/sessions/{sessionId}/summary")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get session summary", description = "Returns a full session summary with GL reconciliation for the given session.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public CashierSessionSummaryData getSessionSummary(
            @PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId,
            @PathParam("sessionId") @Parameter(description = "sessionId") final Long sessionId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        return readService.getSessionSummary(sessionId);
    }

    /**
     * GET /tellers/branch/{officeId}/dashboard — Supervisor view: all open sessions and positions.
     */
    @GET
    @Path("branch/{officeId}/dashboard")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get branch dashboard", description = "Returns all open cashier sessions for the given office (supervisor view).")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public List<CashierSessionData> getBranchDashboard(
            @PathParam("officeId") @Parameter(description = "officeId") final Long officeId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        return readService.findOpenSessionsByOffice(officeId);
    
    }
}
