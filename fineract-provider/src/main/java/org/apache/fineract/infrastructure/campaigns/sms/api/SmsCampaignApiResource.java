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
package org.apache.fineract.infrastructure.campaigns.sms.api;

import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.campaigns.constants.CampaignType;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignConstants;
import org.apache.fineract.infrastructure.campaigns.sms.data.CampaignPreviewData;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsCampaignData;
import org.apache.fineract.infrastructure.campaigns.sms.service.SmsCampaignReadPlatformService;
import org.apache.fineract.infrastructure.campaigns.sms.service.SmsCampaignWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.springframework.stereotype.Component;

@Path("/v1/smscampaigns")
@Component
@RequiredArgsConstructor
public class SmsCampaignApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<SmsCampaignData> toApiJsonSerializer;
    private final SmsCampaignReadPlatformService smsCampaignReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final FromJsonHelper fromJsonHelper;
    private final DefaultToApiJsonSerializer<CampaignPreviewData> previewCampaignMessageDefaultToApiJsonSerializer;
    private final SmsCampaignWritePlatformService smsCampaignWritePlatformService;
    private final PlatformSecurityContext context;
    private final SqlValidator sqlValidator;

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "SMS_CAMPAIGN";

    @GET
    @Path("template")
    @Operation(summary = "Retrieve a SMS Campaign", description = """
            Example Requests:

            smscampaigns/1


            smscampaigns/1?template=true


            smscampaigns/template""")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SmsCampaignData.class)))
    public String template(@Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser().validateHasReadPermission(SmsCampaignConstants.RESOURCE_NAME);
        final SmsCampaignData smsCampaignData = smsCampaignReadPlatformService.retrieveTemplate(CampaignType.SMS.name());
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, smsCampaignData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a SMS Campaign", description = """
            Mandatory Fields
            campaignName, campaignType, triggerType, providerId, runReportId, message

            Mandatory Fields for Cash based on selected report id
            paramValue in json format""")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CommandWrapper.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class)))
    public String createCampaign(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSmsCampaign().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{resourceId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a SMS Campaign", description = """
            Example Requests:

            smscampaigns/1
            """)
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SmsCampaignData.class)))
    public String retrieveCampaign(@PathParam("resourceId") final Long resourceId, @Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser().validateHasReadPermission(SmsCampaignConstants.RESOURCE_NAME);
        SmsCampaignData smsCampaignData = smsCampaignReadPlatformService.retrieveOne(resourceId);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, smsCampaignData);

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List SMS Campaigns", description = """
            Example Requests:

            smscampaigns""")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SmsCampaignData.class)))
    public String retrieveAllEmails(@QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("orderBy") final String orderBy, @QueryParam("sortOrder") final String sortOrder, @Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser().validateHasReadPermission(SmsCampaignConstants.RESOURCE_NAME);
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();
        Page<SmsCampaignData> smsCampaignDataCollection = smsCampaignReadPlatformService.retrieveAll(searchParameters);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, smsCampaignDataCollection);
    }

    @PUT
    @Path("{campaignId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Campaign")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CommandWrapper.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class)))
    public String updateCampaign(@PathParam("campaignId") final Long campaignId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSmsCampaign(campaignId).withJson(apiRequestBodyAsJson)
                .build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{campaignId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "SMS Campaign", description = "Activates | Deactivates | Reactivates")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class)))
    public String handleCommands(@PathParam("campaignId") final Long campaignId, @QueryParam("command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandProcessingResult result = null;
        CommandWrapper commandRequest;
        if (is(commandParam, "activate")) {
            commandRequest = builder.activateSmsCampaign(campaignId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close")) {
            commandRequest = builder.closeSmsCampaign(campaignId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "reactivate")) {
            commandRequest = builder.reactivateSmsCampaign(campaignId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("preview")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String preview(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        CampaignPreviewData campaignMessage;
        final JsonElement parsedQuery = fromJsonHelper.parse(apiRequestBodyAsJson);
        final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, fromJsonHelper);
        campaignMessage = smsCampaignWritePlatformService.previewMessage(query);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return previewCampaignMessageDefaultToApiJsonSerializer.serialize(settings, campaignMessage, new HashSet<>());

    }

    @DELETE
    @Path("{campaignId}")
    @Operation(summary = "Delete a SMS Campaign", description = "Note: Only closed SMS Campaigns can be deleted")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class)))
    public String delete(@PathParam("campaignId") final Long campaignId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSmsCampaign(campaignId).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
}
