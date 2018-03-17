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

import io.swagger.annotations.*;
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

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Path("/{resourceType}/{resourceId}/notes")
@Component
@Scope("singleton")
@Api(value = "Notes", description = "Notes API allows to enter notes for supported resources.")
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
    @ApiOperation(value = "Retrieve a Resource's Notes", httpMethod = "GET", notes = "Retrieves a Resource's Notes\n\n" + "Note: Notes are returned in descending createOn order.\n" + "\n" + "Example Requests:\n" + "\n" + "clients/2/notes\n" + "\n" + "\n" + "groups/2/notes?fields=note,createdOn,createdByUsername")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", responseContainer = "List", response = NotesApiResourceSwagger.GetResourceTypeResourceIdNotesResponse.class)})
    public String retrieveNotesByResource(@PathParam("resourceType") @ApiParam(value = "resourceType") final String resourceType,
            @PathParam("resourceId") @ApiParam(value = "resourceId") final Long resourceId, @Context final UriInfo uriInfo) {

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
    @ApiOperation(value = "Retrieve a Resource Note", httpMethod = "GET", notes = "Retrieves a Resource Note\n\n" + "Example Requests:\n" + "\n" + "clients/1/notes/76\n" + "\n" + "\n" + "groups/1/notes/20\n" + "\n" + "\n" + "clients/1/notes/76?fields=note,createdOn,createdByUsername\n" + "\n" + "\n" + "groups/1/notes/20?fields=note,createdOn,createdByUsername")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = NotesApiResourceSwagger.GetResourceTypeResourceIdNotesNoteIdResponse.class)})
    public String retrieveNote(@PathParam("resourceType") @ApiParam(value = "resourceType")final String resourceType, @PathParam("resourceId") @ApiParam(value = "resourceId")final Long resourceId,
            @PathParam("noteId") @ApiParam(value = "noteId") final Long noteId, @Context final UriInfo uriInfo) {

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
    @ApiOperation(value = "Add a Resource Note", httpMethod = "POST", notes = "Adds a new note to a supported resource.\n\n" + "Example Requests:\n" + "\n" + "clients/1/notes\n" + "\n" + "\n" + "groups/1/notes")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = NotesApiResourceSwagger.PostResourceTypeResourceIdNotesRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = NotesApiResourceSwagger.PostResourceTypeResourceIdNotesResponse.class)})
    public String addNewNote(@PathParam("resourceType") @ApiParam(value = "resourceType")final String resourceType, @PathParam("resourceId") @ApiParam(value = "resourceId")final Long resourceId,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Update a Resource Note", httpMethod = "PUT", notes = "Updates a Resource Note")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = NotesApiResourceSwagger.PutResourceTypeResourceIdNotesNoteIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = NotesApiResourceSwagger.PutResourceTypeResourceIdNotesNoteIdResponse.class)})
    public String updateNote(@PathParam("resourceType") @ApiParam(value = "resourceType") final String resourceType, @PathParam("resourceId") @ApiParam(value = "resourceId") final Long resourceId,
            @PathParam("noteId") @ApiParam(value = "noteId") final Long noteId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Delete a Resource Note", httpMethod = "DELETE", notes = "Deletes a Resource Note")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = NotesApiResourceSwagger.DeleteResourceTypeResourceIdNotesNoteIdResponse.class)})
    public String deleteNote(@PathParam("resourceType") @ApiParam(value = "resourceType")final String resourceType, @PathParam("resourceId") @ApiParam(value = "resourceId")final Long resourceId,
            @PathParam("noteId") @ApiParam(value = "noteId")final Long noteId) {

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