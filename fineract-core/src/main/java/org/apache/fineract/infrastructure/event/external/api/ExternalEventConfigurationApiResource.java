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
import org.apache.fineract.infrastructure.event.external.command.ExternalEventConfigurationCommand;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationData;
import org.apache.fineract.infrastructure.event.external.service.ExternalEventConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Path("/v1/externalevents/configuration")
@Component
@Tag(name = "External event configuration", description = "External event configuration enables user to enable/disable event posting to downstream message channel")
public class ExternalEventConfigurationApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "EXTERNAL_EVENT_CONFIGURATION";

    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final DefaultToApiJsonSerializer<ExternalEventConfigurationData> jsonSerializer;
    private final ExternalEventConfigurationReadPlatformService readPlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all external event configurations", description = "")
    public ExternalEventConfigurationData retrieveExternalEventConfiguration() {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return readPlatformService.findAllExternalEventConfigurations();
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Enable/Disable external events posting", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalEventConfigurationCommand.class)))
    public CommandProcessingResult updateExternalEventConfigurationsDetails(
            @Parameter(hidden = true) ExternalEventConfigurationCommand command) {
        context.authenticatedUser().validateHasUpdatePermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateExternalEventConfigurations()
                .withJson(jsonSerializer.serialize(command)).build();
        return commandWritePlatformService.logCommandSource(commandRequest);
    }
}
