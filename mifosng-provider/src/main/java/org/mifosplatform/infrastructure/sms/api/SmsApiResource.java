/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.sms.api;

import java.util.Collection;

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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.sms.data.SmsData;
import org.mifosplatform.infrastructure.sms.service.SmsReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/sms")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Scope("singleton")
public class SmsApiResource {

    private final String resourceNameForPermissions = "SMS";

    private final PlatformSecurityContext context;
    private final SmsReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<SmsData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public SmsApiResource(final PlatformSecurityContext context, final SmsReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<SmsData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<SmsData> smsMessages = this.readPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, smsMessages);
    }

    @POST
    public String create(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSms().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{resourceId}")
    public String retrieveOne(@PathParam("resourceId") final Long resourceId, @Context final UriInfo uriInfo) {

        final SmsData smsMessage = this.readPlatformService.retrieveOne(resourceId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, smsMessage);
    }

    @PUT
    @Path("{resourceId}")
    public String update(@PathParam("resourceId") final Long resourceId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSms(resourceId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{resourceId}")
    public String delete(@PathParam("resourceId") final Long resourceId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSms(resourceId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}