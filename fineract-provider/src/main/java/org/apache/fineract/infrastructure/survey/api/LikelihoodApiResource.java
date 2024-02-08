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

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.survey.data.LikelihoodData;
import org.apache.fineract.infrastructure.survey.service.ReadLikelihoodService;
import org.springframework.stereotype.Component;

/**
 * Created by Cieyou on 3/12/14.
 */
@Path("/v1/likelihood")
@Component
@Tag(name = "Likelihood", description = "")
@RequiredArgsConstructor
public class LikelihoodApiResource {

    private final DefaultToApiJsonSerializer<LikelihoodData> toApiJsonSerializer;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ReadLikelihoodService readService;

    @GET
    @Path("{ppiName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@PathParam("ppiName") final String ppiName) {

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        List<LikelihoodData> likelihoodData = this.readService.retrieveAll(ppiName);
        return this.toApiJsonSerializer.serialize(likelihoodData);

    }

    @GET
    @Path("{ppiName}/{likelihoodId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieve(@PathParam("likelihoodId") final Long likelihoodId, @PathParam("ppiName") final String ppiName) {

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        LikelihoodData likelihoodData = this.readService.retrieve(likelihoodId);
        return this.toApiJsonSerializer.serialize(likelihoodData);

    }

    @PUT
    @Path("{ppiName}/{likelihoodId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("likelihoodId") final Long likelihoodId, final String apiRequestBodyAsJson,
            @PathParam("ppiName") final String ppiName) {

        this.context.authenticatedUser().validateHasReadPermission(PovertyLineApiConstants.POVERTY_LINE_RESOURCE_NAME);

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateLikelihood(likelihoodId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }
}
