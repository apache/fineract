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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.portfolio.tax.command.TaxGroupCreateCommand;
import org.apache.fineract.portfolio.tax.command.TaxGroupUpdateCommand;
import org.apache.fineract.portfolio.tax.data.TaxGroupCreateRequest;
import org.apache.fineract.portfolio.tax.data.TaxGroupCreateResponse;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.data.TaxGroupUpdateRequest;
import org.apache.fineract.portfolio.tax.data.TaxGroupUpdateResponse;
import org.apache.fineract.portfolio.tax.service.TaxReadService;
import org.springframework.stereotype.Component;

@Path("/v1/taxes/group")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Tax Group", description = "This defines the Tax Group")
@RequiredArgsConstructor
public class TaxGroupApiResource {

    private final TaxReadService readPlatformService;
    private final CommandDispatcher dispatcher;

    @GET
    @Operation(summary = "List Tax Group", operationId = "retrieveAllTaxGroups", description = "List Tax Group")
    public List<TaxGroupData> retrieveAllTaxGroups() {
        return readPlatformService.retrieveAllTaxGroups();
    }

    @GET
    @Path("{taxGroupId}")
    @Operation(summary = "Retrieve Tax Group", operationId = "retrieveOneTaxGroup", description = "Retrieve Tax Group")
    public TaxGroupData retrieveTaxGroup(@PathParam("taxGroupId") @Parameter(description = "taxGroupId") final Long taxGroupId,
            @QueryParam("template") @DefaultValue("false") Boolean template) {
        return Boolean.TRUE.equals(template) ? readPlatformService.retrieveTaxGroupWithTemplate(taxGroupId)
                : readPlatformService.retrieveTaxGroupData(taxGroupId);
    }

    @GET
    @Path("template")
    @Operation(summary = "Retrieve Tax Group Template", operationId = "retrieveTemplateTaxGroup")
    public TaxGroupData retrieveTemplate() {
        return readPlatformService.retrieveTaxGroupTemplate();
    }

    @POST
    @Operation(summary = "Create a new Tax Group", operationId = "createTaxGroup", description = """
            Create a new Tax Group

            Mandatory Fields:

            - name
            - taxComponents

            Mandatory Fields in taxComponents:

            - taxComponentId

            Optional Fields in taxComponents:

            - id
            - startDate
            - endDate""")
    public TaxGroupCreateResponse createTaxGroup(@RequestBody(required = true) TaxGroupCreateRequest request) {
        final var command = new TaxGroupCreateCommand();
        command.setPayload(request);

        final Supplier<TaxGroupCreateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @PUT
    @Path("{taxGroupId}")
    @Operation(summary = "Update Tax Group", operationId = "updateTaxGroup", description = "Updates Tax Group. Only end date can be up-datable and can insert new tax components.")
    public TaxGroupUpdateResponse updateTaxGroup(@PathParam("taxGroupId") @Parameter(description = "taxGroupId") final Long taxGroupId,
            @RequestBody(required = true) TaxGroupUpdateRequest request) {
        requireNonNull(taxGroupId, "taxGroupId is required");

        request.setId(taxGroupId);

        final var command = new TaxGroupUpdateCommand();
        command.setPayload(request);

        final Supplier<TaxGroupUpdateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }
}
