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
package org.apache.fineract.portfolio.repaymentwithpostdatedchecks.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.data.PostDatedChecksData;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/postdatedchecks")
@Component
@Scope("singleton")
@Tag(name = "repayment with post dated checks", description = "Repay with post dated checks")
public class RepaymentWithPostDatedChecksApiResource {

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final DefaultToApiJsonSerializer<PostDatedChecksData> apiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final RepaymentWithPostDatedChecksReadPlatformService repaymentWithPostDatedChecksReadPlatformService;

    @Autowired
    public RepaymentWithPostDatedChecksApiResource(final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,
            final DefaultToApiJsonSerializer<PostDatedChecksData> apiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService,
            final RepaymentWithPostDatedChecksReadPlatformService repaymentWithPostDatedChecksReadPlatformService) {
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.apiJsonSerializer = apiJsonSerializer;
        this.commandsSourceWritePlatformService = portfolioCommandSourceWritePlatformService;
        this.repaymentWithPostDatedChecksReadPlatformService = repaymentWithPostDatedChecksReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get All Post Dated Checks", description = "Get All Post dated Checks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostDatedChecksApiResourceSwagger.GetPostDatedChecks.class)))) })
    public String getPostDatedChecks(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        this.context.authenticatedUser();
        final List<PostDatedChecksData> postDatedChecksDataList = this.repaymentWithPostDatedChecksReadPlatformService
                .getPostDatedChecks(loanId);
        return this.apiJsonSerializer.serialize(postDatedChecksDataList);
    }

    @GET
    @Path("{installmentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Post Dated Check", description = "Get Post Dated Check")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostDatedChecksApiResourceSwagger.GetPostDatedChecks.class)))) })
    public String getPostDatedCheck(@PathParam("installmentId") @Parameter(description = "installmentId") final Integer installmentId,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        this.context.authenticatedUser();
        final PostDatedChecksData postDatedChecksData = this.repaymentWithPostDatedChecksReadPlatformService
                .getPostDatedCheckByInstallmentId(installmentId, loanId);
        return this.apiJsonSerializer.serialize(postDatedChecksData);
    }

    @PUT
    @Path("{postDatedCheckId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Post Dated Check, Bounced Check", description = "Update Post Dated Check, Bounced Check")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = PostDatedChecksApiResourceSwagger.UpdatePostDatedCheckRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostDatedChecksApiResourceSwagger.UpdatePostDatedCheckResponse.class)))) })
    public String updatePostDatedChecks(@PathParam("postDatedCheckId") @Parameter(description = "postDatedCheckId") final Long id,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson,
            @QueryParam("editType") @Parameter(description = "editType") final String type) {

        CommandWrapper commandRequest = null;

        if ("update".equals(type)) {
            commandRequest = new CommandWrapperBuilder().updatePostDatedCheck(id, loanId).withJson(apiRequestBodyAsJson).build();
        } else if ("bounced".equals(type)) {
            commandRequest = new CommandWrapperBuilder().bouncedCheck(id, loanId).withJson(apiRequestBodyAsJson).build();
        }

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializer.serialize(commandProcessingResult);
    }

    @DELETE
    @Path("{postDatedCheckId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Post Dated Check", description = "Delete Post Dated Check")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostDatedChecksApiResourceSwagger.DeletePostDatedCheck.class)))) })
    public String deletePostDatedCheck(@PathParam("postDatedCheckId") @Parameter(description = "postDatedCheckId") final Long id,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deletePostDatedCheck(id, loanId).build();

        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializer.serialize(commandProcessingResult);
    }

}
