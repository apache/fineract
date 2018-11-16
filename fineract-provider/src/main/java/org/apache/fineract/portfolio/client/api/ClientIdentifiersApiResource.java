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
package org.apache.fineract.portfolio.client.api;

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.ClientIdentifierData;
import org.apache.fineract.portfolio.client.exception.DuplicateClientIdentifierException;
import org.apache.fineract.portfolio.client.service.ClientIdentifierReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Path("/clients/{clientId}/identifiers")
@Component
@Scope("singleton")
@Api(value = "Client Identifier", description = "Client Identifiers refer to documents that are used to uniquely identify a customer\n" + "Ex: Drivers License, Passport, Ration card etc ")
public class ClientIdentifiersApiResource {

    private static final Set<String> CLIENT_IDENTIFIER_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "clientId",
            "documentType", "documentKey", "description", "allowedDocumentTypes"));

    private final String resourceNameForPermissions = "CLIENTIDENTIFIER";

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ClientIdentifierReadPlatformService clientIdentifierReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientIdentifierData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientIdentifiersApiResource(final PlatformSecurityContext context, final ClientReadPlatformService readPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final DefaultToApiJsonSerializer<ClientIdentifierData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ClientIdentifierReadPlatformService clientIdentifierReadPlatformService) {
        this.context = context;
        this.clientReadPlatformService = readPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.clientIdentifierReadPlatformService = clientIdentifierReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List all Identifiers for a Client", notes = "Example Requests:\n" + "clients/1/identifiers\n" + "\n" + "\n" + "clients/1/identifiers?fields=documentKey,documentType,description")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", responseContainer = "List", response = ClientIdentifiersApiResourceSwagger.GetClientsClientIdIdentifiersResponse.class)})
    public String retrieveAllClientIdentifiers(@Context final UriInfo uriInfo, @PathParam("clientId") @ApiParam(value = "clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ClientIdentifierData> clientIdentifiers = this.clientIdentifierReadPlatformService
                .retrieveClientIdentifiers(clientId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientIdentifiers, CLIENT_IDENTIFIER_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Client Identifier Details Template", notes = "This is a convenience resource useful for building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + " Field Defaults\n" + " Allowed Value Lists\n" + "\n\nExample Request:\n" + "clients/1/identifiers/template" )
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientIdentifiersApiResourceSwagger.GetClientsClientIdIdentifiersTemplateResponse.class)})
    public String newClientIdentifierDetails(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService.retrieveCodeValuesByCode("Customer Identifier");
        final ClientIdentifierData clientIdentifierData = ClientIdentifierData.template(codeValues);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientIdentifierData, CLIENT_IDENTIFIER_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create an Identifier for a Client", notes = "Mandatory Fields\n" + "documentKey, documentTypeId " )
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ClientIdentifiersApiResourceSwagger.PostClientsClientIdIdentifiersRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientIdentifiersApiResourceSwagger.PostClientsClientIdIdentifiersResponse.class)})
    public String createClientIdentifier(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        try {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().createClientIdentifier(clientId)
                    .withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            return this.toApiJsonSerializer.serialize(result);
        } catch (final DuplicateClientIdentifierException e) {
            DuplicateClientIdentifierException rethrowas = e;
            if (e.getDocumentTypeId() != null) {
                // need to fetch client info
                final ClientData clientInfo = this.clientReadPlatformService.retrieveClientByIdentifier(e.getDocumentTypeId(),
                        e.getIdentifierKey());
                rethrowas = new DuplicateClientIdentifierException(clientInfo.displayName(), clientInfo.officeName(),
                        e.getIdentifierType(), e.getIdentifierKey());
            }
            throw rethrowas;
        }
    }

    @GET
    @Path("{identifierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Client Identifier", notes = "Example Requests:\n" + "clients/1/identifier/2\n" + "\n" + "\n" + "clients/1/identifier/2?template=true\n" + "\n" + "clients/1/identifiers/2?fields=documentKey,documentType,description" )
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientIdentifiersApiResourceSwagger.GetClientsClientIdIdentifiersResponse.class)})
    public String retrieveClientIdentifiers(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
            @PathParam("identifierId") @ApiParam(value = "identifierId") final Long clientIdentifierId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ClientIdentifierData clientIdentifierData = this.clientIdentifierReadPlatformService.retrieveClientIdentifier(clientId,
                clientIdentifierId);
        if (settings.isTemplate()) {
            final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService.retrieveCodeValuesByCode("Customer Identifier");
            clientIdentifierData = ClientIdentifierData.template(clientIdentifierData, codeValues);
        }

        return this.toApiJsonSerializer.serialize(settings, clientIdentifierData, CLIENT_IDENTIFIER_DATA_PARAMETERS);
    }

    @PUT
    @Path("{identifierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Client Identifier", notes = "Updates a Client Identifier")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ClientIdentifiersApiResourceSwagger.PutClientsClientIdIdentifiersIdentifierIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientIdentifiersApiResourceSwagger.PutClientsClientIdIdentifiersIdentifierIdResponse.class)})
    public String updateClientIdentifer(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
            @PathParam("identifierId") @ApiParam(value = "identifierId") final Long clientIdentifierId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        try {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().updateClientIdentifier(clientId, clientIdentifierId)
                    .withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            return this.toApiJsonSerializer.serialize(result);
        } catch (final DuplicateClientIdentifierException e) {
            DuplicateClientIdentifierException reThrowAs = e;
            if (e.getDocumentTypeId() != null) {
                final ClientData clientInfo = this.clientReadPlatformService.retrieveClientByIdentifier(e.getDocumentTypeId(),
                        e.getIdentifierKey());
                reThrowAs = new DuplicateClientIdentifierException(clientInfo.displayName(), clientInfo.officeName(),
                        e.getIdentifierType(), e.getIdentifierKey());
            }
            throw reThrowAs;
        }
    }

    @DELETE
    @Path("{identifierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Client Identifier", notes = "Deletes a Client Identifier")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientIdentifiersApiResourceSwagger.DeleteClientsClientIdIdentifiersIdentifierIdResponse.class)})
    public String deleteClientIdentifier(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
            @PathParam("identifierId") @ApiParam(value = "identifierId") final Long clientIdentifierId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClientIdentifier(clientId, clientIdentifierId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}