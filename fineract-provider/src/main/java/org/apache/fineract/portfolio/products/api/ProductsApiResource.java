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
package org.apache.fineract.portfolio.products.api;

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
import org.apache.fineract.infrastructure.core.exception.ResourceNotFoundException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.products.constants.ProductsApiConstants;
import org.apache.fineract.portfolio.products.data.ProductData;
import org.apache.fineract.portfolio.products.service.ShareProductReadPlatformService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Path("/v1/products/{type}")
@Component
@Tag(name = "Products", description = "")
@RequiredArgsConstructor
public class ProductsApiResource {

    private final ApplicationContext applicationContext;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<ProductData> toApiJsonSerializer;
    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@PathParam("type") @Parameter(description = "type") final String productType,
            @Context final UriInfo uriInfo) {
        String serviceName = productType + ProductsApiConstants.READPLATFORM_NAME;
        try {
            ShareProductReadPlatformService service = (ShareProductReadPlatformService) this.applicationContext.getBean(serviceName);
            ProductData data = service.retrieveTemplate();
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());
        } catch (BeansException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Share Product", description = "Retrieves a Share Product\n\n" + "Example Requests:\n" + "\n"
            + "products/share/1\n" + "\n" + "\n" + "products/share/1?template=true")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProductsApiResourceSwagger.GetProductsTypeProductIdResponse.class))) })
    public String retrieveProduct(@PathParam("productId") @Parameter(description = "productId") final Long productId,
            @PathParam("type") @Parameter(description = "type") final String productType, @Context final UriInfo uriInfo) {
        try {
            String serviceName = productType + ProductsApiConstants.READPLATFORM_NAME;
            ShareProductReadPlatformService service = (ShareProductReadPlatformService) this.applicationContext.getBean(serviceName);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            ProductData data = service.retrieveOne(productId, settings.isTemplate());
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());
        } catch (BeansException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Share Products", description = "Lists Share Products\n\n" + "Mandatory Fields: limit, offset\n\n"
            + "Example Requests:\n" + "\n" + "shareproducts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProductsApiResourceSwagger.GetProductsTypeResponse.class))) })
    public String retrieveAllProducts(@PathParam("type") @Parameter(description = "type") final String productType,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit, @Context final UriInfo uriInfo) {
        try {
            String serviceName = productType + ProductsApiConstants.READPLATFORM_NAME;
            ShareProductReadPlatformService service = (ShareProductReadPlatformService) this.applicationContext.getBean(serviceName);
            Page<ProductData> data = service.retrieveAllProducts(offset, limit);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());
        } catch (BeansException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Share Product", description = "Creates a Share Product\n\n"
            + "Mandatory Fields: name, shortName, description, currencyCode, digitsAfterDecimal,inMultiplesOf, locale, totalShares, unitPrice, nominalShares,allowDividendCalculationForInactiveClients,accountingRule\n\n"
            + "Mandatory Fields for Cash based accounting (accountingRule = 2): shareReferenceId, shareSuspenseId, shareEquityId, incomeFromFeeAccountId\n\n"
            + "Optional Fields: sharesIssued, minimumShares, maximumShares, minimumActivePeriodForDividends, minimumactiveperiodFrequencyType, lockinPeriodFrequency, lockinPeriodFrequencyType, marketPricePeriods, chargesSelected")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ProductsApiResourceSwagger.PostProductsTypeRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProductsApiResourceSwagger.PostProductsTypeResponse.class))) })
    public String createProduct(@PathParam("type") @Parameter(description = "type") final String productType,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        commandWrapper = new CommandWrapperBuilder().createProduct(productType).withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    @POST
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String handleCommands(@PathParam("type") @Parameter(description = "type") final String productType,
            @PathParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @SuppressWarnings("unused") @Context final UriInfo uriInfo, @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createProductCommand(productType, commandParam, productId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Share Product", description = "Updates a Share Product")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ProductsApiResourceSwagger.PutProductsTypeProductIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProductsApiResourceSwagger.PutProductsTypeProductIdResponse.class))) })
    public String updateProduct(@PathParam("type") @Parameter(description = "type") final String productType,
            @PathParam("productId") @Parameter(description = "productId") final Long productId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateProduct(productType, productId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
