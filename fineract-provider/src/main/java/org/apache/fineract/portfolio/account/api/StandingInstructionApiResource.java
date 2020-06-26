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
package org.apache.fineract.portfolio.account.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/standinginstructions")
@Component
@Scope("singleton")
@Tag(name = "Standing Instructions", description = "Standing instructions (or standing orders) refer to instructions a bank account holder (\"the payer\") gives to his or her bank to pay a set amount at regular intervals to another's (\"the payee's\") account.\n"
        + "\n" + "Note: At present only savings account to savings account and savings account to Loan account transfers are permitted.")
public class StandingInstructionApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<StandingInstructionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final StandingInstructionReadPlatformService standingInstructionReadPlatformService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;

    @Autowired
    public StandingInstructionApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<StandingInstructionData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final StandingInstructionReadPlatformService standingInstructionReadPlatformService,
            final AccountTransfersReadPlatformService accountTransfersReadPlatformService) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.standingInstructionReadPlatformService = standingInstructionReadPlatformService;
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Standing Instruction Template", description = "This is a convenience resource. "
            + "It can be useful when building maintenance user interface screens for client applications. "
            + "The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n"
            + "Example Requests:\n" + "\n" + "standinginstructions/template?fromAccountType=2&fromOfficeId=1\n" + "\n"
            + "standinginstructions/template?fromAccountType=2&fromOfficeId=1&fromClientId=1&transferType=1\n" + "\n"
            + "standinginstructions/template?fromClientId=1&fromAccountType=2&fromAccountId=1&transferType=1")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.GetStandingInstructionsTemplateResponse.class))))
    public String template(@QueryParam("fromOfficeId") @Parameter(description = "fromOfficeId") final Long fromOfficeId,
            @QueryParam("fromClientId") @Parameter(description = "fromClientId") final Long fromClientId,
            @QueryParam("fromAccountId") @Parameter(description = "fromAccountId") final Long fromAccountId,
            @QueryParam("fromAccountType") @Parameter(description = "fromAccountType") final Integer fromAccountType,
            @QueryParam("toOfficeId") @Parameter(description = "toOfficeId") final Long toOfficeId,
            @QueryParam("toClientId") @Parameter(description = "toClientId") final Long toClientId,
            @QueryParam("toAccountId") @Parameter(description = "toAccountId") final Long toAccountId,
            @QueryParam("toAccountType") @Parameter(description = "toAccountType") final Integer toAccountType,
            @QueryParam("transferType") @Parameter(description = "transferType") final Integer transferType,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);

        final StandingInstructionData standingInstructionData = this.standingInstructionReadPlatformService.retrieveTemplate(fromOfficeId,
                fromClientId, fromAccountId, fromAccountType, toOfficeId, toClientId, toAccountId, toAccountType, transferType);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, standingInstructionData,
                StandingInstructionApiConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create new Standing Instruction", description = "Ability to create new instruction for transfer of monetary funds from one account to another")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.PostStandingInstructionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.PostStandingInstructionsResponse.class))) })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createStandingInstruction().withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{standingInstructionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Standing Instruction | Delete Standing Instruction", description = "Ability to modify existing instruction for transfer of monetary funds from one account to another.\n"
            + "\n" + "PUT https://DomainName/api/v1/standinginstructions/1?command=update\n" + "\n\n"
            + "Ability to modify existing instruction for transfer of monetary funds from one account to another.\n" + "\n"
            + "PUT https://DomainName/api/v1/standinginstructions/1?command=delete")
    @RequestBody(required = false, content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.PutStandingInstructionsStandingInstructionIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.PutStandingInstructionsStandingInstructionIdResponse.class))) })
    public String update(
            @PathParam("standingInstructionId") @Parameter(description = "standingInstructionId") final Long standingInstructionId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson,
            @QueryParam("command") @Parameter(description = "command") final String commandParam) {

        CommandWrapper commandRequest = null;
        if (is(commandParam, "update")) {
            commandRequest = new CommandWrapperBuilder().updateStandingInstruction(standingInstructionId).withJson(apiRequestBodyAsJson)
                    .build();
        } else if (is(commandParam, "delete")) {
            commandRequest = new CommandWrapperBuilder().deleteStandingInstruction(standingInstructionId).withJson(apiRequestBodyAsJson)
                    .build();
        }

        if (commandRequest == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Standing Instructions", description = "Example Requests:\n" + "\n" + "standinginstructions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.GetStandingInstructionsResponse.class))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("sqlSearch") @Parameter(description = "sqlSearch") final String sqlSearch,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("transferType") @Parameter(description = "transferType") final Integer transferType,
            @QueryParam("clientName") @Parameter(description = "clientName") final String clientName,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("fromAccountId") @Parameter(description = "fromAccountId") final Long fromAccount,
            @QueryParam("fromAccountType") @Parameter(description = "fromAccountType") final Integer fromAccountType) {

        this.context.authenticatedUser().validateHasReadPermission(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);

        final SearchParameters searchParameters = SearchParameters.forAccountTransfer(sqlSearch, externalId, offset, limit, orderBy,
                sortOrder);

        final Date startDateRange = null;
        final Date endDateRange = null;
        StandingInstructionDTO standingInstructionDTO = new StandingInstructionDTO(searchParameters, transferType, clientName, clientId,
                fromAccount, fromAccountType, startDateRange, endDateRange);

        final Page<StandingInstructionData> transfers = this.standingInstructionReadPlatformService.retrieveAll(standingInstructionDTO);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, transfers, StandingInstructionApiConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{standingInstructionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Standing Instruction", description = "Example Requests :\n" + "\n" + "standinginstructions/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.GetStandingInstructionsStandingInstructionIdResponse.class))) })
    public String retrieveOne(
            @PathParam("standingInstructionId") @Parameter(description = "standingInstructionId") final Long standingInstructionId,
            @Context final UriInfo uriInfo, @QueryParam("sqlSearch") @Parameter(description = "sqlSearch") final String sqlSearch,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);

        StandingInstructionData standingInstructionData = this.standingInstructionReadPlatformService.retrieveOne(standingInstructionId);
        final SearchParameters searchParameters = SearchParameters.forAccountTransfer(sqlSearch, externalId, offset, limit, orderBy,
                sortOrder);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        Page<AccountTransferData> transfers = null;
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList("transactions", "template"));
            }
            if (associationParameters.contains("transactions")) {
                transfers = this.accountTransfersReadPlatformService.retrieveByStandingInstruction(standingInstructionId, searchParameters);
                standingInstructionData = StandingInstructionData.withTransferData(standingInstructionData, transfers);
            }
            if (associationParameters.contains("template")) {
                final StandingInstructionData templateData = this.standingInstructionReadPlatformService.retrieveTemplate(
                        standingInstructionData.fromClient().officeId(), standingInstructionData.fromClient().id(),
                        standingInstructionData.fromAccount().accountId(), standingInstructionData.fromAccountType().getValue(),
                        standingInstructionData.toClient().officeId(), standingInstructionData.toClient().id(),
                        standingInstructionData.toAccount().accountId(), standingInstructionData.toAccountType().getValue(),
                        standingInstructionData.transferType().getValue());
                standingInstructionData = StandingInstructionData.withTemplateData(standingInstructionData, templateData);
            }
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, standingInstructionData,
                StandingInstructionApiConstants.RESPONSE_DATA_PARAMETERS);
    }
}
