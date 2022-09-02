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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Created by sanyam on 30/7/17.
 */
final class CodeValuesApiResourceSwagger {

    private CodeValuesApiResourceSwagger() {

    }

    @Schema(description = "GetCodeValuesDataResponse")
    public static final class GetCodeValuesDataResponse {

        private GetCodeValuesDataResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Passport")
        public String name;
        @Schema(example = "Passport information")
        public String description;
        @Schema(example = "0")
        public Integer position;
    }

    @Schema(description = "PostCodeValuesDataRequest")
    public static final class PostCodeValuesDataRequest {

        private PostCodeValuesDataRequest() {

        }

        @Schema(example = "Passport")
        public String name;
        @Schema(example = "true")
        public Boolean isActive;
        @Schema(example = "Passport information")
        public String description;
        @Schema(example = "0")
        public Integer position;
    }

    @Schema(description = "PostCodeValueDataResponse")
    public static final class PostCodeValueDataResponse {

        private PostCodeValueDataResponse() {

        }

        @Schema(example = "4")
        public Long resourceId;
    }

    @Schema(description = "PutCodeValuesDataRequest")
    public static final class PutCodeValuesDataRequest {

        private PutCodeValuesDataRequest() {

        }

        @Schema(example = "Passport")
        public String name;
        @Schema(example = "Passport information")
        public String description;
        @Schema(example = "0")
        public Integer position;
        @Schema(example = "true")
        public Boolean isActive;
    }

    @Schema(description = "PutCodeValueDataResponse")
    public static final class PutCodeValueDataResponse {

        private PutCodeValueDataResponse() {

        }

        private static final class PutCodeValuechangesSwagger {

            private PutCodeValuechangesSwagger() {}

            @Schema(example = "Passport")
            public String name;
            @Schema(example = "Passport information")
            public String description;
            @Schema(example = "0")
            public Integer position;
        }

        @Schema(example = "4")
        public Long resourceId;
        public PutCodeValuechangesSwagger changes;
    }

    @Schema(description = "DeleteCodeValueDataResponse")
    public static final class DeleteCodeValueDataResponse {

        private DeleteCodeValueDataResponse() {

        }

        @Schema(example = "4")
        public Long resourceId;
    }
}
