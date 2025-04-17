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
import org.apache.fineract.accounting.financialactivityaccount.data.request.FinancialActivityAccRequest;
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
@Tag(name = "Mapping Financial Activities to Accounts", description = """
        Organization Level Financial Activities like Asset and Liability Transfer can be mapped to GL Account. \
        Integrated accounting takes these accounts into consideration when an Account transfer is made between \
        a savings to loan/savings account and vice-versa

        Field Descriptions
        financialActivityId
        The identifier of the Financial Activity
        glAccountId
        The identifier of a GL Account ( Ledger Account) which shall be used as the default account for the selected Financial Activity
        """)
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
    public FinancialActivityAccountData retrieveTemplate() {
        context.authenticatedUser().validateHasReadPermission(FinancialActivityAccountsConstants.RESOURCE_NAME_FOR_PERMISSION);
        return financialActivityAccountReadPlatformService.getFinancialActivityAccountTemplate();
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Financial Activities to Accounts Mappings", description = """
            Example Requests:
            financialactivityaccounts""")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.GetFinancialActivityAccountsResponse.class))))
    public List<FinancialActivityAccountData> retrieveAll() {
        context.authenticatedUser().validateHasReadPermission(FinancialActivityAccountsConstants.RESOURCE_NAME_FOR_PERMISSION);
        return financialActivityAccountReadPlatformService.retrieveAll();
    }

    @GET
    @Path("{mappingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Financial Activity to Account Mapping\n", description = """
            Example Requests:
            financialactivityaccounts/1""")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.GetFinancialActivityAccountsResponse.class)))
    public FinancialActivityAccountData retreive(@PathParam("mappingId") @Parameter(description = "mappingId") final Long mappingId,
            @Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(FinancialActivityAccountsConstants.RESOURCE_NAME_FOR_PERMISSION);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        FinancialActivityAccountData financialActivityAccountData = financialActivityAccountReadPlatformService.retrieve(mappingId);
        if (settings.isTemplate()) {
            financialActivityAccountData = financialActivityAccountReadPlatformService.addTemplateDetails(financialActivityAccountData);
        }

        return financialActivityAccountData;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new Financial Activity to Accounts Mapping", description = """
            Mandatory Fields
            financialActivityId, glAccountId""")
    @RequestBody(content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.PostFinancialActivityAccountsRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.PostFinancialActivityAccountsResponse.class)))
    public CommandProcessingResult createGLAccount(@Parameter(hidden = true) FinancialActivityAccRequest activityAccRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createOfficeToGLAccountMapping()
                .withJson(apiJsonSerializerService.serialize(activityAccRequest)).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("{mappingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Financial Activity to Account Mapping", description = "the API updates the Ledger account linked to a Financial Activity")
    @RequestBody(content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.PostFinancialActivityAccountsRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.PutFinancialActivityAccountsResponse.class)))
    public CommandProcessingResult updateGLAccount(@PathParam("mappingId") @Parameter(description = "mappingId") final Long mappingId,
            @Parameter(hidden = true) FinancialActivityAccRequest activityAccRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateOfficeToGLAccountMapping(mappingId)
                .withJson(apiJsonSerializerService.serialize(activityAccRequest)).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{mappingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Financial Activity to Account Mapping")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FinancialActivityAccountsApiResourceSwagger.DeleteFinancialActivityAccountsResponse.class)))
    public CommandProcessingResult deleteGLAccount(@PathParam("mappingId") @Parameter(description = "mappingId") final Long mappingId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteOfficeToGLAccountMapping(mappingId).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
