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

import io.swagger.annotations.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.*;

@Path("/loans/{loanId}/collaterals")
@Component
@Scope("singleton")
@Api(value = "Loan Collateral", description = "In lending agreements, collateral is a borrower's pledge of specific property to a lender, to secure repayment of a loan. The collateral serves as protection for a lender against a borrower's default - that is, any borrower failing to pay the principal and interest under the terms of a loan obligation. If a borrower does default on a loan (due to insolvency or other event), that borrower forfeits (gives up) the property pledged as collateral - and the lender then becomes the owner of the collateral")
public class CollateralsApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "type", "value", "description",
            "allowedCollateralTypes", "currency"));

    private final String resourceNameForPermission = "COLLATERAL";

    private final CollateralReadPlatformService collateralReadPlatformService;
    private final DefaultToApiJsonSerializer<CollateralData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public CollateralsApiResource(final PlatformSecurityContext context, final CollateralReadPlatformService collateralReadPlatformService,
            final DefaultToApiJsonSerializer<CollateralData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.collateralReadPlatformService = collateralReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Collateral Details Template", httpMethod = "GET", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "loans/1/collaterals/template")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CollateralsApiResourceSwagger.GetLoansLoanIdCollateralsTemplateResponse.class)})
    public String newCollateralTemplate(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
        final CollateralData collateralData = CollateralData.template(codeValues);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, collateralData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Loan Collaterals", httpMethod = "GET", notes = "Example Requests:\n" + "\n" + "loans/1/collaterals\n" + "\n" + "\n" + "loans/1/collaterals?fields=value,description")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CollateralsApiResourceSwagger.GetLoansLoanIdCollateralsResponse.class, responseContainer = "List")})
    public String retrieveCollateralDetails(@Context final UriInfo uriInfo, @PathParam("loanId") @ApiParam(value = "loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final List<CollateralData> CollateralDatas = this.collateralReadPlatformService.retrieveCollateralsForValidLoan(loanId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.apiJsonSerializerService.serialize(settings, CollateralDatas, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Collateral", httpMethod = "GET", notes = "Example Requests:\n" + "\n" + "/loans/1/collaterals/1\n" + "\n" + "\n" + "/loans/1/collaterals/1?fields=value,description")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CollateralsApiResourceSwagger.GetLoansLoanIdCollateralsResponse.class)})
    public String retrieveCollateralDetails(@Context final UriInfo uriInfo, @PathParam("loanId") @ApiParam(value = "loanId")final Long loanId,
            @PathParam("collateralId") @ApiParam(value = "collateralId") final Long CollateralId) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        CollateralData CollateralData = this.collateralReadPlatformService.retrieveCollateral(loanId, CollateralId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService
                    .retrieveCodeValuesByCode(CollateralApiConstants.COLLATERAL_CODE_NAME);
            CollateralData = CollateralData.template(CollateralData, codeValues);
        }

        return this.apiJsonSerializerService.serialize(settings, CollateralData, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Collateral", httpMethod = "POST", notes = "Note: Currently, Collaterals may be added only before a Loan is approved")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = CollateralsApiResourceSwagger.PostLoansLoanIdCollateralsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CollateralsApiResourceSwagger.PostLoansLoanIdCollateralsResponse.class)})
    public String createCollateral(@PathParam("loanId") @ApiParam(value = "loanId") final Long loanId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCollateral(loanId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Collateral", httpMethod = "PUT")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = CollateralsApiResourceSwagger.PutLoansLoandIdCollateralsCollateralIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CollateralsApiResourceSwagger.PutLoansLoanIdCollateralsCollateralIdResponse.class)})
    public String updateCollateral(@PathParam("loanId") @ApiParam(value = "loanId") final Long loanId, @PathParam("collateralId") @ApiParam(value = "collateralId") final Long collateralId,
            @ApiParam(hidden = true) final String jsonRequestBody) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCollateral(loanId, collateralId).withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Remove a Collateral", httpMethod = "DELETE", notes = "Note: A collateral can only be removed from Loans that are not yet approved.")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CollateralsApiResourceSwagger.DeleteLoansLoanIdCollateralsCollateralIdResponse.class)})
    public String deleteCollateral(@PathParam("loanId") @ApiParam(value = "loanId") final Long loanId, @PathParam("collateralId") @ApiParam(value = "collateralId")final Long collateralId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCollateral(loanId, collateralId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }
}