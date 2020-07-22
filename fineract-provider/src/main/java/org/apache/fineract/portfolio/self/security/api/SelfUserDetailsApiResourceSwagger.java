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
package org.apache.fineract.portfolio.self.security.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/20/17.
 */
final class SelfUserDetailsApiResourceSwagger {

    private SelfUserDetailsApiResourceSwagger() {}

    @Schema(description = "GetSelfUserDetailsResponse")
    public static final class GetSelfUserDetailsResponse {

        private GetSelfUserDetailsResponse() {}

        static final class GetSelfUserDetailsOrganisationalRole {

            private GetSelfUserDetailsOrganisationalRole() {}

            @Schema(example = "100")
            public Integer id;
            @Schema(example = "staffOrganisationalRoleType.programDirector")
            public String code;
            @Schema(example = "Program Director")
            public String description;
        }

        static final class GetSelfUserDetailsRoles {

            private GetSelfUserDetailsRoles() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Super user")
            public String name;
            @Schema(example = "This role provides all application permissions.")
            public String description;
        }

        @Schema(example = "mifos")
        public String username;
        @Schema(example = "1")
        public Integer userId;
        @Schema(example = "bWlmb3M6cGFzc3dvcmQ=")
        public String base64EncodedAuthenticationKey;
        @Schema(example = "true")
        public Boolean authenticated;
        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "Head Office")
        public String officeName;
        @Schema(example = "1")
        public Integer staffId;
        @Schema(example = "Director, Program")
        public String staffDisplayName;
        public GetSelfUserDetailsOrganisationalRole organisationalRole;
        public Set<GetSelfUserDetailsRoles> roles;
        @Schema(example = "ALL_FUNCTIONS")
        public List<String> permissions;
        @Schema(example = "true")
        public Boolean isSelfServiceUser;
        @Schema(example = "[1, 2, 3]")
        public List<Integer> clients;
    }
}
