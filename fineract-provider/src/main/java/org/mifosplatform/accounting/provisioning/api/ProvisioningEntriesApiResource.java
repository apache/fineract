/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.accounting.provisioning.constant.ProvisioningEntriesApiConstants;
import org.mifosplatform.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.mifosplatform.accounting.provisioning.data.ProvisioningEntryData;
import org.mifosplatform.accounting.provisioning.domain.LoanProductProvisioningEntry;
import org.mifosplatform.accounting.provisioning.service.ProvisioningEntriesReadPlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/provisioningentries")
@Component
@Scope("singleton")
public class ProvisioningEntriesApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<ProvisioningEntryData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<Object> entriesApiJsonSerializer;
    private final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public ProvisioningEntriesApiResource(final PlatformSecurityContext platformSecurityContext,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<ProvisioningEntryData> toApiJsonSerializer,
            final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper, final DefaultToApiJsonSerializer<Object> entriesApiJsonSerializer) {
        this.platformSecurityContext = platformSecurityContext;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.provisioningEntriesReadPlatformService = provisioningEntriesReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.entriesApiJsonSerializer = entriesApiJsonSerializer;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createProvisioningEntries(final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        commandWrapper = new CommandWrapperBuilder().createProvisioningEntries().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    @POST
    @Path("{entryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String modifyProvisioningEntry(@PathParam("entryId") final Long entryId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        if ("createjournalentry".equals(commandParam)) {
            commandWrapper = new CommandWrapperBuilder().createProvisioningJournalEntries(entryId).withJson(apiRequestBodyAsJson).build();
            final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService
                    .logCommandSource(commandWrapper);
            return this.toApiJsonSerializer.serialize(commandProcessingResult);
        } else if ("recreateprovisioningentry".equals(commandParam)) {
            commandWrapper = new CommandWrapperBuilder().reCreateProvisioningEntries(entryId).withJson(apiRequestBodyAsJson).build();
            final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService
                    .logCommandSource(commandWrapper);
            return this.toApiJsonSerializer.serialize(commandProcessingResult);
        }
        throw new UnrecognizedQueryParamException("command", commandParam);
    }

    @GET
    @Path("{entryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveProvisioningEntry(@PathParam("entryId") final Long entryId, @Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser();
        ProvisioningEntryData data = this.provisioningEntriesReadPlatformService.retrieveProvisioningEntryData(entryId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, data, ProvisioningEntriesApiConstants.PROVISIONING_ENTRY_PARAMETERS);
    }

    @GET
    @Path("entries")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveProviioningEntries(@QueryParam("entryId") final Long entryId, @QueryParam("offset") final Integer offset,
            @QueryParam("limit") final Integer limit, @QueryParam("officeId") final Long officeId,
            @QueryParam("productId") final Long productId, @QueryParam("categoryId") final Long categoryId, @Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser();
        SearchParameters params = SearchParameters.forProvisioningEntries(entryId, officeId, productId, categoryId, offset, limit);
        Page<LoanProductProvisioningEntryData> entries = this.provisioningEntriesReadPlatformService.retrieveProvisioningEntries(params);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.entriesApiJsonSerializer.serialize(settings, entries, ProvisioningEntriesApiConstants.PROVISIONING_ENTRY_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllProvisioningEntries(@QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser();
        Page<ProvisioningEntryData> data = this.provisioningEntriesReadPlatformService.retrieveAllProvisioningEntries(offset, limit);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.entriesApiJsonSerializer.serialize(settings, data, ProvisioningEntriesApiConstants.ALL_PROVISIONING_ENTRIES);
    }
}
