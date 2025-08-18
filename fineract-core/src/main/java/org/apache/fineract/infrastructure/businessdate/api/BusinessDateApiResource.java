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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.businessdate.command.BusinessDateUpdateCommand;
import org.apache.fineract.infrastructure.businessdate.data.api.BusinessDateResponse;
import org.apache.fineract.infrastructure.businessdate.data.api.BusinessDateUpdateRequest;
import org.apache.fineract.infrastructure.businessdate.data.api.BusinessDateUpdateResponse;
import org.apache.fineract.infrastructure.businessdate.mapper.BusinessDateMapper;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Path("/v1/businessdate")
@Component
@Tag(name = "Business Date Management", description = "Business date management enables you to set up, fetch and adjust organisation business dates")
public class BusinessDateApiResource {

    private final BusinessDateReadPlatformService readPlatformService;
    private final CommandPipeline commandPipeline;
    private final BusinessDateMapper businessDateMapper;

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all business dates", description = "")
    public List<BusinessDateResponse> getBusinessDates() {
        return businessDateMapper.mapFetchResponse(this.readPlatformService.findAll());
    }

    @GET
    @Path("{type}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a specific Business date", description = "")
    public BusinessDateResponse getBusinessDate(@PathParam("type") @Parameter(description = "type") final String type) {
        return businessDateMapper.mapFetchResponse(this.readPlatformService.findByType(type));
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Business Date", description = "")
    public BusinessDateUpdateResponse updateBusinessDate(@HeaderParam("Idempotency-Key") String idempotencyKey,
            @Valid BusinessDateUpdateRequest request) {

        final BusinessDateUpdateCommand command = new BusinessDateUpdateCommand();

        command.setId(UUID.randomUUID());
        command.setIdempotencyKey(idempotencyKey);
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setPayload(request);

        final Supplier<BusinessDateUpdateResponse> response = commandPipeline.send(command);

        return response.get();
    }

}
