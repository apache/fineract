/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search.api;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.portfolio.search.SearchConstants.SEARCH_RESPONSE_PARAMETERS;
import org.mifosplatform.portfolio.search.data.SearchConditions;
import org.mifosplatform.portfolio.search.data.SearchData;
import org.mifosplatform.portfolio.search.service.SearchReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/search")
@Component
@Scope("singleton")
public class SearchApiResource {

    private final Set<String> searchResponseParameters = SEARCH_RESPONSE_PARAMETERS.getAllValues();

    private final SearchReadPlatformService searchReadPlatformService;
    private final ToApiJsonSerializer<SearchData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public SearchApiResource(final SearchReadPlatformService searchReadPlatformService,
            final ToApiJsonSerializer<SearchData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper) {

        this.searchReadPlatformService = searchReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String searchData(@Context final UriInfo uriInfo, @QueryParam("query") final String query,
            @QueryParam("resource") final String resource) {

        final SearchConditions searchConditions = new SearchConditions(query, resource);

        final Collection<SearchData> searchResults = this.searchReadPlatformService.retriveMatchingData(searchConditions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, searchResults, this.searchResponseParameters);
    }
}