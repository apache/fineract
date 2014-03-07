package org.mifosplatform.infrastructure.survey.api;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.survey.data.LikelihoodData;
import org.mifosplatform.infrastructure.survey.service.ReadLikelihoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Cieyou on 3/12/14.
 */
@Path("/likelihood")
@Component
@Scope("singleton")
public class LikelihoodApiResource {


    private final static Logger logger = LoggerFactory.getLogger(PovertyLineApiResource.class);
    private final DefaultToApiJsonSerializer<LikelihoodData> toApiJsonSerializer;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ReadLikelihoodService readService;


    @Autowired
    LikelihoodApiResource(final PlatformSecurityContext context,
                           final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                           final DefaultToApiJsonSerializer<LikelihoodData> toApiJsonSerializer,
                           final ReadLikelihoodService readService) {

        this.context = context;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.readService = readService;


    }

    @GET
    @Path("{ppiName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@PathParam("ppiName") final String ppiName){

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        List<LikelihoodData> likelihoodData = this.readService.retrieveAll(ppiName);
        return this.toApiJsonSerializer.serialize(likelihoodData);

    }

    @GET
    @Path("{ppiName}/{likelihoodId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieve(@PathParam("likelihoodId") final Long likelihoodId){

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        LikelihoodData likelihoodData = this.readService.retrieve(likelihoodId);
        return this.toApiJsonSerializer.serialize(likelihoodData);

    }

    @PUT
    @Path("{likelihoodId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("likelihoodId") final Long likelihoodId, final String apiRequestBodyAsJson){

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateLikelihood(likelihoodId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }
}
