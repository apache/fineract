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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.command.ClientIdentifierCreateCommand;
import org.apache.fineract.portfolio.client.command.ClientIdentifierDeleteCommand;
import org.apache.fineract.portfolio.client.command.ClientIdentifierUpdateCommand;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierData;
import org.apache.fineract.portfolio.client.data.ClientIdentifierDeleteRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierDeleteResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierUpdateRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierUpdateResponse;
import org.apache.fineract.portfolio.client.exception.DuplicateClientIdentifierException;
import org.apache.fineract.portfolio.client.service.ClientIdentifierReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/clients/{clientId}/identifiers")
@Component
@Tag(name = "Client Identifier", description = "Client Identifiers refer to documents that are used to uniquely identify a customer\n"
        + "Ex: Drivers License, Passport, Ration card etc ")
@RequiredArgsConstructor
public class ClientIdentifiersApiResource {

    private static final Set<String> CLIENT_IDENTIFIER_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "clientId", "documentType",
            "documentKey", "description", "issuanceDate", "expiryDate", "allowedDocumentTypes"));

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "CLIENTIDENTIFIER";

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ClientIdentifierReadPlatformService clientIdentifierReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientIdentifierData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final CommandDispatcher dispatcher;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all Identifiers for a Client", operationId = "retrieveAllClientIdentifiers", description = "Example Requests:\n"
            + "clients/1/identifiers\n" + "\n" + "\n" + "clients/1/identifiers?fields=documentKey,documentType,description")
    public List<ClientIdentifierData> retrieveAllClientIdentifiers(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.clientIdentifierReadPlatformService.retrieveClientIdentifiers(clientId);
    }

    @GET
    @Path("template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Client Identifier Details Template", operationId = "retrieveTemplateClientIdentifier", description = "This is a convenience resource useful for building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + " Field Defaults\n" + " Allowed description Lists\n" + "\n\nExample Request:\n" + "clients/1/identifiers/template")
    @AlternativeOperationId("newClientIdentifierDetails")
    public ClientIdentifierData newClientIdentifierDetails(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final List<CodeValueData> codeValues = this.codeValueReadPlatformService.retrieveCodeValuesByCode("Customer Identifier");
        return ClientIdentifierData.template(codeValues);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Identifier for a Client", operationId = "createClientIdentifier", description = "Mandatory Fields\n"
            + "documentKey, documentTypeId ")
    public ClientIdentifierCreateResponse createClientIdentifier(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @RequestBody(required = true) @Valid ClientIdentifierCreateRequest request) {
        request.setClientId(clientId);
        try {
            final var command = new ClientIdentifierCreateCommand();
            command.setPayload(request);
            final Supplier<ClientIdentifierCreateResponse> response = dispatcher.dispatch(command);
            return response.get();
        } catch (final DuplicateClientIdentifierException e) {
            throw enrichDuplicateIdentifierException(e);
        }
    }

    @GET
    @Path("{identifierId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Identifier", operationId = "retrieveOneClientIdentifier", description = "Example Requests:\n"
            + "clients/1/identifier/2\n" + "\n" + "\n" + "clients/1/identifier/2?template=true\n" + "\n"
            + "clients/1/identifiers/2?fields=documentKey,documentType,description")
    @AlternativeOperationId("retrieveClientIdentifiers")
    public String retrieveClientIdentifiers(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("identifierId") @Parameter(description = "identifierId") final Long clientIdentifierId,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
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
    @Operation(summary = "Update a Client Identifier", operationId = "updateClientIdentifier", description = "Updates a Client Identifier")
    @AlternativeOperationId("updateClientIdentifer")
    public ClientIdentifierUpdateResponse updateClientIdentifier(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("identifierId") @Parameter(description = "identifierId") final Long clientIdentifierId,
            @RequestBody(required = true) @Valid ClientIdentifierUpdateRequest request) {
        request.setId(clientIdentifierId);
        request.setClientId(clientId);
        try {
            final var command = new ClientIdentifierUpdateCommand();
            command.setPayload(request);
            final Supplier<ClientIdentifierUpdateResponse> response = dispatcher.dispatch(command);
            return response.get();
        } catch (final DuplicateClientIdentifierException e) {
            throw enrichDuplicateIdentifierException(e);
        }
    }

    @DELETE
    @Path("{identifierId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Client Identifier", operationId = "deleteClientIdentifier", description = "Deletes a Client Identifier")
    public ClientIdentifierDeleteResponse deleteClientIdentifier(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("identifierId") @Parameter(description = "identifierId") final Long clientIdentifierId) {
        final var request = ClientIdentifierDeleteRequest.builder().id(clientIdentifierId).clientId(clientId).build();
        final var command = new ClientIdentifierDeleteCommand();
        command.setPayload(request);
        final Supplier<ClientIdentifierDeleteResponse> response = dispatcher.dispatch(command);
        return response.get();
    }

    private DuplicateClientIdentifierException enrichDuplicateIdentifierException(final DuplicateClientIdentifierException e) {
        if (e.getDocumentTypeId() != null) {
            final ClientData clientInfo = this.clientReadPlatformService.retrieveClientByIdentifier(e.getDocumentTypeId(),
                    e.getIdentifierKey());
            return new DuplicateClientIdentifierException(clientInfo.getDisplayName(), clientInfo.getOfficeName(), e.getIdentifierType(),
                    e.getIdentifierKey());
        }
        return e;
    }
}
