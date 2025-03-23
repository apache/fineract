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
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Path("/v1/delinquency")
@Component
@Tag(name = "Delinquency Range and Buckets Management", description = "Delinquency Range and Buckets management enables you to set up, fetch and adjust Delinquency overdue ranges")
public class DelinquencyApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<DelinquencyBucketData> jsonSerializerBucket;
    private final DefaultToApiJsonSerializer<DelinquencyRangeData> jsonSerializerRange;
    private final DelinquencyReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;

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
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyRangeRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyRangeResponse.class))) })
    public CommandProcessingResult createDelinquencyRange(final DelinquencyRangeRequest delinquencyRangeRequest) {
        securityContext.authenticatedUser().validateHasCreatePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createDelinquencyRange()
                .withJson(jsonSerializerRange.serialize(delinquencyRangeRequest)).build();

        return commandWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("ranges/{delinquencyRangeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Delinquency Range based on the Id", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyRangeRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PutDelinquencyRangeResponse.class))) })
    public CommandProcessingResult updateDelinquencyRange(
            @PathParam("delinquencyRangeId") @Parameter(description = "delinquencyRangeId") final Long delinquencyRangeId,
            final DelinquencyRangeRequest delinquencyRangeRequest) {
        securityContext.authenticatedUser().validateHasUpdatePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDelinquencyRange(delinquencyRangeId)
                .withJson(jsonSerializerRange.serialize(delinquencyRangeRequest)).build();

        return commandWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("ranges/{delinquencyRangeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Delinquency Range based on the Id", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.DeleteDelinquencyRangeResponse.class))) })
    public CommandProcessingResult deleteDelinquencyRange(
            @PathParam("delinquencyRangeId") @Parameter(description = "delinquencyRangeId") final Long delinquencyRangeId) {
        securityContext.authenticatedUser().validateHasDeletePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDelinquencyRange(delinquencyRangeId).build();

        return commandWritePlatformService.logCommandSource(commandRequest);
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
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyBucketRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyBucketResponse.class))) })
    public CommandProcessingResult createDelinquencyBucket(final DelinquencyBucketRequest delinquencyBucketRequest) {
        securityContext.authenticatedUser().validateHasCreatePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createDelinquencyBucket()
                .withJson(jsonSerializerBucket.serialize(delinquencyBucketRequest)).build();

        return commandWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("buckets/{delinquencyBucketId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Delinquency Bucket based on the Id", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyBucketRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PutDelinquencyBucketResponse.class))) })
    public CommandProcessingResult updateDelinquencyBucket(
            @PathParam("delinquencyBucketId") @Parameter(description = "delinquencyBucketId") final Long delinquencyBucketId,
            final DelinquencyBucketRequest delinquencyBucketRequest) {
        securityContext.authenticatedUser().validateHasUpdatePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDelinquencyBucket(delinquencyBucketId)
                .withJson(jsonSerializerBucket.serialize(delinquencyBucketRequest)).build();

        return commandWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("buckets/{delinquencyBucketId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Delinquency Bucket based on the Id", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.DeleteDelinquencyBucketResponse.class))) })
    public CommandProcessingResult deleteDelinquencyBucket(
            @PathParam("delinquencyBucketId") @Parameter(description = "delinquencyBucketId") final Long delinquencyBucketId) {
        securityContext.authenticatedUser().validateHasDeletePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDelinquencyBucket(delinquencyBucketId).build();

        return commandWritePlatformService.logCommandSource(commandRequest);
    }

}
