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
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.api.SavingsProductsApiResource;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.springframework.stereotype.Component;

@Path("/v1/self/savingsproducts")
@Component
@Tag(name = "Self Savings Products", description = "")
@RequiredArgsConstructor
public class SelfSavingsProductsApiResource {

    private final SavingsProductsApiResource savingsProductsApiResource;
    private final AppuserClientMapperReadService appUserClientMapperReadService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@QueryParam(SavingsApiConstants.clientIdParamName) final Long clientId, @Context final UriInfo uriInfo) {

        this.appUserClientMapperReadService.validateAppuserClientsMapping(clientId);
        return this.savingsProductsApiResource.retrieveAll(uriInfo);

    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam(SavingsApiConstants.productIdParamName) final Long productId,
            @QueryParam(SavingsApiConstants.clientIdParamName) final Long clientId, @Context final UriInfo uriInfo) {

        this.appUserClientMapperReadService.validateAppuserClientsMapping(clientId);
        return this.savingsProductsApiResource.retrieveOne(productId, uriInfo);

    }

}
