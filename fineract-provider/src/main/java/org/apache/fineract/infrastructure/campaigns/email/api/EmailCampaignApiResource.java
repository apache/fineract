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
package org.apache.fineract.infrastructure.campaigns.email.api;

import com.google.gson.JsonElement;
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
import java.util.Collection;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailBusinessRulesData;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignData;
import org.apache.fineract.infrastructure.campaigns.email.data.PreviewCampaignMessage;
import org.apache.fineract.infrastructure.campaigns.email.service.EmailCampaignReadPlatformService;
import org.apache.fineract.infrastructure.campaigns.email.service.EmailCampaignWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 19-5-14 Time: 15:17 To change this template use File | Settings | File
 * Templates.
 */
@Path("/v1/email/campaign")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@RequiredArgsConstructor
public class EmailCampaignApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "EMAIL_CAMPAIGN";

    private final PlatformSecurityContext context;

    private final DefaultToApiJsonSerializer<EmailBusinessRulesData> toApiJsonSerializer;

    private final ApiRequestParameterHelper apiRequestParameterHelper;

    private final EmailCampaignReadPlatformService emailCampaignReadPlatformService;
    private final FromJsonHelper fromJsonHelper;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<EmailCampaignData> emailCampaignDataDefaultToApiJsonSerializer;
    private final EmailCampaignWritePlatformService emailCampaignWritePlatformService;

    private final DefaultToApiJsonSerializer<PreviewCampaignMessage> previewCampaignMessageDefaultToApiJsonSerializer;

    @GET
    @Path("{resourceId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOneCampaign(@PathParam("resourceId") final Long resourceId, @Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        EmailCampaignData emailCampaignData = emailCampaignReadPlatformService.retrieveOne(resourceId);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return emailCampaignDataDefaultToApiJsonSerializer.serialize(settings, emailCampaignData);

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllCampaign(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<EmailCampaignData> emailCampaignDataCollection = emailCampaignReadPlatformService.retrieveAllCampaign();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return emailCampaignDataDefaultToApiJsonSerializer.serialize(settings, emailCampaignDataCollection);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCampaign(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createEmailCampaign().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{resourceId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCampaign(@PathParam("resourceId") final Long campaignId, final String apiRequestBodyAsJson,
            @Context final UriInfo uriInfo) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateEmailCampaign(campaignId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{resourceId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String activate(@PathParam("resourceId") final Long campaignId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        CommandWrapper commandRequest;
        if (is(commandParam, "activate")) {
            commandRequest = builder.activateEmailCampaign(campaignId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close")) {
            commandRequest = builder.closeEmailCampaign(campaignId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "reactivate")) {
            commandRequest = builder.reactivateEmailCampaign(campaignId).build();
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

        PreviewCampaignMessage campaignMessage;
        final JsonElement parsedQuery = fromJsonHelper.parse(apiRequestBodyAsJson);
        final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, fromJsonHelper);
        campaignMessage = emailCampaignWritePlatformService.previewMessage(query);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return previewCampaignMessageDefaultToApiJsonSerializer.serialize(settings, campaignMessage, new HashSet<>());

    }

    @GET()
    @Path("template")
    public String template(@Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<EmailBusinessRulesData> emailBusinessRulesDataCollection = emailCampaignReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, emailBusinessRulesDataCollection);
    }

    @GET
    @Path("template/{resourceId}")
    public String retrieveOneTemplate(@PathParam("resourceId") final Long resourceId, @Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final EmailBusinessRulesData emailBusinessRulesData = emailCampaignReadPlatformService.retrieveOneTemplate(resourceId);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, emailBusinessRulesData);

    }

    @DELETE
    @Path("{resourceId}")
    public String delete(@PathParam("resourceId") final Long resourceId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteEmailCampaign(resourceId).build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
