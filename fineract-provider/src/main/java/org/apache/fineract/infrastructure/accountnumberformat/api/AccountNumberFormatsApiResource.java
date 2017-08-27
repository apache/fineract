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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path(AccountNumberFormatConstants.resourceRelativeURL)
@Component
@Scope("singleton")
@Api(value = "Account number format", description = "Account number preferences are used to describe custom formats for account numbers associated with Customer, Loan and Savings accounts." )
public class AccountNumberFormatsApiResource {

    private final PlatformSecurityContext context;
    private final AccountNumberFormatReadPlatformService accountNumberFormatReadPlatformService;
    private final ToApiJsonSerializer<AccountNumberFormatData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private static final Set<String> ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			AccountNumberFormatConstants.idParamName, AccountNumberFormatConstants.accountTypeParamName,
			AccountNumberFormatConstants.prefixTypeParamName, AccountNumberFormatConstants.accountTypeOptionsParamName,
			AccountNumberFormatConstants.prefixTypeOptionsParamName));

    @Autowired
    public AccountNumberFormatsApiResource(final PlatformSecurityContext context,
            final ToApiJsonSerializer<AccountNumberFormatData> toApiJsonSerializer,
            final AccountNumberFormatReadPlatformService accountNumberFormatReadPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.accountNumberFormatReadPlatformService = accountNumberFormatReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Account number format Template", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "\n" + "Example Request:\n" + "\n" + "accountnumberformats/template")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = AccountNumberFormatsApiResourceSwagger.GetAccountNumberFormatsResponseTemplate.class)})
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(AccountNumberFormatConstants.ENTITY_NAME);

        EntityAccountType accountType = null;
        AccountNumberFormatData accountNumberFormatData = this.accountNumberFormatReadPlatformService.retrieveTemplate(accountType);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, accountNumberFormatData,
				ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Account number formats", notes = "Example Requests:\n" + "\n" + "accountnumberformats\n" + "\n" + "\n" + "accountnumberformats?fields=accountType,prefixType", responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = AccountNumberFormatsApiResourceSwagger.GetAccountNumberFormatsIdResponse.class, responseContainer = "List")})
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(AccountNumberFormatConstants.ENTITY_NAME);

        final List<AccountNumberFormatData> accountNumberFormatData = this.accountNumberFormatReadPlatformService
                .getAllAccountNumberFormats();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, accountNumberFormatData,
				ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountNumberFormatId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve an Account number format", notes = "Example Requests:\n" + "\n" + "accountnumberformats/1\n" + "\n" + "\n" + "accountnumberformats/1?template=true\n" + "\n" + "\n" + "accountnumberformats/1?fields=accountType,prefixType")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = AccountNumberFormatsApiResourceSwagger.GetAccountNumberFormatsIdResponse.class)})
    public String retrieveOne(@Context final UriInfo uriInfo, @PathParam("accountNumberFormatId") @ApiParam(value = "accountNumberFormatId") final Long accountNumberFormatId) {

        this.context.authenticatedUser().validateHasReadPermission(AccountNumberFormatConstants.ENTITY_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        AccountNumberFormatData accountNumberFormatData = this.accountNumberFormatReadPlatformService
                .getAccountNumberFormat(accountNumberFormatId);
        if (settings.isTemplate()) {
            final AccountNumberFormatData templateData = this.accountNumberFormatReadPlatformService.retrieveTemplate(EntityAccountType
                    .fromInt(accountNumberFormatData.getAccountType().getId().intValue()));
            accountNumberFormatData.templateOnTop(templateData.getAccountTypeOptions(), templateData.getPrefixTypeOptions());
        }

		return this.toApiJsonSerializer.serialize(settings, accountNumberFormatData,
				ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create an Account number format", notes = "Note: You may associate a single Account number format for a given account type\n" + "Mandatory Fields for Account number formats\n" + "accountType")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", paramType = "body", dataType = "body", format = "body", dataTypeClass = AccountNumberFormatsApiResourceSwagger.PostAccountNumberFormatsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = AccountNumberFormatsApiResourceSwagger.PostAccountNumberFormatsResponse.class)})
    public String create(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Update an Account number format")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true,paramType = "body", dataType = "body", format = "body", dataTypeClass = AccountNumberFormatsApiResourceSwagger.PutAccountNumberFormatsRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = AccountNumberFormatsApiResourceSwagger.PutAccountNumberFormatsResponse.class)})
    public String update(@PathParam("accountNumberFormatId") @ApiParam(value = "accountNumberFormatId") final Long accountNumberFormatId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Delete an Account number format", notes = "Note: Account numbers created while this format was active would remain unchanged.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = AccountNumberFormatsApiResourceSwagger.DeleteAccountNumberFormatsResponse.class)})
    public String delete(@PathParam("accountNumberFormatId") @ApiParam(value = "accountNumberFormatId") final Long accountNumberFormatId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteAccountNumberFormat(accountNumberFormatId) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
