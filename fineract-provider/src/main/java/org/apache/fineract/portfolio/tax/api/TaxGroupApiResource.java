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
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.service.TaxReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Path("/taxes/group")
@Component
@Scope("singleton")
@Api(value = "Tax Group", description = "This defines the Tax Group")
public class TaxGroupApiResource {

    private final String resourceNameForPermissions = "TAXGROUP";

    private final PlatformSecurityContext context;
    private final TaxReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<TaxGroupData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public TaxGroupApiResource(final PlatformSecurityContext context, final TaxReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<TaxGroupData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
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
    @ApiOperation(value = "List Tax Group", httpMethod = "GET", notes = "List Tax Group")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = TaxGroupApiResourceSwagger.GetTaxesGroupResponse.class, responseContainer = "List")})
    public String retrieveAllTaxGroups(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<TaxGroupData> taxGroupDatas = this.readPlatformService.retrieveAllTaxGroups();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, taxGroupDatas);
    }

    @GET
    @Path("{taxGroupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Tax Group", httpMethod = "GET", notes = "Retrieve Tax Group")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = TaxGroupApiResourceSwagger.GetTaxesGroupResponse.class)})
    public String retrieveTaxGroup(@PathParam("taxGroupId") @ApiParam(value = "taxGroupId") final Long taxGroupId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        TaxGroupData taxGroupData = null;
        if (settings.isTemplate()) {
            taxGroupData = this.readPlatformService.retrieveTaxGroupWithTemplate(taxGroupId);
        } else {
            taxGroupData = this.readPlatformService.retrieveTaxGroupData(taxGroupId);
        }
        return this.toApiJsonSerializer.serialize(settings, taxGroupData);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final TaxGroupData taxGroupData = this.readPlatformService.retrieveTaxGroupTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, taxGroupData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a new Tax Group", httpMethod = "POST", notes = "Create a new Tax Group\n" + "Mandatory Fields: name and taxComponents\n" + "Mandatory Fields in taxComponents: taxComponentId\n" + "Optional Fields in taxComponents: id, startDate and endDate")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = TaxGroupApiResourceSwagger.PostTaxesGroupRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = TaxGroupApiResourceSwagger.PostTaxesGroupResponse.class)})
    public String createTaxGroup(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createTaxGroup().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{taxGroupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update Tax Group", httpMethod = "PUT", notes = "Updates Tax Group. Only end date can be up-datable and can insert new tax components.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = TaxGroupApiResourceSwagger.PutTaxesGroupTaxGroupIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = TaxGroupApiResourceSwagger.PutTaxesGroupTaxGroupIdResponse.class)})
    public String updateTaxGroup(@PathParam("taxGroupId") @ApiParam(value = "taxGroupId") final Long taxGroupId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTaxGroup(taxGroupId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}