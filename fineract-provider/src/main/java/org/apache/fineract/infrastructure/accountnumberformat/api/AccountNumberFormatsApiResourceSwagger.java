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
package org.apache.fineract.infrastructure.accountnumberformat.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

import java.util.List;
import java.util.Map;

/**
 * Created by sanyam on 28/7/17.
 */
final class AccountNumberFormatsApiResourceSwagger {
    private AccountNumberFormatsApiResourceSwagger() {
        // this class is only for Swagger implementation for Live Documentation
    }

    @ApiModel(value = "GetAccountNumberFormatsResponse")
    public static final class GetAccountNumberFormatsResponse {
        private GetAccountNumberFormatsResponse() {

        }
//        public List<GetAccountNumberFormatsIdResponse> _;

    }

    @ApiModel(value = "GetAccountNumberFormatsIdResponse")
    public static final class GetAccountNumberFormatsIdResponse {
        private GetAccountNumberFormatsIdResponse() {

        }
        @ApiModelProperty(example = "2")
        public Long id;
        public EnumOptionData accountType;

        public EnumOptionData prefixType;

    }

    @ApiModel(value = "GetAccountNumberFormatsResponseTemplate")
    public static final class GetAccountNumberFormatsResponseTemplate {
        private GetAccountNumberFormatsResponseTemplate() {

        }
        public List<EnumOptionData> accountTypeOptions;
        public Map<String, List<EnumOptionData>> prefixTypeOptions;

    }

    @ApiModel(value = "PostAccountNumberFormatsRequest")
    public static final class PostAccountNumberFormatsRequest {
        private PostAccountNumberFormatsRequest() {

        }
        @ApiModelProperty(example = "1")
        public Long accountType;
        @ApiModelProperty(example = "101")
        public Long prefixType;

    }

    @ApiModel(value = "PostAccountNumberFormatsResponse")
    public static final class PostAccountNumberFormatsResponse {
        private PostAccountNumberFormatsResponse() {

        }
        @ApiModelProperty(example = "4")
        public Long resourceId;

    }

    @ApiModel(value = "PutAccountNumberFormatsRequest")
    public static final class PutAccountNumberFormatsRequest {
        private PutAccountNumberFormatsRequest() {

        }
        @ApiModelProperty(example = "1")
        public Long prefixType;

    }

    @ApiModel(value = "PutAccountNumberFormatsResponse")
    public static final class PutAccountNumberFormatsResponse {
        private PutAccountNumberFormatsResponse() {

        }
        public static final class PutAccountNumberFormatschangesSwagger {
            private PutAccountNumberFormatschangesSwagger() {

            }
            @ApiModelProperty(example = "OFFICE_NAME")
            public Long prefixType;
        }

        @ApiModelProperty(example = "2")
        public Long resourceId;
        public PutAccountNumberFormatschangesSwagger changes;

    }

    @ApiModel(value = "DeleteAccountNumberFormatsResponse")
    public static final class DeleteAccountNumberFormatsResponse {
        private DeleteAccountNumberFormatsResponse() {

        }
        @ApiModelProperty(example = "2")
        public Long resourceId;

    }

}
