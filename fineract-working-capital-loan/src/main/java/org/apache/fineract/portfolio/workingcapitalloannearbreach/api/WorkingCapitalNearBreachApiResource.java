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
package org.apache.fineract.portfolio.workingcapitalloannearbreach.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachRequest;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.service.WorkingCapitalNearBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/working-capital/near-breach")
@Tag(name = "Working Capital Near Breach", description = "Working Capital Near Breach")
@RequiredArgsConstructor
public class WorkingCapitalNearBreachApiResource {

    private final PlatformSecurityContext context;
    private final WorkingCapitalNearBreachReadPlatformService breachReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<String> jsonSerializer;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveAllWorkingCapitalNearBreaches", summary = "List Working Capital Near Breaches")
    public List<WorkingCapitalNearBreachData> retrieveAll() {
        this.context.authenticatedUser().validateHasReadPermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        return this.breachReadPlatformService.retrieveAll();
    }

    @GET
    @Path("{breachId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalNearBreach", summary = "Retrieve Working Capital Near Breach")
    public WorkingCapitalNearBreachData retrieveOne(@PathParam("breachId") @Parameter(description = "breachId") final Long breachId) {
        this.context.authenticatedUser().validateHasReadPermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        return this.breachReadPlatformService.retrieveOne(breachId);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "createWorkingCapitalNearBreach", summary = "Create Working Capital Near Breach")
    public CommandProcessingResult create(final WorkingCapitalNearBreachRequest request) {
        this.context.authenticatedUser().validateHasCreatePermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createWorkingCapitalNearBreach()
                .withJson(jsonSerializer.serialize(request)).build();
        return this.commandSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("{breachId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "updateWorkingCapitalNearBreach", summary = "Update Working Capital Near Breach")
    public CommandProcessingResult update(@PathParam("breachId") @Parameter(description = "breachId") final Long breachId,
            final WorkingCapitalNearBreachRequest request) {
        this.context.authenticatedUser().validateHasUpdatePermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateWorkingCapitalNearBreach(breachId)
                .withJson(jsonSerializer.serialize(request)).build();
        return this.commandSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{breachId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "deleteWorkingCapitalNearBreach", summary = "Delete Working Capital Near Breach")
    public CommandProcessingResult delete(@PathParam("breachId") @Parameter(description = "breachId") final Long breachId) {
        this.context.authenticatedUser().validateHasDeletePermission(WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteWorkingCapitalNearBreach(breachId).build();
        return this.commandSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
