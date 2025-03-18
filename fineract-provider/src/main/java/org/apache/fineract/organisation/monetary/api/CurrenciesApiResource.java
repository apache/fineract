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
package org.apache.fineract.organisation.monetary.api;

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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.ApplicationCurrencyConfigurationData;
import org.apache.fineract.organisation.monetary.data.request.CurrencyRequest;
import org.apache.fineract.organisation.monetary.service.OrganisationCurrencyReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/currencies")
@Component
@Tag(name = "Currency", description = "Application related configuration around viewing/updating the currencies permitted for use within the MFI.")
@RequiredArgsConstructor
public class CurrenciesApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "CURRENCY";

    private final PlatformSecurityContext context;
    private final OrganisationCurrencyReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<ApplicationCurrencyConfigurationData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Currency Configuration", description = "Returns the list of currencies permitted for use AND the list of currencies not selected (but available for selection).\n"
            + "\n" + "Example Requests:\n" + "\n" + "currencies\n" + "\n" + "\n" + "currencies?fields=selectedCurrencyOptions")
    public ApplicationCurrencyConfigurationData retrieveCurrencies() {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return this.readPlatformService.retrieveCurrencyConfiguration();
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Currency Configuration", description = "Updates the list of currencies permitted for use.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CurrencyRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CurrenciesApiResourceSwagger.PutCurrenciesResponse.class))) })
    public CommandProcessingResult updateCurrencies(@Parameter(hidden = true) CurrencyRequest currencyRequest) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateCurrencies() //
                .withJson(toApiJsonSerializer.serialize(currencyRequest)) //
                .build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
