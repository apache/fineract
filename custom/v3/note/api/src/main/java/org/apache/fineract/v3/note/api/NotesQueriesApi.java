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
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v3/{resourceType}/{resourceId}/notes", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
@Tag(name = "Notes", description = "Notes API allows to retrieve notes for supported resources. (Queries)")
@RequiredArgsConstructor
public class NotesQueriesApi {

    private final NoteReadPlatformService noteReadService;

    @GetMapping
    @Operation(summary = "Retrieve a Resource's description", description = """
            Retrieves a Resource's Notes

            Note: Notes are returned in descending createOn order.

            Example Requests:

            clients/2/notes

            groups/2/notes""")
    public Collection<NoteData> retrieveNotesByResource(@PathVariable("resourceType") final String resourceType,
            @PathVariable("resourceId") final Long resourceId) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        return noteReadService.retrieveNotesByResource(resourceId, noteType.getValue());
    }

    @GetMapping("/{noteId}")
    @Operation(summary = "Retrieve a Resource Note", description = """
            Retrieves a Resource Note

            Example Requests:

            clients/1/notes/76

            groups/1/notes/20

            clients/1/notes/76

            groups/1/notes/20""")
    public NoteData retrieveNote(@PathVariable("resourceType") final String resourceType, @PathVariable("resourceId") final Long resourceId,
            @PathVariable("noteId") final Long noteId) {

        final NoteType noteType = NoteType.fromApiUrl(resourceType);

        if (noteType == null) {
            throw new NoteResourceNotSupportedException(resourceType);
        }

        return this.noteReadService.retrieveNote(noteId, resourceId, noteType.getValue());
    }
}
