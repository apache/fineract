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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Created by Chirag Gupta on 12/29/17.
 */
public final class NotesApiResourceSwagger {

    private NotesApiResourceSwagger() {}

    @Schema(description = "PostResourceTypeResourceIdNotesRequest")
    public static final class PostResourceTypeResourceIdNotesRequest {

        private PostResourceTypeResourceIdNotesRequest() {}

        @Schema(example = "a note about the client")
        public String note;
    }

    @Schema(description = "PostResourceTypeResourceIdNotesResponse")
    public static final class PostResourceTypeResourceIdNotesResponse {

        private PostResourceTypeResourceIdNotesResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "76")
        public Long resourceId;
    }

    @Schema(description = "PutResourceTypeResourceIdNotesNoteIdResponse")
    public static final class PutResourceTypeResourceIdNotesNoteIdResponse {

        private PutResourceTypeResourceIdNotesNoteIdResponse() {}

        static final class PutNotesChanges {

            private PutNotesChanges() {}

            @Schema(example = "a note about the client")
            public String note;
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "76")
        public Long resourceId;
        public PutNotesChanges changes;
    }

    @Schema(description = "DeleteResourceTypeResourceIdNotesNoteIdResponse")
    public static final class DeleteResourceTypeResourceIdNotesNoteIdResponse {

        private DeleteResourceTypeResourceIdNotesNoteIdResponse() {}

        @Schema(example = "76")
        public Long resourceId;
    }
}
