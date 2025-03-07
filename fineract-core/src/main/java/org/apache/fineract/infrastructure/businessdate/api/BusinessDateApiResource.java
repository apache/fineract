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
package org.apache.fineract.infrastructure.businessdate.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateData;
import org.apache.fineract.infrastructure.businessdate.data.request.BusinessDateRequest;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Path("/v1/businessdate")
@Component
@Tag(name = "Business Date Management", description = "Business date management enables you to set up, fetch and adjust organisation business dates")
public class BusinessDateApiResource {

    private static final String BUSINESS_DATE = "BUSINESS_DATE";

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<BusinessDateData> jsonSerializer;
    private final BusinessDateReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all business dates", description = "")
    public List<BusinessDateData> getBusinessDates() {
        securityContext.authenticatedUser().validateHasReadPermission(BUSINESS_DATE);
        return this.readPlatformService.findAll();
    }

    @GET
    @Path("{type}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a specific Business date", description = "")
    public BusinessDateData getBusinessDate(@PathParam("type") @Parameter(description = "type") final String type) {
        securityContext.authenticatedUser().validateHasReadPermission(BUSINESS_DATE);
        return this.readPlatformService.findByType(type);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Business Date", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = BusinessDateRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BusinessDateApiResourceSwagger.BusinessDateResponse.class))) })
    public CommandProcessingResult updateBusinessDate(BusinessDateRequest businessDateRequest) {
        securityContext.authenticatedUser().validateHasUpdatePermission(BUSINESS_DATE);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateBusinessDate()
                .withJson(jsonSerializer.serialize(businessDateRequest)).build();
        return commandWritePlatformService.logCommandSource(commandRequest);
    }

}
