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
package org.apache.fineract.infrastructure.hooks.api;

import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.HOOK_RESOURCE_NAME;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.RESPONSE_DATA_PARAMETERS;

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
import org.apache.fineract.infrastructure.hooks.data.HookData;
import org.apache.fineract.infrastructure.hooks.service.HookReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/hooks")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Hooks", description = "Hooks are a mechanism to trigger custom code on the occurence of events. ")
@RequiredArgsConstructor
public class HookApiResource {

    private final PlatformSecurityContext context;
    private final HookReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<HookData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Operation(summary = "Retrieve Hooks", description = "Returns the list of hooks.\n" + "\n" + "Example Requests:\n" + "\n" + "hooks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HookApiResourceSwagger.GetHookResponse.class)))) })
    public String retrieveHooks(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(HOOK_RESOURCE_NAME);

        final Collection<HookData> hooks = this.readPlatformService.retrieveAllHooks();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, hooks, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{hookId}")
    @Operation(summary = "Retrieve a Hook", description = "Returns the details of a Hook.\n" + "\n" + "Example Requests:\n" + "\n"
            + "hooks/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = HookApiResourceSwagger.GetHookResponse.class))) })
    public String retrieveHook(@PathParam("hookId") @Parameter(description = "hookId") final Long hookId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(HOOK_RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        HookData hook = this.readPlatformService.retrieveHook(hookId);

        if (settings.isTemplate()) {
            final HookData hookData = this.readPlatformService.retrieveNewHookDetails(hook.getTemplateName());
            hook = HookData.templateExisting(hook, hookData.getTemplates(), hookData.getGroupings());
        }
        return this.toApiJsonSerializer.serialize(settings, hook, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Operation(summary = "Retrieve Hooks Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "hooks/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = HookApiResourceSwagger.GetHookTemplateResponse.class))) })
    public String template(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(HOOK_RESOURCE_NAME);

        final HookData hook = this.readPlatformService.retrieveNewHookDetails(null);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, hook, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Operation(summary = "Create a Hook", description = "The following parameters can be passed for the creation of a hook :-\n" + "\n"
            + "name - string - Required. The name of the template that is being called. (See /hooks/template for the list of valid hook names.)\n"
            + "\n" + "isActive - boolean - Determines whether the hook is actually triggered.\n" + "\n"
            + "events - array - Determines what events the hook is triggered for.\n" + "\n"
            + "config - hash - Required. Key/value pairs to provide settings for this hook. These settings vary between the templates.\n"
            + "\n" + "templateId - Optional. The UGD template ID associated with the same entity (client or loan).")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = HookApiResourceSwagger.PostHookRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = HookApiResourceSwagger.PostHookResponse.class))) })
    public String createHook(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createHook().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{hookId}")
    @Operation(summary = "Update a Hook", description = "Updates the details of a hook.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = HookApiResourceSwagger.PutHookRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = HookApiResourceSwagger.PutHookResponse.class))) })
    public String updateHook(@PathParam("hookId") @Parameter(description = "hookId") final Long hookId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateHook(hookId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{hookId}")
    @Operation(summary = "Delete a Hook", description = "Deletes a hook.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = HookApiResourceSwagger.DeleteHookResponse.class))) })
    public String deleteHook(@PathParam("hookId") @Parameter(description = "hookId") final Long hookId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteHook(hookId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
