package org.mifosplatform.audit.data;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.useradministration.data.AppUserLookup;

/**
 * Immutable data object representing client data.
 */
public final class AuditSearchData {

    @SuppressWarnings("unused")
    private final Collection<AppUserLookup> appUsers;
    @SuppressWarnings("unused")
    private final List<String> apiOperations;
    @SuppressWarnings("unused")
    private final List<String> resources;

    public AuditSearchData(final Collection<AppUserLookup> appUsers, final List<String> apiOperations, final List<String> resources) {
        this.appUsers = appUsers;
        this.apiOperations = apiOperations;
        this.resources = resources;
    }
}