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
package org.apache.fineract.portfolio.workingcapitalloanbreach.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachRequest;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachTemplateResponse;
import org.apache.fineract.portfolio.workingcapitalloanbreach.service.WorkingCapitalBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/working-capital/breach")
@Tag(name = "Working Capital Breach", description = "Working Capital Breach")
@RequiredArgsConstructor
public class WorkingCapitalBreachApiResource {

    private final PlatformSecurityContext context;
    private final WorkingCapitalBreachReadPlatformService breachReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<String> jsonSerializer;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalBreachTemplate", summary = "Retrieve Working Capital Breach template", description = "Returns breach options for frequency type and amount calculation type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalBreachTemplateResponse.class))) })
    public WorkingCapitalBreachTemplateResponse retrieveTemplate() {
        this.context.authenticatedUser().validateHasReadPermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        return this.breachReadPlatformService.retrieveTemplate();
    }

    @GET
    @Path("breaches")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveAllWorkingCapitalBreaches", summary = "List Working Capital Breaches")
    public List<WorkingCapitalBreachData> retrieveAll() {
        this.context.authenticatedUser().validateHasReadPermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        return this.breachReadPlatformService.retrieveAll();
    }

    @GET
    @Path("breaches/{breachId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalBreach", summary = "Retrieve Working Capital Breach")
    public WorkingCapitalBreachData retrieveOne(@PathParam("breachId") @Parameter(description = "breachId") final Long breachId) {
        this.context.authenticatedUser().validateHasReadPermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        return this.breachReadPlatformService.retrieveOne(breachId);
    }

    @POST
    @Path("breaches")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "createWorkingCapitalBreach", summary = "Create Working Capital Breach")
    public CommandProcessingResult create(final WorkingCapitalBreachRequest request) {
        this.context.authenticatedUser().validateHasCreatePermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createWorkingCapitalBreach()
                .withJson(jsonSerializer.serialize(request)).build();
        return this.commandSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("breaches/{breachId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "updateWorkingCapitalBreach", summary = "Update Working Capital Breach")
    public CommandProcessingResult update(@PathParam("breachId") @Parameter(description = "breachId") final Long breachId,
            final WorkingCapitalBreachRequest request) {
        this.context.authenticatedUser().validateHasUpdatePermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateWorkingCapitalBreach(breachId)
                .withJson(jsonSerializer.serialize(request)).build();
        return this.commandSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("breaches/{breachId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "deleteWorkingCapitalBreach", summary = "Delete Working Capital Breach")
    public CommandProcessingResult delete(@PathParam("breachId") @Parameter(description = "breachId") final Long breachId) {
        this.context.authenticatedUser().validateHasDeletePermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteWorkingCapitalBreach(breachId).build();
        return this.commandSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
