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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by sanyam on 7/8/17.
 */

final class DocumentManagementApiResourceSwagger {
    private DocumentManagementApiResourceSwagger() {

    }

    @ApiModel(value = "GetEntityTypeEntityIdDocumentsResponse")
    public static final class GetEntityTypeEntityIdDocumentsResponse {
        private GetEntityTypeEntityIdDocumentsResponse() {

        }

        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "clients")
        public String parentEntityType;
        @ApiModelProperty(example = "1")
        public Long parentEntityId;
        @ApiModelProperty(example = "Client Details Form")
        public String name;
        @ApiModelProperty(example = "CGAP.pdf")
        public String fileName;
        @ApiModelProperty(example = "5246719")
        public Long size;
        @ApiModelProperty(example = "application/pdf")
        public String type;
        @ApiModelProperty(example = "A signed form signed by new member")
        public String description;
        @ApiModelProperty(example = "")
        public String location;
        @ApiModelProperty(example = "")
        public Integer storageType;
    }

    @ApiModel(value = "PostEntityTypeEntityIdDocumentsResponse")
    public static final class PostEntityTypeEntityIdDocumentsResponse {
        private PostEntityTypeEntityIdDocumentsResponse() {

        }
        @ApiModelProperty(example = "3")
        public Long resourceId;
        @ApiModelProperty(example = "3")
        public String resourceIdentifier;
    }

    @ApiModel(value = "PutEntityTypeEntityIdDocumentsResponse")
    public static final class PutEntityTypeEntityIdDocumentsResponse {
        private PutEntityTypeEntityIdDocumentsResponse() {

        }

        public final class PutEntityTypeEntityIdDocumentsResponseChangesSwagger{
            private PutEntityTypeEntityIdDocumentsResponseChangesSwagger() {

            }

        }
        @ApiModelProperty(example = "3")
        public Long resourceId;
        public PutEntityTypeEntityIdDocumentsResponseChangesSwagger changes;
        @ApiModelProperty(example = "3")
        public String resourceIdentifier;
    }

    @ApiModel(value = "DeleteEntityTypeEntityIdDocumentsResponse")
    public static final class DeleteEntityTypeEntityIdDocumentsResponse {
        private DeleteEntityTypeEntityIdDocumentsResponse() {

        }

        public final class PutEntityTypeEntityIdDocumentsResponseChangesSwagger{
            private PutEntityTypeEntityIdDocumentsResponseChangesSwagger() {

            }

        }
        @ApiModelProperty(example = "3")
        public Long resourceId;
        public PutEntityTypeEntityIdDocumentsResponseChangesSwagger changes;
        @ApiModelProperty(example = "3")
        public String resourceIdentifier;
    }

}
