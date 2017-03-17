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
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.campaigns.email.data.PreviewCampaignMessage;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailBusinessRulesData;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignData;
import org.apache.fineract.infrastructure.campaigns.email.service.EmailCampaignReadPlatformService;
import org.apache.fineract.infrastructure.campaigns.email.service.EmailCampaignWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 19-5-14
 * Time: 15:17
 * To change this template use File | Settings | File Templates.
 */
@Path("/email/campaign")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Scope("singleton")
public class EmailCampaignApiResource {


    //change name to email campaign
    private final String resourceNameForPermissions = "EMAIL_CAMPAIGN";

    private final PlatformSecurityContext context;

    private final DefaultToApiJsonSerializer<EmailBusinessRulesData> toApiJsonSerializer;

    private final ApiRequestParameterHelper apiRequestParameterHelper;

    private final EmailCampaignReadPlatformService emailCampaignReadPlatformService;
    private final FromJsonHelper fromJsonHelper;


    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<EmailCampaignData> emailCampaignDataDefaultToApiJsonSerializer;
    private final EmailCampaignWritePlatformService emailCampaignWritePlatformService;

    private final DefaultToApiJsonSerializer<PreviewCampaignMessage> previewCampaignMessageDefaultToApiJsonSerializer;


    @Autowired
    public EmailCampaignApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<EmailBusinessRulesData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
                                  final EmailCampaignReadPlatformService emailCampaignReadPlatformService, final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                                  final DefaultToApiJsonSerializer<EmailCampaignData> emailCampaignDataDefaultToApiJsonSerializer,
                                  final FromJsonHelper fromJsonHelper, final EmailCampaignWritePlatformService emailCampaignWritePlatformService,
                                  final DefaultToApiJsonSerializer<PreviewCampaignMessage> previewCampaignMessageDefaultToApiJsonSerializer) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.emailCampaignReadPlatformService = emailCampaignReadPlatformService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.emailCampaignDataDefaultToApiJsonSerializer = emailCampaignDataDefaultToApiJsonSerializer;
        this.fromJsonHelper = fromJsonHelper;
        this.emailCampaignWritePlatformService = emailCampaignWritePlatformService;
        this.previewCampaignMessageDefaultToApiJsonSerializer = previewCampaignMessageDefaultToApiJsonSerializer;
    }


    @GET
    @Path("{resourceId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOneCampaign(@PathParam("resourceId") final Long resourceId,@Context final UriInfo uriInfo){
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        EmailCampaignData emailCampaignData = this.emailCampaignReadPlatformService.retrieveOne(resourceId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.emailCampaignDataDefaultToApiJsonSerializer.serialize(settings,emailCampaignData);

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllCampaign(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<EmailCampaignData> emailCampaignDataCollection = this.emailCampaignReadPlatformService.retrieveAllCampaign();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.emailCampaignDataDefaultToApiJsonSerializer.serialize(settings,emailCampaignDataCollection);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCampaign(final String apiRequestBodyAsJson,@Context final UriInfo uriInfo){

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createEmailCampaign().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{resourceId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCampaign(@PathParam("resourceId") final Long campaignId,final String apiRequestBodyAsJson,@Context final UriInfo uriInfo){

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateEmailCampaign(campaignId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{resourceId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String activate(@PathParam("resourceId") final Long campaignId, @QueryParam("command") final String commandParam,
                           final String apiRequestBodyAsJson){
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        CommandWrapper commandRequest = null;
        if (is(commandParam, "activate")) {
            commandRequest = builder.activateEmailCampaign(campaignId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }else if (is(commandParam, "close")){
            commandRequest = builder.closeEmailCampaign(campaignId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }else if (is(commandParam, "reactivate")){
            commandRequest = builder.reactivateEmailCampaign(campaignId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("preview")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String preview(final String apiRequestBodyAsJson,@Context final UriInfo uriInfo){
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        PreviewCampaignMessage campaignMessage = null;
        final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
        final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
        campaignMessage = this.emailCampaignWritePlatformService.previewMessage(query);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.previewCampaignMessageDefaultToApiJsonSerializer.serialize(settings,campaignMessage, new HashSet<String>());

    }


    @GET()
    @Path("template")
    public String template(@Context final UriInfo uriInfo){
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<EmailBusinessRulesData>  emailBusinessRulesDataCollection = this.emailCampaignReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings,emailBusinessRulesDataCollection);
    }

    @GET
    @Path("template/{resourceId}")
    public String retrieveOneTemplate(@PathParam("resourceId") final Long resourceId,@Context final UriInfo uriInfo){
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final EmailBusinessRulesData emailBusinessRulesData = this.emailCampaignReadPlatformService.retrieveOneTemplate(resourceId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings,emailBusinessRulesData);

    }

    @DELETE
    @Path("{resourceId}")
    public String delete(@PathParam("resourceId") final Long resourceId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteEmailCampaign(resourceId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
