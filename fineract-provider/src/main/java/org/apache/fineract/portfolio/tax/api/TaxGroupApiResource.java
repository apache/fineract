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
package org.apache.fineract.portfolio.tax.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.request.TaxGroupRequest;
import org.apache.fineract.portfolio.tax.service.TaxReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/taxes/group")
@Component
@Tag(name = "Tax Group", description = "This defines the Tax Group")
@RequiredArgsConstructor
public class TaxGroupApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "TAXGROUP";

    private final PlatformSecurityContext context;
    private final TaxReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<String> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Tax Group", description = "List Tax Group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaxGroupApiResourceSwagger.GetTaxesGroupResponse.class)))) })
    public List<TaxGroupData> retrieveAllTaxGroups() {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return readPlatformService.retrieveAllTaxGroups();
    }

    @GET
    @Path("{taxGroupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Tax Group", description = "Retrieve Tax Group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TaxGroupApiResourceSwagger.GetTaxesGroupResponse.class))) })
    public TaxGroupData retrieveTaxGroup(@PathParam("taxGroupId") @Parameter(description = "taxGroupId") final Long taxGroupId,
            @Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return settings.isTemplate() ? readPlatformService.retrieveTaxGroupWithTemplate(taxGroupId)
                : readPlatformService.retrieveTaxGroupData(taxGroupId);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public TaxGroupData retrieveTemplate() {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return readPlatformService.retrieveTaxGroupTemplate();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new Tax Group", description = "Create a new Tax Group\n" + "Mandatory Fields: name and taxComponents\n"
            + "Mandatory Fields in taxComponents: taxComponentId\n" + "Optional Fields in taxComponents: id, startDate and endDate")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TaxGroupApiResourceSwagger.PostTaxesGroupRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TaxGroupApiResourceSwagger.PostTaxesGroupResponse.class))) })
    public CommandProcessingResult createTaxGroup(@Parameter(hidden = true) TaxGroupRequest taxGroupRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createTaxGroup()
                .withJson(toApiJsonSerializer.serialize(taxGroupRequest)).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("{taxGroupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Tax Group", description = "Updates Tax Group. Only end date can be up-datable and can insert new tax components.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TaxGroupApiResourceSwagger.PutTaxesGroupTaxGroupIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TaxGroupApiResourceSwagger.PutTaxesGroupTaxGroupIdResponse.class))) })
    public CommandProcessingResult updateTaxGroup(@PathParam("taxGroupId") @Parameter(description = "taxGroupId") final Long taxGroupId,
            @Parameter(hidden = true) TaxGroupRequest taxGroupRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTaxGroup(taxGroupId)
                .withJson(toApiJsonSerializer.serialize(taxGroupRequest)).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

}
