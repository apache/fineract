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
package org.apache.fineract.infrastructure.security.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.useradministration.data.RoleData;

import java.util.Collection;

/**
 * Created by sanyam on 13/8/17.
 */
final class UserDetailsApiResourceSwagger {
    private UserDetailsApiResourceSwagger() {

    }

    @ApiModel(value = "GetUserDetailsResponse")
    public static final class GetUserDetailsResponse {
        private GetUserDetailsResponse(){

        }
        @ApiModelProperty(example = "mifos")
        public String username;
        @ApiModelProperty(example = "1")
        public Long userId;
        @ApiModelProperty(example = "bWlmb3M6cGFzc3dvcmQ=")
        public String accessToken;
        @ApiModelProperty(example = "true")
        public boolean authenticated;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = "1")
        public Long staffId;
        @ApiModelProperty(example = "mifosStaffDisplayName")
        public String staffDisplayName;
        public EnumOptionData organisationalRole;
        public Collection<RoleData> roles;
        @ApiModelProperty(example = "ALL_FUNCTIONS")
        public Collection<String> permissions;
    }
}
