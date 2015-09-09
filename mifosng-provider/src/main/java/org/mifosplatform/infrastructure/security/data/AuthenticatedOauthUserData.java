/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.data;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.useradministration.data.RoleData;

/**
 * Immutable data object for authentication. Used in case of Oauth2.
 */
public class AuthenticatedOauthUserData {

    @SuppressWarnings("unused")
    private final String username;
    @SuppressWarnings("unused")
    private final Long userId;
    @SuppressWarnings("unused")
    private final String accessToken;
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

    public AuthenticatedOauthUserData(final String username, final Collection<String> permissions) {
        this.username = username;
        this.userId = null;
        this.accessToken = null;
        this.authenticated = false;
        this.officeId = null;
        this.officeName = null;
        this.staffId = null;
        this.staffDisplayName = null;
        this.organisationalRole = null;
        this.roles = null;
        this.permissions = permissions;
        this.shouldRenewPassword = false;
    }

    public AuthenticatedOauthUserData(final String username, final Long officeId, final String officeName, final Long staffId,
            final String staffDisplayName, final EnumOptionData organisationalRole, final Collection<RoleData> roles,
            final Collection<String> permissions, final Long userId, final String accessToken) {
        this.username = username;
        this.officeId = officeId;
        this.officeName = officeName;
        this.staffId = staffId;
        this.staffDisplayName = staffDisplayName;
        this.organisationalRole = organisationalRole;
        this.userId = userId;
        this.accessToken = accessToken;
        this.authenticated = true;
        this.roles = roles;
        this.permissions = permissions;
        this.shouldRenewPassword = false;
    }

    public AuthenticatedOauthUserData(final String username, final Long userId, final String accessToken) {
        this.username = username;
        this.officeId = null;
        this.officeName = null;
        this.staffId = null;
        this.staffDisplayName = null;
        this.organisationalRole = null;
        this.userId = userId;
        this.accessToken = accessToken;
        this.authenticated = true;
        this.roles = null;
        this.permissions = null;
        this.shouldRenewPassword = true;
    }
}