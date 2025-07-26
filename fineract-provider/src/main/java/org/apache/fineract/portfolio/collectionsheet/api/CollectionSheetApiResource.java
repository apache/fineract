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

import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.GENERATE_COLLECTION_SHEET_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.SAVE_COLLECTION_SHEET_COMMAND_VALUE;

import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants;
import org.apache.fineract.portfolio.collectionsheet.data.CollectionSheetRequest;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/collectionsheet")
@Component
@Tag(name = "Collection Sheet", description = "")
@RequiredArgsConstructor
public class CollectionSheetApiResource {

    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final FromJsonHelper fromJsonHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Generate Individual Collection Sheet | Save Collection Sheet", description = "Generate Individual Collection Sheet:\n\n"
            + "This Api retrieves repayment details of all individual loans under a office as on a specified meeting date.\n\n"
            + "Save Collection Sheet:\n\n"
            + "This Api allows the loan officer to perform bulk repayments of individual loans and deposit of mandatory savings on a given meeting date.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CollectionSheetRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollectionSheetApiResourceSwagger.PostCollectionSheetResponse.class))) })
    public Response generateCollectionSheet(@QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) CollectionSheetRequest collectionSheetRequest) {
        final String payload = toApiJsonSerializer.serialize(collectionSheetRequest);
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(payload);

        if (CommandParameterUtil.is(commandParam, GENERATE_COLLECTION_SHEET_COMMAND_VALUE)) {
            this.context.authenticatedUser().validateHasReadPermission(CollectionSheetConstants.COLLECTIONSHEET_RESOURCE_NAME);
            final JsonElement parsedQuery = this.fromJsonHelper.parse(payload);
            final JsonQuery query = JsonQuery.from(payload, parsedQuery, this.fromJsonHelper);
            return Response.ok(this.collectionSheetReadPlatformService.generateIndividualCollectionSheet(query)).build();
        } else if (CommandParameterUtil.is(commandParam, SAVE_COLLECTION_SHEET_COMMAND_VALUE)) {
            final CommandWrapper commandRequest = builder.saveIndividualCollectionSheet().build();
            return Response.ok(this.commandsSourceWritePlatformService.logCommandSource(commandRequest)).build();
        }
        return Response.ok().build();
    }
}
