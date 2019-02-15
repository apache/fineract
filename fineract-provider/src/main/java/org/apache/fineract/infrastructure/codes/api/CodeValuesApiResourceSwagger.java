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
package org.apache.fineract.infrastructure.codes.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by sanyam on 30/7/17.
 */
final class CodeValuesApiResourceSwagger {
    private CodeValuesApiResourceSwagger() {

    }

    @ApiModel(value = "GetCodeValuesDataResponse")
    public static final class GetCodeValuesDataResponse {
        private GetCodeValuesDataResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Passport")
        public String name;
        @ApiModelProperty(example = "Passport information")
        public String description;
        @ApiModelProperty(example = "0")
        public Integer position;
    }

    @ApiModel(value = "PostCodeValuesDataRequest")
    public static final class PostCodeValuesDataRequest {
        private PostCodeValuesDataRequest() {

        }
        @ApiModelProperty(example = "Passport")
        public String name;
        @ApiModelProperty(example = "Passport information")
        public String description;
        @ApiModelProperty(example = "0")
        public Integer position;
    }

    @ApiModel(value = "PostCodeValueDataResponse")
    public static final class PostCodeValueDataResponse {
        private PostCodeValueDataResponse() {

        }
        @ApiModelProperty(example = "4")
        public Long resourceId;
    }

    @ApiModel(value = "PutCodeValuesDataRequest")
    public static final class PutCodeValuesDataRequest {
        private PutCodeValuesDataRequest() {

        }
        @ApiModelProperty(example = "Passport")
        public String name;
        @ApiModelProperty(example = "Passport information")
        public String description;
        @ApiModelProperty(example = "0")
        public Integer position;
    }

    @ApiModel(value = "PutCodeValueDataResponse")
    public static final class PutCodeValueDataResponse {
        private PutCodeValueDataResponse() {

        }
        private final class PutCodeValuechangesSwagger{
            private PutCodeValuechangesSwagger() {}
            @ApiModelProperty(example = "Passport")
            public String name;
            @ApiModelProperty(example = "Passport information")
            public String description;
            @ApiModelProperty(example = "0")
            public Integer position;
        }
        @ApiModelProperty(example = "4")
        public Long resourceId;
        public PutCodeValuechangesSwagger changes;
    }

    @ApiModel(value = "DeleteCodeValueDataResponse")
    public static final class DeleteCodeValueDataResponse {
        private DeleteCodeValueDataResponse() {

        }
        @ApiModelProperty(example = "4")
        public Long resourceId;
    }
}
