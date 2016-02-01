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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

@Path("/clients/{clientId}/identifiers")
@Component
@Scope("singleton")
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
    public String retrieveAllClientIdentifiers(@Context final UriInfo uriInfo, @PathParam("clientId") final Long clientId) {

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
    public String createClientIdentifier(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {

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
    public String retrieveClientIdentifiers(@PathParam("clientId") final Long clientId,
            @PathParam("identifierId") final Long clientIdentifierId, @Context final UriInfo uriInfo) {

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
    public String updateClientIdentifer(@PathParam("clientId") final Long clientId,
            @PathParam("identifierId") final Long clientIdentifierId, final String apiRequestBodyAsJson) {

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
    public String deleteClientIdentifier(@PathParam("clientId") final Long clientId,
            @PathParam("identifierId") final Long clientIdentifierId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClientIdentifier(clientId, clientIdentifierId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}