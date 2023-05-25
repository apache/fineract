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

package org.apache.fineract.portfolio.self.products.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.accounts.constants.ShareAccountApiConstants;
import org.apache.fineract.portfolio.products.api.ProductsApiResource;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.springframework.stereotype.Component;

@Path("/v1/self/products/share")
@Component
@Tag(name = "Self Share Products", description = "")
@RequiredArgsConstructor
public class SelfShareProductsApiResource {

    private final ProductsApiResource productsApiResource;
    private final AppuserClientMapperReadService appUserClientMapperReadService;

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveProduct(@QueryParam(ShareAccountApiConstants.clientid_paramname) final Long clientId,
            @PathParam("productId") final Long productId, @PathParam("type") final String productType, @Context final UriInfo uriInfo) {
        this.appUserClientMapperReadService.validateAppuserClientsMapping(clientId);
        return this.productsApiResource.retrieveProduct(productId, ShareAccountApiConstants.shareEntityType, uriInfo);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllProducts(@QueryParam(ShareAccountApiConstants.clientid_paramname) final Long clientId,
            @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit, @Context final UriInfo uriInfo) {
        this.appUserClientMapperReadService.validateAppuserClientsMapping(clientId);
        return this.productsApiResource.retrieveAllProducts(ShareAccountApiConstants.shareEntityType, offset, limit, uriInfo);

    }

}
