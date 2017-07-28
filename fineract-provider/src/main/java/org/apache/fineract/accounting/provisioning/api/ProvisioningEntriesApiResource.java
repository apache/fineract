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
package org.apache.fineract.accounting.provisioning.api;

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

import org.apache.fineract.accounting.provisioning.constant.ProvisioningEntriesApiConstants;
import org.apache.fineract.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
	private static final Set<String> PROVISIONING_ENTRY_PARAMETERS = new HashSet<>(Arrays.asList(
			ProvisioningEntriesApiConstants.PROVISIONINGENTRY_PARAM, ProvisioningEntriesApiConstants.ENTRIES_PARAM));
    private static final Set<String> ALL_PROVISIONING_ENTRIES = new HashSet<>(Arrays.asList
            (ProvisioningEntriesApiConstants.PROVISIONINGENTRY_PARAM));
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
        return this.toApiJsonSerializer.serialize(settings, data, PROVISIONING_ENTRY_PARAMETERS);
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
        return this.entriesApiJsonSerializer.serialize(settings, entries, PROVISIONING_ENTRY_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllProvisioningEntries(@QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser();
        Page<ProvisioningEntryData> data = this.provisioningEntriesReadPlatformService.retrieveAllProvisioningEntries(offset, limit);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.entriesApiJsonSerializer.serialize(settings, data, ALL_PROVISIONING_ENTRIES);
    }
}
