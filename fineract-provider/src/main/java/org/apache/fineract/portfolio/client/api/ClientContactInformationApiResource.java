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
import org.apache.fineract.portfolio.client.data.ClientContactInformationData;
import org.apache.fineract.portfolio.client.service.ClientContactInformationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/{clientId}/contactinformation")
@Component
@Scope("singleton")
@Tag(name = "Client Contact Information", description = "Client Contact Information refer to information that is used to contact a customer\n"
        + "Ex: Drivers License, Passport, Ration card etc ")
public class ClientContactInformationApiResource {

    private static final Set<String> CLIENT_INFORMATION_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "clientId", "contactType", "contactKey", "allowedDocumentTypes"));

    private final String resourceNameForPermissions = "CLIENTCONTACTINFORMATION";

    private final PlatformSecurityContext context;
    private final ClientContactInformationReadPlatformService clientInformationReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientContactInformationData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientContactInformationApiResource(final PlatformSecurityContext context,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final DefaultToApiJsonSerializer<ClientContactInformationData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ClientContactInformationReadPlatformService clientInformationReadPlatformService) {
        this.context = context;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.clientInformationReadPlatformService = clientInformationReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all Contact Information for a Client", description = "Example Requests:\n" + "clients/1/identifiers\n" + "\n"
            + "\n" + "clients/1/identifiers?fields=contactKey,contactType,description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientContactInformationApiResourceSwagger.GetClientsContactInformationResponse.class)))) })
    public String retrieveAllClientInformation(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ClientContactInformationData> clientInformations = this.clientInformationReadPlatformService
                .retrieveClientContactInformation(clientId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientInformations, CLIENT_INFORMATION_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Client Contact Information Details Template", description = "This is a convenience resource useful for building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + " Field Defaults\n" + " Allowed description Lists\n" + "\n\nExample Request:\n" + "clients/1/identifiers/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientContactInformationApiResourceSwagger.GetClientsContactInformationTemplateResponse.class))) })
    public String newClientInformationDetails(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CONTACT_TYPE_CODENAME);
        final ClientContactInformationData clientInformationData = ClientContactInformationData.template(codeValues);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientInformationData, CLIENT_INFORMATION_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Contact Information for a Client", description = "Mandatory Fields\n" + "contactKey, contactTypeId ")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientContactInformationApiResourceSwagger.PostClientsContactInformationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientContactInformationApiResourceSwagger.PostClientsContactInformationResponse.class))) })
    public String createClientInformation(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createClientContactInformation(clientId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{informationId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Contact Information", description = "Example Requests:\n" + "clients/1/identifier/2\n" + "\n"
            + "\n" + "clients/1/identifier/2?template=true\n" + "\n" + "clients/1/identifiers/2?fields=contactKey,contactType,description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientContactInformationApiResourceSwagger.GetClientsContactInformationResponse.class))) })
    public String retrieveClientInformation(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("informationId") @Parameter(description = "informationId") final Long clientInformationId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ClientContactInformationData clientInformationData = this.clientInformationReadPlatformService
                .retrieveClientContactInformation(clientId, clientInformationId);
        if (settings.isTemplate()) {
            final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService
                    .retrieveCodeValuesByCode("Customer Contact Information");
            clientInformationData = ClientContactInformationData.template(clientInformationData, codeValues);
        }

        return this.toApiJsonSerializer.serialize(settings, clientInformationData, CLIENT_INFORMATION_DATA_PARAMETERS);
    }

    @PUT
    @Path("{informationId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Client Contact Information", description = "Updates a Client Contact Information")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientContactInformationApiResourceSwagger.PutClientsContactInformationIdentifierIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientContactInformationApiResourceSwagger.PutClientsContactInformationIdentifierIdResponse.class))) })
    public String updateClientInformation(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("informationId") @Parameter(description = "informationId") final Long clientInformationId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateClientContactInformation(clientId, clientInformationId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{informationId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Client Contact Information", description = "Deletes a Client Contact Information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientContactInformationApiResourceSwagger.DeleteClientsContactInformationIdentifierIdResponse.class))) })
    public String deleteClientInformation(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("informationId") @Parameter(description = "informationId") final Long clientInformationId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClientContactInformation(clientId, clientInformationId)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
