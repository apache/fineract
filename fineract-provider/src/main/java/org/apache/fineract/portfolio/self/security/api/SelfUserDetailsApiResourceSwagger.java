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
package org.apache.fineract.portfolio.self.security.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/20/17.
 */
final class SelfUserDetailsApiResourceSwagger {
    private SelfUserDetailsApiResourceSwagger() {
    }

    @ApiModel(value = "GetSelfUserDetailsResponse")
    public final static class GetSelfUserDetailsResponse {
        private GetSelfUserDetailsResponse() {
        }

        final class GetSelfUserDetailsOrganisationalRole {
            private GetSelfUserDetailsOrganisationalRole() {
            }

            @ApiModelProperty(example = "100")
            public Integer id;
            @ApiModelProperty(example = "staffOrganisationalRoleType.programDirector")
            public String code;
            @ApiModelProperty(example = "Program Director")
            public String value;
        }

        final class GetSelfUserDetailsRoles {
            private GetSelfUserDetailsRoles() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Super user")
            public String name;
            @ApiModelProperty(example = "This role provides all application permissions.")
            public String description;
        }

        @ApiModelProperty(example = "mifos")
        public String username;
        @ApiModelProperty(example = "1")
        public Integer userId;
        @ApiModelProperty(example = "bWlmb3M6cGFzc3dvcmQ=")
        public String base64EncodedAuthenticationKey;
        @ApiModelProperty(example = "true")
        public Boolean authenticated;
        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = "1")
        public Integer staffId;
        @ApiModelProperty(example = "Director, Program")
        public String staffDisplayName;
        public GetSelfUserDetailsOrganisationalRole organisationalRole;
        public Set<GetSelfUserDetailsRoles> roles;
        @ApiModelProperty(example = "ALL_FUNCTIONS")
        public List<String> permissions;
        @ApiModelProperty(example = "true")
        public Boolean isSelfServiceUser;
        @ApiModelProperty(example = "[1, 2, 3]")
        public List<Integer> clients;
    }
}
