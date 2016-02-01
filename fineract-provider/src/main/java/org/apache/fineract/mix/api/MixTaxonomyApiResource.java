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
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.mix.data.MixTaxonomyData;
import org.apache.fineract.mix.service.MixTaxonomyReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/mixtaxonomy")
@Component
@Scope("singleton")
public class MixTaxonomyApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("taxonomyId", "name", "namespace", "dimension",
            "description"));

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<MixTaxonomyData> toApiJsonSerializer;
    private final MixTaxonomyReadPlatformService readTaxonomyService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public MixTaxonomyApiResource(final PlatformSecurityContext context, final ToApiJsonSerializer<MixTaxonomyData> toApiJsonSerializer,
            final MixTaxonomyReadPlatformService readTaxonomyService, final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.readTaxonomyService = readTaxonomyService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        // FIXME - KW - no check for permission to read mix taxonomy data.
        this.context.authenticatedUser();

        final List<MixTaxonomyData> taxonomyDatas = this.readTaxonomyService.retrieveAll();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, taxonomyDatas, this.RESPONSE_DATA_PARAMETERS);
    }
}