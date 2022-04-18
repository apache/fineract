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
final class ClientContactInformationApiResourceSwagger {

    private ClientContactInformationApiResourceSwagger() {}

    @Schema(description = "GetClientsContactInformationResponse")
    public static final class GetClientsContactInformationResponse {

        private GetClientsContactInformationResponse() {}

        static final class GetClientsDocumentType {

            private GetClientsDocumentType() {}

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "Personal Phone Number")
            public String name;
        }

        @Schema(example = "2")
        public Integer id;
        @Schema(example = "1")
        public Integer clientId;
        public GetClientsDocumentType contactType;
        @Schema(example = "+1234567890")
        public String contactKey;
    }

    @Schema(description = "GetClientsContactInformationTemplateResponse")
    public static final class GetClientsContactInformationTemplateResponse {

        private GetClientsContactInformationTemplateResponse() {}

        static final class GetClientsAllowedDocumentTypes {

            private GetClientsAllowedDocumentTypes() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Email")
            public String name;
            @Schema(example = "0")
            public Integer position;
        }

        public Set<GetClientsAllowedDocumentTypes> allowedDocumentTypes;
    }

    @Schema(description = "PostClientsContactInformationRequest")
    public static final class PostClientsContactInformationRequest {

        private PostClientsContactInformationRequest() {}

        @Schema(example = "1")
        public Integer contactTypeId;
        @Schema(example = "user@example.com")
        public String contactKey;
    }

    @Schema(description = "PutClientsContactInformationIdentifierIdRequest")
    public static final class PutClientsContactInformationIdentifierIdRequest {

        private PutClientsContactInformationIdentifierIdRequest() {}

        @Schema(example = "1")
        public Integer contactTypeId;
        @Schema(example = "user@example.com")
        public String contactKey;
    }

    @Schema(description = "PutClientsContactInformationIdentifierIdResponse")
    public static final class PutClientsContactInformationIdentifierIdResponse {

        private PutClientsContactInformationIdentifierIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "3")
        public Integer resourceId;
        public PutClientsContactInformationIdentifierIdRequest changes;
    }

    @Schema(description = "PostClientsContactInformationResponse")
    public static final class PostClientsContactInformationResponse {

        private PostClientsContactInformationResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "3")
        public Integer resourceId;
    }

    @Schema(description = "DeleteClientsContactInformationIdentifierIdResponse")
    public static final class DeleteClientsContactInformationIdentifierIdResponse {

        private DeleteClientsContactInformationIdentifierIdResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer clientId;
        @Schema(example = "3")
        public Integer resourceId;
    }
}
