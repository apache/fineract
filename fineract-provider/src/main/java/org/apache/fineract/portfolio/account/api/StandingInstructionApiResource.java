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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.account.command.StandingInstructionCreateCommand;
import org.apache.fineract.portfolio.account.command.StandingInstructionDeleteCommand;
import org.apache.fineract.portfolio.account.command.StandingInstructionUpdateCommand;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionCreationRequest;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionDeleteRequest;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionSearchParam;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionUpdateRequest;
import org.apache.fineract.portfolio.account.data.response.StandingInstructionCreateResponse;
import org.apache.fineract.portfolio.account.data.response.StandingInstructionDeleteResponse;
import org.apache.fineract.portfolio.account.data.response.StandingInstructionUpdateResponse;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadService;
import org.springframework.stereotype.Component;

@Path("/v1/standinginstructions")
@Component
@Produces({ MediaType.APPLICATION_JSON })
@Tag(name = "Standing Instructions", description = """
        Standing instructions (or standing orders) refer to instructions a bank account holder ("the payer") gives to his or her bank to pay a set amount at regular intervals to another's ("the payee's") account.

        Note: At present only savings account to savings account and savings account to Loan account transfers are permitted.""")
@RequiredArgsConstructor
public class StandingInstructionApiResource {

    private final StandingInstructionReadService standingInstructionReadService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final SqlValidator sqlValidator;
    private final CommandDispatcher dispatcher;

    @GET
    @Path("template")
    @Operation(summary = "Retrieve Standing Instruction Template", operationId = "retrieveTemplateStandingInstruction", description = """
            This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:

            Field Defaults
            Allowed Value Lists
            Example Requests:

            standinginstructions/template?fromAccountType=2&fromOfficeId=1

            standinginstructions/template?fromAccountType=2&fromOfficeId=1&fromClientId=1&transferType=1

            standinginstructions/template?fromClientId=1&fromAccountType=2&fromAccountId=1&transferType=1""")
    @AlternativeOperationId("template_6")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.GetStandingInstructionsTemplateResponse.class)))
    public StandingInstructionData template(@BeanParam StandingInstructionSearchParam instructionParam) {
        return standingInstructionReadService.retrieveTemplate(instructionParam.getFromOfficeId(), instructionParam.getFromClientId(),
                instructionParam.getFromAccountId(), instructionParam.getFromAccountType(), instructionParam.getToOfficeId(),
                instructionParam.getToClientId(), instructionParam.getToAccountId(), instructionParam.getToAccountType(),
                instructionParam.getTransferType());
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create new Standing Instruction", operationId = "createStandingInstruction", description = "Ability to create new instruction for transfer of monetary funds from one account to another")
    @AlternativeOperationId("create_5")
    public StandingInstructionCreateResponse create(
            @RequestBody(required = true) @Valid final StandingInstructionCreationRequest creationRequest) {
        final StandingInstructionCreateCommand command = new StandingInstructionCreateCommand();
        command.setPayload(creationRequest);

        final Supplier<StandingInstructionCreateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @PUT
    @Path("{standingInstructionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Standing Instruction | Delete Standing Instruction", operationId = "updateStandingInstruction", description = """
            Ability to modify existing instruction for transfer of monetary funds from one account to another.

            PUT https://DomainName/api/v1/standinginstructions/1?command=update


            Ability to modify existing instruction for transfer of monetary funds from one account to another.

            PUT https://DomainName/api/v1/standinginstructions/1?command=delete""")
    @AlternativeOperationId("update_9")
    public StandingInstructionUpdateResponse update(
            @PathParam("standingInstructionId") @Parameter(description = "standingInstructionId") final Long standingInstructionId,
            @RequestBody(description = "The update payload; not required when command=delete") @Valid final StandingInstructionUpdateRequest updatesRequest,
            @QueryParam("command") @Parameter(description = "command") final String commandParam) {

        if (CommandParameterUtil.is(commandParam, CommandParameterUtil.UPDATE_COMMAND_VALUE)) {
            updatesRequest.setId(standingInstructionId);
            final StandingInstructionUpdateCommand command = new StandingInstructionUpdateCommand();
            command.setPayload(updatesRequest);

            final Supplier<StandingInstructionUpdateResponse> response = dispatcher.dispatch(command);

            return response.get();
        } else if (CommandParameterUtil.is(commandParam, CommandParameterUtil.DELETE_COMMAND_VALUE)) {
            final StandingInstructionDeleteCommand command = new StandingInstructionDeleteCommand();
            command.setPayload(StandingInstructionDeleteRequest.builder().id(standingInstructionId).build());

            final Supplier<StandingInstructionDeleteResponse> response = dispatcher.dispatch(command);

            return StandingInstructionUpdateResponse.builder().resourceId(response.get().getResourceId()).build();
        }

        throw new UnrecognizedQueryParamException("command", commandParam);
    }

    @GET
    @Operation(summary = "List Standing Instructions", operationId = "retrieveAllStandingInstructions", description = """
            Example Requests:

            standinginstructions""")
    @AlternativeOperationId("retrieveAll_19")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.GetStandingInstructionsResponse.class)))
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

        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        sqlValidator.validate(externalId);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).externalId(externalId).offset(offset)
                .orderBy(orderBy).sortOrder(sortOrder).build();

        final LocalDate startDateRange = null;
        final LocalDate endDateRange = null;
        StandingInstructionDTO standingInstructionDTO = new StandingInstructionDTO(searchParameters, transferType, clientName, clientId,
                fromAccount, fromAccountType, startDateRange, endDateRange);

        return standingInstructionReadService.retrieveAll(standingInstructionDTO);
    }

    @GET
    @Path("{standingInstructionId}")
    @Operation(summary = "Retrieve Standing Instruction", operationId = "retrieveOneStandingInstruction", description = """
            Example Requests :

            standinginstructions/1""")
    @AlternativeOperationId("retrieveOne_10")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionApiResourceSwagger.GetStandingInstructionsStandingInstructionIdResponse.class)))
    public StandingInstructionData retrieveOne(
            @PathParam("standingInstructionId") @Parameter(description = "standingInstructionId") final Long standingInstructionId,
            @Context final UriInfo uriInfo, @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        StandingInstructionData standingInstructionData = standingInstructionReadService.retrieveOne(standingInstructionId);
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
                final StandingInstructionData templateData = standingInstructionReadService.retrieveTemplate(
                        standingInstructionData.getFromClient().getOfficeId(), standingInstructionData.getFromClient().getId(),
                        standingInstructionData.getFromAccount().getId(), standingInstructionData.getFromAccountTypeEnum().getValue(),
                        standingInstructionData.getToClient().getOfficeId(), standingInstructionData.getToClient().getId(),
                        standingInstructionData.getToAccount().getId(), standingInstructionData.getToAccountTypeEnum().getValue(),
                        standingInstructionData.getTransferTypeEnum().getValue());
                standingInstructionData = StandingInstructionData.withTemplateData(standingInstructionData, templateData);
            }
        }

        return standingInstructionData;
    }
}
