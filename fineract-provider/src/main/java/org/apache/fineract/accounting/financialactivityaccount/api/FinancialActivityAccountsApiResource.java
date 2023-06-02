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
package org.apache.fineract.accounting.financialactivityaccount.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.financialactivityaccount.data.FinancialActivityAccountData;
import org.apache.fineract.accounting.financialactivityaccount.service.FinancialActivityAccountReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/financialactivityaccounts")
@Component
@Tag(name = "Mapping Financial Activities to Accounts", description = "Organization Level Financial Activities like Asset and Liability Transfer can be mapped to GL Account. Integrated accounting takes these accounts into consideration when an Account transfer is made between a savings to loan/savings account and vice-versa\n"
        + "\n" + "Field Descriptions\n" + "financialActivityId\n" + "The identifier of the Financial Activity\n" + "glAccountId\n"
        + "The identifier of a GL Account ( Ledger Account) which shall be used as the default account for the selected Financial Activity")
@RequiredArgsConstructor
public class FinancialActivityAccountsApiResource {

    private final PlatformSecurityContext context;
    private final FinancialActivityAccountReadPlatformService financialActivityAccountReadPlatformService;
    private final DefaultToApiJsonSerializer<FinancialActivityAccountData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(FinancialActivityAccountsConstants.resourceNameForPermission);

        FinancialActivityAccountData financialActivityAccountData = this.financialActivityAccountReadPlatformService
                .getFinancialActivityAccountTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, financialActivityAccountData,
                FinancialActivityAccountsConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Financial Activities to Accounts Mappings", description = "Example Requests:\n" + "\n"
            + "financialactivityaccounts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.GetFinancialActivityAccountsResponse.class)))) })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(FinancialActivityAccountsConstants.resourceNameForPermission);
        final List<FinancialActivityAccountData> financialActivityAccounts = this.financialActivityAccountReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, financialActivityAccounts,
                FinancialActivityAccountsConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{mappingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Financial Activity to Account Mapping\n", description = "Example Requests:\n" + "\n"
            + "financialactivityaccounts/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.GetFinancialActivityAccountsResponse.class))) })
    public String retreive(@PathParam("mappingId") @Parameter(description = "mappingId") final Long mappingId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(FinancialActivityAccountsConstants.resourceNameForPermission);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        FinancialActivityAccountData financialActivityAccountData = this.financialActivityAccountReadPlatformService.retrieve(mappingId);
        if (settings.isTemplate()) {
            financialActivityAccountData = this.financialActivityAccountReadPlatformService
                    .addTemplateDetails(financialActivityAccountData);
        }

        return this.apiJsonSerializerService.serialize(settings, financialActivityAccountData,
                FinancialActivityAccountsConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new Financial Activity to Accounts Mapping", description = "Mandatory Fields\n"
            + "financialActivityId, glAccountId")
    @RequestBody(content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.PostFinancialActivityAccountsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.PostFinancialActivityAccountsResponse.class))) })
    public String createGLAccount(@Parameter(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createOfficeToGLAccountMapping().withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{mappingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Financial Activity to Account Mapping", description = "the API updates the Ledger account linked to a Financial Activity \n")
    @RequestBody(content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.PostFinancialActivityAccountsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.PutFinancialActivityAccountsResponse.class))) })
    public String updateGLAccount(@PathParam("mappingId") @Parameter(description = "mappingId") final Long mappingId,
            @Parameter(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateOfficeToGLAccountMapping(mappingId)
                .withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{mappingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Financial Activity to Account Mapping")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.DeleteFinancialActivityAccountsResponse.class))) })
    public String deleteGLAccount(@PathParam("mappingId") @Parameter(description = "mappingId") final Long mappingId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteOfficeToGLAccountMapping(mappingId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }
}
