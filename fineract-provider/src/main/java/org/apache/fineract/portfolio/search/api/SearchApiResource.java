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
package org.apache.fineract.portfolio.search.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.search.data.AdHocQuerySearchRequest;
import org.apache.fineract.portfolio.search.data.AdHocSearchQueryData;
import org.apache.fineract.portfolio.search.data.SearchConditions;
import org.apache.fineract.portfolio.search.data.SearchData;
import org.apache.fineract.portfolio.search.service.SearchReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Path("/v1/search")
@Component
@Tag(name = "Search API", description = "Search API allows to search scoped resources clients, loans and groups on specified fields.")
@RequiredArgsConstructor
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class SearchApiResource {

    private final SearchReadService searchReadService;

    @GET
    @Path("/template")
    @Operation(summary = "Retrive Adhoc Search query template", description = """
            Mandatory Fields

            search?query=000000001
            """)
    public AdHocSearchQueryData retrieveAdHocSearchQueryTemplate() {

        return searchReadService.retrieveAdHocQueryTemplate();
    }

    @GET
    @Operation(summary = "Search Resources", description = """
            Example Requests:

            search?query=000000001


            search?query=Petra&resource=clients,groups


            search?query=Petra&resource=clients,groups&exactMatch=true""")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SearchApiResourceSwagger.GetSearchResponse.class))))
    public List<SearchData> searchData(@QueryParam("query") @Parameter(description = "query") final String query,
            @QueryParam("resource") @Parameter(description = "resource") final String resource,
            @DefaultValue("false") @QueryParam("exactMatch") @Parameter(description = "exactMatch") Boolean exactMatch) {

        final AppUser currentUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final SearchConditions searchConditions = new SearchConditions(query, resource, exactMatch, hierarchy);

        return searchReadService.retriveMatchingData(searchConditions);
    }

    @POST
    @Path("/advance")
    @Operation(summary = "Adhoc query search", description = """
            AdHocQuery search has more search options, it is a POST request, \
            it uses request body to send search parameters


            Mandatory fields: entities

            Optional fields: \
            loanStatus, loanProducts, offices, loanDateOption, loanFromDate, loanToDate,
            includeOutStandingAmountPercentage, outStandingAmountPercentageCondition,
            minOutStandingAmountPercentage and maxOutStandingAmountPercentage OR outStandingAmountPercentage,
            includeOutstandingAmount, outstandingAmountCondition,
            minOutstandingAmount and maxOutstandingAmount OR outstandingAmount""")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SearchApiResourceSwagger.PostAdhocQuerySearchRequest.class)))
    public List<AdHocSearchQueryData> advancedSearch(final AdHocQuerySearchRequest request) {

        return searchReadService.retrieveAdHocQueryMatchingData(request);
    }
}
