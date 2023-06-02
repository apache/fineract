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
package org.apache.fineract.infrastructure.event.external.api;

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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationData;
import org.apache.fineract.infrastructure.event.external.service.ExternalEventConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Path("/v1/externalevents/configuration")
@Component
@Tag(name = "External event configuration", description = "External event configuration enables user to enable/disable event posting to downstream message channel")
public class ExternalEventConfigurationApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("type", "enabled"));

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "EXTERNAL_EVENT_CONFIGURATION";

    private final PlatformSecurityContext context;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final DefaultToApiJsonSerializer<ExternalEventConfigurationData> jsonSerializer;
    private final ExternalEventConfigurationReadPlatformService readPlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all external event configurations", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all external event configurations", content = @Content(schema = @Schema(implementation = ExternalEventConfigurationApiResourceSwagger.GetExternalEventConfigurationsResponse.class))) })
    public String retrieveExternalEventConfiguration(@Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final ExternalEventConfigurationData configurationData = readPlatformService.findAllExternalEventConfigurations();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return jsonSerializer.serialize(settings, configurationData, RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Enable/Disable external events posting", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalEventConfigurationApiResourceSwagger.PutExternalEventConfigurationsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class))) })
    public String updateExternalEventConfigurationsDetails(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        context.authenticatedUser().validateHasUpdatePermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateExternalEventConfigurations() //
                .withJson(apiRequestBodyAsJson) //
                .build();
        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);
        return this.jsonSerializer.serialize(result);

    }
}
