/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.api;

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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.note.data.NoteData;
import org.mifosplatform.portfolio.note.domain.NoteType;
import org.mifosplatform.portfolio.note.exception.NoteResourceNotSupportedException;
import org.mifosplatform.portfolio.note.service.NoteReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/{resourceType}/{resourceId}/notes")
@Component
@Scope("singleton")
public class NotesApiResource {

    private final Set<String> NOTE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "clientId", "groupId", "loanId",
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

        NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }
        ;

        this.context.authenticatedUser().validateHasReadPermission(getResourceNameForPermissions(noteType));

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

        NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }
        ;

        this.context.authenticatedUser().validateHasReadPermission(getResourceNameForPermissions(noteType));

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

        NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }
        ;

        final String resourceNameForPermissions = getResourceNameForPermissions(noteType);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createNote(resourceNameForPermissions, resourceType, resourceId)
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

        NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }
        ;

        final String resourceNameForPermissions = getResourceNameForPermissions(noteType);

        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .updateNote(resourceNameForPermissions, resourceType, resourceId, noteId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{noteId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteNote(@PathParam("resourceType") final String resourceType, @PathParam("resourceId") final Long resourceId,
            @PathParam("noteId") final Long noteId) {

        NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) { throw new NoteResourceNotSupportedException(resourceType); }
        ;

        final String resourceNameForPermissions = getResourceNameForPermissions(noteType);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteNote(resourceNameForPermissions, resourceType, resourceId,
                noteId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private String getResourceNameForPermissions(final NoteType type) {
        String resourceNameForPermissions = "INVALIDNOTE";
        switch (type) {
            case CLIENT:
                resourceNameForPermissions = "CLIENTNOTE";
            break;
            case LOAN:
                resourceNameForPermissions = "LOANNOTE";
            break;
            case LOAN_TRANSACTION:
                resourceNameForPermissions = "LOANTRANSACTIONNOTE";
            break;
            case SAVING_ACCOUNT:
                resourceNameForPermissions = "SAVINGNOTE";
            break;
            case GROUP:
                resourceNameForPermissions = "GROUPNOTE";
            break;

        }

        return resourceNameForPermissions;
    }

}