/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.api;

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

import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.mix.data.MixTaxonomyData;
import org.mifosplatform.mix.service.MixTaxonomyReadPlatformService;
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