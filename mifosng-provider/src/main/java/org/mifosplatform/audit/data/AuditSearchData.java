package org.mifosplatform.audit.data;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.audit.api.ProcessingResultLookup;
import org.mifosplatform.useradministration.data.AppUserData;

/**
 * Immutable data object representing audit search results.
 */
public final class AuditSearchData {

    @SuppressWarnings("unused")
    private final Collection<AppUserData> appUsers;
    @SuppressWarnings("unused")
    private final List<String> actionNames;
    @SuppressWarnings("unused")
    private final List<String> entityNames;
    @SuppressWarnings("unused")
    private final Collection<ProcessingResultLookup> processingResults;

    public AuditSearchData(final Collection<AppUserData> appUsers, final List<String> apiOperations, final List<String> resources,
            final Collection<ProcessingResultLookup> processingResults) {
        this.appUsers = appUsers;
        this.actionNames = apiOperations;
        this.entityNames = resources;
        this.processingResults = processingResults;
    }
}