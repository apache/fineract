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
package org.apache.fineract.portfolio.delinquency.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.delinquency.command.DelinquencyBucketDeleteCommand;
import org.apache.fineract.portfolio.delinquency.command.DelinquencyBucketUpdateCommand;
import org.apache.fineract.portfolio.delinquency.command.DelinquencyRangeDeleteCommand;
import org.apache.fineract.portfolio.delinquency.command.DelinquencyRangeUpdateCommand;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketCreateRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketCreateResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketDeleteRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketDeleteResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketUpdateRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketUpdateResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeCreateRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeCreateResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeDeleteRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeDeleteResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeUpdateRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeUpdateResponse;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Path("/v1/delinquency")
@Component
@Tag(name = "Delinquency Range and Buckets Management", description = "Delinquency Range and Buckets management enables you to set up, fetch and adjust Delinquency overdue ranges")
public class DelinquencyApiResource {

    private final PlatformSecurityContext securityContext;
    private final DelinquencyReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final CommandPipeline pipeline;

    @GET
    @Path("ranges")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all Delinquency Ranges", description = "")
    public List<DelinquencyRangeData> getDelinquencyRanges() {
        securityContext.authenticatedUser().validateHasReadPermission("DELINQUENCY_BUCKET");
        return this.readPlatformService.retrieveAllDelinquencyRanges();
    }

    @GET
    @Path("ranges/{delinquencyRangeId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a specific Delinquency Range based on the Id", description = "")
    public DelinquencyRangeData getDelinquencyRange(
            @PathParam("delinquencyRangeId") @Parameter(description = "delinquencyRangeId") final Long delinquencyRangeId) {
        securityContext.authenticatedUser().validateHasReadPermission("DELINQUENCY_BUCKET");
        return this.readPlatformService.retrieveDelinquencyRange(delinquencyRangeId);
    }

    @POST
    @Path("ranges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Delinquency Range", description = "")
    public DelinquencyRangeCreateResponse createDelinquencyRange(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            final DelinquencyRangeCreateRequest request) {
        securityContext.authenticatedUser().validateHasCreatePermission("DELINQUENCY_BUCKET");

        var command = new Command<DelinquencyRangeCreateRequest>();
        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);
        command.setIdempotencyKey(idempotencyKey);
        command.setPayload(request);

        Supplier<DelinquencyRangeCreateResponse> result = pipeline.send(command);
        return result.get();
    }

    @PUT
    @Path("ranges/{delinquencyRangeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Delinquency Range based on the Id", description = "")
    public DelinquencyRangeUpdateResponse updateDelinquencyRange(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @PathParam("delinquencyRangeId") @Parameter(description = "delinquencyRangeId") final Long id,
            final DelinquencyRangeUpdateRequest request) {
        securityContext.authenticatedUser().validateHasUpdatePermission("DELINQUENCY_BUCKET");

        var command = new DelinquencyRangeUpdateCommand();
        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);
        command.setIdempotencyKey(idempotencyKey);
        request.setId(id);
        command.setPayload(request);

        Supplier<DelinquencyRangeUpdateResponse> result = pipeline.send(command);
        return result.get();
    }

    @DELETE
    @Path("ranges/{delinquencyRangeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Delinquency Range based on the Id", description = "")
    public DelinquencyRangeDeleteResponse deleteDelinquencyRange(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @PathParam("delinquencyRangeId") @Parameter(description = "delinquencyRangeId") final Long id) {
        securityContext.authenticatedUser().validateHasDeletePermission("DELINQUENCY_BUCKET");

        var command = new DelinquencyRangeDeleteCommand();
        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);
        command.setPayload(new DelinquencyRangeDeleteRequest(id));
        command.setIdempotencyKey(idempotencyKey);

        Supplier<DelinquencyRangeDeleteResponse> result = pipeline.send(command);
        return result.get();
    }

    @GET
    @Path("buckets")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all Delinquency Buckets", description = "")
    public List<DelinquencyBucketData> getDelinquencyBuckets() {
        securityContext.authenticatedUser().validateHasReadPermission("DELINQUENCY_BUCKET");
        return this.readPlatformService.retrieveAllDelinquencyBuckets();
    }

    @GET
    @Path("buckets/{delinquencyBucketId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a specific Delinquency Bucket based on the Id", description = "")
    public DelinquencyBucketData getDelinquencyBucket(
            @PathParam("delinquencyBucketId") @Parameter(description = "delinquencyBucketId") final Long delinquencyBucketId) {
        securityContext.authenticatedUser().validateHasReadPermission("DELINQUENCY_BUCKET");
        return this.readPlatformService.retrieveDelinquencyBucket(delinquencyBucketId);
    }

    @POST
    @Path("buckets")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Delinquency Bucket", description = "")
    public DelinquencyBucketCreateResponse createDelinquencyBucket(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            final DelinquencyBucketCreateRequest request) {
        securityContext.authenticatedUser().validateHasCreatePermission("DELINQUENCY_BUCKET");

        var command = new Command<DelinquencyBucketCreateRequest>();
        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);
        command.setPayload(request);

        Supplier<DelinquencyBucketCreateResponse> result = pipeline.send(command);
        return result.get();
    }

    @PUT
    @Path("buckets/{delinquencyBucketId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Delinquency Bucket based on the Id", description = "")
    public DelinquencyBucketUpdateResponse updateDelinquencyBucket(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @PathParam("delinquencyBucketId") @Parameter(description = "delinquencyBucketId") final Long id,
            final DelinquencyBucketUpdateRequest request) {
        securityContext.authenticatedUser().validateHasUpdatePermission("DELINQUENCY_BUCKET");

        var command = new DelinquencyBucketUpdateCommand();
        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);
        command.setIdempotencyKey(idempotencyKey);
        request.setId(id);
        command.setPayload(request);

        Supplier<DelinquencyBucketUpdateResponse> result = pipeline.send(command);
        return result.get();
    }

    @DELETE
    @Path("buckets/{delinquencyBucketId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Delinquency Bucket based on the Id", description = "")
    public DelinquencyBucketDeleteResponse deleteDelinquencyBucket(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @PathParam("delinquencyBucketId") @Parameter(description = "delinquencyBucketId") final Long id) {
        securityContext.authenticatedUser().validateHasDeletePermission("DELINQUENCY_BUCKET");

        var command = new DelinquencyBucketDeleteCommand();
        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);
        command.setPayload(new DelinquencyBucketDeleteRequest(id));
        command.setIdempotencyKey(idempotencyKey);

        Supplier<DelinquencyBucketDeleteResponse> result = pipeline.send(command);
        return result.get();
    }

}
