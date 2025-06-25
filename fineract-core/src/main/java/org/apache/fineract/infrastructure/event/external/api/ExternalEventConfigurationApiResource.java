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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.external.command.ExternalConfigurationsUpdateCommand;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationResponse;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationUpdateRequest;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationUpdateResponse;
import org.apache.fineract.infrastructure.event.external.service.ExternalEventConfigurationReadPlatformService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Path("/v1/externalevents/configuration")
@Component
@Tag(name = "External event configuration", description = "External event configuration enables user to enable/disable event posting to downstream message channel")
public class ExternalEventConfigurationApiResource {

    private final ExternalEventConfigurationReadPlatformService readPlatformService;
    private final CommandPipeline commandPipeline;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all external event configurations", description = "")
    public ExternalEventConfigurationResponse getExternalEventConfigurations() {
        return readPlatformService.findAllExternalEventConfigurations();
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Enable/Disable external events posting", description = "")
    public ExternalEventConfigurationUpdateResponse updateExternalEventConfigurations(@HeaderParam("Idempotency-Key") String idempotencyKey,
            @Valid ExternalEventConfigurationUpdateRequest request) {
        final var command = new ExternalConfigurationsUpdateCommand();

        command.setId(UUID.randomUUID());
        command.setIdempotencyKey(idempotencyKey);
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setPayload(request);

        final Supplier<ExternalEventConfigurationUpdateResponse> response = commandPipeline.send(command);

        return response.get();
    }
}
