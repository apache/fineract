/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.api;

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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.configuration.data.ExternalServicesData;
import org.mifosplatform.infrastructure.configuration.data.ExternalServicesPropertiesData;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/externalservice")
@Component
@Scope("singleton")
public class ExternalServicesConfigurationApiResource {

    private final PlatformSecurityContext context;
    private final ExternalServicesPropertiesReadPlatformService externalServicePropertiesReadPlatformService;
    private final ExternalServicesReadPlatformService externalServiceReadPlatformService;
    private final ToApiJsonSerializer<ExternalServicesPropertiesData> toApiJsonSerializer;
    // private final ToApiJsonSerializer<S3CredentialsData>
    // s3ToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ExternalServicesConfigurationApiResource(final PlatformSecurityContext context,
            final ExternalServicesPropertiesReadPlatformService readPlatformService,
            final ExternalServicesReadPlatformService externalServicesReadPlatformService,
            final ToApiJsonSerializer<ExternalServicesPropertiesData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.externalServicePropertiesReadPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.externalServiceReadPlatformService = externalServicesReadPlatformService;
        // this.s3ToApiJsonSerializer = s3ToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("{servicename}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("servicename") final String serviceName, @Context final UriInfo uriInfo) {
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
    public String updateExternalServiceProperties(@PathParam("servicename") final String serviceName, final String apiRequestBodyAsJson) {
        // ExternalServicesData external =
        // this.externalServiceReadPlatformService.getExternalServiceDetailsByServiceName(serviceName);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateExternalServiceProperties(serviceName)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }
}
