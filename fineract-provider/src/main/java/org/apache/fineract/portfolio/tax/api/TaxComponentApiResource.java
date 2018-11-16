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

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.service.TaxReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Path("/taxes/component")
@Component
@Scope("singleton")
@Api(value = "Tax Components", description = "This defines the Tax Components")
public class TaxComponentApiResource {

    private final String resourceNameForPermissions = "TAXCOMPONENT";

    private final PlatformSecurityContext context;
    private final TaxReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<TaxComponentData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public TaxComponentApiResource(final PlatformSecurityContext context, final TaxReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<TaxComponentData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Tax Components", httpMethod = "GET", notes = "List Tax Components")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = TaxComponentApiResourceSwagger.GetTaxesComponentsResponse.class, responseContainer = "List")})
    public String retrieveAllTaxComponents(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<TaxComponentData> TaxComponents = this.readPlatformService.retrieveAllTaxComponents();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, TaxComponents);
    }

    @GET
    @Path("{taxComponentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Tax Component", httpMethod = "GET", notes = "Retrieve Tax Component")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = TaxComponentApiResourceSwagger.GetTaxesComponentsResponse.class)})
    public String retrieveTaxComponent(@PathParam("taxComponentId") @ApiParam(value = "taxComponentId") final Long taxComponentId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        TaxComponentData taxComponentData = this.readPlatformService.retrieveTaxComponentData(taxComponentId);
        return this.toApiJsonSerializer.serialize(settings, taxComponentData);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final TaxComponentData taxComponentData = this.readPlatformService.retrieveTaxComponentTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, taxComponentData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a new Tax Component", httpMethod = "POST", notes = "Creates a new Tax Component\n\n" + "Mandatory Fields: name, percentage\n\n" + "Optional Fields: debitAccountType, debitAcountId, creditAccountType, creditAcountId, startDate")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = TaxComponentApiResourceSwagger.PostTaxesComponentsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = TaxComponentApiResourceSwagger.PostTaxesComponentsResponse.class)})
    public String createTaxCompoent(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createTaxComponent().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{taxComponentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update Tax Component", httpMethod = "PUT", notes = "Updates Tax component. Debit and credit account details cannot be modified. All the future tax components would be replaced with the new percentage.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body",dataTypeClass = TaxComponentApiResourceSwagger.PutTaxesComponentsTaxComponentIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = TaxComponentApiResourceSwagger.PutTaxesComponentsTaxComponentIdResponse.class)})
    public String updateTaxCompoent(@PathParam("taxComponentId") @ApiParam(value = "taxComponentId") final Long taxComponentId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTaxComponent(taxComponentId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}