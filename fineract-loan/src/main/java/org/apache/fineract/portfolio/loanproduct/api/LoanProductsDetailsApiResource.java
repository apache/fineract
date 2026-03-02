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
package org.apache.fineract.portfolio.loanproduct.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductBasicDetailsData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadBasicDetailsService;
import org.springframework.stereotype.Component;

@Path("/v1/loanproducts")
@Component
@Tag(name = "Loan Products Details", description = "Loan product basic details to be listed")
@RequiredArgsConstructor
public class LoanProductsDetailsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOANPRODUCT";
    private final PlatformSecurityContext context;
    private final List<LoanProductReadBasicDetailsService> loanProductReadBasicDetailsServices;

    @GET
    @Path("basic-details")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loan Products with basic details", description = "Lists Loan Products with basic details to be listed")
    public Collection<LoanProductBasicDetailsData> fetchProducts(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        Collection<LoanProductBasicDetailsData> products = new ArrayList<>();
        loanProductReadBasicDetailsServices.forEach(service -> products.addAll(service.retrieveProducts()));
        return products;
    }

}
