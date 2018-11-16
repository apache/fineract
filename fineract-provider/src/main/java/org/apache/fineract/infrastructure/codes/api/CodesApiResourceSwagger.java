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
 * Created by sanyam on 29/7/17.
 */
final class CodesApiResourceSwagger {
    private CodesApiResourceSwagger() {
        // this class is only for Swagger Live Documentation
    }

    @ApiModel(value = "GetCodesResponse")
    public static final class GetCodesResponse {
        private GetCodesResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Education")
        public String name;
        @ApiModelProperty(example = "true")
        public boolean systemDefined;
    }

    @ApiModel(value = "PostCodesRequest")
    public static final class PostCodesRequest {
        private PostCodesRequest() {

        }
        @ApiModelProperty(example = "MyNewCode")
        public String name;
    }

    @ApiModel(value = "PostCodesResponse")
    public static final class PostCodesResponse {
        private PostCodesResponse() {

        }
        @ApiModelProperty(example = "4")
        public Long resourceId;
    }

    @ApiModel(value = "PutCodesRequest")
    public static final class PutCodesRequest {
        private PutCodesRequest() {

        }
        @ApiModelProperty(example = "MyNewCode(changed)")
        public String name;
    }

    @ApiModel(value = "PutCodesResponse")
    public static final class PutCodesResponse {
        private PutCodesResponse() {

        }
        private final class PutCodesApichangesSwagger{
            private PutCodesApichangesSwagger() {}
            @ApiModelProperty(example = "MyNewCode(changed)")
            public String name;
        }
        @ApiModelProperty(example = "4")
        public Long resourceId;
        public PutCodesApichangesSwagger changes;
    }

    @ApiModel(value = "DeleteCodesResponse")
    public static final class DeleteCodesResponse {
        private DeleteCodesResponse() {

        }
        @ApiModelProperty(example = "4")
        public Long resourceId;
    }
}
