package org.mifosplatform.infrastructure.survey.api;

import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.survey.data.LikeliHoodPovertyLineData;
import org.mifosplatform.infrastructure.survey.data.PovertyLineData;
import org.mifosplatform.infrastructure.survey.data.PpiPovertyLineData;
import org.mifosplatform.infrastructure.survey.service.PovertyLineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Created by Cieyou on 3/11/14.
 */
@Path("/povertyLine")
@Component
@Scope("singleton")
public class PovertyLineApiResource {

    private final static Logger logger = LoggerFactory.getLogger(PovertyLineApiResource.class);
    private final DefaultToApiJsonSerializer<PpiPovertyLineData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LikeliHoodPovertyLineData> likelihoodToApiJsonSerializer;
   // private final DefaultToApiJsonSerializer<PpiPovertyLineData> toApiJsonSerializer;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PovertyLineService readService;


    @Autowired
    PovertyLineApiResource(final PlatformSecurityContext context,
                           final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                           final DefaultToApiJsonSerializer<PpiPovertyLineData> toApiJsonSerializer,
                           final PovertyLineService readService,
                           final DefaultToApiJsonSerializer<LikeliHoodPovertyLineData> likelihoodToApiJsonSerializer) {

        this.context = context;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.readService = readService;
        this.likelihoodToApiJsonSerializer = likelihoodToApiJsonSerializer;

    }

    @GET
    @Path("{ppiName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@PathParam("ppiName") final String ppiName){

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        PpiPovertyLineData povertyLine = this.readService.retrieveAll(ppiName);
        return this.toApiJsonSerializer.serialize(povertyLine);

    }
    @GET
    @Path("{ppiName}/{likelihoodId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@PathParam("ppiName") final String ppiName,@PathParam("likelihoodId") final Long likelihoodId){

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        LikeliHoodPovertyLineData likeliHoodPovertyLineData  = this.readService.retrieveForLikelihood(ppiName, likelihoodId);

        return this.likelihoodToApiJsonSerializer.serialize(likeliHoodPovertyLineData);

    }



}
