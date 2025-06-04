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
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.command.CommandHandlerRegistry;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionCreationRequest;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionSearchParam;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionUpdatesRequest;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/standinginstructions")
@Component
@Tag(name = "Standing Instructions", description = "Standing instructions (or standing orders) refer to instructions a bank account holder (\"the payer\") gives to his or her bank to pay a set amount at regular intervals to another's (\"the payee's\") account.\n"
        + "\n" + "Note: At present only savings account to savings account and savings account to Loan account transfers are permitted.")
@RequiredArgsConstructor
public class StandingInstructionApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<StandingInstructionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final StandingInstructionReadPlatformService standingInstructionReadPlatformService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final SqlValidator sqlValidator;

    private static final CommandHandlerRegistry<String, Long, String, CommandWrapper> COMMAND_HANDLER_REGISTRY = new CommandHandlerRegistry<>(
            Map.of(CommandParameterUtil.UPDATE_COMMAND_VALUE,
                    (id, json) -> new CommandWrapperBuilder().updateStandingInstruction(id).withJson(json).build(),
                    CommandParameterUtil.DELETE_COMMAND_VALUE,
                    (id, json) -> new CommandWrapperBuilder().deleteStandingInstruction(id).withJson(json).build()));

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
    public StandingInstructionData template(@BeanParam StandingInstructionSearchParam instructionParam) {
        context.authenticatedUser().validateHasReadPermission(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);

        return standingInstructionReadPlatformService.retrieveTemplate(instructionParam.getFromOfficeId(),
                instructionParam.getFromClientId(), instructionParam.getFromAccountId(), instructionParam.getFromAccountType(),
                instructionParam.getToOfficeId(), instructionParam.getToClientId(), instructionParam.getToAccountId(),
                instructionParam.getToAccountType(), instructionParam.getTransferType());
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create new Standing Instruction", description = "Ability to create new instruction for transfer of monetary funds from one account to another")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = StandingInstructionCreationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.PostStandingInstructionsResponse.class))) })
    public CommandProcessingResult create(@Parameter(hidden = true) StandingInstructionCreationRequest creationRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createStandingInstruction()
                .withJson(toApiJsonSerializer.serialize(creationRequest)).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("{standingInstructionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Standing Instruction | Delete Standing Instruction", description = "Ability to modify existing instruction for transfer of monetary funds from one account to another.\n"
            + "\n" + "PUT https://DomainName/api/v1/standinginstructions/1?command=update\n" + "\n\n"
            + "Ability to modify existing instruction for transfer of monetary funds from one account to another.\n" + "\n"
            + "PUT https://DomainName/api/v1/standinginstructions/1?command=delete")
    @RequestBody(content = @Content(schema = @Schema(implementation = StandingInstructionUpdatesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.PutStandingInstructionsStandingInstructionIdResponse.class))) })
    public CommandProcessingResult update(
            @PathParam("standingInstructionId") @Parameter(description = "standingInstructionId") final Long standingInstructionId,
            @Parameter(hidden = true) StandingInstructionUpdatesRequest updatesRequest,
            @QueryParam("command") @Parameter(description = "command") final String commandParam) {

        final String serializedUpdatesRequest = toApiJsonSerializer.serialize(updatesRequest);
        final CommandWrapper commandRequest = COMMAND_HANDLER_REGISTRY.execute(commandParam, standingInstructionId,
                serializedUpdatesRequest, new UnrecognizedQueryParamException("command", commandParam));

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Standing Instructions", description = "Example Requests:\n" + "\n" + "standinginstructions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.GetStandingInstructionsResponse.class))) })
    public Page<StandingInstructionData> retrieveAll(
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

        context.authenticatedUser().validateHasReadPermission(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);

        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        sqlValidator.validate(externalId);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).externalId(externalId).offset(offset)
                .orderBy(orderBy).sortOrder(sortOrder).build();

        final LocalDate startDateRange = null;
        final LocalDate endDateRange = null;
        StandingInstructionDTO standingInstructionDTO = new StandingInstructionDTO(searchParameters, transferType, clientName, clientId,
                fromAccount, fromAccountType, startDateRange, endDateRange);

        return standingInstructionReadPlatformService.retrieveAll(standingInstructionDTO);
    }

    @GET
    @Path("{standingInstructionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Standing Instruction", description = "Example Requests :\n" + "\n" + "standinginstructions/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.GetStandingInstructionsStandingInstructionIdResponse.class))) })
    public StandingInstructionData retrieveOne(
            @PathParam("standingInstructionId") @Parameter(description = "standingInstructionId") final Long standingInstructionId,
            @Context final UriInfo uriInfo, @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        context.authenticatedUser().validateHasReadPermission(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);

        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        StandingInstructionData standingInstructionData = standingInstructionReadPlatformService.retrieveOne(standingInstructionId);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).externalId(externalId).offset(offset)
                .orderBy(orderBy).sortOrder(sortOrder).build();
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList("transactions", "template"));
            }
            if (associationParameters.contains("transactions")) {
                Page<AccountTransferData> transfers = accountTransfersReadPlatformService
                        .retrieveByStandingInstruction(standingInstructionId, searchParameters);
                standingInstructionData = StandingInstructionData.withTransferData(standingInstructionData, transfers);
            }
            if (associationParameters.contains("template")) {
                final StandingInstructionData templateData = standingInstructionReadPlatformService.retrieveTemplate(
                        standingInstructionData.getFromClient().getOfficeId(), standingInstructionData.getFromClient().getId(),
                        standingInstructionData.getFromAccount().getId(), standingInstructionData.getFromAccountType().getValue(),
                        standingInstructionData.getToClient().getOfficeId(), standingInstructionData.getToClient().getId(),
                        standingInstructionData.getToAccount().getId(), standingInstructionData.getToAccountType().getValue(),
                        standingInstructionData.getTransferType().getValue());
                standingInstructionData = StandingInstructionData.withTemplateData(standingInstructionData, templateData);
            }
        }

        return standingInstructionData;
    }
}
