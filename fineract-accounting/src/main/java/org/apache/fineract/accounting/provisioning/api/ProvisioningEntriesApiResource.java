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
package org.apache.fineract.accounting.provisioning.api;

import static org.apache.fineract.accounting.provisioning.constant.ProvisioningEntriesApiConstants.CREATE_JOURNAL_ENTRY;
import static org.apache.fineract.accounting.provisioning.constant.ProvisioningEntriesApiConstants.RECREATE_PROVISION_IN_ENTRY;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.data.request.ProvisionEntryRequest;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/provisioningentries")
@Component
@Tag(name = "Provisioning Entries", description = """
        This defines the Provisioning Entries for all active loan products

        Field Descriptions
        date
        Date on which day provisioning entries should be created
        createjournalentries
        Boolean variable whether to add journal entries for generated provisioning entries
        """)
@RequiredArgsConstructor
public class ProvisioningEntriesApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<ProvisioningEntryData> toApiJsonSerializer;
    private final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create new Provisioning Entries", description = """
            Creates a new Provisioning Entries

            Mandatory Fields
            date
            dateFormat
            locale
            Optional Fields
            createjournalentries""")
    @RequestBody(content = @Content(schema = @Schema(implementation = ProvisionEntryRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProvisioningEntriesApiResourceSwagger.PostProvisioningEntriesResponse.class)))
    public CommandProcessingResult createProvisioningEntries(@Parameter(hidden = true) ProvisionEntryRequest provisionEntryRequest) {
        platformSecurityContext.authenticatedUser();
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createProvisioningEntries()
                .withJson(toApiJsonSerializer.serialize(provisionEntryRequest)).build();
        return commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }

    @POST
    @Path("{entryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Recreates Provisioning Entry", description = "Recreates Provisioning Entry | createjournalentry.")
    @RequestBody(content = @Content(schema = @Schema(implementation = ProvisioningEntriesApiResourceSwagger.PutProvisioningEntriesRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProvisioningEntriesApiResourceSwagger.PutProvisioningEntriesResponse.class)))
    public CommandProcessingResult modifyProvisioningEntry(@PathParam("entryId") @Parameter(description = "entryId") final Long entryId,
            @QueryParam("command") @Parameter(description = "command=createjournalentry\ncommand=recreateprovisioningentry") final String commandParam,
            @Parameter(hidden = true) String provisionCommandRequest) {
        platformSecurityContext.authenticatedUser();
        return getResultByCommandParam(commandParam, entryId, provisionCommandRequest);
    }

    @GET
    @Path("{entryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieves a Provisioning Entry", description = "Returns the details of a generated Provisioning Entry.")
    public ProvisioningEntryData retrieveProvisioningEntry(@PathParam("entryId") @Parameter(description = "entryId") final Long entryId) {
        platformSecurityContext.authenticatedUser();
        return provisioningEntriesReadPlatformService.retrieveProvisioningEntryData(entryId);
    }

    @GET
    @Path("entries")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Page<LoanProductProvisioningEntryData> retrieveProviioningEntries(@QueryParam("entryId") final Long entryId,
            @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("officeId") final Long officeId, @QueryParam("productId") final Long productId,
            @QueryParam("categoryId") final Long categoryId) {
        platformSecurityContext.authenticatedUser();
        final SearchParameters params = SearchParameters.builder().limit(limit).offset(offset).provisioningEntryId(entryId)
                .officeId(officeId).productId(productId).categoryId(categoryId).build();

        return provisioningEntriesReadPlatformService.retrieveProvisioningEntries(params);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all Provisioning Entries")
    public Page<ProvisioningEntryData> retrieveAllProvisioningEntries(
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {
        platformSecurityContext.authenticatedUser();
        return provisioningEntriesReadPlatformService.retrieveAllProvisioningEntries(offset, limit);
    }

    private CommandProcessingResult getResultByCommandParam(String commandParam, Long entryId, String apiRequestBodyAsJson) {
        final CommandWrapperBuilder commandWrapperBuilder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        switch (commandParam) {
            case CREATE_JOURNAL_ENTRY -> {
                return commandsSourceWritePlatformService
                        .logCommandSource(commandWrapperBuilder.createProvisioningJournalEntries(entryId).build());
            }
            case RECREATE_PROVISION_IN_ENTRY -> {
                return commandsSourceWritePlatformService
                        .logCommandSource(commandWrapperBuilder.reCreateProvisioningEntries(entryId).build());
            }
            default -> throw new UnrecognizedQueryParamException("command", commandParam);
        }
    }
}
