/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.api;

import java.util.Collection;

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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.provisioning.constants.ProvisioningCriteriaConstants;
import org.mifosplatform.organisation.provisioning.data.ProvisioningCriteriaData;
import org.mifosplatform.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/provisioningcriteria")
@Component
@Scope("singleton")
public class ProvisioningCriteriaApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<ProvisioningCriteriaData> toApiJsonSerializer;
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
        return this.toApiJsonSerializer.serialize(settings, data, ProvisioningCriteriaConstants.PROVISIONING_CRITERIA_TEMPLATE_PARAMETER);
    }
    
    @GET
    @Path("{criteriaId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveProvisioningCriteria(@PathParam("criteriaId") final Long criteriaId, @Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser() ;
        ProvisioningCriteriaData criteria = this.provisioningCriteriaReadPlatformService.retrieveProvisioningCriteria(criteriaId) ;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if(settings.isTemplate()) {
            criteria = this.provisioningCriteriaReadPlatformService.retrievePrivisiongCriteriaTemplate(criteria);   
        }
        return this.toApiJsonSerializer.serialize(settings, criteria, ProvisioningCriteriaConstants.PROVISIONING_CRITERIA_PARAMETERS); 
    }
        
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllProvisioningCriterias(@Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser() ;
        Collection<ProvisioningCriteriaData> data = this.provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias() ;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, data, ProvisioningCriteriaConstants.ALL_PROVISIONING_CRITERIA_PARAMETERS); 
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createProvisioningCriteria(final String apiRequestBodyAsJson) {
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
    public String updateProvisioningCriteria(@PathParam("criteriaId") final Long criteriaId, final String apiRequestBodyAsJson) {
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
    public String deleteProvisioningCriteria(@PathParam("criteriaId") final Long criteriaId) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteProvisioningCriteria(criteriaId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
