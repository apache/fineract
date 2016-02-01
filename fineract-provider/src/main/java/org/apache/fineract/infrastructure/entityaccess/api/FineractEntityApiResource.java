/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.entityaccess.data.MifosEntityRelationData;
import org.mifosplatform.infrastructure.entityaccess.data.MifosEntityToEntityMappingData;
import org.mifosplatform.infrastructure.entityaccess.service.MifosEntityAccessReadService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/entitytoentitymapping")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Scope("singleton")
public class MifosEntityApiResource {

    private final PlatformSecurityContext context;
    private final MifosEntityAccessReadService readPlatformService;
    private final DefaultToApiJsonSerializer<MifosEntityRelationData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<MifosEntityToEntityMappingData> toApiJsonSerializerOfficeToLoanProducts;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public MifosEntityApiResource(final PlatformSecurityContext context, final MifosEntityAccessReadService readPlatformService,
            final DefaultToApiJsonSerializer<MifosEntityRelationData> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<MifosEntityToEntityMappingData> toApiJsonSerializerOfficeToLoanProducts,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializerOfficeToLoanProducts = toApiJsonSerializerOfficeToLoanProducts;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(MifosEntityApiResourceConstants.MIFOS_ENTITY_RESOURCE_NAME);

        final Collection<MifosEntityRelationData> entityMappings = this.readPlatformService.retrieveAllSupportedMappingTypes();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, entityMappings, MifosEntityApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/{mapId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("mapId") final Long mapId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(MifosEntityApiResourceConstants.MIFOS_ENTITY_RESOURCE_NAME);

        final Collection<MifosEntityToEntityMappingData> entityToEntityMappings = this.readPlatformService.retrieveOneMapping(mapId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerOfficeToLoanProducts.serialize(settings, entityToEntityMappings,
                MifosEntityApiResourceConstants.FETCH_ENTITY_TO_ENTITY_MAPPINGS);
    }

    @GET
    @Path("/{mapId}/{fromId}/{toId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getEntityToEntityMappings(@PathParam("mapId") final Long mapId, @PathParam("fromId") final Long fromId,
            @PathParam("toId") final Long toId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(MifosEntityApiResourceConstants.MIFOS_ENTITY_RESOURCE_NAME);

        final Collection<MifosEntityToEntityMappingData> entityToEntityMappings = this.readPlatformService.retrieveEntityToEntityMappings(
                mapId, fromId, toId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerOfficeToLoanProducts.serialize(settings, entityToEntityMappings,
                MifosEntityApiResourceConstants.FETCH_ENTITY_TO_ENTITY_MAPPINGS);
    }

    @POST
    @Path("/{relId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createMap(@PathParam("relId") final Long relId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createMap(relId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("/{mapId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateMap(@PathParam("mapId") final Long mapId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateMap(mapId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @DELETE
    @Path("{mapId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("mapId") final Long mapId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteMap(mapId) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
