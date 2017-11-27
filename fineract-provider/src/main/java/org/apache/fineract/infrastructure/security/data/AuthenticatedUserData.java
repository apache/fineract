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
package org.apache.fineract.infrastructure.security.data;

import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.useradministration.data.RoleData;

/**
 * Immutable data object for authentication.
 */
public class AuthenticatedUserData {

    @SuppressWarnings("unused")
    private final String username;
    @SuppressWarnings("unused")
    private final Long userId;
    @SuppressWarnings("unused")
    private final String base64EncodedAuthenticationKey;
    @SuppressWarnings("unused")
    private final boolean authenticated;
    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final String officeName;
    @SuppressWarnings("unused")
    private final Long staffId;
    @SuppressWarnings("unused")
    private final String staffDisplayName;
    @SuppressWarnings("unused")
    private final EnumOptionData organisationalRole;
    @SuppressWarnings("unused")
    private final Collection<RoleData> roles;
    @SuppressWarnings("unused")
    private final Collection<String> permissions;

    @SuppressWarnings("unused")
    private final boolean shouldRenewPassword;

    @SuppressWarnings("unused")
    private final boolean isTwoFactorAuthenticationRequired;

    public AuthenticatedUserData(final String username, final Collection<String> permissions) {
        this.username = username;
        this.userId = null;
        this.base64EncodedAuthenticationKey = null;
        this.authenticated = false;
        this.officeId = null;
        this.officeName = null;
        this.staffId = null;
        this.staffDisplayName = null;
        this.organisationalRole = null;
        this.roles = null;
        this.permissions = permissions;
        this.shouldRenewPassword = false;
        this.isTwoFactorAuthenticationRequired = false;
    }

    public AuthenticatedUserData(final String username, final Long officeId, final String officeName, final Long staffId,
            final String staffDisplayName, final EnumOptionData organisationalRole, final Collection<RoleData> roles,
            final Collection<String> permissions, final Long userId, final String base64EncodedAuthenticationKey,
            final boolean isTwoFactorAuthenticationRequired) {
        this.username = username;
        this.officeId = officeId;
        this.officeName = officeName;
        this.staffId = staffId;
        this.staffDisplayName = staffDisplayName;
        this.organisationalRole = organisationalRole;
        this.userId = userId;
        this.base64EncodedAuthenticationKey = base64EncodedAuthenticationKey;
        this.authenticated = true;
        this.roles = roles;
        this.permissions = permissions;
        this.shouldRenewPassword = false;
        this.isTwoFactorAuthenticationRequired = isTwoFactorAuthenticationRequired;
    }

    public AuthenticatedUserData(final String username, final Long userId, final String base64EncodedAuthenticationKey,
             final boolean isTwoFactorAuthenticationRequired) {
        this.username = username;
        this.officeId = null;
        this.officeName = null;
        this.staffId = null;
        this.staffDisplayName = null;
        this.organisationalRole = null;
        this.userId = userId;
        this.base64EncodedAuthenticationKey = base64EncodedAuthenticationKey;
        this.authenticated = true;
        this.roles = null;
        this.permissions = null;
        this.shouldRenewPassword = true;
        this.isTwoFactorAuthenticationRequired = isTwoFactorAuthenticationRequired;
    }
}