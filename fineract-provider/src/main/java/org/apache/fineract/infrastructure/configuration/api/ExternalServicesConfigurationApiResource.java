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
package org.apache.fineract.infrastructure.configuration.api;

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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.configuration.data.ExternalServicesPropertiesData;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/externalservice")
@Component
@Tag(name = "External Services", description = "External Services Configuration related to set of supported configurations for third party services like Amazon S3 and SMTP:\n"
        + "\n" + "S3 (Amazon S3):\n" + "s3_access_key -\n" + "s3_bucket_name -\n" + "s3_secret_key -\n" + "\n" + "\n"
        + "SMTP (Email Service):\n" + "username -\n" + "password -\n" + "host -\n" + "port -\n" + "useTLS -")
@RequiredArgsConstructor
public class ExternalServicesConfigurationApiResource {

    private final PlatformSecurityContext context;
    private final ExternalServicesPropertiesReadPlatformService externalServicePropertiesReadPlatformService;
    private final ToApiJsonSerializer<ExternalServicesPropertiesData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Path("{servicename}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve External Services Configuration", description = "Returns a external Service configurations based on the Service Name.\n"
            + "\n" + "Service Names supported are S3 and SMTP.\n" + "\n" + "Example Requests:\n" + "\n" + "externalservice/SMTP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalServicesPropertiesData.class))) })
    public String retrieveOne(@PathParam("servicename") @Parameter(description = "servicename") final String serviceName,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(ExternalServiceConfigurationApiConstant.EXTERNAL_SERVICE_RESOURCE_NAME);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final Collection<ExternalServicesPropertiesData> externalServiceNVPs = this.externalServicePropertiesReadPlatformService
                .retrieveOne(serviceName);
        return this.toApiJsonSerializer.serialize(settings, externalServiceNVPs,
                ExternalServiceConfigurationApiConstant.EXTERNAL_SERVICE_CONFIGURATION_DATA_PARAMETERS);
    }

    @PUT
    @Path("{servicename}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update External Service", description = "Updates the external Service Configuration for a Service Name.\n" + "\n"
            + "Example: \n" + "\n" + "externalservice/S3")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalServicesConfigurationApiResourceSwagger.PutExternalServiceRequest.class)))
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String updateExternalServiceProperties(
            @PathParam("servicename") @Parameter(description = "servicename") final String serviceName,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        // ExternalServicesData external =
        // this.externalServiceReadPlatformService.getExternalServiceDetailsByServiceName(serviceName);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateExternalServiceProperties(serviceName)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }
}
