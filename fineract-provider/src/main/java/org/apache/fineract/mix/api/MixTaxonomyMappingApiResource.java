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
package org.apache.fineract.mix.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.mix.data.MixTaxonomyMappingData;
import org.apache.fineract.mix.data.MixTaxonomyRequest;
import org.apache.fineract.mix.service.MixTaxonomyMappingReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/mixmapping")
@Component
@Tag(name = "Mix Mapping", description = "")
@RequiredArgsConstructor
public class MixTaxonomyMappingApiResource {

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<MixTaxonomyMappingData> toApiJsonSerializer;
    private final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public MixTaxonomyMappingData retrieveTaxonomyMapping() {
        this.context.authenticatedUser();
        return this.readTaxonomyMappingService.retrieveTaxonomyMapping();
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateTaxonomyMapping(final MixTaxonomyRequest mixTaxonomyRequest) {
        // TODO support multiple configuration file loading
        final Long mappingId = (long) 1;
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTaxonomyMapping(mappingId)
                .withJson(toApiJsonSerializer.serialize(mixTaxonomyRequest)).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
