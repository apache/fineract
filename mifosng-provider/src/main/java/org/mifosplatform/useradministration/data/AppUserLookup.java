package org.mifosplatform.useradministration.data;

/**
 * Immutable data object for application user data.
 */
public class AppUserLookup {

    private final Long id;
    private final String username;

    public AppUserLookup(final Long id, final String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

}