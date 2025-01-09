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
package org.apache.fineract.investor.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformUserRightsContext;
import org.apache.fineract.investor.config.InvestorModuleIsEnabledCondition;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.service.ExternalAssetOwnerLoanProductAttributesReadService;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Path("/v1/external-asset-owners/loan-product")
@Component
@Tag(name = "External Asset Owner Loan Product Attributes", description = "External Asset Owner Loan Product Attributes")
@RequiredArgsConstructor
@Conditional(InvestorModuleIsEnabledCondition.class)
public class ExternalAssetOwnerLoanProductAttributesApiResource {

    private final PlatformUserRightsContext platformUserRightsContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ExternalAssetOwnerLoanProductAttributesReadService externalAssetOwnerLoanProductAttributesReadService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<ExternalTransferLoanProductAttributesData> toApiJsonSerializer;

    @POST
    @Path("/{loanProductId}/attributes")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnerLoanProductAttributesApiResourceSwagger.PostExternalAssetOwnerLoanProductAttributeRequest.class)))
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid Request"),
            @ApiResponse(responseCode = "403", description = "Resource Already Exists"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    public CommandProcessingResult postExternalAssetOwnerLoanProductAttribute(
            @PathParam("loanProductId") @Parameter(description = "loanProductId") final Long loanProductId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandWrapper request = builder.createExternalAssetOwnerLoanProductAttribute(loanProductId).build();

        return commandsSourceWritePlatformService.logCommandSource(request);
    }

    @GET
    @Path("/{loanProductId}/attributes")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owner Loan Product Attributes" }, summary = "Retrieve All Loan Product Attributes", description = "Retrieves all Loan Product Attributes with a given loanProductId", parameters = {
                    @Parameter(name = "loanProductId", description = "loanProductId"),
                    @Parameter(name = "attributeKey", description = "attributeKey") })
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "A paginated group of loan product attributes is returned"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    public Page<ExternalTransferLoanProductAttributesData> getExternalAssetOwnerLoanProductAttributes(@Context final UriInfo uriInfo,
            @PathParam("loanProductId") @Parameter(description = "loanProductId") final Long loanProductId,
            @QueryParam("attributeKey") @Parameter(description = "attributeKey") final String attributeKey) {
        platformUserRightsContext.isAuthenticated();

        return externalAssetOwnerLoanProductAttributesReadService.retrieveAllLoanProductAttributesByLoanProductId(loanProductId,
                attributeKey);
    }

    @PUT
    @Path("/{loanProductId}/attributes/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnerLoanProductAttributesApiResourceSwagger.PutExternalAssetOwnerLoanProductAttributeRequest.class)))
    @Operation(tags = {
            "External Asset Owner Loan Product Attributes" }, summary = "Update a Loan Product Attribute", description = "Updates a loan product attribute with a given loan product id and attribute id", parameters = {
                    @Parameter(name = "loanProductId", description = "loanProductId"),
                    @Parameter(name = "attributeId", description = "attributeId") })
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "A loan product attribute filtered by id is returned"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    public CommandProcessingResult updateLoanProductAttribute(
            @PathParam("loanProductId") @Parameter(description = "loanProductId") final Long loanProductId,
            @PathParam("id") @Parameter(description = "attributeId") final Long attributeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandWrapper request = builder.updateExternalAssetOwnerLoanProductAttribute(loanProductId, attributeId).build();

        return commandsSourceWritePlatformService.logCommandSource(request);
    }

}
