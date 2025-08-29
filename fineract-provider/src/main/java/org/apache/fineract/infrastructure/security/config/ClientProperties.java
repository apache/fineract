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
package org.apache.fineract.infrastructure.security.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ConfigurationProperties(prefix = "fineract.security.oauth2.clients")
public final class ClientProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Map<String, Registration> registrations = new HashMap<>();

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static final class Registration implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String clientId;
        @Builder.Default
        private List<String> scopes = new ArrayList<>();

        @Builder.Default
        private List<String> authorizationGrantTypes = new ArrayList<>();

        @Builder.Default
        private List<String> redirectUris = new ArrayList<>();

        @Builder.Default
        private boolean requireAuthorizationConsent = true;
    }
}
