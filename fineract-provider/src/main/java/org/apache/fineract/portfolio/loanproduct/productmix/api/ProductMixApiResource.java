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
package org.apache.fineract.portfolio.loanproduct.productmix.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.Collection;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.productmix.command.ProductMixCreateCommand;
import org.apache.fineract.portfolio.loanproduct.productmix.command.ProductMixDeleteCommand;
import org.apache.fineract.portfolio.loanproduct.productmix.command.ProductMixUpdateCommand;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixCreateRequest;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixCreateResponse;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixData;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixDeleteRequest;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixDeleteResponse;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixUpdateRequest;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixUpdateResponse;
import org.apache.fineract.portfolio.loanproduct.productmix.service.ProductMixReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loanproducts/{productId}/productmix")
@Component
@Tag(name = "Product Mix")
@RequiredArgsConstructor
public class ProductMixApiResource {

    private final CommandDispatcher commandDispatcher;
    private final ProductMixReadPlatformService productMixReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Product Mix Template", operationId = "retrieveTemplateProductMix")
    public ProductMixData retrieveTemplate(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        ProductMixData productMixData = this.productMixReadPlatformService.retrieveLoanProductMixDetails(productId);

        if (uriInfo.getQueryParameters().containsKey("template")) {
            final Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAvailableLoanProductsForMix();
            productMixData = ProductMixData.withTemplateOptions(productMixData, productOptions);
        }
        return productMixData;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Product Mix", operationId = "createProductMix")
    public ProductMixCreateResponse createProductMix(@PathParam("productId") final Long productId,
            @Valid final ProductMixCreateRequest request) {

        request.setProductId(productId);

        final var command = new ProductMixCreateCommand();
        command.setPayload(request);

        final Supplier<ProductMixCreateResponse> response = commandDispatcher.dispatch(command);
        return response.get();
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Product Mix", operationId = "updateProductMix")
    public ProductMixUpdateResponse updateProductMix(@PathParam("productId") final Long productId,
            @Valid final ProductMixUpdateRequest request) {

        request.setProductId(productId);

        final var command = new ProductMixUpdateCommand();
        command.setPayload(request);

        final Supplier<ProductMixUpdateResponse> response = commandDispatcher.dispatch(command);
        return response.get();
    }

    @DELETE
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Product Mix", operationId = "deleteProductMix")
    public ProductMixDeleteResponse deleteProductMix(@PathParam("productId") final Long productId) {

        final var command = new ProductMixDeleteCommand();
        command.setPayload(ProductMixDeleteRequest.builder().productId(productId).build());

        final Supplier<ProductMixDeleteResponse> response = commandDispatcher.dispatch(command);
        return response.get();
    }
}
