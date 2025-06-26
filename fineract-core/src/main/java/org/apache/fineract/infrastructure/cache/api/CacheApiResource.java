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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.cache.command.CacheSwitchCommand;
import org.apache.fineract.infrastructure.cache.data.CacheData;
import org.apache.fineract.infrastructure.cache.data.CacheSwitchRequest;
import org.apache.fineract.infrastructure.cache.data.CacheSwitchResponse;
import org.apache.fineract.infrastructure.cache.service.RuntimeDelegatingCacheManager;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Path("/v1/caches")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Cache", description = """
        The following settings are possible for cache:

        No Caching: caching turned off

        Single node: caching on for single instance deployments of platorm (works for multiple tenants but only one tomcat).
        By default caching is set to No Caching. Switching between caches results in the cache been clear e.g. from single
        node to no cache and back again would clear down the single node cache.
        """)
@RequiredArgsConstructor
public class CacheApiResource {

    @Qualifier("runtimeDelegatingCacheManager")
    private final RuntimeDelegatingCacheManager cacheService;
    private final CommandPipeline commandPipeline;

    @GET
    @Operation(summary = "Retrieve Cache Types", description = """
            Returns the list of caches.

            Example Requests:

            caches
            """)
    public Collection<CacheData> retrieveAll() {
        return cacheService.retrieveAll();
    }

    @PUT
    @Operation(summary = "Switch Cache", description = "Switches the cache to chosen one.")
    public CacheSwitchResponse switchCache(@Valid CacheSwitchRequest request) {
        final var command = new CacheSwitchCommand();

        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setPayload(request);

        final Supplier<CacheSwitchResponse> response = commandPipeline.send(command);

        return response.get();
    }
}
