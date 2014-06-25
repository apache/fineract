/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.configuration.data.GlobalConfigurationData;
import org.mifosplatform.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.mifosplatform.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/configurations")
@Component
@Scope("singleton")
public class GlobalConfigurationApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("globalConfiguration"));

    private final String resourceNameForPermissions = "CONFIGURATION";

    private final PlatformSecurityContext context;
    private final ConfigurationReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<GlobalConfigurationData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<GlobalConfigurationPropertyData> propertyDataJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public GlobalConfigurationApiResource(final PlatformSecurityContext context,
            final ConfigurationReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<GlobalConfigurationData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<GlobalConfigurationPropertyData> propertyDataJsonSerializer) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.propertyDataJsonSerializer = propertyDataJsonSerializer;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveConfiguration(@Context final UriInfo uriInfo,@DefaultValue("false") @QueryParam("survey") final boolean survey) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final GlobalConfigurationData configurationData = this.readPlatformService.retrieveGlobalConfiguration(survey);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, configurationData, this.RESPONSE_DATA_PARAMETERS);
    }
    
    @GET
    @Path("{configId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("configId") final Long configId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final GlobalConfigurationPropertyData configurationData = this.readPlatformService.retrieveGlobalConfiguration(configId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.propertyDataJsonSerializer.serialize(settings, configurationData, this.RESPONSE_DATA_PARAMETERS);
    }


    @PUT
    @Path("{configId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateConfiguration(@PathParam("configId") final Long configId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateGlobalConfiguration(configId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}