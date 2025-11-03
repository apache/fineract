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
 * Created by sanyam on 29/7/17.
 */
final class CodesApiResourceSwagger {

    private CodesApiResourceSwagger() {
        // this class is only for Swagger Live Documentation
    }

    @Schema(description = "GetCodesResponse")
    public static final class GetCodesResponse {

        private GetCodesResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Education")
        public String name;
        @Schema(example = "true")
        public boolean systemDefined;
    }

    @Schema(description = "PostCodesRequest")
    public static final class PostCodesRequest {

        private PostCodesRequest() {

        }

        @Schema(example = "MyNewCode")
        public String name;
    }

    @Schema(description = "PostCodesResponse")
    public static final class PostCodesResponse {

        private PostCodesResponse() {

        }

        @Schema(example = "4")
        public Long resourceId;
    }

    @Schema(description = "PutCodesRequest")
    public static final class PutCodesRequest {

        private PutCodesRequest() {

        }

        @Schema(example = "MyNewCode(changed)")
        public String name;
    }

    @Schema(description = "PutCodesResponse")
    public static final class PutCodesResponse {

        private PutCodesResponse() {

        }

        private static final class PutCodesApichangesSwagger {

            private PutCodesApichangesSwagger() {}

            @Schema(example = "MyNewCode(changed)")
            public String name;
        }

        @Schema(example = "4")
        public Long resourceId;
        public PutCodesApichangesSwagger changes;
    }

    @Schema(description = "DeleteCodesResponse")
    public static final class DeleteCodesResponse {

        private DeleteCodesResponse() {

        }

        @Schema(example = "4")
        public Long resourceId;
    }
}
