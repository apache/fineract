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

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.products.constants.ProductsApiConstants;
import org.apache.fineract.portfolio.products.data.ProductData;
import org.apache.fineract.portfolio.products.exception.ResourceNotFoundException;
import org.apache.fineract.portfolio.products.service.ProductReadPlatformService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/products/{type}")
@Component
@Scope("singleton")
@Api(value = "Products", description = "")
public class ProductsApiResource {

    private final ApplicationContext applicationContext;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<ProductData> toApiJsonSerializer;
    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ProductsApiResource(final ApplicationContext applicationContext, final ApiRequestParameterHelper apiRequestParameterHelper,
            final DefaultToApiJsonSerializer<ProductData> toApiJsonSerializer, final PlatformSecurityContext platformSecurityContext,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.applicationContext = applicationContext;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.platformSecurityContext = platformSecurityContext;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@PathParam("type") @ApiParam(value = "type") final String productType, @Context final UriInfo uriInfo) {
        String serviceName = productType + ProductsApiConstants.READPLATFORM_NAME;
        try {
            ProductReadPlatformService service = (ProductReadPlatformService) this.applicationContext.getBean(serviceName);
            ProductData data = service.retrieveTemplate();
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());    
        }catch(BeansException e) {
            throw new ResourceNotFoundException() ;
        }
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Share Product", httpMethod = "GET", notes = "Retrieves a Share Product\n\n" + "Example Requests:\n" + "\n" + "products/share/1\n" + "\n" + "\n" + "products/share/1?template=true")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ProductsApiResourceSwagger.GetProductsTypeProductIdResponse.class)})
    public String retrieveProduct(@PathParam("productId") @ApiParam(value = "productId") final Long productId, @PathParam("type") @ApiParam(value = "type") final String productType,
            @Context final UriInfo uriInfo) {
        try {
            String serviceName = productType + ProductsApiConstants.READPLATFORM_NAME;
            ProductReadPlatformService service = (ProductReadPlatformService) this.applicationContext.getBean(serviceName);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            ProductData data = service.retrieveOne(productId, settings.isTemplate());
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());
        } catch (BeansException e) {
            throw new ResourceNotFoundException();
        }
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Share Products", httpMethod = "GET", notes = "Lists Share Products\n\n" + "Mandatory Fields: limit, offset\n\n" + "Example Requests:\n" + "\n" + "shareproducts")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ProductsApiResourceSwagger.GetProductsTypeResponse.class)})
    public String retrieveAllProducts(@PathParam("type") @ApiParam(value = "type") final String productType, @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @Context final UriInfo uriInfo) {
        try {
            String serviceName = productType + ProductsApiConstants.READPLATFORM_NAME;
            ProductReadPlatformService service = (ProductReadPlatformService) this.applicationContext.getBean(serviceName);
            Page<ProductData> data = service.retrieveAllProducts(offset, limit);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());    
        }catch(BeansException e) {
            throw new ResourceNotFoundException();
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Share Product", httpMethod = "POST", notes = "Creates a Share Product\n\n" + "Mandatory Fields: name, shortName, description, currencyCode, digitsAfterDecimal,inMultiplesOf, locale, totalShares, unitPrice, nominalShares,allowDividendCalculationForInactiveClients,accountingRule\n\n" + "Mandatory Fields for Cash based accounting (accountingRule = 2): shareReferenceId, shareSuspenseId, shareEquityId, incomeFromFeeAccountId\n\n" + "Optional Fields: sharesIssued, minimumShares, maximumShares, minimumActivePeriodForDividends, minimumactiveperiodFrequencyType, lockinPeriodFrequency, lockinPeriodFrequencyType, marketPricePeriods, chargesSelected")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ProductsApiResourceSwagger.PostProductsTypeRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ProductsApiResourceSwagger.PostProductsTypeResponse.class)})
    public String createProduct(@PathParam("type") @ApiParam(value = "type") final String productType, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
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
    public String handleCommands(@PathParam("type") @ApiParam(value = "type") final String productType, @PathParam("productId") @ApiParam(value = "productId") final Long productId,
            @QueryParam("command") @ApiParam(value = "command") final String commandParam, @SuppressWarnings("unused") @Context final UriInfo uriInfo, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createProductCommand(productType, commandParam, productId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Share Product", httpMethod = "PUT", notes = "Updates a Share Product")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ProductsApiResourceSwagger.PutProductsTypeProductIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ProductsApiResourceSwagger.PutProductsTypeProductIdResponse.class)})
    public String updateProduct(@PathParam("type") @ApiParam(value = "type")final String productType, @PathParam("productId") @ApiParam(value = "productId")final Long productId,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateProduct(productType, productId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
