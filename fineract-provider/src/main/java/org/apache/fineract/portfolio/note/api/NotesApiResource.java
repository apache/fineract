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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.portfolio.note.command.NoteCreateCommand;
import org.apache.fineract.portfolio.note.command.NoteDeleteCommand;
import org.apache.fineract.portfolio.note.command.NoteUpdateCommand;
import org.apache.fineract.portfolio.note.data.NoteCreateRequest;
import org.apache.fineract.portfolio.note.data.NoteCreateResponse;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.apache.fineract.portfolio.note.data.NoteDeleteRequest;
import org.apache.fineract.portfolio.note.data.NoteDeleteResponse;
import org.apache.fineract.portfolio.note.data.NoteUpdateRequest;
import org.apache.fineract.portfolio.note.data.NoteUpdateResponse;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/{resourceType}/{resourceId}/notes")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Notes", description = """
        Notes API allows to enter notes for supported resources.
        """)
@RequiredArgsConstructor
public class NotesApiResource {

    private final NoteReadPlatformService readPlatformService;
    private final CommandPipeline commandPipeline;

    @GET
    @Operation(summary = "Retrieve a Resource's description", description = """
            Retrieves a resource's notes

            Note: results are returned in descending createOn order.

            Example Requests:

            - clients/2/notes
            - groups/2/notes?fields=note,createdOn,createdByUsername
            """)
    public List<NoteData> retrieveNotesByResource(@PathParam("resourceType") final String resourceType,
            @PathParam("resourceId") final Long resourceId) {
        final var noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        return readPlatformService.retrieveNotesByResource(resourceId, noteType.getValue());
    }

    @GET
    @Path("{noteId}")
    @Operation(summary = "Retrieve a Resource Note", description = """
            Retrieves a resource Note

            Example Requests:

            - clients/1/notes/76
            - groups/1/notes/20
            - clients/1/notes/76?fields=note,createdOn,createdByUsername
            - groups/1/notes/20?fields=note,createdOn,createdByUsername
            """)
    public NoteData retrieveNote(@PathParam("resourceType") final String resourceType, @PathParam("resourceId") final Long resourceId,
            @PathParam("noteId") final Long noteId) {
        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        return readPlatformService.retrieveNote(noteId, resourceId, noteType.getValue());
    }

    @POST
    @Operation(summary = "Add a Resource Note", description = """
            Adds a new note to a supported resource.

            Example Requests:

            - clients/1/notes
            - groups/1/notes
            """)
    public NoteCreateResponse addNewNote(@PathParam("resourceType") final String resourceType,
            @PathParam("resourceId") final Long resourceId, @Valid final NoteCreateRequest request) {
        final var type = NoteType.fromApiUrl(resourceType);

        if (type == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        request.setResourceId(resourceId);
        request.setType(type);

        final var command = new NoteCreateCommand();

        command.setPayload(request);

        final Supplier<NoteCreateResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @PUT
    @Path("{noteId}")
    @Operation(summary = "Update a Resource Note", description = """
            Updates a Resource Note
            """)
    public NoteUpdateResponse updateNote(@PathParam("resourceType") final String resourceType,
            @PathParam("resourceId") final Long resourceId, @PathParam("noteId") final Long noteId,
            @Valid final NoteUpdateRequest request) {
        final var type = NoteType.fromApiUrl(resourceType);

        if (type == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        request.setId(noteId);
        request.setResourceId(resourceId);
        request.setType(type);

        final var command = new NoteUpdateCommand();

        command.setPayload(request);

        final Supplier<NoteUpdateResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @DELETE
    @Path("{noteId}")
    @Operation(summary = "Delete a Resource Note", description = """
            Deletes a Resource Note
            """)
    public NoteDeleteResponse deleteNote(@PathParam("resourceType") final String resourceType,
            @PathParam("resourceId") final Long resourceId, @PathParam("noteId") final Long noteId) {
        final var type = NoteType.fromApiUrl(resourceType);

        if (type == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        var request = NoteDeleteRequest.builder().id(noteId).resourceId(resourceId).type(type).build();

        final var command = new NoteDeleteCommand();

        command.setPayload(request);

        final Supplier<NoteDeleteResponse> response = commandPipeline.send(command);

        return response.get();
    }
}
