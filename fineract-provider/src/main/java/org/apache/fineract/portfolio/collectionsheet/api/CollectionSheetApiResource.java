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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.collectionsheet.command.GenerateCollectionSheetCommand;
import org.apache.fineract.portfolio.collectionsheet.command.SaveCollectionSheetCommand;
import org.apache.fineract.portfolio.collectionsheet.data.CollectionSheetCommandParameter;
import org.apache.fineract.portfolio.collectionsheet.data.CollectionSheetRequest;
import org.apache.fineract.portfolio.collectionsheet.data.GenerateCollectionSheetRequest;
import org.apache.fineract.portfolio.collectionsheet.data.IndividualCollectionSheetData;
import org.apache.fineract.portfolio.collectionsheet.data.SaveCollectionSheetRequest;
import org.springframework.stereotype.Component;

@Path("/v1/collectionsheet")
@Component
@Tag(name = "Collection Sheet", description = "Collection Sheet")
@RequiredArgsConstructor
public class CollectionSheetApiResource {

    private final CommandPipeline commandPipeline;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Generate Individual Collection Sheet | Save Collection Sheet", description = "Generate Individual Collection Sheet:\n\n"
            + "This Api retrieves repayment details of all individual loans under a office as on a specified meeting date.\n\n"
            + "Save Collection Sheet:\n\n"
            + "This Api allows the loan officer to perform bulk repayments of individual loans and deposit of mandatory savings on a given meeting date.")
    public Object generateCollectionSheet(@QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) CollectionSheetRequest collectionSheetRequest) {

        /**
         * Comment no: 1
         *
         * In the comments is the original logic.
         * Deleted CollectionSheetApiResourcesSwagger.java 
         **/

        // final String payload = toApiJsonSerializer.serialize(collectionSheetRequest);
        // final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(payload);
        //
        // if (CommandParameterUtil.is(commandParam, GENERATE_COLLECTION_SHEET_COMMAND_VALUE)) {
        // this.context.authenticatedUser().validateHasReadPermission(CollectionSheetConstants.COLLECTIONSHEET_RESOURCE_NAME);
        // final JsonElement parsedQuery = this.fromJsonHelper.parse(payload);
        // final JsonQuery query = JsonQuery.from(payload, parsedQuery, this.fromJsonHelper);
        // return Response.ok(this.collectionSheetReadPlatformService.generateIndividualCollectionSheet(query)).build();
        // } else if (CommandParameterUtil.is(commandParam, SAVE_COLLECTION_SHEET_COMMAND_VALUE)) {
        // final CommandWrapper commandRequest = builder.saveIndividualCollectionSheet().build();
        // return Response.ok(this.commandsSourceWritePlatformService.logCommandSource(commandRequest)).build();
        // }
        // return Response.ok().build();

        /**
         * Comment No: 2
         *
         * This Api accepts request parameters and based on parameter values : a) generateCollectionSheet - Sends a
         * GenerateCollectionSheetCommand command b) saveCollectionSheet - Sends a SaveCollectionSheetCommand.
         *
         *
         * The request body CollectionSheetRequest collectionSheetRequest is common, but since the handlers are selected
         * based on the generic parameters, this has to have a wrapper
         *
         * Added 2 wrapper classes which wrap the CollectionSheetRequest collectionSheetRequest a)
         * GenerateCollectionSheetRequest & b) SaveCollectionSheetRequest
         *
         *
         * Also, this API does not have any integration and unit tests associated with it.
         *
         *
         *
         */

        if (commandParam.equalsIgnoreCase(CollectionSheetCommandParameter.GENERATE_COLLECTION_SHEET.getValue())) {
            final GenerateCollectionSheetCommand command = new GenerateCollectionSheetCommand();
            final GenerateCollectionSheetRequest payload = new GenerateCollectionSheetRequest();
            payload.setRequest(collectionSheetRequest);

            command.setId(UUID.randomUUID());
            command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
            command.setPayload(payload);
            final Supplier<IndividualCollectionSheetData> response = commandPipeline.send(command);

            return response.get();
        } else if (commandParam.equalsIgnoreCase(CollectionSheetCommandParameter.SAVE_COLLECTION_SHEET.getValue())) {
            final SaveCollectionSheetCommand command = new SaveCollectionSheetCommand();
            final SaveCollectionSheetRequest payload = new SaveCollectionSheetRequest();
            payload.setRequest(collectionSheetRequest);

            command.setId(UUID.randomUUID());
            command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
            command.setPayload(payload);
            final Supplier<CommandProcessingResult> response = commandPipeline.send(command);

            return response.get();

        } else {
            /**
             *
             * TODO Custom Exception Class to be Created
             *
             */
            throw new RuntimeException("Error: Parameter Name not Matching");
        }

    }
}
