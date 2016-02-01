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
package org.apache.fineract.infrastructure.survey.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.survey.data.LikeliHoodPovertyLineData;
import org.apache.fineract.infrastructure.survey.data.PpiPovertyLineData;
import org.apache.fineract.infrastructure.survey.service.PovertyLineService;
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
