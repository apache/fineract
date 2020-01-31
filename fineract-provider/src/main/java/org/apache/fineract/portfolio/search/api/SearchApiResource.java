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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.Collection;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.portfolio.search.SearchConstants.SEARCH_RESPONSE_PARAMETERS;
import org.apache.fineract.portfolio.search.data.AdHocQueryDataValidator;
import org.apache.fineract.portfolio.search.data.AdHocQuerySearchConditions;
import org.apache.fineract.portfolio.search.data.AdHocSearchQueryData;
import org.apache.fineract.portfolio.search.data.SearchConditions;
import org.apache.fineract.portfolio.search.data.SearchData;
import org.apache.fineract.portfolio.search.service.SearchReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/search")
@Component
@Scope("singleton")
@Api(tags = {"Search API"})
@SwaggerDefinition(tags = {
        @Tag(name = "Search API", description = "Search API allows to search scoped resources clients, loans and groups on specified fields.")
})
public class SearchApiResource {

    private final Set<String> searchResponseParameters = SEARCH_RESPONSE_PARAMETERS.getAllValues();

    private final SearchReadPlatformService searchReadPlatformService;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final AdHocQueryDataValidator fromApiJsonDeserializer;

    @Autowired
    public SearchApiResource(final SearchReadPlatformService searchReadPlatformService,
            final ToApiJsonSerializer<Object> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final AdHocQueryDataValidator fromApiJsonDeserializer) {

        this.searchReadPlatformService = searchReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;

    }

    @GET
    @Path("/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrive Adhoc Search query template", httpMethod = "GET", notes = "Mandatory Fields\n" + "\n" + "search?query=000000001\n")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = SearchApiResourceSwagger.GetSearchResponse.class)})
    public String retrieveAdHocSearchQueryTemplate(@Context final UriInfo uriInfo) {

        final AdHocSearchQueryData templateData = this.searchReadPlatformService.retrieveAdHocQueryTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, templateData);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Search Resources", notes = "Example Requests:\n" + "\n" + "search?query=000000001\n" + "\n" + "\n" + "search?query=Petra&resource=clients,groups\n" + "\n" + "\n" + "search?query=Petra&resource=clients,groups&exactMatch=true")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = SearchApiResourceSwagger.GetSearchResponse.class)})
    public String searchData(@Context final UriInfo uriInfo, @QueryParam("query")  @ApiParam(value = "query")final String query,
            @QueryParam("resource")  @ApiParam(value = "resource") final String resource ,@DefaultValue("false") @QueryParam("exactMatch")  @ApiParam(value = "exactMatch")  Boolean exactMatch) {

        final SearchConditions searchConditions = new SearchConditions(query, resource,exactMatch);

        final Collection<SearchData> searchResults = this.searchReadPlatformService.retriveMatchingData(searchConditions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, searchResults, this.searchResponseParameters);
    }

    @POST
    @Path("/advance")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Adhoc query search", notes = "AdHocQuery search has more search options, it is a POST request, it uses request body to send search parameters\n" + "\n" + "\n" + "Mandatory fields:" + "entities" + "\n" + "\n" + "Optional fields:" + "loanStatus, loanProducts, offices, loanDateOption, loanFromDate, loanToDate, \n" +
            "includeOutStandingAmountPercentage, outStandingAmountPercentageCondition, \n" +
            "minOutStandingAmountPercentage and maxOutStandingAmountPercentage OR outStandingAmountPercentage, \n" +
            "includeOutstandingAmount, outstandingAmountCondition, \n" +
            "minOutstandingAmount and maxOutstandingAmount OR outstandingAmount" )
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = SearchApiResourceSwagger.PostAdhocQuerySearchRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = SearchApiResourceSwagger.PostAdhocQuerySearchResponse.class)})
    public String advancedSearch(@Context final UriInfo uriInfo, final String json) {

        final AdHocQuerySearchConditions searchConditions = this.fromApiJsonDeserializer.retrieveSearchConditions(json);

        final Collection<AdHocSearchQueryData> searchResults = this.searchReadPlatformService
                .retrieveAdHocQueryMatchingData(searchConditions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, searchResults);
    }
}