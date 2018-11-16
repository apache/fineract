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

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/externalservice")
@Component
@Scope("singleton")
@Api(value = "External Services", description = "External Services Configuration related to set of supported configurations for third party services like Amazon S3 and SMTP:\n" + "\n" + "S3 (Amazon S3):\n" + "s3_access_key -\n" + "s3_bucket_name -\n" + "s3_secret_key -\n" + "\n" + "\n" + "SMTP (Email Service):\n" + "username -\n" + "password -\n" + "host -\n" + "port -\n" + "useTLS -")
public class ExternalServicesConfigurationApiResource {

    private final PlatformSecurityContext context;
    private final ExternalServicesPropertiesReadPlatformService externalServicePropertiesReadPlatformService;
    private final ToApiJsonSerializer<ExternalServicesPropertiesData> toApiJsonSerializer;
    // private final ToApiJsonSerializer<S3CredentialsData>
    // s3ToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ExternalServicesConfigurationApiResource(final PlatformSecurityContext context,
            final ExternalServicesPropertiesReadPlatformService readPlatformService,
            final ToApiJsonSerializer<ExternalServicesPropertiesData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.externalServicePropertiesReadPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        // this.s3ToApiJsonSerializer = s3ToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("{servicename}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve External Services Configuration", notes = "Returns a external Service configurations based on the Service Name.\n" + "\n" + "Service Names supported are S3 and SMTP.\n" + "\n" + "Example Requests:\n" + "\n" + "externalservice/SMTP")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ExternalServicesPropertiesData.class)})
    public String retrieveOne(@PathParam("servicename") @ApiParam(value = "servicename") final String serviceName, @Context final UriInfo uriInfo) {
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
    @ApiOperation(value = "Update External Service", notes = "Updates the external Service Configuration for a Service Name.\n" + "\n" + "Example: \n" + "\n" + "externalservice/S3")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ExternalServicesConfigurationApiResourceSwagger.PutExternalServiceRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "")})
    public String updateExternalServiceProperties(@PathParam("servicename") @ApiParam(value = "servicename") final String serviceName,
                                                  @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
        // ExternalServicesData external =
        // this.externalServiceReadPlatformService.getExternalServiceDetailsByServiceName(serviceName);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateExternalServiceProperties(serviceName)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }
}
