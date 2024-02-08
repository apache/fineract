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
package org.apache.fineract.infrastructure.documentmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * Created by sanyam on 7/8/17.
 */

final class DocumentManagementApiResourceSwagger {

    private DocumentManagementApiResourceSwagger() {

    }

    @Schema(description = "GetEntityTypeEntityIdDocumentsResponse")
    public static final class GetEntityTypeEntityIdDocumentsResponse {

        private GetEntityTypeEntityIdDocumentsResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "clients")
        public String parentEntityType;
        @Schema(example = "1")
        public Long parentEntityId;
        @Schema(example = "Client Details Form")
        public String name;
        @Schema(example = "CGAP.pdf")
        public String fileName;
        @Schema(example = "5246719")
        public Long size;
        @Schema(example = "application/pdf")
        public String type;
        @Schema(example = "A signed form signed by new member")
        public String description;
        @Schema(example = "")
        public String location;
        @Schema(example = "")
        public Integer storageType;
    }

    @Schema(description = "PostEntityTypeEntityIdDocumentsResponse")
    public static final class PostEntityTypeEntityIdDocumentsResponse {

        private PostEntityTypeEntityIdDocumentsResponse() {

        }

        @Schema(example = "3")
        public Long resourceId;
        @Schema(example = "3")
        public String resourceIdentifier;
    }

    @Schema(description = "PutEntityTypeEntityIdDocumentsResponse")
    public static final class PutEntityTypeEntityIdDocumentsResponse {

        private PutEntityTypeEntityIdDocumentsResponse() {

        }

        public static final class PutEntityTypeEntityIdDocumentsResponseChangesSwagger {

            private PutEntityTypeEntityIdDocumentsResponseChangesSwagger() {

            }

        }

        @Schema(example = "3")
        public Long resourceId;
        public PutEntityTypeEntityIdDocumentsResponse.PutEntityTypeEntityIdDocumentsResponseChangesSwagger changes;
        @Schema(example = "3")
        public String resourceIdentifier;
    }

    @Schema(description = "DeleteEntityTypeEntityIdDocumentsResponse")
    public static final class DeleteEntityTypeEntityIdDocumentsResponse {

        private DeleteEntityTypeEntityIdDocumentsResponse() {

        }

        public static final class PutEntityTypeEntityIdDocumentsResponseChangesSwagger {

            private PutEntityTypeEntityIdDocumentsResponseChangesSwagger() {

            }

        }

        @Schema(example = "3")
        public Long resourceId;
        public DeleteEntityTypeEntityIdDocumentsResponse.PutEntityTypeEntityIdDocumentsResponseChangesSwagger changes;
        @Schema(example = "3")
        public String resourceIdentifier;
    }

    @Schema(description = "Document upload request")
    public static final class DocumentUploadRequest extends UploadRequest {

        @Schema(name = "name", type = "string", accessMode = Schema.AccessMode.READ_WRITE)
        @FormDataParam("name")
        private String name;

        @Schema(name = "description", type = "string", accessMode = Schema.AccessMode.READ_WRITE)
        @FormDataParam("description")
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
