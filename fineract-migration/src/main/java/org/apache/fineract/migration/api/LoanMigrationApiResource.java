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
package org.apache.fineract.migration.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.migration.data.LoanMigrationRequestDto;
import org.apache.fineract.migration.data.LoanMigrationResponseDto;
import org.apache.fineract.migration.service.LoanMigrationReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loans")
@Component
@Tag(name = "Loan Migration", description = "APIs for managing loan migration processes.")
@RequiredArgsConstructor
public class LoanMigrationApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";
    private static final String MODIFY_RESOURCE_NAME_FOR_PERMISSIONS = "UPDATE LOAN";

    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final LoanMigrationReadPlatformService loanMigrationReadPlatformService;

    @POST
    @Path("/{loanId}/migration")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Initiate a migration process for a loan", description = "Allows users to initiate a migration process for a specific loan.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Command successfully processed", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public CommandProcessingResult initiateMigration(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @RequestBody(required = true) final LoanMigrationRequestDto request) {

        this.context.authenticatedUser().validateHasReadPermission(MODIFY_RESOURCE_NAME_FOR_PERMISSIONS);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().initiateLoanMigration(loanId).withJson(request.toJson()).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("/external-id/{loanExternalId}/migration")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Initiate a migration process for a loan using external ID", description = "Allows users to initiate a migration process for a specific loan using the external loan ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Command successfully processed", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public CommandProcessingResult initiateMigrationByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @RequestBody(required = true) final LoanMigrationRequestDto request) {

        this.context.authenticatedUser().validateHasReadPermission(MODIFY_RESOURCE_NAME_FOR_PERMISSIONS);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().initiateLoanMigrationByExternalId(loanExternalId)
                .withJson(request.toJson()).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("/{loanId}/migration")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve migration status for a loan", description = "Fetches the current migration status for a specific loan.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Migration status", content = @Content(schema = @Schema(implementation = LoanMigrationResponseDto.class))) })
    public LoanMigrationResponseDto retrieveMigrationStatus(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return this.loanMigrationReadPlatformService.getMigrationStatus(loanId);
    }

    @GET
    @Path("/external-id/{loanExternalId}/migration")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve migration status for a loan using external ID", description = "Fetches the current migration status for a specific loan using the external loan ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Migration status", content = @Content(schema = @Schema(implementation = LoanMigrationResponseDto.class))) })
    public LoanMigrationResponseDto retrieveMigrationStatusByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return this.loanMigrationReadPlatformService.getMigrationStatus(loanExternalId);
    }
}
