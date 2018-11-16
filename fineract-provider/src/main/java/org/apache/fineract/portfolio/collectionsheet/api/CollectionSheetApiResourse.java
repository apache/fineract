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
package org.apache.fineract.portfolio.collectionsheet.api;

import com.google.gson.JsonElement;
import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants;
import org.apache.fineract.portfolio.collectionsheet.data.IndividualCollectionSheetData;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/collectionsheet")
@Component
@Scope("singleton")
@Api(value = "Collection Sheet", description = "")
public class CollectionSheetApiResourse {

    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final FromJsonHelper fromJsonHelper;
    private final ApiRequestParameterHelper apiRequestPrameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;

    @Autowired
    public CollectionSheetApiResourse(final CollectionSheetReadPlatformService collectionSheetReadPlatformService,
            final ToApiJsonSerializer<Object> toApiJsonSerializer, final FromJsonHelper fromJsonHelper,
            final ApiRequestParameterHelper apiRequestPrameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, final PlatformSecurityContext context) {
        this.collectionSheetReadPlatformService = collectionSheetReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.fromJsonHelper = fromJsonHelper;
        this.apiRequestPrameterHelper = apiRequestPrameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.context = context;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Generate Individual Collection Sheet | Save Collection Sheet", httpMethod = "POST", notes = "Generate Individual Collection Sheet:\n\n" + "This Api retrieves repayment details of all individual loans under a office as on a specified meeting date.\n\n" + "Save Collection Sheet:\n\n" + "This Api allows the loan officer to perform bulk repayments of individual loans and deposit of mandatory savings on a given meeting date.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = CollectionSheetApiResourceSwagger.PostCollectionSheetRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CollectionSheetApiResourceSwagger.PostCollectionSheetResponse.class)})
    public String generateCollectionSheet(@QueryParam("command") @ApiParam(value = "command") final String commandParam, @ApiParam(hidden = true) final String apiRequestBodyAsJson,
                                          @Context final UriInfo uriInfo) {
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandProcessingResult result = null;

        if (is(commandParam, "generateCollectionSheet")) {
            this.context.authenticatedUser().validateHasReadPermission(CollectionSheetConstants.COLLECTIONSHEET_RESOURCE_NAME);
            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
            final IndividualCollectionSheetData collectionSheet = this.collectionSheetReadPlatformService
                    .generateIndividualCollectionSheet(query);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestPrameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, collectionSheet);
        } else if (is(commandParam, "saveCollectionSheet")) {
            final CommandWrapper commandRequest = builder.saveIndividualCollectionSheet().build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        }
        return null;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
}
