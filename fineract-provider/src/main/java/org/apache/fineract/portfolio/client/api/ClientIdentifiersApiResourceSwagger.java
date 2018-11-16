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
package org.apache.fineract.portfolio.client.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

/**
 * Created by Chirag Gupta on 01/13/18.
 */
final class ClientIdentifiersApiResourceSwagger {
    private ClientIdentifiersApiResourceSwagger() {
    }

    @ApiModel(value = "GetClientsClientIdIdentifiersResponse")
    public final static class GetClientsClientIdIdentifiersResponse {
        private GetClientsClientIdIdentifiersResponse() {
        }

        final class GetClientsDocumentType {
            private GetClientsDocumentType() {
            }

            @ApiModelProperty(example = "3")
            public Integer id;
            @ApiModelProperty(example = "Drivers License")
            public String name;
        }

        @ApiModelProperty(example = "2")
        public Integer id;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        public GetClientsDocumentType documentType;
        @ApiModelProperty(example = "12345")
        public String documentKey;
        @ApiModelProperty(example = "Issued in the year 2--7")
        public String description;
    }

    @ApiModel(value = "GetClientsClientIdIdentifiersTemplateResponse")
    public final static class GetClientsClientIdIdentifiersTemplateResponse {
        private GetClientsClientIdIdentifiersTemplateResponse() {
        }

        final class GetClientsAllowedDocumentTypes {
            private GetClientsAllowedDocumentTypes() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Passport")
            public String name;
            @ApiModelProperty(example = "0")
            public Integer position;
        }

        public Set<GetClientsAllowedDocumentTypes> allowedDocumentTypes;
    }

    @ApiModel(value = "PostClientsClientIdIdentifiersRequest")
    public final static class PostClientsClientIdIdentifiersRequest {
        private PostClientsClientIdIdentifiersRequest() {
        }

        @ApiModelProperty(example = "1")
        public Integer documentTypeId;
        @ApiModelProperty(example = "KA-54677")
        public String documentKey;
        @ApiModelProperty(example = "Document has been verified")
        public String description;
    }

    @ApiModel(value = "PutClientsClientIdIdentifiersIdentifierIdRequest")
    public final static class PutClientsClientIdIdentifiersIdentifierIdRequest {
        private PutClientsClientIdIdentifiersIdentifierIdRequest() {
        }

        @ApiModelProperty(example = "4")
        public Integer documentTypeId;
        @ApiModelProperty(example = "KA-94667")
        public String documentKey;
        @ApiModelProperty(example = "Document has been updated")
        public String description;
    }

    @ApiModel(value = "PutClientsClientIdIdentifiersIdentifierIdResponse")
    public final static class PutClientsClientIdIdentifiersIdentifierIdResponse {
        private PutClientsClientIdIdentifiersIdentifierIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "3")
        public Integer resourceId;
        public PutClientsClientIdIdentifiersIdentifierIdRequest changes;
    }

    @ApiModel(value = "PostClientsClientIdIdentifiersResponse")
    public final static class PostClientsClientIdIdentifiersResponse {
        private PostClientsClientIdIdentifiersResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "3")
        public Integer resourceId;
    }

    @ApiModel(value = "DeleteClientsClientIdIdentifiersIdentifierIdResponse")
    public final static class DeleteClientsClientIdIdentifiersIdentifierIdResponse {
        private DeleteClientsClientIdIdentifiersIdentifierIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "1")
        public Integer clientId;
        @ApiModelProperty(example = "3")
        public Integer resourceId;
    }
}
