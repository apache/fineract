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
package org.apache.fineract.portfolio.interestpauses.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.interestpauses.data.InterestPauseRequestDto;
import org.apache.fineract.portfolio.interestpauses.data.InterestPauseResponseDto;
import org.apache.fineract.portfolio.interestpauses.service.InterestPauseReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loans")
@Component
@Tag(name = "Loan Interest Pause", description = "APIs for managing interest pause periods on loans.")
@RequiredArgsConstructor
public class LoanInterestPauseApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";

    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final InterestPauseReadPlatformService interestPauseReadPlatformService;

    @POST
    @Path("/{loanId}/interest-pauses")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new interest pause period for a loan", description = "Allows users to define a period during which no interest will be accrued for a specific loan.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public CommandProcessingResult createInterestPause(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @RequestBody(required = true) final InterestPauseRequestDto request) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInterestPause(loanId).withJson(request.toJson()).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("/external-id/{loanExternalId}/interest-pauses")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new interest pause for a loan using external ID", description = "Allows users to define a period during which no interest will be accrued for a specific loan using the external loan ID.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public CommandProcessingResult createInterestPauseByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @RequestBody(required = true) final InterestPauseRequestDto request) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInterestPauseByExternalId(loanExternalId)
                .withJson(request.toJson()).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("/{loanId}/interest-pauses")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve all interest pause periods for a loan", description = "Fetches a list of all active interest pause periods for a specific loan.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public List<InterestPauseResponseDto> retrieveInterestPauses(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return this.interestPauseReadPlatformService.retrieveInterestPauses(loanId);
    }

    @GET
    @Path("/external-id/{loanExternalId}/interest-pauses")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve all interest pause periods for a loan using external ID", description = "Fetches a list of all active interest pause periods for a specific loan using the external loan ID.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public List<InterestPauseResponseDto> retrieveInterestPausesByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return this.interestPauseReadPlatformService.retrieveInterestPauses(loanExternalId);
    }
}
