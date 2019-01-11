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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.mix.data.MixTaxonomyMappingData;
import org.apache.fineract.mix.service.MixTaxonomyMappingReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/mixmapping")
@Component
@Scope("singleton")
@Api(value = "Mix Mapping")
public class MixTaxonomyMappingApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("identifier", "config"));

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<MixTaxonomyMappingData> toApiJsonSerializer;
    private final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public MixTaxonomyMappingApiResource(final PlatformSecurityContext context,
            final ToApiJsonSerializer<MixTaxonomyMappingData> toApiJsonSerializer,
            final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {

        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.readTaxonomyMappingService = readTaxonomyMappingService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTaxonomyMapping(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser();
        final MixTaxonomyMappingData mappingData = this.readTaxonomyMappingService.retrieveTaxonomyMapping();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, mappingData, this.RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateTaxonomyMapping(final String jsonRequestBody) {
        // TODO support multiple configuration file loading
        final Long mappingId = (long) 1;
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTaxonomyMapping(mappingId).withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
