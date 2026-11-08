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
package org.apache.fineract.infrastructure.entityaccess.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.core.annotation.PartialResponseParameters;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityRelationData;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityToEntityMappingData;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/entitytoentitymapping")
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Fineract Entity", description = "")
@PartialResponseParameters({ FineractEntityApiResourceConstants.mapId, FineractEntityApiResourceConstants.relId,
        FineractEntityApiResourceConstants.fromEnityType, FineractEntityApiResourceConstants.toEntityType })
@RequiredArgsConstructor
public class FineractEntityApiResource {

    private final PlatformSecurityContext context;
    private final FineractEntityAccessReadService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveAll_3")
    @AlternativeOperationId("retrieveAll_7")
    @PartialResponseParameters({ FineractEntityApiResourceConstants.mappingTypes })
    public Collection<FineractEntityRelationData> retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(FineractEntityApiResourceConstants.ENTITY_MAPPING_RESOURCE_NAME);

        return this.readPlatformService.retrieveAllSupportedMappingTypes();
    }

    @GET
    @Path("/{mapId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveOne")
    @AlternativeOperationId("retrieveOne_4")
    public Collection<FineractEntityToEntityMappingData> retrieveOne(@PathParam("mapId") final Long mapId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(FineractEntityApiResourceConstants.ENTITY_MAPPING_RESOURCE_NAME);

        return this.readPlatformService.retrieveOneMapping(mapId);
    }

    @GET
    @Path("/{mapId}/{fromId}/{toId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Collection<FineractEntityToEntityMappingData> getEntityToEntityMappings(@PathParam("mapId") final Long mapId,
            @PathParam("fromId") final Long fromId, @PathParam("toId") final Long toId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(FineractEntityApiResourceConstants.ENTITY_MAPPING_RESOURCE_NAME);

        return this.readPlatformService.retrieveEntityToEntityMappings(mapId, fromId, toId);
    }

    @POST
    @Path("/{relId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult createMap(@PathParam("relId") final Long relId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createMap(relId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("/{mapId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult updateMap(@PathParam("mapId") final Long mapId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateMap(mapId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{mapId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "delete")
    @AlternativeOperationId("delete_4")
    public CommandProcessingResult delete(@PathParam("mapId") final Long mapId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteMap(mapId) //
                .build(); //

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

}
