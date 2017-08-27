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
package org.apache.fineract.organisation.provisioning.api;

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

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.provisioning.constants.ProvisioningCriteriaConstants;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaData;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/provisioningcriteria")
@Component
@Scope("singleton")
@Api(value = "Provisioning Criteria", description = "This defines the Provisioning Criteria")
public class ProvisioningCriteriaApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<ProvisioningCriteriaData> toApiJsonSerializer;

	private static final Set<String> PROVISIONING_CRITERIA_TEMPLATE_PARAMETER = new HashSet<>(
			Arrays.asList(ProvisioningCriteriaConstants.DEFINITIONS_PARAM,
					ProvisioningCriteriaConstants.LOANPRODUCTS_PARAM, ProvisioningCriteriaConstants.GLACCOUNTS_PARAM));

	private static final Set<String> PROVISIONING_CRITERIA_PARAMETERS = new HashSet<>(
			Arrays.asList(ProvisioningCriteriaConstants.CRITERIA_PARAM,
					ProvisioningCriteriaConstants.LOANPRODUCTS_PARAM, ProvisioningCriteriaConstants.DEFINITIONS_PARAM));

	private static final Set<String> ALL_PROVISIONING_CRITERIA_PARAMETERS = new HashSet<>(
			Arrays.asList(ProvisioningCriteriaConstants.CRITERIA_ID_PARAM,
					ProvisioningCriteriaConstants.CRITERIA_NAME_PARAM, ProvisioningCriteriaConstants.CREATED_BY_PARAM));

    @Autowired
    public ProvisioningCriteriaApiResource(final PlatformSecurityContext platformSecurityContext,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<ProvisioningCriteriaData> toApiJsonSerializer) {
        this.platformSecurityContext = platformSecurityContext;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.provisioningCriteriaReadPlatformService = provisioningCriteriaReadPlatformService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService ;
        this.toApiJsonSerializer = toApiJsonSerializer ;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        ProvisioningCriteriaData data = this.provisioningCriteriaReadPlatformService.retrievePrivisiongCriteriaTemplate();
        return this.toApiJsonSerializer.serialize(settings, data, PROVISIONING_CRITERIA_TEMPLATE_PARAMETER);
    }
    
    @GET
    @Path("{criteriaId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieves a Provisioning Criteria", notes = "Retrieves a Provisioning Criteria")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ProvisioningCriteriaApiResourceSwagger.GetProvisioningCriteriaCriteriaIdResponse.class)})
    public String retrieveProvisioningCriteria(@PathParam("criteriaId") @ApiParam(value = "criteriaId") final Long criteriaId, @Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser() ;
        ProvisioningCriteriaData criteria = this.provisioningCriteriaReadPlatformService.retrieveProvisioningCriteria(criteriaId) ;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if(settings.isTemplate()) {
            criteria = this.provisioningCriteriaReadPlatformService.retrievePrivisiongCriteriaTemplate(criteria);   
        }
        return this.toApiJsonSerializer.serialize(settings, criteria, PROVISIONING_CRITERIA_PARAMETERS);
    }
        
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieves all created Provisioning Criterias", notes = "Retrieves all created Provisioning Criterias")
    @ApiResponses({@ApiResponse(code = 200, message = "", responseContainer = "List",response = ProvisioningCriteriaApiResourceSwagger.GetProvisioningCriteriaResponse.class)})
    public String retrieveAllProvisioningCriterias(@Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser() ;
        Collection<ProvisioningCriteriaData> data = this.provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias() ;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, data, ALL_PROVISIONING_CRITERIA_PARAMETERS);
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a new Provisioning Criteria", notes = "Creates a new Provisioning Criteria\n" + "\n" + "Mandatory Fields: \n" + "criteriaName\n" + "provisioningcriteria\n" + "\n" + "Optional Fields: \n" + "loanProducts")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ProvisioningCriteriaApiResourceSwagger.PostProvisioningCriteriaRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ProvisioningCriteriaApiResourceSwagger.PostProvisioningCriteriaResponse.class)})
    public String createProvisioningCriteria(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        commandWrapper = new CommandWrapperBuilder().createProvisioningCriteria().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }
    
    @PUT
    @Path("{criteriaId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Updates a new Provisioning Criteria", notes = "Updates a new Provisioning Criteria\n" + "\n" + "Optional Fields\n" + "criteriaName, loanProducts, provisioningcriteria")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ProvisioningCriteriaApiResourceSwagger.PutProvisioningCriteriaRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ProvisioningCriteriaApiResourceSwagger.PutProvisioningCriteriaResponse.class)})
    public String updateProvisioningCriteria(@PathParam("criteriaId") @ApiParam(value = "criteriaId") final Long criteriaId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateProvisioningCriteria(criteriaId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
    
    @DELETE
    @Path("{criteriaId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Deletes Provisioning Criteria", notes = "Deletes Provisioning Criteria")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ProvisioningCriteriaApiResourceSwagger.DeleteProvisioningCriteriaResponse.class)})
    public String deleteProvisioningCriteria(@PathParam("criteriaId") @ApiParam(value = "criteriaId") final Long criteriaId) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteProvisioningCriteria(criteriaId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
