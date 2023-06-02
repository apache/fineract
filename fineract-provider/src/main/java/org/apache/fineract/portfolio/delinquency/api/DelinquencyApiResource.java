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
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
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

    private final ApiRequestParameterHelper parameterHelper;
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DelinquencyApiResourceSwagger.GetDelinquencyRangesResponse.class)))) })
    public String getDelinquencyRanges(@Context final UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasReadPermission("DELINQUENCY_BUCKET");
        final Collection<DelinquencyRangeData> delinquencyRangeData = this.readPlatformService.retrieveAllDelinquencyRanges();
        ApiRequestJsonSerializationSettings settings = parameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializerRange.serialize(settings, delinquencyRangeData);
    }

    @GET
    @Path("ranges/{delinquencyRangeId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a specific Delinquency Range based on the Id", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.GetDelinquencyRangesResponse.class))) })
    public String getDelinquencyRange(
            @PathParam("delinquencyRangeId") @Parameter(description = "delinquencyRangeId") final Long delinquencyRangeId,
            @Context final UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasReadPermission("DELINQUENCY_BUCKET");
        final DelinquencyRangeData delinquencyRangeData = this.readPlatformService.retrieveDelinquencyRange(delinquencyRangeId);
        ApiRequestJsonSerializationSettings settings = parameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializerRange.serialize(settings, delinquencyRangeData);
    }

    @POST
    @Path("ranges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Delinquency Range", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyRangeRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyRangeResponse.class))) })
    public String createDelinquencyRange(final String jsonRequestBody, @Context UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasCreatePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createDelinquencyRange().withJson(jsonRequestBody).build();

        CommandProcessingResult result = commandWritePlatformService.logCommandSource(commandRequest);
        return jsonSerializerRange.serialize(result);
    }

    @PUT
    @Path("ranges/{delinquencyRangeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Delinquency Range based on the Id", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyRangeRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PutDelinquencyRangeResponse.class))) })
    public String updateDelinquencyRange(
            @PathParam("delinquencyRangeId") @Parameter(description = "delinquencyRangeId") final Long delinquencyRangeId,
            final String jsonRequestBody, @Context UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasUpdatePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDelinquencyRange(delinquencyRangeId)
                .withJson(jsonRequestBody).build();

        CommandProcessingResult result = commandWritePlatformService.logCommandSource(commandRequest);
        return jsonSerializerRange.serialize(result);
    }

    @DELETE
    @Path("ranges/{delinquencyRangeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Delinquency Range based on the Id", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyRangeRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.DeleteDelinquencyRangeResponse.class))) })
    public String deleteDelinquencyRange(
            @PathParam("delinquencyRangeId") @Parameter(description = "delinquencyRangeId") final Long delinquencyRangeId,
            @Context UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasDeletePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDelinquencyRange(delinquencyRangeId).build();

        CommandProcessingResult result = commandWritePlatformService.logCommandSource(commandRequest);
        return jsonSerializerRange.serialize(result);
    }

    @GET
    @Path("buckets")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all Delinquency Buckets", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DelinquencyApiResourceSwagger.GetDelinquencyBucketsResponse.class)))) })
    public String getDelinquencyBuckets(@Context final UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasReadPermission("DELINQUENCY_BUCKET");
        final Collection<DelinquencyBucketData> delinquencyBucketData = this.readPlatformService.retrieveAllDelinquencyBuckets();
        ApiRequestJsonSerializationSettings settings = parameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializerBucket.serialize(settings, delinquencyBucketData);
    }

    @GET
    @Path("buckets/{delinquencyBucketId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a specific Delinquency Bucket based on the Id", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.GetDelinquencyBucketsResponse.class))) })
    public String getDelinquencyBucket(
            @PathParam("delinquencyBucketId") @Parameter(description = "delinquencyBucketId") final Long delinquencyBucketId,
            @Context final UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasReadPermission("DELINQUENCY_BUCKET");
        final DelinquencyBucketData delinquencyBucketData = this.readPlatformService.retrieveDelinquencyBucket(delinquencyBucketId);
        ApiRequestJsonSerializationSettings settings = parameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializerBucket.serialize(settings, delinquencyBucketData);
    }

    @POST
    @Path("buckets")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Delinquency Bucket", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyBucketRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyBucketResponse.class))) })
    public String createDelinquencyBucket(final String jsonRequestBody, @Context UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasCreatePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createDelinquencyBucket().withJson(jsonRequestBody).build();

        CommandProcessingResult result = commandWritePlatformService.logCommandSource(commandRequest);
        return jsonSerializerRange.serialize(result);
    }

    @PUT
    @Path("buckets/{delinquencyBucketId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Delinquency Bucket based on the Id", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyBucketRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PutDelinquencyBucketResponse.class))) })
    public String updateDelinquencyBucket(
            @PathParam("delinquencyBucketId") @Parameter(description = "delinquencyBucketId") final Long delinquencyBucketId,
            final String jsonRequestBody, @Context UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasUpdatePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDelinquencyBucket(delinquencyBucketId)
                .withJson(jsonRequestBody).build();

        CommandProcessingResult result = commandWritePlatformService.logCommandSource(commandRequest);
        return jsonSerializerRange.serialize(result);
    }

    @DELETE
    @Path("buckets/{delinquencyBucketId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Delinquency Bucket based on the Id", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.PostDelinquencyBucketRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DelinquencyApiResourceSwagger.DeleteDelinquencyBucketResponse.class))) })
    public String deleteDelinquencyBucket(
            @PathParam("delinquencyBucketId") @Parameter(description = "delinquencyBucketId") final Long delinquencyBucketId,
            @Context UriInfo uriInfo) {
        securityContext.authenticatedUser().validateHasDeletePermission("DELINQUENCY_BUCKET");
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDelinquencyBucket(delinquencyBucketId).build();

        CommandProcessingResult result = commandWritePlatformService.logCommandSource(commandRequest);
        return jsonSerializerRange.serialize(result);
    }

}
