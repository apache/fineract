/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.survey.data.LikeliHoodPovertyLineData;
import org.mifosplatform.infrastructure.survey.data.PpiPovertyLineData;
import org.mifosplatform.infrastructure.survey.service.PovertyLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/povertyLine")
@Component
@Scope("singleton")
public class PovertyLineApiResource {

    private final DefaultToApiJsonSerializer<PpiPovertyLineData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LikeliHoodPovertyLineData> likelihoodToApiJsonSerializer;
    // private final DefaultToApiJsonSerializer<PpiPovertyLineData>
    // toApiJsonSerializer;
    private final PlatformSecurityContext context;
    private final PovertyLineService readService;

    @Autowired
    PovertyLineApiResource(final PlatformSecurityContext context, final DefaultToApiJsonSerializer<PpiPovertyLineData> toApiJsonSerializer,
            final PovertyLineService readService, final DefaultToApiJsonSerializer<LikeliHoodPovertyLineData> likelihoodToApiJsonSerializer) {

        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.readService = readService;
        this.likelihoodToApiJsonSerializer = likelihoodToApiJsonSerializer;

    }

    @GET
    @Path("{ppiName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@PathParam("ppiName") final String ppiName) {

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        PpiPovertyLineData povertyLine = this.readService.retrieveAll(ppiName);
        return this.toApiJsonSerializer.serialize(povertyLine);

    }

    @GET
    @Path("{ppiName}/{likelihoodId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@PathParam("ppiName") final String ppiName, @PathParam("likelihoodId") final Long likelihoodId) {

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        LikeliHoodPovertyLineData likeliHoodPovertyLineData = this.readService.retrieveForLikelihood(ppiName, likelihoodId);

        return this.likelihoodToApiJsonSerializer.serialize(likeliHoodPovertyLineData);

    }

}
