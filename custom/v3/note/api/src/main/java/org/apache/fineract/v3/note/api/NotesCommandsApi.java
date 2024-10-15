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

package org.apache.fineract.v3.note.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.v3.command.service.CommandDispatcher;
import org.apache.fineract.v3.note.data.NoteCreateRequest;
import org.apache.fineract.v3.note.data.NoteCreateResponse;
import org.apache.fineract.v3.note.data.NoteDeleteRequest;
import org.apache.fineract.v3.note.data.NoteDeleteResponse;
import org.apache.fineract.v3.note.data.NoteRequestBody;
import org.apache.fineract.v3.note.data.NoteUpdateRequest;
import org.apache.fineract.v3.note.data.NoteUpdateResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v3/{resourceType}/{resourceId}/notes", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
@Tag(name = "Notes", description = "Notes API allows to manage notes for supported resources.")
@RequiredArgsConstructor
public class NotesCommandsApi {

    private final CommandDispatcher commandDispatcher;

    @PostMapping
    @Operation(summary = "Add a Resource Note", description = """
            Adds a new note to a supported resource.

            Example Requests:

            clients/1/notes

            groups/1/notes""")
    public NoteCreateResponse addNewNote(@PathVariable("resourceType") @Parameter(description = "resourceType") final String resourceType,
            @PathVariable("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @Valid @RequestBody NoteRequestBody apiRequestBody) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        // TODO: set more metadata (username, tenant ID ...etc.)
        var noteCreateRequest = NoteCreateRequest.builder() //
                .resourceId(resourceId) //
                .noteType(noteType) //
                .body(apiRequestBody) //
                .build();

        return (NoteCreateResponse) commandDispatcher.dispatch(noteCreateRequest);
    }

    @PutMapping("{noteId}")
    @Operation(summary = "Update a Resource Note", description = "Updates a Resource Note")
    public NoteUpdateResponse updateNote(@PathVariable("resourceType") @Parameter(description = "resourceType") final String resourceType,
            @PathVariable("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @PathVariable("noteId") @Parameter(description = "noteId") final Long noteId,
            @Valid @RequestBody NoteRequestBody apiRequestBody) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        // TODO: set more metadata (username, tenant ID ...etc.)
        var noteUpdateRequest = NoteUpdateRequest.builder() //
                .resourceId(resourceId) //
                .noteId(noteId) //
                .noteType(noteType) //
                .body(apiRequestBody) //
                .build();

        return (NoteUpdateResponse) commandDispatcher.dispatch(noteUpdateRequest);
    }

    @DeleteMapping("{noteId}")
    @Operation(summary = "Delete a Resource Note", description = "Deletes a Resource Note")
    public NoteDeleteResponse deleteNote(@PathVariable("resourceType") @Parameter(description = "resourceType") final String resourceType,
            @PathVariable("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @PathVariable("noteId") @Parameter(description = "noteId") final Long noteId) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        // TODO: set more metadata (username, tenant ID ...etc.)
        var noteDeleteRequest = NoteDeleteRequest.builder() //
                .resourceId(resourceId) //
                .noteId(noteId) //
                .noteType(noteType) //
                .build();

        return (NoteDeleteResponse) commandDispatcher.dispatch(noteDeleteRequest);
    }

}
