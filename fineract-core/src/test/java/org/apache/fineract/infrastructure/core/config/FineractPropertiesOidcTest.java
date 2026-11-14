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

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties;
import org.junit.jupiter.api.Test;

class FineractPropertiesOidcTest {

    @Test
    void defaultEnabledIsFalse() {
        assertThat(new FineractSecurityOidcFederationProperties().isEnabled()).isFalse();
    }

    @Test
    void defaultUsernameClaimIsPreferredUsername() {
        assertThat(new FineractSecurityOidcFederationProperties().getUsernameClaim()).isEqualTo("preferred_username");
    }

    @Test
    void defaultTenantClaimName() {
        assertThat(new FineractSecurityOidcFederationProperties().getTenantClaimName()).isEqualTo("fineract_tenant");
    }

    @Test
    void defaultAutoCreateUserIsFalse() {
        assertThat(new FineractSecurityOidcFederationProperties().isAutoCreateUser()).isFalse();
    }

    @Test
    void defaultProviderIsGeneric() {
        assertThat(new FineractSecurityOidcFederationProperties().getProvider().getCode()).isEqualTo("generic");
    }

    @Test
    void defaultDefaultRolesIsEmpty() {
        assertThat(new FineractSecurityOidcFederationProperties().getDefaultRoles()).isEmpty();
    }

    @Test
    void defaultAudienceIsEmpty() {
        assertThat(new FineractSecurityOidcFederationProperties().getAudience()).isEmpty();
    }

    @Test
    void defaultPostLogoutRedirectUriIsNull() {
        assertThat(new FineractSecurityOidcFederationProperties().getPostLogoutRedirectUri()).isNull();
    }
}
