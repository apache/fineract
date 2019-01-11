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
package org.apache.fineract.useradministration.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by sanyam on 22/8/17.
 */
final class PermissionsApiResourceSwagger {
    private PermissionsApiResourceSwagger() {

    }

    @ApiModel(value = "GetPermissionsResponse")
    public static final class GetPermissionsResponse {
        private GetPermissionsResponse() {

        }
        @ApiModelProperty(example = "authorisation")
        public String grouping;
        @ApiModelProperty(example = "READ_PERMISSION")
        public String code;
        @ApiModelProperty(example = "PERMISSION")
        public String entityName;
        @ApiModelProperty(example = "READ")
        public String actionName;
        @ApiModelProperty(example = "true")
        public Boolean selected;
    }

    @ApiModel(value = "PutPermissionsRequest")
    public static final class PutPermissionsRequest {
        private PutPermissionsRequest() {

        }
        @ApiModelProperty(example = "\"CREATE_GUARANTOR\":true,\n" +
                "    \"CREATE_CLIENT\":true")
        public String permissions;
    }
}
