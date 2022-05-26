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

package org.apache.fineract.infrastructure.core.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityValidationConfig {

    @Value("${fineract.security.basicauth.enabled}")
    private Boolean basicAuthEnabled;

    @Value("${fineract.security.oauth.enabled}")
    private Boolean oauthEnabled;

    @PostConstruct
    public void validate() {
        // NOTE: avoid NPE if these values are not set
        if (!Boolean.TRUE.equals(basicAuthEnabled) && !Boolean.TRUE.equals(oauthEnabled)) {
            // NOTE: while we are already doing consistency checks we might as well cover this case; should not happen
            // as defaults are set in application.properties
            throw new IllegalArgumentException(
                    "No authentication scheme selected. Please decide if you want to use basic OR OAuth2 authentication.");
        }

        if (Boolean.TRUE.equals(basicAuthEnabled) && Boolean.TRUE.equals(oauthEnabled)) {
            throw new IllegalArgumentException(
                    "Too many authentication schemes selected. Please decide if you want to use basic OR OAuth2 authentication.");
        }
    }
}
