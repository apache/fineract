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

package org.apache.fineract.portfolio.workingcapitalloan.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargeData;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanChargeConstants;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanChargeReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/working-capital-loans")
@Component
@Tag(name = "Working Capital Loan Charges", description = "Manages Charges for Working Capital loans")
@RequiredArgsConstructor
public class WorkingCapitalLoanChargesApiResource {

    private final WorkingCapitalLoanRepository workingCapitalLoanRepository;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final PlatformSecurityContext securityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final WorkingCapitalLoanChargeReadPlatformService loanChargeReadPlatformService;
    private final WorkingCapitalLoanChargeRepository loanChargeRepository;

    @GET
    @Path("{loanId}/charges/template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Working Capital Loan Charges Template", operationId = "retrieveTemplateWorkingCapitalLoanCharge", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "loans/1/charges/template\n" + "\n")
    @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
    public WorkingCapitalLoanChargeData retrieveTemplate(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        return retrieveTemplate(loanId, null);
    }

    @GET
    @Path("external-id/{loanExternalId}/charges/template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Working Capital Loan Charges Template", operationId = "retrieveTemplateWorkingCapitalLoanChargeByLoanExternalId", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "loans/1/charges/template\n" + "\n")
    @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
    public WorkingCapitalLoanChargeData retrieveTemplate(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {

        return retrieveTemplate(null, loanExternalId);
    }

    @POST
    @Path("{loanId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Loan Charge ", description = "Creates a Loan Charge")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostLoansLoanIdChargesRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostLoansLoanIdChargesResponse.class)))
    public CommandProcessingResult createLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleExecuteLoanCharge(loanId, null, "create", apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Loan Charge (no command provided)", operationId = "executeWorkingCapitalLoanChargeByLoanExternalId", description = "Creates a Loan Charge | Pay a Loan Charge")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostLoansLoanIdChargesRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostLoansLoanIdChargesResponse.class)))
    public CommandProcessingResult executeLoanCharge(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleExecuteLoanCharge(null, loanExternalId, "create", apiRequestBodyAsJson);
    }

    @POST
    @Path("{loanId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Adjust a Working Capital Loan Charge", description = "Adjusts a working capital loan charge by creating a CHARGE_ADJUSTMENT transaction. Pass command=adjustment.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostWorkingCapitalLoansLoanIdChargesChargeIdResponse.class)))
    public CommandProcessingResult adjustLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId,
            @QueryParam("command") @DefaultValue("") @Parameter(description = "command") final String commandParam,
            @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostWorkingCapitalLoansLoanIdChargesChargeIdRequest.class))) final String apiRequestBodyAsJson) {
        return handleExecuteLoanChargeWithChargeId(loanId, null, loanChargeId, null, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("{loanId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Adjust a Working Capital Loan Charge by Charge External Id", description = "Adjusts a working capital loan charge by creating a CHARGE_ADJUSTMENT transaction. Pass command=adjustment.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostWorkingCapitalLoansLoanIdChargesChargeIdResponse.class)))
    public CommandProcessingResult adjustLoanChargeByChargeExternalId(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId,
            @QueryParam("command") @DefaultValue("") @Parameter(description = "command") final String commandParam,
            @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostWorkingCapitalLoansLoanIdChargesChargeIdRequest.class))) final String apiRequestBodyAsJson) {
        return handleExecuteLoanChargeWithChargeId(loanId, null, null, loanChargeExternalId, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Adjust a Working Capital Loan Charge by Loan External Id", description = "Adjusts a working capital loan charge by creating a CHARGE_ADJUSTMENT transaction. Pass command=adjustment.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostWorkingCapitalLoansLoanIdChargesChargeIdResponse.class)))
    public CommandProcessingResult adjustLoanChargeByLoanExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId,
            @QueryParam("command") @DefaultValue("") @Parameter(description = "command") final String commandParam,
            @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostWorkingCapitalLoansLoanIdChargesChargeIdRequest.class))) final String apiRequestBodyAsJson) {
        return handleExecuteLoanChargeWithChargeId(null, loanExternalId, loanChargeId, null, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Adjust a Working Capital Loan Charge by Loan and Charge External Ids", description = "Adjusts a working capital loan charge by creating a CHARGE_ADJUSTMENT transaction. Pass command=adjustment.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostWorkingCapitalLoansLoanIdChargesChargeIdResponse.class)))
    public CommandProcessingResult adjustLoanChargeByLoanAndChargeExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId,
            @QueryParam("command") @DefaultValue("") @Parameter(description = "command") final String commandParam,
            @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.PostWorkingCapitalLoansLoanIdChargesChargeIdRequest.class))) final String apiRequestBodyAsJson) {
        return handleExecuteLoanChargeWithChargeId(null, loanExternalId, null, loanChargeExternalId, commandParam, apiRequestBodyAsJson);
    }

    @GET
    @Path("{loanId}/charges")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loan Charges", operationId = "retrieveAllWorkingCapitalLoanChargesByLoanId", description = "It lists all the Loan Charges specific to a Loan \n\n"
            + "Example Requests:\n" + "\n" + "loans/1/charges\n" + "\n" + "\n" + "loans/1/charges?fields=name,amountOrPercentage")
    @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
    public List<WorkingCapitalLoanChargeData> retrieveAllLoanCharges(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId, @Context final UriInfo uriInfo) {

        return retrieveAllLoanCharges(loanId, null, uriInfo);
    }

    @GET
    @Path("external-id/{loanExternalId}/charges")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loan Charges", operationId = "retrieveAllWorkingCapitalLoanChargesByLoanExternalId", description = "It lists all the Loan Charges specific to a Loan \n\n"
            + "Example Requests:\n" + "\n" + "loans/1/charges\n" + "\n" + "\n" + "loans/1/charges?fields=name,amountOrPercentage")
    @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
    public List<WorkingCapitalLoanChargeData> retrieveAllLoanCharges(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @Context final UriInfo uriInfo) {

        return retrieveAllLoanCharges(null, loanExternalId, uriInfo);
    }

    @GET
    @Path("{loanId}/charges/{loanChargeId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", description = "Retrieves Loan Charge according to the Loan ID and Loan Charge ID"
            + "Example Requests:\n" + "\n" + "/loans/1/charges/1\n" + "\n" + "\n" + "/loans/1/charges/1?fields=name,amountOrPercentage")
    @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
    public WorkingCapitalLoanChargeData retrieveWorkingCapitalLoanCharge(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId) {

        return retrieveWorkingCapitalLoanCharge(loanId, null, loanChargeId, null);
    }

    @GET
    @Path("{loanId}/charges/external-id/{loanChargeExternalId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", operationId = "retrieveWorkingCapitalLoanChargeByChargeExternalId", description = "Retrieves Loan Charge according to the Loan ID and Loan Charge External ID"
            + "Example Requests:\n" + "\n" + "/loans/1/charges/1\n" + "\n" + "\n"
            + "/loans/1/charges/external-id/1?fields=name,amountOrPercentage")
    @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
    public WorkingCapitalLoanChargeData retrieveWorkingCapitalLoanCharge(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId) {

        return retrieveWorkingCapitalLoanCharge(loanId, null, null, loanChargeExternalId);
    }

    @GET
    @Path("external-id/{loanExternalId}/charges/{loanChargeId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", operationId = "retrieveWorkingCapitalLoanChargeByLoanExternalId", description = "Retrieves Loan Charge according to the Loan external ID and Loan Charge ID"
            + "Example Requests:\n" + "\n" + "/loans/1/charges/1\n" + "\n" + "\n" + "/loans/1/charges/1?fields=name,amountOrPercentage")
    @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
    public WorkingCapitalLoanChargeData retrieveWorkingCapitalLoanCharge(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId) {

        return retrieveWorkingCapitalLoanCharge(null, loanExternalId, loanChargeId, null);
    }

    @GET
    @Path("external-id/{loanExternalId}/charges/external-id/{loanChargeExternalId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", operationId = "retrieveWorkingCapitalLoanChargeByLoanAndChargeExternalId", description = "Retrieves Loan Charge according to the Loan External ID and Loan Charge External ID"
            + "Example Requests:\n" + "\n" + "/loans/1/charges/1\n" + "\n" + "\n" + "/loans/1/charges/1?fields=name,amountOrPercentage")
    @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
    public WorkingCapitalLoanChargeData retrieveWorkingCapitalLoanCharge(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId) {

        return retrieveWorkingCapitalLoanCharge(null, loanExternalId, null, loanChargeExternalId);
    }

    private WorkingCapitalLoanChargeData retrieveWorkingCapitalLoanCharge(final Long loanId, final String loanExternalIdStr,
            final Long loanChargeId, final String loanChargeExternalIdStr) {

        securityContext.authenticatedUser().validateHasReadPermission(WorkingCapitalLoanConstants.WCL_RESOURCE_NAME);

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        ExternalId loanChargeExternalId = ExternalIdFactory.produce(loanChargeExternalIdStr);

        Long resolvedLoanId = loanId == null ? workingCapitalLoanRepository.findIdByExternalId(loanExternalId) : loanId;
        Long resolvedLoanChargeId = loanChargeId == null ? loanChargeRepository.findIdByExternalId(loanChargeExternalId) : loanChargeId;

        return loanChargeReadPlatformService.retrieveLoanChargeDetails(resolvedLoanChargeId, resolvedLoanId);
    }

    private WorkingCapitalLoanChargeData retrieveTemplate(final Long loanId, final String loanExternalIdStr) {

        securityContext.authenticatedUser().validateHasReadPermission(WorkingCapitalLoanConstants.WCL_RESOURCE_NAME);

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = loanId == null ? workingCapitalLoanRepository.findIdByExternalId(loanExternalId) : loanId;

        final List<ChargeData> chargeOptions = chargeReadPlatformService.retrieveWorkingCapitalLoanAccountApplicableCharges(resolvedLoanId);
        return WorkingCapitalLoanChargeData.template(chargeOptions);
    }

    private List<WorkingCapitalLoanChargeData> retrieveAllLoanCharges(final Long loanId, final String loanExternalIdStr,
            final UriInfo uriInfo) {

        securityContext.authenticatedUser().validateHasReadPermission(WorkingCapitalLoanConstants.WCL_RESOURCE_NAME);

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = loanId == null ? workingCapitalLoanRepository.findIdByExternalId(loanExternalId) : loanId;
        return loanChargeReadPlatformService.retrieveLoanCharges(resolvedLoanId);
    }

    private CommandProcessingResult handleExecuteLoanCharge(final Long loanId, final String loanExternalIdStr, final String commandParam,
            final String apiRequestBodyAsJson) {

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = loanId == null ? workingCapitalLoanRepository.findIdByExternalId(loanExternalId) : loanId;

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createWorkingCapitalLoanCharge(resolvedLoanId)
                .withJson(apiRequestBodyAsJson).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private CommandProcessingResult handleExecuteLoanChargeWithChargeId(final Long loanId, final String loanExternalIdStr,
            final Long loanChargeId, final String loanChargeExternalIdStr, final String commandParam, final String apiRequestBodyAsJson) {

        final ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        final ExternalId loanChargeExternalId = ExternalIdFactory.produce(loanChargeExternalIdStr);

        final Long resolvedLoanId = loanId == null ? workingCapitalLoanRepository.findIdByExternalId(loanExternalId) : loanId;
        final Long resolvedLoanChargeId = loanChargeId == null ? loanChargeRepository.findIdByExternalId(loanChargeExternalId)
                : loanChargeId;

        if (!CommandParameterUtil.is(commandParam, WorkingCapitalLoanChargeConstants.ADJUSTMENT_LOAN_CHARGE_COMMAND)) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .adjustmentForWorkingCapitalLoanCharge(resolvedLoanId, resolvedLoanChargeId).withJson(apiRequestBodyAsJson).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
