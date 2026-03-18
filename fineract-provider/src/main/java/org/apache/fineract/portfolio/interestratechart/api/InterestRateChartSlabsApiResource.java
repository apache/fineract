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
package org.apache.fineract.portfolio.interestratechart.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.portfolio.interestratechart.command.InterestRateChartSlabsCreateCommand;
import org.apache.fineract.portfolio.interestratechart.command.InterestRateChartSlabsDeleteCommand;
import org.apache.fineract.portfolio.interestratechart.command.InterestRateChartSlabsUpdateCommand;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabData;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsCreateRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsCreateResponse;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsDeleteRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsDeleteResponse;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsUpdateRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsUpdateResponse;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartSlabsReadService;
import org.springframework.stereotype.Component;

@Path("/v1/interestratecharts/{chartId}/chartslabs")
@Component
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Tag(name = "Interest Rate Slab (A.K.A interest bands)", description = "The slabs a.k.a interest bands are associated with Interest Rate Chart. These bands allow to define different interest rates for different deposit term periods.")
@RequiredArgsConstructor
public class InterestRateChartSlabsApiResource {

    private final InterestRateChartSlabsReadService interestRateChartSlabsReadService;
    private final CommandPipeline commandPipeline;

    @GET
    @Path("template")
    public InterestRateChartSlabData template(@PathParam("chartId") final Long chartId) {
        return interestRateChartSlabsReadService.retrieveTemplate();
    }

    @GET
    @Operation(summary = "Retrieve all Slabs", description = "Retrieve list of slabs associated with a chart\n" + "\n"
            + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs")
    public List<InterestRateChartSlabData> retrieveAll(@PathParam("chartId") final Long chartId) {
        return interestRateChartSlabsReadService.retrieveAll(chartId);
    }

    @GET
    @Path("{chartSlabId}")
    @Operation(summary = "Retrieve a Slab", description = "Retrieve a slab associated with an Interest rate chart\n" + "\n"
            + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs/1\n")
    public InterestRateChartSlabData retrieveOne(@PathParam("chartId") final Long chartId,
            @PathParam("chartSlabId") final Long chartSlabId) {
        return interestRateChartSlabsReadService.retrieveOne(chartId, chartSlabId);
    }

    @POST
    @Operation(summary = "Create a Slab", description = "Creates a new interest rate slab for an interest rate chart.\n"
            + "Mandatory Fields\n" + "periodType, fromPeriod, annualInterestRate\n" + "Optional Fields\n" + "toPeriod and description\n"
            + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsCreateResponse.class)))
    public InterestRateChartSlabsCreateResponse create(@PathParam("chartId") final Long chartId,
            final InterestRateChartSlabsCreateRequest request) {
        request.setChartId(chartId);
        final var command = new InterestRateChartSlabsCreateCommand();
        command.setPayload(request);
        final Supplier<InterestRateChartSlabsCreateResponse> responseSupplier = commandPipeline.send(command);
        return responseSupplier.get();
    }

    @PUT
    @Path("{chartSlabId}")
    @Operation(summary = "Update a Slab", description = "It updates the Slab from chart")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsUpdateResponse.class)))
    public InterestRateChartSlabsUpdateResponse update(@PathParam("chartId") final Long chartId,
            @PathParam("chartSlabId") final Long chartSlabId, final InterestRateChartSlabsUpdateRequest request) {
        request.setChartId(chartId);
        request.setChartSlabId(chartSlabId);

        final var command = new InterestRateChartSlabsUpdateCommand();
        command.setPayload(request);

        final Supplier<InterestRateChartSlabsUpdateResponse> responseSupplier = commandPipeline.send(command);

        return responseSupplier.get();
    }

    @DELETE
    @Path("{chartSlabId}")
    @Operation(summary = "Delete a Slab", description = "Delete a Slab from a chart")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsDeleteResponse.class)))
    public InterestRateChartSlabsDeleteResponse delete(@PathParam("chartId") final Long chartId,
            @PathParam("chartSlabId") final Long chartSlabId) {
        final var command = new InterestRateChartSlabsDeleteCommand();
        command.setPayload(InterestRateChartSlabsDeleteRequest.builder().chartId(chartId).chartSlabId(chartSlabId).build());
        final Supplier<InterestRateChartSlabsDeleteResponse> responseSupplier = commandPipeline.send(command);
        return responseSupplier.get();
    }
}
