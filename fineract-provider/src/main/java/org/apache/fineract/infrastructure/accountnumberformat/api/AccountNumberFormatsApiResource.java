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
package org.apache.fineract.infrastructure.accountnumberformat.api;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.accountnumberformat.data.AccountNumberFormatData;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.accountnumberformat.service.AccountNumberFormatConstants;
import org.apache.fineract.infrastructure.accountnumberformat.service.AccountNumberFormatReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path(AccountNumberFormatConstants.resourceRelativeURL)
@Component
@Tag(name = "Account number format", description = "Account number preferences are used to describe custom formats for account numbers associated with Customer, Loan and Savings accounts.")
@RequiredArgsConstructor
public class AccountNumberFormatsApiResource {

    private final PlatformSecurityContext context;
    private final AccountNumberFormatReadPlatformService accountNumberFormatReadPlatformService;
    private final ToApiJsonSerializer<AccountNumberFormatData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private static final Set<String> ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(AccountNumberFormatConstants.idParamName, AccountNumberFormatConstants.accountTypeParamName,
                    AccountNumberFormatConstants.prefixTypeParamName, AccountNumberFormatConstants.accountTypeOptionsParamName,
                    AccountNumberFormatConstants.prefixTypeOptionsParamName));

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Account number format Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "\n" + "Example Request:\n" + "\n" + "accountnumberformats/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountNumberFormatsApiResourceSwagger.GetAccountNumberFormatsResponseTemplate.class))) })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(AccountNumberFormatConstants.ENTITY_NAME);

        EntityAccountType accountType = null;
        AccountNumberFormatData accountNumberFormatData = this.accountNumberFormatReadPlatformService.retrieveTemplate(accountType);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, accountNumberFormatData, ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Account number formats", description = "Example Requests:\n" + "\n" + "accountnumberformats\n" + "\n" + "\n"
            + "accountnumberformats?fields=accountType,prefixType")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountNumberFormatsApiResourceSwagger.GetAccountNumberFormatsIdResponse.class)))) })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(AccountNumberFormatConstants.ENTITY_NAME);

        final List<AccountNumberFormatData> accountNumberFormatData = this.accountNumberFormatReadPlatformService
                .getAllAccountNumberFormats();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, accountNumberFormatData, ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountNumberFormatId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve an Account number format", description = "Example Requests:\n" + "\n" + "accountnumberformats/1\n" + "\n"
            + "\n" + "accountnumberformats/1?template=true\n" + "\n" + "\n" + "accountnumberformats/1?fields=accountType,prefixType")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountNumberFormatsApiResourceSwagger.GetAccountNumberFormatsIdResponse.class))) })
    public String retrieveOne(@Context final UriInfo uriInfo,
            @PathParam("accountNumberFormatId") @Parameter(description = "accountNumberFormatId") final Long accountNumberFormatId) {

        this.context.authenticatedUser().validateHasReadPermission(AccountNumberFormatConstants.ENTITY_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        AccountNumberFormatData accountNumberFormatData = this.accountNumberFormatReadPlatformService
                .getAccountNumberFormat(accountNumberFormatId);
        if (settings.isTemplate()) {
            final AccountNumberFormatData templateData = this.accountNumberFormatReadPlatformService
                    .retrieveTemplate(EntityAccountType.fromInt(accountNumberFormatData.getAccountType().getId().intValue()));
            accountNumberFormatData.templateOnTop(templateData.getAccountTypeOptions(), templateData.getPrefixTypeOptions());
        }

        return this.toApiJsonSerializer.serialize(settings, accountNumberFormatData, ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Account number format", description = "Note: You may associate a single Account number format for a given account type\n"
            + "Mandatory Fields for Account number formats\n" + "accountType")
    @RequestBody(content = @Content(schema = @Schema(implementation = AccountNumberFormatsApiResourceSwagger.PostAccountNumberFormatsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountNumberFormatsApiResourceSwagger.PostAccountNumberFormatsResponse.class))) })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createAccountNumberFormat() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{accountNumberFormatId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update an Account number format")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountNumberFormatsApiResourceSwagger.PutAccountNumberFormatsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountNumberFormatsApiResourceSwagger.PutAccountNumberFormatsResponse.class))) })
    public String update(
            @PathParam("accountNumberFormatId") @Parameter(description = "accountNumberFormatId") final Long accountNumberFormatId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateAccountNumberFormat(accountNumberFormatId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{accountNumberFormatId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete an Account number format", description = "Note: Account numbers created while this format was active would remain unchanged.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountNumberFormatsApiResourceSwagger.DeleteAccountNumberFormatsResponse.class))) })
    public String delete(
            @PathParam("accountNumberFormatId") @Parameter(description = "accountNumberFormatId") final Long accountNumberFormatId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteAccountNumberFormat(accountNumberFormatId) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
