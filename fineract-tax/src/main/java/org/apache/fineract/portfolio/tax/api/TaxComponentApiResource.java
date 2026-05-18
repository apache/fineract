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
package org.apache.fineract.portfolio.tax.api;

import static java.util.Objects.requireNonNull;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.portfolio.tax.command.TaxComponentCreateCommand;
import org.apache.fineract.portfolio.tax.command.TaxComponentUpdateCommand;
import org.apache.fineract.portfolio.tax.data.TaxComponentCreateRequest;
import org.apache.fineract.portfolio.tax.data.TaxComponentCreateResponse;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxComponentUpdateRequest;
import org.apache.fineract.portfolio.tax.data.TaxComponentUpdateResponse;
import org.apache.fineract.portfolio.tax.service.TaxReadService;
import org.springframework.stereotype.Component;

@Path("/v1/taxes/component")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Tax Components", description = "This defines the Tax Components")
@RequiredArgsConstructor
public class TaxComponentApiResource {

    private final TaxReadService readPlatformService;
    private final CommandDispatcher dispatcher;

    @GET
    @Operation(summary = "List Tax Components", operationId = "retrieveAllTaxComponents", description = "List Tax Components")
    public List<TaxComponentData> retrieveAllTaxComponents() {
        return readPlatformService.retrieveAllTaxComponents();
    }

    @GET
    @Path("{taxComponentId}")
    @Operation(summary = "Retrieve Tax Component", operationId = "retrieveOneTaxComponent", description = "Retrieve Tax Component")
    public TaxComponentData retrieveTaxComponent(
            @PathParam("taxComponentId") @Parameter(description = "taxComponentId") final Long taxComponentId) {
        return readPlatformService.retrieveTaxComponentData(taxComponentId);
    }

    @GET
    @Path("template")
    @Operation(summary = "Retrieve Tax Component Template", operationId = "retrieveTemplateTaxComponent")
    public TaxComponentData retrieveTemplate() {
        return readPlatformService.retrieveTaxComponentTemplate();
    }

    @POST
    @Operation(summary = "Create a new Tax Component", operationId = "createTaxComponent", description = """
            Creates a new Tax Component

            Mandatory Fields:

            - name
            - percentage

            Optional Fields:

            - debitAccountType
            - debitAccountId
            - creditAccountType
            - creditAccountId
            - startDate""")
    public TaxComponentCreateResponse createTaxComponent(@RequestBody(required = true) TaxComponentCreateRequest request) {
        final var command = new TaxComponentCreateCommand();
        command.setPayload(request);

        final Supplier<TaxComponentCreateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @PUT
    @Path("{taxComponentId}")
    @Operation(summary = "Update Tax Component", operationId = "updateTaxComponent", description = """
            Updates Tax component. Debit and credit account details cannot be modified.
            All the future tax components would be replaced with the new percentage.""")
    public TaxComponentUpdateResponse updateTaxCompoent(
            @PathParam("taxComponentId") @Parameter(description = "taxComponentId") final Long taxComponentId,
            @RequestBody(required = true) TaxComponentUpdateRequest request) {
        requireNonNull(taxComponentId, "taxComponentId is required");

        request.setId(taxComponentId);

        final var command = new TaxComponentUpdateCommand();
        command.setPayload(request);

        final Supplier<TaxComponentUpdateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }
}
