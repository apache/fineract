/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.note.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

/**
 * Created by Chirag Gupta on 12/29/17.
 */
final class NotesApiResourceSwagger {
    private NotesApiResourceSwagger() {
    }

    @ApiModel(value = "GetResourceTypeResourceIdNotesResponse")
    public final static class GetResourceTypeResourceIdNotesResponse {
        private GetResourceTypeResourceIdNotesResponse() {
        }

        final class GetNotesNoteType {
            private GetNotesNoteType() {
            }

            @ApiModelProperty(example = "100")
            public Integer id;
            @ApiModelProperty(example = "noteType.client")
            public String code;
            @ApiModelProperty(example = "Client note")
            public String value;
        }

        @ApiModelProperty(example = "2")
        public Integer id;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        public GetNotesNoteType noteType;
        @ApiModelProperty(example = "First note edited")
        public String note;
        @ApiModelProperty(example = "1")
        public Integer createdById;
        @ApiModelProperty(example = "mifos")
        public String createdByUsername;
        @ApiModelProperty(example = "1342498505000")
        public DateTime createdOn;
        @ApiModelProperty(example = "1")
        public Integer updatedById;
        @ApiModelProperty(example = "mifos")
        public String updatedByUsername;
        @ApiModelProperty(example = "1342498517000")
        public DateTime updatedOn;
    }

    @ApiModel(value = "GetResourceTypeResourceIdNotesNoteIdResponse")
    public final static class GetResourceTypeResourceIdNotesNoteIdResponse {
        private GetResourceTypeResourceIdNotesNoteIdResponse() {
        }

        @ApiModelProperty(example = "76")
        public Integer id;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        public GetResourceTypeResourceIdNotesResponse.GetNotesNoteType noteType;
        @ApiModelProperty(example = "a note about the client")
        public String note;
        @ApiModelProperty(example = "1")
        public Integer createdById;
        @ApiModelProperty(example = "mifos")
        public String createdByUsername;
        @ApiModelProperty(example = "1359463135000")
        public DateTime createdOn;
        @ApiModelProperty(example = "1")
        public Integer updatedById;
        @ApiModelProperty(example = "mifos")
        public String updatedByUsername;
        @ApiModelProperty(example = "1359463135000")
        public DateTime updatedOn;
    }

    @ApiModel(value = "PostResourceTypeResourceIdNotesRequest")
    public final static class PostResourceTypeResourceIdNotesRequest {
        private PostResourceTypeResourceIdNotesRequest() {
        }

        @ApiModelProperty(example = "a note about the client")
        public String note;
    }

    @ApiModel(value = "PostResourceTypeResourceIdNotesResponse")
    public final static class PostResourceTypeResourceIdNotesResponse {
        private PostResourceTypeResourceIdNotesResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "76")
        public Integer resourceId;
    }

    @ApiModel(value = "PutResourceTypeResourceIdNotesNoteIdRequest")
    public final static class PutResourceTypeResourceIdNotesNoteIdRequest {
        private PutResourceTypeResourceIdNotesNoteIdRequest() {
        }

        @ApiModelProperty(example = "a note about the client")
        public String note;
    }

    @ApiModel(value = "PutResourceTypeResourceIdNotesNoteIdResponse")
    public final static class PutResourceTypeResourceIdNotesNoteIdResponse {
        private PutResourceTypeResourceIdNotesNoteIdResponse() {
        }

        final class PutNotesChanges {
            private PutNotesChanges() {
            }

            @ApiModelProperty(example = "a note about the client")
            public String note;
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "76")
        public Integer resourceId;
        public PutNotesChanges changes;
    }

    @ApiModel(value = "DeleteResourceTypeResourceIdNotesNoteIdResponse")
    public final static class DeleteResourceTypeResourceIdNotesNoteIdResponse {
        private DeleteResourceTypeResourceIdNotesNoteIdResponse() {
        }

        @ApiModelProperty(example = "76")
        public Integer resourceId;
    }
}
