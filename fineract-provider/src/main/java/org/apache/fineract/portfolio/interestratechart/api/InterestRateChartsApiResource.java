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
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants;
import org.apache.fineract.portfolio.interestratechart.command.InterestRateChartCreateCommand;
import org.apache.fineract.portfolio.interestratechart.command.InterestRateChartDeleteCommand;
import org.apache.fineract.portfolio.interestratechart.command.InterestRateChartUpdateCommand;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartCreateRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartCreateResponse;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartData;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartDeleteRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartDeleteResponse;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartUpdateRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartUpdateResponse;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadService;
import org.springframework.stereotype.Component;

@Path("/v1/interestratecharts")
@Component
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Tag(name = "Interest Rate Chart", description = "This defines an interest rate scheme that can be associated to a term deposit product. This will have a slab (band or range) of deposit periods and the associated interest rates applicable along with incentives for each band.")
@RequiredArgsConstructor
public class InterestRateChartsApiResource {

    private final InterestRateChartReadService chartReadPlatformService;

    private final CommandPipeline commandPipeline;

    @GET
    @Path("template")
    @Operation(summary = "Retrieve Chart Details Template", description = """
            This is a convenience resource. It can be useful when building maintenance user interface screens for creating a chart. The template data returned consists of any or all of: Field Defaults Allowed Value Lists
            Example Request: interestratecharts/template
            """)
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.GetInterestRateChartsTemplateResponse.class)))
    public InterestRateChartData template() {
        return chartReadPlatformService.template();
    }

    @GET
    @Operation(summary = "Retrieve all Charts", description = """
            Retrieve list of charts associated with a term deposit product(FD or RD).
            Example Requests: interestratecharts?productId=1
            """)
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.GetInterestRateChartsResponse.class))))
    public Collection<InterestRateChartData> retrieveAll(@QueryParam("productId") final Long productId) {

        return chartReadPlatformService.retrieveAllWithSlabs(productId);
    }

    @GET
    @Path("{chartId}")
    @Operation(summary = "Retrieve a Chart", description = "It retrieves the Interest Rate Chart\n" + "Example Requests:\n" + "\n"
            + "interestratecharts/1")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.GetInterestRateChartsResponse.class)))
    public InterestRateChartData retrieveOne(@PathParam("chartId") final Long chartId,
            @QueryParam("associations") final String associations) {
        InterestRateChartData chartData;
        if (associations != null && associations.contains(InterestRateChartApiConstants.chartSlabs)) {
            chartData = chartReadPlatformService.retrieveOneWithSlabs(chartId);
        } else {
            chartData = chartReadPlatformService.retrieveOne(chartId);
        }
        return chartData;
    }

    @POST
    @Operation(summary = "Create a Chart", description = "Creates a new chart which can be attached to a term deposit products (FD or RD).")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartCreateResponse.class)))
    public InterestRateChartCreateResponse create(final InterestRateChartCreateRequest request) {

        final var command = new InterestRateChartCreateCommand();
        command.setPayload(request);
        final Supplier<InterestRateChartCreateResponse> responseSupplier = commandPipeline.send(command);
        return responseSupplier.get();
    }

    @PUT
    @Path("{chartId}")
    @Operation(summary = "Update a Chart", description = "It updates the chart")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartUpdateResponse.class)))
    public InterestRateChartUpdateResponse update(@PathParam("chartId") final Long chartId, final InterestRateChartUpdateRequest request) {
        request.setId(chartId);
        final var command = new InterestRateChartUpdateCommand();
        command.setPayload(request);
        final Supplier<InterestRateChartUpdateResponse> responseSupplier = commandPipeline.send(command);
        return responseSupplier.get();
    }

    @DELETE
    @Path("{chartId}")
    @Operation(summary = "Delete a Chart", description = "It deletes the chart")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartDeleteResponse.class)))
    public InterestRateChartDeleteResponse delete(@PathParam("chartId") final Long chartId) {
        final var command = new InterestRateChartDeleteCommand();
        command.setPayload(InterestRateChartDeleteRequest.builder().chartId(chartId).build());
        final Supplier<InterestRateChartDeleteResponse> responseSupplier = commandPipeline.send(command);
        return responseSupplier.get();
    }
}
