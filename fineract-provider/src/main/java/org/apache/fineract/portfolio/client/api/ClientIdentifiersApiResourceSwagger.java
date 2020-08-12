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
package org.apache.fineract.portfolio.client.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Chirag Gupta on 01/13/18.
 */
final class ClientIdentifiersApiResourceSwagger {

    private ClientIdentifiersApiResourceSwagger() {}

    @Schema(description = "GetClientsClientIdIdentifiersResponse")
    public static final class GetClientsClientIdIdentifiersResponse {

        private GetClientsClientIdIdentifiersResponse() {}

        static final class GetClientsDocumentType {

            private GetClientsDocumentType() {}

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "Drivers License")
            public String name;
        }

        @Schema(example = "2")
        public Integer id;
        @Schema(example = "1")
        public Integer clientId;
        public GetClientsDocumentType documentType;
        @Schema(example = "12345")
        public String documentKey;
        @Schema(example = "Issued in the year 2--7")
        public String description;
    }

    @Schema(description = "GetClientsClientIdIdentifiersTemplateResponse")
    public static final class GetClientsClientIdIdentifiersTemplateResponse {

        private GetClientsClientIdIdentifiersTemplateResponse() {}

        static final class GetClientsAllowedDocumentTypes {

            private GetClientsAllowedDocumentTypes() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Passport")
            public String name;
            @Schema(example = "0")
            public Integer position;
        }

        public Set<GetClientsAllowedDocumentTypes> allowedDocumentTypes;
    }

    @Schema(description = "PostClientsClientIdIdentifiersRequest")
    public static final class PostClientsClientIdIdentifiersRequest {

        private PostClientsClientIdIdentifiersRequest() {}

        @Schema(example = "1")
        public Integer documentTypeId;
        @Schema(example = "KA-54677")
        public String documentKey;
        @Schema(example = "Document has been verified")
        public String description;
    }

    @Schema(description = "PutClientsClientIdIdentifiersIdentifierIdRequest")
    public static final class PutClientsClientIdIdentifiersIdentifierIdRequest {

        private PutClientsClientIdIdentifiersIdentifierIdRequest() {}

        @Schema(example = "4")
        public Integer documentTypeId;
        @Schema(example = "KA-94667")
        public String documentKey;
        @Schema(example = "Document has been updated")
        public String description;
    }

    @Schema(description = "PutClientsClientIdIdentifiersIdentifierIdResponse")
    public static final class PutClientsClientIdIdentifiersIdentifierIdResponse {

        private PutClientsClientIdIdentifiersIdentifierIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "3")
        public Integer resourceId;
        public PutClientsClientIdIdentifiersIdentifierIdRequest changes;
    }

    @Schema(description = "PostClientsClientIdIdentifiersResponse")
    public static final class PostClientsClientIdIdentifiersResponse {

        private PostClientsClientIdIdentifiersResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "3")
        public Integer resourceId;
    }

    @Schema(description = "DeleteClientsClientIdIdentifiersIdentifierIdResponse")
    public static final class DeleteClientsClientIdIdentifiersIdentifierIdResponse {

        private DeleteClientsClientIdIdentifiersIdentifierIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "3")
        public Integer resourceId;
    }
}
