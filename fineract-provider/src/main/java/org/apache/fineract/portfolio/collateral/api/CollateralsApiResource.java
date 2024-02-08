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
package org.apache.fineract.portfolio.collateral.api;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateral.data.CollateralData;
import org.apache.fineract.portfolio.collateral.service.CollateralReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loans/{loanId}/collaterals")
@Component
@Tag(name = "Loan Collateral", description = "In lending agreements, collateral is a borrower's pledge of specific property to a lender, to secure repayment of a loan. The collateral serves as protection for a lender against a borrower's default - that is, any borrower failing to pay the principal and interest under the terms of a loan obligation. If a borrower does default on a loan (due to insolvency or other event), that borrower forfeits (gives up) the property pledged as collateral - and the lender then becomes the owner of the collateral")
@RequiredArgsConstructor
public class CollateralsApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "type", "value", "description", "allowedCollateralTypes", "currency"));

    private static final String RESOURCE_NAME_FOR_PERMISSION = "COLLATERAL";

    private final CollateralReadPlatformService collateralReadPlatformService;
    private final DefaultToApiJsonSerializer<CollateralData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Collateral Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "loans/1/collaterals/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollateralsApiResourceSwagger.GetLoansLoanIdCollateralsTemplateResponse.class))) })
    public String newCollateralTemplate(@Context final UriInfo uriInfo,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
        final CollateralData collateralData = CollateralData.template(codeValues);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, collateralData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loan Collaterals", description = "Example Requests:\n" + "\n" + "loans/1/collaterals\n" + "\n" + "\n"
            + "loans/1/collaterals?fields=value,description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CollateralsApiResourceSwagger.GetLoansLoanIdCollateralsResponse.class)))) })
    public String retrieveCollateralDetails(@Context final UriInfo uriInfo,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        final List<CollateralData> collateralDatas = this.collateralReadPlatformService.retrieveCollateralsForValidLoan(loanId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.apiJsonSerializerService.serialize(settings, collateralDatas, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Collateral", description = "Example Requests:\n" + "\n" + "/loans/1/collaterals/1\n" + "\n" + "\n"
            + "/loans/1/collaterals/1?fields=description,description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollateralsApiResourceSwagger.GetLoansLoanIdCollateralsResponse.class))) })
    public String retrieveCollateralDetails(@Context final UriInfo uriInfo,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long CollateralId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        CollateralData collateralData = this.collateralReadPlatformService.retrieveCollateral(loanId, CollateralId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService
                    .retrieveCodeValuesByCode(CollateralApiConstants.COLLATERAL_CODE_NAME);
            collateralData = collateralData.template(collateralData, codeValues);
        }

        return this.apiJsonSerializerService.serialize(settings, collateralData, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Collateral", description = "Note: Currently, Collaterals may be added only before a Loan is approved")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CollateralsApiResourceSwagger.PostLoansLoanIdCollateralsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollateralsApiResourceSwagger.PostLoansLoanIdCollateralsResponse.class))) })
    public String createCollateral(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCollateral(loanId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Collateral")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CollateralsApiResourceSwagger.PutLoansLoandIdCollateralsCollateralIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollateralsApiResourceSwagger.PutLoansLoanIdCollateralsCollateralIdResponse.class))) })
    public String updateCollateral(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @Parameter(hidden = true) final String jsonRequestBody) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCollateral(loanId, collateralId).withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Remove a Collateral", description = "Note: A collateral can only be removed from Loans that are not yet approved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CollateralsApiResourceSwagger.DeleteLoansLoanIdCollateralsCollateralIdResponse.class))) })
    public String deleteCollateral(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCollateral(loanId, collateralId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }
}
