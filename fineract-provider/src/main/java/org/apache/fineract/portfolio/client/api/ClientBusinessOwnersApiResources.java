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

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientBusinessOwnerData;
import org.apache.fineract.portfolio.client.domain.ClientBusinessOwners;
import org.apache.fineract.portfolio.client.service.BusinessOwnerWritePlatformService;
import org.apache.fineract.portfolio.client.service.ClientBusinessOwnerReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/{clientId}/businessOwners")
@Component
@Scope("singleton")
@Tag(name = "Client Business Owner", description = "")
public class ClientBusinessOwnersApiResources {

    private final Set<String> responseDataParameters = new HashSet<>(
            Arrays.asList("id", "clientId", "firstName", "title", "lastName", "middleName", "email", "mobileNumber", "alterMobileNumber",
                    "isActive", "city", "username", "streetNumberAndName", "dateOfBirth", "lga", "stateProvince", "country", "bvn"));
    private final String resourceNameForPermissions = "BusinessOwners";
    private final PlatformSecurityContext context;
    private final ClientBusinessOwnerReadPlatformService readPlatformService;
    private final BusinessOwnerWritePlatformService writePlatformService;
    private final ToApiJsonSerializer<ClientBusinessOwnerData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientBusinessOwnersApiResources(final PlatformSecurityContext context,
            final ClientBusinessOwnerReadPlatformService readPlatformService,
            final ToApiJsonSerializer<ClientBusinessOwnerData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final BusinessOwnerWritePlatformService writePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.writePlatformService = writePlatformService;

    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getBusinessOwnerTemplate(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ClientBusinessOwnerData template = this.readPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, template, this.responseDataParameters);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveBusinessOwners(@Context final UriInfo uriInfo, @PathParam("clientId") final long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ClientBusinessOwnerData> businessOwners = this.readPlatformService.getClientBusinessOwners(clientId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, businessOwners, this.responseDataParameters);

    }

    @GET
    @Path("/{businessOwnerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getFamilyMember(@Context final UriInfo uriInfo, @PathParam("businessOwnerId") final Long businessOwnerId,
            @PathParam("clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ClientBusinessOwnerData businessOwners = this.readPlatformService.getBusinessOwner(businessOwnerId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, businessOwners, this.responseDataParameters);

    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String addClientBusinessOwner(@PathParam("clientId") final long clientid, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().addBusinessOwner(clientid).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("/{businessOwnerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClientBusinessOwner(@PathParam("businessOwnerId") final long businessOwnerId, final String apiRequestBodyAsJson,
            @PathParam("clientId") final Long clientId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateBusinessOwner(businessOwnerId, clientId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("/{businessOwnerId}/updateOwnerStatus")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Boolean updateOwnerStatus(@Context final UriInfo uriInfo, @PathParam("businessOwnerId") final Long businessOwnerId,
            @QueryParam("status") final Boolean status, @PathParam("clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ClientBusinessOwners businessOwners = this.writePlatformService.updateBusinessOwnerStatus(businessOwnerId, status);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return businessOwners.getActive();

    }
}
