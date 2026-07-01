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

import static java.util.Objects.requireNonNull;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
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
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.hooks.command.HookCreateCommand;
import org.apache.fineract.infrastructure.hooks.command.HookDeleteCommand;
import org.apache.fineract.infrastructure.hooks.command.HookUpdateCommand;
import org.apache.fineract.infrastructure.hooks.data.HookCreateRequest;
import org.apache.fineract.infrastructure.hooks.data.HookCreateResponse;
import org.apache.fineract.infrastructure.hooks.data.HookData;
import org.apache.fineract.infrastructure.hooks.data.HookDeleteRequest;
import org.apache.fineract.infrastructure.hooks.data.HookDeleteResponse;
import org.apache.fineract.infrastructure.hooks.data.HookDetailsData;
import org.apache.fineract.infrastructure.hooks.data.HookUpdateRequest;
import org.apache.fineract.infrastructure.hooks.data.HookUpdateResponse;
import org.apache.fineract.infrastructure.hooks.service.HookReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/hooks")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Hooks", description = "Hooks are a mechanism to trigger custom code on the occurence of events. ")
@RequiredArgsConstructor
public class HookApiResource {

    private final HookReadPlatformService readPlatformService;
    private final CommandDispatcher dispatcher;

    @GET
    @Operation(summary = "Retrieve Hooks", operationId = "retrieveAllHooks", description = "Returns the list of hooks")
    public Collection<HookData> retrieveHooks(@Context final UriInfo uriInfo) {
        return readPlatformService.retrieveAllHooks();
    }

    @GET
    @Path("{hookId}")
    @Operation(summary = "Retrieve a Hook", operationId = "retrieveOneHook", description = "Returns the details of a Hook.")
    public HookData retrieveHook(@PathParam("hookId") @Parameter(description = "hookId") final Long hookId,
            @QueryParam("template") @DefaultValue("false") @Parameter(description = "template") Boolean template) {
        var hook = readPlatformService.retrieveHook(hookId);

        if (Boolean.TRUE.equals(template)) {
            var hookTemplate = readPlatformService.retrieveNewHookDetails(hook.getTemplateName());

            hook.setTemplates(hookTemplate.getTemplates());
            hook.setGroupings(hookTemplate.getGroupings());
        }

        return hook;
    }

    @GET
    @Path("template")
    @Operation(summary = "Retrieve Hooks Template", operationId = "retrieveTemplateHook", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications.")
    public HookDetailsData template() {
        return readPlatformService.retrieveNewHookDetails(null);
    }

    @POST
    @Operation(summary = "Create a Hook", operationId = "createHook", description = "")
    public HookCreateResponse createHook(@Valid final HookCreateRequest request) {
        final var command = new HookCreateCommand();
        command.setPayload(request);

        final Supplier<HookCreateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @PUT
    @Path("{hookId}")
    @Operation(summary = "Update a Hook", operationId = "updateHook", description = "Updates the details of a hook.")
    public HookUpdateResponse updateHook(@PathParam("hookId") @Parameter(description = "hookId") final Long hookId,
            @Valid HookUpdateRequest request) {
        requireNonNull(hookId, "hookId is required");

        request.setId(hookId);

        final var command = new HookUpdateCommand();
        command.setPayload(request);

        final Supplier<HookUpdateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @DELETE
    @Path("{hookId}")
    @Operation(summary = "Delete a Hook", operationId = "deleteHook", description = "Deletes a hook.")
    public HookDeleteResponse deleteHook(@PathParam("hookId") @Parameter(description = "hookId") final Long hookId) {
        var request = HookDeleteRequest.builder().id(hookId).build();

        final var command = new HookDeleteCommand();
        command.setPayload(request);

        final Supplier<HookDeleteResponse> response = dispatcher.dispatch(command);

        return response.get();
    }
}
