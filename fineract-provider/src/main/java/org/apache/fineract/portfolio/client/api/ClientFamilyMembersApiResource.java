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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientFamilyMemberRequest;
import org.apache.fineract.portfolio.client.data.ClientFamilyMembersData;
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
    private final ToApiJsonSerializer<ClientFamilyMembersData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Path("/{familyMemberId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public ClientFamilyMembersData getFamilyMember(@PathParam("familyMemberId") final Long familyMemberId,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return this.readPlatformService.getClientFamilyMember(familyMemberId);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public List<ClientFamilyMembersData> getFamilyMembers(@PathParam("clientId") final long clientId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.getClientFamilyMembers(clientId);
    }

    @GET
    @Path("/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public ClientFamilyMembersData getTemplate(@PathParam("clientId") final long clientId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.retrieveTemplate();
    }

    @PUT
    @Path("/{familyMemberId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult updateClientFamilyMembers(@PathParam("familyMemberId") final long familyMemberId,
            ClientFamilyMemberRequest clientFamilyMemberRequest,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateFamilyMembers(familyMemberId)
                .withJson(toApiJsonSerializer.serialize(clientFamilyMemberRequest)).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult addClientFamilyMembers(@PathParam("clientId") final long clientid,
            ClientFamilyMemberRequest clientFamilyMemberRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().addFamilyMembers(clientid)
                .withJson(toApiJsonSerializer.serialize(clientFamilyMemberRequest)).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("/{familyMemberId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult deleteClientFamilyMembers(@PathParam("familyMemberId") final long familyMemberId,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteFamilyMembers(familyMemberId).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

}
