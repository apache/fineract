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
package org.apache.fineract.infrastructure.cache.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.cache.data.CacheData;
import org.apache.fineract.infrastructure.cache.data.request.CacheRequest;
import org.apache.fineract.infrastructure.cache.service.RuntimeDelegatingCacheManager;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Path("/v1/caches")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Cache", description = "The following settings are possible for cache:\n" + "\n" + "No Caching: caching turned off\n"
        + "Single node: caching on for single instance deployments of platorm (works for multiple tenants but only one tomcat)\n"
        + "By default caching is set to No Caching. Switching between caches results in the cache been clear e.g. from Single node to No cache and back again would clear down the single node cache.")
@RequiredArgsConstructor
public class CacheApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "CACHE";

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<CacheData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Qualifier("runtimeDelegatingCacheManager")
    private final RuntimeDelegatingCacheManager cacheService;

    @GET
    @Operation(summary = "Retrieve Cache Types", description = "Returns the list of caches.\n" + "\n" + "Example Requests:\n" + "\n"
            + "caches")
    public Collection<CacheData> retrieveAll() {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return cacheService.retrieveAll();
    }

    @PUT
    @Operation(summary = "Switch Cache", description = "Switches the cache to chosen one.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CacheRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CacheApiResourceSwagger.PutCachesResponse.class))) })
    public CommandProcessingResult switchCache(@Parameter(hidden = true) CacheRequest cacheRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCache()
                .withJson(toApiJsonSerializer.serialize(cacheRequest)).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
