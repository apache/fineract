/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants;
import org.mifosplatform.portfolio.collectionsheet.data.IndividualCollectionSheetData;
import org.mifosplatform.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Path("/collectionsheet")
@Component
@Scope("singleton")
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
    public String generateCollectionSheet(@QueryParam("command") final String commandParam, final String apiRequestBodyAsJson,
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
