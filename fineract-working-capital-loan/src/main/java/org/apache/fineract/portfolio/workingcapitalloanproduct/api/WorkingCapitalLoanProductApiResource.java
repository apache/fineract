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
package org.apache.fineract.portfolio.workingcapitalloanproduct.api;

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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.service.WorkingCapitalLoanProductReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/working-capital-loan-products")
@Component
@Tag(name = "Working Capital Loan Products", description = "A Working Capital Loan Product is a template that is used when creating a Working Capital loan. This is a separate product type from standard loan products.")
@RequiredArgsConstructor
public class WorkingCapitalLoanProductApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME;

    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanProductReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "createWorkingCapitalLoanProduct", summary = "Create a Working Capital Loan Product", description = "Creates a new Working Capital Loan Product.\n\n"
            + "Mandatory Fields: name, shortName, currencyCode, digitsAfterDecimal, inMultiplesOf, amortizationType, npvDayCount, "
            + "principal, periodPaymentRate, repaymentEvery, repaymentFrequencyType\n\n"
            + "Optional Fields: externalId, fundId, startDate, closeDate, description, "
            + "delinquencyBucketClassification, minPrincipal, maxPrincipal, minPeriodPaymentRate, maxPeriodPaymentRate, "
            + "discount, paymentAllocation, allowAttributeOverrides")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.PostWorkingCapitalLoanProductsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.PostWorkingCapitalLoanProductsResponse.class))) })
    public CommandProcessingResult createWorkingCapitalLoanProduct(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createWorkingCapitalLoanProduct().withJson(apiRequestBodyAsJson)
                .build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveAllWorkingCapitalLoanProducts", summary = "List Working Capital Loan Products", description = "Lists all Working Capital Loan Products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsResponse.class)))) })
    public List<WorkingCapitalLoanProductData> retrieveAllWorkingCapitalLoanProducts() {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.retrieveAllWorkingCapitalLoanProducts();
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Working Capital Loan Product Details Template", operationId = "retrieveTemplateWorkingCapitalLoanProduct", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n"
            + "workingcapitalloanproducts/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsTemplateResponse.class))) })
    public WorkingCapitalLoanProductData retrieveTemplate() {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.retrieveNewWorkingCapitalLoanProductDetails();
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Working Capital Loan Product", operationId = "retrieveOneWorkingCapitalLoanProduct", description = "Retrieves a Working Capital Loan Product\n\n"
            + "Example Requests:\n" + "\n" + "workingcapitalloanproducts/1\n" + "\n" + "\n" + "workingcapitalloanproducts/1?template=true\n"
            + "\n" + "\n" + "workingcapitalloanproducts/1?fields=name,description,principal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsProductIdResponse.class))) })
    public WorkingCapitalLoanProductData retrieveWorkingCapitalLoanProductDetails(
            @PathParam("productId") @Parameter(description = "productId") final Long productId, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return getWorkingCapitalLoanProductDetails(productId, uriInfo);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Working Capital Loan Product", operationId = "updateWorkingCapitalLoanProduct", description = "Updates a Working Capital Loan Product")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.PutWorkingCapitalLoanProductsProductIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.PutWorkingCapitalLoanProductsProductIdResponse.class))) })
    public CommandProcessingResult updateWorkingCapitalLoanProduct(
            @PathParam("productId") @Parameter(description = "productId") final Long productId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return getUpdateWorkingCapitalLoanProductResult(apiRequestBodyAsJson, productId);
    }

    @DELETE
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Working Capital Loan Product", operationId = "deleteWorkingCapitalLoanProduct", description = "Deletes a Working Capital Loan Product if it is not in use")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.DeleteWorkingCapitalLoanProductsProductIdResponse.class))) })
    public CommandProcessingResult deleteWorkingCapitalLoanProduct(
            @PathParam("productId") @Parameter(description = "productId") final Long productId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteWorkingCapitalLoanProduct(productId).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("external-id/{externalProductId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Working Capital Loan Product", operationId = "deleteWorkingCapitalLoanProductByExternalId", description = "Deletes a Working Capital Loan Product by external ID if it is not in use")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.DeleteWorkingCapitalLoanProductsProductIdResponse.class))) })
    public CommandProcessingResult deleteWorkingCapitalLoanProduct(
            @PathParam("externalProductId") @Parameter(description = "externalProductId") final String externalProductId) {
        final ExternalId externalId = ExternalIdFactory.produce(externalProductId);

        final Long productId = resolveProductId(externalId);
        if (productId == null) {
            throw new WorkingCapitalLoanProductNotFoundException(externalId);
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteWorkingCapitalLoanProduct(productId).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("external-id/{externalProductId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Working Capital Loan Product", operationId = "retrieveOneWorkingCapitalLoanProductByExternalId", description = "Retrieves a Working Capital Loan Product by external ID\n\n"
            + "Example Requests:\n" + "\n" + "workingcapitalloanproducts/external-id/2075e308-d4a8-44d9-8203-f5a947b8c2f4")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.GetWorkingCapitalLoanProductsProductIdResponse.class))) })
    public WorkingCapitalLoanProductData retrieveWorkingCapitalLoanProductDetails(
            @PathParam("externalProductId") @Parameter(description = "externalProductId") final String externalProductId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ExternalId externalId = ExternalIdFactory.produce(externalProductId);

        final Long productId = resolveProductId(externalId);
        if (productId == null) {
            throw new WorkingCapitalLoanProductNotFoundException(externalId);
        }

        return getWorkingCapitalLoanProductDetails(productId, uriInfo);
    }

    @PUT
    @Path("external-id/{externalProductId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Working Capital Loan Product", operationId = "updateWorkingCapitalLoanProductByExternalId", description = "Updates a Working Capital Loan Product by external ID")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.PutWorkingCapitalLoanProductsProductIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanProductApiResourceSwagger.PutWorkingCapitalLoanProductsProductIdResponse.class))) })
    public CommandProcessingResult updateWorkingCapitalLoanProduct(
            @PathParam("externalProductId") @Parameter(description = "externalProductId") final String externalProductId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final ExternalId externalId = ExternalIdFactory.produce(externalProductId);

        final Long productId = resolveProductId(externalId);

        if (productId == null) {
            throw new WorkingCapitalLoanProductNotFoundException(externalId);
        }

        return getUpdateWorkingCapitalLoanProductResult(apiRequestBodyAsJson, productId);
    }

    private CommandProcessingResult getUpdateWorkingCapitalLoanProductResult(final String apiRequestBodyAsJson, final Long productId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateWorkingCapitalLoanProduct(productId)
                .withJson(apiRequestBodyAsJson).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private Long resolveProductId(final ExternalId externalProductId) {
        try {
            return readPlatformService.retrieveWorkingCapitalLoanProductByExternalId(externalProductId).getId();
        } catch (Exception e) {
            return null;
        }
    }

    private WorkingCapitalLoanProductData getWorkingCapitalLoanProductDetails(final Long productId, final UriInfo uriInfo) {
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final WorkingCapitalLoanProductData product = this.readPlatformService.retrieveWorkingCapitalLoanProduct(productId);
        if (settings.isTemplate()) {
            return product.applyTemplate(readPlatformService.retrieveNewWorkingCapitalLoanProductDetails());
        }
        return product;
    }
}
