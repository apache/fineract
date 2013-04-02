/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.data;

import java.util.Collection;
import java.util.List;

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