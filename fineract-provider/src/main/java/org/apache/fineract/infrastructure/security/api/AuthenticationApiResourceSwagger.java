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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.useradministration.data.RoleData;

/**
 * Created by sanyam on 13/8/17.
 */
public final class AuthenticationApiResourceSwagger {

    private AuthenticationApiResourceSwagger() {

    }

    @Schema(description = "PostAuthenticationRequest")
    public static final class PostAuthenticationRequest {

        private PostAuthenticationRequest() {

        }

        @Schema(required = true, example = "mifos")
        public String username;
        @Schema(required = true, example = "password")
        public String password;
    }

    @Schema(description = "PostAuthenticationResponse")
    public static final class PostAuthenticationResponse {

        private PostAuthenticationResponse() {

        }

        @Schema(example = "mifos")
        public String username;
        @Schema(example = "1")
        public Long userId;
        @Schema(example = "bWlmb3M6cGFzc3dvcmQ=")
        public String base64EncodedAuthenticationKey;
        @Schema(example = "true")
        public boolean authenticated;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "Head Office")
        public String officeName;
        @Schema(example = "1")
        public Long staffId;
        @Schema(example = "Director, Program")
        public String staffDisplayName;
        public EnumOptionData organisationalRole;
        public Collection<RoleData> roles;
        @Schema(example = "ALL_FUNCTIONS")
        public Collection<String> permissions;
    }
}
