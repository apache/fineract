package org.mifosplatform.infrastructure.security.data;

import java.util.Collection;

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
    private final Collection<String> permissions;

    public AuthenticatedUserData(final String username, final Collection<String> permissions) {
        this.username = username;
        this.userId = null;
        this.base64EncodedAuthenticationKey = null;
        this.authenticated = false;
        this.permissions = permissions;
    }

    public AuthenticatedUserData(final String username, final Collection<String> permissions, final Long userId,
            final String base64EncodedAuthenticationKey) {
        this.username = username;
        this.userId = userId;
        this.base64EncodedAuthenticationKey = base64EncodedAuthenticationKey;
        this.authenticated = true;
        this.permissions = permissions;
    }
}