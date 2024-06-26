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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Component;

@Path("/v1/{resourceType}/{resourceId}/notes")
@Component
@Tag(name = "Notes", description = "Notes API allows to enter notes for supported resources.")
@RequiredArgsConstructor
public class NotesApiResource {

    public static final String CLIENTNOTE = "CLIENTNOTE";
    public static final String LOANNOTE = "LOANNOTE";
    public static final String LOANTRANSACTIONNOTE = "LOANTRANSACTIONNOTE";
    public static final String SAVINGNOTE = "SAVINGNOTE";
    public static final String GROUPNOTE = "GROUPNOTE";
    public static final String INVALIDNOTE = "INVALIDNOTE";
    private static final Set<String> NOTE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "resourceId", "clientId", "groupId", "loanId", "loanTransactionId", "depositAccountId", "savingAccountId",
                    "noteType", "note", "createdById", "createdByUsername", "createdOn", "updatedById", "updatedByUsername", "updatedOn"));
    private final PlatformSecurityContext context;
    private final NoteReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<NoteData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Resource's description", description = "Retrieves a Resource's Notes\n\n"
            + "Note: Notes are returned in descending createOn order.\n" + "\n" + "Example Requests:\n" + "\n" + "clients/2/notes\n" + "\n"
            + "\n" + "groups/2/notes?fields=note,createdOn,createdByUsername")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotesApiResourceSwagger.GetResourceTypeResourceIdNotesResponse.class)))) })
    public String retrieveNotesByResource(@PathParam("resourceType") @Parameter(description = "resourceType") final String resourceType,
            @PathParam("resourceId") @Parameter(description = "resourceId") final Long resourceId, @Context final UriInfo uriInfo) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        this.context.authenticatedUser().validateHasReadPermission(getResourceDetails(noteType, resourceId).entityName());

        final Integer noteTypeId = noteType.getValue();

        final Collection<NoteData> notes = this.readPlatformService.retrieveNotesByResource(resourceId, noteTypeId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, notes, NOTE_DATA_PARAMETERS);
    }

    @GET
    @Path("{noteId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Resource Note", description = "Retrieves a Resource Note\n\n" + "Example Requests:\n" + "\n"
            + "clients/1/notes/76\n" + "\n" + "\n" + "groups/1/notes/20\n" + "\n" + "\n"
            + "clients/1/notes/76?fields=note,createdOn,createdByUsername\n" + "\n" + "\n"
            + "groups/1/notes/20?fields=note,createdOn,createdByUsername")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = NotesApiResourceSwagger.GetResourceTypeResourceIdNotesNoteIdResponse.class))) })
    public String retrieveNote(@PathParam("resourceType") @Parameter(description = "resourceType") final String resourceType,
            @PathParam("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @PathParam("noteId") @Parameter(description = "noteId") final Long noteId, @Context final UriInfo uriInfo) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        this.context.authenticatedUser().validateHasReadPermission(getResourceDetails(noteType, resourceId).entityName());

        final Integer noteTypeId = noteType.getValue();

        final NoteData note = this.readPlatformService.retrieveNote(noteId, resourceId, noteTypeId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, note, NOTE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Add a Resource Note", description = "Adds a new note to a supported resource.\n\n" + "Example Requests:\n" + "\n"
            + "clients/1/notes\n" + "\n" + "\n" + "groups/1/notes")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = NotesApiResourceSwagger.PostResourceTypeResourceIdNotesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = NotesApiResourceSwagger.PostResourceTypeResourceIdNotesResponse.class))) })
    public String addNewNote(@PathParam("resourceType") @Parameter(description = "resourceType") final String resourceType,
            @PathParam("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

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
    @Operation(summary = "Update a Resource Note", description = "Updates a Resource Note")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = NotesApiResourceSwagger.PutResourceTypeResourceIdNotesNoteIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = NotesApiResourceSwagger.PutResourceTypeResourceIdNotesNoteIdResponse.class))) })
    public String updateNote(@PathParam("resourceType") @Parameter(description = "resourceType") final String resourceType,
            @PathParam("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @PathParam("noteId") @Parameter(description = "noteId") final Long noteId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

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
    @Operation(summary = "Delete a Resource Note", description = "Deletes a Resource Note")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = NotesApiResourceSwagger.DeleteResourceTypeResourceIdNotesNoteIdResponse.class))) })
    public String deleteNote(@PathParam("resourceType") @Parameter(description = "resourceType") final String resourceType,
            @PathParam("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @PathParam("noteId") @Parameter(description = "noteId") final Long noteId) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        final CommandWrapper resourceDetails = getResourceDetails(noteType, resourceId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteNote(resourceDetails, resourceType, resourceId, noteId)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private CommandWrapper getResourceDetails(final NoteType type, final Long resourceId) {
        CommandWrapperBuilder resourceDetails = new CommandWrapperBuilder();
        String resourceNameForPermissions;
        switch (type) {
            case CLIENT -> {
                resourceNameForPermissions = CLIENTNOTE;
                resourceDetails.withClientId(resourceId);
            }
            case LOAN -> {
                resourceNameForPermissions = LOANNOTE;
                resourceDetails.withLoanId(resourceId);
            }
            case LOAN_TRANSACTION -> {
                resourceNameForPermissions = LOANTRANSACTIONNOTE;
                // updating loanId, to distinguish saving transaction note and
                // loan transaction note as we are using subEntityId for both.
                resourceDetails.withLoanId(resourceId);
                resourceDetails.withSubEntityId(resourceId);
            }
            case SAVING_ACCOUNT -> {
                resourceNameForPermissions = SAVINGNOTE;
                resourceDetails.withSavingsId(resourceId);
            }
            case GROUP -> {
                resourceNameForPermissions = GROUPNOTE;
                resourceDetails.withGroupId(resourceId);
            }
            default -> resourceNameForPermissions = INVALIDNOTE;
        }
        return resourceDetails.withEntityName(resourceNameForPermissions).build();
    }
}
