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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.address.service.AddressReadPlatformServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/client")
@Component
@Scope("singleton")
@Tag(name = "Clients Address", description = "Address module is an optional module and can be configured into the system by using GlobalConfiguration setting: enable-address. In order to activate Address module, we need to enable the configuration, enable-address by setting its value to true.")
public class ClientAddressApiResources {

    private final Set<String> responseDataParameters = new HashSet<>(Arrays.asList("addressId", "street", "addressLine1", "addressLine2",
            "addressLine3", "townVillage", "city", "countyDistrict", "stateProvinceId", "countryId", "postalCode", "latitude", "longitude",
            "createdBy", "createdOn", "updatedBy", "updatedOn", "clientAddressId", "client_id", "address_id", "address_type_id", "isActive",
            "fieldConfigurationId", "entity", "table", "field", "is_enabled", "is_mandatory", "validation_regex"));
    private final String resourceNameForPermissions = "Address";
    private final PlatformSecurityContext context;
    private final AddressReadPlatformServiceImpl readPlatformService;
    private final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientAddressApiResources(final PlatformSecurityContext context, final AddressReadPlatformServiceImpl readPlatformService,
            final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("addresses/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getAddressesTemplate(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final AddressData template = this.readPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, template, this.responseDataParameters);

    }

    @POST
    @Path("/{clientid}/addresses")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an address for a Client", description = "Mandatory Fields : \n" + "type and clientId")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientAddressApiResourcesSwagger.PostClientClientIdAddressesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientAddressApiResourcesSwagger.PostClientClientIdAddressesResponse.class))) })
    public String addClientAddress(@QueryParam("type") @Parameter(description = "type") final long addressTypeId,
            @PathParam("clientid") @Parameter(description = "clientId") final long clientid,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().addClientAddress(clientid, addressTypeId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("/{clientid}/addresses")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all addresses for a Client", description = "Example Requests:\n" + "\n" + "client/1/addresses\n" + "\n"
            + "\n" + "clients/1/addresses?status=false,true&&type=1,2,3")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientAddressApiResourcesSwagger.GetClientClientIdAddressesResponse.class)))) })
    public String getAddresses(@QueryParam("status") @Parameter(description = "status") final String status,
            @QueryParam("type") @Parameter(description = "type") final long addressTypeId,
            @PathParam("clientid") @Parameter(description = "clientId") final long clientid, @Context final UriInfo uriInfo) {
        Collection<AddressData> address;

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        // TODO: This is quite a confusing implementation with all these checks
        // These have to be considered as filtering criterias instead
        if (addressTypeId == 0 && status == null) {
            address = this.readPlatformService.retrieveAllClientAddress(clientid);
        } else if (addressTypeId != 0 && status == null) {
            address = this.readPlatformService.retrieveAddressbyType(clientid, addressTypeId);
        } else if (addressTypeId != 0 && status != null) {
            address = this.readPlatformService.retrieveAddressbyTypeAndStatus(clientid, addressTypeId, status);
        } else {
            address = this.readPlatformService.retrieveAddressbyStatus(clientid, status);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, address, this.responseDataParameters);

    }

    @PUT
    @Path("/{clientid}/addresses")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update an address for a Client", description = "All the address fields can be updated by using update client address API\n"
            + "\n" + "Mandatory Fields\n" + "type and addressId")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientAddressApiResourcesSwagger.PutClientClientIdAddressesRequest.class)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientAddressApiResourcesSwagger.PutClientClientIdAddressesResponse.class))) })
    public String updateClientAddress(@PathParam("clientid") @Parameter(description = "clientId") final long clientid,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateClientAddress(clientid).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
