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
package org.apache.fineract.portfolio.note.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/{resourceType}/{resourceId}/notes")
@Component
@Scope("singleton")
public class NotesApiResource {

    private final Set<String> NOTE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "clientId", "groupId", "loanId",
            "loanTransactionId", "depositAccountId", "savingAccountId", "noteType", "note", "createdById", "createdByUsername",
            "createdOn", "updatedById", "updatedByUsername", "updatedOn"));

    private final PlatformSecurityContext context;
    private final NoteReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<NoteData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public NotesApiResource(final PlatformSecurityContext context, final NoteReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<NoteData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNotesByResource(@PathParam("resourceType") final String resourceType,
            @PathParam("resourceId") final Long resourceId, @Context final UriInfo uriInfo) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }

        this.context.authenticatedUser().validateHasReadPermission(getResourceDetails(noteType, resourceId).entityName());

        final Integer noteTypeId = noteType.getValue();

        final Collection<NoteData> notes = this.readPlatformService.retrieveNotesByResource(resourceId, noteTypeId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, notes, this.NOTE_DATA_PARAMETERS);
    }

    @GET
    @Path("{noteId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNote(@PathParam("resourceType") final String resourceType, @PathParam("resourceId") final Long resourceId,
            @PathParam("noteId") final Long noteId, @Context final UriInfo uriInfo) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }

        this.context.authenticatedUser().validateHasReadPermission(getResourceDetails(noteType, resourceId).entityName());

        final Integer noteTypeId = noteType.getValue();

        final NoteData note = this.readPlatformService.retrieveNote(noteId, resourceId, noteTypeId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, note, this.NOTE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String addNewNote(@PathParam("resourceType") final String resourceType, @PathParam("resourceId") final Long resourceId,
            final String apiRequestBodyAsJson) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }

        final CommandWrapper resourceDetails = getResourceDetails(noteType, resourceId);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createNote(resourceDetails, resourceType, resourceId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{noteId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateNote(@PathParam("resourceType") final String resourceType, @PathParam("resourceId") final Long resourceId,
            @PathParam("noteId") final Long noteId, final String apiRequestBodyAsJson) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }

        final CommandWrapper resourceDetails = getResourceDetails(noteType, resourceId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateNote(resourceDetails, resourceType, resourceId, noteId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{noteId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteNote(@PathParam("resourceType") final String resourceType, @PathParam("resourceId") final Long resourceId,
            @PathParam("noteId") final Long noteId) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }

        final CommandWrapper resourceDetails = getResourceDetails(noteType, resourceId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteNote(resourceDetails, resourceType, resourceId, noteId)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private CommandWrapper getResourceDetails(final NoteType type, final Long resourceId) {
        CommandWrapperBuilder resourceDetails = new CommandWrapperBuilder();
        String resourceNameForPermissions = "INVALIDNOTE";
        switch (type) {
            case CLIENT:
                resourceNameForPermissions = "CLIENTNOTE";
                resourceDetails.withClientId(resourceId);
            break;
            case LOAN:
                resourceNameForPermissions = "LOANNOTE";
                resourceDetails.withLoanId(resourceId);
            break;
            case LOAN_TRANSACTION:
                resourceNameForPermissions = "LOANTRANSACTIONNOTE";
                // updating loanId, to distinguish saving transaction note and loan transaction note as we are using subEntityId for both.
                resourceDetails.withLoanId(resourceId);
                resourceDetails.withSubEntityId(resourceId);
            break;
            case SAVING_ACCOUNT:
                resourceNameForPermissions = "SAVINGNOTE";
                resourceDetails.withSavingsId(resourceId);
            break;
            case GROUP:
                resourceNameForPermissions = "GROUPNOTE";
                resourceDetails.withGroupId(resourceId);
            break;

        }

        return resourceDetails.withEntityName(resourceNameForPermissions).build();
    }

}