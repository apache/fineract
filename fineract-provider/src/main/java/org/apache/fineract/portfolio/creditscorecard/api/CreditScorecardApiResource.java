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
package org.apache.fineract.portfolio.creditscorecard.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.creditscorecard.data.CreditScorecardFeatureData;
import org.apache.fineract.portfolio.creditscorecard.domain.CreditScorecardFeature;
import org.apache.fineract.portfolio.creditscorecard.provider.ScorecardServiceProvider;
import org.apache.fineract.portfolio.creditscorecard.service.CreditScorecardReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/creditScorecard")
@Component
@Scope("singleton")
@Tag(name = "CreditScorecard", description = "")
public class CreditScorecardApiResource {

    private final Set<String> featuresDataParameters = new HashSet<>(
            Arrays.asList("id", "name", "valueType", "dataType", "category", "valueTypeOptions", "dataTypeOptions", "categoryOptions"));

    private final String resourceNameForPermissions = "CREDIT_SCORECARD";

    private final PlatformSecurityContext context;
    private final ScorecardServiceProvider scorecardServiceProvider;
    private final ScorecardServiceProvider serviceProvider;
    private final DefaultToApiJsonSerializer<CreditScorecardFeatureData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public CreditScorecardApiResource(final PlatformSecurityContext context, final ScorecardServiceProvider scorecardServiceProvider,
            final ScorecardServiceProvider serviceProvider,
            final DefaultToApiJsonSerializer<CreditScorecardFeatureData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.scorecardServiceProvider = scorecardServiceProvider;
        this.serviceProvider = serviceProvider;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("features")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Scorecard Features", description = "Returns the list of defined scorecard features.\n" + "\n"
            + "Example Requests:\n" + "\n" + "features")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CreditScorecardApiResourceSwagger.GetScorecardFeatureResponse.class)))) })
    public String retrieveAllScoringFeatures(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final String serviceName = "CreditScorecardReadPlatformService";
        final CreditScorecardReadPlatformService scorecardService = (CreditScorecardReadPlatformService) scorecardServiceProvider
                .getScorecardService(serviceName);

        if (scorecardService == null) {
            throw new PlatformServiceUnavailableException("err.msg.credit.scorecard.service.implementation.missing",
                    ScorecardServiceProvider.SERVICE_MISSING + serviceName, serviceName);
        }

        final Collection<CreditScorecardFeatureData> scoringFeatures = scorecardService.findAllFeaturesWithNotFoundDetection().stream()
                .map(CreditScorecardFeature::toData).collect(Collectors.toList());

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, scoringFeatures, this.featuresDataParameters);
    }

    @GET
    @Path("features/{featureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Scorecard Feature", description = "Returns the details of a defined Scorecard Feature.\n" + "\n"
            + "Example Requests:\n" + "\n" + "scorecard/features/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CreditScorecardApiResourceSwagger.GetScorecardFeatureResponse.class))) })
    public String retrieveScoringFeature(@PathParam("featureId") @Parameter(description = "featureId") final Long featureId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        CreditScorecardFeatureData scorecardFeature = null;
        if (settings.isTemplate()) {

            final String serviceName = "CreditScorecardReadPlatformService";
            final CreditScorecardReadPlatformService scorecardService = (CreditScorecardReadPlatformService) scorecardServiceProvider
                    .getScorecardService(serviceName);

            if (scorecardService == null) {
                throw new PlatformServiceUnavailableException("err.msg.credit.scorecard.service.implementation.missing",
                        ScorecardServiceProvider.SERVICE_MISSING + serviceName, serviceName);
            }

            scorecardFeature = scorecardService.findOneFeatureWithNotFoundDetection(featureId).toData();
            final CreditScorecardFeatureData templateData = scorecardService.retrieveNewScorecardFeatureDetails();
            scorecardFeature = CreditScorecardFeatureData.withTemplate(scorecardFeature, templateData);
        }

        return this.toApiJsonSerializer.serialize(settings, scorecardFeature, this.featuresDataParameters);
    }

    @GET
    @Path("features/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Scorecard Feature Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "scorecard/features/template\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CreditScorecardApiResourceSwagger.GetScorecardFeaturesTemplateResponse.class))) })
    public String retrieveNewScorecardFeatureDetails(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final String serviceName = "CreditScorecardReadPlatformService";
        final CreditScorecardReadPlatformService scorecardService = (CreditScorecardReadPlatformService) scorecardServiceProvider
                .getScorecardService(serviceName);

        if (scorecardService == null) {
            throw new PlatformServiceUnavailableException("err.msg.credit.scorecard.service.implementation.missing",
                    ScorecardServiceProvider.SERVICE_MISSING + serviceName, serviceName);
        }

        final CreditScorecardFeatureData scorecardFeature = scorecardService.retrieveNewScorecardFeatureDetails();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, scorecardFeature, this.featuresDataParameters);
    }

    @POST
    @Path("features")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create/Define a Scorecard Feature", description = "Define a new scorecard feature that can later be associated with loans and savings through their respective product definitions or directly on each account instance.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CreditScorecardApiResourceSwagger.PostScorecardFeatureRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CreditScorecardApiResourceSwagger.PostScorecardFeatureResponse.class))) })
    public String createScoringFeature(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCreditScorecardFeature().withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("features/{featureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Scorecard Feature", description = "Updates the details of a Scorecard Feature.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CreditScorecardApiResourceSwagger.PutScorecardFeaturesFeatureIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CreditScorecardApiResourceSwagger.PutScorecardFeaturesFeatureIdResponse.class))) })
    public String updateScoringFeature(@PathParam("featureId") @Parameter(description = "featureId") final Long featureId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCreditScorecardFeature(featureId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("features/{featureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Scorecard Feature", description = "Deletes a Scorecard Feature.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CreditScorecardApiResourceSwagger.DeleteScorecardFeaturesFeatureIdResponse.class))) })
    public String deleteScoringFeature(@PathParam("featureId") @Parameter(description = "featureId") final Long featureId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCreditScorecardFeature(featureId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
