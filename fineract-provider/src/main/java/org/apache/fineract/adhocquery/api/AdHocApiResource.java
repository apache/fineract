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
package org.apache.fineract.adhocquery.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.adhocquery.data.AdHocData;
import org.apache.fineract.adhocquery.data.AdHocRequest;
import org.apache.fineract.adhocquery.service.AdHocReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/adhocquery")
@Component
@Tag(name = "AdhocQuery Api", description = "")
@RequiredArgsConstructor
public class AdHocApiResource {

    /**
     * The set of parameters that are supported in response for {@link AdhocData}
     */
    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "query", "tableName",
            "tableField", "isActive", "createdBy", "createdOn", "createdById", "updatedById", "updatedOn", "email"));

    private final PlatformSecurityContext context;
    private final AdHocReadPlatformService adHocReadPlatformService;
    private final DefaultToApiJsonSerializer<AdHocData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public List<AdHocData> retrieveAll() {

        this.context.authenticatedUser();
        return adHocReadPlatformService.retrieveAllAdHocQuery();
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("template")
    public AdHocData template() {
        this.context.authenticatedUser();
        return adHocReadPlatformService.retrieveNewAdHocDetails();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult createAdHocQuery(final AdHocRequest adHocRequest) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createAdHoc()
                .withJson(toApiJsonSerializer.serialize(adHocRequest)).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("{adHocId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public AdHocData retrieveAdHocQuery(@PathParam("adHocId") @Parameter(description = "adHocId") final Long adHocId) {

        this.context.authenticatedUser();

        return adHocReadPlatformService.retrieveOne(adHocId);
    }

    @PUT
    @Path("{adHocId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult update(@PathParam("adHocId") @Parameter(description = "adHocId") final Long adHocId,
            final AdHocRequest adHocRequest) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateAdHoc(adHocId)
                .withJson(toApiJsonSerializer.serialize(adHocRequest)).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    /**
     * Delete AdHocQuery
     *
     * @param adHocId
     * @return
     */
    @DELETE
    @Path("{adHocId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CommandProcessingResult deleteAdHocQuery(@PathParam("adHocId") @Parameter(description = "adHocId") final Long adHocId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteAdHoc(adHocId).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

}
