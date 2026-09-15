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
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.command.FamilyMemberCreateCommand;
import org.apache.fineract.portfolio.client.command.FamilyMemberDeleteCommand;
import org.apache.fineract.portfolio.client.command.FamilyMemberUpdateCommand;
import org.apache.fineract.portfolio.client.data.ClientFamilyMembersData;
import org.apache.fineract.portfolio.client.data.FamilyMemberCreateRequest;
import org.apache.fineract.portfolio.client.data.FamilyMemberCreateResponse;
import org.apache.fineract.portfolio.client.data.FamilyMemberDeleteRequest;
import org.apache.fineract.portfolio.client.data.FamilyMemberDeleteResponse;
import org.apache.fineract.portfolio.client.data.FamilyMemberUpdateRequest;
import org.apache.fineract.portfolio.client.data.FamilyMemberUpdateResponse;
import org.apache.fineract.portfolio.client.service.ClientFamilyMembersReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/clients/{clientId}/familymembers")
@Component
@Tag(name = "Client Family Member", description = "")
@RequiredArgsConstructor
public class ClientFamilyMembersApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "FamilyMembers";

    private final PlatformSecurityContext context;
    private final ClientFamilyMembersReadPlatformService readPlatformService;
    private final CommandDispatcher dispatcher;

    @GET
    @Path("/{familyMemberId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a client family member", operationId = "retrieveOneClientFamilyMember")
    @AlternativeOperationId("getFamilyMember")
    public ClientFamilyMembersData getFamilyMember(@PathParam("familyMemberId") final Long familyMemberId,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.getClientFamilyMember(clientId, familyMemberId);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all client family members", operationId = "retrieveAllClientFamilyMembers")
    @AlternativeOperationId("getFamilyMembers")
    public List<ClientFamilyMembersData> getFamilyMembers(@PathParam("clientId") final long clientId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.getClientFamilyMembers(clientId);
    }

    @GET
    @Path("/template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client family member template", operationId = "retrieveTemplateClientFamilyMember")
    @AlternativeOperationId("getTemplate_2")
    public ClientFamilyMembersData getTemplate(@PathParam("clientId") final long clientId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.retrieveTemplate();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Add a client family member", operationId = "createClientFamilyMember")
    @AlternativeOperationId("addClientFamilyMembers")
    public FamilyMemberCreateResponse addClientFamilyMembers(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @RequestBody(required = true) @Valid FamilyMemberCreateRequest request) {
        request.setClientId(clientId);
        final var command = new FamilyMemberCreateCommand();
        command.setPayload(request);
        final Supplier<FamilyMemberCreateResponse> response = dispatcher.dispatch(command);
        return response.get();
    }

    @PUT
    @Path("/{familyMemberId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a client family member", operationId = "updateClientFamilyMember")
    @AlternativeOperationId("updateClientFamilyMembers")
    public FamilyMemberUpdateResponse updateClientFamilyMembers(
            @PathParam("familyMemberId") @Parameter(description = "familyMemberId") final Long familyMemberId,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @RequestBody(required = true) @Valid FamilyMemberUpdateRequest request) {
        request.setId(familyMemberId);
        request.setClientId(clientId);
        final var command = new FamilyMemberUpdateCommand();
        command.setPayload(request);
        final Supplier<FamilyMemberUpdateResponse> response = dispatcher.dispatch(command);
        return response.get();
    }

    @DELETE
    @Path("/{familyMemberId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a client family member", operationId = "deleteClientFamilyMember")
    @AlternativeOperationId("deleteClientFamilyMembers")
    public FamilyMemberDeleteResponse deleteClientFamilyMembers(
            @PathParam("familyMemberId") @Parameter(description = "familyMemberId") final Long familyMemberId,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        final var request = FamilyMemberDeleteRequest.builder().id(familyMemberId).clientId(clientId).build();
        final var command = new FamilyMemberDeleteCommand();
        command.setPayload(request);
        final Supplier<FamilyMemberDeleteResponse> response = dispatcher.dispatch(command);
        return response.get();
    }
}
